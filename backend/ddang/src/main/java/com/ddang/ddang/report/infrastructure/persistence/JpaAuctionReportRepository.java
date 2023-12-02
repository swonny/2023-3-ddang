package com.ddang.ddang.report.infrastructure.persistence;

import com.ddang.ddang.report.domain.AuctionReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JpaAuctionReportRepository extends JpaRepository<AuctionReport, Long> {

    boolean existsByAuctionIdAndReporterId(final Long auctionId, final Long reporterId);

    @Query("""
        SELECT ar
        FROM AuctionReport ar
        JOIN FETCH ar.reporter r
        LEFT JOIN FETCH r.profileImage
        JOIN FETCH ar.auction a
        JOIN FETCH a.seller s
        LEFT JOIN FETCH s.profileImage
        ORDER BY ar.id ASC
    """)
    List<AuctionReport> findAll();
}
