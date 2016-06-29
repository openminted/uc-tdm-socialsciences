package main;

import java.io.File;
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
		String docFolder = "R:\\DATA-SETS\\OpenMinTeD\\Variable Extraction\\Corpus";
		String goldDataFile = "H:\\OpenMinTeD\\WP9\\variable mentions\\ISSP_Religiosität.xlsx"; // TODO

		if (!dbExists(dbName)) {
			System.out.println("Database doesn't exist yet, have to read in study data...");
			StudyReader r = new StudyReader(new DBWriter(dbName, true));
			r.read(-1);
		}

		DBReader dbReader = new DBReader(dbName);
		Set<Dataset> datasets = dbReader.readData();
		System.out.println("Number of datasets: " + datasets.size());

		DocReader docReader = new DocReader(Paths.get(docFolder));
		Map<String, Document> documents = docReader.readDocuments();
		System.out.println("Number of documents: " + documents.size());

		GoldDataReader goldReader = new GoldDataReader();
		// TODO add path to constructor
		Set<GoldData> goldData = goldReader.read(new File(goldDataFile)); // TODO:
																			// read
																			// from
																			// multiple
																			// files?
		System.out.println("Number of gold data: " + goldData.size());
	}

	private static boolean dbExists(String db) {
		return Files.exists(Paths.get(db));
	}
}
