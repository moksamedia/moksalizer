import ch.qos.logback.classic.Level
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender


appender('console', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = '%level %logger - %msg%n'
    }
}

root(Level.INFO, ["console"])