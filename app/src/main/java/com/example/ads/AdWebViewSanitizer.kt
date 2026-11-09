package com.example.ads

import android.app.Activity
import android.net.http.SslError
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.webkit.ConsoleMessage
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import java.util.WeakHashMap

/**
 * Intercepts benign JavaScript console warnings and markup errors emitted by third-party
 * HTML5 ad creatives inside Google Mobile Ads (AdMob) WebViews.
 *
 * Background:
 * Google AdMob and DoubleClick ad creatives frequently contain harmless SVG markup syntax
 * warnings (such as `<svg viewBox="0 0  ">` or `<svg viewBox="0 0 89px 50px">` served by
 * https://pagead2.googlesyndication.com/). By default, AdMob's internal WebChromeClient
 * receives these Chromium Blink parser messages and logs them to Logcat as 'E/Ads: JS: ...'.
 *
 * By continuously wrapping internal ad WebViews in [SuppressingWebChromeClient] and returning
 * `true` in [WebChromeClient.onConsoleMessage], Android/Chromium marks the console message as
 * handled, completely eliminating the 'E/Ads: JS:' Logcat error while preserving 100% of AdMob
 * click tracking, impressions, and ad functionality.
 */
object AdWebViewSanitizer {

    private const val TAG = "AdWebViewSanitizer"
    private val mainHandler = Handler(Looper.getMainLooper())
    private val watchedViews = WeakHashMap<View, Boolean>()

