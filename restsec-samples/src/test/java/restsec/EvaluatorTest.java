package restsec;
import org.junit.jupiter.api.*;

import java.io.*;

class EvaluatorTest {
    @BeforeEach
    void setUp() {

    }

    @Test
    @DisplayName("Writing Vulnerability (vulnType, endpoint, payload, comment) to file.")
    void writeVulnerabilityToFileTest() {
        int counter = 0;
        Evaluator.writeVulnerabilityToFile("vulnType_test","endpoint_test", "payload_test", "comment_test");
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("src/main/resources/results/results.json"));
            while (bufferedReader.readLine() != null) {
                counter++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(counter == 8);
    }

    @AfterEach
    void tearDown() {

    }

}