package util.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReader {

	public String simpleTextExtraction(File file) {
		String text = null;
		try (XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(file));
				XSSFExcelExtractor extractor = new XSSFExcelExtractor(wb)) {

			extractor.setFormulasNotResults(true);
			extractor.setIncludeSheetNames(false);
			text = extractor.getText();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return text;
	}

	public void read(File file) {
		try (XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(file))) {
			for (int i = 0; i < wb.getNumberOfSheets(); i++) {
				processSheet(wb.getSheetAt(i));
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void processSheet(XSSFSheet sheet) {
		System.out.println("Processing sheet: " + sheet.getSheetName());
		String[] labels = null;
		Cell cell;

		System.out.println("Number of rows: " + sheet.getPhysicalNumberOfRows());

		for (Row row : sheet) {
			Map<String, String> map = new TreeMap<String, String>();
			if (row.getRowNum() == 0) {
				labels = getLabels(row);
				for (String label : labels) {
					map.put(label, null);
				}
				continue;
			}

			for (int i = 0; i < labels.length; i++) {
				cell = row.getCell(i, Row.RETURN_BLANK_AS_NULL);

				if (cell == null) {
					continue;
				}

				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_STRING:
					System.out.println(cell.getRichStringCellValue().getString());
					break;
				default:
					System.err.println("Unexpected data type");
				}
			}
		}
	}

	private String[] getLabels(Row row) {
		String[] labels = new String[row.getLastCellNum()];

		int i = 0;
		for (Cell cell : row) {
			labels[i] = cell.getStringCellValue();
			i++;
		}

		for (String label : labels) {
			System.out.println(label);
		}
		return labels;
	}
}