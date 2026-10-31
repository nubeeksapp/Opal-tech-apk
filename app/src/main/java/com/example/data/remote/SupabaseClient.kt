package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object SupabaseClient {

    private const val TAG = "SupabaseClient"

    private const val LIVE_SUPABASE_URL = "https://fabjwphsotffrmofwksr.supabase.co"
    private const val LIVE_ANON_KEY = "sb_publishable_M-1gqzd47xis9gHTTHXT7A_GkQlPJnt"

    val supabaseUrl: String = BuildConfig.SUPABASE_URL.takeIf {
        it.isNotBlank() && !it.contains("your-project", ignoreCase = true)
    } ?: LIVE_SUPABASE_URL

    val supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY.takeIf {
        it.isNotBlank() && !it.contains("your-anon-key", ignoreCase = true)
    } ?: LIVE_ANON_KEY

    val isConfigured: Boolean by lazy {
        supabaseUrl.isNotBlank() &&
            supabaseAnonKey.isNotBlank() &&
            !supabaseUrl.contains("your-project", ignoreCase = true) &&
            !supabaseAnonKey.contains("your-anon-key", ignoreCase = true)
    }

    val api: SupabaseApi? by lazy {
        if (!isConfigured) {
            Log.w(TAG, "Supabase credentials are not configured or using placeholders. Offline fallback will be used.")
            null
        } else {
            try {
                createApi(supabaseUrl, supabaseAnonKey)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Supabase Retrofit client", e)
                null
            }
        }
    }

    private fun createApi(rawUrl: String, apiKey: String): SupabaseApi {
        val trimmed = rawUrl.trim()
        val normalizedUrl = if (trimmed.endsWith("/")) trimmed else "$trimmed/"
        val baseUrl = if (normalizedUrl.contains("/rest/v1/")) normalizedUrl else "${normalizedUrl}rest/v1/"

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("apikey", apiKey.trim())
                    .header("Authorization", "Bearer ${apiKey.trim()}")
                    .header("Accept", "application/json")
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(SupabaseApi::class.java)
    }
}
