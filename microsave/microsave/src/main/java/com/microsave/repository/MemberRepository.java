package com.microsave.repository;

import com.microsave.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByGroupId(Long groupId);
    boolean existsByPhone(String phone);
    boolean existsByGroupId(Long groupId);
}
