package restsec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restsec.config.Configuration;

import java.io.*;
import java.util.Set;

public class AttackSet extends JSONObject {

    private Logger LOGGER = LoggerFactory.getLogger(AttackSet.class);

    private Configuration config = new Configuration();

    void writeAttackSetToFile(AttackSet attackSet, String filePath) {

        if (filePath.equals("default")) {
            filePath = config.getAttackSetFileLocation();
        }

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

        LOGGER.info("AttackSet written to file (size : "+attackSet.size()+").");

    }

    JSONObject getAttackSetFromFile(String fileName) {
        JSONObject payloads = new JSONObject();
        try {
            JSONParser parser = new JSONParser();
            payloads = (JSONObject) parser.parse(new FileReader(new File(fileName)));
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        return payloads;
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

    private void print(AttackSet attackSet) {
        LOGGER.info("--- Attack Set ---");
        for (Object key : attackSet.keySet()) {
            LOGGER.info(key + " : " + attackSet.get(key));
        }
        LOGGER.info("------------------");
    }

}
