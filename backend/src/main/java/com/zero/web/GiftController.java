package com.zero.web;

import com.zero.domain.GiftOccasionStat;
import com.zero.domain.GiftRecipient;
import com.zero.domain.GiftRecord;
import com.zero.service.GiftService;
import com.zero.support.CurrentUser;
import java.time.LocalDate;
import java.util.LinkedHashMap;
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
@RequestMapping("/api/v1/gifts")
public class GiftController {
  private final GiftService giftService;

  public GiftController(GiftService giftService) {
    this.giftService = giftService;
  }

  public record RecipientBody(String name, String relationship, String note) {}
  public record GiftBody(
      String recipientId, String occasion, String giftDate, Double amount, String paymentMethod, String note) {}

  @GetMapping("/recipients")
  public Map<String, List<GiftRecipient>> listRecipients(
      @RequestParam(value = "includeInactive", defaultValue = "false") boolean includeInactive) {
    return Map.of("recipients", giftService.listRecipients(CurrentUser.require().userId(), includeInactive));
  }

  @PostMapping("/recipients")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, GiftRecipient> createRecipient(@RequestBody RecipientBody body) {
    GiftRecipient recipient = giftService.createRecipient(
        CurrentUser.require().userId(), body.name(), body.relationship(), body.note());
    return Map.of("recipient", recipient);
  }

  @PutMapping("/recipients/{id}")
  public Map<String, Boolean> updateRecipient(@PathVariable String id, @RequestBody RecipientBody body) {
    giftService.updateRecipient(CurrentUser.require().userId(), id, body.name(), body.relationship(), body.note());
    return Map.of("ok", true);
  }

  @DeleteMapping("/recipients/{id}")
  public Map<String, Boolean> deactivateRecipient(@PathVariable String id) {
    giftService.deactivateRecipient(CurrentUser.require().userId(), id);
    return Map.of("ok", true);
  }

  @GetMapping
  public Map<String, List<GiftRecord>> listRecords(
      @RequestParam(value = "year", required = false) Integer year,
      @RequestParam(value = "recipientId", required = false) String recipientId,
      @RequestParam(value = "keyword", required = false) String keyword) {
    return Map.of("records", giftService.listRecords(CurrentUser.require().userId(), year, recipientId, keyword));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, GiftRecord> createRecord(@RequestBody GiftBody body) {
    GiftRecord record = giftService.createRecord(CurrentUser.require().userId(), body.recipientId(), body.occasion(),
        body.giftDate(), body.amount(), body.paymentMethod(), body.note());
    return Map.of("record", record);
  }

  @PutMapping("/{id}")
  public Map<String, Boolean> updateRecord(@PathVariable String id, @RequestBody GiftBody body) {
    giftService.updateRecord(CurrentUser.require().userId(), id, body.recipientId(), body.occasion(),
        body.giftDate(), body.amount(), body.paymentMethod(), body.note());
    return Map.of("ok", true);
  }

  @DeleteMapping("/{id}")
  public Map<String, Boolean> deleteRecord(@PathVariable String id) {
    giftService.deleteRecord(CurrentUser.require().userId(), id);
    return Map.of("ok", true);
  }

  @GetMapping("/stats")
  public Map<String, Object> stats(@RequestParam(value = "year", required = false) Integer year) {
    int y = year == null ? LocalDate.now().getYear() : year;
    String userId = CurrentUser.require().userId();
    List<GiftOccasionStat> rows = giftService.statsByOccasion(userId, y);
    Map<String, Double> byOccasion = new LinkedHashMap<>();
    Map<String, Integer> countByOccasion = new LinkedHashMap<>();
    double grandTotal = 0;
    int count = 0;
    for (GiftOccasionStat row : rows) {
      byOccasion.put(row.getOccasion(), row.getTotal());
      countByOccasion.put(row.getOccasion(), row.getCount());
      grandTotal += row.getTotal();
      count += row.getCount();
    }
    return Map.of("year", y, "grandTotal", grandTotal, "count", count,
        "recipientCount", giftService.countRecipientsForYear(userId, y),
        "byOccasion", byOccasion, "countByOccasion", countByOccasion);
  }
}
