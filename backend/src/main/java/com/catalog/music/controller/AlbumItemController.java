package com.catalog.music.controller;

import com.catalog.music.dto.AlbumRequestDto;
import com.catalog.music.dto.AlbumResponseDto;
import com.catalog.music.dto.ITunesSearchResponseDto;
import com.catalog.music.service.AlbumItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/albums")
@CrossOrigin(origins = "*")
public class AlbumItemController {

    private final AlbumItemService albumItemService;

    public AlbumItemController(AlbumItemService albumItemService) {
        this.albumItemService = albumItemService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchExternalAlbums(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "query", required = false) String query) {

        // Fallback: Accept either 'q' or 'query' parameter from frontend
        String searchTerm = (q != null && !q.trim().isEmpty()) ? q : query;

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            ITunesSearchResponseDto emptyResponse = new ITunesSearchResponseDto();
            emptyResponse.setResultCount(0);
            emptyResponse.setResults(Collections.emptyList());
            return ResponseEntity.ok(emptyResponse);
        }

        try {
            ITunesSearchResponseDto response = albumItemService.searchExternalAlbums(searchTerm.trim());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // Logs full stack trace to Railway console
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching catalog: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<AlbumResponseDto>> getUserAlbums(Authentication authentication) {
        List<AlbumResponseDto> albums = albumItemService.getUserAlbums(authentication.getName());
        return ResponseEntity.ok(albums);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumResponseDto> getUserAlbum(
            @PathVariable Long id,
            Authentication authentication) {
        AlbumResponseDto album = albumItemService.getUserAlbum(id, authentication.getName());
        return ResponseEntity.ok(album);
    }

    @PostMapping
    public ResponseEntity<AlbumResponseDto> addAlbum(
            @Valid @RequestBody AlbumRequestDto dto,
            Authentication authentication) {
        AlbumResponseDto createdAlbum = albumItemService.addAlbum(dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAlbum);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlbumResponseDto> updateAlbum(
            @PathVariable Long id,
            @Valid @RequestBody AlbumRequestDto dto,
            Authentication authentication) {
        AlbumResponseDto updatedAlbum = albumItemService.updateAlbum(id, dto, authentication.getName());
        return ResponseEntity.ok(updatedAlbum);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlbum(
            @PathVariable Long id,
            Authentication authentication) {
        albumItemService.deleteAlbum(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}