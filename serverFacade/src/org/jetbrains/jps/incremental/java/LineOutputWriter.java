// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.incremental.java;

import java.io.IOException;
import java.io.Writer;

/**
* @author Eugene Zhuravlev
*         Date: 9/24/11
*/
abstract class LineOutputWriter extends Writer {
  private final LineParser myLineParser = new LineParser();

  public void write(int c) throws IOException {
    processData(new CharSequenceIterator(c));
  }

  public void write(char[] cbuf) throws IOException {
    processData(new CharSequenceIterator(cbuf));
  }

  public void write(String str) throws IOException {
    processData(new CharSequenceIterator(str));
  }

  public void write(String str, int off, int len) throws IOException {
    processData(new CharSequenceIterator(str.subSequence(off, off + len)));
  }

  public Writer append(CharSequence csq) throws IOException {
    processData(new CharSequenceIterator(csq));
    return this;
  }

  public Writer append(CharSequence csq, int start, int end) throws IOException {
    processData(new CharSequenceIterator(csq.subSequence(start, end)));
    return this;
  }

  public Writer append(char c) throws IOException {
    processData(new CharSequenceIterator(c));
    return this;
  }

  public void write(char[] cbuf, int off, int len) throws IOException {
    processData(new CharSequenceIterator(cbuf, off, len));
  }

  private void processData(CharIterator data) {
    while (myLineParser.parse(data)) {
      final String line = myLineParser.getResult();
      myLineParser.reset();
      lineAvailable(line);
    }
  }


  public void flush() throws IOException {
  }

  public void close() throws IOException {
    try {
      if (myLineParser.hasData()) {
        lineAvailable(myLineParser.getResult());
      }
    }
    finally {
      myLineParser.reset();
    }
  }

  protected abstract void lineAvailable(String line);

  private static interface CharIterator {
    char nextChar();
    boolean hasData();
  }

  private static class LineParser {
    private final StringBuilder myData = new StringBuilder();
    private boolean myFoundCR = false;

    public boolean parse(CharIterator it) {
      while (it.hasData()) {
        final char ch = it.nextChar();
        if (ch == '\r') {
          myFoundCR = true;
        }
        else if (ch == '\n') {
          myFoundCR = false;
          return true;
        }
        else {
          if (myFoundCR) {
            myData.append('\r');
            myFoundCR = false;
          }
          myData.append(ch);
        }
      }
      return false;
    }

    public boolean hasData() {
      return myData.length() > 0;
    }

    public String getResult() {
      return myData.toString();
    }

    public void reset() {
      myFoundCR = false;
      myData.setLength(0);
    }
  }

  private static class CharSequenceIterator implements CharIterator {
    private final CharSequence myChars;
    private int myCursor = 0;

    CharSequenceIterator(final int ch) {
      this((char)ch);
    }

    CharSequenceIterator(final char ch) {
      this(new SingleCharSequence(ch));
    }

    CharSequenceIterator(char[] chars) {
      this(chars, 0, chars.length);
    }

    CharSequenceIterator(final char[] chars, final int offset, final int length) {
      this(new ArrayCharSequence(chars, offset, length));
    }

    CharSequenceIterator(CharSequence sequence) {
      myChars = sequence;
    }

    public char nextChar() {
      return myChars.charAt(myCursor++);
    }

    public boolean hasData() {
      return myCursor < myChars.length();
    }
  }

  private static class SingleCharSequence implements CharSequence {
    private final char myCh;

    public SingleCharSequence(char ch) {
      myCh = ch;
    }

    public int length() {
      return 1;
    }

    public char charAt(int index) {
      if (index != 0) {
        throw new IndexOutOfBoundsException("Index out of bounds: " + index);
      }
      return myCh;
    }

    public CharSequence subSequence(int start, int end) {
      throw new RuntimeException("Method subSequence not implemented");
    }
  }

  private static class ArrayCharSequence implements CharSequence {
    private final char[] myChars;
    private final int myOffset;
    private final int myLength;

    public ArrayCharSequence(char[] chars, int offset, int length) {
      myChars = chars;
      myOffset = offset;
      myLength = length;
    }

    public int length() {
      return myLength;
    }

    public char charAt(int index) {
      return myChars[index];
    }

    public CharSequence subSequence(int start, int end) {
      return new ArrayCharSequence(myChars, start, end - start);
    }

    public String toString() {
      return new String(myChars, myOffset, myLength);
    }
  }
}
