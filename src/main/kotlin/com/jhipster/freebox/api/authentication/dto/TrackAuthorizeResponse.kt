package com.jhipster.freebox.api.authentication.dto

data class TrackAuthorizeResponse(override val success: Boolean, override val result: TrackAuthorizeReponseData) : FreeboxResponse<TrackAuthorizeResponse.TrackAuthorizeReponseData>() {
    data class TrackAuthorizeReponseData(val status: AuthorizationStatus, val challenge: String, val passwordSalt: String) {
        enum class AuthorizationStatus {
            unknown, pending, timeout, granted, denied
        }
    }
}
