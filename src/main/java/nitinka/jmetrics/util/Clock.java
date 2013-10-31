package nitinka.jmetrics.util;

import java.util.Date;

/**
 * User: NitinK.Agarwal@yahoo.com
 */
public class Clock {

    public static long nsTick() {
        return System.nanoTime();
    }

    public static long microTick() {
        return System.nanoTime() / 1000;
    }

    public static long secTick() {
        return System.currentTimeMillis()/1000;
    }

    public static long milliTick() {
        return System.currentTimeMillis();
    }

    public static void sleep(int ms) throws InterruptedException {
        Thread.sleep(ms);
    }

    public static void sleepSec(int secs) throws InterruptedException {
        sleep(secs * 1000);
    }
    public static void sleepMin(int mins) throws InterruptedException {
        sleep(mins * 60 * 1000);
    }

    public static Date dateFromNS(long oldNS) {
        long currentNS = nsTick();
        long nsPassed = currentNS - oldNS;
        long msPassed = nsPassed / MathConstant.MILLION;
        long oldMS = Clock.milliTick() - msPassed;
        return new Date(oldMS);
    }

    public static Date dateFromMS(long ms) {
        return new Date(ms);
    }

    public static long secondsFromEPOC(String time) {
        if(time == null)
            return Clock.secTick();

        time = time.trim();
        int future = 1;

        if(time.startsWith("-")) {
            time = time.substring(1).trim();
            future = -1;
        }

        String unit = "";
        if(time.endsWith("s") || time.endsWith("m") || time.endsWith("h") || time.endsWith("d")) {
            unit = time.substring(time.length()-1);
            time = time.substring(0, time.length()-1);
        }
        else {
            // Return the value as is
            long timeLong = Long.parseLong(time);
            return  timeLong;
        }

        long timeLong = Long.parseLong(time);
        if(unit.equalsIgnoreCase("S")) {
            return  Clock.secTick() + (future * timeLong);
        }

        if(unit.equalsIgnoreCase("m")) {
            return  Clock.secTick() + (future * timeLong * 60);
        }

        if(unit.equalsIgnoreCase("h")) {
            return  Clock.secTick() + (future * timeLong * 60 * 60);
        }

        if(unit.equalsIgnoreCase("d")) {
            return  Clock.secTick() + (future * timeLong * 24 * 60 * 60);
        }

        return Clock.secTick() + (future * timeLong);
    }
}
