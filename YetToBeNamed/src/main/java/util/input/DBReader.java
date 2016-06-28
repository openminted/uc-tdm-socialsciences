package util.input;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sqlite.JDBC;

import datamodel.Dataset;
import datamodel.Variable;

public class DBReader {

	private String dbURL;
	private Connection conn;
	private Statement stmt;

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

		Map<Dataset, Set<Variable>> temp = new HashMap<>();

		String query = "SELECT Datasets.externalID, Datasets.title, Variables.name, Variables.label, "
				+ "Variables.qstntext from Variables join Datasets on Variables.dataset_id = Datasets.id";
		System.out.println(query);

		try {
			ResultSet rs = stmt.executeQuery(query);

			String extid, name, label, qstn, title;

			Dataset ds;
			Variable var;

			while (rs.next()) {
				extid = rs.getString("externalID");
				name = rs.getString("name");
				label = rs.getString("label");
				qstn = rs.getString("qstntext");
				title = rs.getString("title");

				ds = new Dataset(extid);
				ds.setTitle(title);

				temp.putIfAbsent(ds, new HashSet<Variable>());

				var = new Variable();
				var.setName(name);
				var.setLabel(label);
				var.setQuestion(qstn);

				temp.get(ds).add(var);
			}

			for (Dataset dataset : temp.keySet()) {
				dataset.addVariables(temp.get(dataset));
				result.add(dataset);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}
}
