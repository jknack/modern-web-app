# The MVC Module
The MVC module add one special features: "model contributions"

A model contribution add extra data or information to any Spring view. This let you keep your view methods simpler, shorter and cleaner.

## Enabling the MvcModule
You can enable the ```MvcModule``` in your ```startup``` class.

```java
...
import com.github.jknack.mwa.mvc.MvcModule;

public class MyApp extends Startup {
  ...
  @Override
  public Class[] imports() {
    return new Class[] {MvcModule.class};
  }
  ...
}
```

## A custom model contribution
Let's say you need to access to the currently logged-on user from potencially all your views.
So, instead of retrieving and then adding the current logged-on user in all the view methods you can use a model contribution:

```java

import com.github.jknack.mwa.mvc.ModelContribution;
import com.github.jknack.mwa.mvc.AbstractModelContribution;

@Configuration
public class SecurityModule {

  @Bean
  public ModelContribution loggedOnUser() {
    return new AbstractModelContribution() {
      public void contribute(HttpServletRequest request, HttpServletResponse response, ModelAndView modelAndView) throws IOException {
        modelAndView.addObject("user", currentUser);
      }
    }
  }
} 
```

Later, you can access to the currently logged-on user using the model attribute: ```user```.

## The GoogleAnalytics contribution
The mvc module has a google analytics contribution that publish the GA javascript code.

### Using the GA contribution
You need to provide a ```ga.trackingCode``` property:

```properties
ga.trackingCode=...
```

If you don't set a tracking code the GA contribution don't add anything to the model.

Finally, you need to publish the contribution in the Spring application context.

```java
import com.github.jknack.mwa.mvc.GoogleAnalyticsContribution;
...
  @Bean
  public GoogleAnalyticsContribution gaContrib(Environment env) {
    return new GoogleAnalyticsContribution(env);
  }
...
```