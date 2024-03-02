package com.dburyak.vertx.core.config;

import lombok.EqualsAndHashCode;

/**
 * Memory size. All prefixes are based on 1024 (binary system).
 */
@EqualsAndHashCode
public class Memory implements Comparable<Memory> {
    private static final String FMT_FLOAT = "%.2f %s";
    private static final String FMT_INT = "%d %s";

    private final long bytes;

    public Memory(long bytes) {
        this.bytes = bytes;
    }

    public static Memory ofBytes(long bytes) {
        return new Memory(bytes);
    }

    public static Memory ofKb(long kiloBytes) {
        return new Memory(kiloBytes * 1024);
    }

    public static Memory ofKiloBytes(long kiloBytes) {
        return ofKb(kiloBytes);
    }

    public static Memory ofKb(double kiloBytes) {
        return new Memory((long) (kiloBytes * 1024));
    }

    public static Memory ofKiloBytes(double kiloBytes) {
        return ofKb(kiloBytes);
    }

    public static Memory ofMb(long megaBytes) {
        return new Memory(megaBytes * 1024 * 1024);
    }

    public static Memory ofMegaBytes(long megaBytes) {
        return ofMb(megaBytes);
    }

    public static Memory ofMb(double megaBytes) {
        return new Memory((long) (megaBytes * 1024 * 1024));
    }

    public static Memory ofMegaBytes(double megaBytes) {
        return ofMb(megaBytes);
    }

    public static Memory ofGb(long gigaBytes) {
        return new Memory(gigaBytes * 1024 * 1024 * 1024);
    }

    public static Memory ofGigaBytes(long gigaBytes) {
        return ofGb(gigaBytes);
    }

    public static Memory ofGb(double gigaBytes) {
        return new Memory((long) (gigaBytes * 1024 * 1024 * 1024));
    }

    public static Memory ofGigaBytes(double gigaBytes) {
        return ofGb(gigaBytes);
    }

    public static Memory ofTb(long teraBytes) {
        return new Memory(teraBytes * 1024 * 1024 * 1024 * 1024);
    }

    public static Memory ofTeraBytes(long teraBytes) {
        return ofTb(teraBytes);
    }

    public static Memory ofTb(double teraBytes) {
        return new Memory((long) (teraBytes * 1024 * 1024 * 1024 * 1024));
    }

    public static Memory ofTeraBytes(double teraBytes) {
        return ofTb(teraBytes);
    }

    public long getBytes() {
        return bytes;
    }

    public long getB() {
        return bytes;
    }

    public String getBytesString() {
        return String.format(FMT_INT, bytes, "B");
    }

    public String getbString() {
        return getBytesString();
    }

    public double getKiloBytes() {
        return getKb();
    }

    public double getKb() {
        return (double) bytes / 1024;
    }

    public String getKiloBytesString() {
        return getKbString();
    }

    public String getKbString() {
        return String.format(FMT_FLOAT, getKb(), "KB");
    }

    public long getKiloBytesRounded() {
        return getKbRounded();
    }

    public long getKbRounded() {
        return Math.round(getKb());
    }

    public String getKiloBytesRoundedString() {
        return getKbRoundedString();
    }

    public String getKbRoundedString() {
        return String.format(FMT_INT, getKbRounded(), "KB");
    }

    public double getMegaBytes() {
        return getMb();
    }

    public double getMb() {
        return getKb() / 1024;
    }

    public String getMegaBytesString() {
        return getMbString();
    }

    public String getMbString() {
        return String.format(FMT_FLOAT, getMb(), "MB");
    }

    public long getMegaBytesRounded() {
        return getMbRounded();
    }

    public long getMbRounded() {
        return Math.round(getMb());
    }

    public String getMegaBytesRoundedString() {
        return getMbRoundedString();
    }

    public String getMbRoundedString() {
        return String.format(FMT_INT, getMbRounded(), "MB");
    }

    public double getGigaBytes() {
        return getGb();
    }

    public double getGb() {
        return getMb() / 1024;
    }

    public String getGigaBytesString() {
        return getGbString();
    }

    public String getGbString() {
        return String.format(FMT_FLOAT, getGb(), "GB");
    }

    public long getGigaBytesRounded() {
        return getGbRounded();
    }

    public long getGbRounded() {
        return Math.round(getGb());
    }

    public String getGigaBytesRoundedString() {
        return getGbRoundedString();
    }

    public String getGbRoundedString() {
        return String.format(FMT_INT, getGbRounded(), "GB");
    }

    public double getTeraBytes() {
        return getTb();
    }

    public double getTb() {
        return getGb() / 1024;
    }

    public String getTeraBytesString() {
        return getTbString();
    }

    public String getTbString() {
        return String.format(FMT_FLOAT, getTb(), "TB");
    }

    public long getTeraBytesRounded() {
        return getTbRounded();
    }

    public long getTbRounded() {
        return Math.round(getTb());
    }

    public String getTeraBytesRoundedString() {
        return getTbRoundedString();
    }

    public String getTbRoundedString() {
        return String.format(FMT_INT, getTbRounded(), "TB");
    }

    @Override
    public String toString() {
        return getTb() >= 1 ? getTbString()
                : getGb() >= 1 ? getGbString()
                        : getMb() >= 1 ? getMbString()
                                : getKb() >= 1 ? getKbString()
                                        : getBytesString();
    }

    @Override
    public int compareTo(Memory otherMem) {
        return Long.compare(bytes, otherMem.bytes);
    }
}
