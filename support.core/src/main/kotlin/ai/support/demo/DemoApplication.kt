package ai.support.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
class DemoApplication {
	@Bean
	fun restTemplate() : RestTemplate {
		val requestFactory = SimpleClientHttpRequestFactory();
		return RestTemplate(requestFactory);
	}

}

fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}
