package de.schroeer.b.urlshortener.service;

import de.schroeer.b.urlshortener.exception.ShortUrlTakenException;
import de.schroeer.b.urlshortener.model.db.UrlEntity;
import de.schroeer.b.urlshortener.model.dto.ShortUrl;
import de.schroeer.b.urlshortener.model.dto.ShortenRequest;
import de.schroeer.b.urlshortener.repository.UrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UrlService {

    private final Logger logger = LoggerFactory.getLogger(UrlService.class);

    private final UrlRepository urlRepository;
    private final Pattern urlRegex = Pattern.compile("^(http(s)?:\\/\\/)(w{3}\\.)?([a-zA-Z0-9öäüÄÖÜ_\\-\\.\\#\\/\\~\\&]+(\\.[a-zA-Z]{2,6})|localhost)(:[1-9][0-9]{0,4})?(\\/[a-zA-Z0-9_\\-\\.\\#\\/\\~\\&]*)?(\\?[a-zA-Z0-9_=&\\-\\%]*)?$");
    private final Pattern shortenRegex = Pattern.compile("^[a-zA-Z]+$");

    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public ShortUrl storeFullUrl(ShortenRequest request) throws URISyntaxException, ShortUrlTakenException {
        logger.debug("getting shortUrl");
        String shortUrl = getShortUrl(request.requestedShortUrl());
        logger.debug("shortUrl to be used: '{}'", shortUrl);
        UrlEntity urlEntity = new UrlEntity(request.fullUrl(), shortUrl);
        logger.debug("saving entry");
        UrlEntity saved = this.urlRepository.save(urlEntity);
        return new ShortUrl(saved.getShortUrl());
    }

    public void validateUrl(String urlAsString) throws URISyntaxException {
        Matcher matcher = urlRegex.matcher(urlAsString);
        if(!matcher.find()){
            throw new URISyntaxException(urlAsString, "Syntax of given URI is invalid");
        }
    }

    public void validateShortenText(String string) throws URISyntaxException {
        Matcher matcher = shortenRegex.matcher(string);
        if(!matcher.find()){
            throw new URISyntaxException(string, "Syntax of given shorted URI is invalid");
        }
    }

    private String getShortUrl(String requestedShortUrl) throws ShortUrlTakenException {
        if (requestedShortUrl != null && !requestedShortUrl.isEmpty()) {
            logger.debug("Requested short url: '{}'", requestedShortUrl);
            List<UrlEntity> entities = this.urlRepository.findUrlByShortUrl(requestedShortUrl);
            if (entities.isEmpty()) {
                logger.debug("No entries with given shortUrl found");
                return requestedShortUrl;
            } else {
                throw new ShortUrlTakenException(requestedShortUrl);
            }
        }
        logger.debug("Generating random ShortUrl");
        return generateRandomShortUrl();
    }

    public List<UrlEntity> getByFullUrl(String fullUrl){
        return this.urlRepository.findUrlByFullUrl(fullUrl);
    }
    public List<UrlEntity> getByShortUrl(String shortUrl){
        return this.urlRepository.findUrlByShortUrl(shortUrl);
    }

    private String generateRandomShortUrl() {
        int urlLength = 8;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(urlLength);
        for (int i = 0; i < urlLength; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }
}
