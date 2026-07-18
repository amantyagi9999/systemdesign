package com.tutorial.sd.logger.core;

public enum LogLevel {
    DEBUG(1),
    INFO(2),
    WARN(3),
    ERROR(4),
    FATAL(5);

    private final int priority;

    LogLevel(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isGreaterOrEqual(LogLevel level){
        return this.priority >= level.priority;
    }
}
