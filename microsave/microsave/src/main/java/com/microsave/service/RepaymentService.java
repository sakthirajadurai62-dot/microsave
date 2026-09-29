package com.microsave.service;

import com.microsave.dto.Dtos.*;
import com.microsave.entity.*;
import com.microsave.exception.*;
import com.microsave.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class RepaymentService {
    private final RepaymentRepository repaymentRepo;
    private final LoanRepository loanRepo;

    public RepaymentService(RepaymentRepository repaymentRepo, LoanRepository loanRepo) {
        this.repaymentRepo = repaymentRepo; this.loanRepo = loanRepo;
    }

    /** Feature 3: log a repayment against an ACTIVE loan. */
    @Transactional
    public RepaymentResponse repay(RepaymentRequest req) {
        Loan loan = loanRepo.findById(req.loanId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan", req.loanId()));

        if (loan.getStatus() != LoanStatus.ACTIVE)
            throw new BusinessRuleException("Loan is already closed; repayment not allowed");
        if (req.amount().compareTo(loan.getOutstandingBalance()) > 0)
            throw new BusinessRuleException("Repayment " + req.amount()
                    + " exceeds outstanding balance " + loan.getOutstandingBalance());

        Repayment r = new Repayment();
        r.setLoan(loan);
        r.setAmount(req.amount());
        r.setRepaymentDate(LocalDate.now());

        BigDecimal remaining = loan.getOutstandingBalance().subtract(req.amount());
        loan.setOutstandingBalance(remaining);
        if (remaining.compareTo(BigDecimal.ZERO) == 0) loan.setStatus(LoanStatus.CLOSED);

        loanRepo.save(loan);
        return RepaymentResponse.from(repaymentRepo.save(r));
    }

    @Transactional(readOnly = true)
    public List<RepaymentResponse> findByLoan(Long loanId) {
        return repaymentRepo.findByLoanIdOrderByRepaymentDateDesc(loanId)
                .stream().map(RepaymentResponse::from).toList();
    }
}
