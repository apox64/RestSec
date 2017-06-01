package restsec;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restsec.config.Configuration;

import static io.restassured.RestAssured.*;

public class Authentication {

    private static final Logger LOGGER = LoggerFactory.getLogger(Authentication.class);

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
                            body("{\"email\":\"" + config.getCredsUsername() + "\",\"password\":\"" + config.getCredsPassword() + "\"}")
                            .contentType("application/json").
                            when().
                            post("/rest/user/login");

            JsonObject jsonObject = new JsonParser().parse(response.getBody().asString()).getAsJsonObject();
            JsonObject newjsonObject = (JsonObject) jsonObject.get("authentication");
            token = newjsonObject.get("token").toString().replace("\"", "");
        } catch (Exception e) {
            LOGGER.info("Couldn't obtain ID token. Target offline? / Invalid credentials?");
            return token;
        }

        LOGGER.info("ID token for \"" + config.getCredsUsername() + " : " + config.getCredsPassword() + "\" : " + token);
        return token;
    }

    public static String getOAuth2TokenFromConfig() {
        Configuration config = new Configuration();
        RestAssured.baseURI = config.getBaseURI();
        RestAssured.basePath = config.getBasePath();
        RestAssured.port = Integer.parseInt(config.getPort());
        RestAssured.proxy(config.getProxyIP(), Integer.parseInt(config.getProxyPort()));

        String token = config.getOAuth2Token();
        LOGGER.info("OAuth2Token from config : " + token);
        Response response =
                given().
                        auth().oauth2(token).when().post("/SOME/PATH");
        return token;
    }

}
