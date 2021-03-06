import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Yangzhe Xie
 * @date 28/4/20
 */
public class HttpResponse {
    private String version;
    private String code;
    private String reason;
    private Map<String, String> headers;
    private byte[] content;

    public static HttpResponse getDefaultInstance() {
        HttpResponse response = new HttpResponse();
        response.setVersion("HTTP/1.1");
        response.setCode("200");
        response.setReason("OK");
        response.setHeaders(new LinkedHashMap<String, String>());
        response.putDefaultHeaders();
        return response;
    }

    private void putDefaultHeaders() {
        headers.put("Date", new Date().toString());
        headers.put("Server", "Joseph's Tiny HTTP Server");
        headers.put("Connection", "close");
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
        headers.put("Content-Length", Integer.toString(content.length));
    }
}
