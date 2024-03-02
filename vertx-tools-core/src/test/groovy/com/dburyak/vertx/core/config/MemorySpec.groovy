package com.dburyak.vertx.core.config

import spock.lang.Specification
import spock.lang.Unroll

class MemorySpec extends Specification {

    @Unroll
    def 'of#unit constructs memory'() {
        when:
        def memory = Memory."of$unit"(amount)

        then:
        memory.bytes == expectedBytes

        where:
        amount | unit        || expectedBytes
        5432   | 'Bytes'     || 5432L
        5432   | 'Kb'        || 5432L * 1024
        5.432  | 'Kb'        || (5.432 * 1024).toLong()
        5432   | 'KiloBytes' || 5432L * 1024
        5.432  | 'KiloBytes' || (5.432 * 1024).toLong()
        5432   | 'Mb'        || 5432L * 1024 * 1024
        5.432  | 'Mb'        || (5.432 * 1024 * 1024).toLong()
        5432   | 'MegaBytes' || 5432L * 1024 * 1024
        5.432  | 'MegaBytes' || (5.432 * 1024 * 1024).toLong()
        5432   | 'Gb'        || 5432L * 1024 * 1024 * 1024
        5.432  | 'Gb'        || (5.432 * 1024 * 1024 * 1024).toLong()
        5432   | 'GigaBytes' || 5432L * 1024 * 1024 * 1024
        5.432  | 'GigaBytes' || (5.432 * 1024 * 1024 * 1024).toLong()
        5432   | 'Tb'        || 5432L * 1024 * 1024 * 1024 * 1024
        5.432  | 'Tb'        || (5.432 * 1024 * 1024 * 1024 * 1024).toLong()
        5432   | 'TeraBytes' || 5432L * 1024 * 1024 * 1024 * 1024
        5.432  | 'TeraBytes' || (5.432 * 1024 * 1024 * 1024 * 1024).toLong()
    }

    @Unroll
    def 'get<Unit> converts #amount#unit memory'() {
        given:
        def memory = Memory."of$unit"(amount)

        expect:
        memory.bytes == expBytes
        memory.b == expBytes
        memory.kiloBytes == expKb
        memory.kb == expKb
        memory.megaBytes == expMb
        memory.mb == expMb
        memory.gigaBytes == expGb
        memory.gb == expGb
        memory.teraBytes == expTb
        memory.tb == expTb

        where:
        amount | unit || expBytes       | expKb          | expMb         | expGb         | expTb
        512    | 'Gb' || 512L * 1024**3 | 512L * 1024**2 | 512L * 1024   | 512L          | 0.5
        512    | 'Mb' || 512L * 1024**2 | 512L * 1024    | 512L          | 0.5           | 0.5 / 1024
        512    | 'Kb' || 512L * 1024    | 512L           | 0.5           | 0.5 / 1024    | 0.5 / 1024**2
        0.5    | 'Tb' || 0.5 * 1024**4  | 0.5 * 1024**3  | 0.5 * 1024**2 | 0.5 * 1024    | 0.5
        0.5    | 'Gb' || 0.5 * 1024**3  | 0.5 * 1024**2  | 0.5 * 1024    | 0.5           | 0.5 / 1024
        0.5    | 'Mb' || 0.5 * 1024**2  | 0.5 * 1024     | 0.5           | 0.5 / 1024    | 0.5 / 1024**2
        0.5    | 'Kb' || 0.5 * 1024     | 0.5            | 0.5 / 1024    | 0.5 / 1024**2 | 0.5 / 1024**3
    }

