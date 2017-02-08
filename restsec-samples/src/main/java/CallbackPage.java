
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;

public class CallbackPage {

    //opens jetty server and waits for input

    private static Server server = new Server(5555);

    public CallbackPage() {
        configureJettyLogging();
    }

    private static void configureJettyLogging() {

        NCSARequestLog requestLog = new NCSARequestLog("restsec-samples/src/main/resources/jetty-logs/jetty-yyyy_mm_dd.request.log");

        requestLog.setAppend(true);
        requestLog.setExtended(false);
        requestLog.setLogTimeZone("GMT");
        requestLog.setLogLatency(true);
        requestLog.setRetainDays(90);

        server.setRequestLog(requestLog);

    }

    public void startTestPageServer() {
        try {
            server.start();
            System.out.println("CallbackPage: Server started. Listening ... ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopTestPageServer() {
        try {
            server.stop();
            System.out.println("CallbackPage: Server stopped.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //this lets you actually execute the stored xss payload by reloading the page (with selenium webdriver)
    public void reloadResource(String url){
        System.out.print("CallbackPage: Reloading: " + url + " ... ");

        //Drivers for other OS can be downloaded here: https://sites.google.com/a/chromium.org/chromedriver/downloads
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\DMD\\Development\\RestSec\\restsec-samples\\chromedriver-win32.exe");

        //WebDriver firefoxDriver = new FirefoxDriver();
        WebDriver chromeDriver = new ChromeDriver();
        //firefoxDriver.get(url);
        chromeDriver.get(url);

        try {
            System.out.println("CallbackPage: Sleeping ...");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //firefoxDriver.navigate().refresh();
        chromeDriver.navigate().refresh();
        chromeDriver.close();
        System.out.println("Done.");
    }

}
