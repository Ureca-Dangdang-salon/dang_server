package com.dangdangsalon.util;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

public class UUIDUtil {
    private static final TimeBasedGenerator generator = Generators.timeBasedGenerator();

    public static String generateTimeBasedUUID() {
        return generator.generate().toString();
    }
}
