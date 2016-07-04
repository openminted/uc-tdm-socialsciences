package util.output;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import datamodel.Dataset;
import datamodel.Variable;
import util.Property;

public class DBWriter {

	private static final String TITLE = "title";
	private static final String EXT_ID = "externalID";

	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String LABEL = "label";
	private static final String QSTNTEXT = "qstntext";
	private static final String DATASET_ID = "dataset_id";

	private Connection conn;

	private static DBWriter instance;

	private static final String DATASETS = "datasets";
	private static final String VARIABLES = "variables";

	public DBWriter(Connection c) {
		this.conn = c;
	}

	/**
	 * @param path
	 *            example path: "H:/OpenMinTeD/WP9/DatasetsVariables.sqlite"
	 * @param dropTables
	 *            specifies if all tables should be cleared
	 *
	 */
	public static DBWriter getInstance(String path, boolean dropTables) {
		// return instance if it is not null and the connection is alive
		try {
			if (instance != null) {
				if (instance.getConn() != null && !instance.getConn().isClosed()) {
					return instance;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// create new connection and instance for it
		Connection c = connect();

		instance = new DBWriter(c);

		if (dropTables) {
			instance.dropAllTables();
		}
		instance.createTables();

		return instance;
	}

	private static Connection connect() {
		String connectionType = Property.load("type.connection");
		String dbType = Property.load(connectionType + ".type");

		String prefix = connectionType + "." + dbType;
		String driver = Property.load(prefix + ".driver");
		String url = Property.load(prefix + ".url");
		String username = Property.load(prefix + ".username");
		String password = Property.load(prefix + ".password");

		try {
			Class.forName(driver);
		} catch (ClassNotFoundException cnfe) {
			System.err.println("Couldn't find driver class:");
			cnfe.printStackTrace();
		}

		Connection c = null;

		try {
			c = DriverManager.getConnection(url, username, password);
			System.out.println("Connected to the database");
			DatabaseMetaData dm = c.getMetaData();
			System.out.println("Driver name: " + dm.getDriverName());
			System.out.println("Driver version: " + dm.getDriverVersion());
			System.out.println("Product name: " + dm.getDatabaseProductName());
			System.out.println("Product version: " + dm.getDatabaseProductVersion());
			System.out.println("--------------------\n");

			c.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return c;
	}

	private void dropAllTables() {
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.addBatch("DROP TABLE IF EXISTS " + DATASETS);
			stmt.addBatch("DROP TABLE IF EXISTS " + VARIABLES);
			stmt.executeBatch();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void createTables() {
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.addBatch(
					"CREATE TABLE IF NOT EXISTS " + DATASETS + " (" + ID + " INTEGER NOT NULL PRIMARY KEY UNIQUE, "
							+ TITLE + " VARCHAR(255) NOT NULL UNIQUE, " + EXT_ID + " TEXT NOT NULL)");
			stmt.addBatch("CREATE TABLE IF NOT EXISTS " + VARIABLES
					+ " (id INTEGER NOT NULL PRIMARY KEY UNIQUE AUTO_INCREMENT, " + NAME + " TEXT NOT NULL, " + LABEL
					+ " TEXT NOT NULL, " + QSTNTEXT + " TEXT, " + DATASET_ID + " INTEGER NOT NULL)");
			stmt.executeBatch();

			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void write(Dataset dataset) {
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(
					"INSERT INTO " + DATASETS + " (" + ID + ", " + TITLE + ", " + EXT_ID + ")  VALUES (?, ?, ?);");
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

			conn.commit();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	public void printDatabases() {
		ResultSet rs;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM " + DATASETS + ";");
			while (rs.next()) {
				System.out.println("ID = " + rs.getInt(ID));
				System.out.println("Title = " + rs.getString(TITLE));
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

			conn.commit();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	public Connection getConn() {
		return conn;
	}
}
