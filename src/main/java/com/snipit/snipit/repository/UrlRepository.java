package com.snipit.snipit.repository;

import com.snipit.snipit.model.Url;
import com.snipit.snipit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    List<Url> findByUserOrderByCreatedAtDesc(User user);

    Optional<Url> findByIdAndUser(Long id, User user);

    @Modifying
    @Query("UPDATE Url u SET u.clickCount = u.clickCount + 1 "
            + "WHERE u.id = :id AND (u.maxClicks IS NULL OR u.clickCount < u.maxClicks)")
    int incrementClickCount(@Param("id") Long id);
}