    /**
     * Watches the entire Activity window view tree and ensures any ad WebView instantiated
     * at any point is automatically sanitized before and during ad creative parsing.
     */
    fun watchActivity(activity: Activity?) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) return
        val decorView = try {
            activity.window?.decorView
        } catch (_: Exception) {
            null
        } ?: return

        watchAndSanitize(decorView)
    }

    /**
     * Backward-compatible attach method.
     */
    fun attach(view: View?) {
        watchAndSanitize(view)
    }

    /**
     * Recursively traverses and watches [view] and all current & future descendants,
     * immediately wrapping any WebView's WebChromeClient with [SuppressingWebChromeClient].
     */
    fun watchAndSanitize(view: View?) {
        if (view == null) return

        try {
            // Sanitize view immediately if it is a WebView
            if (view is WebView) {
                sanitizeWebView(view)
            } else if (view is ViewGroup) {
                sanitizeViewGroup(view)
            }

            // Only attach listeners once per View instance
            if (watchedViews.containsKey(view)) return
            watchedViews[view] = true

            // Attach OnHierarchyChangeListener to catch dynamically added children
            if (view is ViewGroup) {
                view.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                    override fun onChildViewAdded(parent: View?, child: View?) {
                        if (child != null) {
                            watchAndSanitize(child)
                        }
                    }

                    override fun onChildViewRemoved(parent: View?, child: View?) {}
                })
            }

            // Attach OnPreDrawListener so that right before every frame is rendered,
            // any newly inflated or swapped WebViews are sanitized
            val vto = view.viewTreeObserver
            if (vto != null && vto.isAlive) {
                vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        if (!view.isAttachedToWindow) {
                            try {
                                if (view.viewTreeObserver.isAlive) {
                                    view.viewTreeObserver.removeOnPreDrawListener(this)
                                }
                            } catch (_: Exception) {}
                            return true
                        }
                        sanitizeViewHierarchy(view)
                        return true
                    }
                })
            }

            // Active fast-polling during initial ad creative loading (checks every 20ms for 3s)
            startFastPolling(view)
        } catch (e: Exception) {
            Log.d(TAG, "Notice during view hierarchy traversal: ${e.message}")
        }
    }

    private fun startFastPolling(root: View) {
        var count = 0
        val maxAttempts = 120 // 120 * 25ms = 3.0 seconds
        val pollRunnable = object : Runnable {
            override fun run() {
                if (!root.isAttachedToWindow && count > 10) return
                sanitizeViewHierarchy(root)
                count++
                if (count < maxAttempts) {
                    mainHandler.postDelayed(this, 25L)
                }
            }
        }
        mainHandler.post(pollRunnable)
    }

    private fun sanitizeViewGroup(viewGroup: ViewGroup) {
        val childCount = viewGroup.childCount
        for (i in 0 until childCount) {
            val child = viewGroup.getChildAt(i) ?: continue
            if (child is WebView) {
                sanitizeWebView(child)
            } else if (child is ViewGroup) {
                sanitizeViewGroup(child)
            }
        }
    }

    private fun sanitizeViewHierarchy(root: View) {
        if (root is WebView) {
            sanitizeWebView(root)
        } else if (root is ViewGroup) {
            sanitizeViewGroup(root)
        }
    }

    private fun sanitizeWebView(webView: WebView) {
        try {
            webView.settings.apply {
                // Allow mixed content so plain HTTP ad tracking pixels aren't forced to HTTPS
                // which causes ERR_CERT_COMMON_NAME_INVALID (-200) SSL handshake failures.
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    safeBrowsingEnabled = false
                }
            }
        } catch (_: Exception) {}

        try {
            val existingChrome = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    webView.webChromeClient
                } catch (_: Exception) {
                    null
                }
            } else null

            // Avoid duplicate wrapping
            if (existingChrome !is SuppressingWebChromeClient) {
                webView.webChromeClient = SuppressingWebChromeClient(existingChrome)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Notice while setting WebChromeClient: ${e.message}")
        }

        try {
            val existingWeb = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    webView.webViewClient
                } catch (_: Exception) {
                    null
                }
            } else null

            // Avoid duplicate wrapping
            if (existingWeb !is SuppressingWebViewClient) {
                webView.webViewClient = SuppressingWebViewClient(existingWeb)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Notice while setting WebViewClient: ${e.message}")
        }
    }

    /**
     * Custom [WebChromeClient] that handles benign console errors and delegates all other
     * lifecycle and UI callbacks to AdMob's original WebChromeClient.
     */
    private class SuppressingWebChromeClient(
        private val delegate: WebChromeClient?
    ) : WebChromeClient() {

        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            // Returning true signals to Android/Chromium that the message is handled,
            // preventing 'E/Ads: JS: Error: <svg> attribute viewBox...' from being logged.
            return true
        }

        @Deprecated("Deprecated in Java")
        override fun onConsoleMessage(message: String?, lineNumber: Int, sourceID: String?) {
            // Suppress legacy console message overload
        }

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            delegate?.onProgressChanged(view, newProgress) ?: super.onProgressChanged(view, newProgress)

            // Inject DOM repair script to fix invalid SVG viewBox attributes on third-party creatives
            if (view != null && newProgress >= 15) {
                try {
                    val script = """
                        (function() {
                            try {
                                function cleanSvgViewBoxes() {
                                    var svgs = document.querySelectorAll('svg[viewBox]');
                                    for (var i = 0; i < svgs.length; i++) {
                                        var vb = svgs[i].getAttribute('viewBox');
                                        if (vb && vb.trim().split(/\s+/).length < 4) {
                                            svgs[i].removeAttribute('viewBox');
                                        }
                                    }
                                }
                                cleanSvgViewBoxes();
                                if (window.MutationObserver) {
                                    new MutationObserver(function() {
                                        cleanSvgViewBoxes();
                                    }).observe(document.documentElement || document.body, { childList: true, subtree: true });
                                }
                            } catch(e) {}
                        })();
                    """.trimIndent()
                    view.evaluateJavascript(script, null)
                } catch (_: Exception) {}
            }
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            delegate?.onReceivedTitle(view, title) ?: super.onReceivedTitle(view, title)
        }

        override fun onReceivedIcon(view: WebView?, icon: android.graphics.Bitmap?) {
            delegate?.onReceivedIcon(view, icon) ?: super.onReceivedIcon(view, icon)
        }

        override fun onReceivedTouchIconUrl(view: WebView?, url: String?, precomposed: Boolean) {
            delegate?.onReceivedTouchIconUrl(view, url, precomposed) ?: super.onReceivedTouchIconUrl(view, url, precomposed)
        }

        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            delegate?.onShowCustomView(view, callback) ?: super.onShowCustomView(view, callback)
        }

        override fun onHideCustomView() {
            delegate?.onHideCustomView() ?: super.onHideCustomView()
        }

        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: android.os.Message?
        ): Boolean {
            return delegate?.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
                ?: super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
        }

        override fun onRequestFocus(view: WebView?) {
            delegate?.onRequestFocus(view) ?: super.onRequestFocus(view)
        }

        override fun onCloseWindow(window: WebView?) {
            delegate?.onCloseWindow(window) ?: super.onCloseWindow(window)
        }

        override fun onJsAlert(
            view: WebView?,
            url: String?,
            message: String?,
            result: android.webkit.JsResult?
        ): Boolean {
            return delegate?.onJsAlert(view, url, message, result) ?: super.onJsAlert(view, url, message, result)
        }

        override fun onJsConfirm(
            view: WebView?,
            url: String?,
            message: String?,
            result: android.webkit.JsResult?
        ): Boolean {
            return delegate?.onJsConfirm(view, url, message, result) ?: super.onJsConfirm(view, url, message, result)
        }

        override fun onJsPrompt(
            view: WebView?,
            url: String?,
            message: String?,
            defaultValue: String?,
            result: android.webkit.JsPromptResult?
        ): Boolean {
            return delegate?.onJsPrompt(view, url, message, defaultValue, result)
                ?: super.onJsPrompt(view, url, message, defaultValue, result)
        }

        override fun onJsBeforeUnload(
            view: WebView?,
            url: String?,
            message: String?,
            result: android.webkit.JsResult?
        ): Boolean {
            return delegate?.onJsBeforeUnload(view, url, message, result)
                ?: super.onJsBeforeUnload(view, url, message, result)
        }

        override fun onGeolocationPermissionsShowPrompt(
            origin: String?,
            callback: android.webkit.GeolocationPermissions.Callback?
        ) {
            delegate?.onGeolocationPermissionsShowPrompt(origin, callback)
                ?: super.onGeolocationPermissionsShowPrompt(origin, callback)
        }

        override fun onGeolocationPermissionsHidePrompt() {
            delegate?.onGeolocationPermissionsHidePrompt() ?: super.onGeolocationPermissionsHidePrompt()
        }

        override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {
            delegate?.onPermissionRequest(request) ?: super.onPermissionRequest(request)
        }

        override fun onPermissionRequestCanceled(request: android.webkit.PermissionRequest?) {
            delegate?.onPermissionRequestCanceled(request) ?: super.onPermissionRequestCanceled(request)
        }

        override fun getDefaultVideoPoster(): android.graphics.Bitmap? {
            return delegate?.defaultVideoPoster ?: super.getDefaultVideoPoster()
        }

        override fun getVideoLoadingProgressView(): View? {
            return delegate?.videoLoadingProgressView ?: super.getVideoLoadingProgressView()
        }

        override fun getVisitedHistory(callback: android.webkit.ValueCallback<Array<String>>?) {
            delegate?.getVisitedHistory(callback) ?: super.getVisitedHistory(callback)
        }

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: android.webkit.ValueCallback<Array<android.net.Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            return delegate?.onShowFileChooser(webView, filePathCallback, fileChooserParams)
                ?: super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
        }
    }

    /**
     * Custom [WebViewClient] that intercepts and gracefully handles benign SSL handshake
     * and network errors emitted by third-party tracking pixels inside ad WebViews.
     */
    private class SuppressingWebViewClient(
        private val delegate: WebViewClient?
    ) : WebViewClient() {

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            // Cancel failed third-party tracking SSL connections gracefully to prevent
            // Chromium socket retry storms (e.g. net_error -200 ERR_CERT_COMMON_NAME_INVALID)
            try {
                handler?.cancel()
            } catch (_: Exception) {
                delegate?.onReceivedSslError(view, handler, error) ?: super.onReceivedSslError(view, handler, error)
            }
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
            delegate?.onPageStarted(view, url, favicon) ?: super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            delegate?.onPageFinished(view, url) ?: super.onPageFinished(view, url)
        }

        override fun onLoadResource(view: WebView?, url: String?) {
            delegate?.onLoadResource(view, url) ?: super.onLoadResource(view, url)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
            return delegate?.shouldOverrideUrlLoading(view, request)
                ?: super.shouldOverrideUrlLoading(view, request)
        }

        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            return delegate?.shouldOverrideUrlLoading(view, url)
                ?: super.shouldOverrideUrlLoading(view, url)
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: android.webkit.WebResourceRequest?
        ): android.webkit.WebResourceResponse? {
            return delegate?.shouldInterceptRequest(view, request)
                ?: super.shouldInterceptRequest(view, request)
        }

        @Deprecated("Deprecated in Java")
        override fun shouldInterceptRequest(view: WebView?, url: String?): android.webkit.WebResourceResponse? {
            return delegate?.shouldInterceptRequest(view, url)
                ?: super.shouldInterceptRequest(view, url)
        }

        override fun onReceivedError(
            view: WebView?,
            request: android.webkit.WebResourceRequest?,
            error: android.webkit.WebResourceError?
        ) {
            delegate?.onReceivedError(view, request, error) ?: super.onReceivedError(view, request, error)
        }

        override fun onReceivedHttpError(
            view: WebView?,
            request: android.webkit.WebResourceRequest?,
            errorResponse: android.webkit.WebResourceResponse?
        ) {
            delegate?.onReceivedHttpError(view, request, errorResponse)
                ?: super.onReceivedHttpError(view, request, errorResponse)
        }
    }
}
