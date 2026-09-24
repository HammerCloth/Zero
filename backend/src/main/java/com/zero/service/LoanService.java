package com.zero.service;

import com.zero.domain.Loan;
import com.zero.domain.LoanRepayment;
import com.zero.mapper.LoanMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LoanService {
  private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

  private final LoanMapper loanMapper;

  public LoanService(LoanMapper loanMapper) {
    this.loanMapper = loanMapper;
  }

  public List<Loan> listLoans(String userId, String keyword, String status) {
    String normalized = normalizeStatus(status);
    List<Loan> loans = loanMapper.listLoans(userId, blankToNull(keyword));
    loans.forEach(Loan::refreshDerived);
    if ("all".equals(normalized)) {
      return loans;
    }
    return loans.stream()
        .filter(loan -> "settled".equals(normalized) == loan.isSettled())
        .toList();
  }

  public Loan getLoan(String userId, String id) {
    Loan loan = requireLoan(userId, id);
    loan.setRepayments(loanMapper.listRepayments(id, userId));
    return loan;
  }

  @Transactional
  public Loan createLoan(
      String userId,
      String borrowerName,
      String relationship,
      Double amount,
      String loanDate,
      String dueDate,
      String note) {
    Loan loan = validLoan(borrowerName, relationship, amount, loanDate, dueDate, note);
    loan.setId(newId());
    loan.setUserId(userId);
    loanMapper.insertLoan(loan);
    return requireLoan(userId, loan.getId());
  }

  @Transactional
  public void updateLoan(
      String userId,
      String id,
      String borrowerName,
      String relationship,
      Double amount,
      String loanDate,
      String dueDate,
      String note) {
    Loan existing = requireLoan(userId, id);
    Loan next = validLoan(borrowerName, relationship, amount, loanDate, dueDate, note);
    if (money(next.getAmount()).compareTo(money(existing.getRepaidTotal())) < 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "本金不能小于已还金额");
    }
    next.setId(id);
    next.setUserId(userId);
    loanMapper.updateLoan(next);
  }

  @Transactional
  public void deleteLoan(String userId, String id) {
    if (loanMapper.deleteLoan(id, userId) == 0) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "借款不存在");
    }
  }

  public List<LoanRepayment> listRepayments(String userId, String loanId) {
    requireLoan(userId, loanId);
    return loanMapper.listRepayments(loanId, userId);
  }

  @Transactional
  public LoanRepayment createRepayment(
      String userId, String loanId, Double amount, String repayDate, String note) {
    Loan loan = requireLoan(userId, loanId);
    LoanRepayment repayment = validRepayment(amount, repayDate, note);
    if (money(repayment.getAmount()).compareTo(money(loan.getRemaining())) > 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "还款金额超过剩余本金");
    }
    repayment.setId(newId());
    repayment.setUserId(userId);
    repayment.setLoanId(loanId);
    loanMapper.insertRepayment(repayment);
    return loanMapper.findRepayment(repayment.getId(), userId);
  }

  @Transactional
  public void updateRepayment(
      String userId, String loanId, String repaymentId, Double amount, String repayDate, String note) {
    Loan loan = requireLoan(userId, loanId);
    LoanRepayment existing = requireRepayment(userId, loanId, repaymentId);
    LoanRepayment next = validRepayment(amount, repayDate, note);
    BigDecimal remainingWithoutThis =
        money(loan.getRemaining()).add(money(existing.getAmount()));
    if (money(next.getAmount()).compareTo(remainingWithoutThis) > 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "还款金额超过剩余本金");
    }
    next.setId(repaymentId);
    next.setUserId(userId);
    next.setLoanId(loanId);
    loanMapper.updateRepayment(next);
  }

  @Transactional
  public void deleteRepayment(String userId, String loanId, String repaymentId) {
    requireLoan(userId, loanId);
    requireRepayment(userId, loanId, repaymentId);
    loanMapper.deleteRepayment(repaymentId, userId);
  }

  public Map<String, Object> stats(String userId, Integer year) {
    int y = year == null ? LocalDate.now().getYear() : year;
    if (y < 2000 || y > 2100) {
      throw badRequest();
    }
    List<Loan> loans = listLoans(userId, null, "all");
    BigDecimal outstanding = ZERO;
    BigDecimal principal = ZERO;
    int settledCount = 0;
    for (Loan loan : loans) {
      outstanding = outstanding.add(money(loan.getRemaining()));
      principal = principal.add(money(loan.getAmount()));
      if (loan.isSettled()) {
        settledCount++;
      }
    }
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("year", y);
    out.put("loanCount", loans.size());
    out.put("openCount", loans.size() - settledCount);
    out.put("settledCount", settledCount);
    out.put("principalTotal", principal.doubleValue());
    out.put("outstandingTotal", outstanding.doubleValue());
    out.put("repaidThisYear", money(loanMapper.repaidTotalForYear(userId, String.valueOf(y))).doubleValue());
    return out;
  }

  private Loan requireLoan(String userId, String id) {
    if (id == null || id.isBlank()) {
      throw badRequest();
    }
    Loan loan = loanMapper.findLoan(id, userId);
    if (loan == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "借款不存在");
    }
    loan.refreshDerived();
    return loan;
  }

  private LoanRepayment requireRepayment(String userId, String loanId, String repaymentId) {
    if (repaymentId == null || repaymentId.isBlank()) {
      throw badRequest();
    }
    LoanRepayment repayment = loanMapper.findRepayment(repaymentId, userId);
    if (repayment == null || !loanId.equals(repayment.getLoanId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "还款记录不存在");
    }
    return repayment;
  }

  private Loan validLoan(
      String borrowerName, String relationship, Double amount, String loanDate, String dueDate, String note) {
    requireDate(loanDate);
    String due = blankToNull(dueDate);
    if (due != null) {
      requireDate(due);
    }
    Loan loan = new Loan();
    loan.setBorrowerName(requireText(borrowerName));
    loan.setRelationship(blankToNull(relationship));
    loan.setAmount(requirePositive(amount));
    loan.setLoanDate(loanDate);
    loan.setDueDate(due);
    loan.setNote(blankToNull(note));
    return loan;
  }

  private LoanRepayment validRepayment(Double amount, String repayDate, String note) {
    requireDate(repayDate);
    LoanRepayment repayment = new LoanRepayment();
    repayment.setAmount(requirePositive(amount));
    repayment.setRepayDate(repayDate);
    repayment.setNote(blankToNull(note));
    return repayment;
  }

  private static String normalizeStatus(String status) {
    String value = blankToNull(status);
    if (value == null || "all".equals(value)) {
      return "all";
    }
    if ("open".equals(value) || "settled".equals(value)) {
      return value;
    }
    throw badRequest();
  }

  private static double requirePositive(Double amount) {
    if (amount == null || !Double.isFinite(amount) || amount <= 0) {
      throw badRequest();
    }
    return money(amount).doubleValue();
  }

  private static void requireDate(String value) {
    try {
      LocalDate.parse(value);
    } catch (RuntimeException ex) {
      throw badRequest();
    }
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

  private static BigDecimal money(double value) {
    return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
  }

  private static ResponseStatusException badRequest() {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
  }

  private static String newId() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
