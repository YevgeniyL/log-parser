package com.st.application.infrastructure;

import com.st.application.Parser;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Util for working with parsed data from logs.
 */
public class LogUtil {

    /**
     * Build statistics for average duration by request-resource.
     * @param logs - valid data from parsed string
     * @param maxResultLinesCount - setup limit to result records count. Null value will be as Integer.MAX_VALUE
     * @return sorted map. Key: resource name of grouped by  and summarized duration. Sorted from fresh request to oldest.
     */
    public static Map<String, Double> buildAvgDurationByResource(List<Parser.Data> logs, Integer maxResultLinesCount){
        maxResultLinesCount = maxResultLinesCount != null ? maxResultLinesCount : Integer.MAX_VALUE;
        return logs.stream().collect(Collectors.groupingBy(Parser.Data::getResourceName, Collectors.averagingLong(Parser.Data::getRequestDuration)))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(maxResultLinesCount)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o1, o2) -> o1, LinkedHashMap::new));
    }

    /**
     * Calculate sum of requests duration for every hour
     *
     * @param logs - valid data from parsed string
     * @return sorted map. Key: LocalDateTime grouped by hour. Value: summarized duration in this hour. Sorted from fresh time to oldest.
     */
    public static Map<LocalDateTime, Long> buildDurationSumByHour(List<Parser.Data> logs) {
        Map<LocalDateTime, List<Parser.Data>> groupByHour = new LinkedHashMap<>();
        LocalDateTime truncatedTime;

        for (Parser.Data log : logs) {
            truncatedTime = log.getRequestTime().plusHours(1).truncatedTo(ChronoUnit.HOURS);
            if (groupByHour.containsKey(truncatedTime)) {
                List<Parser.Data> logList = groupByHour.get(truncatedTime);
                logList.add(log);
            } else {
                groupByHour.put(truncatedTime, new ArrayList<>(Collections.singletonList(log)));
            }
        }

        Map<LocalDateTime, Long> sumDurationByHour = groupByHour.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        list -> list.getValue()
                                .stream()
                                .mapToLong(Parser.Data::getRequestDuration)
                                .sum()));

        return new TreeMap<>(sumDurationByHour);
    }
}
