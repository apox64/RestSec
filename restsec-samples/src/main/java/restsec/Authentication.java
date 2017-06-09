package restsec;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restsec.config.Configuration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.*;

public class Authentication {

    private static final Logger LOGGER = LoggerFactory.getLogger(Authentication.class);

    public static String getAccessToken() {
        Configuration configuration = new Configuration();
        String tokenFromConfig = configuration.getOAuth2AccessToken();
        if (tokenFromConfig != null && !tokenFromConfig.isEmpty()) {
            LOGGER.info("Token from config: " + tokenFromConfig);
        } else {
            LOGGER.info("No access token set in config. Getting access token ...");
            switch (configuration.getAuthType()) {
                case NONE: return "";
                case BASIC: return "";
                case BODY: getIDTokenForJuiceShop_BodyAuth();
                case OAUTH2:
                    if (!getOAuth2TokenFromConfig().isEmpty()) {
                        return getOAuth2TokenFromConfig();
                    } else {
                        return getOAuth2TokenFromBrentertainment();
                    }
                case OPENIDCONNECT: return "";
            }
        }
        return "";
    }

    public static String getIDTokenForJuiceShop_BodyAuth() {
        Configuration config = new Configuration();
        String token = "";
        RestAssured.baseURI = config.getBaseURI();
        RestAssured.basePath = config.getBasePath();
        RestAssured.port = Integer.parseInt(config.getPort());
        RestAssured.proxy(config.getProxyIP(), Integer.parseInt(config.getProxyPort()));

        try {
            Response response =
                    given().
                            body("{\"email\":\"" + config.getCredsUsername() + "\",\"password\":\"" + config.getCredsPassword() + "\"}").
                            contentType("application/json").
                            when().
                            post("/rest/user/login");

            JsonObject jsonObject = new JsonParser().parse(response.getBody().asString()).getAsJsonObject();
            JsonObject newjsonObject = (JsonObject) jsonObject.get("authentication");
            token = newjsonObject.get("token").toString().replace("\"", "");
        } catch (Exception e) {
            LOGGER.info("Couldn't obtain ID token for given target. Offline? / Invalid credentials?");
            return token;
        }

        LOGGER.info("ID token for \"" + config.getCredsUsername() + " : " + config.getCredsPassword() + "\" : " + token);
        return token;
    }

    public static String getOAuth2TokenFromConfig() {
        Configuration config = new Configuration();
        String token = config.getOAuth2AccessToken();

        if (token == null) {
            LOGGER.info("No token found in config.");
            return "";
        }

        Pattern pattern = Pattern.compile("[a-zA-Z0-9]{16,}");
        Matcher matcher = pattern.matcher(token);

        if (!matcher.matches()) {
            LOGGER.info("Token invalid/insecure (less than 16 chars).");
            return token;
        } else {
            LOGGER.info("OAuth2Token from config : " + token);
            return token;
        }
    }

    public static String getOAuth2TokenFromBrentertainment() {
        Configuration config = new Configuration();
        RestAssured.baseURI = "http://brentertainment.com";
        RestAssured.port = 80;

        if (config.getBoolUseProxy()) {
            RestAssured.proxy(config.getProxyIP(), Integer.parseInt(config.getProxyPort()));
        }

        String access_token = "";

        String username = "demouser";
        String password = "testpass";
        String client_id = "demoapp";
        String client_secret = "demopass";

        String response =
                given()
                        .params("grant_type", "password", "client_id", client_id, "client_secret", client_secret,
                                "username", username, "password", password)
                        .when()
                        .post("/oauth2/lockdin/token")
                        .asString();

        JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
        access_token = jsonObject.get("access_token").getAsString();
        LOGGER.info("access_token: " + access_token);
        return access_token;
    }

    public static void accessBrentertainmentAPIwithToken(String access_token) {
        String response =
                given()
                        .auth().oauth2(access_token)
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                .when()
                        .get("/oauth2/lockdin/resource")
                .asString();
        JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
        LOGGER.info(jsonObject.toString());
    }

}
