package org.example.webfluxstudy

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import kotlin.test.Test

class KotlinTest {

    var start = 0L
    var end = 0L

    @BeforeEach
    fun setUp() {
        log.info("main Thread: ${Thread.currentThread().name}")

        start = System.currentTimeMillis()
    }

    @AfterEach
    fun tearDown() {
        end = System.currentTimeMillis()

        println("duration: ${(end - start) / 1000}초")
    }

    @Nested
    inner class FlatMapTest {

        @Test
        @DisplayName("단일스레드 블로킹 flatMap")
        fun test1() {
            Flux.just("A", "B", "C")
                .flatMap { v ->
                    Mono.fromCallable {
                        println("[start] $v on ${Thread.currentThread().name}")
                        Thread.sleep(1000)
                        println("[end] $v on ${Thread.currentThread().name}")
                        "$v!"
                    } // 단일 스레드에서 블로킹 메서드 호출
                }
                .doOnNext { v ->
                    println("[result] $v on ${Thread.currentThread().name}")
                }
                .blockLast()
            // 순서보장 o, 3초
        }

        @Test
        @DisplayName("멀티스레드 블로킹 flatMap")
        fun test2() {
            Flux.just("A", "B", "C")
                .flatMap { v ->
                    Mono.fromCallable {
                        println("[start] $v on ${Thread.currentThread().name}")
                        Thread.sleep(1000)
                        println("[end] $v on ${Thread.currentThread().name}")
                        "$v!"
                    }.subscribeOn(Schedulers.boundedElastic()) // 멀티 스레드에서 블로킹 메서드 호출
                }
                .doOnNext { v ->
                    println("[result] $v on ${Thread.currentThread().name}")
                }
                .blockLast()
            // 순서보장 x, 1초
        }

        @Test
        @DisplayName("단일스레드 논블로킹 flatMap")
        fun test3() {
            Flux.just("A", "B", "C")
                .flatMap { v ->
                    Mono.just("$v!")
                        .delayElement(Duration.ofMillis(1000)) // 단일 스레드에서 논블로킹 메서드 호출
                }
                .doOnNext { v ->
                    println("[result] $v on ${Thread.currentThread().name}")
                }
                .blockLast()
            // 순서보장 x, 1초
        }
    }

    @Nested
    inner class MapTest {

        @Test
        @DisplayName("단일스레드 블로킹 map")
        fun test1() {
            Flux.just("A", "B", "C")
                .map { v ->
                    println("[start] $v on ${Thread.currentThread().name}")
                    Thread.sleep(1000)
                    println("[end] $v on ${Thread.currentThread().name}")
                    "$v!"
                } // 단일 스레드에서 블로킹 메서드 호출
                .doOnNext { v ->
                    println("[result] $v on ${Thread.currentThread().name}")
                }
                .blockLast()
            // 순서보장 o, 3초
        }

        @Test
        @DisplayName("멀티스레드 블로킹 map")
        fun test2() {
            Flux.just("A", "B", "C")
                .map { v ->
                    println("[start] $v on ${Thread.currentThread().name}")
                    Thread.sleep(1000)
                    println("[end] $v on ${Thread.currentThread().name}")
                    "$v!"
                }.subscribeOn(Schedulers.boundedElastic()) // 멀티 스레드에서 블로킹 메서드 호출
                .doOnNext { v ->
                    println("[result] $v on ${Thread.currentThread().name}")
                }
                .blockLast()
            // 순서보장 o, 3초
            // 어짜피 데이터를 받아서 처리하고 넘기고 나서 다음 데이터를 처리하는 구조라 의미 없음
        }
    }
}
