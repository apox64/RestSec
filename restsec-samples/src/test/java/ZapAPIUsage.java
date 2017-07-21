import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.restassured.RestAssured;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import io.restassured.response.Response;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ClientApi;

import java.io.PrintWriter;

import static io.restassured.RestAssured.get;

public class ZapAPIUsage {

    @Test
    @DisplayName("ZAP Proxy online?")
    public void zapOnline() {
        RestAssured.proxy("127.0.0.1", 8080);
        Response response =
                        get("http://localhost:8081/");
        Assertions.assertThat(response.getStatusCode() == 200);
    }

    @Ignore
    @Test
    @DisplayName("Spidering the Juice Shop")
    public void spiderJuiceShop(){
        RestAssured.proxy("127.0.0.1", 8080);
        // Starting the spider
        Response res =
                get("http://localhost:8081/JSON/spider/action/scan/?zapapiformat=JSON&formMethod=GET&url=http://192.168.99.100:3000&maxChildren=&recurse=&contextName=&subtreeOnly=");

        JsonObject jo = new JsonParser().parse(res.getBody().asString()).getAsJsonObject();

        int id = jo.get("scan").getAsInt();

        // Checking the status (percent completed)
        int percentage = 0;

        while (percentage != 100) {
            Response response =
                    get("http://localhost:8081/JSON/spider/view/status/?zapapiformat=JSON&formMethod=GET&scanId="+id);
            JsonObject jobject = new JsonParser().parse(response.getBody().asString()).getAsJsonObject();
            percentage = jobject.get("status").getAsInt();
            LoggerFactory.getLogger(ZapAPIUsage.class).info(percentage + "% done.");

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @Test
    @DisplayName("Simple Example")
    public void simpleExample() {

        final String ZAP_ADDRESS = "localhost";
        final int ZAP_PORT = 8081;
        final String ZAP_API_KEY = null; // Change this if you have set the apikey in ZAP via Options / API

        final String TARGET = "http://192.168.99.100:3000/rest";

        ClientApi api = new ClientApi(ZAP_ADDRESS, ZAP_PORT, ZAP_API_KEY);

        try {
            // Start spidering the target
            System.out.println("Spider : " + TARGET);
            // It's not necessary to pass the ZAP API key again, already set when creating the ClientApi.
            ApiResponse resp = api.spider.scan(TARGET, null, null, null, null);
            String scanid;
            int progress;

            // The scan now returns a scan id to support concurrent scanning
            scanid = ((ApiResponseElement) resp).getValue();

            // Poll the status until it completes
            while (true) {
                Thread.sleep(1000);
                progress = Integer.parseInt(((ApiResponseElement) api.spider.status(scanid)).getValue());
                System.out.println("Spider progress : " + progress + "%");
                if (progress >= 100) {
                    break;
                }
            }
            System.out.println("Spider complete");

            // Give the passive scanner a chance to complete
            Thread.sleep(2000);

            System.out.println("Active scan : " + TARGET);
            resp = api.ascan.scan(TARGET, "True", "False", null, null, null);

            // The scan now returns a scan id to support concurrent scanning
            scanid = ((ApiResponseElement) resp).getValue();

            // Poll the status until it completes
            while (true) {
                Thread.sleep(5000);
                progress = Integer.parseInt(((ApiResponseElement) api.ascan.status(scanid)).getValue());
                System.out.println("Active Scan progress : " + progress + "%");
                if (progress >= 100) {
                    break;
                }
            }
            System.out.println("Active Scan complete");

            //System.out.println("Alerts:");
            //System.out.println(new String(api.core.htmlreport()));

            PrintWriter printWriter = new PrintWriter("zap-report.html", "UTF-8");
            printWriter.write(new String(api.core.htmlreport()));
            printWriter.close();

            System.out.println("ZAP HTML Report written.");

        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
            e.printStackTrace();
        }
    }

}
