package com.st.application;


import com.st.application.infrastructure.LogUtil;
import com.st.application.parsers.LogParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private final static Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        final DateTimeFormatter printTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd: HH");
        final Parser parser = new LogParser();

        Instant startTime = Instant.now();

        if (args.length == 0) {
            log.log(Level.INFO, "Please, parse with required arguments. For help run with argument -h");
            return;
        }

        final int maxArgumentSize = 2;
        if (args.length > maxArgumentSize) {
            log.log(Level.INFO, "Too many arguments. For help run with argument -h");
            return;
        }

        List<String> inputArgumentList = Arrays.asList(args);
        if (inputArgumentList.contains("-h")) {
            printHelp();
            return;
        }

        Integer maxAvgLinesCount = null;
        if (inputArgumentList.size() > 1) {
            try {
                maxAvgLinesCount = Integer.valueOf(inputArgumentList.get(1));
            } catch (NumberFormatException e) {
                log.log(Level.WARNING, "Max count for highest average requests is not integer value. For help run with argument -h");
                return;
            }
        }

        String logName = inputArgumentList.get(0);
        try {
            List<String> lineList = Files.readAllLines(Paths.get(logName));
            List<Parser.Data> logs = parser.parse(lineList);
            if (logs.size() > 0) {

                //Print out top n (exact value of n is passed as program argument) resources with highest average request duration.
                maxAvgLinesCount = maxAvgLinesCount != null ? maxAvgLinesCount : Integer.MAX_VALUE;
                Map<String, Double> resourceAvgDuration = LogUtil.buildAvgDurationByResource(logs, maxAvgLinesCount);
                if (!resourceAvgDuration.isEmpty()) {
                    printAvgDuration(resourceAvgDuration);
                } else {
                    log.log(Level.WARNING, "Not have data to print resources with highest average request duration");
                }

                //Draw histogram of hourly number of requests
                Map<LocalDateTime, Long> resultSumDuration = LogUtil.buildDurationSumByHour(logs);
                Long maxDuration = resultSumDuration.values().stream()
                        .max(Long::compare)
                        .orElse(null);

                if (!resultSumDuration.isEmpty() && maxDuration != null) {
                    printHistogram(printTimeFormat, resultSumDuration, maxDuration);
                } else {
                    log.log(Level.WARNING, "Not have data to draw histogram of hourly number of requests");
                }

                //Print out number of (milli)seconds your program run
                System.out.println("\nProgram work time = " + ChronoUnit.MILLIS.between(startTime, Instant.now()) + " millis");
            } else {
                log.log(Level.WARNING, "Empty log file");
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Please, run application with argument, contain log file name or path. For help run with argument -h");
        }
    }

    private static void printHistogram(DateTimeFormatter printTimeFormat, Map<LocalDateTime, Long> resultSumDurationByHour, Long maxDuration) {
        System.out.println("\nDraw histogram of hourly number of requests:");
        System.out.println("    0-100(step 1%) => ****************************************************************************************************");
        Map<LocalDateTime, Long> sortedDuration = new TreeMap<>(resultSumDurationByHour);
        for (Map.Entry<LocalDateTime, Long> entry : sortedDuration.entrySet()) {
            Long durationPercents = entry.getValue() * 100 / maxDuration;
            System.out.println("    " + entry.getKey().format(printTimeFormat) + " => " + String.join("", Collections.nCopies(durationPercents.intValue(), "*")));
        }
    }

    private static void printAvgDuration(Map<String, Double> resourceAvgDuration) {
        System.out.println("Resources with highest average request duration: ");
        AtomicInteger lineNumber = new AtomicInteger();
        resourceAvgDuration.forEach((resource, avgDuration) -> System.out.println("    " + lineNumber.incrementAndGet() + ") " + resource + " = " + avgDuration));
    }

    private static void printHelp() {
        System.out.println("Input arguments:");
        System.out.println("   [fileName]   Log file name (required)");
        System.out.println("   [n]          Integer argument (optional). Print out top n (exact value of n is passed as program argument) resources with highest average request duration. Optional\n");
        System.out.println("Example parse:    java -jar log-parser-20190302.jar log.log 10");
        System.out.println("                java -jar log-parser-20190302.jar log.log");
    }
}
