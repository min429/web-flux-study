package org.example.webfluxstudy

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import kotlin.test.Test

@Component
class KotlinTest {

    @BeforeEach
    fun setUp() {
        log.info("main Thread: ${Thread.currentThread().name}")
    }

    @Nested
    inner class MonoTest {

        @Test
        @DisplayName("just vs fromCallable vs defer")
        fun test1() {
            // value initialize
            var value = "just"
            println("initial value: $value")

            // just
            val just = Mono.just(value)

            // fromCallable
            val fromCallable = Mono.fromCallable {
                value
            }

            // defer
            val defer = Mono.defer {
                Mono.just(value)
            }

            println("================================================")
            just.subscribe { println("just value: $it") }
            println("================================================")
            fromCallable.subscribe { println("fromCallable value1: $it") }

            // value reinitialize
            value = "fromCallable"

            fromCallable.subscribe { println("fromCallable value2: $it") }
            println("================================================")
            defer.subscribe { println("defer value1: $it") }

            // value reinitialize
            value = "defer"

            defer.subscribe { println("defer value2: $it") }

            // just: 체인 형성 도중에 즉시 값 생성,
            // fromCallable: subscribe 시 값 생성,
            // defer: subscribe 시 내부 Publisher 생성 후 바로 subscribe (값 생성)
            // fromCallable, defer는 반환 형식만 다르고 동일한 기능으로 예상됨
        }
    }
}
