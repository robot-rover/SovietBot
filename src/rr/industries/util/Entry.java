package rr.industries.util;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/18/2016
 */
public class Entry<T, V> {
    T object1;
    V object2;

    public Entry(T t, V v) {
        object1 = t;
        object2 = v;
    }

    public T getFirst() {
        return object1;
    }

    public V getSecond() {
        return object2;
    }
}
