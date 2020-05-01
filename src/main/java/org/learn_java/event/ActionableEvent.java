package org.learn_java.event;

public interface ActionableEvent<T> {
    void handle(T t);
}
