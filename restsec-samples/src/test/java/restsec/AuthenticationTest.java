package restsec;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthenticationTest {

    @Test
    @DisplayName("token is not empty")
    void getJuiceShopTokenBodyAuthIsNotEmpty() {
        Assertions.assertTrue(!Authentication.getTokenForJuiceShop_BodyAuth().equals(""));
    }

}