package com.jhipster.freebox.domain

import com.jhipster.freebox.api.authentication.dto.TrackAuthorizeResponse
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class FreeboxAuthorization(
    @Id @GeneratedValue
    var id: Long,
    var url: String,
    var appId: String,
    var appName: String,
    var appVersion: String,
    var deviceName: String,
    var appToken: String? = null,
    var trackId: Long? = null,
    var status: TrackAuthorizeResponse.TrackAuthorizeReponseData.AuthorizationStatus
)
