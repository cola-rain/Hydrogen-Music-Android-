package com.hydrogen.padzero.network

sealed class NetworkActionResult {
    data object Disabled : NetworkActionResult()
    data class Message(val text: String) : NetworkActionResult()
}

class NeteaseApiStub {
    fun login(): NetworkActionResult = NetworkActionResult.Disabled
    fun sync(): NetworkActionResult = NetworkActionResult.Disabled
    fun search(keyword: String): NetworkActionResult = NetworkActionResult.Message("联网搜索入口已保留：$keyword")
}
