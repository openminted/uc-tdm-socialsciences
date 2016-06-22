package util.output;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.sqlite.JDBC;

import datamodel.Dataset;
import datamodel.Variable;

public class DBWriter {

	private static final String DATASET_ID = "dataset_id";
	private static final String QSTNTEXT = "qstntext";
	private static final String NAME = "name";
	private static final String ID = "id";
	private static final String LABEL = "label";
	private static final String EXT_ID = "externalID";
	private String dbURL;
	private Connection conn;
	private Statement stmt;

	private static final String DATASETS = "Datasets";
	private static final String VARIABLES = "Variables";

	/**
	 * @param path
	 *            example path: "H:/OpenMinTeD/WP9/DatasetsVariables.sqlite"
	 * @param dropTables
	 *            specifies if all tables should be cleared
	 *
	 */
	public DBWriter(String path, boolean dropTables) {
		dbURL = "jdbc:sqlite:" + path;

		try {
			connect();
			if (dropTables) {
				dropAllTables();
			}
			createTables();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	private void dropAllTables() throws SQLException {
		stmt.addBatch("DROP TABLE IF EXISTS " + DATASETS);
		stmt.addBatch("DROP TABLE IF EXISTS " + VARIABLES);
		stmt.executeBatch();
		conn.commit();
	}

	private void createTables() throws SQLException {
		stmt.addBatch("CREATE TABLE IF NOT EXISTS " + DATASETS + " (" + ID + " INTEGER NOT NULL PRIMARY KEY UNIQUE, "
				+ LABEL + " TEXT NOT NULL UNIQUE, " + EXT_ID + " TEXT NOT NULL)");/*  */
		stmt.addBatch("CREATE TABLE IF NOT EXISTS " + VARIABLES
				+ "(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," + NAME + " TEXT NOT NULL, " + LABEL
				+ " TEXT NOT NULL, " + QSTNTEXT + " TEXT, " + DATASET_ID + " INTEGER NOT NULL)");
		stmt.executeBatch();

		conn.commit();
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

	public void write(Dataset dataset) {
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(
					"INSERT INTO " + DATASETS + " (" + ID + ", " + LABEL + ", " + EXT_ID + ")  VALUES (?, ?, ?);");
			ps.setInt(1, dataset.getId());
			ps.setString(2, dataset.getTitle());
			ps.setString(3, dataset.getExternalID());
			ps.addBatch();

			ps.executeBatch();

			conn.commit();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	public void write(Variable variable, int datasetID) {
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement("INSERT INTO " + VARIABLES + "  (" + NAME + ", " + LABEL + ", " + QSTNTEXT + ", "
					+ DATASET_ID + ") VALUES (?, ?, ?, ?);");
			ps.setString(1, variable.getName());
			ps.setString(2, variable.getLabel());
			ps.setString(3, variable.getQuestion());
			ps.setInt(4, datasetID);

			ps.addBatch();

			ps.executeBatch();

			// stmt.close();
			conn.commit();
			// conn.close();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	public void printDatabases() {
		ResultSet rs;
		try {
			rs = stmt.executeQuery("SELECT * FROM " + DATASETS + ";");
			while (rs.next()) {
				System.out.println("ID = " + rs.getInt(ID));
				System.out.println("Label = " + rs.getString(LABEL));
				System.out.println();
			}

			rs = stmt.executeQuery("SELECT * FROM " + VARIABLES + ";");
			while (rs.next()) {
				System.out.println("ID = " + rs.getInt(ID));
				System.out.println("Name = " + rs.getString(NAME));
				System.out.println("Label = " + rs.getString(LABEL));
				System.out.println("Question = " + rs.getString(QSTNTEXT));
				System.out.println("Dataset ID = " + rs.getInt(DATASET_ID));
				System.out.println();
			}
			rs.close();

			// stmt.close();
			conn.commit();
			// conn.close();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}
}
