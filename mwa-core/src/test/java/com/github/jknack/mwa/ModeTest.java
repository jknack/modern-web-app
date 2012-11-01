package com.github.jknack.mwa;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ModeTest {

  @Test
  public void dev() {
    assertTrue(Mode.valueOf("dev").isDev());
    assertTrue(Mode.valueOf("Dev").isDev());
    assertTrue(Mode.valueOf("DEV").isDev());
  }

  @Test
  public void noDev() {
    assertTrue(!Mode.valueOf("deve").isDev());
    assertTrue(!Mode.valueOf("prod").isDev());
    assertTrue(!Mode.valueOf("development").isDev());
  }
}
