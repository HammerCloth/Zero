package com.zero.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GiftRecord {
  private String id;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("gift_recipient_id")
  private String giftRecipientId;

  @JsonProperty("recipient_name")
  private String recipientName;

  @JsonProperty("recipient_relationship")
  private String recipientRelationship;

  private String occasion;

  @JsonProperty("gift_date")
  private String giftDate;

  private double amount;

  @JsonProperty("payment_method")
  private String paymentMethod;

  private String note;

  @JsonProperty("created_at")
  private String createdAt;

  @JsonProperty("updated_at")
  private String updatedAt;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getUserId() { return userId; }
  public void setUserId(String userId) { this.userId = userId; }
  public String getGiftRecipientId() { return giftRecipientId; }
  public void setGiftRecipientId(String giftRecipientId) { this.giftRecipientId = giftRecipientId; }
  public String getRecipientName() { return recipientName; }
  public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
  public String getRecipientRelationship() { return recipientRelationship; }
  public void setRecipientRelationship(String recipientRelationship) { this.recipientRelationship = recipientRelationship; }
  public String getOccasion() { return occasion; }
  public void setOccasion(String occasion) { this.occasion = occasion; }
  public String getGiftDate() { return giftDate; }
  public void setGiftDate(String giftDate) { this.giftDate = giftDate; }
  public double getAmount() { return amount; }
  public void setAmount(double amount) { this.amount = amount; }
  public String getPaymentMethod() { return paymentMethod; }
  public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }
  public String getCreatedAt() { return createdAt; }
  public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
  public String getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
