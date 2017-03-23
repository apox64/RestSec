package restsec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class AttackSet extends JSONObject {

    private Logger LOGGER = LoggerFactory.getLogger(AttackSet.class);

    public AttackSet() {
    }

    void writeAttackSetToFile(AttackSet attackSet, String filePath) {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        print(attackSet);

        try {

            FileWriter file = new FileWriter(filePath);

            String output = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(attackSet);

            file.write(output);
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("AttackSet written to file (size : "+getSize(attackSet)+").");

    }

    @SuppressWarnings("unchecked")
    public AttackSet createFullAttackSet(JSONObject pathsObject) {
        AttackSet attackSet = new AttackSet();

        for (String currentPath : (Set<String>) pathsObject.keySet()) {
            JSONArray array = new JSONArray();
            array.add("POST");
            array.add("PATCH");
            array.add("PUT");
            array.add("DELETE");
            attackSet.put(currentPath, array);
        }
        return attackSet;
    }

    private int getSize(AttackSet attackSet) {
        int counter = 0;

        for (Object key : attackSet.keySet()) {
            JSONArray a = (JSONArray) attackSet.get(key);
            counter += a.size();
        }

        return counter;
    }

    private void print(AttackSet attackSet) {
        LOGGER.info("--- Attack Set ---");
        for (Object key : attackSet.keySet()) {
            LOGGER.info(key + " : " + attackSet.get(key));
        }
        LOGGER.info("------------------");
    }

}
