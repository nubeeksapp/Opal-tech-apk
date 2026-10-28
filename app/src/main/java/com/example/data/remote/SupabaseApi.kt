package com.example.data.remote

import com.example.data.remote.dto.SupabaseCategoryDto
import com.example.data.remote.dto.SupabasePostDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {

    @GET("categories")
    suspend fun getCategories(
        @Query("select") select: String = "*",
        @Query("order") order: String? = "order.asc"
    ): List<SupabaseCategoryDto>

    @GET("categories")
    suspend fun getCategoriesRaw(
        @Query("select") select: String = "*"
    ): List<SupabaseCategoryDto>

    @GET("posts")
    suspend fun getPosts(
        @Query("select") select: String = "*",
        @Query("order") order: String = "publish_date.desc"
    ): List<SupabasePostDto>

    @GET("posts")
    suspend fun getPostsRaw(
        @Query("select") select: String = "*"
    ): List<SupabasePostDto>

    @GET("posts")
    suspend fun getPostsByCategory(
        @Query(value = "category_id", encoded = true) categoryFilter: String,
        @Query("select") select: String = "*",
        @Query("order") order: String? = null
    ): List<SupabasePostDto>

    // Alternate table name 'articles' in case admin panel creates 'articles' instead of 'posts'
    @GET("articles")
    suspend fun getArticles(
        @Query("select") select: String = "*",
        @Query("order") order: String = "publish_date.desc"
    ): List<SupabasePostDto>

    @GET("articles")
    suspend fun getArticlesRaw(
        @Query("select") select: String = "*"
    ): List<SupabasePostDto>

    @GET("articles")
    suspend fun getArticlesByCategory(
        @Query(value = "category_id", encoded = true) categoryFilter: String,
        @Query("select") select: String = "*",
        @Query("order") order: String? = null
    ): List<SupabasePostDto>

    @POST("rpc/increment_view_count")
    suspend fun incrementViewCountRpc(
        @Body body: Map<String, String>
    ): Response<Unit>

    @PATCH("posts")
    suspend fun updatePostViewCount(
        @Query(value = "id", encoded = true) idFilter: String,
        @Body body: Map<String, Long>
    ): Response<Unit>

    @PATCH("articles")
    suspend fun updateArticleViewCount(
        @Query(value = "id", encoded = true) idFilter: String,
        @Body body: Map<String, Long>
    ): Response<Unit>
}
