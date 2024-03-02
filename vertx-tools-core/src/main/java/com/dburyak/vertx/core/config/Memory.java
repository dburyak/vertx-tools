package com.dburyak.vertx.core.config;

/**
 * Memory size. All prefixes are based on 1024 (binary system).
 */
public class Memory {
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

    long getBytes() {
        return bytes;
    }

    long getB() {
        return bytes;
    }

    String getBytesString() {
        return String.format(FMT_INT, bytes, "B");
    }

    String getbString() {
        return getBytesString();
    }

    double getKiloBytes() {
        return getKb();
    }

    double getKb() {
        return (double) bytes / 1024;
    }

    String getKiloBytesString() {
        return getKbString();
    }

    String getKbString() {
        return String.format(FMT_FLOAT, getKb(), "KB");
    }

    long getKiloBytesRounded() {
        return getKbRounded();
    }

    long getKbRounded() {
        return Math.round(getKb());
    }

    String getKiloBytesRoundedString() {
        return getKbRoundedString();
    }

    String getKbRoundedString() {
        return String.format(FMT_INT, getKbRounded(), "KB");
    }

    double getMegaBytes() {
        return getMb();
    }

    double getMb() {
        return getKb() / 1024;
    }

    String getMegaBytesString() {
        return getMbString();
    }

    String getMbString() {
        return String.format(FMT_FLOAT, getMb(), "MB");
    }

    long getMegaBytesRounded() {
        return getMbRounded();
    }

    long getMbRounded() {
        return Math.round(getMb());
    }

    String getMegaBytesRoundedString() {
        return getMbRoundedString();
    }

    String getMbRoundedString() {
        return String.format(FMT_INT, getMbRounded(), "MB");
    }

    double getGigaBytes() {
        return getGb();
    }

    double getGb() {
        return getMb() / 1024;
    }

    String getGigaBytesString() {
        return getGbString();
    }

    String getGbString() {
        return String.format(FMT_FLOAT, getGb(), "GB");
    }

    long getGigaBytesRounded() {
        return getGbRounded();
    }

    long getGbRounded() {
        return Math.round(getGb());
    }

    String getGigaBytesRoundedString() {
        return getGbRoundedString();
    }

    String getGbRoundedString() {
        return String.format(FMT_INT, getGbRounded(), "GB");
    }

    double getTeraBytes() {
        return getTb();
    }

    double getTb() {
        return getGb() / 1024;
    }

    String getTeraBytesString() {
        return getTbString();
    }

    String getTbString() {
        return String.format(FMT_FLOAT, getTb(), "TB");
    }

    long getTeraBytesRounded() {
        return getTbRounded();
    }

    long getTbRounded() {
        return Math.round(getTb());
    }

    String getTeraBytesRoundedString() {
        return getTbRoundedString();
    }

    String getTbRoundedString() {
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
}
