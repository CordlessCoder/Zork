package org.example;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

public class Matrix<T> implements Iterable<MatrixElement<T>> {
    private final ArrayList<T> storage;
    final int width;
    final int height;

    /// Creates a matrix with the given width and height, filling all elements with null.
    public Matrix(int width, int height) {
        this.width = width;
        this.height = height;
        this.storage = new ArrayList<>(width * height);
        for (int i = 0; i < width * height; i++) {
            this.storage.add(null);
        }
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    private void checkBounds(int row, int column) {
        if (row >= 0 && column >= 0 && row < height && column < width) {
            return;
        }
        throw new IndexOutOfBoundsException("Index (" + row + ", " + column + ") is out of bounds for matrix of size " + width + "x" + height);
    }

    public void set(int row, int column, T value) {
        checkBounds(row, column);
        storage.set(row * width + column, value);
    }

    public T get(int row, int column) {
        checkBounds(row, column);
        return storage.get(row * width + column);
    }

    // Discards all elements in the matrix and resizes it to the given width and height.
    public void resize(int width, int height) {
        storage.clear();
        if (this.width * this.height > width * height) {
            storage.trimToSize();
        }
        storage.ensureCapacity(width * height);
        for (int i = 0; i < width * height; i++) {
            this.storage.add(null);
        }
    }

    public List<T> row(int row) {
        if (row < 0 || row >= height) {
            throw new IndexOutOfBoundsException("Row " + row + " is out of bounds for matrix of size " + width + "x" + height);
        }
        return this.storage.subList(row * width, (row + 1) * width);
    }

    @Override
    public Iterator<MatrixElement<T>> iterator() {
        return new MatrixIterator<>(this);
    }

    public Stream<MatrixElement<T>> stream() {
        return java.util.stream.StreamSupport.stream(this.spliterator(), false);
    }
    public Stream<T> streamVal() {
        return java.util.stream.StreamSupport.stream(this.spliterator(), false).map(v -> v.value);
    }


    @Override
    public String toString() {
        StringBuilder data = new StringBuilder();
        for (int row = 0; row < height; row++) {
            var row_data = this.row(row);
            boolean first = true;
            for (var entry : row_data) {
                if (!first) {
                    data.append(", ");
                }
                first = false;
                data.append(entry);
            }
            data.append('\n');
        }
        return "Matrix{width=" + width + ", height=" + height + ", data=[\n" + data + "]}";
    }
}

class MatrixIterator<T> implements Iterator<MatrixElement<T>> {
    private final Matrix<T> matrix;
    private int row;
    private int column;

    public MatrixIterator(Matrix<T> matrix) {
        this.matrix = matrix;
        this.row = 0;
        this.column = 0;
    }

    @Override
    public boolean hasNext() {
        return this.column < matrix.width && this.row < matrix.height;
    }

    @Override
    public MatrixElement<T> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        var element = new MatrixElement<T>(row, column, null);
        element.value = matrix.get(row, column++);
        if (column >= matrix.width) {
            column = 0;
            row++;
        }
        return element;
    }
}