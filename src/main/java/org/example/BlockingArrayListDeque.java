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
        synchronized (list) {
            return len;
        }
    }

    public int capacity() {
        // This does not require synchronization as the length of the storage list is never modified
        return list.size();
    }

    private int freeBackIndex() {
        synchronized (list) {
            return (start + len) % this.capacity();
        }
    }

    private int backIndex() {
        synchronized (list) {
            return (start + len - 1) % this.capacity();
        }
    }

    private int frontIndex() {
        synchronized (list) {
            return start;
        }
    }

    private int freeFrontIndex() {
        synchronized (list) {
            return (this.capacity() + start - 1) % this.capacity();
        }
    }

    ///  Returns true if this operation successfully pushed the value
    public boolean try_push_back(T item) {
        synchronized (list) {
            if (len >= this.capacity()) {
                return false;
            }
            list.set(this.freeBackIndex(), item);
            len++;
            list.notifyAll();
            return true;
        }
    }

    public void push_back(T item) throws InterruptedException {
        synchronized (list) {
            while (!this.try_push_back(item)) {
                list.wait();
            }
            list.notifyAll();
        }
    }

    ///  Returns true if this operation successfully pushed the value
    public boolean try_push_front(T item) {
        synchronized (list) {
            if (len >= this.capacity()) {
                return false;
            }
            start = this.freeFrontIndex();
            len++;
            list.set(start, item);
            list.notifyAll();
            return true;
        }
    }

    public void push_front(T item) throws InterruptedException {
        synchronized (list) {
            while (!this.try_push_front(item)) {
                list.wait();
            }
            list.notifyAll();
        }
    }

    public boolean isEmpty() {
        synchronized (list) {
            return len == 0;
        }
    }

    public Optional<T> try_pop_front() {
        synchronized (list) {
            if (this.isEmpty()) {
                return Optional.empty();
            }
            var value = list.get(this.frontIndex());
            len -= 1;
            start = (start + 1) % this.capacity();
            list.notifyAll();
            return Optional.of(value);
        }
    }

    public T pop_front() throws InterruptedException {
        synchronized (list) {
            while (true) {
                var maybe_pop = try_pop_front();
                if (maybe_pop.isEmpty()) {
                    list.wait();
                    continue;
                }
                return maybe_pop.get();
            }
        }
    }

    public Optional<T> try_pop_back() {
        synchronized (list) {
            if (this.isEmpty()) {
                return Optional.empty();
            }
            var value = list.get(this.backIndex());
            len -= 1;
            list.notifyAll();
            return Optional.of(value);
        }
    }

    public T pop_back() throws InterruptedException {
        synchronized (list) {
            while (true) {
                var maybe_pop = try_pop_back();
                if (maybe_pop.isEmpty()) {
                    list.wait();
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
