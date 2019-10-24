package xoled.java.misc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MapsTest {
    @Mock Consumer<Entry<String, String>> leftOnly;
    @Mock Consumer<Entry<String, String>> rightOnly;
    @Mock Consumer<Entry<String, String>> common;
    @Mock BiConsumer<Entry<String, String>, Entry<String, String>> diff;

    @Test
    public void diff_no_null_values() {
        final Map<String, String> left = mapOf(
            "A", "a1",
            "B", "b1",
            "C", "c1"
        );
        final Map<String, String> right = mapOf(
            "A", "a1",
            "B", "b2",
            "D", "d1"
        );
        Maps.diff(left, right, leftOnly, rightOnly, common, diff);

        verify(leftOnly).accept(Maps.immutableEntry("C", "c1"));
        verify(rightOnly).accept(Maps.immutableEntry("D", "d1"));
        verify(common).accept(Maps.immutableEntry("A", "a1"));
        verify(diff).accept(Maps.immutableEntry("B", "b1"), Maps.immutableEntry("B", "b2"));
        verifyNoMoreInteractions(leftOnly, rightOnly, common, diff);
    }

    @Test
    public void diff_null_values() {
        final Map<String, String> left = mapOf(
            "A", "a1",
            "B", null,
            "C", null,
            "D", null
        );
        final Map<String, String> right = mapOf(
            "A", null,
            "B", "b1",
            "C", null,
            "E", null
        );
        Maps.diff(left, right, leftOnly, rightOnly, common, diff);

        verify(leftOnly).accept(Maps.immutableEntry("D", null));
        verify(rightOnly).accept(Maps.immutableEntry("E", null));
        verify(common).accept(Maps.immutableEntry("C", null));
        verify(diff).accept(Maps.immutableEntry("A", "a1"), Maps.immutableEntry("A", null));
        verify(diff).accept(Maps.immutableEntry("B", null), Maps.immutableEntry("B", "b1"));
        verifyNoMoreInteractions(leftOnly, rightOnly, common, diff);
    }

    @Test
    public void diff_left_only_entry_with_null_key() {
        final Map<String, String> left = Collections.singletonMap(null, "a");
        final Map<String, String> right = Collections.emptyMap();

        Maps.diff(left, right, leftOnly, rightOnly, common, diff);

        verify(leftOnly).accept(Maps.immutableEntry(null, "a"));
        verifyNoMoreInteractions(leftOnly, rightOnly, common, diff);
    }

    @Test
    public void diff_right_only_entry_with_null_key() {
        final Map<String, String> left = Collections.emptyMap();
        final Map<String, String> right = Collections.singletonMap(null, "b");

        Maps.diff(left, right, leftOnly, rightOnly, common, diff);

        verify(rightOnly).accept(Maps.immutableEntry(null, "b"));
        verifyNoMoreInteractions(leftOnly, rightOnly, common, diff);
    }

    @Test
    public void diff_common_entry_with_null_key() {
        final Map<String, String> left = Collections.singletonMap(null, "a");
        final Map<String, String> right = Collections.singletonMap(null, "a");

        Maps.diff(left, right, leftOnly, rightOnly, common, diff);

        verify(common).accept(Maps.immutableEntry(null, "a"));
        verifyNoMoreInteractions(leftOnly, rightOnly, common, diff);
    }

    @Test
    public void diff_null_key_with_different_values() {
        final Map<String, String> left = Collections.singletonMap(null, "a");
        final Map<String, String> right = Collections.singletonMap(null, "b");

        Maps.diff(left, right, leftOnly, rightOnly, common, diff);

        verify(diff).accept(Maps.immutableEntry(null, "a"), Maps.immutableEntry(null, "b"));
        verifyNoMoreInteractions(leftOnly, rightOnly, common, diff);
    }

    static <K, V> Map<K, V> mapOf(
        K k1, V v1,
        K k2, V v2
    ) {
        final Map<K, V> map = new HashMap<>(5, 1f);
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    static <K, V> Map<K, V> mapOf(
        K k1, V v1,
        K k2, V v2,
        K k3, V v3
    ) {
        final Map<K, V> map = new HashMap<>(5, 1f);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }

    static <K, V> Map<K, V> mapOf(
        K k1, V v1,
        K k2, V v2,
        K k3, V v3,
        K k4, V v4
    ) {
        final Map<K, V> map = new HashMap<>(5, 1f);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        return map;
    }

    static <K, V> Map<K, V> mapOf(
        K k1, V v1,
        K k2, V v2,
        K k3, V v3,
        K k4, V v4,
        K k5, V v5
    ) {
        final Map<K, V> map = new HashMap<>(5, 1f);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        return map;
    }
}