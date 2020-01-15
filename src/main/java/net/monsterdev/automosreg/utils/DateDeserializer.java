package net.monsterdev.automosreg.utils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateDeserializer extends StdDeserializer<Date> implements ContextualDeserializer {

  private static final SimpleDateFormat commonFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
  private DateFormat customFormat;
  private String formatString;

  public DateDeserializer() {
    this(null);
  }

  public DateDeserializer(Class<?> vc) {
    super(vc);
  }

  public DateDeserializer(DateDeserializer base, DateFormat df, String formatString) {
    this(base._valueClass);
    this.customFormat = df;
    this.formatString = formatString;
  }

  private DateDeserializer withDateFormat(DateFormat df, String formatString) {
    return new DateDeserializer(this, df, formatString);
  }

  @Override
  public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
      throws JsonMappingException {
    final JsonFormat.Value format = findFormatOverrides(ctxt, property,
        handledType());

    if (format != null) {
      TimeZone tz = format.getTimeZone();
      final Boolean lenient = format.getLenient();

      // First: fully custom pattern?
      if (format.hasPattern()) {
        final String pattern = format.getPattern();
        final Locale loc = format.hasLocale() ? format.getLocale() : ctxt.getLocale();
        SimpleDateFormat df = new SimpleDateFormat(pattern, loc);
        if (tz == null) {
          tz = ctxt.getTimeZone();
        }
        df.setTimeZone(tz);
        if (lenient != null) {
          df.setLenient(lenient);
        }
        return withDateFormat(df, pattern);
      }
      // But if not, can still override timezone
      if (tz != null) {
        DateFormat df = ctxt.getConfig().getDateFormat();
        // one shortcut: with our custom format, can simplify handling a bit
        if (df.getClass() == StdDateFormat.class) {
          final Locale loc = format.hasLocale() ? format.getLocale() : ctxt.getLocale();
          StdDateFormat std = (StdDateFormat) df;
          std = std.withTimeZone(tz);
          std = std.withLocale(loc);
          if (lenient != null) {
            std = std.withLenient(lenient);
          }
          df = std;
        } else {
          // otherwise need to clone, re-set timezone:
          df = (DateFormat) df.clone();
          df.setTimeZone(tz);
          if (lenient != null) {
            df.setLenient(lenient);
          }
        }
        return withDateFormat(df, formatString);
      }
      // or maybe even just leniency?
      if (lenient != null) {
        DateFormat df = ctxt.getConfig().getDateFormat();
        String pattern = formatString;
        // one shortcut: with our custom format, can simplify handling a bit
        if (df.getClass() == StdDateFormat.class) {
          StdDateFormat std = (StdDateFormat) df;
          std = std.withLenient(lenient);
          df = std;
          pattern = std.toPattern();
        } else {
          // otherwise need to clone,
          df = (DateFormat) df.clone();
          df.setLenient(lenient);
          if (df instanceof SimpleDateFormat) {
            ((SimpleDateFormat) df).toPattern();
          }
        }
        if (pattern == null) {
          pattern = "[unknown]";
        }
        return withDateFormat(df, pattern);
      }
    }
    return this;
  }

  @Override
  public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String dateString = p.getText();
    if (dateString.isEmpty()) {
      //handle empty strings however you want,
      //but I am setting the Date objects null
      return null;
    }
    // удаляем милисекунды из строки
    int millisStartPos = dateString.lastIndexOf('.');
    int millisEndPos = dateString.lastIndexOf('Z');
    dateString = dateString.substring(0, millisStartPos >= 0 ? millisStartPos : millisEndPos).concat("Z");
    try {
      return customFormat != null ? customFormat.parse(dateString) : commonFormat.parse(dateString);
    } catch (ParseException e) {
      throw new IOException(String.format("Failed to parse date %s", dateString));
    }
  }

}
