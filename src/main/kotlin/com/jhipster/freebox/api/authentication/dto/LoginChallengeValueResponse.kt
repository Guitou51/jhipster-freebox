package com.jhipster.freebox.api.authentication.dto

data class LoginChallengeValueResponse(override val success: Boolean, override val result: LoginChallengeValueData) : FreeboxResponse<LoginChallengeValueResponse.LoginChallengeValueData>() {
    data class LoginChallengeValueData(val loggedIn: Boolean, val challenge: String, val passwordSalt: String, val passwordSet: Boolean = false)
}
