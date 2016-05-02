package util;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

/**
 * Created by miroslav.kudlac on 4/18/2016.
 */
public class XLS {
    private HSSFWorkbook workbook;
    private HSSFSheet sheet;
    private File file;

    public XLS(String name, int rows, int cells) {
        this.file = new File(name);
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            workbook = new HSSFWorkbook(new FileInputStream(file));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        sheet = workbook.getSheetAt(0);
        for (int i = 0; i < rows; i++) {
            Row row = sheet.createRow(i);
            for (int a = 0; a < cells; a++) {
                row.createCell(a).setCellValue(0);
            }
        }
    }

    public void setCell(int x, int y, Object value) {
        Row row = sheet.getRow(x);
        if(row == null) {
            row = sheet.createRow(x);
        }
        Cell cell = row.getCell(y);
        if(cell == null) {
            cell = row.createCell(y);
        }
        cell.setCellValue(value.toString());
    }

    public void write() {
        try {
            FileOutputStream outFile = new FileOutputStream(file);
            workbook.write(outFile);
            outFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

