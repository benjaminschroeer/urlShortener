package de.schroeer.b.urlshortener.model.dto;

public record ShortenRequest (String fullUrl, String requestedShortUrl){}
