package bmstu.ru.plugins

import bmstu.ru.data.Data
import bmstu.ru.data.Response
import io.ktor.serialization.kotlinx.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.serialization.DeserializationStrategy

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }

    routing {
        webSocket("/num/input") {
            println("connect input")
            incoming.consumeAsFlow()
                .mapNotNull { it as? Frame.Text }
                .map { it.readText() }
                .map { Json.decodeFromString<Data>(it) }
                .onCompletion {
                    //here comes what you would put in the `finally` block,
                    //which is executed after flow collection completes
                }
                .collect { a -> sendSerialized(processRequest(a))}
        }
    }
}

private fun processRequest(d: Data): Response {
    println(d)
    if (d.v1.length != 3 || d.v2.length != 3) return Response("unspecified")
    val v1 = d.v1.toCharArray().map { n -> n.digitToInt() }.toCollection(ArrayList())
    val v2 = d.v2.toCharArray().map { n -> n.digitToInt() }.toCollection(ArrayList())
    return Response((v1.mul(v2) == 0).toString()).also { println(it) }
}

fun ArrayList<Int>.mul(other: ArrayList<Int>): Int {
    if (this.size != other.size) return -1
    return this.mapIndexed { i, v -> v * other[i] }.sum()
}
