package com.zero.domain;

public class OAuthAuthorizationView {
  private String id;
  private String clientId;
  private String clientName;
  private String scope;
  private String createdAt;
  private String lastUsedAt;
  private String expiresAt;
  private String revokedAt;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientName() {
    return clientName;
  }

  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public String getLastUsedAt() {
    return lastUsedAt;
  }

  public void setLastUsedAt(String lastUsedAt) {
    this.lastUsedAt = lastUsedAt;
  }

  public String getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(String expiresAt) {
    this.expiresAt = expiresAt;
  }

  public String getRevokedAt() {
    return revokedAt;
  }

  public void setRevokedAt(String revokedAt) {
    this.revokedAt = revokedAt;
  }
}
