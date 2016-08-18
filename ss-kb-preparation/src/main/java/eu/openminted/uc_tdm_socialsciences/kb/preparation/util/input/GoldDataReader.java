package eu.openminted.uc_tdm_socialsciences.kb.preparation.util.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import eu.openminted.uc_tdm_socialsciences.kb.preparation.util.output.DBManager;

/**
 * Gold data is in xlsx format. This class reads the data from the tables and
 * stores them in an appropriate format for later evaluation.
 *
 * @author neumanmy
 *
 */
public class GoldDataReader {

	private List<Path> toProcess;

	private DBManager writer;

	private static int VARIABLE;
	private static int PAPER = 4;
	private static int REFERENCE = 3;

	public GoldDataReader(Path root) {
		toProcess = new ArrayList<>();
		setRootDir(root);
	}

	private void setRootDir(Path root) {
		try {
			Files.walk(root).filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".xlsx"))
					.forEach(toProcess::add);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readData(DBManager dbManager) {
		this.writer = dbManager;

		for (Path path : toProcess) {
			System.out.println("Reading from path " + path);
			readData(path);
		}
	}

	private void readData(Path file) {
		try (XSSFWorkbook wb = new XSSFWorkbook(Files.newInputStream(file))) {
			for (int i = 0; i < wb.getNumberOfSheets(); i++) {
				processSheet(wb.getSheetAt(i));
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void processSheet(XSSFSheet sheet) {
		String sheetName = sheet.getSheetName();
		System.out.println("Processing sheet: " + sheetName);

		String datasetID = sheetName;

		Cell varCell, paperCell, refCell;
		Row row;

		String varRef = null, refText, paperRef;

		setLabels(sheet.getRow(0));

		for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
			row = sheet.getRow(i);

			varCell = row.getCell(VARIABLE, Row.CREATE_NULL_AS_BLANK);
			paperCell = row.getCell(PAPER, Row.RETURN_BLANK_AS_NULL);
			refCell = row.getCell(REFERENCE, Row.RETURN_BLANK_AS_NULL);

			if (!(varCell.getCellType() == Cell.CELL_TYPE_BLANK)) {
				varRef = varCell.getStringCellValue();
			}

			refText = refCell.getStringCellValue();
			paperRef = paperCell.getStringCellValue();

			writer.writeReference(varRef, paperRef, datasetID, refText);
		}
	}

	private static void setLabels(Row row) {
		for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
			switch (row.getCell(i).getStringCellValue()) {
			case "Variable":
				VARIABLE = i;
				break;
			case "Paper":
				PAPER = i;
				break;
			case "Reference":
				REFERENCE = i;
				break;
			}
		}
	}
}