package com.jhipster.freebox.service

import com.jhipster.freebox.api.authentication.dto.AuthorizeResponse
import com.jhipster.freebox.api.authentication.dto.TokenRequest
import com.jhipster.freebox.api.authentication.request.FreeboxApi
import com.jhipster.freebox.repository.FreeboxRepositoryMapDb
import com.tyro.oss.arbitrater.arbitraryInstance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockitoExtension::class)
internal class FreeboxServiceIT {
    @InjectMocks
    lateinit var freeboxService: FreeboxService

    @Mock
    lateinit var freeboxApi: FreeboxApi

    @Mock
    lateinit var freeboxRepositoryMapDb: FreeboxRepositoryMapDb

    @Test
    fun requestAuthorization_success() {
        val tokenRequest = TokenRequest::class.arbitraryInstance()
        val authorizeResponse = AuthorizeResponse(true, AuthorizeResponse.AuthorizeReponseData::class.arbitraryInstance())

        `when`(freeboxApi.loginRequestAuthorization(tokenRequest)).thenReturn(Mono.just(authorizeResponse))
        `when`(freeboxRepositoryMapDb.saveAuthorizeReponse(authorizeResponse.result)).thenReturn(Mono.create { it.success() })
        `when`(freeboxRepositoryMapDb.saveTokenRequest(tokenRequest)).thenReturn(Mono.create { it.success() })

        val requestAuthorization = freeboxService.callRequestAuthorization(tokenRequest)

        StepVerifier.create(requestAuthorization).expectNext(authorizeResponse).expectComplete().verify()

        verify(freeboxRepositoryMapDb).saveAuthorizeReponse(authorizeResponse.result)
        verify(freeboxRepositoryMapDb).saveTokenRequest(tokenRequest)
        verify(freeboxApi).loginRequestAuthorization(tokenRequest)
    }

    @Test
    fun requestAuthorization_failed() {
        val tokenRequest = TokenRequest::class.arbitraryInstance()
        val authorizeResponse = AuthorizeResponse(false, AuthorizeResponse.AuthorizeReponseData::class.arbitraryInstance())

        `when`(freeboxApi.loginRequestAuthorization(tokenRequest)).thenReturn(Mono.just(authorizeResponse))

        val requestAuthorization = freeboxService.callRequestAuthorization(tokenRequest)

        StepVerifier.create(requestAuthorization).expectError().verify()

        verifyNoInteractions(freeboxRepositoryMapDb)
        verify(freeboxApi).loginRequestAuthorization(tokenRequest)
    }
}
