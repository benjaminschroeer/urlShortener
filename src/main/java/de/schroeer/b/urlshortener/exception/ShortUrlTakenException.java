package de.schroeer.b.urlshortener.exception;

public class ShortUrlTakenException extends Exception {
    private final String shortUrl;

    public ShortUrlTakenException(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }
}
