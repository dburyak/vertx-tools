import java.time.Duration

mindis {
    config {
        format {
            it['decode-special'] = true
            it['map-key-separator'] = '.'
            it['list-join-separator'] = ','
        }

        reader {
            it['stream-min-interval'] = Duration.ofSeconds(30)
        }
    }
}
