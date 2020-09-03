package com.jhipster.freebox.api.authentication.dto

import java.io.Serializable

data class AuthorizeResponse(override val success: Boolean, override val result: AuthorizeReponseData) : FreeboxResponse<AuthorizeResponse.AuthorizeReponseData>() {
    data class AuthorizeReponseData(val appToken: String, val trackId: Int) : Serializable
}
