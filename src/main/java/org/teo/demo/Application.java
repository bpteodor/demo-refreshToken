package org.teo.demo;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Command(name = "drt", mixinStandardHelpOptions = true, version = "1.0", description = "Demo - refresh-token usage")
public class Application implements Callable<Integer> {

    @Parameters(index = "0", description = "The refresh-token to start testing")
    private String refreshToken;

    @Option(names = {"-wt", "--wait-time"}, description = "how much time to wait until refreshing the token")
    private Duration waitTime = Duration.ofMinutes(1);

    @Option(names = {"-cid", "--client-id"}, description = "oauth2 client_id")
    private String clientId;

    @Option(names = {"-cs", "--client-secret"}, description = "oauth2 client_secret")
    private String clientSecret;

    @Override
    public Integer call() throws Exception {
        RefreshTokenWrapper token = new RefreshTokenWrapper(refreshToken);
        if (!token.isValid()) return -1;

        while (System.in.available() == 0) {

            token = token.renew(clientId, clientSecret);

            TimeUnit.MILLISECONDS.sleep(waitTime.toMillis());
        }

        return 0;
    }

    // this example implements Callable, so parsing, error handling and handling user
    // requests for usage help or version help can be done with one line of code.
    public static void main(String... args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }
}