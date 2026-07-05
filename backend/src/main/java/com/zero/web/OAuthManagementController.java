package com.zero.web;

import com.zero.domain.OAuthAuthorizationView;
import com.zero.service.OAuthService;
import com.zero.support.CurrentUser;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/oauth/authorizations")
public class OAuthManagementController {

  private final OAuthService oauthService;

  public OAuthManagementController(OAuthService oauthService) {
    this.oauthService = oauthService;
  }

  @GetMapping
  public Map<String, List<OAuthAuthorizationView>> list() {
    String uid = CurrentUser.require().userId();
    return Map.of("authorizations", oauthService.listAuthorizations(uid));
  }

  @DeleteMapping("/{id:[a-fA-F0-9]{32}}")
  public Map<String, Boolean> revoke(@PathVariable String id) {
    String uid = CurrentUser.require().userId();
    oauthService.revokeAuthorization(uid, id);
    return Map.of("ok", true);
  }

  @DeleteMapping
  public Map<String, Boolean> revokeAll() {
    String uid = CurrentUser.require().userId();
    oauthService.revokeAllAuthorizations(uid);
    return Map.of("ok", true);
  }
}
