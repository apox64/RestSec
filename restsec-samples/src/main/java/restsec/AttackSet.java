package restsec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restsec.config.Configuration;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class AttackSet {

    private Logger LOGGER = LoggerFactory.getLogger(AttackSet.class);
    private JSONObject attackSet = new JSONObject();

//    public AttackSet(JSONObject attackSet) {
//        this.attackSet = attackSet;
//    }

    public AttackSet() {

    }

    public String getAttackSetFileName() {

        Configuration config = new Configuration();

        if (config.getAttackSetFileLocation().equals("default")) {
            return "src/main/resources/attackable/attackset.json";
        }
        return config.getAttackSetFileLocation();
    }

    public void writeAttackSetToFile(AttackSet attackSet) {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        printAttackSet();

        try {

            FileWriter file = new FileWriter("src/main/resources/attackable/attackset.json");

            String output = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(attackSet);

            file.write(output);
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("AttackSet written to File.");

    }

    @SuppressWarnings("unchecked")
    public JSONObject createFullAttackSetForPathsObject(JSONObject pathsObject) {
        JSONObject attackSet = new JSONObject();

        for (String currentPath : (Set<String>) pathsObject.keySet()) {
            JSONArray array = new JSONArray();
            array.add("POST");
            array.add("PATCH");
            array.add("PUT");
            array.add("DELETE");
            attackSet.put(currentPath, array);
        }

        LOGGER.info("attackSet created for bruteforcing (size: " + pathsObject.size() * 4 + ")");
        return attackSet;
    }

    public JSONObject getAttackSet() {
        return this.attackSet;
    }

    public void setAttackSet(JSONObject attackSet) {
        this.attackSet = attackSet;
    }

    private void printAttackSet() {
        System.out.println("--- Attack Set ---");
        for (Object key : attackSet.keySet()) {
            System.out.println(key + " : " + attackSet.get(key));
        }
        System.out.println("------------------");
    }

    @SuppressWarnings("unchecked")
    public void put(Object key, Boolean aBoolean) {
        attackSet.put(key, aBoolean);
    }
}
