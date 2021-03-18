def console = 'console'

appender(console, ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = '%white(%date{yyyy-MM-dd HH:mm:ss.SSS}) ' +// date+time
                '%highlight(%-5level) ' + // log level
                '[%yellow(%20.20thread)] ' + // thread
                '%cyan(%40logger{40}) - ' + // logger name
                '%msg%n' // message
    }
}

root DEBUG, [console]
logger 'com.athaydes.spockframework.report', WARN
logger 'io.netty', INFO
logger 'io.vertx', INFO
logger 'io.micronaut', INFO
