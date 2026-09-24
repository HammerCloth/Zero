package com.zero.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Loan {
  private String id;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("borrower_name")
  private String borrowerName;

  private String relationship;
  private double amount;

  @JsonProperty("loan_date")
  private String loanDate;

  @JsonProperty("due_date")
  private String dueDate;

  private String note;

  @JsonProperty("repaid_total")
  private double repaidTotal;

  @JsonProperty("remaining")
  private double remaining;

  @JsonProperty("repayment_count")
  private int repaymentCount;

  private boolean settled;

  @JsonProperty("created_at")
  private String createdAt;

  @JsonProperty("updated_at")
  private String updatedAt;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<LoanRepayment> repayments = new ArrayList<>();

  public void refreshDerived() {
    BigDecimal left = BigDecimal.valueOf(amount)
        .subtract(BigDecimal.valueOf(repaidTotal))
        .setScale(2, RoundingMode.HALF_UP);
    remaining = left.max(BigDecimal.ZERO).doubleValue();
    settled = remaining <= 0;
  }

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getUserId() { return userId; }
  public void setUserId(String userId) { this.userId = userId; }
  public String getBorrowerName() { return borrowerName; }
  public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }
  public String getRelationship() { return relationship; }
  public void setRelationship(String relationship) { this.relationship = relationship; }
  public double getAmount() { return amount; }
  public void setAmount(double amount) { this.amount = amount; }
  public String getLoanDate() { return loanDate; }
  public void setLoanDate(String loanDate) { this.loanDate = loanDate; }
  public String getDueDate() { return dueDate; }
  public void setDueDate(String dueDate) { this.dueDate = dueDate; }
  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }
  public double getRepaidTotal() { return repaidTotal; }
  public void setRepaidTotal(double repaidTotal) { this.repaidTotal = repaidTotal; }
  public double getRemaining() { return remaining; }
  public void setRemaining(double remaining) { this.remaining = remaining; }
  public int getRepaymentCount() { return repaymentCount; }
  public void setRepaymentCount(int repaymentCount) { this.repaymentCount = repaymentCount; }
  public boolean isSettled() { return settled; }
  public void setSettled(boolean settled) { this.settled = settled; }
  public String getCreatedAt() { return createdAt; }
  public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
  public String getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
  public List<LoanRepayment> getRepayments() { return repayments; }
  public void setRepayments(List<LoanRepayment> repayments) { this.repayments = repayments; }
}
