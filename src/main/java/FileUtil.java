import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Yangzhe Xie
 * @date 29/4/20
 */
public class FileUtil {

    public static byte[] toByteArray(File file) throws IOException {

        FileChannel fileChannel = null;
        try {
            fileChannel = new RandomAccessFile(file, "r").getChannel();
            MappedByteBuffer byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0,
                    fileChannel.size()).load();
            byte[] result = new byte[(int) fileChannel.size()];
            if (byteBuffer.remaining() > 0) {
                byteBuffer.get(result, 0, byteBuffer.remaining());
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                assert fileChannel != null;
                fileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
