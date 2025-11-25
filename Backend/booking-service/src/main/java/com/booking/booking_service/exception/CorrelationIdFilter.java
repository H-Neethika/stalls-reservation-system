package com.booking.booking_service.exception;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CorrelationIdFilter implements Filter {

  public static final String CORRELATION_ID_ATTR = "correlationId";
  public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    String correlationId = UUID.randomUUID().toString();
    request.setAttribute(CORRELATION_ID_ATTR, correlationId);
    if (response instanceof HttpServletResponse httpResponse) {
      httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
    }
    chain.doFilter(request, response);
  }
}
