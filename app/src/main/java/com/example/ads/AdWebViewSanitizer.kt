package com.example.ads

import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView

/**
 * Intercepts benign JavaScript console warnings and errors emitted by third-party
 * HTML5 ad creatives inside Google Mobile Ads (AdMob) WebViews.
 *
 * Background:
 * Google DoubleClick and gstatic test/production ad creatives frequently contain
 * harmless script or SVG markup warnings (such as `<svg viewBox="0 0 89px 50px">`
 * or missing optional video addon callbacks). By default, Chromium WebViews pass
 * unhandled console messages to Logcat with priority ERROR and tag 'Ads'.
 *
 * By overriding [WebChromeClient.onConsoleMessage] and returning `true`, the host
 * application signals that the message is handled, preventing Chromium from logging
 * 'E/Ads: JS:' messages to Logcat while preserving complete ad functionality, click
 * tracking, and impressions.
 */
object AdWebViewSanitizer {

    private const val TAG = "AdWebViewSanitizer"

    fun attach(view: View?) {
        if (view == null) return

        try {
            if (view is WebView) {
                sanitizeWebView(view)
                return
            }

            if (view is ViewGroup) {
                // Intercept any newly added child views (e.g. AdMob dynamically inflating WebViews)
                view.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                    override fun onChildViewAdded(parent: View?, child: View?) {
                        if (child != null) {
                            attach(child)
                        }
                    }

                    override fun onChildViewRemoved(parent: View?, child: View?) {}
                })

                for (i in 0 until view.childCount) {
                    attach(view.getChildAt(i))
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Notice during view hierarchy traversal: ${e.message}")
        }
    }

    private fun sanitizeWebView(webView: WebView) {
        try {
            val existingClient = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    webView.webChromeClient
                } catch (_: Exception) {
                    null
                }
            } else null

            // Avoid duplicate wrapping
            if (existingClient is SuppressingWebChromeClient) return

            webView.webChromeClient = SuppressingWebChromeClient(existingClient)
        } catch (e: Exception) {
            Log.d(TAG, "Notice while setting WebChromeClient: ${e.message}")
        }
    }

    /**
     * Custom [WebChromeClient] that consumes console messages and delegates all other
     * lifecycle and UI callbacks to the delegate if one existed.
     */
    private class SuppressingWebChromeClient(
        private val delegate: WebChromeClient?
    ) : WebChromeClient() {

        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            // Returning true tells Android/Chromium that the console message has been
            // handled, preventing it from printing 'E/Ads : JS: ...' to logcat.
            return true
        }

        @Deprecated("Deprecated in Java")
        override fun onConsoleMessage(message: String?, lineNumber: Int, sourceID: String?) {
            // Suppress legacy console message overload
        }

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            delegate?.onProgressChanged(view, newProgress) ?: super.onProgressChanged(view, newProgress)
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
}
