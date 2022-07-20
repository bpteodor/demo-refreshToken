package org.teo.demo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Function;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * dependency free implementation of the refresh_token flow
 * https://backstage.forgerock.com/docs/am/7.1/oauth2-guide/oauth2-refresh-tokens.html#oauth2-refresh-tokens
 */
@Slf4j
@RequiredArgsConstructor
public class MyTokenRefresher implements Function<String, String> {

    final URI tokenEndpoint;
    final String clientId;
    final String clientSecret;
    final String scope;

    static int index = 0;

    @SneakyThrows
    @Override
    public String apply(String token) {
        final TokenResponse resp = refreshToken(token);
        log.info("token no. {} refreshed", index++);
        return resp.getRefreshToken();
    }

    private TokenResponse refreshToken(String token) throws IOException, InterruptedException {

        final String data = String.format("client_id=%s&client_secret=%s&grant_type=refresh_token&scope=%s&refresh_token=%s",
                clientId, clientSecret, scope, URLEncoder.encode(token, UTF_8));

        final HttpRequest req = HttpRequest.newBuilder()
                .uri(tokenEndpoint)
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("x-trace-id", "teo-test" + index)
                .build();

        final HttpResponse<String> resp = HttpClient.newHttpClient().send(req, ofString(UTF_8));

        if (resp.statusCode() != 200) {
            log.error("failed to renew token. \nrefresh-token: {} \nheaders: {} \n{}",
                    token, resp.headers(), StringUtils.truncate(resp.body(), 500));
            System.exit(-1);
        }

        return new ObjectMapper()
                .readValue(resp.body(), TokenResponse.class);
    }

    @Data
    @Accessors(chain = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TokenResponse {

        @JsonProperty("access_token")
        String accessToken;
        @JsonProperty("refresh_token")
        String refreshToken;
        @JsonProperty("id_token")
        String idToken;

        String scope;

        @JsonProperty(value = "token_type", defaultValue = "Bearer")
        String tokenType;

        @JsonProperty("expires_in")
        int expiresIn;
    }
}
