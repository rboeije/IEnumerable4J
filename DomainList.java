package net.rboeije.enumerable4j;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;


public class DomainList<T> extends LinkedList<T> implements IEnumerable<T> {

    public DomainList() {
        super();
    }

    public DomainList(Collection<T> collection) {
        super(collection);
    }

    public DomainList(T... items) {
        this();

        if (items != null) {
            for (final T item : items) {
                add(item);
            }
        }
    }

    private DomainList<T> removedItems;

    public DomainList<T> getRemovedItems(){
        return (removedItems == null) ? (removedItems = new DomainList<>()) : removedItems;
    }

    public boolean isItemRemoved(T item){
        return removedItems != null && removedItems.contains(item);
    }

    public List<T> getAllItems() {
        ArrayList<T> list = new ArrayList<>(this);

        if (removedItems != null)
        {
            list.addAll(removedItems);
        }
        return list;
    }

    @Override
    public boolean add(T domainObject) {
        throwIfNull(domainObject);

        if (contains(domainObject)) throw new IllegalArgumentException("An item with the same key has already been added.");

        if (isItemRemoved(domainObject)) {
            removedItems.remove(domainObject);
        }

        return super.add(domainObject);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throwIfNull(c);

        boolean isChanged = false;
        for (T o : c) {
            isChanged |= add(o);
        }
        return isChanged;
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof DomainObject)) {
            throw new IllegalArgumentException("Not a DomainObject");
        }
        T domainObject = (T)o;
        if (super.remove(domainObject)){
            return getRemovedItems().add(domainObject);
        }
        return false;
    }

    public T remove(int index){
        T item = super.remove(index);
        getRemovedItems().add(item);
        return item;
    }

    public boolean remove(Id id){
        for (final T o : this) {
            if (o.getId().equals(id)){
                return remove(o);
            }
        }
        return false;
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        boolean changed = false;

        for (final Object o : c) {
            changed |= remove(o);
        }
        return changed;
    }

    @Override
    public void clear() {
        super.clear();
        if (removedItems != null) {
            removedItems.clear();
        }
    }

    public DomainList<T> first(int count) {

        DomainList<T> list = new DomainList();

        count = Math.min(count, this.size());

        for (int i = 0; i < count; i++) {
            list.add(this.get(i));
        }

        return list;
    }

    @SafeVarargs
    public final DomainList<T> sortAscending(Function<T, ? extends Comparable>... properties) {

        return sortList(getComparator(properties));
    }

    @SafeVarargs
    public final DomainList<T> sortDecending(Function<T, ? extends Comparable>... properties) {

        return sortList(getComparator(properties).reversed());
    }

    private Comparator<T> getComparator(final Function<T, ? extends Comparable>[] properties) {
        if (properties == null || properties.length == 0) throw new IllegalArgumentException("properties may not be empty");

        Comparator<T> c = null;

        for (final Function<T, ? extends Comparable> f : properties) {
            Comparator<T> comp = (o1, o2) -> f.apply(o1).compareTo(f.apply(o2));
            if (c == null) c = comp;
            else c = c.thenComparing(comp);
        }
        return c;
    }

    public DomainList<T> sortList(Comparator<? super T> c) {

        super.sort(c);
        return this;
    }

    @Override
    public Stream<T> getStream() {
        return stream();
    }

    public DomainList<T> find(Predicate<T> expression) {
        return where(expression).toList(DomainList.class);
    }
}
