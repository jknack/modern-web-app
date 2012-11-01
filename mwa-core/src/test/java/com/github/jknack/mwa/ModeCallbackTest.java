package com.github.jknack.mwa;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ModeCallbackTest {

  @Test
  public void onDev() {
    String result = Mode.valueOf("dev").execute(new ModeCallback<String>() {
      @Override
      public String on(final Mode mode) {
        return null;
      }

      @Override
      public String onDev() {
        return "dev";
      }
    });
    assertEquals("dev", result);
  }

  @Test
  public void onDemo() {
    assertEquals("demo",
        Mode.valueOf("demo").execute(new ModeCallback<String>() {
          @Override
          public String on(final Mode mode) {
            return null;
          }

          @SuppressWarnings("unused")
          public String onDemo() {
            return "demo";
          }

          @Override
          public String onDev() {
            return null;
          }
        }));
  }

  @Test
  public void onFallback() {
    assertEquals("fallback",
        Mode.valueOf("fallback").execute(new ModeCallback<String>() {
          @Override
          public String on(final Mode mode) {
            return mode.name();
          }

          @Override
          public String onDev() {
            return null;
          }
        }));
  }

}
