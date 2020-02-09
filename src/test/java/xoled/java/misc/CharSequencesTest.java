package xoled.java.misc;


import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CharSequencesTest {

    @ParameterizedTest
    @MethodSource("wellformed_key_value_examples")
    void asDelimitedStringPairs_valid_cases(String input, Map<String, String> expectedValues) {
        final Map<String, String> values = new LinkedHashMap<>();
        CharSequences.asDelimitedStringPairs(
            input,
            '|',
            '=',
            (kStart, kEnd, vStart, vEnd) -> values.put(input.substring(kStart, kEnd), input.substring(vStart, vEnd))
            );
        assertThat(values).containsExactlyEntriesOf(expectedValues);
    }

    static Stream<Arguments> wellformed_key_value_examples() {
        return Stream.of(
            Arguments.of("k1=v1|k2=v2|k3=v3", ImmutableMap.of("k1", "v1", "k2", "v2","k3", "v3")),
            Arguments.of("k=", ImmutableMap.of("k", "")),
            Arguments.of("=v", ImmutableMap.of("", "v")),
            Arguments.of("k=v=a", ImmutableMap.of("k", "v=a")),
            Arguments.of("k=v=a|k1==v1", ImmutableMap.of("k", "v=a", "k1", "=v1")),
            Arguments.of("=", ImmutableMap.of("", ""))
        );
    }

    @Test
    void asDelimitedStringPairs_allows_duplicate_keys() {
        final StringBuilder collector = new StringBuilder();
        final String input = "k=a1|k=a2|k=a3";
        CharSequences.asDelimitedStringPairs(
            input,
            '|',
            '=',
            (kStart, kEnd, vStart, vEnd) ->
                collector.append(input.subSequence(kStart, kEnd))
                         .append('=')
                         .append(input.subSequence(vStart, vEnd))
                         .append(';')
        );
        assertThat(collector.toString()).isEqualTo("k=a1;k=a2;k=a3;");
    }

    @ParameterizedTest
    @MethodSource("malformed_key_value_examples")
    void asDelimitedKeyValuePairs_malformed_cases(String input, Class<? extends Throwable> expectedException) {
        assertThrows(expectedException, () ->
            CharSequences.asDelimitedStringPairs(input, '|', '=', (kStart, kEnd, vStart, vEnd) -> {})
        );
    }

    static Stream<Arguments> malformed_key_value_examples() {
        return Stream.of(
            Arguments.of("k1=v1|k2v2", IllegalStateException.class),
            Arguments.of("k", IllegalStateException.class),
            Arguments.of("|", IllegalStateException.class)
        );
    }

}