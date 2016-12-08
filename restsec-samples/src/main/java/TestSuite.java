import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(Suite.class)

@Suite.SuiteClasses({
        JuiceShopBasic.class,
        JuiceShopXSS.class
})

public class TestSuite {

    private static String serverIP = "";
    private static int serverPort = 0;
    private static String proxyIp = "";
    private static int proxyPort = 0;

    @BeforeClass
    public static void init() throws IOException {

        System.out.println("Loading properties from file ...");

        loadProperties();

        if (proxyIp != "" && proxyPort != 0) {
            if (isOnline(proxyIp,proxyPort)){
                System.out.println("Proxy online.");
            } else {
                Assert.fail("Proxy unreachable.");
            }
        }

        if (isOnline(serverIP,serverPort)) {
            System.out.println("Target online.");
        } else {
            Assert.fail("Target unreachable.");
        }

        System.out.println("\nInit sequence complete. Starting test suite ...\n");

    }

    public static void loadProperties() {
        Properties properties = new Properties();

        try(InputStream stream = JuiceShopBasic.class.getClassLoader().getResourceAsStream("config.properties")){
            properties.load(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Pattern r = Pattern.compile("(?:[0-9]{1,3}\\.){3}[0-9]{1,3}");
        Matcher m = r.matcher(properties.getProperty("base-uri"));
        m.find();
        serverIP = m.group(0);
        serverPort = Integer.parseInt(properties.getProperty("port"));
        System.out.println("Target: "+serverIP+":"+serverPort);

        if (!properties.getProperty("proxy_ip").equals("")) {
            proxyIp = properties.getProperty("proxy_ip");
            proxyPort = Integer.parseInt(properties.getProperty("proxy_port"));
            System.out.println("Proxy: "+proxyIp+":"+proxyPort);
        } else {
            System.out.println("Proxy: -");
        }
    }

    public static boolean isOnline(String ip, int port){
        try (Socket s = new Socket(ip, port)) {
            if (s.isConnected()) {
                s.close();
            }
            return true;
        } catch (IOException ex) {
            /* ignore */
        }
        return false;
    }

}
