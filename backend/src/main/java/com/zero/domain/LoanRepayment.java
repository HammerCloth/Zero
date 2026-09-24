package com.zero.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoanRepayment {
  private String id;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("loan_id")
  private String loanId;

  private double amount;

  @JsonProperty("repay_date")
  private String repayDate;

  private String note;

  @JsonProperty("created_at")
  private String createdAt;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getUserId() { return userId; }
  public void setUserId(String userId) { this.userId = userId; }
  public String getLoanId() { return loanId; }
  public void setLoanId(String loanId) { this.loanId = loanId; }
  public double getAmount() { return amount; }
  public void setAmount(double amount) { this.amount = amount; }
  public String getRepayDate() { return repayDate; }
  public void setRepayDate(String repayDate) { this.repayDate = repayDate; }
  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }
  public String getCreatedAt() { return createdAt; }
  public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
