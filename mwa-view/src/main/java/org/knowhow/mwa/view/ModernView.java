package org.knowhow.mwa.view;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;
import org.springframework.web.servlet.view.AbstractTemplateView;

/**
 * A view with extended capabilities. It allow to add model contributions before
 * merging the view within the model.
 *
 * @author edgar.espina
 * @since 0.1
 */
public abstract class ModernView extends AbstractTemplateView {

  /**
   * The contribution registry.
   */
  private ModelContribution[] contributions = new ModelContribution[0];

  /**
   * Creates a new {@link ModernView}. Default constructor is required, Spring
   * creates this class using reflection.
   */
  public ModernView() {
  }

  /**
   * Fire {@link ModelContribution#contribute(String, Map)} events. It will
   * fails if a name clash is found. {@inheritDoc}
   */
  @Override
  protected final Map<String, Object> createMergedOutputModel(
      final Map<String, ?> model, final HttpServletRequest request,
      final HttpServletResponse response) {
    final Map<String, Object> mergedModel =
        super.createMergedOutputModel(model, request, response);
    // Don't allow conflict.
    // if an attribute was already set an exception will be raised.
    Map<String, Object> safeModel = new AbstractMap<String, Object>() {
      @Override
      public Object get(final Object key) {
        return mergedModel.get(key);
      }

      @Override
      public Object put(final String key, final Object value) {
        Object oldValue = mergedModel.put(key, value);
        if (oldValue != null) {
          throw new IllegalArgumentException(
              "Cannot expose attribute '" + key
                  + "' because of an existing model object of the same name");
        }
        return oldValue;
      }

      @Override
      public Set<Entry<String, Object>> entrySet() {
        return mergedModel.entrySet();
      }
    };
    // Save commons variables
    safeModel.put(ModelContribution.VIEW, getBeanName());
    safeModel.put(ModelContribution.CONTEXT_PATH, request.getContextPath());
    try {
      for (ModelContribution contribution : contributions) {
        contribution.contribute(getBeanName(), safeModel);
      }
    } catch (IOException ex) {
      // don't go further
      throw new IllegalStateException("A model contribution has fail.", ex);
    }
    return mergedModel;
  }

  /**
   * Set model contributions.
   *
   * @param contributions The model contributions. Required.
   */
  public void setContributions(final ModelContribution... contributions) {
    Assert.notNull(contributions, "The model contributions are required.");
    this.contributions = contributions;
  }
}
