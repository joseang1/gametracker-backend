package com.josedev.gametracker.gamelog;

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

import com.josedev.gametracker.gamelog.dtos.GameLogDTORequest;
import com.josedev.gametracker.gamelog.dtos.GameLogDTOResponse;
import com.josedev.gametracker.gamelog.dtos.MyGameEntryDTOResponse;
import com.josedev.gametracker.globals.dtos.PageDTOResponse;
import com.josedev.gametracker.review.InterfaceReviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "${api-endpoint}")
public class GameLogController {

    private final InterfaceGameLogService service;
    private final InterfaceReviewService reviewService;

    public GameLogController(InterfaceGameLogService service, InterfaceReviewService reviewService) {
        this.service = service;
        this.reviewService = reviewService;
    }

    // Public: a user's library. ?status=COMPLETED or ?favorites=true
    @GetMapping("users/{username}/logs")
    public PageDTOResponse<GameLogDTOResponse> byUser(@PathVariable String username,
            @RequestParam(required = false) LogStatus status,
            @RequestParam(defaultValue = "false") boolean favorites,
            @RequestParam(defaultValue = "0") int page) {
        return service.getByUser(username, status, favorites, page);
    }

    // Logged user: log + review for one game (used to fill the "log game" form)
    @GetMapping("games/{gameId}/me")
    public MyGameEntryDTOResponse myEntry(@AuthenticationPrincipal Jwt jwt, @PathVariable Long gameId) {
        String username = jwt.getSubject();
        return new MyGameEntryDTOResponse(
                service.getMine(username, gameId).orElse(null),
                reviewService.getMine(username, gameId).orElse(null));
    }

    @PutMapping("games/{gameId}/log")
    public GameLogDTOResponse save(@AuthenticationPrincipal Jwt jwt, @PathVariable Long gameId,
            @Valid @RequestBody GameLogDTORequest dto) {
        return service.save(jwt.getSubject(), gameId, dto);
    }

    @DeleteMapping("games/{gameId}/log")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long gameId) {
        service.deleteMine(jwt.getSubject(), gameId);
        return ResponseEntity.noContent().build();
    }

}
