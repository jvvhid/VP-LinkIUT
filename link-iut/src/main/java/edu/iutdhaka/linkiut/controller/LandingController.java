package edu.iutdhaka.linkiut.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@Controller
public class LandingController {

    @GetMapping("/")
    public String landing(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            // Already logged in, redirect to feed
            return "redirect:/feed";
        }
        return "landing";
    }
}
