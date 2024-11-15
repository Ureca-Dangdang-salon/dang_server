package com.dangdangsalon.config.logger;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class TraceAspect {

    private final TraceLogger traceLogger;

    public TraceAspect(TraceLogger traceLogger) {
        this.traceLogger = traceLogger;
    }

    private static final ThreadLocal<TraceId> traceIdHolder = new ThreadLocal<>();

    @Pointcut("execution(* com.dangdangsalon.domain..controller..*(..)) || " +
            "execution(* com.dangdangsalon.domain..service..*(..)) || " +
            "execution(* com.dangdangsalon.domain..repository..*(..))")
    private void applicationPackagePointcut() {}

    @Around("applicationPackagePointcut()")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        TraceId traceId = traceIdHolder.get();
        TraceStatus status = null;

        try {
            String methodName = joinPoint.getSignature().toShortString();
            Object[] args = joinPoint.getArgs();
            String argsString = Arrays.toString(args);

            traceId = (traceId == null) ? new TraceId() : traceId.createNextId();
            traceIdHolder.set(traceId);

            status = traceLogger.begin(traceId, methodName + " with args: " + argsString);

            Object result = joinPoint.proceed();

            traceLogger.end(status);
            return result;

        } catch (Exception e) {
            if (status != null) {
                traceLogger.exception(status, e);
            }

            throw e;
        } finally {
            TraceId currentTraceId = traceIdHolder.get();
            if (currentTraceId != null && currentTraceId.isFirstLevel()) {
                traceIdHolder.remove();
            } else {
                traceIdHolder.set(currentTraceId.createPreviousId());
            }
        }
    }
}
