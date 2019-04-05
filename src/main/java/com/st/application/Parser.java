package com.st.application;

import java.time.LocalDateTime;
import java.util.List;

public interface Parser {

    List<Data> parse(List<String> logLineList);

    class Data {
        private LocalDateTime requestTime;
        private String resourceName;
        private long requestDuration;

        public Data(LocalDateTime requestTime, String resourceName, int requestDuration) {
            this.requestTime = requestTime;
            this.resourceName = resourceName;
            this.requestDuration = requestDuration;
        }

        public LocalDateTime getRequestTime() {
            return requestTime;
        }

        public long getRequestDuration() {
            return requestDuration;
        }

        public String getResourceName() {
            return resourceName;
        }
    }
}
