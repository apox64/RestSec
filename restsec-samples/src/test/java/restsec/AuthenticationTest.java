package restsec;

import org.junit.jupiter.api.Assertions;
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
            Assertions.assertTrue(!Authentication.getTokenForJuiceShop_BodyAuth().equals(""));
        } catch (IOException e) {
            LoggerFactory.getLogger(AuthenticationTest.class).info("Target is offline. Skipping test.");
            return;
        }
    }
}