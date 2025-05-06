package com.example.weatherforecast.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Clase de utilidad para operaciones relacionadas con fechas y tiempos
 * para procesamiento de datos meteorológicos.
 */
public class TimeUtils {
    private static final SimpleDateFormat INPUT_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private static final SimpleDateFormat OUTPUT_HOUR_FORMAT =
            new SimpleDateFormat("HH:00", Locale.getDefault());
    private static final SimpleDateFormat DATE_ONLY_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final String[] DAY_ABBREVIATIONS = {"Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"};

    /**
     * Formatea una fecha para mostrar solo la hora en formato HH:00
     */
    public static String formatHourOnly(Date date) {
        return OUTPUT_HOUR_FORMAT.format(date);
    }

    /**
     * Extrae solo la fecha de un texto de fecha/hora
     */
    public static String extractDateOnly(String dateTimeText) {
        return dateTimeText.split(" ")[0];
    }

    /**
     * Obtiene la abreviatura del día de la semana para una fecha dada
     */
    public static String getDayAbbreviation(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        return DAY_ABBREVIATIONS[dayOfWeek];
    }

    /**
     * Parsea una fecha en formato texto usando el formato estándar de la aplicación
     */
    public static Date parseDateTime(String dateTimeText) {
        try {
            return INPUT_DATE_FORMAT.parse(dateTimeText);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Formatea una fecha para mostrar solo la parte de fecha
     */
    public static String formatDateOnly(Date date) {
        return DATE_ONLY_FORMAT.format(date);
    }

    /**
     * Obtiene un calendario para una fecha específica
     */
    public static Calendar getCalendarForDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    /**
     * Calcula la diferencia en milisegundos entre dos fechas
     */
    public static long getTimeDifference(Date date1, Date date2) {
        return date2.getTime() - date1.getTime();
    }
}
