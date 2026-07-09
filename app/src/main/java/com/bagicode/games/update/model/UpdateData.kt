package com.bagicode.games.update.model

data class UpdateData(
    val latestVersion: String = "",
    val buildNumber: Int = 0,
    val url: String = "",          // Contoh: "market://details?id=com.package.namakamu"
    val urlFallback: String = "",  // Contoh: "https://google.com"
    val isForceUpdate: Boolean = false,
    val changelog: List<String> = listOf()
)