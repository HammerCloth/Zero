package com.zero.web;

import com.zero.service.UserOptionDefinitions;
import com.zero.service.UserOptionService;
import com.zero.service.UserOptionService.OptionIn;
import com.zero.support.CurrentUser;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

  private final UserOptionService userOptionService;

  public SettingsController(UserOptionService userOptionService) {
    this.userOptionService = userOptionService;
  }

  @GetMapping("/options")
  public Map<String, List<Map<String, Object>>> getOptions() {
    String uid = CurrentUser.require().userId();
    return userOptionService.getOptions(uid);
  }

  public record DimensionBody(List<OptionIn> items) {}

  @PutMapping("/options/{dimension}")
  public Map<String, Boolean> putDimension(
      @PathVariable String dimension, @RequestBody DimensionBody body) {
    String uid = CurrentUser.require().userId();
    userOptionService.replaceDimension(uid, normalizeDimension(dimension), body.items());
    return Map.of("ok", true);
  }

  @PostMapping("/options/reset")
  public Map<String, Boolean> reset() {
    String uid = CurrentUser.require().userId();
    userOptionService.resetToDefaults(uid);
    return Map.of("ok", true);
  }

  private static String normalizeDimension(String path) {
    return switch (path) {
      case "account-type" -> UserOptionDefinitions.DIM_ACCOUNT_TYPE;
      case "account-owner" -> UserOptionDefinitions.DIM_ACCOUNT_OWNER;
      case "event-category" -> UserOptionDefinitions.DIM_EVENT_CATEGORY;
      default -> path;
    };
  }
}
