import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author Yangzhe Xie
 * @date 28/4/20
 */
public class Main {
    public static void main(String[] args) {
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
