package restsec;

import org.junit.jupiter.api.*;

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
    @DisplayName("Scanning for XSS with payloads file xss-short.json")
    void scannerTest() {
        Thread scannerThread = new Thread(new restsec.Scanner("src/main/resources/attackable/attackset.json", "src/main/resources/payloads/xss-short.json", "xss"));
        scannerThread.start();

        try {
            scannerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Scanner Thread completed.");

    }

}