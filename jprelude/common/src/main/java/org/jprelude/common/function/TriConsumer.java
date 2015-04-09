package org.jprelude.common.function;

@FunctionalInterface
public interface TriConsumer<A1, A2, A3> {
    void accept(A1 a1, A2 a2, A3 a3);
}