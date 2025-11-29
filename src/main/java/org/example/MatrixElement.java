package org.example;

public class MatrixElement<T> {
    private final int row, column;
    public T value;

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    MatrixElement(int row, int column, T value) {
        this.row = row;
        this.column = column;
        this.value = value;
    }

    @Override
    public String toString() {
        return "MatrixElement{" +
                "row=" + row +
                ", column=" + column +
                ", value=" + value +
                '}';
    }
}
