package com.st.application;

import com.st.application.parsers.LogParser;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class ParserTest {
    private final Parser parser = new LogParser();
    private final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");

    @Test
    public void parseResourceTest() {
        expectSingleItemInList(Collections.singletonList("2015-08-19 00:06:42,375 (http--0.0.0.0-28080-370) [] updateSubscriptionFromBackend 300109921258 in 243"));
        expectSingleItemInList(Collections.singletonList("2015-08-19 00:06:42,375 (http--0.0.0.0-28080-370) [] updateSubscriptionFromBackend 300109921258 true 1.0 in 243"));
        expectSingleItemInList(Collections.singletonList("2015-08-19 00:06:42,375 () [] updateSubscriptionFromBackend 300109921258  true 1.0 in 0"));
    }

    @Test
    public void parseURITest() {
        expectSingleItemInList(Collections.singletonList("2015-08-19 05:06:39,679 () [USER:300406591035] /mobilityServices.do?action=SERVICES&msisdn=300406591035&contentId=main_subscription in 46"));
        expectSingleItemInList(Collections.singletonList("2015-08-19 05:06:39,679 (http--0.0.0.0-28080-297) [USER:300406591035] /mobilityServices.do?action=SERVICES in 46"));
        expectSingleItemInList(Collections.singletonList("2015-08-19 05:06:39,679 (http--0.0.0.0-28080-297) [USER:300406591035] /mobilityServices.do?msisdn=300406591035&action=SERVICES&contentId=main_subscription in 0"));
        expectSingleItemInList(Collections.singletonList("2015-08-19 05:06:39,679 (http--0.0.0.0-28080-297) [USER:300406591035] /mobilityServices.do?msisdn=300406591035&action=SERVICES in 46"));
    }

    @Test
    public void parseResourceDataTest() {
        String time = "2015-08-19 00:06:36,599", resourceName = "updateSubscriptionFromBackend", duration = "243";
        List<String> dirtyList = Collections.singletonList(
                String.format("%s (http--0.0.0.0-28080-3) [CUST:CUS88O8888] /mainContent.do?action=%s&notificationType=invoice&contentId=notifications in %s", time, resourceName, duration));

        List<Parser.Data> dataList = parser.parse(dirtyList);
        Assert.assertNotNull(dataList);
        Assert.assertEquals(1, dataList.size());
        Parser.Data data = dataList.get(0);
        Assert.assertTrue(LocalDateTime.parse(time, dateTimeFormat).isEqual(data.getRequestTime()));
        Assert.assertEquals(resourceName, data.getResourceName());
        Assert.assertEquals(Long.valueOf(duration).longValue(), data.getRequestDuration());
    }


    @Test
    public void parseURIDataTest() {
        String time = "2015-08-19 00:06:39,632", resourceName = "NOTIFICATIONS", duration = "2";
        List<String> dirtyList = Collections.singletonList(
                String.format("%s (http--0.0.0.0-28080-3) [CUST:CUS88O8888] /mainContent.do?action=%s&notificationType=invoice&contentId=notifications in %s", time, resourceName, duration));

        List<Parser.Data> dataList = parser.parse(dirtyList);
        Assert.assertNotNull(dataList);
        Assert.assertEquals(1, dataList.size());
        Parser.Data data = dataList.get(0);
        Assert.assertTrue(LocalDateTime.parse(time, dateTimeFormat).isEqual(data.getRequestTime()));
        Assert.assertEquals(resourceName, data.getResourceName());
        Assert.assertEquals(Long.valueOf(duration).longValue(), data.getRequestDuration());
    }

    @Test
    public void wrongParseResourceTest() {
        //2015-08-19 00:06:42,375 (http--0.0.0.0-28080-370) [] updateSubscriptionFromBackend 300109921258 in 243
        expectEmptyList(null);
        expectEmptyList("");
        expectEmptyList("2015-08-19 00:06:42,375 (http--0.0.0.0-28080-370) [] updateSubscriptionFromBackend 300109921258 in");
        expectEmptyList("2015-08-19 00:06:42,375 (http--0.0.0.0-28080-370) [] updateSubscriptionFromBackend 300109921258 243");
        expectEmptyList("2015-08-19 00:06:42,375 (http--0.0.0.0-28080-370) [] updateSubscriptionFromBackend 300109921258");
        expectEmptyList("2015-08-19 00:06:42,375 (http--0.0.0.0-28080-370) updateSubscriptionFromBackend 300109921258 in 243");
        expectEmptyList("2015-08-19 00:06:42,375 [] updateSubscriptionFromBackend 300109921258 in 243");
        expectEmptyList("(http--0.0.0.0-28080-370) [] updateSubscriptionFromBackend 300109921258 in 243");
        expectEmptyList("2015-08-19 00:06:42, (http--0.0.0.0-28080-370) [] updateSubscriptionFromBackend 300109921258 in 243");
        expectEmptyList("2015^08^19 00:06:42,375 (http--0.0.0.0-28080-370) [] updateSubscriptionFromBackend 300109921258 in 243");
        expectEmptyList("2015-08-19 00:06:42,375 (http--0.0.0.0-28080-370) [] UpdateSubscriptionFromBackend 300109921258 in 243");
        expectEmptyList("2015-08-19 00:06:42,375 (http--0.0.0.0-28080-370) [] updateSubscript&&ionFromBackend 300109921258 in 243");
        expectEmptyList("2015-08-19 00:06:42,375 ( [] updateSubscriptionFromBackend 300109921258 in 243");
        expectEmptyList("2015-08-19 00:06:42,375 (http--0.0.0.0-28080-370) ] updateSubscripti=onFromBackend 300109921258 in 243");
        expectEmptyList("2015-08-19 (http--0.0.0.0-28080-370) [] updateSubscriptionFromBackend 300109921258 in 243");
        expectEmptyList("2015-08-19 00:06:42,375 (http--0.0.0.0-28080-370) [] !updateSubscriptionFromBackend 300109921258 true false in 243");
        expectEmptyList("2015-08-19 00:06:42,375 (http--0.0.0.0-28080-370) [] /updateSubscriptionFromBackend 300109921258 in 243");
    }

    @Test
    public void wrongParseURITest() {
        //2015-08-19 05:06:39,679 (http--0.0.0.0-28080-297) [USER:300406591035] /mobilityServices.do?action=SERVICES&msisdn=300406591035&contentId=main_subscription in 46
        expectEmptyList(null);
        expectEmptyList("");
        expectEmptyList("2015-08-19 05:06:39,679 [USER:300406591035] /mobilityServices.do?action=SERVICES&msisdn=300406591035 in 46");
        expectEmptyList("2015-08-19 05:06:39,0 (http--0.0.0.0-28080-297) [USER:300406591035] /mobilityServices.do?action=SERVICES&msisdn=300406591035 in 46");
        expectEmptyList("2015-08-19 05:06:39,679 (http--0.0.0.0-28080-297) [] /mobilityServices.do?action=SERVICES&msisdn=300406591035 in 46");
        expectEmptyList("2015-08-19 05:06:39,679 (http--0.0.0.0-28080-297) [USER:300406591035] /mobilityServices.do?=SERVICES&msisdn=300406591035 in 46");
        expectEmptyList("2015-08-19 05:06:39,679 (http--0.0.0.0-28080-297) [USER:300406591035] /mobilityServices.do?action=SERVICES&msisdn=300406591035 in ");
        expectEmptyList("2015-08-19 05:06:39,679 (http--0.0.0.0-28080-297) [USER:300406591035] /mobilityServices.do?action=SERVICES&msisdn=300406591035 46");
        expectEmptyList("2015-08-19 05:06:39,679 (http--0.0.0.0-28080-297) [USER:300406591035] mobilityServices.do?action=SERVICES&msisdn=300406591035 in 46");
        expectEmptyList("2015:08:19 05:06:39,679 (http--0.0.0.0-28080-297) [USER:300406591035] /mobilityServices.do?action=SERVICES&msisdn=300406591035&contentId=main_subscription in 46");
        expectEmptyList("2015-08-19 (http--0.0.0.0-28080-297) [USER:300406591035] /mobilityServices.do?action=SERVICES&msisdn=300406591035&contentId=main_subscription in 46");
    }

    private void expectEmptyList(String resourceString) {
        List<String> dirtyList;
        List<Parser.Data> dataList;
        dirtyList = Collections.singletonList(resourceString);
        dataList = parser.parse(dirtyList);
        Assert.assertNotNull(dataList);
        Assert.assertEquals(0, dataList.size());
    }

    private void expectSingleItemInList(List<String> dirtyList) {
        List<Parser.Data> dataList = parser.parse(dirtyList);
        Assert.assertNotNull(dataList);
        Assert.assertEquals(1, dataList.size());
    }
}
