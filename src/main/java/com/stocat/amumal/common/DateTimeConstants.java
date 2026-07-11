package com.stocat.amumal.common;

import java.time.format.DateTimeFormatter;

public final class DateTimeConstants {

  public static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private DateTimeConstants() {}
}
