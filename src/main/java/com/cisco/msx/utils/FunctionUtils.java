/*
 * Copyright (c) 2021. Cisco Systems, Inc and its affiliates
 * All Rights reserved
 */

package com.cisco.msx.utils;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * FunctionUtils
 *
 * @author Livan Du
 * Created on 2018-03-14
 */
public class FunctionUtils {

    private FunctionUtils() {}

    public static Optional<Integer> findOrder(Object obj) {
        if (obj instanceof Ordered) {
            return Optional.of(((Ordered) obj).getOrder());
        }
        if (obj != null) {
            Class<?> clazz = (obj instanceof Class ? (Class<?>) obj : obj.getClass());
            Order order = AnnotationUtils.findAnnotation(clazz, Order.class);
            if (order != null) {
                return Optional.of(order.value());
            }
        }
        return Optional.empty();
    }

    /**
     * Create a {@link Comparator} that put ordered object first (unordered object count as lowest precedence)
     *
     * An object is ordered if:
     *  - it implements {@link Ordered} interface
     *  - it's annotated with {@link Order}
     *
     * @param <T>
     * @return
     */
    public static <T> Comparator<T> orderedFirst() {
        return (o1, o2) -> {
            int o1Order = findOrder(o1).orElse(Ordered.LOWEST_PRECEDENCE);
            int o2Order = findOrder(o2).orElse(Ordered.LOWEST_PRECEDENCE);
            return Integer.compare(o1Order, o2Order);
        };
    }

    /**
     * Create {@link Comparator} that reverse the orderedFirst()
     *
     * @param <T>
     * @return
     */
    public static <T> Comparator<T> reversedOrderedFirst() {
        return Collections.reverseOrder(orderedFirst());
    }

    /**
     * Create a {@link Comparator} that put ordered object last (unordered object count as highest precedence)
     *
     * An object is ordered if:
     *  - it implements {@link Ordered} interface
     *  - it's annotated with {@link Order}
     *
     * @param <T>
     * @return
     */
    public static <T> Comparator<T> orderedLast() {
        return (o1, o2) -> {
            int o1Order = findOrder(o1).orElse(Ordered.HIGHEST_PRECEDENCE);
            int o2Order = findOrder(o2).orElse(Ordered.HIGHEST_PRECEDENCE);
            return Integer.compare(o1Order, o2Order);
        };
    }

    /**
     * Create {@link Comparator} that reverse the orderedLast()
     *
     * @param <T>
     * @return
     */
    public static <T> Comparator<T> reversedOrderedLast() {
        return Collections.reverseOrder(orderedLast());
    }

    /**
     * Convenient Map {@link Collector} that collect stream of {@link Entry} with give map supplier.
     *
     * @param mapFactory
     * @param <T> the Entry type
     * @param <K> the key type
     * @param <U> the value type
     * @param <M> the collected Map type
     */
    public static <T extends Entry<K, U>, K, U, M extends Map<K, U>>
    Collector<T, ?, M> entryToMapCollector(Supplier<M> mapFactory) {
        BinaryOperator<U> mergeFunction = (oldVal, newVal) -> newVal;
        return Collectors.toMap(Entry::getKey, Entry::getValue, mergeFunction, mapFactory);
    }

    /**
     * Convenient {@link Predicate} that used by filtering stream of objects with distinct property
     * If property is null, the object is filtered
     *
     * @param propertyExtractor a function to extract to-be-compared property
     * @param <T> stream content type
     */
    public static <T> Predicate<T> distinctByProperty(Function<? super T, ?> propertyExtractor) {

        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> {
            Object value = propertyExtractor.apply(t);
            if (value == null) {
                return false;
            }
            return seen.putIfAbsent(value, Boolean.TRUE) == null;
        };
    }

    /**
     * Create wrapper on a checked exception throwing {@link Consumer}, for stream to use
     *
     * @param throwingConsumer
     * @return A consumer that wrap check exception into RuntimeException
     */
    @SuppressWarnings("squid:S00112")
    public static <T> Consumer<T> safe(Consumer<T> throwingConsumer) {
        return t -> {
            try {
                throwingConsumer.accept(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Create wrapper on a checked exception throwing {@link Function}, for stream to use
     *
     * @param throwingFunction
     * @return A function that wrap check exception into RuntimeException
     */
    @SuppressWarnings("squid:S00112")
    public static <T, R> Function<T, R> safe(Function<T, R> throwingFunction) {
        return t -> {
            try {
                return throwingFunction.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

}
