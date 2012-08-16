package com.github.jknack.mwa;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.mwa.handler.MessageConverterHandlerExceptionResolver;

/**
 * All the changes made by MWA are here.
 *
 * @author edgar.espina
 * @since 0.1.3
 */
enum BeanPostProcessors {

  /**
   * Do nothing.
   */
  DEFAULT {
    @Override
    public boolean matches(final Object bean) {
      return true;
    }
  },

  /**
   * Configure {@link MessageConverterHandlerExceptionResolver}.
   */
  MESSAGE_CONVERTER_EXCEPTION_RESOLVER {
    @Override
    public boolean matches(final Object bean) {
      return bean instanceof MessageConverterHandlerExceptionResolver;
    }

    @Override
    public Object processAfterInstantiation(
        final ApplicationContext context, final Object bean) {
      MessageConverterHandlerExceptionResolver exceptionResolver =
          (MessageConverterHandlerExceptionResolver) bean;
      RequestMappingHandlerAdapter mapping =
          context.getBean(RequestMappingHandlerAdapter.class);
      exceptionResolver.setMessageConverters(mapping.getMessageConverters());
      return bean;
    }
  },

  /**
   * Add handler interceptors.
   */
  ADD_HANDLER_INTERCEPTORS {
    @Override
    public boolean matches(final Object bean) {
      return bean instanceof AbstractHandlerMapping;
    }

    @Override
    public Object processAfterInstantiation(
        final ApplicationContext context, final Object bean) {
      AbstractHandlerMapping handlerMapping = (AbstractHandlerMapping) bean;
      List<HandlerInterceptor> candidates =
          Beans.lookFor(context, HandlerInterceptor.class);
      OrderComparator.sort(candidates);
      Object[] interceptors = new Object[candidates.size()];
      for (int i = interceptors.length - 1, j = 0; i >= 0; i--, j++) {
        HandlerInterceptor interceptor = candidates.get(i);
        RequestMapping mapping =
            AnnotationUtils.findAnnotation(interceptor.getClass(),
                RequestMapping.class);
        if (mapping != null && mapping.value().length > 0) {
          interceptors[j] =
              new MappedInterceptor(mapping.value(), interceptor);
        } else {
          interceptors[j] = interceptor;
        }
      }
      handlerMapping.setInterceptors(interceptors);
      return bean;
    }
  },

  /**
   * Add custom argument and return value resolvers.
   */
  REQUEST_MAPPING_HANDLER_ADAPTER {
    @Override
    public boolean matches(final Object bean) {
      return bean instanceof RequestMappingHandlerAdapter;
    }

    @Override
    public Object processAfterInitialization(final ApplicationContext context,
        final Object bean) {
      RequestMappingHandlerAdapter handlerMapping =
          (RequestMappingHandlerAdapter) bean;
      // New applications should consider setting it to true.
      handlerMapping.setIgnoreDefaultModelOnRedirect(true);

      // Add custom argument resolvers
      handlerMapping.setArgumentResolvers(argumentValueHandlers(context,
          handlerMapping));

      // Add custom return value handlers
      handlerMapping.setReturnValueHandlers(returnValueHandlers(context,
          handlerMapping));

      // Find Jackson2 Converter
      List<HttpMessageConverter<?>> messageConverters =
          handlerMapping.getMessageConverters();
      MappingJackson2HttpMessageConverter jacksonMessageConverter =
          find(messageConverters, MappingJackson2HttpMessageConverter.class);
      ObjectMapper jackson2ObjectMapper =
          context.getBean(WebDefaults.OBJECT_MAPPER, ObjectMapper.class);
      jacksonMessageConverter.setObjectMapper(jackson2ObjectMapper);
      return bean;
    }

    /**
     * Merge all the {@link HandlerMethodArgumentResolver} found in the
     * {@link RequestMappingHandlerAdapter} with all the
     * {@link HandlerMethodArgumentResolver} found in the
     * {@link ApplicationContext}. A {@link HandlerMethodArgumentResolver} might
     * implement {@link Ordered} interface.
     *
     * @param context The application context.
     * @param handlerMapping The request mapping handler.
     * @return A new set of {@link HandlerMethodArgumentResolver}.
     */
    private List<HandlerMethodArgumentResolver> argumentValueHandlers(
        final ApplicationContext context,
        final RequestMappingHandlerAdapter handlerMapping) {
      Set<HandlerMethodArgumentResolver> returnValueHandlerSet =
          new LinkedHashSet<HandlerMethodArgumentResolver>(
              handlerMapping.getArgumentResolvers().getResolvers());

      returnValueHandlerSet.addAll(Beans.lookFor(context,
          HandlerMethodArgumentResolver.class));

      List<HandlerMethodArgumentResolver> returnValueHandlerList =
          new ArrayList<HandlerMethodArgumentResolver>(returnValueHandlerSet);
      OrderComparator.sort(returnValueHandlerList);

      return returnValueHandlerList;
    }

    /**
     * Merge all the {@link HandlerMethodReturnValueHandler} found in the
     * {@link RequestMappingHandlerAdapter} with all the
     * {@link HandlerMethodReturnValueHandler} found in the
     * {@link ApplicationContext}. A {@link HandlerMethodReturnValueHandler}
     * might implement {@link Ordered} interface.
     *
     * @param context The application context.
     * @param handlerMapping The request mapping handler.
     * @return A new set of {@link HandlerMethodReturnValueHandler}.
     */
    private List<HandlerMethodReturnValueHandler> returnValueHandlers(
        final ApplicationContext context,
        final RequestMappingHandlerAdapter handlerMapping) {
      Set<HandlerMethodReturnValueHandler> returnValueHandlerSet =
          new LinkedHashSet<HandlerMethodReturnValueHandler>(
              handlerMapping.getReturnValueHandlers().getHandlers());

      returnValueHandlerSet.addAll(Beans.lookFor(context,
          HandlerMethodReturnValueHandler.class));

      List<HandlerMethodReturnValueHandler> returnValueHandlerList =
          new ArrayList<HandlerMethodReturnValueHandler>(returnValueHandlerSet);
      OrderComparator.sort(returnValueHandlerList);

      return returnValueHandlerList;
    }
  };

