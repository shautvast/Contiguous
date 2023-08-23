package com.github.shautvast.contiguous;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * Base class with all the methods that will not be implemented
 * Only purpose: reduce linecount in the subclass.
 *
 * @param <E>
 */
public abstract class NotImplementedList<E> implements List<E> {

    @Override
    public boolean contains(Object o) {
        throw new RuntimeException("Not implemented");
        // is possible, but not feasible: iterate, create instance, then compare ??
    }

    @Override
    public Spliterator<E> spliterator() {
        return List.super.spliterator();
        // not sure about this yet
    }

    @Override
    public boolean remove(Object o) {
        throw new RuntimeException("Not yet implemented");
        // is possible, but not feasible: iterate, create instance, then compare ??
        // and then removing, which requires moving a lot of bytes
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public boolean containsAll(Collection<?> collection) {
        throw new RuntimeException("Not yet implemented");
        // is possible, but not feasible: iterate, create instance, then compare ??
    }

    @Override
    public boolean addAll(int i, Collection<? extends E> collection) {
        throw new RuntimeException("Not implemented");
        // is possible, but not feasible because it would require moving a lot of bytes
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        throw new RuntimeException("Not implemented");
        // is possible, but not feasible because it would require moving a lot of bytes
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new RuntimeException("Not implemented");
        // is possible, but not feasible because it would require moving a lot of bytes
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new RuntimeException("Not implemented");
        // is possible, but not feasible because it would require moving a lot of bytes
    }

    @Override
    public E set(int i, E e) {
        throw new RuntimeException("Not implemented");
        // is possible, but not feasible because it would require moving a lot of bytes
    }

    @Override
    public void add(int i, E e) {
        throw new RuntimeException("Not implemented");
        // is possible, but not feasible because it would require moving a lot of bytes
    }

    @Override
    public E remove(int i) {
        throw new RuntimeException("Not implemented");
        // is possible, but not feasible because it would require moving a lot of bytes
    }

    @Override
    public int indexOf(Object o) {
        throw new RuntimeException("Not implemented");
        // is possible, but not feasible: iterate, create instance, then compare ??
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new RuntimeException("Not implemented");
        // is possible, but not feasible
    }

    @Override
    public ListIterator<E> listIterator() {
        throw new RuntimeException("Not implemented");
        // ListIterator contains remove, so not possible
    }

    @Override
    public ListIterator<E> listIterator(int i) {
        throw new RuntimeException("Not yet implemented");
        // ListIterator contains remove, so not possible
    }

    @Override
    public void sort(Comparator<? super E> c) {
        throw new RuntimeException("Not implemented");
        // an immutable sort would be an option
    }

    @Override
    public List<E> subList(int i, int i1) {
        throw new RuntimeException("Not yet implemented");
        // possible
    }
}
