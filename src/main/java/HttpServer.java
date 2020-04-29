import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * @author Yangzhe Xie
 * @date 28/4/20
 */
public class HttpServer {
    private final InetSocketAddress inetSocketAddress;
    private final Logger logger = Logger.getLogger(HttpServer.class);
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private SelectionKeyHandler selectionKeyHandler;

    public HttpServer(InetSocketAddress inetSocketAddress) {
        this.inetSocketAddress = inetSocketAddress;
    }

    /**
     * Initialize the server
     *
     * @throws IOException
     */
    public void init() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(inetSocketAddress, 100);  /* At most 100 connections */
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        selectionKeyHandler = new SelectionKeyHandler(selector);
    }

    /**
     * Start receiving the requests
     *
     * @throws IOException
     */
    public void start() throws IOException {
        logger.info("Server started at 127.0.0.1:" + Config.PORT);
        try {
            while (!Thread.currentThread().isInterrupted()) {
                selector.select(Config.TIMEOUT);
                Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                if (selectionKeySet.isEmpty()) {
                    continue;
                }
                for (SelectionKey selectionKey : selectionKeySet) {
                    try {
                        if (selectionKey.isValid()) {
                            /* Send the command to SelectionKeyHandler */
                            selectionKeyHandler.handleSelectionKeyCommand(selectionKey);
                        }
                    } catch (Exception e) {
                        /* Error happens, close connection */
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        socketChannel.close();
                    }
                }
            }
        } catch (IOException e) {
            /* Error happens, close the server */
            logger.info("Error happens, closing the server");
            stop();
            throw e;
        }
    }

    /**
     * Close the server
     *
     * @throws IOException
     */
    private void stop() throws IOException {
        selector.close();
        serverSocketChannel.close();
    }
}