  /**
   * Find the given bean type on the collection.
   *
   * @param iterable The candidate collection.
   * @param beanType The bean type.
   * @param <T> The bean type.
   * @return The bean or null if not found.
   */
  @SuppressWarnings({"rawtypes", "unchecked" })
  protected <T> T find(final Iterable iterable, final Class<T> beanType) {
    for (Object bean : iterable) {
      if (beanType.isInstance(bean)) {
        return (T) bean;
      }
    }
    return null;
  }

  /**
   * True if the given post processor should be executed over the bean.
   *
   * @param bean The candidate bean.
   * @return True if the given post processor should be executed over the bean.
   */
  public abstract boolean matches(Object bean);

  /**
   * Apply this BeanPostProcessor to the given new bean instance <i>before</i>
   * any bean
   * initialization callbacks (like InitializingBean's
   * <code>afterPropertiesSet</code> or a custom init-method). The bean will
   * already be populated with property values.
   * The returned bean instance may be a wrapper around the original.
   *
   * @param context The application context.
   * @param bean the new bean instance
   * @return the bean instance to use, either the original or a wrapped one; if
   *         <code>null</code>, no subsequent BeanPostProcessors will be invoked
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
   */
  public Object processBeforeInitialization(
      final ApplicationContext context, final Object bean) {
    return bean;
  }

  /**
   * Apply this BeanPostProcessor to the given new bean instance <i>before</i>
   * any bean
   * initialization callbacks (like InitializingBean's
   * <code>afterPropertiesSet</code> or a custom init-method). The bean will
   * already be populated with property values.
   * The returned bean instance may be a wrapper around the original.
   *
   * @param context The application context.
   * @param bean the new bean instance
   * @return the bean instance to use, either the original or a wrapped one; if
   *         <code>null</code>, no subsequent BeanPostProcessors will be invoked
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
   */
  public Object processAfterInstantiation(final ApplicationContext context,
      final Object bean) {
    return bean;
  }

  /**
   * Apply this BeanPostProcessor to the given new bean instance <i>after</i>
   * any bean
   * initialization callbacks (like InitializingBean's
   * <code>afterPropertiesSet</code> or a custom init-method). The bean will
   * already be populated with property values.
   * The returned bean instance may be a wrapper around the original.
   * <p>
   * In case of a FactoryBean, this callback will be invoked for both the
   * FactoryBean instance and the objects created by the FactoryBean (as of
   * Spring 2.0). The post-processor can decide whether to apply to either the
   * FactoryBean or created objects or both through corresponding
   * <code>bean instanceof FactoryBean</code> checks.
   * <p>
   * This callback will also be invoked after a short-circuiting triggered by a
   * {@link InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation}
   * method, in contrast to all other BeanPostProcessor callbacks.
   *
   * @param context The application context.
   * @param bean the new bean instance
   * @return the bean instance to use, either the original or a wrapped one; if
   *         <code>null</code>, no subsequent BeanPostProcessors will be invoked
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
   * @see org.springframework.beans.factory.FactoryBean
   */
  public Object processAfterInitialization(final ApplicationContext context,
      final Object bean) {
    return bean;
  }

  /**
   * Get a {@link BeanPostProcessor} for the given bean.
   *
   * @param bean The candidate bean.
   * @return A {@link BeanPostProcessor} for the given bean.
   */
  public static BeanPostProcessors get(final Object bean) {
    Set<BeanPostProcessors> beanPostProcessors =
        EnumSet.allOf(BeanPostProcessors.class);
    beanPostProcessors.remove(DEFAULT);
    for (BeanPostProcessors beanPostProcessor : beanPostProcessors) {
      if (beanPostProcessor.matches(bean)) {
        return beanPostProcessor;
      }
    }
    return DEFAULT;
  }

}
