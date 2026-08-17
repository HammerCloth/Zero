package com.zero.service;

import com.zero.domain.GiftOccasionStat;
import com.zero.domain.GiftRecipient;
import com.zero.domain.GiftRecord;
import com.zero.mapper.GiftMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GiftService {
  private final GiftMapper giftMapper;

  public GiftService(GiftMapper giftMapper) {
    this.giftMapper = giftMapper;
  }

  public List<GiftRecipient> listRecipients(String userId, boolean includeInactive) {
    return giftMapper.listRecipients(userId, includeInactive);
  }

  public List<GiftRecord> listRecords(String userId, Integer year, String recipientId, String keyword) {
    if (year != null && (year < 2000 || year > 2100)) {
      throw badRequest();
    }
    return giftMapper.listRecords(userId, year == null ? null : String.valueOf(year), blankToNull(recipientId), blankToNull(keyword));
  }

  public GiftRecipient createRecipient(String userId, String name, String relationship, String note) {
    GiftRecipient recipient = new GiftRecipient();
    recipient.setId(newId());
    recipient.setUserId(userId);
    recipient.setName(requireText(name));
    recipient.setRelationship(blankToNull(relationship));
    recipient.setNote(blankToNull(note));
    recipient.setActive(true);
    try {
      giftMapper.insertRecipient(recipient);
    } catch (RuntimeException ex) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "该礼金对象已存在");
    }
    return giftMapper.findRecipient(recipient.getId(), userId);
  }

  @Transactional
  public void updateRecipient(String userId, String id, String name, String relationship, String note) {
    GiftRecipient recipient = requireRecipient(userId, id);
    recipient.setName(requireText(name));
    recipient.setRelationship(blankToNull(relationship));
    recipient.setNote(blankToNull(note));
    try {
      giftMapper.updateRecipient(recipient);
    } catch (RuntimeException ex) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "该礼金对象已存在");
    }
  }

  @Transactional
  public void deactivateRecipient(String userId, String id) {
    requireRecipient(userId, id);
    giftMapper.deactivateRecipient(id, userId);
  }

  @Transactional
  public GiftRecord createRecord(
      String userId, String recipientId, String occasion, String giftDate, Double amount, String paymentMethod, String note) {
    GiftRecord record = validRecord(userId, recipientId, occasion, giftDate, amount, paymentMethod, note);
    record.setId(newId());
    record.setUserId(userId);
    giftMapper.insertRecord(record);
    return giftMapper.findRecord(record.getId(), userId);
  }

  @Transactional
  public void updateRecord(
      String userId, String id, String recipientId, String occasion, String giftDate, Double amount, String paymentMethod, String note) {
    GiftRecord existing = giftMapper.findRecord(id, userId);
    if (existing == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "礼金记录不存在");
    }
    GiftRecord next = validRecord(userId, recipientId, occasion, giftDate, amount, paymentMethod, note);
    next.setId(id);
    next.setUserId(userId);
    giftMapper.updateRecord(next);
  }

  @Transactional
  public void deleteRecord(String userId, String id) {
    if (giftMapper.deleteRecord(id, userId) == 0) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "礼金记录不存在");
    }
  }

  public List<GiftOccasionStat> statsByOccasion(String userId, int year) {
    if (year < 2000 || year > 2100) {
      throw badRequest();
    }
    return giftMapper.statsByOccasion(userId, String.valueOf(year));
  }

  public int countRecipientsForYear(String userId, int year) {
    return giftMapper.countRecipientsForYear(userId, String.valueOf(year));
  }

  private GiftRecord validRecord(
      String userId, String recipientId, String occasion, String giftDate, Double amount, String paymentMethod, String note) {
    GiftRecipient recipient = requireRecipient(userId, recipientId);
    if (!recipient.isActive()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "礼金对象已停用");
    }
    if (amount == null || !Double.isFinite(amount) || amount <= 0) {
      throw badRequest();
    }
    try {
      LocalDate.parse(giftDate);
    } catch (RuntimeException ex) {
      throw badRequest();
    }
    GiftRecord record = new GiftRecord();
    record.setGiftRecipientId(recipientId);
    record.setOccasion(requireText(occasion));
    record.setGiftDate(giftDate);
    record.setAmount(amount);
    record.setPaymentMethod(blankToNull(paymentMethod));
    record.setNote(blankToNull(note));
    return record;
  }

  private GiftRecipient requireRecipient(String userId, String id) {
    if (id == null || id.isBlank()) {
      throw badRequest();
    }
    GiftRecipient recipient = giftMapper.findRecipient(id, userId);
    if (recipient == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "礼金对象不存在");
    }
    return recipient;
  }

  private static String requireText(String value) {
    String text = blankToNull(value);
    if (text == null || text.length() > 100) {
      throw badRequest();
    }
    return text;
  }

  private static String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String trimmed = value.trim();
    if (trimmed.length() > 1000) {
      throw badRequest();
    }
    return trimmed;
  }

  private static ResponseStatusException badRequest() {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
  }

  private static String newId() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
