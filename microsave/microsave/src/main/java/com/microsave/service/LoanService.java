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
public class LoanService {

    private final LoanRepository loanRepo;
    private final MemberRepository memberRepo;
    private final GroupRepository groupRepo;
    private final ContributionRepository contributionRepo;

    public LoanService(
            LoanRepository loanRepo,
            MemberRepository memberRepo,
            GroupRepository groupRepo,
            ContributionRepository contributionRepo) {

        this.loanRepo = loanRepo;
        this.memberRepo = memberRepo;
        this.groupRepo = groupRepo;
        this.contributionRepo = contributionRepo;
    }

    // =========================
    // CREATE / DISBURSE LOAN
    // =========================
    @Transactional
    public LoanResponse disburse(LoanRequest req) {

        Member m = memberRepo.findById(req.memberId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Member", req.memberId()));

        if (!m.isActive()) {
            throw new BusinessRuleException(
                    "Inactive member cannot take a loan");
        }

        // One active loan per member
        if (loanRepo.existsByMemberIdAndStatus(
                m.getId(), LoanStatus.ACTIVE)) {

            throw new BusinessRuleException(
                    "Member already has an active unpaid loan");
        }

        Long groupId = m.getGroup().getId();

        // Lock group row
        groupRepo.findByIdForUpdate(groupId);

        // Calculate available pool
        BigDecimal pool =
                contributionRepo.sumByGroup(groupId)
                        .subtract(
                                loanRepo.sumOutstandingByGroup(
                                        groupId,
                                        LoanStatus.ACTIVE)
                        );

        if (req.amount().compareTo(pool) > 0) {
            throw new BusinessRuleException(
                    "Loan amount " + req.amount()
                            + " exceeds the group's available pool of "
                            + pool);
        }

        Loan loan = new Loan();

        loan.setMember(m);
        loan.setPrincipal(req.amount());
        loan.setOutstandingBalance(req.amount());
        loan.setDisbursedDate(
                req.disbursedDate() != null
                        ? req.disbursedDate()
                        : LocalDate.now()
        );
        loan.setStatus(
                req.status() != null
                        ? req.status()
                        : LoanStatus.ACTIVE
        );

        return LoanResponse.from(
                loanRepo.save(loan)
        );
    }

    // =========================
    // GET ONE LOAN
    // =========================
    @Transactional(readOnly = true)
    public LoanResponse findById(Long id) {

        return LoanResponse.from(
                getOrThrow(id)
        );
    }

    // =========================
    // GET ALL LOANS
    // =========================
    @Transactional(readOnly = true)
    public List<LoanResponse> findAll(Long memberId) {

        List<Loan> loans =
                (memberId == null)
                        ? loanRepo.findAll()
                        : loanRepo.findByMemberId(memberId);

        return loans.stream()
                .map(LoanResponse::from)
                .toList();
    }

    // =========================
    // UPDATE LOAN
    // =========================
    @Transactional
    public LoanResponse update(Long id, LoanRequest req) {

        Loan loan = getOrThrow(id);

        Member member = memberRepo.findById(req.memberId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Member",
                                req.memberId()
                        ));

        if (!member.isActive()) {
            throw new BusinessRuleException(
                    "Inactive member cannot have a loan");
        }

        // Update values
        loan.setMember(member);

        if (req.amount() != null) {
            loan.setPrincipal(req.amount());
        }

        if (req.outstandingBalance() != null) {
            if (req.outstandingBalance()
                    .compareTo(BigDecimal.ZERO) < 0) {

                throw new BusinessRuleException(
                        "Outstanding balance cannot be negative");
            }

            loan.setOutstandingBalance(
                    req.outstandingBalance()
            );
        }

        if (req.disbursedDate() != null) {
            loan.setDisbursedDate(
                    req.disbursedDate()
            );
        }

        if (req.status() != null) {
            loan.setStatus(
                    req.status()
            );
        }

        return LoanResponse.from(
                loanRepo.save(loan)
        );
    }

    // =========================
    // DELETE LOAN
    // =========================
    @Transactional
    public void delete(Long id) {

        Loan loan = getOrThrow(id);

        loanRepo.delete(loan);
    }

    // =========================
    // FIND OR THROW
    // =========================
    private Loan getOrThrow(Long id) {

        return loanRepo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan",
                                id
                        ));
    }
}