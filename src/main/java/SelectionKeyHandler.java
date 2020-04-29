import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author Yangzhe Xie
 * @date 28/4/20
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class SelectionKeyHandler {

    private final Selector selector;

    private final Logger logger = Logger.getLogger(SelectionKeyHandler.class);

    public SelectionKeyHandler(Selector selector) {
        this.selector = selector;
    }

    /**
     * Handle selectionKeys
     *
     * @param selectionKey
     * @throws IOException
     */
    public void handleSelectionKeyCommand(SelectionKey selectionKey) throws IOException {
        if (selectionKey.isAcceptable()) {
            handleAcceptableCommand(selectionKey);
        } else if (selectionKey.isReadable()) {
            handleReadableCommand(selectionKey);
        } else if (selectionKey.isWritable()) {
            handleWritableCommand(selectionKey);
        }
    }

    /**
     * Handle acceptable selectionKeys
     *
     * @param selectionKey
     * @throws IOException
     */
    private void handleAcceptableCommand(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        if (socketChannel == null) {
            return;
        }
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    /**
     * Handle readble selectionKeys
     *
     * @param selectionKey
     * @throws IOException
     */
    private void handleReadableCommand(SelectionKey selectionKey) throws IOException {
        HTTPRequestHandler requestHandler = (HTTPRequestHandler) selectionKey.attachment();
        if (requestHandler == null) {
            requestHandler = new HTTPRequestHandler();
            selectionKey.attach(requestHandler);
        }
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        try {
            requestHandler.processRequestHeader(selectionKey);
        } catch (IOException e) {
            socketChannel.close();
            selectionKey.cancel();
        }
    }

    /**
     * Handle writable selectionKeys
     *
     * @param selectionKey
     * @throws IOException
     */
    private void handleWritableCommand(SelectionKey selectionKey) throws IOException {
        HTTPRequestHandler requestHandler = (HTTPRequestHandler) selectionKey.attachment();
        if (requestHandler == null) {
            throw new IOException("Response not ready");
        }
        logger.info(requestHandler.getRequest());
        HttpResponse response = HttpResponse.getDefaultInstance();

        try {
            String path = requestHandler.getRequest().getPath();
            if ("/".equals(path)) {
                path = "/index.html";
            }
            File file = new File(Config.WWWROOT, path);
            FileInputStream in = new FileInputStream(file);
            byte[] data = new byte[in.available()];
            in.read(data);
            in.close();
            String extensionName = path.substring(path.lastIndexOf(".") + 1);
            response.addHeader("Content-Type: ", Mime.get(extensionName));
            response.setContent(data);
        } catch (FileNotFoundException e) {
            response.setCode("404");
            response.setReason("Not Found");
            response.setContent("404 Not Found.".getBytes());
            response.addHeader("Content-Type: ", "text/html");
        }

        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        requestHandler.sendResponse(socketChannel, response);
        selectionKey.interestOps(SelectionKey.OP_READ);
    }
}
