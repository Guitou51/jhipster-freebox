package com.jhipster.freebox.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.jhipster.freebox.api.authentication.request.FreeboxApi
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.reactive.ReactorFeign
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(FreeboxApiProperties::class)
class FreeboxApiConfig {
    @Bean
    fun freeboxApi(freeboxApiProperties: FreeboxApiProperties): FreeboxApi {
        val objectMapper = ObjectMapper().registerModule(KotlinModule())
        objectMapper.propertyNamingStrategy = SNAKE_CASE
        return ReactorFeign.builder()
            .encoder(JacksonEncoder(objectMapper))
            .decoder(JacksonDecoder(objectMapper))
//            .errorDecoder { methodKey: String?, response: Response? ->
//                response.body()
//            }
            .target(FreeboxApi::class.java, freeboxApiProperties.url)
    }
}
