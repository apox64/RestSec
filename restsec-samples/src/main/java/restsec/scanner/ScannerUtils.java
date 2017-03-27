package restsec.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restsec.config.ScannerType;

class ScannerUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScannerUtils.class);

    static int numberOfSentPackets = 0;
    static int acceptedPackets = 0;
    static int rejectedPackets = 0;

    static void printPackageStatistics(ScannerType scannerType) {
        LOGGER.info("----------------------------");
        LOGGER.info("Stats for "+scannerType+" Scan:");
        LOGGER.info("----------------------------");
        LOGGER.info(numberOfSentPackets + " packets sent.");
        if (numberOfSentPackets != 0) {
            LOGGER.info(acceptedPackets + " packets accepted. (" + (acceptedPackets * 100 / numberOfSentPackets) + "%)");
        }
        LOGGER.info("----------------------------");
    }

}
