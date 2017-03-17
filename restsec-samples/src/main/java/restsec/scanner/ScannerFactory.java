package restsec.scanner;

import org.slf4j.LoggerFactory;
import restsec.config.Configuration;

public class ScannerFactory {

    private Configuration config;

    public ScannerFactory(Configuration configuration) {
        config = configuration;
    }

    public Scanner createScanner() {

        String scannerType = config.getVulnerabilityScanType();

        if (scannerType == null) {
            return null;
        } else if (scannerType.toUpperCase().equals("XSS")) {
            return new XSSScanner(config.getAttackSetFileLocation(), config.getXSSPayloadsFileLocation());
        } else if (scannerType.toUpperCase().equals("SQLI")) {
            return new SQLiScanner(config.getAttackSetFileLocation(), config.getSQLiPayloadsFileLocation());
        } else if (scannerType.toUpperCase().equals("ALL")) {
            LoggerFactory.getLogger(ScannerFactory.class).warn("Returning Scanner for ALL methods not implemented.");
            System.exit(0);
        }

        return null;

    }

}
