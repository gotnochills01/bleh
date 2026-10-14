package com.example.mahila.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CycleCalculator {

    public static class CycleInfo {
        public int cycleDay;
        public String currentPhase;
        public String nextPeriodDate;
        public String ovulationWindow;
        public boolean isEstimated;
        public boolean isIrregular;
        public double averageCycleLength;
        public String statusMessage;

        public CycleInfo(int cycleDay, String currentPhase, String nextPeriodDate, String ovulationWindow, boolean isEstimated, boolean isIrregular, double averageCycleLength, String statusMessage) {
            this.cycleDay = cycleDay;
            this.currentPhase = currentPhase;
            this.nextPeriodDate = nextPeriodDate;
            this.ovulationWindow = ovulationWindow;
            this.isEstimated = isEstimated;
            this.isIrregular = isIrregular;
            this.averageCycleLength = averageCycleLength;
            this.statusMessage = statusMessage;
        }
    }

    public static CycleInfo calculateCycleInfo(long lastPeriodMillis, int defaultCycleLength) {
        return calculateCycleInfoFromHistory(lastPeriodMillis <= 0 ? null : java.util.Collections.singletonList(lastPeriodMillis), defaultCycleLength);
    }

    public static CycleInfo calculateCycleInfoFromHistory(List<Long> periodHistoryMillis, int fallbackCycleLength) {
        if (periodHistoryMillis == null || periodHistoryMillis.isEmpty() || periodHistoryMillis.get(0) <= 0) {
            return new CycleInfo(
                    0,
                    "No History Recorded",
                    "Not Available",
                    "Not Available",
                    true,
                    false,
                    28.0,
                    "No period recorded yet. Log your first period to unlock cycle predictions."
            );
        }

        long latestMillis = periodHistoryMillis.get(0);
        long nowMillis = System.currentTimeMillis();

        long diffDays = (nowMillis - latestMillis) / (24L * 60 * 60 * 1000);
        if (diffDays < 0) diffDays = 0;

        double avgLength = fallbackCycleLength <= 0 ? 28.0 : fallbackCycleLength;
        boolean isIrregular = false;

        if (periodHistoryMillis.size() >= 2) {
            long totalGaps = 0;
            int gapCount = 0;
            long maxVariance = 0;

            for (int i = 0; i < periodHistoryMillis.size() - 1; i++) {
                long prev = periodHistoryMillis.get(i);
                long next = periodHistoryMillis.get(i + 1);
                long gap = (prev - next) / (24L * 60 * 60 * 1000);
                if (gap >= 15 && gap <= 60) {
                    totalGaps += gap;
                    gapCount++;
                }
            }

            if (gapCount > 0) {
                avgLength = (double) totalGaps / gapCount;
                for (int i = 0; i < periodHistoryMillis.size() - 1; i++) {
                    long gap = (periodHistoryMillis.get(i) - periodHistoryMillis.get(i + 1)) / (24L * 60 * 60 * 1000);
                    if (gap >= 15 && gap <= 60) {
                        long variance = Math.abs(gap - (long) avgLength);
                        if (variance > maxVariance) maxVariance = variance;
                    }
                }
                if (maxVariance > 4) {
                    isIrregular = true;
                }
            }
        }

        int cycleDay = (int) (diffDays % (long) avgLength) + 1;

        String phase;
        if (cycleDay <= 5) {
            phase = "Menstrual Phase";
        } else if (cycleDay <= 11) {
            phase = "Follicular Phase";
        } else if (cycleDay <= 16) {
            phase = "Ovulatory Phase";
        } else {
            phase = "Luteal Phase";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        Calendar calNext = Calendar.getInstance();
        calNext.setTimeInMillis(latestMillis);
        calNext.add(Calendar.DAY_OF_YEAR, (int) Math.round(avgLength));
        String nextPeriodStr = sdf.format(calNext.getTime());

        Calendar calOvStart = Calendar.getInstance();
        calOvStart.setTimeInMillis(latestMillis);
        calOvStart.add(Calendar.DAY_OF_YEAR, (int) Math.round(avgLength) - 16);

        Calendar calOvEnd = Calendar.getInstance();
        calOvEnd.setTimeInMillis(latestMillis);
        calOvEnd.add(Calendar.DAY_OF_YEAR, (int) Math.round(avgLength) - 10);

        SimpleDateFormat sdfShort = new SimpleDateFormat("MMM dd", Locale.getDefault());
        String ovulationWindowStr = sdfShort.format(calOvStart.getTime()) + " – " + sdfShort.format(calOvEnd.getTime());

        String statusMsg = isIrregular ? "Irregular cycle detected (High variance). Predictions labeled as estimates." :
                (periodHistoryMillis.size() < 2 ? "Calculated from 1 period entry. Log more cycles to improve accuracy." : "Regular cycle prediction active.");

        return new CycleInfo(
                cycleDay,
                phase,
                nextPeriodStr,
                ovulationWindowStr,
                periodHistoryMillis.size() < 2,
                isIrregular,
                avgLength,
                statusMsg
        );
    }
}
