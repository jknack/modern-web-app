package com.github.jknack.mwa.web;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Process method return values of methods marked with {@link JsonView} and
 * {@link ResponseBody}.
 *
 * @author edgar.espina
 * @since 0.3.0
 */
public class JacksonViewMethodProcessor implements
    HandlerMethodReturnValueHandler, Ordered {

  /**
   * The JSON parser.
   */
  private ObjectMapper mapper;

  /**
   * Creates a new {@link JacksonViewMethodProcessor}.
   *
   * @param mapper The JSON parser. Required.
   */
  public JacksonViewMethodProcessor(final ObjectMapper mapper) {
    this.mapper = notNull(mapper, "The JSON parser is required.");
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  @Override
  public boolean supportsReturnType(final MethodParameter returnType) {
    return returnType.getMethodAnnotation(ResponseBody.class) != null
        && returnType.getMethodAnnotation(JsonView.class) != null;
  }

  @Override
  public void handleReturnValue(final Object returnValue,
      final MethodParameter returnType,
      final ModelAndViewContainer mavContainer,
      final NativeWebRequest webRequest) throws Exception {
    // Stop Spring MVC.
    mavContainer.setRequestHandled(true);
    if (returnValue == null) {
      return;
    }
    // Configure the HTTP response.
    HttpServletResponse response =
        webRequest.getNativeResponse(HttpServletResponse.class);
    response.setContentType("application/json");
    Writer writer = response.getWriter();

    try {
      JsonFactory jsonFactory = mapper.getFactory();
      JsonGenerator jsonGenerator =
          jsonFactory.createJsonGenerator(writer);

      JsonView view = returnType.getMethodAnnotation(JsonView.class);
      Class<?>[] viewClass = view.value();
      notEmpty(viewClass, "The view class is missing: " + returnType);
      // prepare a writer
      ObjectWriter objectWriter = mapper.writerWithView(viewClass[0]);
      // write output
      objectWriter.writeValue(jsonGenerator, returnValue);
    } finally {
      writer.flush();
    }
  }

}
