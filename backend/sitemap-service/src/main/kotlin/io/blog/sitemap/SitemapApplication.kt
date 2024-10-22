package io.blog.sitemap

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
class SitemapApplication

fun main(args: Array<String>) {
	runApplication<SitemapApplication>(*args)
}
