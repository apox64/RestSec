package restsec.scanner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import restsec.config.Configuration;

import static org.junit.jupiter.api.Assertions.*;

class HTTPSecurityHeadersScannerTest {

    @Test
    @DisplayName("Scanning for Common HTTP Seurity Headers")
    void scanForSecurityHeaders() {
        Configuration config = new Configuration();
        HTTPSecurityHeadersScanner httpSecurityHeadersScanner = new HTTPSecurityHeadersScanner();
        //TODO: Unit Testing doesn't make sense since external resource is not there
        //httpSecurityHeadersScanner.scanForSecurityHeaders(config.getBasePath());
    }

}