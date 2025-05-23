package com.example.demo.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import com.example.demo.entities.RoleEntity;
import com.example.demo.service.RoleServcie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
public class RoleController {

    private final RoleServcie roleServcie;

    @GetMapping("/roles")
    public List<RoleEntity> getRoles() {

        return roleServcie.getAllRolles();
    }

}
