package restsec.crawler;

import restsec.Configuration;

public class CrawlerFactory {

    private Configuration config;

    public CrawlerFactory(Configuration configuration) {
        config = configuration;
    }

    public Crawler createCrawler() {

        String crawlerType = config.getDocumentationType();

        if (crawlerType == null) {
            return null;
        } else if (crawlerType.toUpperCase().equals("HATEOAS")) {
            return new HATEOASCrawler(config.getHATEOASEntryPoint());
        } else if (crawlerType.toUpperCase().equals("SWAGGER")) {
            return new SwaggerParser(config.getSwaggerFileLocation(), config.getBoolUseAllHTTPMethods());
        }

        return null;
    }
}
