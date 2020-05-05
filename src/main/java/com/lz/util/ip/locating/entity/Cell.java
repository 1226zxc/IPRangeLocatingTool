package com.lz.util.ip.locating.entity;

import java.util.Objects;

public class Cell {
    private String column;

    private String content;

    public Cell(String column, String content) {
        this.column = column;
        this.content = content;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cell)) return false;
        Cell cell = (Cell) o;
        return Objects.equals(column, cell.column) &&
                Objects.equals(content, cell.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(column, content);
    }

    @Override
    public String toString() {
        return "Cell{" +
                "column='" + column + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
