package restsec.scanner;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLiScanner implements Scanner {

    private final static Logger LOGGER = LoggerFactory.getLogger(SQLiScanner.class);
    private String attackSetFile = "";
    private JSONObject attackSet = new JSONObject();
    private String payloadsFile = "";
    private JSONObject payloads = new JSONObject();

    public SQLiScanner(String attackSetFile, String payloadsFile) {
        this.attackSetFile = attackSetFile;
        this.payloadsFile = payloadsFile;
    }

    @Override
    public void scan() {
        LOGGER.info("Trying SQLi ...");
        int numberOfSentPackets = 0;
        int acceptedPackets = 0;
        int rejectedPackets = 0;

        System.out.println("----------------------------\nrestsec.Scanner: Stats for SQLi Scan:");
        //noinspection ConstantConditions
        if (numberOfSentPackets == 0) {
            System.out.println("No packets sent.");
        } else {
            System.out.println("----------------------------\n"+acceptedPackets+" packets accepted. "+
                    rejectedPackets+" packets rejected. ("+(acceptedPackets/numberOfSentPackets)+"% accepted).");
        }
        System.out.println("----------------------------");
    }
}
