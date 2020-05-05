package com.lz.util.ip.locating;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.poi.excel.ExcelUtil;
import com.lz.util.ip.locating.entity.Cell;
import com.lz.util.ip.locating.entity.Record;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class EntryPointTest {
    private final static File file = new File("E:\\git\\IPRangeLocating\\IPRangeLocatingTool\\example-input.xlsx");;

    private static final String IP_SHEET_NAME = "user";

    private static final String IP_RANGE_SHEET_NAME = "institution";
    public static final String IP_ID = "IP_Id";
    public static final String IP = "IP";
    public static final String IP_RANGE = "IP_Range";

    private static Excel excel;

    public static final Logger logger = LoggerFactory.getLogger(EntryPointTest.class);


    static {
        try {
            excel = new Excel(file, IP_SHEET_NAME, IP_RANGE_SHEET_NAME);
        } catch (NoSuchFileException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void listOutputRecords() throws Exception {
        Map<String, List<String>> results = new HashMap<>(3);
        results.put("192.168.1.1-192.168.1.5", Arrays.asList("Lily", "Mark"));
        results.put("192.168.1.6-192.168.1.10", Arrays.asList("Jane", "John"));
        results.put("10.10.168.6-10.10.168.254", Arrays.asList("Ben"));

        EntryPoint entryPoint = new EntryPoint("example-input.xlsx", IP_SHEET_NAME, IP_RANGE_SHEET_NAME);
        List<Map<String, String>> outputResults = Whitebox.invokeMethod(entryPoint, "listOutputRecords", results);
        assertThat(outputResults).isNotEmpty();
        assertThat(outputResults).hasSize(4);
    }

    @Test
    public void writeRecords() throws Exception {
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

        EntryPoint entryPoint = new EntryPoint("example-input.xlsx", IP_SHEET_NAME, IP_RANGE_SHEET_NAME);
        Whitebox.invokeMethod(entryPoint, "writeRecords", results);
        assertThat(new File("results.xlsx")).exists();
    }

    @Test
    public void execute() throws NoSuchFileException {
        Excel excel = new Excel(file, IP_SHEET_NAME, IP_RANGE_SHEET_NAME);
        WorkSheet userSheet = new WorkSheet(ExcelUtil.getReader(file, IP_SHEET_NAME), IP_ID, IP);
        WorkSheet institutionSheet = new WorkSheet(ExcelUtil.getReader(file, IP_RANGE_SHEET_NAME), IP_RANGE);
        if (!userSheet.existMandatoryColumn() || !institutionSheet.existMandatoryColumn()) {
            System.exit(1);
        }
        List<Record> userRecords = userSheet.listRecords();
        List<Record> institutionRecords = institutionSheet.listRecords();

        Map<String, List<String>> results = new HashMap<>(institutionRecords.size());
        institutionRecords.forEach(institutionRecord -> {
            List<String> institutionMandatory = institutionSheet.getMandatoryFieldName();
            List<Cell> institutionCells = institutionRecord.getCells();
            List<Cell> institutionMandatoryCells = institutionCells.stream().filter(cell -> institutionMandatory.contains(cell.getColumn())).collect(Collectors.toList());
            if (institutionMandatoryCells.isEmpty()) {
                logger.error("record:{} has an empty ip range", institutionRecord);
                return;
            }
            Cell range = institutionMandatoryCells.get(0);
            String[] ipRange = range.getContent().split("-");
            if (ipRange.length != 2) {
                logger.error("record:{} has an invalid ip range,the correct one is 'ip-ip'", institutionRecord);
                return;
            }
            List<String> ipIds = new LinkedList<>();
            userRecords.forEach(record -> {
                List<Cell> cells = record.getCells();
                List<String> fieldName = userSheet.getMandatoryFieldName();
                List<Cell> ipId = cells.stream().filter(cell -> IP_ID.equals(cell.getColumn())).collect(Collectors.toList());
                List<Cell> ip = cells.stream().filter(cell -> IP.equals(cell.getColumn())).collect(Collectors.toList());
                if (ip.isEmpty() || ipId.isEmpty()) {
                    logger.error("record:{} missing mandatory field, they are{}'", record, fieldName);
                    return;
                }
                boolean hit = isInner(NetUtil.ipv4ToLong(ip.get(0).getContent())
                        , NetUtil.ipv4ToLong(ipRange[0])
                        , NetUtil.ipv4ToLong(ipRange[1]));
                if (!hit) {
                    return;
                }
                ipIds.add(ipId.get(0).getContent());
            });
            results.put(range.getContent(), ipIds);
        });
        System.out.println(results);
    }

    private static boolean isInner(long userIp, long begin, long end) {
        return (userIp >= begin) && (userIp <= end);
    }
}
