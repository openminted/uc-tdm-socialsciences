package main;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import datamodel.Dataset;
import datamodel.Document;
import eval.GoldData;
import util.input.DBReader;
import util.input.DocReader;
import util.input.GoldDataReader;
import util.input.StudyReader;
import util.output.DBWriter;

public class Application {

	public static void main(String[] args) {

		String dbName = "studyData.sqlite";

		if (!dbExists(dbName)) {
			System.out.println("Database doesn't exist yet, have to read in study data...");
			StudyReader r = new StudyReader(DBWriter.getInstance(dbName, true));
			r.read(-1);
		}

		DBReader dbReader = new DBReader(dbName);
		Set<Dataset> datasets = dbReader.readData();
		System.out.println("Number of datasets: " + datasets.size());

		DocReader docReader = new DocReader(Paths.get(ProjectConstants.docFolder));
		Map<String, Document> documents = docReader.readDocuments();
		System.out.println("Number of documents: " + documents.size());

		GoldDataReader goldReader = new GoldDataReader(Paths.get(ProjectConstants.goldDataPath));
		Set<GoldData> goldData = goldReader.readData();

		System.out.println("Number of gold data: " + goldData.size());

		/*
		 * TODO: in GoldData weiß ich, welches Dokument welche Studie behandelt
		 * (indirekt). Dieses Wissen nutzen, um gezielt die Variablen aus der
		 * Studie im Dokument zu finden. Schlüssel: Dokumentname.
		 *
		 * Evtl. da auch noch was im StudyReader hinzufügen, was mir die
		 * Variablen on the fly holt, weil die DB wegen Heap Overflow nicht
		 * vollständig ist.
		 */
	}

	private static boolean dbExists(String db) {
		return Files.exists(Paths.get(db));
	}
}
