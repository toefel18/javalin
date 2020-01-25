package nl.toefel.javalin.exposed.rest

import io.javalin.Javalin
import io.javalin.http.Context
import nl.toefel.javalin.exposed.dto.ActorDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class Router(val port: Int) {
    private val logger: Logger = LoggerFactory.getLogger(Router::class.java)
    private val executor = Executors.newFixedThreadPool(64)

    val app = Javalin.create { cfg -> cfg.requestLogger(::logRequest).enableCorsForAllOrigins() }
        .get("/actors", ::getActor)
        .get("/actors-future", ::getActorFuture)

    private fun logRequest(ctx: Context, executionTimeMs: Float) =
        logger.info("${ctx.method()} ${ctx.fullUrl()} status=${ctx.status()} durationMs=$executionTimeMs")

    fun start(): Router {
        app.start(port)
        return this
    }

    fun getActor(ctx: Context) {
        logger.info("GET " + ctx.req.requestURL)
        val delay = ctx.queryParam("delayMs")?.toLong() ?: 0
        if (delay > 0) {
            Thread.sleep(delay)
        }
        ctx.json(ActorDto(
            id = 1,
            firstName = "TestDto",
            lastName = "Jemeur"
        ))
    }

    fun getActorFuture(ctx: Context) {
        logger.info("GET " + ctx.req.requestURL)
        val delay = ctx.queryParam("delayMs")?.toLong() ?: 0L
        val cf = CompletableFuture.supplyAsync<ActorDto>({
            if (delay > 0) {
                Thread.sleep(delay)
            }
            ActorDto(
                id = 1,
                firstName = "TestDto",
                lastName = "Jemeur"
            )
        }, executor::execute)
        ctx.result(cf)
    }
}