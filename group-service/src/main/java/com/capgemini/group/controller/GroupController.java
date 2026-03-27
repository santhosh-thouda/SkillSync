package com.capgemini.group.controller;

import com.capgemini.group.dto.GroupDto;
import com.capgemini.group.dto.GroupRequest;
import com.capgemini.group.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @accessControlService.canCreateGroup(#request, authentication)")
    public ResponseEntity<GroupDto> createGroup(@Valid @RequestBody GroupRequest request) {
        return new ResponseEntity<>(groupService.createGroup(request), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("hasRole('ADMIN') or @accessControlService.isCurrentUser(#userId, authentication)")
    public ResponseEntity<GroupDto> joinGroup(@PathVariable Long id, @RequestParam Long userId) {
        return ResponseEntity.ok(groupService.joinGroup(id, userId));
    }

    @PostMapping("/{id}/leave")
    @PreAuthorize("hasRole('ADMIN') or @accessControlService.isCurrentUser(#userId, authentication)")
    public ResponseEntity<GroupDto> leaveGroup(@PathVariable Long id, @RequestParam Long userId) {
        return ResponseEntity.ok(groupService.leaveGroup(id, userId));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GroupDto>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GroupDto> getGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accessControlService.isGroupOwner(#id, authentication)")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }
}
