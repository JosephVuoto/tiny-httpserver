import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author Yangzhe Xie
 * @date 29/4/20
 */
@SuppressWarnings("ALL")
public class WritableCommandWorker implements Runnable {
    private final SelectionKey selectionKey;
    private final Logger logger = Logger.getLogger(WritableCommandWorker.class);

    public WritableCommandWorker(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    public void run() {
        HTTPRequestHandler requestHandler = (HTTPRequestHandler) selectionKey.attachment();
        if (requestHandler == null) {
            logger.info("Response not ready");
            return;
        }
        logger.info(requestHandler.getRequest());
        HttpResponse response = HttpResponse.getDefaultInstance();

        try {
            String path = requestHandler.getRequest().getPath();
            if ("/".equals(path)) {
                path = "/index.html";
            }
            File file = new File(Config.WWWROOT, path);
            if (file.exists()) {
                byte[] data = FileUtil.toByteArray(file);
                String extensionName = path.substring(path.lastIndexOf(".") + 1);
                response.addHeader("Content-Type: ", Mime.get(extensionName));
                response.setContent(data);
            } else {
                response.setCode("404");
                response.setReason("Not Found");
                response.setContent("404 Not Found.".getBytes());
                response.addHeader("Content-Type: ", "text/html");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        try {
            requestHandler.sendResponse(socketChannel, response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
