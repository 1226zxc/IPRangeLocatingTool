package com.lz.util.ip.locating;

import cn.hutool.poi.excel.ExcelUtil;
import com.lz.util.ip.locating.entity.Record;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkSheetTest {
    private final static File file = new File("E:\\git\\IPRangeLocating\\IPRangeLocatingTool\\example-input.xlsx");;

    private static final String IP_SHEET_NAME = "user";

    private static final String IP_RANGE_SHEET_NAME = "institution";

    private static Excel excel;

    static {
        try {
            excel = new Excel(file, IP_SHEET_NAME, IP_RANGE_SHEET_NAME);
        } catch (NoSuchFileException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void valid_mandatory_column() {
        WorkSheet userSheet = new WorkSheet(ExcelUtil.getReader(file, "user"), "IP_Id", "IP");
        assertThat(userSheet.existMandatoryColumn()).isTrue();

        WorkSheet institutionSheet = new WorkSheet(ExcelUtil.getReader(file, "institution"), "IP_Range");
        assertThat(institutionSheet.existMandatoryColumn()).isTrue();
    }

    @Test
    public void listRecords() {
        WorkSheet userSheet = new WorkSheet(ExcelUtil.getReader(file, "user"), "IP_Id", "IP");
        List<Record> records = userSheet.listRecords();
        assert records != null;
        assertThat(records.size()).isEqualTo(7);
    }
}