package com.xiaominote.app.data.sync

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubApi {

    /** List the contents of a directory (the notes/ folder). */
    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun listContents(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Query("ref") branch: String,
    ): Response<List<GithubContent>>

    /** Download a single note file metadata + base64 content. */
    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getContent(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Query("ref") branch: String,
    ): Response<GithubContent>

    /** Create or update a file. Pass [sha] when updating an existing file. */
    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun putContent(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Body request: GithubPutRequest,
    ): Response<GithubPutResponse>

    /** Delete a file (requires its sha). */
    @HTTP(method = "DELETE", path = "repos/{owner}/{repo}/contents/{path}", hasBody = true)
    suspend fun deleteContent(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Body request: GithubDeleteRequest,
    ): Response<Unit>

    /** Verify the token works and returns the authenticated user. */
    @GET("user")
    suspend fun getUser(@Header("Authorization") auth: String): Response<GithubUser>
}

data class GithubContent(
    val name: String,
    val path: String,
    val sha: String,
    val type: String,
    val content: String? = null,
    @SerializedName("encoding") val encoding: String? = null,
)

data class GithubPutRequest(
    val message: String,
    val content: String,        // base64-encoded
    val sha: String? = null,    // required when updating
    val branch: String,
)

data class GithubPutResponse(
    val content: GithubContent? = null,
)

data class GithubDeleteRequest(
    val message: String,
    val sha: String,
    val branch: String,
)

data class GithubUser(
    val login: String,
    val name: String? = null,
)
