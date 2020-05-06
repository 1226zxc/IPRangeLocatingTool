package com.lz.util.ip.locating;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.lz.util.ip.locating.entity.Cell;
import com.lz.util.ip.locating.entity.Record;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class EntryPoint {
    private final String excelPath;

    private final String ipSheetName;

    private final String ipRangeSheetName;

    public static final String IP_ID = "IP_Id";

    public static final String IP = "IP";

    public static final String IP_RANGE = "IP_Range";

    public static final String RESULT_OUTPUT_FILE_PATH = "results.xlsx";


    public static final Logger logger = LoggerFactory.getLogger(EntryPoint.class);

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please specify file name(only support *.xlsx):");
        String excel = sc.nextLine();
        System.out.println("Please specify sheet name which contains Ip that to be executed:");
        String ipSheet = sc.nextLine();
        System.out.println("Please specify sheet name which contains RangIp:");
        String rangeIpSheet = sc.nextLine();

        if (StrUtil.isEmpty(excel) || StrUtil.isEmpty(ipSheet) || StrUtil.isEmpty(rangeIpSheet)) {
            logger.error("invalid input!");
            System.exit(1);
        }
        new EntryPoint(excel, ipSheet, rangeIpSheet).execute();
    }

    public EntryPoint(String excel, String ipSheet, String rangeIpSheet) {
        this.excelPath = excel;
        ipSheetName = ipSheet;
        ipRangeSheetName = rangeIpSheet;
    }

    public void execute() {
        File file = new File(excelPath);;
        WorkSheet userSheet = new WorkSheet(ExcelUtil.getReader(file, ipSheetName), IP_ID, IP);
        WorkSheet institutionSheet = new WorkSheet(ExcelUtil.getReader(file, ipRangeSheetName), IP_RANGE);
        if (!userSheet.existMandatoryColumn() || !institutionSheet.existMandatoryColumn()) {
            System.exit(1);
        }

        Map<Record, List<String>> results = getResults(userSheet, institutionSheet);

        List<Map<String, String>> outputRecords = listOutputRecords(results);
        writeRecords(outputRecords);
    }

    private Map<Record, List<String>> getResults(WorkSheet userSheet, WorkSheet institutionSheet) {
        List<Record> institutionRecords = institutionSheet.listRecords();
        Map<Record, List<String>> results = new HashMap<>(institutionRecords.size());
        institutionRecords.forEach(institutionRecord -> {
            List<String> institutionMandatory = institutionSheet.getMandatoryFieldName();
            List<Cell> institutionCells = institutionRecord.getCells();
            List<Cell> institutionMandatoryCells = institutionCells.stream()
                    .filter(cell -> institutionMandatory.contains(cell.getColumn()))
                    .collect(Collectors.toList());
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
            // ips that hit the range
            List<String> hits = listHits(userSheet, ipRange);
            results.put(institutionRecord, hits);
        });
        return results;
    }

    private List<String> listHits(WorkSheet userSheet, String[] ipRange) {
        List<String> results = new LinkedList<>();
        List<Record> userRecords = userSheet.listRecords();
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
            results.add(ipId.get(0).getContent());
        });
        return results;
    }

    private List<Map<String, String>> listOutputRecords(Map<Record, List<String>> rangeToIpIds) {
        List<Map<String, String>> results = new LinkedList<>();
        rangeToIpIds.forEach((key, ids) -> {
            Map<String, String> record = new LinkedHashMap<>(2);
            List<Cell> cells = key.getCells();
            cells.forEach(cell -> record.put(cell.getColumn(), cell.getContent()));
            record.put(IP_ID, ids.toString().replace("[", "").replace("]", ""));
            record.put("Count", String.valueOf(ids.size()));
            results.add(record);
        });
        return results;
    }


    private void writeRecords(List<Map<String, String>> records) {
        ExcelWriter writer = ExcelUtil.getWriter(RESULT_OUTPUT_FILE_PATH, "result");
        writer.getCellStyle().setAlignment(HorizontalAlignment.LEFT);
        writer.write(records, true);
        writer.flush();
        writer.close();
        logger.info("execution done!");
    }

    private static boolean isInner(long userIp, long begin, long end) {
        return (userIp >= begin) && (userIp <= end);
    }

}
