/*
 * Copyright © 2015, Oracle and/or its affiliates.  All rights reserved. 
 *
 * This software is dual-licensed to you under the MIT License (MIT) and the
 * Universal Permissive License (UPL).  See the LICENSE file in the root directory
 * for license terms.  You may choose either license, or both.
 */
package com.oracle.jmee.samples.webserver.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utility class which helps to work with dates of format used in HTTP protocol.
 * Date formats, with which the HTTP protocol operates, are defined in the <a
 * href="https://tools.ietf.org/html/rfc7231#section-7.1.1.1">Date/Time Formats
 * section of RFC 7231</a>. This class allows to convert date to the string of
 * the needed format and vice versa. Also there is a method to get current time
 * using GMT time zone. The obsolete date formats mentioned in the RFC are not
 * supported by this class.
 */
public final class DateUtils {

    // Timezone which corresponds to the Greenwich Mean Time
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    // Exception message
    private static final String WRONG_DATE_FORMAT_EXCEPTION_MESSAGE = "Wrong date format. Only such formats are supported: \"EEE, dd MMM yyyy HH:mm:ss GMT\" and \"EEE, dd-MMM-yyyy HH:mm:ss GMT\".";

    // Strings which identify months
    private static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    // Utility class should be noninstantiable
    private DateUtils() {
        // throw new UnsupportedOperationException("Constructor should not be called");
        System.out.println("DateUtils()->Constructor should not be called");
    }

    /**
     * Converts the date to the string. It is assumed that date is in GMT time
     * zone. An example of the output string: "Sun, 06 Nov 1994 08:49:37 GMT".
     *
     * @param date date in GMT time zone to convert to string
     * @return the string which corresponds to this date of null if date is null
     */
    public static String httpDateToString(Date date) {
        return dateToRFC5322Date(date);
    }

    /**
     * Parses the string to the date. String must be formatted according the
     * preferred form of <a
     * href="https://tools.ietf.org/html/rfc7231#section-7.1.1.1">Date/Time
     * Formats section of RFC 7231</a>, e.g. "Sun, 06 Nov 1994 08:49:37 GMT".
     *
     * @param date date to convert to string
     * @return the resulting date or null if date is null
     * @throws IllegalArgumentException if the specified date string is
     * malformed
     */
    public static Date stringToHttpDate(String date) {
        return parseDate(date);
    }

    /**
     * Returns the current date in the GMT time zone.
     *
     * @return the current date in the GMT time zone
     */
    public static Date getCurrentGmtDateTime() {
        // System.out.println("getCurrentGmtDateTime()");
        return Calendar.getInstance(GMT).getTime();
    }

    private static Date parseDate(String date) {
        if (date == null) {
            return null;
        }
        // According to the https://tools.ietf.org/html/rfc7231#section-7.1.1.1
        // String must have the following format: Sun, 06 Nov 1994 08:49:37 GMT
        date = date.trim();
        if (date.length() != "Sun, 06 Nov 1994 08:49:37 GMT".length()) {
            throw new IllegalArgumentException(WRONG_DATE_FORMAT_EXCEPTION_MESSAGE + " The specified date is: " + date);
        }
        try {
            // Extracting parts of the date
            int day = Integer.parseInt(date.substring(5, 7));
            int month = parseMonth(date.substring(8, 11));
            int year = Integer.parseInt(date.substring(12, 16));

            int hour = Integer.parseInt(date.substring(17, 19));
            int minute = Integer.parseInt(date.substring(20, 22));
            int second = Integer.parseInt(date.substring(23, 25));

            Calendar calendar = Calendar.getInstance(GMT);

            // Enabling calendar leniency so that dates are correct
            // calendar.setLenient(false);            
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, second);

            return calendar.getTime();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(WRONG_DATE_FORMAT_EXCEPTION_MESSAGE + " The specified date is: " + date);
        }
    }

    private static int parseMonth(String parsedMonth) {
        // Getting an index of the month
        for (int monthIndex = 0; monthIndex < MONTHS.length; monthIndex++) {
            if (MONTHS[monthIndex].equals(parsedMonth)) {
                return monthIndex;
            }
        }
        throw new IllegalArgumentException("Illegal month: " + parsedMonth);
    }

    private static String dateToRFC5322Date(Date date) {
        if (date == null) {
            return null;
        }
        // According to the https://tools.ietf.org/html/rfc7231#section-7.1.1.1
        // date should be formatted like this: Sun, 06 Nov 1994 08:49:37 GMT
        
        // return String.format("%1$ta, %1$td %1$tb %1$tY %1$tH:%1$tM:%1$tS GMT", date);
        return date.toString();
    }

}
