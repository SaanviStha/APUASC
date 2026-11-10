package model;

public enum AppointmentStatus {

    ASSIGNED("Assigned"),
    COMPLETED("Completed");

    private final String displayName;

    AppointmentStatus(String displayName) {
        this.displayName = displayName;
    }

    // Text shown on screen, e.g. "Completed"
    @Override
    public String toString() {
        return displayName;
    }
}
