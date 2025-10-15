package org.example.webfluxstudy

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.ConcurrentHashMap

@RestController
class TestController {

    companion object {
        val nettyThreads: ConcurrentHashMap<Any, Int> = ConcurrentHashMap()
    }

    @GetMapping
    fun test() {
        val thread = Thread.currentThread()
        println("netty Thread: $thread")
        nettyThreads.merge(thread, 1) { old, _ -> old + 1 }
    }
}