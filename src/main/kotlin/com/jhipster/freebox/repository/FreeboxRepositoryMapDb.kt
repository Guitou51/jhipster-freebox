package com.jhipster.freebox.repository

import com.jhipster.freebox.api.authentication.dto.AuthorizeResponse
import com.jhipster.freebox.api.authentication.dto.TokenRequest
import org.mapdb.DB
import org.mapdb.HTreeMap
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class FreeboxRepositoryMapDb(db: DB) : FreeboxRepository {
    val map: HTreeMap<Any, Any> = db.hashMap("freebox").createOrOpen() as HTreeMap<Any, Any>

    override fun saveAuthorizeReponse(authorizeReponseData: AuthorizeResponse.AuthorizeReponseData): Mono<Void> {
        return Mono.create { t ->
            map["AuthorizeReponse"] = authorizeReponseData
            t.success()
        }
    }

    override fun findAuthorizeReponse(): Mono<AuthorizeResponse.AuthorizeReponseData> {
        return Mono.justOrEmpty(map["AuthorizeReponse"]) as Mono<AuthorizeResponse.AuthorizeReponseData>
    }

    override fun saveTokenRequest(tokenRequest: TokenRequest): Mono<Void> {
        return Mono.create { t ->
            map["TokenRequest"] = tokenRequest
            t.success()
        }
    }

    override fun findTokenRequest(): Mono<TokenRequest> {
        return Mono.justOrEmpty(map["TokenRequest"]) as Mono<TokenRequest>
    }

    override fun resetTokenRequest(): Mono<Void> {
        return Mono.create { t ->
            map.remove("TokenRequest")
            t.success()
        }
    }
}
