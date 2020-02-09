package xoled.java.misc;

public enum CharSequences {
    ;

    /**
     * Invokes the specified callback for every pair of character subsequences
     * separated by the provided delimiters: one is to separate elements within a pair,
     * another - to separate pairs.
     * Allocating memory for the found components is a caller responsibility. It receives
     * callback invocation with start-end positions of each of the pair elements.<br>
     * In case of missing separator for a pair, an exception is thrown.
     * The callback is invoked for any valid pair that occurs prior to the malformed one.
     * @param input The input character sequence.
     * @param pairDelimiter Delimiter separating pair components.
     * @param keyValueDelimiter Delimiter separating the pairs.
     * @param consumer Callback is invoked on every well-formed pair. To minimize memory impact
     *                 does not create any object, but returns start-end positions of the first
     *                 and second component, suitable for direct use with
     *                 {@link CharSequence#subSequence(int, int)} method.
     * @throws IllegalStateException thrown in case of malformed input.
     */
    public static void asDelimitedStringPairs(
        CharSequence input,
        char pairDelimiter,
        char keyValueDelimiter,
        StringPairConsumer consumer
    ) {
        if (pairDelimiter == keyValueDelimiter)
            throw new IllegalArgumentException(
                "key-value delimiter is the same as pair delimiter"
            );
        if (input == null || input.length() == 0)
            return;
        int kvStart = 0;
        int kvSepPos = -1;
        for(int i = 0; i < input.length(); i++) {
            final char ch = input.charAt(i);
            if (ch == keyValueDelimiter && kvSepPos < 0) {
                kvSepPos = i;
            }
            if (ch == pairDelimiter) {
                if (kvSepPos < 0)
                    throw new IllegalStateException("No key/value separator preceeding delimiter at " + i);
                consumer.apply(kvStart, kvSepPos, kvSepPos + 1, i);
                kvStart = i + 1;
                kvSepPos = -1;
            }
        }
        if (kvSepPos >= 0)
            consumer.apply(kvStart, kvSepPos, kvSepPos + 1, input.length());
        else
            throw new IllegalStateException("No key/value separator preceeding EOL");
    }

    public interface StringPairConsumer {
        /**
         * Consumes the start-end positions of the elements of every pair
         * discovered by
         * {@link #asDelimitedStringPairs(CharSequence, char, char, StringPairConsumer)} method.
         * Each end position is one after the end position of the respective element for
         * direct use with {@link CharSequence#subSequence(int, int)} method.
         * @param kStart Start position of the 1st element.
         * @param kEnd First position after the end position of the 1st element.
         * @param vStart Start position of the 2nd element.
         * @param vEnd First position after the end position of the 2nd element.
         */
        void apply(int kStart, int kEnd, int vStart, int vEnd);
    }
}
