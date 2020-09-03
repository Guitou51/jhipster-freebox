package com.jhipster.freebox.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.core.io.Resource

@ConstructorBinding
@ConfigurationProperties(prefix = "spring.mapdb")
data class MapDbProperties(val fileResource: Resource)
