package eu.menzerath.bpchat.chat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Helper {
    private static final String SERVER_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String CLIENT_DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";

    /**
     * Konvertiert ein Date-Objekt in eine lesbare Zeitangabe (als String)
     *
     * @param timestamp zu konvertierndes Date-Objekt
     * @return Zeitangabe (String)
     */
    public static String formatDateToString(Date timestamp) {
        return new SimpleDateFormat(CLIENT_DATE_FORMAT, Locale.getDefault()).format(timestamp);
    }

    /**
     * Konvertiert den vom Server übergebenen Timestamp (String) in ein Date-Objekt
     *
     * @param timestamp zu konvertiernder Timestamp
     * @return Zeitangabe (Date-Objekt)
     */
    public static Date serverTimestampToDate(String timestamp) {
        try {
            return new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.GERMANY).parse(timestamp);
        } catch (ParseException e) {
            return new Date();
        }
    }
}