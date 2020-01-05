def console = 'console'

appender(console, ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = '%white(%date{yyyy-MM-dd HH:mm:ss.SSS}) ' +// date+time
                '%highlight(%-5level) ' + // log level
                '[%yellow(%20.20thread)] ' + // thread
                '%cyan(%40logger{40}) - ' + // logger name
                '%msg%n' // message
        /*
        pattern = '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                '%clr(%5p) ' + // Log level
                '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                '%m%n%wex'
         */
    }
}

root DEBUG, [console]
logger 'com.athaydes.spockframework.report', WARN
logger 'io.netty', INFO
logger 'io.vertx', INFO
logger 'io.micronaut', INFO
