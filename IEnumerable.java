package net.rboeije.enumerable4j;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Wrapper around java stream, based on .NET IEnumerable: https://msdn.microsoft.com/en-us/library/9eekhta0(v=vs.110).aspx
 */
public interface IEnumerable<TSource> extends Iterable<TSource> {

    Stream<TSource> getStream();

    default IEnumerable<TSource> where(Predicate<? super TSource> predicate) {
        return new Enumerable<>(getStream().filter(predicate));
    }

    default <TResult> IEnumerable<TResult> select(Function<TSource, TResult> expression) {
        return new Enumerable<>(getStream().map(o -> expression.apply(o)));
    }

    default <TResult> IEnumerable<TResult> selectMany(Function<TSource, Collection<TResult>> expression) {
        return new Enumerable<>(getStream().flatMap(o -> expression.apply(o).stream()));
    }

    default IEnumerable<TSource> concat(IEnumerable<TSource> e) {
        return new Enumerable<>(Stream.concat(this.getStream(), e.getStream()));
    }

    default IEnumerable<TSource> orderBy(Function<TSource, ? extends Comparable>... properties) {
        if (properties == null || properties.length == 0) throw new IllegalArgumentException("properties may not be empty");

        Comparator<TSource> c = null;

        for (final Function<TSource, ? extends Comparable> f : properties) {
            Comparator<TSource> comp = (o1, o2) -> f.apply(o1).compareTo(f.apply(o2));
            if (c == null) c = comp;
            else c = c.thenComparing(comp);
        }
        return new Enumerable<>(getStream().sorted(c));
    }

    default IEnumerable<TSource> orderByDescending(Function<TSource, ? extends Comparable>... properties) {
        if (properties == null || properties.length == 0) throw new IllegalArgumentException("properties may not be empty");

        Comparator<TSource> c = null;

        for (final Function<TSource, ? extends Comparable> f : properties) {
            Comparator<TSource> comp = (o1, o2) -> f.apply(o1).compareTo(f.apply(o2));
            if (c == null) c = comp;
            else c = c.thenComparing(comp);
        }
        return new Enumerable<>(getStream().sorted(c.reversed()));
    }

    // terminals

    default long count() {
        return getStream().count();
    }

    default Optional<TSource> findFirst() {
        return getStream().findFirst();
    }

    default Optional<TSource> findFirst(Predicate<TSource> expression) {
        return getStream().filter(expression).findFirst();
    }

    default TSource findFirstOrNull() {
        return getStream().findFirst().orElse(null);
    }

    default TSource findFirstOrNull(Predicate<TSource> expression) {
        return getStream().filter(expression).findFirst().orElse(null);
    }

    default <T extends Comparable> T max(Function<TSource, T> f) {
        Optional<TSource> max = getStream().max((o1, o2) -> f.apply(o1).compareTo(f.apply(o2)));

        return max.isPresent() ? f.apply(max.get()) : null;
    }

    default <T extends Comparable> T min(Function<TSource, T> f) {
        Optional<TSource> min = getStream().min((o1, o2) -> f.apply(o1).compareTo(f.apply(o2)));

        return min.isPresent() ? f.apply(min.get()) : null;
    }

    default <TResult> TResult sum(Function<TSource, TResult> expression, TResult identity, BinaryOperator<TResult> accumulator) {
        return getStream().map(expression).reduce(identity, accumulator);
    }

    default List<TSource> toList() {
        return getStream().collect(Collectors.toList());
    }

    default <TList extends List<TSource>> TList toList(Class<TList> listType) {

        return getStream().collect(
                () -> createInstance(listType),
                List::add,
                List::addAll);
    }

    static <T> T createInstance(Class<T> type) {
        try {
            return type.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    default <TResult> Map<TResult, List<TSource>> groupBy(Function<TSource, TResult> expression) {
        return getStream().collect(Collectors.groupingBy(o -> expression.apply(o)));
    }
}
