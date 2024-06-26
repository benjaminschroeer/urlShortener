package de.schroeer.b.urlshortener.controller;

import de.schroeer.b.urlshortener.exception.ShortUrlTakenException;
import de.schroeer.b.urlshortener.model.db.UrlEntity;
import de.schroeer.b.urlshortener.model.dto.ShortUrl;
import de.schroeer.b.urlshortener.model.dto.ShortenRequest;
import de.schroeer.b.urlshortener.service.UrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
public class UrlController {

    private final Logger logger = LoggerFactory.getLogger(UrlController.class);

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<ShortUrl> saveUrl(@RequestBody ShortenRequest request) throws ShortUrlTakenException, URISyntaxException {
        logger.debug("Validating request");
        this.urlService.validateUrl(request.fullUrl());
        this.urlService.validateShortenText(request.requestedShortUrl());
        logger.debug("Getting all entries by full url '{}'", request.fullUrl());
        List<UrlEntity> shortUrlOptional = this.urlService.getByFullUrl(request.fullUrl());
        if (shortUrlOptional.size() == 1) {
            logger.info("FullUrl '{}' already has a shortened Url: '{}'. Returning", shortUrlOptional.get(0).getFullUrl(), shortUrlOptional.get(0).getShortUrl());
            return ResponseEntity.ok(new ShortUrl(shortUrlOptional.get(0).getShortUrl()));
        }
        logger.debug("FullUrl not saved yet, saving.");
        ShortUrl shortUrl = this.urlService.storeFullUrl(request);
        return ResponseEntity.created(new URI(shortUrl.shortUrl())).body(shortUrl);
    }

    @GetMapping("/{shortenString}")
    public ResponseEntity<Object> redirectToFullUrl(@PathVariable String shortenString) {
        logger.debug("Searching for short url '{}'", shortenString);
        List<UrlEntity> results = this.urlService.getByShortUrl(shortenString);
        if (results.size() == 1) {
            logger.debug("Found full url '{}'", results.get(0).getFullUrl());
            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).header(HttpHeaders.LOCATION, results.get(0).getFullUrl()).build();
        } else {
            logger.info("No entry found for short url '{}'", shortenString);
            return ResponseEntity.notFound().build();
        }
    }

    @ExceptionHandler({URISyntaxException.class})
    public ErrorResponse handleInvalidURIException(URISyntaxException e) {
        return ErrorResponse.builder(e, HttpStatus.BAD_REQUEST, e.getReason()).build();
    }

    @ExceptionHandler({ShortUrlTakenException.class})
    public ErrorResponse handleShortUrlTakenException(ShortUrlTakenException e) {
        return ErrorResponse.builder(e, HttpStatus.BAD_REQUEST, e.getMessage()).build();
    }
}
