package restsec.config;

// TODO: reads the config file, offers getMethods that return all values

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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

    public String getCrawlerType(){
        // TODO: public CrawlerType getCrawlerType()
        return properties.getProperty("crawlerType");
    }

    public String getHATEOASEntryPoint(){
        return properties.getProperty("entryPointHATEOAS");
    }

    public String getSwaggerFileLocation(){
        return properties.getProperty("swaggerLocation");
    }

    public String getAttackSetFileLocation() {
        return properties.getProperty("attackSetFileLocation");
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

    public String getVulnerabilityScanType(){
        return properties.getProperty("scanForVulnerabilityTypes");
    }

    public boolean getBoolDeleteOldResultsFile(){
        return Boolean.parseBoolean(properties.getProperty("deleteOldResultsFile"));
    }

}
