package main;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import datamodel.Document;
import eval.GoldData;
import eval.Reference;
import util.input.DocReader;
import util.input.GoldDataReader;
import util.input.StudyReader;
import util.output.DBManager;

public class DataPreparator {

	public static void main(String[] args) {

		/*
		 * read study data from RDF into DB
		 */
		StudyReader studyReader = new StudyReader(DBManager.getInstance(false));
		studyReader.read(-1);

		/*
		 * read labeled data from Excel (xlsx) into DB
		 */
		GoldDataReader goldReader = new GoldDataReader(Paths.get(ProjectConstants.goldDataPath));
		Set<GoldData> goldData = goldReader.readData();
		System.out.println("Number of gold data: " + goldData.size());

		/*
		 * read documents from PDF to text and into DB
		 */
		DocReader docReader = new DocReader(Paths.get(ProjectConstants.docFolder));
		Map<String, Document> documents = docReader.readDocuments();
		System.out.println("Number of documents: " + documents.size());


		for (GoldData labeled : goldData) {
			String datasetID = labeled.getDatasetID();
			Map<String, List<Reference>> references = labeled.getReferences();

			studyReader.readDataset(datasetID);

			for (Entry<String, List<Reference>> varRefs : references.entrySet()) {
				String varName = varRefs.getKey();

				// TODO: query db for var

				List<Reference> refs = varRefs.getValue();
				for (Reference reference : refs) {
					reference.getPaperID();
					reference.getReferenceText();
				}
			}
		}
	}
}