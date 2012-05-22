package com.github.edgarespina.mwa;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.github.edgarespina.mwa.handler.MessageConverterHandlerExceptionResolver;
import com.github.edgarespina.mwa.view.ModelContribution;
import com.github.edgarespina.mwa.view.ModelContributionInterceptor;

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
      // Add model contributions.
      candidates.add(new ModelContributionInterceptor(Beans.lookFor(context,
          ModelContribution.class)));
      Object[] interceptors = new Object[candidates.size()];
      for (int i = 0; i < interceptors.length; i++) {
        HandlerInterceptor interceptor = candidates.get(i);
        RequestMapping mapping =
            AnnotationUtils.findAnnotation(interceptor.getClass(),
                RequestMapping.class);
        if (mapping != null && mapping.value().length > 0) {
          interceptors[i] =
              new MappedInterceptor(mapping.value(), interceptor);
        } else {
          interceptors[i] = interceptor;
        }
      }
      handlerMapping.setInterceptors(interceptors);
      return bean;
    }
  },

  /**
   * Add custom argument resolvers.
   */
  ADD_ARGUMENT_RESOLVERS_RETURN_VALUE_HANDLERS {
    @Override
    public boolean matches(final Object bean) {
      return bean instanceof RequestMappingHandlerAdapter;
    }

    @Override
    public Object processBeforeInitialization(
        final ApplicationContext context, final Object bean) {
      RequestMappingHandlerAdapter handlerMapping =
          (RequestMappingHandlerAdapter) bean;
      // Add custom argument resolvers
      Set<HandlerMethodArgumentResolver> argumentResolvers =
          new LinkedHashSet<HandlerMethodArgumentResolver>(
              handlerMapping.getCustomArgumentResolvers());
      argumentResolvers.addAll(Beans.lookFor(context,
          HandlerMethodArgumentResolver.class));
      handlerMapping.setCustomArgumentResolvers(
          new ArrayList<HandlerMethodArgumentResolver>(argumentResolvers));
      // Add custom return value handlers
      Set<HandlerMethodReturnValueHandler> returnValueHandlers =
          new LinkedHashSet<HandlerMethodReturnValueHandler>(
              handlerMapping.getCustomReturnValueHandlers());
      returnValueHandlers.addAll(Beans.lookFor(context,
          HandlerMethodReturnValueHandler.class));
      handlerMapping.setCustomReturnValueHandlers(
          new ArrayList<HandlerMethodReturnValueHandler>(returnValueHandlers));
      // Find Jackson Converter
      List<HttpMessageConverter<?>> messageConverters =
          handlerMapping.getMessageConverters();
      MappingJacksonHttpMessageConverter jacksonMessageConverter =
          find(messageConverters, MappingJacksonHttpMessageConverter.class);
      ObjectMapper defaultObjectMapper =
          context.getBean("defaultObjectMapper", ObjectMapper.class);
      jacksonMessageConverter.setObjectMapper(defaultObjectMapper);
      return bean;
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
