package eu.openminted.uc.socialsciences.kb.preparation.util.output;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import eu.openminted.uc.socialsciences.kb.preparation.datamodel.Dataset;
import eu.openminted.uc.socialsciences.kb.preparation.datamodel.Variable;
import eu.openminted.uc.socialsciences.kb.preparation.util.Property;
import org.apache.log4j.Logger;

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

	private static final Logger logger = Logger.getLogger(DBManager.class);

	private DBManager(Connection c) {
		this.conn = c;
	}

	public static DBManager getInstance(boolean test) {
		// return instance if it is not null and the connection is alive
		try {
			if (instance != null) {
				if (instance.getConn() != null && !instance.getConn().isClosed()) {
					return instance;
				}
			}
		} catch (SQLException e) {
			logger.error("SQLException occured while trying to get DBManager instance.", e);
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
			autoincrement = "AUTOINCREMENT";
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
			logger.error(cnfe.getMessage(), cnfe);
		}

		try {
			c = DriverManager.getConnection(url, username, password);
			logger.info("Connected to the database");
			DatabaseMetaData dm = c.getMetaData();
			logger.info("Driver name: " + dm.getDriverName());
			logger.info("Driver version: " + dm.getDriverVersion());
			logger.info("Product name: " + dm.getDatabaseProductName());
			logger.info("Product version: " + dm.getDatabaseProductVersion());
			logger.info("--------------------\n");

		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}

		return c;
	}

	public DBManager dropAllTables() {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.addBatch("DROP TABLE IF EXISTS " + REFERENCES);
			stmt.addBatch("DROP TABLE IF EXISTS " + DOCUMENTS);
			stmt.addBatch("DROP TABLE IF EXISTS " + VARIABLES);
			stmt.addBatch("DROP TABLE IF EXISTS " + DATASETS);
			stmt.executeBatch();

		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			close(stmt);
		}
		return instance;
	}

	public DBManager createTables() {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			if (conn.getMetaData().getDatabaseProductName().equals("SQLite")) {
				stmt.addBatch("PRAGMA foreign_keys = ON");
			}
			stmt.addBatch(
					"CREATE TABLE IF NOT EXISTS " + DATASETS + " (" + ID + " INTEGER NOT NULL PRIMARY KEY UNIQUE "
							+ autoincrement + ", "
							+ TITLE + " VARCHAR(255) NOT NULL, " + EXT_ID + " VARCHAR(20) NOT NULL UNIQUE)");
			stmt.addBatch("CREATE TABLE IF NOT EXISTS " + VARIABLES + " (" + ID + " INTEGER NOT NULL PRIMARY KEY "
					+ autoincrement + ", " + NAME + " TEXT NOT NULL, " + LABEL + " TEXT NOT NULL, " + QSTNTEXT
					+ " TEXT, " + DATASET_ID + " VARCHAR(20) NOT NULL, FOREIGN KEY (" + DATASET_ID + ") REFERENCES "
					+ DATASETS + "(" + EXT_ID + "))");
			stmt.addBatch("CREATE TABLE IF NOT EXISTS " + DOCUMENTS + " (" + ID + " INTEGER NOT NULL PRIMARY KEY "
					+ autoincrement + ", " + NAME + " VARCHAR(6) NOT NULL UNIQUE, " + TEXT + " LONGTEXT NOT NULL)");
			stmt.addBatch("CREATE TABLE IF NOT EXISTS " + REFERENCES + " (" + ID + " INTEGER NOT NULL PRIMARY KEY "
					+ autoincrement + ", " + DOC_ID + " INTEGER NOT NULL, " + STUDY_ID + " INTEGER NOT NULL, "
					+ VAR_ID
					+ " INTEGER NOT NULL, " + REFTEXT + " MEDIUMTEXT NOT NULL, FOREIGN KEY (" + DOC_ID + ") REFERENCES "
					+ DOCUMENTS + "(" + ID + "), FOREIGN KEY (" + STUDY_ID + ") REFERENCES " + DATASETS + "(" + ID
					+ "), FOREIGN KEY (" + VAR_ID + ") REFERENCES " + VARIABLES + "(" + ID + "))");

			stmt.executeBatch();

		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			close(stmt);
		}

		return instance;
	}

	public void write(Dataset dataset) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("INSERT IGNORE INTO " + DATASETS + " (" + TITLE + ", " + EXT_ID
					+ ")  VALUES (?, ?);");
			// ps.setInt(1, dataset.getId());
			ps.setString(1, dataset.getTitle());
			ps.setString(2, dataset.getExternalID());
			ps.addBatch();

			ps.executeBatch();

		} catch (SQLException e) {
			logger.error("Error executing statement: " + ps);
			logger.error(e.getMessage(), e);
		} finally {
			close(ps);
		}
	}

	public void write(Variable variable, String datasetID) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(
					"INSERT IGNORE INTO " + VARIABLES + "  (" + NAME + ", " + LABEL + ", " + QSTNTEXT + ", "
					+ DATASET_ID + ") VALUES (?, ?, ?, ?);");
			ps.setString(1, variable.getName());
			ps.setString(2, variable.getLabel());
			ps.setString(3, variable.getQuestion());
			ps.setString(4, datasetID);

			ps.addBatch();

			ps.executeBatch();
		} catch (SQLException e) {
			logger.error("Error executing statement: " + ps);
			logger.error(e.getMessage(), e);
		} finally {
			close(ps);
		}
	}

	public void writeReference(String varRef, String paperRef, String datasetID, String refText) {
		PreparedStatement ps = null;
		try {

			ps = conn.prepareStatement(
					"INSERT IGNORE INTO " + REFERENCES + " (" + DOC_ID + ", " + STUDY_ID + ", " + VAR_ID
					+ ", " + REFTEXT + ") VALUES (?, ?, ?, ?);");

			/*
			 * for dataset: get from datasets where externalID = datasetID
			 * for doc: get from documents where name = paperRef
			 * for var: get from variables where name = varRef && dataset_id =
			 * foreignStudy
			 */

			String sql = "SELECT " + ID + " FROM " + DATASETS + " WHERE " + EXT_ID + "='" + datasetID + "'";
			ResultSet rs = conn.createStatement().executeQuery(sql);
			if (rs.isClosed()) {
				close(ps);
				return;
			}
			if (!rs.next()) {
				return;
			}
			int foreignStudy = rs.getInt(1);

			sql = "SELECT " + ID + " FROM " + DOCUMENTS + " WHERE " + NAME + "='" + paperRef + "'";
			rs = conn.createStatement().executeQuery(sql);
			if (rs.isClosed()) {
				close(ps);
				return;
			}
			if (!rs.next()) {
				return;
			}
			int foreignPaper = rs.getInt(1);

			sql = "SELECT " + ID + " FROM " + VARIABLES + " WHERE " + NAME + "='" + varRef + "' and " + DATASET_ID
					+ "='"
					+ datasetID + "'";
			rs = conn.createStatement().executeQuery(sql);
			if (rs.isClosed()) {
				close(ps);
				return;
			}
			if (!rs.next()) {
				return;
			}
			int foreignVar = rs.getInt(1);

			ps.setInt(1, foreignPaper);
			ps.setInt(2, foreignStudy);
			ps.setInt(3, foreignVar);
			ps.setString(4, refText);

			ps.addBatch();

			// logger.info("Executing sql: " + ps);
			ps.executeBatch();

			close(ps);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			close(ps);
		}
	}

	public void writeDocument(String docName, String docText) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(
					"INSERT IGNORE INTO " + DOCUMENTS + " (" + NAME + ", " + TEXT + ") VALUES (?, ?);");

			ps.setString(1, docName);
			ps.setString(2, docText);
			ps.addBatch();

			ps.executeBatch();

		} catch (SQLException e) {
			logger.error(e.getMessage());
		} finally {
			close(ps);
		}
	}

	public ResultSet query(final String sqlQuery) {
		ResultSet rs = null;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlQuery);

		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			close(stmt);
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
			logger.error(e.getMessage(), e);
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
			logger.error(e.getMessage(), e);
		}

		try {
			rs.close();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}

		if (closeConnection) {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public Connection getConn() {
		return conn;
	}
}
