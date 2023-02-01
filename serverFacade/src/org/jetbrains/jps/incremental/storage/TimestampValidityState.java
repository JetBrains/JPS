// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.incremental.storage;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A simple implementation of ValidityState that is enough for most cases.
 * The file is considered modified if its timestamp is changed.
 */
public final class TimestampValidityState implements ValidityState {
  private final long myTimestamp;

  /**
   * Loads the validity state from the specified stream.
   *
   * @param is the stream to load the validity state from.
   * @throws IOException if the stream read fails.
   */
  public TimestampValidityState(DataInput is) throws IOException{
    myTimestamp = is.readLong();
  }

  /**
   * Creates a validity state with the specified timestamp.
   *
   * @param timestamp the timestamp for the validity state.
   */
  public TimestampValidityState(long timestamp) {
    myTimestamp = timestamp;
  }

  public long getTimestamp() {
    return myTimestamp;
  }

  public boolean equalsTo(ValidityState otherState) {
    return otherState instanceof TimestampValidityState && myTimestamp == ((TimestampValidityState)otherState).myTimestamp;
  }

  /**
   * Saves the validity state to the specified stream.
   *
   * @param out
   * @throws java.io.IOException if the stream write fails.
   */
  public void save(DataOutput out) throws IOException {
    out.writeLong(myTimestamp);
  }
}
