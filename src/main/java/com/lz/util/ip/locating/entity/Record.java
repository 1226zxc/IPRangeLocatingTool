package com.lz.util.ip.locating.entity;

import java.util.List;
import java.util.Objects;

public class Record {
    private List<Cell> cells;

    public Record(List<Cell> cells) {
        this.cells = cells;
    }

    public List<Cell> getCells() {
        return cells;
    }

    @Override
    public String toString() {
        return "Record{" +
                "cells=" + cells +
                '}';
    }
}
