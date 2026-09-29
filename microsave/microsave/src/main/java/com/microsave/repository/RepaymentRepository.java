package com.microsave.repository;

import com.microsave.entity.Repayment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {
    List<Repayment> findByLoanIdOrderByRepaymentDateDesc(Long loanId);
}
