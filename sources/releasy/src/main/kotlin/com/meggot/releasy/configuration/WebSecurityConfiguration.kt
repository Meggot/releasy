package com.meggot.releasy.configuration

import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@EnableWebSecurity
open class WebSecurityConfiguration()
    : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        //TODO make it not basic auth....dunno how to set the session in postman
        http.cors().and().csrf().disable()
        http.authorizeRequests()
                .antMatchers("/registration/user").permitAll()
                .antMatchers("/payload").permitAll()
                .antMatchers("/*").permitAll()
                .and()
                .httpBasic()
    }


    override fun configure(webSecurity: WebSecurity) {
        webSecurity
                .ignoring()
                .antMatchers("/**/health")
    }
}
