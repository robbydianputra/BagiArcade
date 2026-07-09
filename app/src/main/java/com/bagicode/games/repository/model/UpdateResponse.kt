package com.bagicode.games.repository.model

import com.bagicode.games.update.model.UpdateData
import com.google.gson.annotations.SerializedName

data class UpdateResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: UpdateDataResponse?
)

data class UpdateDataResponse(
    @SerializedName("latest_version") val latestVersion: String?,
    @SerializedName("build_number") val buildNumber: Int?,
    @SerializedName("url") val url: String?,
    @SerializedName("url_fallback") val urlFallback: String?,
    @SerializedName("is_force_update") val isForceUpdate: Boolean?,
    @SerializedName("changelog") val changelog: List<String>?
) {
    fun mappingToUi(): UpdateData = UpdateData(
        latestVersion = this.latestVersion.orEmpty(),
        buildNumber = this.buildNumber ?: 1,
        url = this.url.orEmpty(),
        urlFallback = this.urlFallback.orEmpty(),
        isForceUpdate = this.isForceUpdate ?: false,
        changelog = this.changelog ?: listOf()
    )
}
