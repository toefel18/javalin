package nl.toefel.javalin.exposed

import nl.toefel.javalin.exposed.rest.Router
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Starts an in-memory H2 database, creates the schema and loads some test data
 */
class Main {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Main::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            Router(8080).start()
        }
    }
}





