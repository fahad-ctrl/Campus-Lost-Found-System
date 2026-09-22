package com.campus.lostfound.model;

/**
 * Represents a candidate pairing between a LOST item and a FOUND item,
 * scored by the MatchEngine.
 */
public class MatchRecord {

    public enum Status {
        SUGGESTED,
        CONFIRMED,
        REJECTED
    }

    private int id;
    private int lostItemId;
    private int foundItemId;
    private double score;
    private Status status;

    // Convenience fields for display in the UI (populated by joined queries)
    private String lostItemDescription;
    private String foundItemDescription;

    public MatchRecord() {
    }

    public MatchRecord(int lostItemId, int foundItemId, double score) {
        this.lostItemId = lostItemId;
        this.foundItemId = foundItemId;
        this.score = score;
        this.status = Status.SUGGESTED;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLostItemId() {
        return lostItemId;
    }

    public void setLostItemId(int lostItemId) {
        this.lostItemId = lostItemId;
    }

    public int getFoundItemId() {
        return foundItemId;
    }

    public void setFoundItemId(int foundItemId) {
        this.foundItemId = foundItemId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getLostItemDescription() {
        return lostItemDescription;
    }

    public void setLostItemDescription(String lostItemDescription) {
        this.lostItemDescription = lostItemDescription;
    }

    public String getFoundItemDescription() {
        return foundItemDescription;
    }

    public void setFoundItemDescription(String foundItemDescription) {
        this.foundItemDescription = foundItemDescription;
    }
}
