package com.microsave.controller;

import com.microsave.dto.Dtos.*;
import com.microsave.service.ContributionService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/contributions")
public class ContributionController {
    private final ContributionService service;
    public ContributionController(ContributionService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<ContributionResponse> record(@Valid @RequestBody ContributionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.record(req));
    }
    @GetMapping
    public List<ContributionResponse> byMember(@RequestParam Long memberId) {
        return service.findByMember(memberId);
    }
}
