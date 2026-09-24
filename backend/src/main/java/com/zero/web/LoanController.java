package com.zero.web;

import com.zero.domain.Loan;
import com.zero.domain.LoanRepayment;
import com.zero.service.LoanService;
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
@RequestMapping("/api/v1/loans")
public class LoanController {
  private final LoanService loanService;

  public LoanController(LoanService loanService) {
    this.loanService = loanService;
  }

  public record LoanBody(
      String borrowerName, String relationship, Double amount, String loanDate, String dueDate, String note) {}

  public record RepaymentBody(Double amount, String repayDate, String note) {}

  @GetMapping("/stats")
  public Map<String, Object> stats(@RequestParam(value = "year", required = false) Integer year) {
    return loanService.stats(CurrentUser.require().userId(), year);
  }

  @GetMapping
  public Map<String, List<Loan>> list(
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "status", required = false) String status) {
    return Map.of("loans", loanService.listLoans(CurrentUser.require().userId(), keyword, status));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Loan> create(@RequestBody LoanBody body) {
    Loan loan = loanService.createLoan(
        CurrentUser.require().userId(),
        body.borrowerName(),
        body.relationship(),
        body.amount(),
        body.loanDate(),
        body.dueDate(),
        body.note());
    return Map.of("loan", loan);
  }

  @GetMapping("/{id}")
  public Map<String, Loan> get(@PathVariable String id) {
    return Map.of("loan", loanService.getLoan(CurrentUser.require().userId(), id));
  }

  @PutMapping("/{id}")
  public Map<String, Boolean> update(@PathVariable String id, @RequestBody LoanBody body) {
    loanService.updateLoan(
        CurrentUser.require().userId(),
        id,
        body.borrowerName(),
        body.relationship(),
        body.amount(),
        body.loanDate(),
        body.dueDate(),
        body.note());
    return Map.of("ok", true);
  }

  @DeleteMapping("/{id}")
  public Map<String, Boolean> delete(@PathVariable String id) {
    loanService.deleteLoan(CurrentUser.require().userId(), id);
    return Map.of("ok", true);
  }

  @GetMapping("/{id}/repayments")
  public Map<String, List<LoanRepayment>> listRepayments(@PathVariable String id) {
    return Map.of("repayments", loanService.listRepayments(CurrentUser.require().userId(), id));
  }

  @PostMapping("/{id}/repayments")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, LoanRepayment> createRepayment(
      @PathVariable String id, @RequestBody RepaymentBody body) {
    LoanRepayment repayment = loanService.createRepayment(
        CurrentUser.require().userId(), id, body.amount(), body.repayDate(), body.note());
    return Map.of("repayment", repayment);
  }

  @PutMapping("/{id}/repayments/{repaymentId}")
  public Map<String, Boolean> updateRepayment(
      @PathVariable String id, @PathVariable String repaymentId, @RequestBody RepaymentBody body) {
    loanService.updateRepayment(
        CurrentUser.require().userId(), id, repaymentId, body.amount(), body.repayDate(), body.note());
    return Map.of("ok", true);
  }

  @DeleteMapping("/{id}/repayments/{repaymentId}")
  public Map<String, Boolean> deleteRepayment(@PathVariable String id, @PathVariable String repaymentId) {
    loanService.deleteRepayment(CurrentUser.require().userId(), id, repaymentId);
    return Map.of("ok", true);
  }
}
