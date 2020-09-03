package com.jhipster.freebox.repository

import com.jhipster.freebox.api.authentication.dto.AuthorizeResponse
import com.jhipster.freebox.api.authentication.dto.TokenRequest
import reactor.core.publisher.Mono

interface FreeboxRepository {
    fun saveAuthorizeReponse(authorizeReponseData: AuthorizeResponse.AuthorizeReponseData): Mono<Void>
    fun findAuthorizeReponse(): Mono<AuthorizeResponse.AuthorizeReponseData>
    fun saveTokenRequest(tokenRequest: TokenRequest): Mono<Void>
    fun findTokenRequest(): Mono<TokenRequest>
    fun resetTokenRequest(): Mono<Void>
}
