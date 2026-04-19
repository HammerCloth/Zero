package com.zero.web;

import com.zero.domain.EventCategoryCount;
import com.zero.domain.EventCategoryStat;
import com.zero.mapper.SnapshotMapper;
import com.zero.support.CurrentUser;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

  private final SnapshotMapper snapshotMapper;

  public EventController(SnapshotMapper snapshotMapper) {
    this.snapshotMapper = snapshotMapper;
  }

  @GetMapping("/stats")
  public Map<String, Object> stats(@RequestParam(value = "year", required = false) Integer year) {
    String uid = CurrentUser.require().userId();
    int y = year != null ? year : LocalDate.now().getYear();
    List<EventCategoryStat> rows = snapshotMapper.sumEventsByCategoryForYear(uid, String.valueOf(y));
    Map<String, Double> byCategory = new LinkedHashMap<>();
    double grand = 0;
    for (EventCategoryStat r : rows) {
      byCategory.put(r.getCategory(), r.getTotal());
      grand += r.getTotal();
    }
    List<EventCategoryCount> counts = snapshotMapper.countExpenseEventsByCategoryForYear(uid, String.valueOf(y));
    Map<String, Integer> countByCategory = new LinkedHashMap<>();
    for (EventCategoryCount c : counts) {
      countByCategory.put(c.getCategory(), c.getCnt());
    }
    return Map.of("year", y, "byCategory", byCategory, "grandTotal", grand, "countByCategory", countByCategory);
  }
}
