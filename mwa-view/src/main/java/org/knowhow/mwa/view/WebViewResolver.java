package org.knowhow.mwa.view;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

/**
 * A {@link ViewResolver} for {@link WebView}.
 *
 * @author edgar.espina
 * @since 0.1
 */
public abstract class WebViewResolver extends AbstractTemplateViewResolver
    implements InitializingBean {

  /**
   * The contribution registry.
   */
  private ModelContribution[] contributions;

  /**
   * The {@link WebViewResolver} constructor.
   *
   * @param contributions The model contributions. Cannot be null.
   */
  public WebViewResolver(final ModelContribution... contributions) {
    Assert.notNull(contributions, "The contributions cannot be null.");
    this.contributions = contributions;
  }

  /**
   * Build the view with modern web support. {@inheritDoc}
   */
  @Override
  protected final AbstractUrlBasedView buildView(final String viewName)
      throws Exception {
    WebView view = (WebView) super.buildView(viewName);
    view.setContributions(contributions);
    buildView(view);
    return view;
  }

  /**
   * Fire {@link ModelContribution#init(javax.servlet.ServletContext)
   * events} and propagate the use cache option. {@inheritDoc}
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    for (ModelContribution contribution : contributions) {
      contribution.setUseCache(isCache());
      contribution.init(getServletContext());
    }
  }

  /**
   * Build the view.
   *
   * @param view The view. It's never null.
   * @throws Exception If something goes wrong.
   */
  protected abstract void buildView(WebView view) throws Exception;

  /**
   * Set the view class that should be used to create views.
   *
   * @param viewClass class that is assignable to the required view class (by
   *        default, AbstractUrlBasedView)
   * @see AbstractUrlBasedView
   */
  @Override
  public void setViewClass(
      @SuppressWarnings("rawtypes") final Class viewClass) {
    Assert.notNull(viewClass, "A mustache view class is required.");
    Assert.isTrue(WebView.class.isAssignableFrom(viewClass),
        "The view class must be a: " + WebView.class);
    super.setViewClass(viewClass);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected abstract Class<?> requiredViewClass();
}
