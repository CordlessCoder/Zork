package org.example;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexCommandHelper<R> {
    private final Pattern minimal_pattern;
    private final Pattern complete_pattern;
    private final String incomplete_message;
    private final Function<Matcher, Optional<R>> complete_callback;

    public RegexCommandHelper(
            // The smallest regex
            String minimal_pattern, String complete_pattern, String incomplete_message, Function<Matcher, Optional<R>> complete_callback) {
        this.minimal_pattern = Pattern.compile(minimal_pattern);
        this.complete_pattern = Pattern.compile(complete_pattern);
        this.incomplete_message = incomplete_message;
        this.complete_callback = complete_callback;
    }

    public Optional<R> apply(String text) {
        var complete_matcher = complete_pattern.matcher(text);
        if (!complete_matcher.matches()) {
            if (minimal_pattern.matcher(text).matches()) {
                System.out.println(incomplete_message);
            }
            return Optional.empty();
        }
        return complete_callback.apply(complete_matcher);
    }
}
