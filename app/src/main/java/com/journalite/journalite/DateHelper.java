// Hecho por Gael
package com.journalite.journalite;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Locale;

// Date conversions shared by all screens. In the database dates are always saved as ISO text.
public final class DateHelper {

    private static final String EXIF_DATE_FORMAT = "yyyy:MM:dd HH:mm:ss";

    private DateHelper() {
    }

    // ISO text (yyyy-MM-ddTHH:mm:ss) -> readable date and time in the language of the phone
    public static String toDisplay(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "";
        try {
            LocalDateTime dateTime = LocalDateTime.parse(isoDate);
            return dateTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT));
        } catch (DateTimeParseException e) {
            return isoDate;
        }
    }

    // Milliseconds -> ISO text
    public static String millisToIso(long millis) {
        return new SimpleDateFormat(Constants.DATE_FORMAT_ISO, Locale.US).format(new Date(millis));
    }

    // Date text read by ExifInterface (yyyy:MM:dd HH:mm:ss) -> ISO text, or null if it cannot be read
    public static String exifToIso(String exifDate) {
        if (exifDate == null) return null;
        try {
            Date date = new SimpleDateFormat(EXIF_DATE_FORMAT, Locale.US).parse(exifDate.trim());
            return date == null ? null : millisToIso(date.getTime());
        } catch (ParseException e) {
            return null;
        }
    }
}
