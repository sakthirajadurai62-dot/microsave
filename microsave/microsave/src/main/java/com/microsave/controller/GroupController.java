package com.microsave.controller;

import com.microsave.dto.Dtos.*;
import com.microsave.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    private final GroupService service;
    public GroupController(GroupService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<GroupResponse> create(@Valid @RequestBody GroupRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }
    @GetMapping public List<GroupResponse> all() { return service.findAll(); }
    @GetMapping("/{id}") public GroupResponse one(@PathVariable Long id) { return service.findById(id); }
    @PutMapping("/{id}")
    public GroupResponse update(@PathVariable Long id, @Valid @RequestBody GroupRequest req) {
        return service.update(id, req);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}/pool") public PoolResponse pool(@PathVariable Long id) { return service.getPool(id); }
}
