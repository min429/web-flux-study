package org.example.webfluxstudy

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KotlinSpringTest {

    @LocalServerPort
    private var port: Int = 0

    @Nested
    inner class WebFlux {

        val boundedElasticThreads: ConcurrentHashMap<Any, Int> = ConcurrentHashMap()

        @Test
        @DisplayName("API 동시 요청 (netty)")
        fun test1() {
            val webClient = WebClient.builder()
                .baseUrl("http://localhost:$port")
                .build()

            Flux.range(1, Runtime.getRuntime().availableProcessors() * 10) // Logical CPU 코어 수 x 10 만큼 반복
                .flatMap { i ->
                    Mono.defer {
                        val thread = Thread.currentThread().name
                        println("Bounded Elastic Thread $i: $thread")
                        boundedElasticThreads.merge(thread, 1) { old, _ -> old + 1 }

                        webClient.get()
                            .retrieve()
                            .bodyToMono(String::class.java)
                    }
                        .subscribeOn(Schedulers.boundedElastic())
                }
                .blockLast()

            println("eventLoop Threads num:" + TestController.nettyThreads.entries.size) // Logical CPU 코어 수
            println("boundedElastic Threads num:" + boundedElasticThreads.entries.size) // Logical CPU 코어 수 x 10
        }
    }
}