package com.jhipster.freebox.api.authentication.dto

abstract class FreeboxResponse<T> {
    abstract val success: Boolean
    abstract val result: T
}
