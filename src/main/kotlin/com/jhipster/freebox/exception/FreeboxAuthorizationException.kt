package com.jhipster.freebox.exception

import com.jhipster.freebox.api.authentication.dto.TrackAuthorizeResponse

class FreeboxAuthorizationException(val status: TrackAuthorizeResponse.TrackAuthorizeReponseData.AuthorizationStatus?) : Throwable()
