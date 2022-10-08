package bmstu.ru

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import bmstu.ru.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureSockets()
        configureRouting()
    }.start(wait = true)
}
