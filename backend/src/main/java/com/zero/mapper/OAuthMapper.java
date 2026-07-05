package com.zero.mapper;

import com.zero.domain.OAuthAuthorizationCode;
import com.zero.domain.OAuthAuthorizationView;
import com.zero.domain.OAuthClient;
import com.zero.domain.OAuthRefreshToken;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OAuthMapper {

  @Insert(
      "INSERT INTO oauth_clients(id, client_id, client_name, redirect_uris, grant_types, scopes) "
          + "VALUES(#{id}, #{clientId}, #{clientName}, #{redirectUris}, #{grantTypes}, #{scopes})")
  int insertClient(OAuthClient client);

  @Select(
      "SELECT id, client_id as clientId, client_name as clientName, redirect_uris as redirectUris, "
          + "grant_types as grantTypes, scopes, created_at as createdAt, last_used_at as lastUsedAt "
          + "FROM oauth_clients WHERE client_id = #{clientId}")
  OAuthClient findClientByClientId(String clientId);

  @Update("UPDATE oauth_clients SET last_used_at = CURRENT_TIMESTAMP WHERE client_id = #{clientId}")
  int touchClient(String clientId);

  @Insert(
      "INSERT INTO oauth_authorization_codes("
          + "id, code_hash, user_id, client_id, redirect_uri, scope, code_challenge, code_challenge_method, expires_at) "
          + "VALUES(#{id}, #{codeHash}, #{userId}, #{clientId}, #{redirectUri}, #{scope}, "
          + "#{codeChallenge}, #{codeChallengeMethod}, #{expiresAt})")
  int insertAuthorizationCode(OAuthAuthorizationCode code);

  @Select(
      "SELECT id, code_hash as codeHash, user_id as userId, client_id as clientId, redirect_uri as redirectUri, "
          + "scope, code_challenge as codeChallenge, code_challenge_method as codeChallengeMethod, "
          + "expires_at as expiresAt, used_at as usedAt, created_at as createdAt "
          + "FROM oauth_authorization_codes WHERE code_hash = #{codeHash}")
  OAuthAuthorizationCode findCodeByHash(String codeHash);

  @Update("UPDATE oauth_authorization_codes SET used_at = CURRENT_TIMESTAMP WHERE id = #{id} AND used_at IS NULL")
  int markCodeUsed(String id);

  @Insert(
      "INSERT INTO oauth_refresh_tokens(id, token_hash, user_id, client_id, scope, expires_at) "
          + "VALUES(#{id}, #{tokenHash}, #{userId}, #{clientId}, #{scope}, #{expiresAt})")
  int insertRefreshToken(OAuthRefreshToken token);

  @Select(
      "SELECT id, token_hash as tokenHash, user_id as userId, client_id as clientId, scope, "
          + "expires_at as expiresAt, revoked_at as revokedAt, last_used_at as lastUsedAt, created_at as createdAt "
          + "FROM oauth_refresh_tokens WHERE token_hash = #{tokenHash}")
  OAuthRefreshToken findRefreshTokenByHash(String tokenHash);

  @Update(
      "UPDATE oauth_refresh_tokens SET revoked_at = CURRENT_TIMESTAMP, last_used_at = CURRENT_TIMESTAMP "
          + "WHERE id = #{id} AND revoked_at IS NULL")
  int revokeRefreshToken(String id);

  @Update(
      "UPDATE oauth_refresh_tokens SET revoked_at = CURRENT_TIMESTAMP "
          + "WHERE user_id = #{userId} AND id = #{id} AND revoked_at IS NULL")
  int revokeUserRefreshToken(@Param("userId") String userId, @Param("id") String id);

  @Update(
      "UPDATE oauth_refresh_tokens SET revoked_at = CURRENT_TIMESTAMP "
          + "WHERE user_id = #{userId} AND revoked_at IS NULL")
  int revokeAllUserRefreshTokens(String userId);

  @Select(
      "SELECT rt.id, rt.client_id as clientId, c.client_name as clientName, rt.scope, "
          + "rt.created_at as createdAt, rt.last_used_at as lastUsedAt, rt.expires_at as expiresAt, "
          + "rt.revoked_at as revokedAt "
          + "FROM oauth_refresh_tokens rt INNER JOIN oauth_clients c ON rt.client_id = c.client_id "
          + "WHERE rt.user_id = #{userId} ORDER BY rt.created_at DESC")
  List<OAuthAuthorizationView> listAuthorizations(String userId);
}
