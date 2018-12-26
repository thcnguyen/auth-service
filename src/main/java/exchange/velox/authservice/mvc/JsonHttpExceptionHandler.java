package exchange.velox.authservice.mvc;

import net.etalia.crepuscolo.utils.ChainMap;
import net.etalia.crepuscolo.utils.HttpException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JsonHttpExceptionHandler implements HandlerExceptionResolver, Ordered {

    protected Log log = LogFactory.getLog(JsonHttpExceptionHandler.class);

    private int order = -1;

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
                                         Exception ex) {
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);
        HttpException htex;
        if (!(ex instanceof HttpException)) {
            htex = new HttpException().cause(ex);
        } else {
            htex = (HttpException) ex;
        }

        try {
            int statusCode = htex.getStatusCode();
            String reason = htex.getMessage();
            String errorCode = htex.getErrorCode();

            if (!htex.hasSetStatusCode() && responseStatus != null) {
                statusCode = responseStatus.value().value();
            }

            if (!StringUtils.hasLength(reason) && responseStatus != null) {
                reason = responseStatus.reason();
            }
            if (!StringUtils.hasLength(reason)) {
                reason = ex.getMessage();
            }
            if (!StringUtils.hasLength(reason)) {
                response.setStatus(statusCode);
            } else {
                setErrorStatus(response, statusCode, errorCode);
            }

            if (!StringUtils.hasLength(errorCode)) {
                errorCode = "ERROR";
            }
            ChainMap<Object> errmap = new ChainMap<Object>("code", errorCode).add("message", reason);

            if (htex.getProperties() != null) {
                errmap.putAll(htex.getProperties());
            }

            log.error(errmap, ex);

            return new ModelAndView(new JsonView(), errmap);

        } catch (Exception e) {
            log.error(ex.getMessage(), ex);
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("deprecation")
    private void setErrorStatus(HttpServletResponse response, int statusCode, String errorCode) {
        response.setStatus(statusCode, errorCode);
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

}
