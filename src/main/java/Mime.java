import java.util.HashMap;
import java.util.Map;

public class Mime {
    private static final Map<String, String> mimes;

    static {
        mimes = new HashMap<String, String>();
        mimes.put("doc", "application/msword");
        mimes.put("exe", "application/octet-stream");
        mimes.put("js", "application/javascript");
        mimes.put("zip", "application/zip");
        mimes.put("aiff", "audio/aiff");
        mimes.put("midi", "audio/midi");
        mimes.put("mp3", "audio/mpeg3");
        mimes.put("wav", "audio/wav");
        mimes.put("gif", "image/gif");
        mimes.put("jpg", "image/jpeg");
        mimes.put("png", "image/png");
        mimes.put("tiff", "image/tiff");
        mimes.put("css", "text/css");
        mimes.put("html", "text/html; charset=UTF-8");
        mimes.put("txt", "text/plain; charset=UTF-8");
        mimes.put("xml", "text/xml");
        mimes.put("avi", "video/avi");
        mimes.put("mov", "video/quicktime");
        mimes.put("mpeg", "video/mpeg");
        mimes.put("mp4", "video/mp4");
    }

    public static String get(String fileType) {
        return mimes.get(fileType);
    }

}
