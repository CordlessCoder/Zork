package org.example;

import java.util.ArrayList;
import java.util.Optional;

public class BlockingArrayListDeque<T> {
    private final ArrayList<T> list;
    private int len = 0;
    private int start = 0;

    public BlockingArrayListDeque(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        list = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) {
            list.add(null);
        }
    }

    public int size() {
        synchronized (this) {
            return len;
        }
    }

    public int capacity() {
        // This does not require synchronization as the length of the storage list is never modified
        return list.size();
    }

    private int freeBackIndex() {
        synchronized (this) {
            return (start + len) % this.capacity();
        }
    }

    private int backIndex() {
        synchronized (this) {
            return (start + len - 1) % this.capacity();
        }
    }

    private int frontIndex() {
        synchronized (this) {
            return start;
        }
    }

    private int freeFrontIndex() {
        synchronized (this) {
            return (this.capacity() + start - 1) % this.capacity();
        }
    }

    ///  Returns true if this operation successfully pushed the value
    public boolean try_push_back(T item) {
        synchronized (this) {
            if (len >= this.capacity()) {
                return false;
            }
            list.set(this.freeBackIndex(), item);
            len++;
            this.notifyAll();
            return true;
        }
    }

    public void push_back(T item) throws InterruptedException {
        synchronized (this) {
            while (!this.try_push_back(item)) {
                this.wait();
            }
            this.notifyAll();
        }
    }

    ///  Returns true if this operation successfully pushed the value
    public boolean try_push_front(T item) {
        synchronized (this) {
            if (len >= this.capacity()) {
                return false;
            }
            start = this.freeFrontIndex();
            len++;
            list.set(start, item);
            this.notifyAll();
            return true;
        }
    }

    public void push_front(T item) throws InterruptedException {
        synchronized (this) {
            while (!this.try_push_front(item)) {
                this.wait();
            }
            this.notifyAll();
        }
    }

    public boolean isEmpty() {
        synchronized (this) {
            return len == 0;
        }
    }

    public Optional<T> try_pop_front() {
        synchronized (this) {
            if (this.isEmpty()) {
                return Optional.empty();
            }
            var value = list.get(this.frontIndex());
            len -= 1;
            start = (start + 1) % this.capacity();
            this.notifyAll();
            return Optional.of(value);
        }
    }

    public T pop_front() throws InterruptedException {
        synchronized (this) {
            while (true) {
                var maybe_pop = try_pop_front();
                if (maybe_pop.isEmpty()) {
                    this.wait();
                    continue;
                }
                return maybe_pop.get();
            }
        }
    }

    public Optional<T> try_pop_back() {
        synchronized (this) {
            if (this.isEmpty()) {
                return Optional.empty();
            }
            var value = list.get(this.backIndex());
            len -= 1;
            this.notifyAll();
            return Optional.of(value);
        }
    }

    public T pop_back() throws InterruptedException {
        synchronized (this) {
            while (true) {
                var maybe_pop = try_pop_back();
                if (maybe_pop.isEmpty()) {
                    this.wait();
                    continue;
                }
                return maybe_pop.get();
            }
        }
    }

    @Override
    public String toString() {
        return "BlockingArrayListDeque{" + "list=" + list + ", len=" + len + ", start=" + start + '}';
    }
}
