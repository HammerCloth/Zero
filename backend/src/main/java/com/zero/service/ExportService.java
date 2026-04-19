package com.zero.service;

import com.zero.domain.Account;
import com.zero.domain.Snapshot;
import com.zero.domain.SnapshotEvent;
import com.zero.domain.SnapshotItem;
import com.zero.mapper.AccountMapper;
import com.zero.mapper.SnapshotMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ExportService {

  private static final byte[] UTF8_BOM = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

  private final SnapshotMapper snapshotMapper;
  private final AccountMapper accountMapper;

  public ExportService(SnapshotMapper snapshotMapper, AccountMapper accountMapper) {
    this.snapshotMapper = snapshotMapper;
    this.accountMapper = accountMapper;
  }

  public byte[] exportCsv(String userId) {
    Map<String, Account> accounts =
        accountMapper.listAllByUser(userId).stream()
            .collect(Collectors.toMap(Account::getId, Function.identity()));
    List<Snapshot> snaps = snapshotMapper.listSnapshotsByUser(userId);
    StringBuilder sb = new StringBuilder();
    sb.append("snapshot_id,date,note,created_at,account_id,account_name,type,owner,balance\n");
    for (Snapshot s : snaps) {
      List<SnapshotItem> items = snapshotMapper.listItems(s.getId());
      for (SnapshotItem it : items) {
        Account a = accounts.get(it.getAccountId());
        sb.append(csv(s.getId()))
            .append(',')
            .append(csv(s.getDate()))
            .append(',')
            .append(csv(s.getNote()))
            .append(',')
            .append(csv(s.getCreatedAt()))
            .append(',')
            .append(csv(it.getAccountId()))
            .append(',')
            .append(csv(a != null ? a.getName() : ""))
            .append(',')
            .append(csv(a != null ? a.getType() : ""))
            .append(',')
            .append(csv(a != null ? a.getOwner() : ""))
            .append(',')
            .append(it.getBalance())
            .append('\n');
      }
    }
    sb.append("\nevents_snapshot_id,snapshot_date,category,description,amount,created_at\n");
    for (Snapshot s : snaps) {
      for (SnapshotEvent e : snapshotMapper.listEvents(s.getId())) {
        sb.append(csv(s.getId()))
            .append(',')
            .append(csv(s.getDate()))
            .append(',')
            .append(csv(e.getCategory()))
            .append(',')
            .append(csv(e.getDescription()))
            .append(',')
            .append(e.getAmount())
            .append(',')
            .append(csv(e.getCreatedAt()))
            .append('\n');
      }
    }
    byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
    byte[] out = new byte[UTF8_BOM.length + body.length];
    System.arraycopy(UTF8_BOM, 0, out, 0, UTF8_BOM.length);
    System.arraycopy(body, 0, out, UTF8_BOM.length, body.length);
    return out;
  }

  private static String csv(String s) {
    if (s == null) {
      return "";
    }
    String x = s.replace("\"", "\"\"");
    if (x.contains(",") || x.contains("\n") || x.contains("\"")) {
      return "\"" + x + "\"";
    }
    return x;
  }
}
