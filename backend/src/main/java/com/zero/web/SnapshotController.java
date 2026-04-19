package com.zero.web;

import com.zero.service.SnapshotService;
import com.zero.service.SnapshotService.EventIn;
import com.zero.service.SnapshotService.ItemIn;
import com.zero.support.CurrentUser;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/snapshots")
public class SnapshotController {

  private final SnapshotService snapshotService;

  public SnapshotController(SnapshotService snapshotService) {
    this.snapshotService = snapshotService;
  }

  @GetMapping
  public Map<String, List<Map<String, Object>>> list() {
    String uid = CurrentUser.require().userId();
    return Map.of("snapshots", snapshotService.listSummaries(uid));
  }

  @GetMapping("/latest")
  public Map<String, Object> latest() {
    String uid = CurrentUser.require().userId();
    return snapshotService.getLatestOrNull(uid);
  }

  @GetMapping("/for-date")
  public Map<String, Object> forDate(@RequestParam String date) {
    String uid = CurrentUser.require().userId();
    return snapshotService.findForDate(uid, date);
  }

  @GetMapping("/dates-in-range")
  public Map<String, Object> datesInRange(@RequestParam String from, @RequestParam String to) {
    String uid = CurrentUser.require().userId();
    return snapshotService.listDatesInRange(uid, from, to);
  }

  @GetMapping("/{id:[a-fA-F0-9]{32}}")
  public Map<String, Object> get(@PathVariable String id) {
    String uid = CurrentUser.require().userId();
    return Map.of("snapshot", snapshotService.getDetail(uid, id));
  }

  public record WriteBody(String date, String note, List<ItemIn> items, List<EventIn> events) {}

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> create(@RequestBody WriteBody body) {
    var p = CurrentUser.require();
    return snapshotService.create(
        p.userId(), p.userId(), body.date(), body.note(), body.items(), body.events());
  }

  @PutMapping("/{id:[a-fA-F0-9]{32}}")
  public Map<String, Object> update(@PathVariable String id, @RequestBody WriteBody body) {
    String uid = CurrentUser.require().userId();
    return snapshotService.update(uid, id, body.date(), body.note(), body.items(), body.events());
  }

  @DeleteMapping("/{id:[a-fA-F0-9]{32}}")
  public Map<String, Boolean> delete(@PathVariable String id) {
    String uid = CurrentUser.require().userId();
    snapshotService.delete(uid, id);
    return Map.of("ok", true);
  }
}
