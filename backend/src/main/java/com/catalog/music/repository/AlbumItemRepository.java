package com.catalog.music.repository;

import com.catalog.music.model.AlbumItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumItemRepository extends JpaRepository<AlbumItem, Long> {

        List<AlbumItem> findByUserId(Long userId);

        Page<AlbumItem> findByUserId(Long userId, Pageable pageable);

        Optional<AlbumItem> findByIdAndUserId(Long id, Long userId);

        Optional<AlbumItem> findByUserIdAndAppleCatalogId(Long userId, Long appleCatalogId);

        boolean existsByUserIdAndAppleCatalogId(Long userId, Long appleCatalogId);

        @Query("""
                        select coalesce(a.genre, 'Unknown'), count(a)
                        from AlbumItem a
                        where a.user.id = :userId
                        group by a.genre
                        order by count(a) desc
                        """)
        List<Object[]> countByGenreForUser(@Param("userId") Long userId);

        @Query(value = """
                        select extract(year from release_date) as release_year, count(*) as total
                        from album_items
                        where user_id = :userId
                          and release_date is not null
                        group by extract(year from release_date)
                        order by release_year
                        """, nativeQuery = true)
        List<Object[]> countByReleaseYearForUser(@Param("userId") Long userId);
}