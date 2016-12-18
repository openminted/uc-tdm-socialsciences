package eu.openminted.uc.socialsciences.kb.preparation.main;

import java.nio.file.Paths;

import eu.openminted.uc.socialsciences.kb.preparation.util.input.DocReader;
import eu.openminted.uc.socialsciences.kb.preparation.util.input.GoldDataReader;
import eu.openminted.uc.socialsciences.kb.preparation.util.input.StudyReader;
import eu.openminted.uc.socialsciences.kb.preparation.util.output.DBManager;

public class DataPreparator {

	public static void main(String[] args) {

		// DBManager.getInstance(false).dropAllTables();

		// fillStudyDB();

		// fillDocumentsDB();

		fillLabeledDataDB();
	}

	/*
	 * read documents from PDF to text and into DB
	 */
	private static void fillDocumentsDB() {
		DocReader docReader = new DocReader(Paths.get(ProjectConstants.docFolder));
		docReader.readDocuments(DBManager.getInstance(false).createTables());
	}

	/*
	 * read labeled data from Excel (xlsx) into DB
	 */
	private static void fillLabeledDataDB() {
		GoldDataReader goldReader = new GoldDataReader(Paths.get(ProjectConstants.goldDataPath));
		goldReader.readData(DBManager.getInstance(false).createTables());
	}

	/*
	 * read study data from RDF into DB
	 */
	private static void fillStudyDB() {
		StudyReader studyReader = new StudyReader(DBManager.getInstance(false).createTables());
		studyReader.read(-1);
	}
}