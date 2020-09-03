package com.jhipster.freebox.api.authentication.dto

import com.fasterxml.jackson.annotation.JsonCreator
import java.io.Serializable

data class TokenRequest @JsonCreator constructor(val appId: String, val appName: String, val appVersion: String, val deviceName: String) : Serializable
