package restsec;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restsec.config.Configuration;

import java.io.*;

class EvaluatorTest {

    static final Logger LOGGER = LoggerFactory.getLogger(EvaluatorTest.class);

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {

    }

    @Test
    @DisplayName("Writing Vulnerability (vulnType, endpoint, payload, comment) to file.")
    void writeVulnerabilityToFileTest() {
        int counter = 0;
        Evaluator.writeVulnerabilityToResultsFile("vulnType_test","endpoint_test", "payload_test", "comment_test");
        //BufferedReader bufferedReader = null;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("src/main/resources/results/results.json"))) {



//            bufferedReader = new BufferedReader(new FileReader("src/main/resources/results/results.json"));
            while (bufferedReader.readLine() != null) {
                counter++;
            }
//            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
//        } finally {
//            try {
//                if (bufferedReader != null) {
//                    bufferedReader.close();
//                }
//            } catch (IOException e) {
                e.printStackTrace();
//            }
        }
        Assertions.assertTrue(counter == 8);
    }

    @Test
    @DisplayName("Old results.json deleted properly.")
    void deleteOldResultsFileTest() {
        File fileToDelete = new File("src/main/resources/results/results.json");
        if (!fileToDelete.exists()) {
            LOGGER.info("File doesn't exist.");
            try {
                LOGGER.info("File created: " + fileToDelete.createNewFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LOGGER.info("File exists.");
        }
        Evaluator evaluator = new Evaluator(new Configuration());
        evaluator.deleteOldResultsFile();
        Assertions.assertTrue(!fileToDelete.exists());
    }

}