package com.zero.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SnapshotItem {
  private String id;

  @JsonProperty("snapshot_id")
  private String snapshotId;

  @JsonProperty("account_id")
  private String accountId;

  private double balance;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSnapshotId() {
    return snapshotId;
  }

  public void setSnapshotId(String snapshotId) {
    this.snapshotId = snapshotId;
  }

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public double getBalance() {
    return balance;
  }

  public void setBalance(double balance) {
    this.balance = balance;
  }
}
