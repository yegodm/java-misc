package xoled.java.misc;

@FunctionalInterface
public interface TriConsumer<T1, T2, T3> {
    /**
     * Performs operation on the three given parameters.
     */
    void accept(T1 value1, T2 value2, T3 value3);
}
