package exchange.velox.authservice.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class JsonView extends AbstractView {

    private boolean updateContentLength = false;
    private String jsonPrefix;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
                                           HttpServletResponse response) throws Exception {
        OutputStream stream = this.updateContentLength ? this.createTemporaryOutputStream() : response
                    .getOutputStream();
        this.writeContent(stream, model, this.jsonPrefix);
        if (this.updateContentLength) {
            this.writeToResponse(response, (ByteArrayOutputStream) stream);
        }
    }

    public boolean isUpdateContentLength() {
        return updateContentLength;
    }

    public void setUpdateContentLength(boolean updateContentLength) {
        this.updateContentLength = updateContentLength;
    }

    public String getJsonPrefix() {
        return jsonPrefix;
    }

    public void setJsonPrefix(String jsonPrefix) {
        this.jsonPrefix = jsonPrefix;
    }

    protected void writeContent(OutputStream stream, Object value, String jsonPrefix) throws IOException {
        if (jsonPrefix != null) {
            stream.write(jsonPrefix.getBytes("UTF-8"));
        }

        this.objectMapper.writeValue(stream, value);
    }

    public final ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }
}
