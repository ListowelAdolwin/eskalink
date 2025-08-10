package com.listo.eskalink.job.enums;

public enum JobStatus {
    DRAFT, OPEN, CLOSED;

    public boolean canTransitionTo(JobStatus newStatus) {
        return switch (this) {
            case DRAFT -> newStatus == OPEN || newStatus == CLOSED;
            case OPEN -> newStatus == CLOSED;
            case CLOSED -> false;
        };
    }
}
