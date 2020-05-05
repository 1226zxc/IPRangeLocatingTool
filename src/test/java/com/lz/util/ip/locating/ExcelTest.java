package com.lz.util.ip.locating;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.lz.util.ip.locating.entity.Record;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;


public class ExcelTest {

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
    public void valid_excel() throws NoSuchFileException {
        new Excel(file, IP_SHEET_NAME, IP_RANGE_SHEET_NAME);
    }

    @Test
    public void listSheets() throws NoSuchFileException {
        List<Sheet> sheets = excel.listSheets();
        assertThat(sheets).isNotEmpty();
        assertThat(sheets.get(0).getSheetName()).isEqualTo("user");
        assertThat(sheets.get(1).getSheetName()).isEqualTo("institution");
    }

    @Test
    public void parse() throws Exception {
        Whitebox.invokeMethod(excel, "parse", IP_SHEET_NAME, IP_RANGE_SHEET_NAME);
        List<Sheet> sheets = Whitebox.getInternalState(excel, "sheets");
        assertThat(sheets.get(0).getSheetName()).isEqualTo("user");
        assertThat(sheets.get(1).getSheetName()).isEqualTo("institution");
    }


    @Test
    public void test_read_all() {
        ExcelReader reader = ExcelUtil.getReader(file);
        System.out.println(reader.readAll());
    }

    @Test
    public void write() {
        ExcelWriter writer = ExcelUtil.getWriter("results.xlsx", "result");
        Map<String, String> rows = new LinkedHashMap<>(3);
        rows.put("id", null);
        rows.put("name", null);
        rows.put("age", null);
        Map<String, String> rows2 = new LinkedHashMap<>(3);
        rows2.put("id", "123");
        rows2.put("name", "546");
        rows2.put("age", "123");
        List<Map<String, String>> results = new LinkedList<>();
        results.add(rows);
        results.add(rows2);
        writer.write(results);
        writer.flush();
        writer.close();
    }
}
