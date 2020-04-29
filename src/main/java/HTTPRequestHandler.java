import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Map;

/**
 * @author Yangzhe Xie
 * @date 28/4/20
 */
public class HTTPRequestHandler {
    private final CharsetEncoder charsetEncoder;
    private final ByteBuffer buffer;
    private final StringBuilder content;
    private int mark;

    private HttpRequest request;

    /**
     * Initialize the request handler
     */
    public HTTPRequestHandler() {
        charsetEncoder = Charset.forName("UTF-8").newEncoder();
        buffer = ByteBuffer.allocate(4096);
        content = new StringBuilder();
        mark = 0;
    }

    /**
     * Process the request
     *
     * @param selectionKey selectionKey
     * @throws IOException
     */
    public void processRequestHeader(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        readBuffer(socketChannel);
        String line;
        String requestLine = null;
        while ((line = getLine()) != null) {
            if (requestLine == null) {
                /* Get the request line (Method + path + version) */
                requestLine = line;
            }
        }
        assert requestLine != null;
        String[] lineToken = requestLine.replace("\r\n", "").split(" ");
        HttpRequest request = new HttpRequest();
        request.setMethod(lineToken[0]);
        request.setPath(lineToken[1]);
        request.setVersion(lineToken[2]);
        setRequest(request);

        /* write for next round */
        selectionKey.interestOps(SelectionKey.OP_WRITE);
    }

    /**
     * Read lines from buffer
     *
     * @return
     */
    private String getLine() {
        StringBuilder sb = new StringBuilder();
        char tmp = ' ';
        while (buffer.hasRemaining()) {
            char c = (char) buffer.get();
            sb.append(c);
            if (c == '\n' && tmp == '\r') {
                mark = buffer.position();
                content.append(sb);
                return sb.substring(0, sb.length() - 2);
            }
            tmp = c;
        }
        return null;
    }

    /* Read raw request and write to buffer */
    private void readBuffer(SocketChannel socketChannel) throws IOException {
        int capacity = buffer.capacity();
        buffer.limit(capacity);
        int read = socketChannel.read(buffer);
        if (read == -1) {
            throw new IOException("Stream ends");
        }
        buffer.flip();
        buffer.position(mark);
    }

    /**
     * Send response
     *
     * @param socketChannel
     * @param response
     * @throws IOException
     */
    public void sendResponse(SocketChannel socketChannel, HttpResponse response) throws IOException {
        String statusLine = response.getVersion() + " " + response.getCode() + " " + response.getReason();
        writeLine(socketChannel, statusLine);
        Map<String, String> headers = response.getHeaders();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            writeLine(socketChannel, key + ": " + value);
        }
        writeLine(socketChannel);
        ByteBuffer byteBuffer = ByteBuffer.wrap(response.getContent());
        socketChannel.write(byteBuffer);
    }

    private void writeLine(SocketChannel socketChannel) throws IOException {
        writeLine(socketChannel, "");
    }

    private void writeLine(SocketChannel socketChannel, String line) throws IOException {
        CharBuffer charBuffer = CharBuffer.wrap(line + "\r\n");
        ByteBuffer byteBuffer = charsetEncoder.encode(charBuffer);

        socketChannel.write(byteBuffer);
    }

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }
}
