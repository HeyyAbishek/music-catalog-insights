package com.catalog.music.service;

import com.catalog.music.dto.AlbumRequestDto;
import com.catalog.music.dto.AlbumResponseDto;
import com.catalog.music.dto.ITunesSearchResponseDto;

import java.util.List;

public interface AlbumItemService {

    ITunesSearchResponseDto searchExternalAlbums(String query);

    List<AlbumResponseDto> getUserAlbums(String username);

    AlbumResponseDto getUserAlbum(Long id, String username);

    AlbumResponseDto addAlbum(AlbumRequestDto dto, String username);

    AlbumResponseDto updateAlbum(Long id, AlbumRequestDto dto, String username);

    void deleteAlbum(Long id, String username);
}