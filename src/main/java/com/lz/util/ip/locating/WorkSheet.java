package com.lz.util.ip.locating;

import cn.hutool.poi.excel.ExcelReader;
import com.lz.util.ip.locating.entity.Cell;
import com.lz.util.ip.locating.entity.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class WorkSheet {
    private final List<String> mandatoryFieldName;

    private final ExcelReader reader;

    public static final Logger logger = LoggerFactory.getLogger(Excel.class);

    public WorkSheet(ExcelReader reader, String... mandatoryField) {
        this.reader = reader;
        this.mandatoryFieldName = new ArrayList<>(mandatoryField.length);
        this.mandatoryFieldName.addAll(Arrays.asList(mandatoryField));
    }

    public boolean existMandatoryColumn() {
        List<List<Object>> ipHeader = reader.read(0, 0);
        for (List<Object> record : ipHeader) {
            List<Object> requiredFields = record.stream().filter(mandatoryFieldName::contains).collect(Collectors.toList());
            if (requiredFields.size() != mandatoryFieldName.size()) {
                logger.error("the column of {} is mandatory,and each of them existence must be only once," +
                        " please check the count of them!", mandatoryFieldName);
                return false;
            }
        }
        return true;
    }

    public List<Record> listRecords() {
        List<Map<String, Object>> rows = reader.readAll();
        return rows.stream().map(row -> {
            List<Cell> cells = new ArrayList<>(row.size());
            row.forEach((column, value) -> cells.add(new Cell(column, value.toString())));
            return new Record(cells);
        }).collect(Collectors.toList());
    }

    public List<String> getMandatoryFieldName() {
        return mandatoryFieldName;
    }
}
