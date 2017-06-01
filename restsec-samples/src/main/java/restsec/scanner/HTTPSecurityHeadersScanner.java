package restsec.scanner;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restsec.Authentication;
import restsec.Evaluator;
import restsec.config.Configuration;

import static io.restassured.RestAssured.given;

public class HTTPSecurityHeadersScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPSecurityHeadersScanner.class);
    private static String serverIP = "";
    private static int serverPort = 0;
    private static String proxyIp = "";
    private static int proxyPort = 0;

    private static String email = "email";
    private static String password = "password";
    private static String resource = "";

    private String juiceToken = Authentication.getIDTokenForJuiceShop_BodyAuth();

    public void scanForSecurityHeaders(String endpoint) {
        resource = endpoint;
        Configuration config = new Configuration();
        RestAssured.baseURI = config.getBaseURI();
        RestAssured.basePath = config.getBasePath();
        RestAssured.port = Integer.parseInt(config.getPort());
        //TODO: Proxy not defined for testing
        invalidAcceptHeaderReflected();
        mismatchingContentTypeHeader();
        securityHeader_XContentTypeOptions();
        securityHeader_XFrameOptions();
        securityHeader_XXSSProtection();
        noContentTypeHeader();
        strictTransportSecurityHeader();
        contentSecurityPolicyExists();
        permittedCrossDomainPoliciesExists();
    }

    private void invalidAcceptHeaderReflected() {

        Response response =
                given().
                        header(new Header("Authorization", "Bearer "+juiceToken)).
                        cookie("token", juiceToken).
                        request().
                        header("Accept", "myHeader").
                        body("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}").
                        contentType(ContentType.JSON).
                        when().
                        post(resource);

        if (response.getHeader("Content-Type").equals("myHeader")) {
            Evaluator.writeVulnerabilityToResultsFile("Insecure HTTP Header", resource, "Content-Type: myHeader", "Reflected invalid header type.");
            LOGGER.info("Fake Request Accept-Header was reflected in Response Content-Type. Missing server-side validation?");
        } else {
            Evaluator.writeVulnerabilityToResultsFile("Secure HTTP Header", resource, "Content-Type: myHeader", "DID NOT reflect invalid header type.");
        }
    }

    private void mismatchingContentTypeHeader() {

        Response response =
                given().
                        header(new Header("Authorization", "Bearer "+juiceToken)).
                        request().
                        body("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}").
                        contentType(ContentType.URLENC).
                        when().
                        post(resource);

        int statusCode = response.getStatusCode();

        if (statusCode == 200) {
            Evaluator.writeVulnerabilityToResultsFile("Insecure HTTP Header", resource, "Content-Type: URLENC", "Server accepted Content-Type: URLENC when sending different Content-Type.");
            LOGGER.info("Server accepted Content-Type: URLENC when sending different Content-Type.");
        } else {
            Evaluator.writeVulnerabilityToResultsFile("Secure HTTP Header", resource, "Content-Type: URLENC", "Server DID NOT accept Content-Type: URLENC when sending different Content-Type.");
        }

    }

    private void securityHeader_XContentTypeOptions() {
        Response response =
                given().
                        request().
                        body("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}").
                        contentType(ContentType.JSON).
                        when().
                        post(resource);

        if (!response.getHeader("X-Content-Type-Options").equals("nosniff")) {
            Evaluator.writeVulnerabilityToResultsFile("Insecure HTTP Header", resource, "X-Content-Type-Options: nosniff", "Security-Header \"X-Content-Type-Options: nosniff\" missing.");
            LOGGER.info("Security-Header \"X-Content-Type-Options: nosniff\" not found.");
        } else {
            Evaluator.writeVulnerabilityToResultsFile("Secure HTTP Header", resource, "X-Content-Type-Options: nosniff", "Security-Header \"X-Content-Type-Options: nosniff\" found!");
        }
    }

    private void securityHeader_XFrameOptions() {
        Response response =
                given().
                        request().
                        body("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}").
                        contentType(ContentType.JSON).
                        when().
                        post(resource);

        if (!response.getHeaders().hasHeaderWithName("X-Frame-Options")) {
            Evaluator.writeVulnerabilityToResultsFile("Insecure HTTP Header", resource, "X-Frame-Options", "Security-Header \"X-Frame-Options\" missing.");
            LOGGER.info("Security-Header \"X-Frame-Options\" not found.");
        } else {
            Evaluator.writeVulnerabilityToResultsFile("Secure HTTP Header", resource, "X-Frame-Options", "Security-Header \"X-Frame-Options\" found!");
        }

        if (!response.getHeader("X-Frame-Options").equals("SAMEORIGIN") || !response.getHeader("X-Frame-Options").equals("deny")) {
            Evaluator.writeVulnerabilityToResultsFile("Insecure HTTP Header", resource, "X-Frame-Options: \"SAMEORIGIN\" / \"deny\"", "Security-Header \"X-Frame-Options: SAMEORIGIN\" OR \"deny\" missing.");
            LOGGER.info("Security-Header \"X-Frame-Options: SAMEORIGIN\" not found.");
        } else {
            Evaluator.writeVulnerabilityToResultsFile("Secure HTTP Header", resource, "X-Frame-Options: \"SAMEORIGIN\" / \"deny\"", "Security-Header \"X-Frame-Options: SAMEORIGIN\" OR \"deny\" found!");
        }
    }

    private void securityHeader_XXSSProtection() {
        Response response =
                given().
                        request().
                        body("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}").
                        contentType(ContentType.JSON).
                        when().
                        post(resource);

        if (!response.getHeaders().hasHeaderWithName("X-XSS-Protection")) {
            Evaluator.writeVulnerabilityToResultsFile("Insecure HTTP Header", resource, "X-XSS-Protection", "Security-Header \"X-XSS-Protection\" missing.");
            LOGGER.info("Security-Header \"X-XSS-Protection\" missing.");
        } else {
            Evaluator.writeVulnerabilityToResultsFile("Secure HTTP Header", resource, "X-XSS-Protection", "Security-Header \"X-XSS-Protection\" found!");
        }
    }

    private void noContentTypeHeader() {

        Response response =
                given().
                        request().
                        body("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}").
                        when().
                        post(resource);

        int statusCode = response.getStatusCode();

        if (statusCode == 200) {
            Evaluator.writeVulnerabilityToResultsFile("Insecure HTTP Header", resource, "No Content-Type Header", "Target accepted not using a content-type header at all (200 OK).");
            LOGGER.info(">>> Server accepted not using a content-type header at all: 200 OK");
        } else {
            Evaluator.writeVulnerabilityToResultsFile("Secure HTTP Header", resource, "No Content-Type Header", "Some other response for not using a content-type header at all (at least not 200).");
        }
    }

    private void strictTransportSecurityHeader() {
        Response response =
                given().
                        request().
                        body("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}").
                        contentType(ContentType.JSON).
                        when().
                        post(resource);

        if (!response.getHeaders().hasHeaderWithName("Strict-Transport-Security")) {
            Evaluator.writeVulnerabilityToResultsFile("Insecure HTTP Header", resource, "Strict-Transport-Security", "Security-Header \"Strict-Transport-Security\" missing.");
            LOGGER.info("Security-Header \"Strict-Transport-Security\" missing.");
        } else {
            Evaluator.writeVulnerabilityToResultsFile("Secure HTTP Header", resource, "Strict-Transport-Security", "Security-Header \"Strict-Transport-Security\" found!");
        }
    }

    private void contentSecurityPolicyExists() {
        Response response =
                given().
                        request().
                        body("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}").
                        contentType(ContentType.JSON).
                        when().
                        post(resource);

        if (!response.getHeaders().hasHeaderWithName("Content-Security-Policy")) {
            Evaluator.writeVulnerabilityToResultsFile("Insecure HTTP Header", resource, "Content-Security-Policy", "Security-Header \"Content-Security-Policy\" missing.");
            LOGGER.info("Security-Header \"Content-Security-Policy\" missing.");
        } else {
            Evaluator.writeVulnerabilityToResultsFile("Secure HTTP Header", resource, "Content-Security-Policy", "Security-Header \"Content-Security-Policy\" found.");
        }
    }

    private void permittedCrossDomainPoliciesExists() {
        Response response =
                given().
                        request().
                        body("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}").
                        contentType(ContentType.JSON).
                        when().
                        post(resource);

        if (!response.getHeaders().hasHeaderWithName("X-Permitted-Cross-Domain-Policies")) {
            Evaluator.writeVulnerabilityToResultsFile("Insecure HTTP Header", resource, "X-Permitted-Cross-Domain-Policies", "Security-Header \"X-Permitted-Cross-Domain-Policies\" missing.");
            LOGGER.info("Security-Header \"X-Permitted-Cross-Domain-Policies\" missing.");
        } else {
            Evaluator.writeVulnerabilityToResultsFile("Secure HTTP Header", resource, "X-Permitted-Cross-Domain-Policies", "Security-Header \"X-Permitted-Cross-Domain-Policies\" found.");
        }
    }

}
