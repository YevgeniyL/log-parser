package com.st.application;

import com.st.application.infrastructure.LogUtil;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LogUtilTest {

    @Test
    public void data_avgDurationTest() {
        String resourceName = "someRes";
        int requestDuration1 = 100;
        int requestDuration2 = 20;
        int[] durationsArray = {requestDuration1, requestDuration2};

        List<Parser.Data> logs = new ArrayList<>();
        logs.add(new Parser.Data(LocalDateTime.now(), resourceName, requestDuration1));
        logs.add(new Parser.Data(LocalDateTime.now(), resourceName, requestDuration2));

        Map<String, Double> result = LogUtil.buildAvgDurationByResource(logs, null);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(Arrays.stream(durationsArray).average().orElse(Double.NaN), result.get(resourceName), 0d);


        String resourceName1 = "someRes";
        String resourceName2 = "someRes2";
        String resourceName3 = "someRes3";
        requestDuration1 = 100;
        requestDuration2 = 20;
        int requestDuration3 = 0;

        logs = new ArrayList<>();
        logs.add(new Parser.Data(LocalDateTime.now(), resourceName1, requestDuration1));
        logs.add(new Parser.Data(LocalDateTime.now(), resourceName2, requestDuration2));
        logs.add(new Parser.Data(LocalDateTime.now(), resourceName3, requestDuration3));

        result = LogUtil.buildAvgDurationByResource(logs, 2);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.containsKey(resourceName1));
        Assert.assertTrue(result.containsKey(resourceName2));
        Assert.assertFalse(result.containsKey(resourceName3));
    }

    @Test
    public void orderDuration_avgDurationTest() {
        String resourceName1 = "someRes";
        String resourceName2 = "someRes2";
        int requestDuration1 = 100;
        int requestDuration2 = 90;
        int requestDuration3 = 20;

        List<Parser.Data> logs = new ArrayList<>();
        logs.add(new Parser.Data(LocalDateTime.now(), resourceName1, requestDuration1));
        logs.add(new Parser.Data(LocalDateTime.now(), resourceName2, requestDuration2));
        logs.add(new Parser.Data(LocalDateTime.now(), resourceName2, requestDuration3));

        Map<String, Double> result = LogUtil.buildAvgDurationByResource(logs, null);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Double lastVal = null;
        for (Double value : result.values()) {
            if (lastVal != null) {
                Assert.assertTrue(lastVal > value);
            } else {
                lastVal = value;
            }
        }
    }

    @Test
    public void data_sumByHourTest() {
        String resourceName1 = "someRes";
        String resourceName2 = "someRes2";
        int requestDuration1 = 100;
        int requestDuration2 = 90;
        int requestDuration3 = 20;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime key = now.plusHours(1).truncatedTo(ChronoUnit.HOURS);

        List<Parser.Data> logs = new ArrayList<>();
        logs.add(new Parser.Data(now, resourceName1, requestDuration1));
        logs.add(new Parser.Data(now, resourceName2, requestDuration2));
        logs.add(new Parser.Data(now, resourceName2, requestDuration3));

        Map<LocalDateTime, Long> result = LogUtil.buildDurationSumByHour(logs);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.containsKey(key));
        Assert.assertEquals(requestDuration1 + requestDuration2 + requestDuration3, result.get(key), 0d);
    }

    @Test
    public void orderTime_sumByHourTest() {
        String resourceName1 = "someRes";
        String resourceName2 = "someRes2";
        int requestDuration1 = 5;
        int requestDuration2 = 95;
        int requestDuration3 = 200;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = LocalDateTime.now().plusHours(1);

        List<Parser.Data> logs = new ArrayList<>();
        logs.add(new Parser.Data(now, resourceName1, requestDuration1));
        logs.add(new Parser.Data(now, resourceName2, requestDuration2));
        logs.add(new Parser.Data(next, resourceName2, requestDuration3));

        Map<LocalDateTime, Long> result = LogUtil.buildDurationSumByHour(logs);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());

        LocalDateTime lastVal = null;
        for (LocalDateTime thisValue : result.keySet()) {
            if (lastVal != null) {
                Assert.assertTrue(thisValue.isAfter(lastVal));
            } else {
                lastVal = thisValue;
            }
        }
    }
}
