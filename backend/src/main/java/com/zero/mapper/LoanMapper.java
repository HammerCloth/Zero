package com.zero.mapper;

import com.zero.domain.Loan;
import com.zero.domain.LoanRepayment;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LoanMapper {

  @Select("SELECT l.id, l.user_id AS userId, l.borrower_name AS borrowerName, l.relationship, "
      + "l.amount, l.loan_date AS loanDate, l.due_date AS dueDate, l.note, "
      + "l.created_at AS createdAt, l.updated_at AS updatedAt, "
      + "COALESCE(SUM(r.amount), 0) AS repaidTotal, COUNT(r.id) AS repaymentCount "
      + "FROM loans l LEFT JOIN loan_repayments r ON r.loan_id = l.id "
      + "WHERE l.user_id = #{userId} "
      + "AND (#{keyword} IS NULL OR l.borrower_name LIKE '%' || #{keyword} || '%' "
      + "OR COALESCE(l.relationship, '') LIKE '%' || #{keyword} || '%' "
      + "OR COALESCE(l.note, '') LIKE '%' || #{keyword} || '%') "
      + "GROUP BY l.id "
      + "ORDER BY l.loan_date DESC, l.created_at DESC")
  List<Loan> listLoans(@Param("userId") String userId, @Param("keyword") String keyword);

  @Select("SELECT l.id, l.user_id AS userId, l.borrower_name AS borrowerName, l.relationship, "
      + "l.amount, l.loan_date AS loanDate, l.due_date AS dueDate, l.note, "
      + "l.created_at AS createdAt, l.updated_at AS updatedAt, "
      + "COALESCE(SUM(r.amount), 0) AS repaidTotal, COUNT(r.id) AS repaymentCount "
      + "FROM loans l LEFT JOIN loan_repayments r ON r.loan_id = l.id "
      + "WHERE l.id = #{id} AND l.user_id = #{userId} "
      + "GROUP BY l.id")
  Loan findLoan(@Param("id") String id, @Param("userId") String userId);

  @Insert("INSERT INTO loans(id, user_id, borrower_name, relationship, amount, loan_date, due_date, note) "
      + "VALUES(#{id}, #{userId}, #{borrowerName}, #{relationship}, #{amount}, #{loanDate}, #{dueDate}, #{note})")
  int insertLoan(Loan loan);

  @Update("UPDATE loans SET borrower_name = #{borrowerName}, relationship = #{relationship}, amount = #{amount}, "
      + "loan_date = #{loanDate}, due_date = #{dueDate}, note = #{note}, updated_at = CURRENT_TIMESTAMP "
      + "WHERE id = #{id} AND user_id = #{userId}")
  int updateLoan(Loan loan);

  @Delete("DELETE FROM loans WHERE id = #{id} AND user_id = #{userId}")
  int deleteLoan(@Param("id") String id, @Param("userId") String userId);

  @Select("SELECT id, user_id AS userId, loan_id AS loanId, amount, repay_date AS repayDate, note, "
      + "created_at AS createdAt FROM loan_repayments "
      + "WHERE loan_id = #{loanId} AND user_id = #{userId} "
      + "ORDER BY repay_date DESC, created_at DESC")
  List<LoanRepayment> listRepayments(@Param("loanId") String loanId, @Param("userId") String userId);

  @Select("SELECT id, user_id AS userId, loan_id AS loanId, amount, repay_date AS repayDate, note, "
      + "created_at AS createdAt FROM loan_repayments "
      + "WHERE id = #{id} AND user_id = #{userId}")
  LoanRepayment findRepayment(@Param("id") String id, @Param("userId") String userId);

  @Insert("INSERT INTO loan_repayments(id, user_id, loan_id, amount, repay_date, note) "
      + "VALUES(#{id}, #{userId}, #{loanId}, #{amount}, #{repayDate}, #{note})")
  int insertRepayment(LoanRepayment repayment);

  @Update("UPDATE loan_repayments SET amount = #{amount}, repay_date = #{repayDate}, note = #{note} "
      + "WHERE id = #{id} AND user_id = #{userId}")
  int updateRepayment(LoanRepayment repayment);

  @Delete("DELETE FROM loan_repayments WHERE id = #{id} AND user_id = #{userId}")
  int deleteRepayment(@Param("id") String id, @Param("userId") String userId);

  @Select("SELECT COALESCE(SUM(amount), 0) FROM loan_repayments "
      + "WHERE user_id = #{userId} AND substr(repay_date, 1, 4) = #{year}")
  double repaidTotalForYear(@Param("userId") String userId, @Param("year") String year);
}
