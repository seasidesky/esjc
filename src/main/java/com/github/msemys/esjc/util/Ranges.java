package com.github.msemys.esjc.util;

public class Ranges {

    public static final IntRange BATCH_SIZE_RANGE = new IntRange(1, 4 * 1024, IntRange.Type.CLOSED);

    public static final IntRange ATTEMPTS_RANGE = new IntRange(-1, Integer.MAX_VALUE, IntRange.Type.CLOSED);

}
