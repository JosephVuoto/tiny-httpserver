import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Yangzhe Xie
 * @date 28/4/20
 */
public class HttpServer {
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private final InetSocketAddress inetSocketAddress;
    private SelectionKeyHandler selectionKeyHandler;

    public HttpServer(InetSocketAddress inetSocketAddress) {
        this.inetSocketAddress = inetSocketAddress;
    }

    public void init() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(inetSocketAddress, 100);
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        selectionKeyHandler = new SelectionKeyHandler(selector);
    }

    public void start() throws IOException {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                selector.select(Config.TIMEOUT);
                Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                if (selectionKeySet.isEmpty()) {
                    continue;
                }
                Iterator<SelectionKey> iterator = selectionKeySet.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();
                    try {
                        if (selectionKey.isValid()) {
                            selectionKeyHandler.handleSelectionKeyCommand(selectionKey);
                        }
                    } catch (Exception e) {
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        socketChannel.close();
                    }
                }
            }
        } catch (IOException e) {
            stop();
            throw e;
        }
    }

    private void stop() throws IOException {
        selector.close();
        serverSocketChannel.close();
    }
}
