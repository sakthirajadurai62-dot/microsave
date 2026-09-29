package com.microsave.repository;

import com.microsave.entity.Loan;
import com.microsave.entity.LoanStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    boolean existsByMemberIdAndStatus(Long memberId, LoanStatus status);
    List<Loan> findByMemberId(Long memberId);

    @Query("select coalesce(sum(l.outstandingBalance),0) from Loan l " +
           "where l.member.group.id = :groupId and l.status = :status")
    BigDecimal sumOutstandingByGroup(@Param("groupId") Long groupId, @Param("status") LoanStatus status);

    @Query("select coalesce(sum(l.outstandingBalance),0) from Loan l " +
           "where l.member.id = :memberId and l.status = :status")
    BigDecimal sumOutstandingByMember(@Param("memberId") Long memberId, @Param("status") LoanStatus status);
}
