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
			prepare();
			if (dropTables) {
				dropAllTables();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void dropAllTables() throws SQLException {
		stmt.addBatch("DROP TABLE IF EXISTS " + DATASETS);
		stmt.addBatch("DROP TABLE IF EXISTS " + VARIABLES);
		stmt.executeBatch();

		String sql;
		sql = "CREATE TABLE " + DATASETS
				+ " (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, label TEXT NOT NULL)";
		stmt.addBatch(sql);
		sql = "CREATE TABLE " + VARIABLES + "(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,"
				+ "name TEXT NOT NULL, label TEXT NOT NULL, qstntext TEXT, id_dataset INTEGER NOT NULL)";
		stmt.addBatch(sql);
		stmt.executeBatch();

		conn.commit();
	}

	private void prepare() throws SQLException {
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

	public void write(Dataset dataset) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("INSERT INTO " + DATASETS + "  VALUES (?, ?);");

		ps.setString(2, dataset.getTitle());
		ps.addBatch();

		ps.executeBatch();

		// stmt.close();
		conn.commit();
		// conn.close();
	}

	public void write(Variable variable, int datasetID) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("INSERT INTO " + VARIABLES + "  VALUES (?, ?, ?, ?, ?);");

		ps.setString(2, variable.getName());
		ps.setString(3, variable.getLabel());
		ps.setString(4, variable.getQuestion());
		ps.setInt(5, datasetID);

		ps.addBatch();

		ps.executeBatch();

		// stmt.close();
		conn.commit();
		// conn.close();
	}

	public void printDatabases() throws SQLException {
		ResultSet rs = stmt.executeQuery("SELECT * FROM " + DATASETS + ";");
		while (rs.next()) {
			System.out.println("ID = " + rs.getInt("id"));
			System.out.println("Label = " + rs.getString("label"));
			System.out.println();
		}

		rs = stmt.executeQuery("SELECT * FROM " + VARIABLES + ";");
		while (rs.next()) {
			System.out.println("ID = " + rs.getInt("id"));
			System.out.println("Name = " + rs.getString("name"));
			System.out.println("Label = " + rs.getString("label"));
			System.out.println("Question = " + rs.getString("qstntext"));
			System.out.println();
		}
		rs.close();

		// stmt.close();
		conn.commit();
		// conn.close();
	}
}
