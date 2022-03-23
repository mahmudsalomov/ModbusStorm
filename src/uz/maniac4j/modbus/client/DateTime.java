package uz.maniac4j.modbus.client;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateTime {
    public DateTime() {
    }

    protected static long getDateTimeTicks() {
        long TICKS_AT_EPOCH = 621355968000000000L;
        return System.currentTimeMillis() * 10000L + TICKS_AT_EPOCH;
    }

    protected static String getDateTimeString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }
}
