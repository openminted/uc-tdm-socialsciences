package main;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import datamodel.Dataset;
import datamodel.Document;
import eval.GoldData;
import util.input.DocReader;
import util.input.GoldDataReader;
import util.output.DBManager;

public class Application {

	public static void main(String[] args) {

		Set<Dataset> datasets = DBManager.getInstance(false).readData();
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
		 *
		 * neuer Workflow:
		 *
		 * erst gelabelte Daten einlesen
		 *
		 * parallel die Dokumente einlesen
		 *
		 * für jede neue Dokument ID zu jeder Studien ID die Studie und ihre
		 * Variablen holen und in DB schreiben
		 *
		 * jeweils über IDs Verknüpfungen herstellen
		 */
	}
}
