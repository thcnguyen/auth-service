package exchange.velox.authservice.util;

import org.apache.commons.collections4.MapUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public abstract class VeloService<T extends VeloService> {
    protected String host;
    protected int port;
    protected String contextPath;
    protected Map<String, String> params = new HashMap<>();
    protected StringBuilder path = new StringBuilder();
    protected T _this = (T) this;

    public static log log() {
        return new log();
    }

    public static docgen docgen() {
        return new docgen();
    }

    public static notification notification() {
        return new notification();
    }

    public T p(String name, String value) {
        params.put(name, value);
        return _this;
    }

    public String build() {
        try {
            StringBuilder sb = new StringBuilder("http://")
                        .append(host).append(":").append(port)
                        .append("/").append(contextPath)
                        .append(path);
            if (MapUtils.isNotEmpty(params)) {
                sb.append("?");
                boolean first = true;
                for (Map.Entry<String, String> param : params.entrySet()) {
                    if (!first) {
                        sb.append("&");
                    }
                    sb.append(param.getKey()).append("=").append(URLEncoder.encode(param.getValue(), "UTF-8"));
                    first = false;
                }
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't build the url to " + host + " with path " + path, e);
        }
    }

    public static class log extends VeloService<log> {
        private log() {
            this.host = "log-service";
            this.port = 9001;
            this.contextPath = "log";
        }

        public log add() {
            path.append("/add");
            return this;
        }

        public log list() {
            path.append("/list");
            return this;
        }
    }

    public static class notification extends VeloService<notification> {
        private notification() {
            this.host = "notification-service";
            this.port = 9002;
            this.contextPath = "notification";
        }

        public notification sendMailRequest() {
            path.append("/email/request-mail");
            return this;
        }
    }

    public static class docgen extends VeloService<docgen> {
        private docgen() {
            this.host = "docgen-service";
            this.port = 9003;
            this.contextPath = "docgen";
        }

        public docgen rest() {
            path.append("/rest");
            return this;
        }

        public docgen generate() {
            path.append("/generate");
            return this;
        }
    }

}
