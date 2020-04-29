import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Map;

/**
 * @author Yangzhe Xie
 * @date 28/4/20
 */
public class HTTPRequestHandler {
    private Charset charset;
    private CharsetEncoder charsetEncoder;
    private ByteBuffer buffer;
    private StringBuilder content;
    private int mark;

    public HTTPRequestHandler() {
        charset = Charset.forName("UTF-8");
        charsetEncoder = charset.newEncoder();
        buffer = ByteBuffer.allocate(4096);
        content = new StringBuilder();
        mark = 0;
    }

    public void processRequestHeader(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        readBuffer(socketChannel);
        String line;
        while ((line = getLine()) != null) {
            System.out.println(line);
            //TODO: process request headers
        }
        // write the next round
        selectionKey.interestOps(SelectionKey.OP_WRITE);
    }

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
}
