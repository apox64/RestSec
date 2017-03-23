package restsec.config;

// TODO: reads the config file, offers getMethods that return all values

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import static restsec.config.CrawlerType.HATEOAS;
import static restsec.config.CrawlerType.SWAGGER;
import static restsec.config.ScannerType.SQLI;
import static restsec.config.ScannerType.XSS;

public class Configuration {

    //TODO: Guice = Google Dependency Injection Framework
    //TODO: Config validate, throwing Exceptions
    //TODO: Switch-case on CrawlerType.valueOf()

    private static final Logger logger = Logger.getLogger(Configuration.class);
    private Properties properties;

    //TODO: HashMap properties;

    public Configuration() {
        loadProperties();
    }

    private void loadProperties() {
        //TODO: check all properties from file ONCE, put to HashMap (e.g. CrawlerType)
        this.properties = new Properties();
        try(InputStream inputStream = Configuration.class.getClassLoader().getResourceAsStream("config.properties")){
            properties.load(inputStream);
        } catch (IOException ioe) {
            logger.warn("Properties could not be loaded.");
            ioe.printStackTrace();
        }
        logger.info(Integer.toString(properties.size()) + " properties loaded.");
    }

    public String getTargetURLAsString() {
        return getBaseURI()+":"+getPort()+getBasePath();
    }

    public String getBaseURI(){
        return properties.getProperty("base-uri");
    }

    public String getPort(){
        return properties.getProperty("port");
    }

    public String getBasePath(){
        return properties.getProperty("base-path");
    }

    public boolean getBoolUseProxy() {
        return Boolean.parseBoolean(properties.getProperty("useProxy"));
    }

    public String getProxyIP(){
        return properties.getProperty("proxy_ip");
    }

    public String getProxyPort(){
        return properties.getProperty("proxy_port");
    }

    public String getCredsUsername(){
        return properties.getProperty("username");
    }

    public String getCredsPassword(){
        return properties.getProperty("password");
    }

    public String getCookie() {
        return properties.getProperty("cookie");
    }

    public CrawlerType getCrawlerType() {
        switch (String.valueOf(properties.getProperty("crawlerType").toUpperCase())) {
            case "HATEOAS":
                return HATEOAS;
            case "SWAGGER":
                return SWAGGER;
            default:
                return null;
        }
        /*
        if (properties.getProperty("crawlerType").toUpperCase().equals("HATEOAS")) {
            return HATEOAS;
        } else if (properties.getProperty("crawlerType").toUpperCase().equals("SWAGGER")) {
            return SWAGGER;
        }
        return null;
        */
    }

    public ScannerType getScannerType(){
        switch (String.valueOf(properties.getProperty("scannerType").toUpperCase())) {
            case "XSS":
                return XSS;
            case "SQLI":
                return SQLI;
            default:
                return null;
        }
        /*
        if (properties.getProperty("scannerType").toUpperCase().equals("XSS")) {
            return XSS;
        } else if (properties.getProperty("scannerType").toUpperCase().equals("SQLI")) {
            return SQLI;
        }
        return null;
        */
    }

    public String getHATEOASEntryPoint(){
        return properties.getProperty("entryPointHATEOAS");
    }

    public String getSwaggerFileLocation(){
        return properties.getProperty("swaggerLocation");
    }

    public String getAttackSetFileLocation() {
        if (properties.getProperty("attackSetFileLocation").toLowerCase().equals("default")) {
            return "src/main/resources/attackable/attackset.json";
        } else {
            return properties.getProperty("attackSetFileLocation");
        }
    }

    public String getXSSPayloadsFileLocation(){
        return properties.getProperty("xssPayloadsFile");
    }

    public String getSQLiPayloadsFileLocation(){
        return properties.getProperty("sqliPayloadsFile");
    }

    public Boolean getBoolDeleteOldJettyLogs() {
        return Boolean.parseBoolean(properties.getProperty("deleteOldJettyLogs"));
    }

    public int getJettyCallbackPort() {
        return Integer.parseInt(properties.getProperty("callback_port"));
    }

    public boolean getBoolUseAllHTTPMethods(){
        return Boolean.parseBoolean(properties.getProperty("allHTTPMethods"));
    }

    public boolean getBoolDeleteOldResultsFile(){
        return Boolean.parseBoolean(properties.getProperty("deleteOldResultsFile"));
    }

}
