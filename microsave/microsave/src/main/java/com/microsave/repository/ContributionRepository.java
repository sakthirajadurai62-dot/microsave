package com.microsave.repository;

import com.microsave.entity.Contribution;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface ContributionRepository extends JpaRepository<Contribution, Long> {
    List<Contribution> findByMemberIdOrderByContributionDateDesc(Long memberId);

    @Query("select coalesce(sum(c.amount),0) from Contribution c where c.member.group.id = :groupId")
    BigDecimal sumByGroup(@Param("groupId") Long groupId);

    @Query("select coalesce(sum(c.amount),0) from Contribution c where c.member.id = :memberId")
    BigDecimal sumByMember(@Param("memberId") Long memberId);
}
