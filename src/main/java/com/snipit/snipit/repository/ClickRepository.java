package com.snipit.snipit.repository;

import com.snipit.snipit.model.Click;
import com.snipit.snipit.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ClickRepository extends JpaRepository<Click, Long> {

    long countByUrl(Url url);

    @Query("SELECT c.clickedAt FROM Click c WHERE c.url = :url AND c.clickedAt >= :since")
    List<LocalDateTime> findClickTimesSince(@Param("url") Url url,
                                            @Param("since") LocalDateTime since);

    @Query("SELECT c.browser, COUNT(c) FROM Click c WHERE c.url = :url GROUP BY c.browser")
    List<Object[]> browserBreakdown(@Param("url") Url url);

    @Query("SELECT c.deviceType, COUNT(c) FROM Click c WHERE c.url = :url GROUP BY c.deviceType")
    List<Object[]> deviceBreakdown(@Param("url") Url url);

    @Query("SELECT c.referrer, COUNT(c) FROM Click c WHERE c.url = :url GROUP BY c.referrer")
    List<Object[]> referrerBreakdown(@Param("url") Url url);
}