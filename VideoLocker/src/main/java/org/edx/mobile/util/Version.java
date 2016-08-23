package org.edx.mobile.util;

import android.support.annotation.NonNull;

import java.util.Arrays;

/**
 * Simple representation of the app's version.
 */
public class Version implements Comparable<Version> {
    /**
     * The number of version number tokens to parse.
     */
    private static final int NUMBERS_COUNT = 3;

    /**
     * The version numbers
     */
    @NonNull
    private final int[] numbers = new int[3];

    /**
     * Create a new instance from the provided version string.
     *
     * @param version The version string. The first three present dot-separated
     *                tokens will be parsed as major, minor, and patch version
     *                numbers respectively, and any further tokens will be
     *                discarded.
     * @throws NumberFormatException If one or more of the first three present
     *                               dot-separated tokens contain non-numeric
     *                               characters.
     */
    public Version(@NonNull String version) throws NumberFormatException {
        final String[] numberStrings = version.split("\\.");
        final int versionsCount = Math.min(NUMBERS_COUNT, numberStrings.length);
        for (int i = 0; i < versionsCount; i++) {
            final String numberString = numberStrings[i];
            /* Integer.parseInt() parses a string as a signed integer value, and
             * there is no available method for parsing as unsigned instead.
             * Therefore, we first check the first character manually to see
             * whether it's a plus or minus sign, and throw a
             * NumberFormatException if it is.
             */
            final char firstChar = numberString.charAt(0);
            if (firstChar == '-' || firstChar == '+') {
                throw new NumberFormatException();
            }
            numbers[i] = Integer.parseInt(numberString);
        }
    }

    /**
     * @return The major version.
     */
    public int getMajorVersion() {
        return getVersionAt(0);
    }

    /**
     * @return The minor version.
     */
    public int getMinorVersion() {
        return getVersionAt(1);
    }

    /**
     * @return The patch version.
     */
    public int getPatchVersion() {
        return getVersionAt(2);
    }

    /**
     * Returns the version number at the provided index.
     *
     * @param index The index at which to get the version number
     * @return The version number.
     */
    private int getVersionAt(int index) {
        return index < numbers.length ? numbers[index] : 0;
    }

    @Override
    public boolean equals(@NonNull Object object) {
        if (this == object) return true;
        if (!(object instanceof Version)) return false;
        return Arrays.equals(numbers, ((Version) object).numbers);

    }

    @Override
    public int compareTo(@NonNull Version other) {
        for (int i = 0; i < NUMBERS_COUNT; i++) {
            final int number = numbers[i];
            final int otherNumber = other.numbers[i];
            if (number != otherNumber) {
                return number < otherNumber ? -1 : 1;
            }
        }
        return 0;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(numbers);
    }
}