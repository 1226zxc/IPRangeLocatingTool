package com.lz.util.ip.locating;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelFileUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.Collections;
import java.util.List;

public class Excel {

    private final boolean valid;

    private File file;

    public static final Logger logger = LoggerFactory.getLogger(Excel.class);

    private List<Sheet> sheets;

    private ExcelReader ipReader;

    private ExcelReader ipRangeReader;

    public Excel(File source, String IpSheet, String IpRangeSheet) throws NoSuchFileException {
        this.file = source;
        this.valid = valid();
        parse(IpSheet, IpRangeSheet);
    }

    private boolean valid() throws NoSuchFileException {
        if (FileUtil.pathEndsWith(file, "xlsx")) {
            return ExcelFileUtil.isXlsx(FileUtil.getInputStream(file.toPath()));
        }
        String msg = StrUtil.format("the suffix of {} is not support!", FileUtil.extName(this.file));
        logger.error(msg);
        throw new NoSuchFileException(this.file.getPath(),  msg, "only support the suffix of *.xlsx excel currently!");
    }

    public List<Sheet> listSheets() {
        if (!valid) {
            return Collections.emptyList();
        }
        return this.sheets;
    }

    private void parse(String ipSheet, String ipRangeSheet) {
        ipReader = ExcelUtil.getReader(file, ipSheet);
        ipRangeReader = ExcelUtil.getReader(file, ipRangeSheet);
        this.sheets = ipRangeReader.getSheets();
    }
}
