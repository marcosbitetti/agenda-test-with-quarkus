package org.acme.health;

public class HealthStatus {
    public State status;
    public State database;

    public HealthStatus() {
    }

    public HealthStatus(State status, State database) {
        this.status = status;
        this.database = database;
    }

    public enum State {
        UP,
        DOWN
    }
}
