import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author Yangzhe Xie
 * @date 28/4/20
 */
public class SelectionKeyHandler {

    private final Selector selector;

    public SelectionKeyHandler(Selector selector) {
        this.selector = selector;
    }

    public void handleSelectionKeyCommand(SelectionKey selectionKey) throws IOException {
        if (selectionKey.isAcceptable()) {
            handleAcceptableCommand(selectionKey);
        } else if (selectionKey.isReadable()) {
            handleReadableCommand(selectionKey);
        } else if (selectionKey.isWritable()) {
            handleWritableCommand(selectionKey);
        }
    }

    private void handleAcceptableCommand(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        if (socketChannel == null) {
            return;
        }
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

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

    private void handleWritableCommand(SelectionKey selectionKey) throws IOException {
        HTTPRequestHandler requestHandler = (HTTPRequestHandler) selectionKey.attachment();
        if (requestHandler == null) {
            throw new IOException("Response not ready");
        }
        HttpResponse response = HttpResponse.getDefaultInstance();
        response.setContent("Hello World!".getBytes());
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        requestHandler.sendResponse(socketChannel, response);
        selectionKey.interestOps(SelectionKey.OP_READ);
    }
}
