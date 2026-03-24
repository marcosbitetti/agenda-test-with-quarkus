package org.acme.health;

public class HealthStatus {
    public String status;
    public String database;

    public HealthStatus() {
    }

    public HealthStatus(String status, String database) {
        this.status = status;
        this.database = database;
    }
}
