package exchange.velox.authservice.mvc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RequestLoggerFilter implements Filter {

	protected Log log = LogFactory.getLog(RequestLoggerFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                ServletException {
		long start = System.currentTimeMillis();
		chain.doFilter(request, response);
		long end = System.currentTimeMillis();
		long time = end - start;
		String url = null;
		String queryString = null;
		String method = null;
		if (request instanceof HttpServletRequest) {
			url = ((HttpServletRequest)request).getContextPath();
			url += ((HttpServletRequest)request).getServletPath();
			queryString = ((HttpServletRequest)request).getQueryString();
			method = ((HttpServletRequest)request).getMethod();
		}
		int code = -1;
		if (response instanceof HttpServletResponse) {
			code = ((HttpServletResponse) response).getStatus();
		}
		log.info("TIME: " + time + " - CODE: " + code + " - METHOD: " + method + " - URL: " + url + " - PARAMS: " + queryString);
	}

	@Override
	public void destroy() {
	}

}
