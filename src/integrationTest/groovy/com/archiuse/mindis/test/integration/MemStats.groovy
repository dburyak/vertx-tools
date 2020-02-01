package com.archiuse.mindis.test.integration

class MemStats {
    private static final KB = 1024
    private static final MB = 1024 * 1024
    private static final GB = 1024 * 1024 * 1024

    long max
    long allocated
    long used
    long free

    long getMaxKb() {
        max.intdiv KB
    }

    long getMaxMb() {
        max.intdiv MB
    }

    long getMaxGb() {
        max.intdiv GB
    }

    long getAllocatedKb() {
        allocated.intdiv KB
    }

    long getAllocatedMb() {
        allocated.intdiv MB
    }

    long getAllocatedGb() {
        allocated.intdiv GB
    }

    long getUsedKb() {
        used.intdiv KB
    }

    long getUsedMb() {
        used.intdiv MB
    }

    long getUsedGb() {
        used.intdiv GB
    }

    long getFreeKb() {
        free.intdiv KB
    }

    long getFreeMb() {
        free.intdiv MB
    }

    long getFreeGb() {
        free.intdiv GB
    }
}
