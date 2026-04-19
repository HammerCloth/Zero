package com.zero.web;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class LoginRateLimiter {

  private static final int MAX_FAIL = 5;
  private static final long WINDOW_SECONDS = 15 * 60;

  private static class State {
    int failures;
    Instant windowStart = Instant.now();
  }

  private final Map<String, State> byIp = new ConcurrentHashMap<>();

  public void check(String ip) {
    State s = byIp.computeIfAbsent(ip, k -> new State());
    synchronized (s) {
      if (s.failures >= MAX_FAIL) {
        if (Instant.now().isBefore(s.windowStart.plusSeconds(WINDOW_SECONDS))) {
          throw new ResponseStatusException(
              HttpStatus.TOO_MANY_REQUESTS, "登录尝试次数过多，请15分钟后再试");
        }
        s.failures = 0;
        s.windowStart = Instant.now();
      }
    }
  }

  public void onFailure(String ip) {
    State s = byIp.computeIfAbsent(ip, k -> new State());
    synchronized (s) {
      if (s.failures == 0) {
        s.windowStart = Instant.now();
      }
      s.failures++;
    }
  }

  public void onSuccess(String ip) {
    byIp.remove(ip);
  }
}
