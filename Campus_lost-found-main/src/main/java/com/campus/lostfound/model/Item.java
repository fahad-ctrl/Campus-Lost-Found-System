package com.campus.lostfound.model;

import java.time.LocalDate;

/**
 * Represents a single lost or found report logged by a user.
 */
public class Item {

    private int id;
    private int reporterId;
    private ItemType itemType;
    private int categoryId;
    private String categoryName;   // populated by joined queries, optional
    private int locationId;
    private String locationName;   // populated by joined queries, optional
    private LocalDate itemDate;
    private String description;
    private ItemStatus status;
    private String referenceCode;

    public Item() {
    }

    public Item(int reporterId, ItemType itemType, int categoryId, int locationId,
                LocalDate itemDate, String description) {
        this.reporterId = reporterId;
        this.itemType = itemType;
        this.categoryId = categoryId;
        this.locationId = locationId;
        this.itemDate = itemDate;
        this.description = description;
        this.status = ItemStatus.PENDING;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReporterId() {
        return reporterId;
    }

    public void setReporterId(int reporterId) {
        this.reporterId = reporterId;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public LocalDate getItemDate() {
        return itemDate;
    }

    public void setItemDate(LocalDate itemDate) {
        this.itemDate = itemDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s (%s)", itemType, categoryName, description, status);
    }
}
