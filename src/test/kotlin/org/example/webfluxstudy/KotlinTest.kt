package org.example.webfluxstudy

import org.junit.jupiter.api.BeforeEach
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import kotlin.test.Test

@Component
class KotlinTest {

    @BeforeEach
    fun setUp() {
        log.info("main Thread: ${Thread.currentThread().name}")
    }

    /**
     * 동기 JDBC 블로킹 호출 시 flatMap 결과 순서 보장 테스트
     */
    @Test
    fun syncJdbc_flatMap_orderPreserved() {
        Flux.just("A", "B", "C")
            .flatMap { v ->
                Mono.fromCallable {
                    Thread.sleep(200) // 동기 블로킹 JDBC 흉내
                    "$v!"
                }
            }
            .doOnNext { v ->
                println("[result] $v on ${Thread.currentThread().name}")
            }
            .blockLast()
        // 출력: 항상 A!, B!, C! 순서
    }

    /**
     * JDBC 블로킹 호출 + subscribeOn(boundedElastic) → 각 작업을 다른 스레드에서 실행
     * 결과 순서는 뒤섞일 수 있음
     */
    @Test
    fun syncJdbc_withSubscribeOn_orderNotGuaranteed() {
        Flux.just("A", "B", "C")
            .flatMap { v ->
                Mono.fromCallable {
                    if (v == "A") Thread.sleep(300) else Thread.sleep(100)
                    "$v!"
                }.subscribeOn(Schedulers.boundedElastic())
            }
            .doOnNext { v ->
                println("[result] $v on ${Thread.currentThread().name}")
            }
            .blockLast()
        // 출력 예: B!, C!, A!  (실행마다 순서 달라짐)
    }

    /**
     * R2DBC/논블로킹 DB 호출 흉내 (delayElement 사용)
     * 결과 순서는 응답 속도에 따라 달라짐 → flatMap이라 순서 보장 안 됨
     */
    @Test
    fun reactiveDb_flatMap_orderNotGuaranteed() {
        Flux.just("A", "B", "C")
            .flatMap { v ->
                Mono.just("$v!")
                    .delayElement(if (v == "A") Duration.ofMillis(300) else Duration.ofMillis(100))
            }
            .doOnNext { v ->
                println("[result] $v on ${Thread.currentThread().name}")
            }
            .blockLast()
        // 출력 예: B!, C!, A! (실행마다 순서 달라짐)
    }

    /**
     * flatMap + delayElement
     * → 비동기 emit이므로 순서 보장 안 됨
     */
    @Test
    fun flatMap_withDelayElement_orderNotGuaranteed() {
        Flux.just("A", "B", "C")
            .flatMap { v ->
                Mono.just("$v!")
                    .delayElement(
                        if (v == "A") Duration.ofMillis(300) else Duration.ofMillis(100)
                    )
            }
            .doOnNext { v ->
                println("[result] $v on ${Thread.currentThread().name}")
            }
            .blockLast()
        // 출력 예: B!, C!, A! (실행마다 순서 달라짐)
    }

    /**
     * flatMap + 즉시 emit (Mono.just)
     * → 동기 emit이라 순서 보장됨
     */
    @Test
    fun flatMap_withImmediatePublisher_orderPreserved() {
        Flux.just("A", "B", "C")
            .flatMap { v -> Mono.just("$v!") }
            .doOnNext { v ->
                println("[result] $v on ${Thread.currentThread().name}")
            }
            .blockLast()
        // 출력: 항상 A!, B!, C!
    }
}
