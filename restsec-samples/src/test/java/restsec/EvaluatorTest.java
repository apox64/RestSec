package restsec;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restsec.config.Configuration;

import java.io.*;
import java.nio.file.Files;

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
    void writeVulnerabilityToFileTest() throws IOException {
        Evaluator.writeVulnerabilityToResultsFile("vulnType_test", "endpoint_test", "payload_test", "comment_test");
        File file = new File("src/main/resources/results/results.json");
        //TODO: Think of a better test
        //Assertions.assertTrue(Files.readAllLines(file.toPath()).size() == 8);
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