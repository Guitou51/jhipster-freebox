package com.jhipster.freebox.web.rest

import com.jhipster.freebox.api.authentication.dto.*
import com.jhipster.freebox.api.authentication.dto.TrackAuthorizeResponse.TrackAuthorizeReponseData
import com.jhipster.freebox.api.authentication.dto.TrackAuthorizeResponse.TrackAuthorizeReponseData.AuthorizationStatus
import com.jhipster.freebox.api.authentication.request.FreeboxApi
import com.jhipster.freebox.exception.FreeboxAuthorizationException
import com.jhipster.freebox.security.ADMIN
import com.jhipster.freebox.service.FreeboxService
import java.time.Duration
import java.util.logging.Level
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink

@RestController
@RequestMapping("/api/freebox")
class FreeboxController(val freeboxService: FreeboxService, val freeboxApi: FreeboxApi) {

    private fun recursiveTrackCall(trackId: Int): Flux<TrackAuthorizeReponseData> {
        val loginTrackRequestAuthorization = freeboxApi.loginTrackRequestAuthorization(trackId)

        val trackRequestAuthorization = loginTrackRequestAuthorization.log("loginTrackRequestAuthorization", Level.WARNING)
        return trackRequestAuthorization
            .flatMapMany {
                var trackAuthorizeReponseData = it.result
                Mono.create<TrackAuthorizeReponseData> { t -> t.success(trackAuthorizeReponseData) }
                    .concatWith(loginTrackRequestAuthorization.delayElement(Duration.ofSeconds(1)).doOnNext { t -> trackAuthorizeReponseData = t.result }
                        .map { t -> t.result })
                    .handle { res, sink: SynchronousSink<TrackAuthorizeReponseData> ->
                        if (res.status == AuthorizationStatus.timeout || res.status == AuthorizationStatus.denied) {
                            sink.next(res)
                            sink.error(FreeboxAuthorizationException(res.status))
                        } else if (res.status == AuthorizationStatus.granted) {
                            sink.next(res)
                            sink.complete()
                        } else
                            sink.next(res)
                    }
                    .repeat { trackAuthorizeReponseData.status != AuthorizationStatus.granted }
            }
    }

    @GetMapping("/login/authorize")
    @PreAuthorize("hasAuthority(\"$ADMIN\")")
    fun getTokenRequest(): Mono<TokenRequest> {
        return freeboxService.findTokenRequest()
    }

    @GetMapping("/login/authorize/track")
    @PreAuthorize("hasAuthority(\"$ADMIN\")")
    fun getTrackId(): Mono<Int> {
        return freeboxService.findAuthorizeReponse().map { it.trackId }
    }

    @PostMapping("/login/authorize")
    @PreAuthorize("hasAuthority(\"$ADMIN\")")
    fun requestAuthorization(@RequestBody request: TokenRequest): Mono<Int> {
        return freeboxService.callRequestAuthorization(request).map { it.result.trackId }
    }

    @GetMapping("/login/authorize/{trackId}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @PreAuthorize("hasAuthority(\"$ADMIN\")")
    fun trackRequestAuthorization(@PathVariable trackId: Int): Flux<TrackAuthorizeReponseData> {
        return recursiveTrackCall(trackId)
    }

    @GetMapping("/login")
    @PreAuthorize("hasAuthority(\"$ADMIN\")")
    fun loginChallengeValue(): Mono<LoginChallengeValueResponse.LoginChallengeValueData> {
        return freeboxApi.loginChallengeValue().map { it.result }
    }

    @PostMapping("/login/session")
    @PreAuthorize("hasAuthority(\"$ADMIN\")")
    fun loginOpenSession(): Mono<OpenSessionResponse.OpenSessionReponseData> {
        return freeboxService.findAuthorizeReponse()
            .switchIfEmpty(Mono.error(RuntimeException("need call requestAuthorization before")))
            .map { it.appToken }.flatMap { appToken ->
                freeboxService.findTokenRequest().flatMap { token ->
                    freeboxApi.loginChallengeValue().flatMap { loginChallenge ->
                        val hmacHex: String = freeboxService.encodePassword(appToken, loginChallenge.result.challenge)
                        freeboxApi.loginOpenSession(OpenSessionRequest(token.appId, hmacHex))
                            .filter { it.success }
                            .switchIfEmpty(Mono.error(RuntimeException("open session not success")))
                            .map { it.result }
                    }
                }
            }
    }
}
