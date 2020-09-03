package com.jhipster.freebox.web.rest

import com.jhipster.freebox.api.authentication.dto.*
import com.jhipster.freebox.api.authentication.dto.TrackAuthorizeResponse.TrackAuthorizeReponseData
import com.jhipster.freebox.api.authentication.request.FreeboxApi
import com.jhipster.freebox.service.FreeboxService
import com.tyro.oss.arbitrater.arbitraryInstance
import java.lang.RuntimeException
import java.time.Duration
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Hooks
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockitoExtension::class)
internal class FreeboxControllerTest {
    @Mock
    lateinit var freeboxApi: FreeboxApi

    @Mock
    lateinit var freeboxService: FreeboxService

    @InjectMocks
    lateinit var freeboxController: FreeboxController

    private val pending = TrackAuthorizeResponse(true, TrackAuthorizeReponseData(TrackAuthorizeReponseData.AuthorizationStatus.pending, "", ""))
    private val denied = TrackAuthorizeResponse(true, TrackAuthorizeReponseData(TrackAuthorizeReponseData.AuthorizationStatus.denied, "", ""))
    private val granted = TrackAuthorizeResponse(true, TrackAuthorizeReponseData(TrackAuthorizeReponseData.AuthorizationStatus.granted, "", ""))

    @Test
    fun requestAuthorization_success() {
        Hooks.onOperatorDebug()
        val token: TokenRequest = TokenRequest::class.arbitraryInstance()
        val authorizeReponseData = AuthorizeResponse.AuthorizeReponseData("", 1)
        `when`(freeboxService.callRequestAuthorization(token))
            .thenReturn(Mono.just(AuthorizeResponse(true, authorizeReponseData)))

        StepVerifier.create(freeboxController.requestAuthorization(token))
            .expectNext(1)
            .expectComplete()
            .verify(Duration.ofSeconds(10))
    }

    @Test
    fun requestAuthorization_failed() {
        Hooks.onOperatorDebug()
        val token: TokenRequest = TokenRequest::class.arbitraryInstance()
        `when`(freeboxService.callRequestAuthorization(token))
            .thenReturn(Mono.error(RuntimeException()))

        StepVerifier.create(freeboxController.requestAuthorization(token))
            .expectError()
            .verify(Duration.ofSeconds(10))
    }

    @Test
    fun loginOpenSession() {
        val authorizeReponseData = AuthorizeResponse.AuthorizeReponseData::class.arbitraryInstance()
        val tokenRequest = TokenRequest::class.arbitraryInstance()
        val challengeValueResponse = LoginChallengeValueResponse::class.arbitraryInstance()
        `when`(freeboxService.findAuthorizeReponse()).thenReturn(Mono.just(authorizeReponseData))
        `when`(freeboxService.findTokenRequest()).thenReturn(Mono.just(tokenRequest))
        `when`(freeboxService.encodePassword(authorizeReponseData.appToken, challengeValueResponse.result.challenge)).thenReturn("password")
        `when`(freeboxApi.loginChallengeValue()).thenReturn(Mono.just(challengeValueResponse))
        val openSessionResponse = OpenSessionResponse::class.arbitraryInstance()
        `when`(freeboxApi.loginOpenSession(any())).thenReturn(Mono.just(openSessionResponse))

        StepVerifier.create(freeboxController.loginOpenSession()).expectNext(openSessionResponse.result).verifyComplete()
    }

    private fun <T> any(): T {
        return Mockito.any<T>()
    }

//    @Test
//    fun password() {
//        val password = freeboxController.password("qSlyCT1aLsUqMlzUrCMww+8qFN9U2CCe6p8XBekClyEiT6nz7uVcdo1CQUBQ8Kos", "0mLo4C9Sq0uxuBg22dyAl788+NdZvKuf")
//        assertThat(password).isEqualTo("1e1d4415f4264d01df6fcb413b0e33c0d7aef134")
//    }
}
