package com.zero.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zero.service.McpService;
import com.zero.support.CurrentUser;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mcp")
public class McpController {

  private final McpService mcpService;
  private final ObjectMapper objectMapper;

  public McpController(McpService mcpService, ObjectMapper objectMapper) {
    this.mcpService = mcpService;
    this.objectMapper = objectMapper;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<String, Object> handle(@RequestBody Map<String, Object> body) {
    Object id = body.get("id");
    String method = (String) body.get("method");
    try {
      Object result = switch (method) {
        case "initialize" -> Map.of(
            "protocolVersion", "2025-06-18",
            "capabilities", Map.of("tools", Map.of()),
            "serverInfo", Map.of("name", "project-zero-mcp", "version", "1.0.0"));
        case "tools/list" -> Map.of("tools", mcpService.tools());
        case "tools/call" -> callTool(body);
        default -> throw new IllegalArgumentException("Unknown method");
      };
      return response(id, result);
    } catch (Exception e) {
      return error(id, -32603, e.getMessage() == null ? "MCP request failed" : e.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> callTool(Map<String, Object> body) throws JsonProcessingException {
    String uid = CurrentUser.require().userId();
    Map<String, Object> params = (Map<String, Object>) body.getOrDefault("params", Map.of());
    String name = (String) params.get("name");
    Map<String, Object> arguments = (Map<String, Object>) params.getOrDefault("arguments", Map.of());
    Object structured = mcpService.call(uid, name, arguments);
    return Map.of(
        "content",
        java.util.List.of(Map.of("type", "text", "text", objectMapper.writeValueAsString(structured))),
        "structuredContent",
        structured,
        "isError",
        false);
  }

  private static Map<String, Object> response(Object id, Object result) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("jsonrpc", "2.0");
    out.put("id", id);
    out.put("result", result);
    return out;
  }

  private static Map<String, Object> error(Object id, int code, String message) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("jsonrpc", "2.0");
    out.put("id", id);
    out.put("error", Map.of("code", code, "message", message));
    return out;
  }
}
