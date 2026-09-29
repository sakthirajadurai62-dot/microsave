package com.microsave.service;

import com.microsave.dto.Dtos.*;
import com.microsave.entity.*;
import com.microsave.exception.*;
import com.microsave.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ContributionService {

    private final ContributionRepository contributionRepo;
    private final MemberRepository memberRepo;

    public ContributionService(
            ContributionRepository contributionRepo,
            MemberRepository memberRepo) {

        this.contributionRepo = contributionRepo;
        this.memberRepo = memberRepo;
    }

    // =========================
    // CREATE CONTRIBUTION
    // =========================
    @Transactional
    public ContributionResponse record(
            ContributionRequest req) {

        Member m = memberRepo.findById(req.memberId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Member",
                                req.memberId()
                        ));

        if (!m.isActive()) {
            throw new BusinessRuleException(
                    "Inactive member cannot contribute");
        }

        LocalDate date =
                req.contributionDate() != null
                        ? req.contributionDate()
                        : LocalDate.now();

        if (date.isAfter(LocalDate.now())) {
            throw new BusinessRuleException(
                    "Contribution date cannot be in the future");
        }

        Contribution c = new Contribution();

        c.setMember(m);
        c.setAmount(req.amount());
        c.setContributionDate(date);

        return ContributionResponse.from(
                contributionRepo.save(c)
        );
    }

    // =========================
    // GET CONTRIBUTIONS
    // =========================
    @Transactional(readOnly = true)
    public List<ContributionResponse> findByMember(
            Long memberId) {

        return contributionRepo
                .findByMemberIdOrderByContributionDateDesc(memberId)
                .stream()
                .map(ContributionResponse::from)
                .toList();
    }

    // =========================
    // UPDATE CONTRIBUTION
    // =========================
    @Transactional
    public ContributionResponse update(
            Long id,
            ContributionRequest req) {

        Contribution contribution =
                contributionRepo.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Contribution",
                                        id
                                ));

        Member member =
                memberRepo.findById(req.memberId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Member",
                                        req.memberId()
                                ));

        if (!member.isActive()) {
            throw new BusinessRuleException(
                    "Inactive member cannot contribute");
        }

        if (req.amount() == null) {
            throw new BusinessRuleException(
                    "Contribution amount is required");
        }

        LocalDate date =
                req.contributionDate() != null
                        ? req.contributionDate()
                        : contribution.getContributionDate();

        if (date.isAfter(LocalDate.now())) {
            throw new BusinessRuleException(
                    "Contribution date cannot be in the future");
        }

        contribution.setMember(member);
        contribution.setAmount(req.amount());
        contribution.setContributionDate(date);

        return ContributionResponse.from(
                contributionRepo.save(contribution)
        );
    }

    // =========================
    // DELETE CONTRIBUTION
    // =========================
    @Transactional
    public void delete(Long id) {

        Contribution contribution =
                contributionRepo.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Contribution",
                                        id
                                ));

        contributionRepo.delete(contribution);
    }
}