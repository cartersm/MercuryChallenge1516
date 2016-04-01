package edu.rosehulman.mercury.mercurycontroller;

public class LedCommand {
    private String status;
    private long timestamp;

    public LedCommand() {

    }

    public LedCommand(String status, long timestamp) {
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
