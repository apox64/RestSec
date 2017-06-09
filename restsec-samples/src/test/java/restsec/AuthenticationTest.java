package restsec;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import restsec.config.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AuthenticationTest {

    @Test
    @DisplayName("JuiceShop: if token received, then not empty")
    void getJuiceShopTokenBodyAuthIsNotEmpty() {
        Configuration config = new Configuration();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(config.getBaseURI().replace("http://", ""), Integer.parseInt(config.getPort())), 2000);
            socket.connect(new InetSocketAddress(config.getProxyIP(), Integer.parseInt(config.getPort())), 2000);
            Assertions.assertTrue(!Authentication.getIDTokenForJuiceShop_BodyAuth().equals(""));
        } catch (IOException e) {
            LoggerFactory.getLogger(AuthenticationTest.class).info("Target or proxy is offline. Skipping test.");
        }
    }

    @Test
    @DisplayName("get OAuth2 token from config")
    void returnsOKforGivenOAuth2AccessToken() {
        String access_token = Authentication.getOAuth2TokenFromConfig();
        Pattern pattern = Pattern.compile("[a-zA-Z0-9]{16,}");
        Matcher matcher = pattern.matcher(access_token);
        Assertions.assertTrue(matcher.matches() || access_token.equals(""));
    }

    @Test
    @DisplayName("brentertainment.com : get OAuth2 token (user/pass)")
    void getTestOauth2TokenForUsernamePassword() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("brentertainment.com", 80), 2000);
            String access_token = Authentication.getOAuth2TokenFromBrentertainment();
            Pattern pattern = Pattern.compile("[a-f0-9]{40}");
            Matcher matcher = pattern.matcher(access_token);
            Assertions.assertTrue(matcher.matches());
        } catch (IOException e) {
            LoggerFactory.getLogger(AuthenticationTest.class).info("Target or proxy is offline. Skipping test.");
        }
    }

    @Test
    @DisplayName("brentertainment.com : access API with given OAuth2 token")
    void testAccessBrentertainmentAPIwithToken() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("brentertainment.com", 80), 2000);
            String access_token = Authentication.getOAuth2TokenFromBrentertainment();
            Authentication.accessBrentertainmentAPIwithToken(access_token);
        } catch (IOException e) {
            LoggerFactory.getLogger(AuthenticationTest.class).info("Target or proxy is offline. Skipping test.");
        }
    }

    @Test
    @DisplayName("if token not set in config, get from brentertainment.com")
    void getTokenForCorrectAuthType() {
        String token = Authentication.getAccessToken();
        Assertions.assertTrue(!token.isEmpty() && token != null);
    }
}