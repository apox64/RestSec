package restsec.scanner;

public class ScannerUtils {

    public static int numberOfSentPackets = 0;
    public static int acceptedPackets = 0;
    public static int rejectedPackets = 0;

    public static void printPackageStatistics() {
        System.out.println("----------------------------\nrestsec.Scanner: Stats for XSS Scan:\n----------------------------\n" + numberOfSentPackets + " packets sent. ");
        if (numberOfSentPackets != 0) {
            System.out.println(acceptedPackets + " packets accepted. (" + (acceptedPackets * 100 / numberOfSentPackets) + "%).\n----------------------------");
        }
        System.out.println("----------------------------");
    }

}
