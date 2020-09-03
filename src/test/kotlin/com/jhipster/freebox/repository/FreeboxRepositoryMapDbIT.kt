package com.jhipster.freebox.repository

import com.jhipster.freebox.api.authentication.dto.AuthorizeResponse
import com.jhipster.freebox.api.authentication.dto.TokenRequest
import com.tyro.oss.arbitrater.arbitraryInstance
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.test.StepVerifier

@SpringBootTest
internal class FreeboxRepositoryMapDbIT {
    @Autowired
    lateinit var freeboxRepositoryMapDb: FreeboxRepositoryMapDb

    @Test
    fun saveTokenRequest() {
        val result = freeboxRepositoryMapDb.saveTokenRequest(TokenRequest::class.arbitraryInstance())

        StepVerifier.create(result).expectComplete().verify()
    }

    @Test
    fun findTokenRequest_empty() {
        val findTokenRequest = freeboxRepositoryMapDb.resetTokenRequest().flatMap {
            freeboxRepositoryMapDb.findTokenRequest()
        }

        StepVerifier.create(findTokenRequest).expectComplete().verify()
    }

    @Test
    fun findTokenRequest_value() {
        val tokenRequest = TokenRequest::class.arbitraryInstance()
        freeboxRepositoryMapDb.saveTokenRequest(tokenRequest).block()
        val findTokenRequest = freeboxRepositoryMapDb.findTokenRequest()

        StepVerifier.create(findTokenRequest).expectNext(tokenRequest).expectComplete().verify()
    }

    @Test
    fun saveAuthorizeReponse() {
        val authorizeReponseData = AuthorizeResponse.AuthorizeReponseData::class.arbitraryInstance()
        val saveAuthorizeReponse = freeboxRepositoryMapDb.saveAuthorizeReponse(authorizeReponseData)
        StepVerifier.create(saveAuthorizeReponse).expectComplete().verify()
    }

    @Test
    fun findAuthorizeReponse() {
        val expected = AuthorizeResponse.AuthorizeReponseData::class.arbitraryInstance()
        freeboxRepositoryMapDb.saveAuthorizeReponse(expected).block()

        val result = freeboxRepositoryMapDb.findAuthorizeReponse()

        StepVerifier.create(result).expectNext(expected).expectComplete().verify()
    }
}
