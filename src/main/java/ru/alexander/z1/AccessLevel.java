package ru.alexander.z1;

public enum AccessLevel {
    BOOTLOADER(13),
    OS(11),
    ADMIN(7),
    USER(3);

    private final int securityLevel;

    AccessLevel(int securityLevel) {
        this.securityLevel = securityLevel;
    }

    public int getSecurityLevel() {
        return securityLevel;
    }
}
