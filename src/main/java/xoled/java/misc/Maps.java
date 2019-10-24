package xoled.java.misc;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public enum Maps {
    ;

    private static final Object NIL = new Object();

    /**
     * Calculates two map difference invoking the respective callbacks
     * for left-only, right-only, same-value, and different-value entries
     * in the maps.
     * @param left First map to compare.
     * @param right Second map to compare.
     * @param forLeftOnly Callback invoked for each key-value that occurs in the first map only.
     * @param forRightOnly Callback invoked for each key-value  that occurs in the second map only.
     * @param forCommon Callback invoked on each key-value that is found in both maps.
     * @param forDiff Callback invoked for every entry whose key is in both maps,
     *                but values are different in terms of {@link Objects#equals(Object, Object)}.
     *                The key is provided as the first argument.
     *                Value found in the first map - as the second argument.
     *                Value found in the second map - as the third argument,
     */
    public static <K, V> void diff(
        Map<K, V> left,
        Map<K, V> right,
        BiConsumer<K, V> forLeftOnly,
        BiConsumer<K, V> forRightOnly,
        BiConsumer<K, V> forCommon,
        TriConsumer<K, V, V> forDiff
    ) {
        assert forLeftOnly != null : "forLeftOnly is null";
        assert forRightOnly != null : "forRightOnly is null";
        assert forCommon != null : "forCommon is null";
        assert forDiff != null : "forDiff is null";

        if (right == null && left == null)
            return;
        if (right == null) {
            left.forEach(forLeftOnly);
            return;
        }
        if (left == null) {
            right.forEach(forRightOnly);
            return;
        }

        left.forEach((k, v) -> {
                final Object rv = ((Map) right).getOrDefault(k, NIL);
                if (rv == NIL)
                    forLeftOnly.accept(k, v);
                else if (Objects.equals(v, rv))
                    forCommon.accept(k, v);
                else
                    forDiff.accept(k, v, (V)rv);
            });
        right.forEach((k, v) -> {
            if (!left.containsKey(k))
                forRightOnly.accept(k, v);
        });
    }

    public static <K, V> Map.Entry<K, V> immutableEntry(K key, V value) {
        return new AbstractMap.SimpleEntry<K, V>(key, value) {
            @Override
            public V setValue(V value) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
