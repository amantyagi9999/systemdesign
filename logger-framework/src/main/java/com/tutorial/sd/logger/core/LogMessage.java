package com.tutorial.sd.logger.core;

import lombok.Data;

import java.time.Instant;

@Data
public class LogMessage {
    private final Instant timestamp;
    private final LogLevel level;
    private final String message;
    private final String  source;

    private LogMessage(Builder builder) {
        this.timestamp = builder.timestamp;
        this.level = builder.level;
        this.message = builder.message;
        this.source = builder.source;
    }

    @Override
    public String toString() {
        return String.format("LogMessage{timestamp=%s, level=%s, message='%s', source='%s'}",
                timestamp, level, message, source);
    }

    public static class Builder{
        private Instant timestamp;
        private LogLevel level;
        private String message;
        private String source;

        public LogMessage build() {
            if(level == null)
                throw new IllegalStateException("LogLevel required");
            if(message == null || message.trim().isEmpty())
                throw new IllegalStateException("Message is Required");
            return new LogMessage(this);
        }

        public Builder level(LogLevel level) {
            this.level = level;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

    }
}
