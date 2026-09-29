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
public class GroupService {
    private final GroupRepository groupRepo;
    private final MemberRepository memberRepo;
    private final ContributionRepository contributionRepo;
    private final LoanRepository loanRepo;

    public GroupService(GroupRepository groupRepo, MemberRepository memberRepo,
                        ContributionRepository contributionRepo, LoanRepository loanRepo) {
        this.groupRepo = groupRepo; this.memberRepo = memberRepo;
        this.contributionRepo = contributionRepo; this.loanRepo = loanRepo;
    }

    @Transactional
    public GroupResponse create(GroupRequest req) {
        if (groupRepo.existsByNameIgnoreCase(req.name()))
            throw new BusinessRuleException("A group named '" + req.name() + "' already exists");
        SelfHelpGroup g = new SelfHelpGroup();
        g.setName(req.name().trim());
        g.setLocation(req.location());
        return GroupResponse.from(groupRepo.save(g));
    }

    public List<GroupResponse> findAll() {
        return groupRepo.findAll().stream().map(GroupResponse::from).toList();
    }

    public GroupResponse findById(Long id) { return GroupResponse.from(getOrThrow(id)); }

    @Transactional
    public GroupResponse update(Long id, GroupRequest req) {
        SelfHelpGroup g = getOrThrow(id);
        g.setName(req.name().trim());
        g.setLocation(req.location());
        return GroupResponse.from(groupRepo.save(g));
    }

    @Transactional
    public void delete(Long id) {
        SelfHelpGroup g = getOrThrow(id);
        if (memberRepo.existsByGroupId(id))
            throw new BusinessRuleException("Cannot delete a group that still has members");
        groupRepo.delete(g);
    }

    /** Pool = total contributions - outstanding loans. */
    public PoolResponse getPool(Long id) {
        SelfHelpGroup g = getOrThrow(id);
        BigDecimal contributions = contributionRepo.sumByGroup(id);
        BigDecimal outstanding = loanRepo.sumOutstandingByGroup(id, LoanStatus.ACTIVE);
        return new PoolResponse(id, g.getName(), contributions, outstanding, contributions.subtract(outstanding));
    }

    private SelfHelpGroup getOrThrow(Long id) {
        return groupRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Group", id));
    }
}
