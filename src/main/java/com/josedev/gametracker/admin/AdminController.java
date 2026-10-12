package com.josedev.gametracker.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.josedev.gametracker.admin.dtos.AdminStatsDTOResponse;
import com.josedev.gametracker.globals.dtos.PageDTOResponse;
import com.josedev.gametracker.review.InterfaceReviewService;
import com.josedev.gametracker.review.dtos.ReviewDTOResponse;
import com.josedev.gametracker.user.dtos.UserAdminDTOResponse;

@RestController
@RequestMapping(path = "${api-endpoint}/admin")
public class AdminController {

    private final InterfaceAdminService service;
    private final InterfaceReviewService reviewService;

    public AdminController(InterfaceAdminService service, InterfaceReviewService reviewService) {
        this.service = service;
        this.reviewService = reviewService;
    }

    @GetMapping("stats")
    public AdminStatsDTOResponse stats() {
        return service.getStats();
    }

    @GetMapping("users")
    public PageDTOResponse<UserAdminDTOResponse> users(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page) {
        return service.getUsers(search, page);
    }

    @PatchMapping("users/{id}/ban")
    public UserAdminDTOResponse ban(@PathVariable Long id) {
        return service.setBanned(id, true);
    }

    @PatchMapping("users/{id}/unban")
    public UserAdminDTOResponse unban(@PathVariable Long id) {
        return service.setBanned(id, false);
    }

    @GetMapping("reviews")
    public PageDTOResponse<ReviewDTOResponse> reviews(@RequestParam(defaultValue = "0") int page) {
        return reviewService.getRecent(page);
    }

    @DeleteMapping("reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
