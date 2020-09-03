package com.jhipster.freebox.api.authentication.request

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.jhipster.freebox.JhipsterFreeboxApp
import com.jhipster.freebox.api.authentication.dto.*
import feign.FeignException
import io.netty.handler.codec.http.HttpResponseStatus
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.junit4.SpringRunner
import reactor.test.StepVerifier

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [JhipsterFreeboxApp::class], properties = ["freebox.url=http://localhost:8089"])
@AutoConfigureWireMock(port = 8089)
class FreeboxApiIT {
    @Autowired
    private lateinit var freeboxApi: FreeboxApi

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun requestAuthorization() {
        stubFor(post(urlPathMatching("/login/authorize/"))
            .willReturn(aResponse()
                .withStatus(HttpResponseStatus.OK.code())
                .withBody("{\n" +
                    "   \"success\": true,\n" +
                    "   \"result\": {\n" +
                    "      \"app_token\": \"dyNYgfK0Ya6FWGqq83sBHa7TwzWo+pg4fDFUJHShcjVYzTfaRrZzm93p7OTAfH/0\",\n" +
                    "      \"track_id\": 42\n" +
                    "   }\n" +
                    "}")))
        val result = freeboxApi.loginRequestAuthorization(TokenRequest("test", "test", "test", "test"))

        Assertions.assertThat(result.block()).isEqualTo(AuthorizeResponse(true, AuthorizeResponse.AuthorizeReponseData("dyNYgfK0Ya6FWGqq83sBHa7TwzWo+pg4fDFUJHShcjVYzTfaRrZzm93p7OTAfH/0", 42)))
    }

    @Test
    fun trackRequestAuthorization() {
        stubFor(get(urlPathMatching("/login/authorize/1"))
            .willReturn(aResponse()
                .withStatus(HttpResponseStatus.OK.code())
                .withBody("""
{
  "success": true,
  "result": {
    "status": "pending",
    "challenge": "Bj6xMqoe+DCHD44KqBljJ579seOXNWr2",
    "password_salt": "0zCkC8lNJ6Cxt8Ec4BmBKv58L/zZmGgW"
  }
}"""
                )))
        val result = freeboxApi.loginTrackRequestAuthorization(1)

        Assertions.assertThat(result.block()).isEqualTo(TrackAuthorizeResponse(true, TrackAuthorizeResponse.TrackAuthorizeReponseData(TrackAuthorizeResponse.TrackAuthorizeReponseData.AuthorizationStatus.pending, "Bj6xMqoe+DCHD44KqBljJ579seOXNWr2", "0zCkC8lNJ6Cxt8Ec4BmBKv58L/zZmGgW")))
    }

    @Test
    fun loginChallengeValue() {
        stubFor(get(urlPathMatching("/login/"))
            .willReturn(aResponse()
                .withStatus(HttpResponseStatus.OK.code())
                .withBody("{\n" +
                    "    \"success\": true,\n" +
                    "    \"result\": {\n" +
                    "        \"logged_in\": false,\n" +
                    "        \"challenge\": \"VzhbtpR4r8CLaJle2QgJBEkyd8JPb0zL\"\n,\n" +
                    "    \"password_salt\": \"\"" +
                    "    }\n" +
                    "}")))
        val result = freeboxApi.loginChallengeValue()

        Assertions.assertThat(result.block()).isEqualTo(LoginChallengeValueResponse(true, LoginChallengeValueResponse.LoginChallengeValueData(false, "VzhbtpR4r8CLaJle2QgJBEkyd8JPb0zL", "", false)))
    }

    @Test
    fun loginOpenSession() {
        stubFor(post(urlPathMatching("/login/session/"))
            .willReturn(aResponse()
                .withStatus(HttpResponseStatus.OK.code())
                .withBody("""
{
  "success": true,
  "result" : {
    "session_token" : "35JYdQSvkcBYK84IFMU7H86clfhS75OzwlQrKlQN1gBch\\/Dd62RGzDpgC7YB9jB2",
    "challenge":"jdGL6CtuJ3Dm7p9nkcIQ8pjB+eLwr4Ya",
    "password_salt": "",
    "password_set": "",
    "permissions": {
      "downloader": true
    }
  }
}
""")))
        val result = freeboxApi.loginOpenSession(OpenSessionRequest("test", "test"))

        Assertions.assertThat(result.block())
            .isEqualTo(OpenSessionResponse(true,
                OpenSessionResponse.OpenSessionReponseData("35JYdQSvkcBYK84IFMU7H86clfhS75OzwlQrKlQN1gBch\\/Dd62RGzDpgC7YB9jB2", "jdGL6CtuJ3Dm7p9nkcIQ8pjB+eLwr4Ya", "", "", OpenSessionResponse.OpenSessionReponseData.Permissions(downloader = true))))
    }

    @Test
    fun loginOpenSession_failed() {
        stubFor(post(urlPathMatching("/login/session/"))
            .willReturn(aResponse()
                .withStatus(HttpResponseStatus.FORBIDDEN.code())
                .withBody("""
{
    "msg": "Erreur d'authentification de l'application",
    "success": false,
    "uid": "23b86ec8091013d668829fe12791fdab",
    "error_code": "invalid_token",
    "result": {
         "challenge": "DLjXFEf1kaDwAEn6xRUnEVPU++gnjiSn"
    }
}
""")))
        val result = freeboxApi.loginOpenSession(OpenSessionRequest("test", "test"))

        StepVerifier.create(result).expectError(FeignException.Forbidden::class.java).log().verify()
    }
}
