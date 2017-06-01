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

class AuthenticationTest {

    @Test
    @DisplayName("token is not empty")
    void getJuiceShopTokenBodyAuthIsNotEmpty() {
        Configuration config = new Configuration();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(config.getBaseURI().replace("http://", ""), Integer.parseInt(config.getPort())), 2000);
            socket.connect(new InetSocketAddress(config.getProxyIP(), Integer.parseInt(config.getPort())), 2000);
            Assertions.assertTrue(!Authentication.getIDTokenForJuiceShop_BodyAuth().equals(""));
        } catch (IOException e) {
            LoggerFactory.getLogger(AuthenticationTest.class).info("Target or proxy is offline. Skipping test.");
            return;
        }
    }

    @Disabled
    @Test
    @DisplayName("get access for given oauth2token")
    void returnsOKforGivenOAuth2AccessToken() {
        Configuration config = new Configuration();
        Authentication.getOAuth2TokenFromConfig();
    }
}