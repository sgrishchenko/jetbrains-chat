package me.sgrishchenko.jetbrainschat.server

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.delay
import kotlinx.html.*
import java.util.*
import kotlin.math.min

fun HTML.index() {
    head {
        meta {
            charset = "UTF-8"
        }
        title("Jetbrains Chat")
    }
    body {
        div {
            id = "root"
        }
        script(src = "/static/output.js") {}
    }
}

fun main() {
    val serverProperties = Properties().apply {
        load(ClassLoader.getSystemResource("jetbrainschat.properties").openStream())
    }

    val messageDefaultLimit = serverProperties.getProperty("messages.default-limit").toInt()
    val messageCount = serverProperties.getProperty("messages.count").toInt()
    val messageDelay = serverProperties.getProperty("messages.delay").toLong()

    embeddedServer(Netty, port = 8000) {
        install(ContentNegotiation) {
            json()
        }

        routing {
            get("/") {
                call.respondHtml(io.ktor.http.HttpStatusCode.OK, HTML::index)
            }
            get("/messages") {
                val offset = call.request.queryParameters["offset"]?.toInt() ?: 0
                val limit = call.request.queryParameters["limit"]?.toInt() ?: messageDefaultLimit

                val messages = (offset until min(offset + limit, messageCount))
                    .map { generateMessage(it) }


                delay(messageDelay)
                call.respond(messages)
            }
            static("/static") {
                resources()
            }
        }
    }.start(wait = true)
}