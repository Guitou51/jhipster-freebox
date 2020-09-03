package com.jhipster.freebox.service

import com.jhipster.freebox.api.authentication.dto.AuthorizeResponse
import com.jhipster.freebox.api.authentication.dto.OpenSessionRequest
import com.jhipster.freebox.api.authentication.dto.OpenSessionResponse
import com.jhipster.freebox.api.authentication.dto.TokenRequest
import com.jhipster.freebox.api.authentication.request.FreeboxApi
import com.jhipster.freebox.repository.FreeboxRepositoryMapDb
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class FreeboxService(val freeboxApi: FreeboxApi, val freeboxRepositoryMapDb: FreeboxRepositoryMapDb) {

    fun callRequestAuthorization(@RequestBody request: TokenRequest): Mono<AuthorizeResponse> {
        return freeboxApi.loginRequestAuthorization(request)
            .filter { it.success }
            .switchIfEmpty { Mono.error(RuntimeException("RequestAuthorization not success")) }
            .flatMap {
                freeboxRepositoryMapDb.saveTokenRequest(request)
                    .then(freeboxRepositoryMapDb.saveAuthorizeReponse(it.result))
                    .thenReturn(it)
            }
    }

    fun findTokenRequest(): Mono<TokenRequest> {
        return freeboxRepositoryMapDb.findTokenRequest()
    }

    fun findAuthorizeReponse(): Mono<AuthorizeResponse.AuthorizeReponseData> {
        return freeboxRepositoryMapDb.findAuthorizeReponse()
    }

    fun encodePassword(appToken: String, challenge: String): String {
        return HmacUtils(HmacAlgorithms.HMAC_SHA_1, appToken).hmacHex(challenge)
    }

    fun login(): Mono<OpenSessionResponse.OpenSessionReponseData> {
        return findAuthorizeReponse()
            .switchIfEmpty(Mono.error(RuntimeException("need call requestAuthorization before")))
            .map { it.appToken }.flatMap { appToken ->
                findTokenRequest().flatMap { token ->
                    freeboxApi.loginChallengeValue().flatMap { loginChallenge ->
                        val hmacHex: String = encodePassword(appToken, loginChallenge.result.challenge)
                        freeboxApi.loginOpenSession(OpenSessionRequest(token.appId, hmacHex))
                            .filter { it.success }
                            .switchIfEmpty(Mono.error(RuntimeException("open session not success")))
                            .map { it.result }
                    }
                }
            }
    }
}
