package org.jprelude.core.util.function;

@FunctionalInterface
public interface TriPredicate<A1, A2, A3> {
    boolean test(A1 a1, A2 a2, A3 a3);
}
