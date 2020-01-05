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
}
