package com.jhipster.freebox.api.authentication.dto

data class OpenSessionResponse(override val success: Boolean, override val result: OpenSessionReponseData) : FreeboxResponse<OpenSessionResponse.OpenSessionReponseData>() {
    data class OpenSessionReponseData(val sessionToken: String, val challenge: String, val passwordSalt: String, val passwordSet: String, val permissions: Permissions) {
        data class Permissions(
            val parental: Boolean = false,
            val contacts: Boolean = false,
            val explorer: Boolean = false,
            val tv: Boolean = false,
            val wdo: Boolean = false,
            val downloader: Boolean = false,
            val profile: Boolean = false,
            val camera: Boolean = false,
            val settings: Boolean = false,
            val calls: Boolean = false,
            val home: Boolean = false,
            val pvr: Boolean = false,
            val vm: Boolean = false,
            val player: Boolean = false
        )
    }
}
