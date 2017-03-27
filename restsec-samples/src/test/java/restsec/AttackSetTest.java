package restsec;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import restsec.config.Configuration;

import java.util.HashMap;

class AttackSetTest {

    private static Configuration config = new Configuration();

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Creating full attack set for pathsObject")
    void createFullAttackSetTest() {
        JSONObject pathsObject = new JSONObject();
        pathsObject.put("/api/Products/1", new HashMap<>().put("put", new JSONObject()));
        AttackSet attackSet = new AttackSet().createFullAttackSet(pathsObject);
        String s = attackSet.get("/api/Products/1").toString();
        Assertions.assertTrue(s.contains("POST") && s.contains("PATCH") && s.contains("PUT") && s.contains("DELETE"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void writeAttackSetToFileTest() {
        AttackSet attackSet = new AttackSet();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key", "value");
        attackSet.put("/api/Products/1", new HashMap<>().put("put", jsonObject));
        new AttackSet().writeAttackSetToFile(attackSet, config.getAttackSetFileLocation());
        JSONObject written = new AttackSet().getAttackSetFromFile(config.getAttackSetFileLocation());
        Assertions.assertEquals(written.entrySet(), attackSet.entrySet());
    }

}