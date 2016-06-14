package util.output;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import org.sqlite.JDBC;

import datamodel.Dataset;

public class DBWriter {

	private String dbURL;

	public DBWriter(String path) {
		// example path: "H:/OpenMinTeD/WP9/DatasetsVariables.sqlite"
		dbURL = "jdbc:sqlite:" + path;
	}

	// TODO adapt
	public void write(Set<Dataset> datasets) {
		try {
			DriverManager.registerDriver(new JDBC());
			Connection conn = DriverManager.getConnection(dbURL);

			Statement stmt = null;

			if (conn != null) {
				System.out.println("Connected to the database");
				DatabaseMetaData dm = conn.getMetaData();
				System.out.println("Driver name: " + dm.getDriverName());
				System.out.println("Driver version: " + dm.getDriverVersion());
				System.out.println("Product name: " + dm.getDatabaseProductName());
				System.out.println("Product version: " + dm.getDatabaseProductVersion());

				stmt = conn.createStatement();

				// String sql = "INSERT INTO Datasets
				// (id,label,language,categories) "
				// + "VALUES (2, 'test', 'en', 'test1;test2');";

				String sql = "DROP TABLE IF EXISTS COMPANY";
				stmt.executeUpdate(sql);

				sql = "CREATE TABLE COMPANY " + "(ID INT PRIMARY KEY     NOT NULL,"
						+ " NAME           TEXT    NOT NULL, " + " AGE            INT     NOT NULL, "
						+ " ADDRESS        CHAR(50), " + " SALARY         REAL)";
				stmt.executeUpdate(sql);

				sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
						+ "VALUES (1, 'Paul', 32, 'California', 20000.00 );";
				stmt.executeUpdate(sql);

				sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
						+ "VALUES (2, 'Allen', 25, 'Texas', 15000.00 );";
				stmt.executeUpdate(sql);

				sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
						+ "VALUES (3, 'Teddy', 23, 'Norway', 20000.00 );";
				stmt.executeUpdate(sql);

				sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
						+ "VALUES (4, 'Mark', 25, 'Rich-Mond ', 65000.00 );";
				stmt.executeUpdate(sql);

				PreparedStatement ps = conn.prepareStatement("INSERT INTO COMPANY VALUES (?, ?, ?, ?, ?);");

				ps.setInt(1, 5);
				ps.setString(2, "Peter");
				ps.setInt(3, 35);
				ps.setString(4, "Poland");
				ps.setDouble(5, 80000.00);
				ps.addBatch();

				conn.setAutoCommit(false);
				ps.executeBatch();
				conn.setAutoCommit(true);

				ResultSet rs = stmt.executeQuery("SELECT * FROM COMPANY;");
				while (rs.next()) {
					System.out.println("Name = " + rs.getString("NAME"));
					System.out.println("Age = " + rs.getInt("AGE"));
					System.out.println("Address = " + rs.getString("ADDRESS"));
					System.out.println("Salary = " + rs.getDouble("SALARY"));
					System.out.println();
				}
				rs.close();

				stmt.close();
				// conn.commit();
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}
