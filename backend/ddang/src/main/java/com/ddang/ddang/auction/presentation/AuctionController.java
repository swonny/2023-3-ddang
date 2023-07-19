package com.ddang.ddang.auction.presentation;

import com.ddang.ddang.auction.application.AuctionService;
import com.ddang.ddang.auction.application.dto.CreateAuctionDto;
import com.ddang.ddang.auction.application.dto.ReadAuctionDto;
import com.ddang.ddang.auction.presentation.dto.CreateAuctionRequest;
import com.ddang.ddang.auction.presentation.dto.CreateAuctionResponse;
import com.ddang.ddang.auction.presentation.dto.ReadAuctionDetailResponse;
import com.ddang.ddang.auction.presentation.dto.ReadAuctionResponse;
import com.ddang.ddang.auction.presentation.dto.ReadAuctionsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    @PostMapping
    public ResponseEntity<CreateAuctionResponse> create(@RequestBody @Valid final CreateAuctionRequest request) {
        final Long auctionId = auctionService.create(CreateAuctionDto.from(request));
        final CreateAuctionResponse response = new CreateAuctionResponse(auctionId);

        return ResponseEntity.created(URI.create("/auctions/" + auctionId))
                             .body(response);
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<ReadAuctionDetailResponse> read(@PathVariable final Long auctionId) {
        final ReadAuctionDto readAuctionDto = auctionService.readByAuctionId(auctionId);
        final ReadAuctionDetailResponse response = ReadAuctionDetailResponse.from(readAuctionDto);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ReadAuctionsResponse> readAllByLastAuctionId(
            @RequestParam(required = false) final Long lastAuctionId,
            @RequestParam(required = false, defaultValue = "9999") final int size
    ) {
        final List<ReadAuctionDto> readAuctionDtos = auctionService.readAllByLastAuctionId(lastAuctionId, size);
        final List<ReadAuctionResponse> readAuctionResponses = readAuctionDtos.stream()
                                                                              .map(ReadAuctionResponse::from)
                                                                              .toList();

        final ReadAuctionsResponse response = new ReadAuctionsResponse(
                readAuctionResponses,
                findLastAuctionId(readAuctionResponses)
        );

        return ResponseEntity.ok(response);
    }

    private Long findLastAuctionId(final List<ReadAuctionResponse> readAuctionResponses) {
        if (readAuctionResponses.isEmpty()) {
            return null;
        }

        return readAuctionResponses.get(readAuctionResponses.size() - 1)
                                   .id();
    }

    @DeleteMapping("/{auctionId}")
    public ResponseEntity<Void> delete(@PathVariable final Long auctionId) {
        auctionService.deleteByAuctionId(auctionId);

        return ResponseEntity.noContent()
                             .build();
    }
}
