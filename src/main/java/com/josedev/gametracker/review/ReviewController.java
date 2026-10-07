package com.josedev.gametracker.review;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.josedev.gametracker.globals.dtos.PageDTOResponse;
import com.josedev.gametracker.review.dtos.ReviewDTORequest;
import com.josedev.gametracker.review.dtos.ReviewDTOResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "${api-endpoint}")
public class ReviewController {

    private final InterfaceReviewService service;

    public ReviewController(InterfaceReviewService service) {
        this.service = service;
    }

    // Public

    @GetMapping("reviews")
    public PageDTOResponse<ReviewDTOResponse> recent(@RequestParam(defaultValue = "0") int page) {
        return service.getRecent(page);
    }

    @GetMapping("games/{gameId}/reviews")
    public PageDTOResponse<ReviewDTOResponse> byGame(@PathVariable Long gameId,
            @RequestParam(defaultValue = "0") int page) {
        return service.getByGame(gameId, page);
    }

    @GetMapping("users/{username}/reviews")
    public PageDTOResponse<ReviewDTOResponse> byUser(@PathVariable String username,
            @RequestParam(defaultValue = "0") int page) {
        return service.getByUser(username, page);
    }

    // Logged user

    @PutMapping("games/{gameId}/review")
    public ReviewDTOResponse save(@AuthenticationPrincipal Jwt jwt, @PathVariable Long gameId,
            @Valid @RequestBody ReviewDTORequest dto) {
        return service.save(jwt.getSubject(), gameId, dto);
    }

    @DeleteMapping("games/{gameId}/review")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long gameId) {
        service.deleteMine(jwt.getSubject(), gameId);
        return ResponseEntity.noContent().build();
    }

}
