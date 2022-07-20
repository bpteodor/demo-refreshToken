package org.teo.demo;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Map;

import static java.util.Objects.requireNonNullElseGet;
import static org.apache.commons.lang3.StringUtils.appendIfMissing;

@Slf4j
public class RefreshTokenWrapper {

    final DecodedJWT decodedJWT;
    static Map<String, Object> discoveryData;

    public RefreshTokenWrapper(String token) {
        decodedJWT = JWT.decode(token);
    }

    public boolean isValid() {
        if (decodedJWT.getExpiresAtAsInstant().isBefore(Instant.now())) {
            log.info("invalid token - expired");
            return false;
        }
        // introspection?
        return true;
    }

    public RefreshTokenWrapper renew(String clientIdParam, String clientSecret) throws IOException, InterruptedException {

        final URI tokenEndpoint = getTokenEndpoint();
        final String clientId = requireNonNullElseGet(clientIdParam, () -> decodedJWT.getClaim("aud").asString());
        final String scope = String.join(" ", decodedJWT.getClaims().get("scope").asList(String.class));

        final String freshToken = new MyTokenRefresher(tokenEndpoint, clientId, clientSecret, scope)
                .apply(decodedJWT.getToken());

        return new RefreshTokenWrapper(freshToken);
    }

    private Map<String, Object> resolveDiscovery() throws IOException, InterruptedException {
        final URI discoveryURI = URI.create(appendIfMissing(decodedJWT.getIssuer(), "/") + ".well-known/openid-configuration");

        final HttpRequest req = HttpRequest.newBuilder().uri(discoveryURI).GET().build();

        final HttpClient client = HttpClient.newHttpClient();

        log.info("discovery: {}", discoveryURI);
        final HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        log.info("discovery resolved");

        return new ObjectMapper().readValue(resp.body(), Map.class);
    }

    public Map<String, Object> getDiscoveryData() throws IOException, InterruptedException {
        if (discoveryData == null) discoveryData = resolveDiscovery();
        return discoveryData;
    }

    public URI getTokenEndpoint() throws IOException, InterruptedException {
        return URI.create((String) getDiscoveryData().get("token_endpoint"));
    }
}
