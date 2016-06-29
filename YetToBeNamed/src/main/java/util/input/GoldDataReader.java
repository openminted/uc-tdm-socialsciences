package util.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
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

	private List<Path> toProcess;

	private static int VARIABLE;
	private static int PAPER = 4;
	private static int REFERENCE = 3;

	public GoldDataReader(Path root) {
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

	public Set<GoldData> readData() {
		Set<GoldData> result = new HashSet<GoldData>();
		for (Path path : toProcess) {
			result.addAll(readData(path));
		}
		return result;
	}

	private Set<GoldData> readData(Path file) {
		Set<GoldData> result = new HashSet<GoldData>();
		try (XSSFWorkbook wb = new XSSFWorkbook(Files.newInputStream(file))) {
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

		GoldData gold = null;
		Cell varCell, paperCell, refCell;
		Row row;

		String varRef = null, refText, paperRef;

		setLabels(sheet.getRow(0));

		gold = new GoldData();
		gold.setDatasetID(datasetID);

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

			gold.addRef(varRef, refText, paperRef);
		}

		return gold;
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