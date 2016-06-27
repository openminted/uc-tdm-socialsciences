package util.input;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.sqlite.JDBC;

import datamodel.Dataset;

public class DBReader {

	private String dbURL;
	private Connection conn;
	private Statement stmt;

	private static final String DATASETS = "Datasets";
	private static final String VARIABLES = "Variables";

	// TODO: duplicated in reader and writer; maybe outsourcing
	private static final String DATASET_ID = "dataset_id";
	private static final String QSTNTEXT = "qstntext";
	private static final String NAME = "name";
	private static final String ID = "id";
	private static final String LABEL = "label";
	private static final String EXT_ID = "externalID";

	/**
	 * @param path
	 *            example path: "H:/OpenMinTeD/WP9/DatasetsVariables.sqlite"
	 * @param dropTables
	 *            specifies if all tables should be cleared
	 *
	 */
	public DBReader(String path) {
		dbURL = "jdbc:sqlite:" + path;

		try {
			connect();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	private void connect() throws SQLException {
		DriverManager.registerDriver(new JDBC());
		conn = DriverManager.getConnection(dbURL);

		System.out.println("Connected to the database");
		DatabaseMetaData dm = conn.getMetaData();
		System.out.println("Driver name: " + dm.getDriverName());
		System.out.println("Driver version: " + dm.getDriverVersion());
		System.out.println("Product name: " + dm.getDatabaseProductName());
		System.out.println("Product version: " + dm.getDatabaseProductVersion());
		System.out.println("--------------------\n");

		conn.setAutoCommit(false);
		stmt = conn.createStatement();
	}

	public Set<Dataset> readData() {
		Set<Dataset> result = new HashSet<>();
		// ...
		/*
		 * join tables
		 *
		 * read data
		 *
		 */

		String query = "SELECT Datasets.externalID, Datasets.label as dlabel, Variables.name, Variables.label as vlabel, "
				+ "Variables.qstntext from Variables join Datasets on Variables.dataset_id = Datasets.id";
		System.out.println(query);

		try {
			ResultSet rs = stmt.executeQuery(query);

			String extid, name, label, qstn;

			while (rs.next()) {
				extid = rs.getString("externalID");
				name = rs.getString("name");
				label = rs.getString("vlabel");
				qstn = rs.getString("qstntext");

				System.out.println(extid + " | " + name + " | " + label + " | " + qstn);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}
}
