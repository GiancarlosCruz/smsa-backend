package util;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class LoggingFilter implements ContainerRequestFilter {

  private static final Logger LOG = Logger.getLogger(LoggingFilter.class);

  @Override
  public void filter(ContainerRequestContext context) {
    LOG.infof("Método: %s | URL: %s",
            context.getMethod(),
            context.getUriInfo().getRequestUri());
  }
}