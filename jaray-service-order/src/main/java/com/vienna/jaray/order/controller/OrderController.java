package com.vienna.jaray.order.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    @GetMapping("addOrder")
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    public String addOrder(){
        return "addOrder";
    }
}
