package com.jhipster.freebox.api.authentication.request

import com.jhipster.freebox.api.authentication.dto.*
import feign.Headers
import feign.Param
import feign.RequestLine
import reactor.core.publisher.Mono

interface FreeboxApi {
    @RequestLine("POST /login/authorize/")
    @Headers("Content-Type: application/json")
    fun loginRequestAuthorization(token: TokenRequest): Mono<AuthorizeResponse>

    @RequestLine("GET /login/authorize/{trackId}")
    @Headers("Content-Type: application/json")
    fun loginTrackRequestAuthorization(@Param("trackId") trackId: Int): Mono<TrackAuthorizeResponse>

    @RequestLine("GET /login/")
    @Headers("Content-Type: application/json")
    fun loginChallengeValue(): Mono<LoginChallengeValueResponse>

    @RequestLine("POST /login/session/")
    @Headers("Content-Type: application/json")
    fun loginOpenSession(openSessionRequest: OpenSessionRequest): Mono<OpenSessionResponse>
}
