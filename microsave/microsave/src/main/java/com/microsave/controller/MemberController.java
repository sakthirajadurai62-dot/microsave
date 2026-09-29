package com.microsave.controller;

import com.microsave.dto.Dtos.*;
import com.microsave.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService service;
    public MemberController(MemberService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }
    @GetMapping public List<MemberResponse> all(@RequestParam(required = false) Long groupId) {
        return service.findAll(groupId);
    }
    @GetMapping("/{id}") public MemberResponse one(@PathVariable Long id) { return service.findById(id); }
    @PutMapping("/{id}")
    public MemberResponse update(@PathVariable Long id, @Valid @RequestBody MemberRequest req) {
        return service.update(id, req);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}/summary") public MemberSummaryResponse summary(@PathVariable Long id) {
        return service.summary(id);
    }
}
