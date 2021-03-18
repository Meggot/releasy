package com.meggot.releasy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class ReleasyApplication

fun main(args: Array<String>) {
    val runApplication = runApplication<ReleasyApplication>(*args)

}


