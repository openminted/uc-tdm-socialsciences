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
import eval.GoldData;
import util.Property;

public class DBManager {
	private Connection conn;

	private static DBManager instance;

	/*
	 * field names
	 */
	private static final String REFTEXT = "reftext";
	private static final String VAR_ID = "var_id";
	private static final String STUDY_ID = "study_id";
	private static final String DOC_ID = "doc_id";
	private static final String TITLE = "title";
	private static final String EXT_ID = "externalID";
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String LABEL = "label";
	private static final String QSTNTEXT = "qstntext";
	private static final String DATASET_ID = "dataset_id";
	private static final String TEXT = "text";

	/*
	 * table names
	 */
	private static final String DATASETS = "datasets";
	private static final String VARIABLES = "variables";
	private static final String REFERENCES = "varrefs";
	private static final String DOCUMENTS = "documents";

	/*
	 * keywords
	 */
	private static String autoincrement;

	private DBManager(Connection c) {
		this.conn = c;
	}

	/**
	 *
	 *
	 */
	public static DBManager getInstance(boolean test) {
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
		Connection c = connect(test);

		instance = new DBManager(c);

		return instance;
	}

	private static Connection connect(boolean test) {
		Connection c = null;

		String driver, url, username = null, password = null;

		if (test) {
			driver = "org.sqlite.JDBC";
			url = "jdbc:sqlite:test.sqlite";
		} else {
			String connectionType = Property.load("type.connection");
			String dbType = Property.load(connectionType + ".type");

			String prefix = connectionType + "." + dbType;
			driver = Property.load(prefix + ".driver");
			url = Property.load(prefix + ".url");
			username = Property.load(prefix + ".username");
			password = Property.load(prefix + ".password");
			autoincrement = Property.load(prefix + ".autoincrement");
		}

		try {
			Class.forName(driver);
		} catch (ClassNotFoundException cnfe) {
			System.err.println("Couldn't find driver class:");
			cnfe.printStackTrace();
		}

		try {
			c = DriverManager.getConnection(url, username, password);
			System.out.println("Connected to the database");
			DatabaseMetaData dm = c.getMetaData();
			System.out.println("Driver name: " + dm.getDriverName());
			System.out.println("Driver version: " + dm.getDriverVersion());
			System.out.println("Product name: " + dm.getDatabaseProductName());
			System.out.println("Product version: " + dm.getDatabaseProductVersion());
			System.out.println("--------------------\n");

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return c;
	}

	public DBManager dropAllTables() {
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.addBatch("DROP TABLE IF EXISTS " + DATASETS);
			stmt.addBatch("DROP TABLE IF EXISTS " + VARIABLES);
			stmt.addBatch("DROP TABLE IF EXISTS " + REFERENCES);
			stmt.addBatch("DROP TABLE IF EXISTS " + DOCUMENTS);
			stmt.executeBatch();

			close(stmt);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return instance;
	}

	public DBManager createTables() {
		Statement stmt;
		try {
			stmt = conn.createStatement();
			if (conn.getMetaData().getDatabaseProductName().equals("SQLite")) {
				stmt.addBatch("PRAGMA foreign_keys = ON");
			}
			stmt.addBatch(
					"CREATE TABLE IF NOT EXISTS " + DATASETS + " (" + ID + " INTEGER NOT NULL PRIMARY KEY UNIQUE, "
							+ TITLE + " VARCHAR(255) NOT NULL UNIQUE, " + EXT_ID + " TEXT NOT NULL)");
			stmt.addBatch("CREATE TABLE IF NOT EXISTS " + VARIABLES + " (" + ID
					+ " INTEGER NOT NULL PRIMARY KEY UNIQUE " + autoincrement + ", " + NAME + " TEXT NOT NULL, " + LABEL
					+ " TEXT NOT NULL, " + QSTNTEXT + " TEXT, " + DATASET_ID + " INTEGER NOT NULL, FOREIGN KEY ("
					+ DATASET_ID + ") REFERENCES " + DATASETS + "(" + ID + "))");
			stmt.addBatch(
					"CREATE TABLE IF NOT EXISTS " + DOCUMENTS + " (" + ID + " INTEGER NOT NULL PRIMARY KEY UNIQUE "
							+ autoincrement + ", " + NAME + " TEXT NOT NULL, " + TEXT + " VARCHAR(255) NOT NULL)");
			stmt.addBatch("CREATE TABLE IF NOT EXISTS " + REFERENCES + " (" + ID
					+ " INTEGER NOT NULL PRIMARY KEY UNIQUE " + autoincrement + ", " + DOC_ID + " INTEGER NOT NULL, "
					+ STUDY_ID + " INTEGER NOT NULL, " + VAR_ID + " INTEGER NOT NULL, " + REFTEXT
					+ " VARCHAR(255) NOT NULL, FOREIGN KEY (" + DOC_ID + ") REFERENCES " + DOCUMENTS + "(" + ID
					+ "), FOREIGN KEY (" + STUDY_ID + ") REFERENCES " + DATASETS + "(" + ID + "), FOREIGN KEY ("
					+ VAR_ID + ") REFERENCES " + VARIABLES + "(" + ID + "))");

			stmt.executeBatch();

			close(stmt);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return instance;
	}

	public void write(Dataset dataset) {
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement("INSERT OR IGNORE INTO " + DATASETS + " (" + ID + ", " + TITLE + ", " + EXT_ID
					+ ")  VALUES (?, ?, ?);");
			ps.setInt(1, dataset.getId());
			ps.setString(2, dataset.getTitle());
			ps.setString(3, dataset.getExternalID());
			ps.addBatch();

			ps.executeBatch();

			close(ps);
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

			close(ps);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	public void writeReference(GoldData ref) {
		PreparedStatement ps;
		try {
			// TODO
			ps = conn.prepareStatement("INSERT INTO " + REFERENCES + " (" + ") VALUES (?, ?, ?, ?);");

			ps.addBatch();

			ps.executeBatch();

			close(ps);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	public void writeDocument(String docName, String docText) {
		PreparedStatement ps;
		try {
			// TODO
			ps = conn.prepareStatement("INSERT INTO " + DOCUMENTS + " (" + ") VALUES (?, ?, ?, ?);");

			ps.addBatch();

			ps.executeBatch();

			close(ps);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	public ResultSet query(String sqlQuery) {
		ResultSet rs = null;

		try {
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}

	public void close(Statement stmt) {
		if (stmt == null) {
			return;
		}

		try {
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void close(ResultSet rs, boolean closeConnection) {
		if (rs == null) {
			return;
		}

		// close statement
		try {
			Statement stmt = rs.getStatement();

			if (stmt != null) {
				stmt.close();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (closeConnection) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public Connection getConn() {
		return conn;
	}
}
