package com.zero.domain;

public class OAuthAuthorizationCode {
  private String id;
  private String codeHash;
  private String userId;
  private String clientId;
  private String redirectUri;
  private String scope;
  private String codeChallenge;
  private String codeChallengeMethod;
  private String expiresAt;
  private String usedAt;
  private String createdAt;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCodeHash() {
    return codeHash;
  }

  public void setCodeHash(String codeHash) {
    this.codeHash = codeHash;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public String getCodeChallenge() {
    return codeChallenge;
  }

  public void setCodeChallenge(String codeChallenge) {
    this.codeChallenge = codeChallenge;
  }

  public String getCodeChallengeMethod() {
    return codeChallengeMethod;
  }

  public void setCodeChallengeMethod(String codeChallengeMethod) {
    this.codeChallengeMethod = codeChallengeMethod;
  }

  public String getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(String expiresAt) {
    this.expiresAt = expiresAt;
  }

  public String getUsedAt() {
    return usedAt;
  }

  public void setUsedAt(String usedAt) {
    this.usedAt = usedAt;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }
}
