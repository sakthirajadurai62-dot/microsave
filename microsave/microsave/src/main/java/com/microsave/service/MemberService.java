package com.microsave.service;

import com.microsave.dto.Dtos.*;
import com.microsave.entity.*;
import com.microsave.exception.*;
import com.microsave.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class MemberService {
    private final MemberRepository memberRepo;
    private final GroupRepository groupRepo;
    private final ContributionRepository contributionRepo;
    private final LoanRepository loanRepo;

    public MemberService(MemberRepository memberRepo, GroupRepository groupRepo,
                         ContributionRepository contributionRepo, LoanRepository loanRepo) {
        this.memberRepo = memberRepo; this.groupRepo = groupRepo;
        this.contributionRepo = contributionRepo; this.loanRepo = loanRepo;
    }

    @Transactional
    public MemberResponse create(MemberRequest req) {
        if (memberRepo.existsByPhone(req.phone()))
            throw new BusinessRuleException("Phone number already registered");
        SelfHelpGroup group = groupRepo.findById(req.groupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group", req.groupId()));
        Member m = new Member();
        m.setName(req.name().trim());
        m.setPhone(req.phone());
        m.setGroup(group);
        return MemberResponse.from(memberRepo.save(m));
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> findAll(Long groupId) {
        List<Member> list = (groupId == null) ? memberRepo.findAll() : memberRepo.findByGroupId(groupId);
        return list.stream().map(MemberResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public MemberResponse findById(Long id) { return MemberResponse.from(getOrThrow(id)); }

    @Transactional
    public MemberResponse update(Long id, MemberRequest req) {
        Member m = getOrThrow(id);
        m.setName(req.name().trim());
        if (!m.getPhone().equals(req.phone()) && memberRepo.existsByPhone(req.phone()))
            throw new BusinessRuleException("Phone number already registered");
        m.setPhone(req.phone());
        if (req.active() != null) m.setActive(req.active());
        return MemberResponse.from(memberRepo.save(m));
    }

    /** DELETE = soft delete (deactivate) so financial history is never lost. */
    @Transactional
    public void deactivate(Long id) {
        Member m = getOrThrow(id);
        if (loanRepo.existsByMemberIdAndStatus(id, LoanStatus.ACTIVE))
            throw new BusinessRuleException("Cannot deactivate a member with an active loan");
        m.setActive(false);
        memberRepo.save(m);
    }

    /** Feature 4: current savings balance and outstanding loan. */
    @Transactional(readOnly = true)
    public MemberSummaryResponse summary(Long id) {
        Member m = getOrThrow(id);
        BigDecimal savings = contributionRepo.sumByMember(id);
        BigDecimal outstanding = loanRepo.sumOutstandingByMember(id, LoanStatus.ACTIVE);
        return new MemberSummaryResponse(id, m.getName(), savings, outstanding, savings.subtract(outstanding));
    }

    private Member getOrThrow(Long id) {
        return memberRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Member", id));
    }
}
