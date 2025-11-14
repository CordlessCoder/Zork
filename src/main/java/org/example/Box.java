package org.example;

public class Box<T> {
    public T inner;

    public Box(T inner) {
        this.inner = inner;
    }

    public T get() {
        return inner;
    }

    public void set(T inner) {
        this.inner = inner;
    }

    public T replace(T inner) {
        T old = this.inner;
        this.inner = inner;
        return old;
    }

    @Override
    public String toString() {
        return "Box(" + inner + ')';
    }
}
