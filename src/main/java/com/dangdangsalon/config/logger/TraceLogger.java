package com.dangdangsalon.config.logger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TraceLogger {

    public TraceStatus begin(TraceId traceId, String message) {
        Long startTimeMs = System.currentTimeMillis();
        traceId = (traceId == null) ? new TraceId() : traceId.createNextId();
        log.info("[{}] {}{}", traceId.getId(), addSpace("-->", traceId.getLevel()), message);
        return new TraceStatus(traceId, startTimeMs, message);
    }

    public void end(TraceStatus status) {
        complete(status, null);
    }

    public void exception(TraceStatus status, Exception e) {
        complete(status, e);
    }

    private void complete(TraceStatus status, Exception e) {
        Long stopTimeMs = System.currentTimeMillis();
        long elapsedTime = stopTimeMs - status.getStartTimeMs();
        TraceId traceId = status.getTraceId();
        if (e == null) {
            log.info("[{}] {}{} time={}ms", traceId.getId(),
                    addSpace("<--", traceId.getLevel()), status.getMessage(), elapsedTime);
        } else {
            log.error("[{}] {}{} time={}ms ex={}", traceId.getId(),
                    addSpace("<X-", traceId.getLevel()), status.getMessage(), elapsedTime, e);
        }
    }

    private String addSpace(String prefix, int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append((i == level - 1) ? "|" + prefix : "| ");
        }
        return sb.toString();
    }
}

