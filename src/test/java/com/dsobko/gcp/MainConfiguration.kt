package com.dsobko.gcp

import org.slf4j.LoggerFactory.getLogger
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration


@Configuration
@ComponentScan(basePackages = ["com.dsobko.gcp"])
@EnableAutoConfiguration(exclude = [DataSourceAutoConfiguration::class])
@EnableConfigurationProperties
class MainConfiguration : CommandLineRunner {

    private val log = getLogger(this.javaClass)

    @Throws(Exception::class)
    override fun run(vararg args: String) {
        log.info("GCP is Ready!!!!")
    }


}
