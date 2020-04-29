import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author Yangzhe Xie
 * @date 28/4/20
 */
public class HttpServerStarter {
    private static final Logger logger = Logger.getLogger(HttpServerStarter.class);

    public static void main(String[] args) {
        if (args.length > 0) {
            if (args.length != 2) {
                System.out.println("usage: java -jar HttpServerStarter.jar <port> <www root path>");
                return;
            }
            try {
                Config.PORT = Integer.parseInt(args[0]);
                Config.WWWROOT = args[1];
            } catch (Exception e) {
                System.out.println("usage: java -jar HttpServerStarter.jar <port> <www root path>");
                return;
            }
        }
        InetSocketAddress address = new InetSocketAddress(Config.PORT);
        HttpServer server = new HttpServer(address);
        try {
            server.init();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
