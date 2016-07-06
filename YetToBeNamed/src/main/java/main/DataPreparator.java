package main;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import datamodel.Document;
import eval.GoldData;
import util.input.DocReader;
import util.input.GoldDataReader;
import util.input.StudyReader;
import util.output.DBManager;

public class DataPreparator {

	public static void main(String[] args) {

		fillStudyDB();

		fillLabeledDataDB();

		fillDocumentsDB();
		/*
		 * TODO: in GoldData weiß ich, welches Dokument welche Studie behandelt
		 * (indirekt). Dieses Wissen nutzen, um gezielt die Variablen aus der
		 * Studie im Dokument zu finden. Schlüssel: Dokumentname.
		 *
		 * alternativer Workflow:
		 *
		 * erst gelabelte Daten einlesen
		 *
		 * parallel die Dokumente einlesen
		 *
		 * für jede neue Dokument ID zu jeder Studien ID die Studie und ihre
		 * Variablen holen und in DB schreiben
		 *
		 * oder, da die vollständige DB ja nun da ist, diese nutzen
		 *
		 * jeweils über IDs Verknüpfungen herstellen
		 */
	}

	/*
	 * TODO read documents from PDF to text and into DB
	 */
	private static void fillDocumentsDB() {
		DocReader docReader = new DocReader(Paths.get(ProjectConstants.docFolder));
		Map<String, Document> documents = docReader.readDocuments();
		System.out.println("Number of documents: " + documents.size());

	}

	/*
	 * TODO read labeled data from Excel (xlsx) into DB
	 */
	private static void fillLabeledDataDB() {

		GoldDataReader goldReader = new GoldDataReader(Paths.get(ProjectConstants.goldDataPath));
		Set<GoldData> goldData = goldReader.readData();
		System.out.println("Number of gold data: " + goldData.size());
	}

	/*
	 * read study data from RDF into DB
	 */
	private static void fillStudyDB() {
		StudyReader studyReader = new StudyReader(DBManager.getInstance(false).createTables());
		studyReader.read(-1);
	}
}