package dev.byblos.core.stacklang;

import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Stream;

public record Stack(List<Object> items) implements Iterable<Object> {
    private static final Stack EMPTY = new Stack(List.of());

    public static Stack of(Object... items) {
        if (items.length == 0) {
            return EMPTY;
        }
        return new Stack(List.of(items));
    }

    public Stack reverse() {
        return new Stack(Lists.reverse(items));
    }

    public Stack push(Object item) {
        return new Stack(Stream.concat(Stream.of(item), items.stream()).toList());
    }

    public Stack pushRight(Object item) {
        return new Stack(Stream.concat(items.stream(), Stream.of(item)).toList());
    }

    public Stack popAndPush(int n, Object item) {
        return new Stack(Stream.concat(Stream.of(item), items.stream().skip(n)).toList());
    }

    public Stack drop(int n) {
        return new Stack(items.stream().skip(n).toList());
    }

    public Stack dropRight(int n) {
        return new Stack(items.stream().limit(items.size() - n).toList());
    }

    public Stack popAndPush(Object item) {
        return popAndPush(1, item);
    }

    public Stack concat(Stack other) {
        return new Stack(Stream.concat(stream(), other.stream()).toList());
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public boolean nonEmpty() {
        return !items.isEmpty();
    }

    public int size() {
        return items.size();
    }

    public Object get(int n) {
        var item = items.get(n);
        if (null == item) {
            throw new NoSuchElementException();
        }
        return item;
    }

    public Stream<Object> stream() {
        return items.stream();
    }

    @SafeVarargs
    public final boolean matches(Predicate<Object>... predicates) {
        if (predicates.length > items.size()) {
            return false;
        }
        for (var i = 0; i < predicates.length; i++) {
            if (!predicates[i].test(items.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Iterator<Object> iterator() {
        return items.iterator();
    }
}
