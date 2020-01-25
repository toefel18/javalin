package nl.toefel.javalin.performance

import com.fasterxml.jackson.databind.ObjectMapper
import io.javalin.Javalin
import io.javalin.http.Context
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.function.Supplier

fun main() {
    Router(8080).start()
}

data class CustomerDto(
    val id: Int?,
    val firstName: String,
    val lastName: String
)

class Router(private val port: Int) {
    private val logger: Logger = LoggerFactory.getLogger(Router::class.java)
    private val executor = Executors.newCachedThreadPool()
    private val mapper = ObjectMapper().findAndRegisterModules()

    val app = Javalin.create { cfg -> cfg.requestLogger(::logRequest).enableCorsForAllOrigins() }
        .get("/perf/instant", ::getCustomer)
        .get("/perf/future", ::getCustomerFuture)

    private fun logRequest(ctx: Context, executionTimeMs: Float) =
        logger.info("${ctx.method()} ${ctx.fullUrl()} status=${ctx.status()} durationMs=$executionTimeMs")

    fun start(): Router {
        app.start(port)
        return this
    }

    private fun getCustomer(ctx: Context) {
        val delay = ctx.queryParam("delayMs")?.toLong() ?: 0
        if (delay > 0) {
            Thread.sleep(delay)
        }
        ctx.json(CustomerDto(
            id = 1,
            firstName = "TestDto",
            lastName = "Jemeur"
        ))
    }

    private fun getCustomerFuture(ctx: Context) {
        val delay = ctx.queryParam("delayMs")?.toLong() ?: 0L
        val cf = CompletableFuture.supplyAsync(Supplier<String> {
            if (delay > 0) {
                Thread.sleep(delay)
            }
            mapper.writeValueAsString(
                CustomerDto(
                    id = 1,
                    firstName = "TestDto",
                    lastName = "Jemeur"
                ))
        }, executor)
        ctx.result(cf)
    }
}

