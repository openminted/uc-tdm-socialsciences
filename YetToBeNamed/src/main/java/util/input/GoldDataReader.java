package util.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import eval.GoldData;

/**
 * Gold data is in xlsx format. This class reads the data from the tables and
 * stores them in an appropriate format for later evaluation.
 *
 * @author neumanmy
 *
 */
public class GoldDataReader {

	static final int VARIABLE = 0;
	static final int PAPER = 4;
	static final int REFERENCE = 3;

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

	public Set<GoldData> read(File file) {
		Set<GoldData> result = new HashSet<GoldData>();
		try (XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(file))) {
			for (int i = 0; i < wb.getNumberOfSheets(); i++) {
				result.add(processSheet(wb.getSheetAt(i)));
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return result;
	}

	private GoldData processSheet(XSSFSheet sheet) {
		String sheetName = sheet.getSheetName();
		System.out.println("Processing sheet: " + sheetName);

		String datasetID = sheetName;

		String[] labels = null;

		// System.out.println("Number of rows: " +
		// sheet.getPhysicalNumberOfRows());

		GoldData gold = null;
		for (Row row : sheet) {
			if (row.getRowNum() == 0) {
				labels = getLabels(row);
				continue;
			}

			gold = new GoldData();
			gold.setDatasetID(datasetID);

			Cell varCell = row.getCell(VARIABLE);
			Cell paperCell = row.getCell(PAPER);
			Cell refCell = row.getCell(REFERENCE);

			if (!(varCell.getCellType() == Cell.CELL_TYPE_STRING && paperCell.getCellType() == Cell.CELL_TYPE_STRING
					&& refCell.getCellType() == Cell.CELL_TYPE_STRING)) {
				System.err.println("Wrong cell type in row, skipping row...");
				continue;
			}

			String varRef = varCell.getRichStringCellValue().getString();
			String refText = refCell.getRichStringCellValue().getString();
			String paperRef = paperCell.getRichStringCellValue().getString();

			gold.addRef(varRef, refText, paperRef);
		}

		return gold;
	}

	private String[] getLabels(Row row) {
		String[] labels = new String[row.getLastCellNum()];

		int i = 0;
		for (Cell cell : row) {
			labels[i] = cell.getStringCellValue();
			i++;
		}

		return labels;
	}
}