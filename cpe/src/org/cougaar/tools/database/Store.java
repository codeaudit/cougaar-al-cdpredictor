package org.cougaar.tools.database;
import java.sql.*;

public class Store {
	public Store(){
		//main(null);
	}
	
	public static void main(String args[]) {
		try {
			/* Test loading driver */

			String driver = "com.mysql.jdbc.Driver";

			//System.out.println("\n=> loading driver:");
			Class.forName(driver).newInstance();
			//System.out.println("OK");

			/* Test the connection */

			String url = "jdbc:mysql://lb234f.ie.psu.edu/test";

			System.out.println("\n=> connecting:");
			Connection con = DriverManager.getConnection(url, "user", "class(2004)");
			System.out.println("CONNECT OK");

			Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery("Insert ");
			rs.afterLast();
			while (rs.previous()) {
				String string1, string2;
				string1 = rs.getString("String1");
				string2 = rs.getString("String2");
				System.out.println(string1 + "," + string2);
			}

		} catch (Exception x) {
			System.err.println(x);
		}
	}
	
	
}