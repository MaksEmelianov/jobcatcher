package online.jobcatcher.grabber.utils;

import java.time.LocalDateTime;

@FunctionalInterface
public interface DateTimeParser {
    LocalDateTime parse(String parse);
}