    @Unroll
    def 'get<Unit>Rounded converts #amount#unit memory'() {
        given:
        def memory = Memory."of$unit"(amount)

        expect:
        memory.kiloBytesRounded == expKb.toDouble().round().toLong()
        memory.kbRounded == expKb.toDouble().round().toLong()
        memory.megaBytesRounded == expMb.toDouble().round().toLong()
        memory.mbRounded == expMb.toDouble().round().toLong()
        memory.gigaBytesRounded == expGb.toDouble().round().toLong()
        memory.gbRounded == expGb.toDouble().round().toLong()
        memory.teraBytesRounded == expTb.toDouble().round().toLong()
        memory.tbRounded == expTb.toDouble().round().toLong()

        where:
        amount | unit || expKb           | expMb         | expGb      | expTb
        512    | 'Gb' || 512L * 1024**2  | 512L * 1024   | 512L       | 1
        1200   | 'Gb' || 1200L * 1024**2 | 1200L * 1024  | 1200L      | 1
        512    | 'Mb' || 512L * 1024     | 512L          | 1          | 0
        1200   | 'Mb' || 1200L * 1024    | 1200L         | 1          | 0
        512    | 'Kb' || 512L            | 1             | 0          | 0
        1200   | 'Kb' || 1200L           | 1             | 0          | 0
        0.5    | 'Tb' || 0.5 * 1024**3   | 0.5 * 1024**2 | 0.5 * 1024 | 1
        1.3    | 'Tb' || 1.3 * 1024**3   | 1.3 * 1024**2 | 1.3 * 1024 | 1
        0.5    | 'Gb' || 0.5 * 1024**2   | 0.5 * 1024    | 1          | 0
        1.3    | 'Gb' || 1.3 * 1024**2   | 1.3 * 1024    | 1          | 0
        0.5    | 'Mb' || 0.5 * 1024      | 1             | 0          | 0
        1.3    | 'Mb' || 1.3 * 1024      | 1             | 0          | 0
        0.5    | 'Kb' || 1               | 0             | 0          | 0
        1.3    | 'Kb' || 1               | 0             | 0          | 0
        2.7    | 'Kb' || 3               | 0             | 0          | 0
        3.3    | 'Kb' || 3               | 0             | 0          | 0
    }

    @Unroll
    def 'get<Unit>String prints #amount#unit memory'() {
        given:
        def memory = Memory."of$unit"(amount)
        def fmt = '%.2f'

        expect:
        memory.bString == "${expB.toLong()} B"
        memory.bytesString == "${expB.toLong()} B"
        memory.kbString == "${fmt.formatted(expKb.toDouble().round(2))} KB"
        memory.kiloBytesString == "${fmt.formatted(expKb.toDouble().round(2))} KB"
        memory.mbString == "${fmt.formatted(expMb.toDouble().round(2))} MB"
        memory.megaBytesString == "${fmt.formatted(expMb.toDouble().round(2))} MB"
        memory.gbString == "${fmt.formatted(expGb.toDouble().round(2))} GB"
        memory.gigaBytesString == "${fmt.formatted(expGb.toDouble().round(2))} GB"
        memory.tbString == "${fmt.formatted(expTb.toDouble().round(2))} TB"
        memory.teraBytesString == "${fmt.formatted(expTb.toDouble().round(2))} TB"

        where:
        amount | unit    || expB             | expKb            | expMb            | expGb            | expTb
        5432   | 'Bytes' || 5432             | 5432 / 1024      | 5432 / 1024L**2  | 5432 / 1024L**3  | 5432 / 1024L**4
        5432   | 'Kb'    || 5432 * 1024      | 5432             | 5432 / 1024      | 5432 / 1024L**2  | 5432 / 1024L**3
        5.432  | 'Kb'    || 5.432 * 1024     | 5.432            | 5.432 / 1024     | 5.432 / 1024L**2 | 5.432 / 1024L**3
        5432   | 'Mb'    || 5432L * 1024L**2 | 5432 * 1024      | 5432             | 5432 / 1024      | 5432 / 1024L**2
        5.432  | 'Mb'    || 5.432 * 1024L**2 | 5.432 * 1024     | 5.432            | 5.432 / 1024     | 5.432 / 1024L**2
        5432   | 'Gb'    || 5432L * 1024L**3 | 5432L * 1024L**2 | 5432 * 1024      | 5432             | 5432 / 1024
        5.432  | 'Gb'    || 5.432 * 1024L**3 | 5.432 * 1024L**2 | 5.432 * 1024     | 5.432            | 5.432 / 1024
        5432   | 'Tb'    || 5432L * 1024L**4 | 5432L * 1024L**3 | 5432L * 1024L**2 | 5432 * 1024      | 5432
        5.432  | 'Tb'    || 5.432 * 1024L**4 | 5.432 * 1024L**3 | 5.432 * 1024L**2 | 5.432 * 1024     | 5.432
    }

    @Unroll
    def 'toString prints #amount#unit memory using best suitable unit'() {
        given:
        def memory = Memory."of$unit"(amount)

        expect:
        memory.toString() == expToString

        where:
        amount | unit    || expToString
        5432   | 'Bytes' || '5.30 KB'
        5432   | 'Kb'    || '5.30 MB'
        5.432  | 'Kb'    || '5.43 KB'
        5432   | 'Mb'    || '5.30 GB'
        5.432  | 'Mb'    || '5.43 MB'
        5432   | 'Gb'    || '5.30 TB'
        5.432  | 'Gb'    || '5.43 GB'
        5432   | 'Tb'    || '5432.00 TB'
        5.432  | 'Tb'    || '5.43 TB'
    }
}
