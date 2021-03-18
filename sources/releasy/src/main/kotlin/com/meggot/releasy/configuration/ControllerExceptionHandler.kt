package com.meggot.releasy.configuration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.meggot.releasy.model.dto.releasy.ReleasyException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody

@ControllerAdvice
class ControllerExceptionHandler {

    @ExceptionHandler(ReleasyException::class)
    @ResponseBody
    fun handleReleasyException(ex: ReleasyException): ResponseEntity<Any> {
        return ResponseEntity.badRequest().body(jacksonObjectMapper().writeValueAsString(ReleasyException.body(ex)))
    }

}