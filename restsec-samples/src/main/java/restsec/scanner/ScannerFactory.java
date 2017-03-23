package restsec.scanner;

import restsec.config.Configuration;

public class ScannerFactory {

    private Configuration config;

    public ScannerFactory(Configuration configuration) {
        config = configuration;
    }

    public Scanner createScanner() {

        switch (config.getScannerType()) {
            case XSS:
                return new XSSScanner(config.getAttackSetFileLocation(), config.getXSSPayloadsFileLocation());
            case SQLI:
                return new SQLiScanner(config.getAttackSetFileLocation(), config.getSQLiPayloadsFileLocation());
            default:
                throw new IllegalStateException("Unknown Scanner Type");
        }

    }

}
