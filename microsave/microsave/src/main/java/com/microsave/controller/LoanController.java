package com.microsave.controller;

import com.microsave.dto.Dtos.*;
import com.microsave.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
    private final LoanService service;
    public LoanController(LoanService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<LoanResponse> disburse(@Valid @RequestBody LoanRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.disburse(req));
    }
    @GetMapping public List<LoanResponse> all(@RequestParam(required = false) Long memberId) {
        return service.findAll(memberId);
    }
    @GetMapping("/{id}") public LoanResponse one(@PathVariable Long id) { return service.findById(id); }

    @PutMapping("/{id}")
    public LoanResponse update(
            @PathVariable Long id,
            @Valid @RequestBody LoanRequest req) {
        return service.update(id, req);
    }

}
