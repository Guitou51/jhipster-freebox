package com.jhipster.freebox.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("freebox")
class FreeboxApiProperties {
    var url: String = "http://192.168.0.254/api/v8"
}
