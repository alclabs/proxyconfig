package com.alcshare.proxyconfig.util;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class JavaVersion implements Comparable
{
    /*
    String version is like 1.3.1_05-ea.
     */
    private int major;
    private int minor;
    private int tertiary;
    private int update;
    private String identifier;

    public JavaVersion() {
        this(System.getProperty("java.version"));
    }

    public  JavaVersion(String stringVersion) {
        Pattern pattern = Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:_([0-9]+))?(?:-(.+))?");
        Matcher matcher = pattern.matcher(stringVersion);
        if (matcher.matches()) {
            String stringMajor = matcher.group(1);
            String stringMinor = matcher.group(2);
            String stringTertiary = matcher.group(3);
            String stringUpdate = matcher.group(4);
            identifier = matcher.group(5);

            major = parseInt(stringMajor, -1);
            minor = parseInt(stringMinor, -1);
            tertiary = parseInt(stringTertiary, -1);
            update = parseInt(stringUpdate, -1);
        }
    }

    public String getIdentifier() { return identifier; }


    private int parseInt(String string, int defaultValue) {
        int result = defaultValue;
        if (string != null) {
            try {
               result = Integer.parseInt(string);
            } catch (NumberFormatException ignored) {} // ignore and use default
        }
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JavaVersion that = (JavaVersion) o;

        if (major != that.major) return false;
        if (minor != that.minor) return false;
        if (tertiary != that.tertiary) return false;
        if (update != that.update) return false;
        if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + tertiary;
        result = 31 * result + update;
        result = 31 * result + (identifier != null ? identifier.hashCode() : 0);
        return result;
    }

    public boolean isAtLeast(JavaVersion other) {
        return (compareTo(other) >= 0);
    }

    public int compareTo(@NotNull Object o)
    {
        // - if this is less
        JavaVersion other = (JavaVersion) o;
        if (major != other.major) {
            return major - other.major;
        } else {
            if (minor != other.minor) {
                return minor - other.minor;
            } else {
                if (tertiary != other.tertiary) {
                    return tertiary - other.tertiary;
                } else {
                    if (update != other.update) {
                        return update - other.update;
                    } else {
                        return 0;
                    }
                }
            }
        }
    }
}
