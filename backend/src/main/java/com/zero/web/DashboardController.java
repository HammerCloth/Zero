package com.zero.web;

import com.zero.service.DashboardService;
import com.zero.support.CurrentUser;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

  private final DashboardService dashboardService;

  public DashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping("/summary")
  public Map<String, Object> summary() {
    String uid = CurrentUser.require().userId();
    return dashboardService.summary(uid);
  }

  @GetMapping("/trend")
  public Map<String, Object> trend(@RequestParam(value = "range", required = false) String range) {
    String uid = CurrentUser.require().userId();
    return dashboardService.trend(uid, range);
  }

  @GetMapping("/composition")
  public Map<String, Object> composition() {
    String uid = CurrentUser.require().userId();
    return dashboardService.composition(uid);
  }

  @GetMapping("/monthly-growth")
  public Map<String, Object> monthlyGrowth(@RequestParam(value = "year", required = false) Integer year) {
    String uid = CurrentUser.require().userId();
    return dashboardService.monthlyGrowth(uid, year);
  }

  @GetMapping("/stacked-by-type")
  public Map<String, Object> stackedByType(@RequestParam(value = "range", required = false) String range) {
    String uid = CurrentUser.require().userId();
    return dashboardService.stackedByType(uid, range);
  }

  @GetMapping("/account-trends")
  public Map<String, Object> accountTrends(@RequestParam(value = "range", required = false) String range) {
    String uid = CurrentUser.require().userId();
    return dashboardService.accountTrends(uid, range);
  }
}
