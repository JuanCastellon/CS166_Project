/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		while(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup
	static int MAX_doc_id = 249;
	static int MAX_pat_id = 249;
	static int MAX_apt_id = 549;
//	static int MAX_dept_id = 124;
	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Doctor");
				System.out.println("2. Add Patient");
				System.out.println("3. Add Appointment");
				System.out.println("4. Make an Appointment");
				System.out.println("5. List appointments of a given doctor and date range");
				System.out.println("6. List all available appointments of a given department");
				System.out.println("7. List total number of different types of appointments per doctor in descending order");
				System.out.println("8. Find total number of patients per doctor with a given status");
				System.out.println("9. < EXIT");
				
				switch (readChoice()){
					case 1: AddDoctor(esql); break;
					case 2: AddPatient(esql); break;
					case 3: AddAppointment(esql); break;
					case 4: MakeAppointment(esql); break;
					case 5: ListAppointmentsOfDoctor(esql); break;
					case 6: ListAvailableAppointmentsOfDepartment(esql); break;
					case 7: ListStatusNumberOfAppointmentsPerDoctor(esql); break;
					case 8: FindPatientsCountWithStatus(esql); break;
					case 9: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	public static void AddDoctor(DBproject esql) {//1
		String input = "";
		int number;
		try {
			String query = "INSERT INTO Doctor (doctor_ID, name, specialty, did) VALUES (\'";
			do {System.out.print("\tEnter new doctor's id: ");
			    input = in.readLine();
			    number  = Integer.parseInt(input);
			} while (number < MAX_doc_id);
			query += (input + "\', \'");
			System.out.print("\tEnter new doctor's name: ");
			input = in.readLine();
			query += (input + "\', \'");
			System.out.print("\tEnter new doctor's specialty: ");
			input = in.readLine();
			query += (input + "\', \'");
			System.out.print("\tEnter new doctor's department id: ");
				input = in.readLine();
//				number = Integer.parseInt(input);
//			} while (number < MAX_dept_id);
			query += (input + "\');");

			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
			MAX_doc_id++;
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
// need to test
	public static void AddPatient(DBproject esql) {//2
		String input =  "";
		int number;
		try {
			String query = "INSERT INTO Patient (patient_ID, name, gtype, age, address, number_of_appts) VALUES (\'";
			do{
				System.out.print("\tEnter new patient's id: ");
				input = in.readLine();
				number = Integer.parseInt(input);
			}while(number < MAX_pat_id);
			query += (input + "\', \'");
			System.out.print("\tEnter new patient's name: ");
			input = in.readLine();
			query += (input + "\', \'");
			System.out.print("\tEnter new patient's gender: ");
			input = in.readLine();
			query += (input + "\', \'");
			do{
				System.out.print("\tEnter new patient's number of appointments: ");
				input = in.readLine();
				number = Integer.parseInt(input);
			}while(number < 0);
			query += (input + "\');");
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
			MAX_pat_id++;
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
// need to test
	public static void AddAppointment(DBproject esql) {//3
		String input = "";
		int number;
		try {
			String query = "INSERT INTO Appointment (appnt_ID , adate, time_slot, status) VALUES (\'";
			do{
				System.out.print("\tEnter new appointment's id: ");
				input = in.readLine();
				number = Integer.parseInt(input);
			}while(number < MAX_apt_id);
			query += (input + "\', \'");
			System.out.print("\tEnter new appointment's date (MM/DD/YYYY): ");
			input = in.readLine();
			query += (input + "\', \'");
			System.out.print("\tEnter new appointment's time slot (HH:MM-HH:MM): ");
			input = in.readLine();
			query += (input + "\', \'");
			do{
				System.out.print("\tEnter new appointment's status (AV, AC, PA, WA): ");
				input = in.readLine();
			}while(input != "AV" || input != "AC" || input != "PA" || input != "WA");
			query += (input + "\');");
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
			MAX_apt_id++;
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}


	public static void MakeAppointment(DBproject esql) {//4
		// Given a patient, a doctor and an appointment of the doctor that s/he wants to take, add an appointment to the DB
		// First, we have to find out the status from appointment table. If the status is available/active, insert/update tuples 
		// in patient, has_appointment tables, and change the status (available -> active, active -> waitlisted) in appointment 
		// table. If the status is waitlisted, update/insert tuples in patient and had_appointment tables. For the past status, we have nothing to do.
		try {
			System.out.print("\tEnter patient ID of patient who wants to make an appointment: " );
                        String pat_id = in.readLine();
                        System.out.print("\tEnter doctor ID of doctor patient wants to make an appointment with: ");
                        String doc_id = in.readLine();
                        System.out.print("\tEnter appointment ID of appointment the patient wants: ");
                        String appt_id = in.readLine();
                        String query = "SELECT A.status FROM Appointment A, has_appointment H, Doctor D, searches S, Patient P WHERE P.patient_ID = " + pat_id + " AND D.doctor_ID = " + doc_id + " AND A.appnt_ID = " + appt_id + " ANDP.patient_ID = S.pid AND S.aid = A.appnt_ID AND A.appnt_ID = H.appt_id AND H.doctor_id = D.doctor_ID AND appnt_ID = \'" + appt_id + "\' AND A.status = \'WL\';";
                        int status = esql.executeQueryAndPrintResult(query);
			String query2 = "";
                        if (status != 0) {
                                // appt wl and update patient, has_appt
				query2 = "UPDATE Patient SET number_of_appts = (number_of_appts + 1)";
				status = esql.executeQueryAndPrintResult(query);
				query2 = "INSERT INTO has_appt(appt_id, doctor_id) VALUES (\'" + appt_id + "\', \'" + doc_id + "\';)"; 
				status = esql.executeQueryAndPrintResult(query);
				System.out.println("total row(s): " + status);
				System.out.print("Updated to waitlist\n");
			}
                        query = "SELECT A.status FROM Appointment A, has_appointment H, Doctor D, searches S, Patient P WHERE P.patient_ID = " + pat_id + " AND D.doctor_ID = " + doc_id + " AND A.appnt_ID = " + appt_id + " ANDP.patient_ID = S.pid AND S.aid = A.appnt_ID AND A.appnt_ID = H.appt_id AND H.doctor_id = D.doctor_ID AND appnt_ID = \'" + appt_id + "\' AND A.status = \'AC\';";
                        status = esql.executeQueryAndPrintResult(query);
                        if (status != 0) {
                                // appt ac and update patient, has_appt, appt (status ac -> wl)
				query2 = "UPDATE Appointment SET status = \'WL\'";
				status = esql.executeQueryAndPrintResult(query);
				query2 = "UPDATE Patient SET number_of_appts = (number_of_appts + 1)";
				status = esql.executeQueryAndPrintResult(query);
				query2 = "INSERT INTO has_appt(appt_id, doctor_id) VALUES (\'" + appt_id + "\', \'" + doc_id + "\';)"; 
				status = esql.executeQueryAndPrintResult(query);
				System.out.println("total row(s): " + status);
				System.out.print("Updated to waitlist\n");

			}
                        query = "SELECT A.status FROM Appointment A, has_appointment H, Doctor D, searches S, Patient P WHERE P.patient_ID = " + pat_id + " AND D.doctor_ID = " + doc_id + " AND A.appnt_ID = " + appt_id + " ANDP.patient_ID = S.pid AND S.aid = A.appnt_ID AND A.appnt_ID = H.appt_id AND H.doctor_id = D.doctor_ID AND appnt_ID = \'" + appt_id + "\' AND A.status = \'AV\';";
                        status = esql.executeQueryAndPrintResult(query);
                        if (status != 0) {
                                // appt exists and add update patient, has_appt, appt (status av -> ac)
				query2 = "UPDATE Appointment SET status = \'AC\'";
				status = esql.executeQueryAndPrintResult(query);
				query2 = "UPDATE Patient SET number_of_appts = (number_of_appts + 1)";
				status = esql.executeQueryAndPrintResult(query);
				query2 = "INSERT INTO has_appt(appt_id, doctor_id) VALUES (\'" + appt_id + "\', \'" + doc_id + "\';)"; 
				status = esql.executeQueryAndPrintResult(query);
				System.out.println("total row(s): " + status);
				System.out.print("Updated to active\n");
			}
			else { System.out.print("No updates\n"); }
			
			
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}

	}

	public static void ListAppointmentsOfDoctor(DBproject esql) {//5
		// For a doctor ID and a date range, find the list of active and available appointments of the doctor
		try {
			String query = "SELECT * FROM Appointment, has_appointment WHERE appnt_ID = appt_id AND (status = \'AC\' OR status = \'AV\') AND doctor_id = \'";
			System.out.print("\tEnter doctor id: ");
			String input = in.readLine();
			query += input;
			query += "\' AND (adate BETWEEN \'"; 
			System.out.print("\tEnter first date of date range of the appt (MM/DD/YYYY): ");
			input = in.readLine();
			query += (input + "\' AND \'");
			System.out.print("\tEnter second date of date range of the appt (MM/DD/YYYY): ");
			input = in.readLine();
			query += (input + "\');");
						
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println ("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void ListAvailableAppointmentsOfDepartment(DBproject esql) {//6
		// For a department name and a specific date, find the list of available appointments of the department
		try {
			String query = "SELECT * FROM Appointment, has_appointment, request_maintenance WHERE appnt_ID = appt_id AND doctor_id = did AND status = \'AV\' ANDdept_name = \'";
                        System.out.print("\tEnter department name: ");
                        String input = in.readLine();
                        query += input + "\' AND adate = \'";
                        System.out.print("\tEnter date of appointment: ");
                        input = in.readLine();
                        query += input + "\';";
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void ListStatusNumberOfAppointmentsPerDoctor(DBproject esql) {//7
		// Count number of different types of appointments per doctors and list them in descending order
		try {
			String query = "SELECT D.doctor_ID, COUNT(A.appnt_id) AS count FROM Doctor D, has_appointment H, Appointment A WHERE D.doctor_ID = H.doctor_id AND A.appnt_ID = H.appt_id GROUP BY D.doctor_ID ORDER BY count DESC;";

                        int rowCount = esql.executeQueryAndPrintResult(query);	
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void FindPatientsCountWithStatus(DBproject esql) {//8
		// Find how many patients per doctor there are with a given status (i.e. PA, AC, AV, WL) and list that number per doctor.
		try {
			String query = "SELECT D.doctor_ID, COUNT(P.patient_ID) AS pcount FROM Patient P, searches S, Appointment A, has_appointment H, Doctor D WHERE P.patient_ID = S.pid AND S.aid = A.appnt_ID AND A.appnt_ID = H.appt_id AND H.doctor_id = D.doctor_ID AND A.status = \'";
                        System.out.println("\tEnter status of appointment: ");
                        String input = in.readLine();
                        query += (input + "\' GROUP BY D.doctor_ID;");
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + rowCount);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
}
