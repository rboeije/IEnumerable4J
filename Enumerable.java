package net.rboeije.enumerable4j;

import java.util.Iterator;
import java.util.stream.Stream;

public class Enumerable<TSource> implements IEnumerable<TSource> {

    private Stream<TSource> stream;

    public Enumerable(Stream<TSource> stream) {

        this.stream = stream;
    }

    @Override
    public Stream<TSource> getStream() {
        return stream;
    }

    @Override
    public Iterator<TSource> iterator() {
        return stream.iterator();
    }

    public static <T> Enumerable<T> empty() {
        return new Enumerable<>(Stream.empty());
    }
}
