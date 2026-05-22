package com.techton.global;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class WeekRange {

    private WeekRange() {
    }

    public static LocalDateTime startOfWeek() {
        return LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
    }

    public static LocalDateTime endOfWeek() {
        return startOfWeek().plusDays(7);
    }
}
