package com.microsave.dto;

import com.microsave.entity.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * All request/response DTOs in one file.
 */
public final class Dtos {

    private Dtos() {
    }

    // =====================================================
    // REQUEST DTOs
    // =====================================================

    public record GroupRequest(

            @NotBlank(message = "Group name is required")
            String name,

            String location

    ) {
    }


    public record MemberRequest(

            @NotBlank(message = "Member name is required")
            String name,

            @NotBlank(message = "Phone is required")
            @Pattern(
                    regexp = "\\d{10}",
                    message = "Phone must be 10 digits"
            )
            String phone,

            @NotNull(message = "groupId is required")
            Long groupId,

            Boolean active

    ) {
    }


    public record ContributionRequest(

            @NotNull(message = "memberId is required")
            Long memberId,

            @NotNull(message = "Amount is required")
            @Positive(message = "Amount must be positive")
            BigDecimal amount,

            LocalDate contributionDate

    ) {
    }


    // =====================================================
    // LOAN REQUEST
    // =====================================================

    public record LoanRequest(

            @NotNull(message = "memberId is required")
            Long memberId,

            @NotNull(message = "Amount is required")
            @Positive(message = "Amount must be positive")
            BigDecimal amount,

            @PositiveOrZero(message =
                    "Outstanding balance cannot be negative")
            BigDecimal outstandingBalance,

            LocalDate disbursedDate,

            LoanStatus status

    ) {
    }


    public record RepaymentRequest(

            @NotNull(message = "loanId is required")
            Long loanId,

            @NotNull(message = "Amount is required")
            @Positive(message = "Amount must be positive")
            BigDecimal amount

    ) {
    }


    // =====================================================
    // RESPONSE DTOs
    // =====================================================

    public record GroupResponse(

            Long id,
            String name,
            String location,
            LocalDate createdDate

    ) {

        public static GroupResponse from(SelfHelpGroup g) {

            return new GroupResponse(
                    g.getId(),
                    g.getName(),
                    g.getLocation(),
                    g.getCreatedDate()
            );
        }
    }


    public record PoolResponse(

            Long groupId,
            String groupName,
            BigDecimal totalContributions,
            BigDecimal outstandingLoans,
            BigDecimal availablePool

    ) {
    }


    public record MemberResponse(

            Long id,
            String name,
            String phone,
            boolean active,
            LocalDate joinedDate,
            Long groupId

    ) {

        public static MemberResponse from(Member m) {

            return new MemberResponse(
                    m.getId(),
                    m.getName(),
                    m.getPhone(),
                    m.isActive(),
                    m.getJoinedDate(),
                    m.getGroup().getId()
            );
        }
    }


    public record MemberSummaryResponse(

            Long memberId,
            String name,
            BigDecimal totalSavings,
            BigDecimal outstandingLoan,
            BigDecimal netSavings

    ) {
    }


    public record ContributionResponse(

            Long id,
            Long memberId,
            BigDecimal amount,
            LocalDate date

    ) {

        public static ContributionResponse from(
                Contribution c) {

            return new ContributionResponse(
                    c.getId(),
                    c.getMember().getId(),
                    c.getAmount(),
                    c.getContributionDate()
            );
        }
    }


    public record LoanResponse(

            Long id,
            Long memberId,
            BigDecimal principal,
            BigDecimal outstandingBalance,
            LoanStatus status,
            LocalDate disbursedDate

    ) {

        public static LoanResponse from(Loan l) {

            return new LoanResponse(
                    l.getId(),
                    l.getMember().getId(),
                    l.getPrincipal(),
                    l.getOutstandingBalance(),
                    l.getStatus(),
                    l.getDisbursedDate()
            );
        }
    }


    public record RepaymentResponse(

            Long id,
            Long loanId,
            BigDecimal amount,
            LocalDate date,
            BigDecimal remainingBalance,
            LoanStatus loanStatus

    ) {

        public static RepaymentResponse from(
                Repayment r) {

            return new RepaymentResponse(
                    r.getId(),
                    r.getLoan().getId(),
                    r.getAmount(),
                    r.getRepaymentDate(),
                    r.getLoan().getOutstandingBalance(),
                    r.getLoan().getStatus()
            );
        }
    }
}