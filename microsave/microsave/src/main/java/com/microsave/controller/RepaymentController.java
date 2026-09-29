package com.microsave.controller;

import com.microsave.dto.Dtos.*;
import com.microsave.service.RepaymentService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/repayments")
public class RepaymentController {
    private final RepaymentService service;
    public RepaymentController(RepaymentService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<RepaymentResponse> repay(@Valid @RequestBody RepaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.repay(req));
    }
    @GetMapping
    public List<RepaymentResponse> byLoan(@RequestParam Long loanId) { return service.findByLoan(loanId); }
}
