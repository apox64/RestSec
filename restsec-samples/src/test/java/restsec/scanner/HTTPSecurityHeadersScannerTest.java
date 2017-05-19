package restsec.scanner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import restsec.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.util.List;

class HTTPSecurityHeadersScannerTest {

    @Test
    @DisplayName("Scanning for Common HTTP Seurity Headers")
    void scanForSecurityHeaders() throws IOException {
        Configuration config = new Configuration();
        HTTPSecurityHeadersScanner httpSecurityHeadersScanner = new HTTPSecurityHeadersScanner();

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(config.getBaseURI().replace("http://", ""), Integer.parseInt(config.getPort())), 2000);
            httpSecurityHeadersScanner.scanForSecurityHeaders(config.getBasePath());
        } catch (IOException e) {
            LoggerFactory.getLogger(HTTPSecurityHeadersScannerTest.class).info("Target is offline. Skipping test.");
        }

        File file = new File("src/main/resources/results/results.json");
        List<String> list = Files.readAllLines(file.toPath());
        for (String string : list) {
            //When all Header scans are written to file, it's at least 62 lines long
            if (string.contains("X-Permitted-Cross-Domain-Policies") && list.size() >= 62) {
                Assertions.assertTrue(true);
                return;
            }
        }
        Assertions.assertFalse(true);

    }
}
