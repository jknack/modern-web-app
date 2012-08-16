package com.github.jknack.mwa.wro4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A lessCss error.
 *
 * @author edgar.espina
 * @since 0.1.2
 */
final class LessCssError {

  /**
   * The logging system.
   */
  private static final Logger logger = LoggerFactory
      .getLogger(LessCssError.class);

  /**
   * The type of error.
   */
  private String type = "Syntax";

  /**
   * The error's message.
   */
  private String message;

  /**
   * The error file's name.
   */
  private String filename;

  /**
   * The error's line.
   */
  private int line = 0;

  /**
   * The error's column.
   */
  private int column = 0;

  /**
   * The lines with the problem.
   */
  private List<String> extract = new ArrayList<String>();

  /**
   * Creates a new {@link LessCssError}.
   */
  private LessCssError() {
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @return the line
   */
  public int getLine() {
    return line;
  }

  /**
   * @return the column
   */
  public int getColumn() {
    return column;
  }

  /**
   * @return the lines
   */
  public List<String> getExtract() {
    return extract;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this,
        ToStringStyle.MULTI_LINE_STYLE);
  }

  /**
   * Creates a {@link LessCssError} from the given exception.
   *
   * @param filename The lessCss file's name.
   * @param ex The lessCss exception.
   * @return A {@link LessCssError} from the given exception.
   */
  @SuppressWarnings("unchecked")
  public static LessCssError of(final String filename, final Throwable ex) {
    LessCssError error = new LessCssError();
    if (ex instanceof JavaScriptException) {
      Scriptable value = (Scriptable) ((JavaScriptException) ex).getValue();
      if (value != null) {
        error.column = ((Number) getProperty(value, "column", 0)).intValue();
        error.filename = (String) getProperty(value, "filenam", filename);
        error.line = ((Number) getProperty(value, "line", 0)).intValue();
        error.setExtract((List<String>) getProperty(value, "extract",
            Collections.emptyList()));
        error.message = (String) getProperty(value, "message", ex.getMessage());
        error.type = (String) getProperty(value, "type", "Syntax");
      }
    } else if (ex instanceof EcmaError) {
      EcmaError ecmaError = (EcmaError) ex;
      error.column = ecmaError.columnNumber();
      error.filename = ecmaError.sourceName();
      error.line = ecmaError.lineNumber();
      error.message = ecmaError.details();
    } else {
      logger.trace("Unknown exception", ex);
    }
    // set defaults
    if (error.filename == null) {
      error.filename = filename;
    }
    if (error.message == null) {
      error.message = ex.getMessage();
    }
    return error;
  }

  /**
   * Set the extracted lines.
   *
   * @param extract The line with the problem.
   */
  private void setExtract(final List<String> extract) {
    if (extract != null) {
      for (int i = 0; i < extract.size(); i++) {
        String line = extract.get(i);
        this.extract.add(line == null ? "" : line);
      }
    }
  }

  /**
   * Try to read a property from a jsObject.
   *
   * @param jsObject The script object.
   * @param property The property's name.
   * @param defaultValue The default's value.
   * @return The property's value.
   */
  private static Object getProperty(final Scriptable jsObject,
      final String property, final Object defaultValue) {
    try {
      if (ScriptableObject.hasProperty(jsObject, property)) {
        Object value = ScriptableObject.getProperty(jsObject, property);
        if (value instanceof String) {
          return value;
        } else if (value instanceof Number) {
          return value;
        } else if (value instanceof Boolean) {
          return value;
        } else if (value instanceof NativeArray) {
          return Arrays.asList(((NativeArray) value).toArray());
        }
      }
    } catch (Exception ex) {
      logger.error("Unable to read property: " + property, ex);
    }
    return defaultValue;
  }
}
