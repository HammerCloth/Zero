package com.zero.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class McpService {

  private final SnapshotService snapshotService;

  public McpService(SnapshotService snapshotService) {
    this.snapshotService = snapshotService;
  }

  public List<Map<String, Object>> tools() {
    return List.of(
        tool(
            "get_analysis_guide",
            "Explains how to use this snapshot-based asset system and its data limitations.",
            Map.of("type", "object", "properties", Map.of(), "additionalProperties", false)),
        tool(
            "list_snapshots",
            "Lists available asset snapshots for the current user.",
            Map.of("type", "object", "properties", Map.of(), "additionalProperties", false)),
        tool(
            "get_asset_snapshot",
            "Gets the latest, id-based, or date-based asset snapshot for the current user.",
            Map.of(
                "type",
                "object",
                "properties",
                Map.of(
                    "latest", Map.of("type", "boolean"),
                    "snapshotId", Map.of("type", "string"),
                    "date", Map.of("type", "string", "description", "YYYY-MM-DD")),
                "additionalProperties",
                false)),
        tool(
            "get_major_financial_events",
            "Lists major financial events by their linked snapshot date range, not by record creation time.",
            Map.of(
                "type",
                "object",
                "properties",
                Map.of(
                    "fromDate", Map.of("type", "string", "description", "YYYY-MM-DD"),
                    "toDate", Map.of("type", "string", "description", "YYYY-MM-DD"),
                    "limit", Map.of("type", "integer", "minimum", 1, "maximum", 200)),
                "required",
                List.of("fromDate", "toDate"),
                "additionalProperties",
                false)));
  }

  public Object call(String userId, String name, Map<String, Object> arguments) {
    Map<String, Object> args = arguments == null ? Map.of() : arguments;
    return switch (name) {
      case "get_analysis_guide" -> analysisGuide();
      case "list_snapshots" -> Map.of("snapshots", snapshotService.listSummaries(userId));
      case "get_asset_snapshot" -> getAssetSnapshot(userId, args);
      case "get_major_financial_events" -> getMajorEvents(userId, args);
      default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "未知 MCP tool");
    };
  }

  private Object getAssetSnapshot(String userId, Map<String, Object> args) {
    Object snapshotId = args.get("snapshotId");
    if (snapshotId instanceof String sid && !sid.isBlank()) {
      return Map.of("snapshot", snapshotService.getDetail(userId, sid));
    }
    Object date = args.get("date");
    if (date instanceof String d && !d.isBlank()) {
      return snapshotService.getDetailForDate(userId, d);
    }
    return snapshotService.getLatestOrNull(userId);
  }

  private Object getMajorEvents(String userId, Map<String, Object> args) {
    String from = requireString(args, "fromDate");
    String to = requireString(args, "toDate");
    Integer limit = null;
    Object n = args.get("limit");
    if (n instanceof Number number) {
      limit = number.intValue();
    }
    return snapshotService.listMajorEvents(userId, from, to, limit);
  }

  private Map<String, Object> analysisGuide() {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("model", "snapshot_based_asset_tracking");
    out.put(
        "concepts",
        Map.of(
            "snapshot", "A dated asset inventory record.",
            "snapshot_items", "Balances for each account inside one snapshot.",
            "events", "Major financial events linked to snapshots; they are not complete transaction records.",
            "account_type", "Current default types include cash, deposit, fund, pension, housing_fund, and credit.",
            "owner", "Account ownership labels are user-configurable."));
    out.put(
        "balance_rules",
        Map.of(
            "credit", "Credit accounts are treated as liabilities and count negatively toward net worth.",
            "other_types", "Other account balances count as stored."));
    out.put(
        "recommended_workflow",
        List.of(
            "Call list_snapshots first to understand available dates.",
            "Use get_asset_snapshot for the latest snapshot or a selected snapshot.",
            "Compare two snapshots to describe balance changes.",
            "Use get_major_financial_events for the same date range to explain large changes."));
    out.put(
        "limitations",
        List.of(
            "The system does not store complete transaction ledgers.",
            "Events do not identify the account that paid or received money.",
            "Event date filtering uses the linked snapshot date.",
            "The schema does not store currency, cost basis, liquidity, or risk level.",
            "Cash flow and investment return analysis should be phrased as approximate."));
    return out;
  }

  private static Map<String, Object> tool(String name, String description, Map<String, Object> inputSchema) {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("name", name);
    row.put("description", description);
    row.put("inputSchema", inputSchema);
    return row;
  }

  private static String requireString(Map<String, Object> args, String key) {
    Object value = args.get(key);
    if (value instanceof String s && !s.isBlank()) {
      return s;
    }
    if ("toDate".equals(key)) {
      return LocalDate.now().toString();
    }
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, key + " 不能为空");
  }
}
