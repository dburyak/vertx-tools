import java.time.Duration

mindis {
    config {
        format {
            decodeSpecial = true
            mapKeySeparator = '.'
            listJoinSeparator = ','
        }
        reader {
            streamMinInterval = Duration.ofSeconds(35)
        }
    }
    service {
        discovery {
            announceAddress = 'vertx.discovery.announce'
            usageAddress = 'vertx .discovery.usage'
        }
        eb {
            nameActionSeparator = '/'
            addrSeparator = '-'
        }
    }
}
