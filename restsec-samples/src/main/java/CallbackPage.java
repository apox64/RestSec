
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;

public class CallbackPage {

    //opens jetty server and waits for input

    private static Server server;

    public CallbackPage() {
        configureJettyLogging();
    }

    private static void configureJettyLogging() {
        HandlerCollection handlers = new HandlerCollection();
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        RequestLogHandler requestLogHandler = new RequestLogHandler();
        handlers.setHandlers(new Handler[]{contexts,new DefaultHandler(),requestLogHandler});
        server.setHandler(handlers);

        NCSARequestLog requestLog = new NCSARequestLog("restsec-samples/src/main/resources/jetty-logs/jetty-request.log");
        requestLog.setRetainDays(90);
        requestLog.setAppend(false);
        requestLog.setExtended(false);
        requestLog.setLogTimeZone("GMT");
        requestLogHandler.setRequestLog(requestLog);

        System.out.println("CallbackPage: configureJettyLogging done.");
    }

    public static void startTestPageServer() throws Exception {
        server = new Server(5555);
        configureJettyLogging();
        server.start();
    }

    public static void stopTestPageServer() throws Exception {
        server.stop();
    }

    //this lets you execute the stored payload
    private static void reloadResource(){
        //use selenium webdriver here
    }


}
