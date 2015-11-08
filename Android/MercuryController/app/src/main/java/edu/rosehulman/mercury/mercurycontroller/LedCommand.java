package edu.rosehulman.mercury.mercurycontroller;

public class LedCommand {
    private int ledNumber;
    private String status;
    private long timestamp;

    public LedCommand() {

    }

    public LedCommand(int ledNumber, String status, long timestamp) {
        this.ledNumber = ledNumber;
        this.status = status;
        this.timestamp = timestamp;
    }

    public int getLedNumber() {
        return ledNumber;
    }

    public String getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
