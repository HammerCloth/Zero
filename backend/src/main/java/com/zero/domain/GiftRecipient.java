package com.zero.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GiftRecipient {
  private String id;

  @JsonProperty("user_id")
  private String userId;

  private String name;
  private String relationship;
  private String note;

  @JsonProperty("is_active")
  private boolean active;

  @JsonProperty("gift_count")
  private int giftCount;

  @JsonProperty("gift_total")
  private double giftTotal;

  @JsonProperty("created_at")
  private String createdAt;

  @JsonProperty("updated_at")
  private String updatedAt;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getUserId() { return userId; }
  public void setUserId(String userId) { this.userId = userId; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getRelationship() { return relationship; }
  public void setRelationship(String relationship) { this.relationship = relationship; }
  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }
  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
  public int getGiftCount() { return giftCount; }
  public void setGiftCount(int giftCount) { this.giftCount = giftCount; }
  public double getGiftTotal() { return giftTotal; }
  public void setGiftTotal(double giftTotal) { this.giftTotal = giftTotal; }
  public String getCreatedAt() { return createdAt; }
  public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
  public String getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
