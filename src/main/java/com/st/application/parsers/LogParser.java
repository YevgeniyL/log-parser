package com.st.application.parsers;


import com.st.application.Parser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Log parser collect data from catched strings to Data class.
 * Catch URI with not empty: (LocalDateTime) [user context], 'resource name' or 'action' URI-parameter, and 'duration' as digital in the end of string.
 */
public class LogParser implements Parser {
    private final Pattern resourceLinePattern = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3}) \\(.*\\) \\[.*\\] ([a-z][\\w]*) .*in ([\\d]+)$");
    private final Pattern uriLinePattern = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3}) \\(.*\\) \\[.+\\] \\/[\\S*]*action=(\\w+)[\\S]* in ([\\d]+)$");
    private final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");

    /**
     * Take dirty strings and collect validated by patterns to Data.
     * Resource pattern: ^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2},\d{3}) \(.*\) \[.*\] ([a-z][\w]*) .*in ([\d]+)$
     * URI pattern: ^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2},\d{3}) \(.*\) \[.+\] \/[\S*]*action=(\w+)[\S]* in ([\d]+)$
     * Request with parameter 'action' like a REST-POST. Catch it.
     *
     * @param logLineList - incoming dirty string list
     * @return result list with filled Data
     */
    public List<Data> parse(List<String> logLineList) {
        if (logLineList == null || logLineList.size() == 0) {
            return Collections.emptyList();
        }

        List<Data> logs = new ArrayList<>();
        for (String line : logLineList) {
            if (line == null) continue;
            if (!addToLogs(resourceLinePattern.matcher(line), logs)) {
                addToLogs(uriLinePattern.matcher(line), logs);
            }
        }

        return logs;
    }

    private boolean addToLogs(Matcher matcher, List<Data> logs) {
        if (matcher.find()) {
            String dateTime = matcher.group(1);
            String resource = matcher.group(2);
            String duration = matcher.group(3);
            if (dateTime == null || dateTime.isEmpty()) return false;
            if (resource == null || resource.isEmpty()) return false;
            if (duration == null || duration.isEmpty()) return false;
            logs.add(new Data(LocalDateTime.parse(dateTime, dateTimeFormat), resource, Integer.valueOf(duration)));
            return true;
        }

        return false;
    }
}
