package model;

public enum Role {

    MANAGER("Manager", "M"),
    COUNTER_STAFF("Counter Staff", "S"),
    TECHNICIAN("Technician", "T"),
    CUSTOMER("Customer", "C");

    private final String displayName;
    private final String idPrefix;

    Role(String displayName, String idPrefix) {
        this.displayName = displayName;
        this.idPrefix = idPrefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIdPrefix() {
        return idPrefix;
    }

    // Text shown on screen, e.g. "Counter Staff"
    @Override
    public String toString() {
        return displayName;
    }
}
