package com.josedev.gametracker.game;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.josedev.gametracker.game.dtos.GameDTORequest;
import com.josedev.gametracker.game.dtos.GameDetailDTOResponse;
import com.josedev.gametracker.game.dtos.GameSummaryDTOResponse;
import com.josedev.gametracker.globals.dtos.PageDTOResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "${api-endpoint}/games")
public class GameController {

    private final InterfaceGameService service;

    public GameController(InterfaceGameService service) {
        this.service = service;
    }

    @GetMapping("")
    public PageDTOResponse<GameSummaryDTOResponse> search(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page) {
        return service.search(search, Math.max(page, 0));
    }

    @GetMapping("trending")
    public List<GameSummaryDTOResponse> trending() {
        return service.getTrending();
    }

    @GetMapping("{id}")
    public GameDetailDTOResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("rawg/{rawgId}")
    public GameDetailDTOResponse getByRawgId(@PathVariable Long rawgId) {
        return service.getOrImportByRawgId(rawgId);
    }

    @PostMapping("")
    public ResponseEntity<GameDetailDTOResponse> create(@Valid @RequestBody GameDTORequest dto) {
        GameDetailDTOResponse created = service.create(dto);
        return ResponseEntity.created(URI.create("/games/" + created.id())).body(created);
    }

    @PutMapping("{id}")
    public GameDetailDTOResponse update(@PathVariable Long id, @Valid @RequestBody GameDTORequest dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
