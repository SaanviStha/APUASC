package model;

public enum ServiceType {

    NORMAL(1, "Normal Service"),
    MAJOR(3, "Major Service");

    private final int hours;
    private final String displayName;

    ServiceType(int hours, String displayName) {
        this.hours = hours;
        this.displayName = displayName;
    }

    public int getHours() {
        return hours;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Text shown on screen, e.g. "Normal Service (1 hour)"
    @Override
    public String toString() {
        return displayName + " (" + hours + (hours == 1 ? " hour)" : " hours)");
    }
}
