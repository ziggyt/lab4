
import java.sql.*; // JDBC stuff.
import java.util.Properties;

public class PortalConnection {

    // For connecting to the portal database on your local machine
    static final String DATABASE = "jdbc:postgresql://localhost/portal";
    static final String USERNAME = "postgres";
    static final String PASSWORD = "postgres";

    // For connecting to the chalmers database server (from inside chalmers)
    // static final String DATABASE = "jdbc:postgresql://ate.ita.chalmers.se/";
    // static final String USERNAME = "tda357_nnn";
    // static final String PASSWORD = "yourPasswordGoesHere";


    // This is the JDBC connection object you will be using in your methods.
    private Connection conn;

    public PortalConnection() throws SQLException, ClassNotFoundException {
        this(DATABASE, USERNAME, PASSWORD);  
    }

    // Initializes the connection, no need to change anything here
    public PortalConnection(String db, String user, String pwd) throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pwd);
        conn = DriverManager.getConnection(db, props);
    }


    // Register a student on a course, returns a tiny JSON document (as a String)
    public String register(String student, String courseCode){

        try {
            PreparedStatement st = conn.prepareStatement("INSERT INTO Registrations VALUES (?, ?)");

            st.setString(1, student);
            st.setString(2, courseCode);

            st.execute();

        } catch (SQLException e){

            return "{\"success\":false, \"error\":\""+getError(e)+"\"}";

        }

        return "{\"success\":true}";
    }

    // Unregister a student from a course, returns a tiny JSON document (as a String)
    public String unregister(String student, String courseCode){

        try {
            PreparedStatement st = conn.prepareStatement("DELETE FROM Registrations WHERE student = ? AND course = ?");

            st.setString(1, student);
            st.setString(2, courseCode);

            st.execute();


        } catch (SQLException e){

            return "{\"success\":false, \"error\":\""+getError(e)+"\"}";
        }

        return "{\"success\":true}";

    }

    // Return a JSON document containing lots of information about a student, it should validate against the schema found in information_schema.json
    public String getInfo(String student) throws SQLException{

        //String query = "DELETE FROM Registered WHERE student=? AND course=?";
        String query1 = "SELECT jsonb_build_object('finished',jsonb_agg(f1)) AS jsondata FROM (SELECT name AS course,course AS code,grade, credits FROM Taken JOIN Courses ON course=code WHERE student=?) AS f1";
        String query2 = "SELECT jsonb_build_object('student',idnr, 'name',name, 'login',login, 'program',program, 'branch',branch, 'seminarCourses',seminarcourses, 'mathCredits',mathcredits, 'researchCredits',researchcredits, 'totalCredits',totalcredits, 'canGraduate',qualified ) AS jsondata FROM BasicInformation JOIN PathToGraduation on student=idnr WHERE idnr=?";
        //String query3 = "SELECT jsonb_build_object('course',name, 'code',code, 'status', status, 'position', position) AS jsondata from coursequeuepositions natural right outer JOIN registrations JOIN Courses on code=course";

        String result1;
        String result2;
        String result3;

        String query3 = "SELECT jsonb_build_object('registered',jsonb_agg(f1)) AS jsondata FROM (SELECT name AS course, code, status, position from coursequeuepositions natural right outer JOIN registrations JOIN Courses on code=course where student=?) AS f1";


        try( PreparedStatement ps = conn.prepareStatement(query1) ) {
            ps.setString(1, student);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                //System.out.println(rs.getString("data"));
                result1 =  rs.getString("jsondata");
            }
            else{
                result1 = "{\"student\":\"does not exist :(\"}";
            }

            ///////////////

            //PreparedStatement ps2 = conn.prepareStatement(query);

        }

        try( PreparedStatement ps = conn.prepareStatement(query2) ) {
            ps.setString(1, student);

            //ResultSet rs = ps.executeQuery();
            ResultSet resultSet = ps.executeQuery();

            if(resultSet.next()) {
                //System.out.println(rs.getString("data"));
                result2 = resultSet.getString("jsondata");
            }
            else{
                result2 =  "{\"student\":\"does not exist :(\"}";
            }

        }

        try( PreparedStatement ps = conn.prepareStatement(query3) ) {
            ps.setString(1, student);

            //ResultSet rs = ps.executeQuery();
            ResultSet resultSet = ps.executeQuery();

            if(resultSet.next()) {
                //System.out.println(rs.getString("data"));
                result3 = resultSet.getString("jsondata");
            }
            else{
                result3 =  "{\"student\":\"does not exist :(\"}";
            }

        }



        result1 = result1.substring(1, result1.length()-1);
        result2 = result2.substring(1, result2.length()-1);
        result3 = result3.substring(1, result3.length()-1);

        System.out.println(result1);
        System.out.println(result2);
        System.out.println(result3);

        return "{" + result1 + "," + result2 + "," + result3 + "}";


        /*try(

                PreparedStatement st = conn.prepareStatement(
            // replace this with something more useful
                "SELECT jsonb_build_object('finished',jsonb_agg(f1)) AS jsondata FROM (SELECT name AS course,course AS code,grade, credits FROM Taken JOIN Courses ON course=code WHERE student='4444444444') AS f1; SELECT jsonb_build_object('data', idnr) AS data from students where idnr='1111111111"
            );){

            String s = "WITH JSONTEST AS (SELECT jsonb_build_object('student',idnr, 'name',name, 'login',login) AS data";

            //'student',idnr, 'name',name, 'login',login, 'program',program, 'branch',branch, 'seminarCourses',seminarcourses, 'mathCredits',mathcredits, 'researchCredits',researchcredits, 'totalCredits',totalcredits, 'canGraduate',qualified ) AS jsondata FROM BasicInformation JOIN PathToGraduation on student=idnr WHERE idnr=?"
           // );){

                //st.setString(1, student);

                ResultSet rs = st.executeQuery();

                if(rs.next()) {
                    System.out.println(rs.getString("data"));
                    return rs.getString("jsondata");
                }
                else
                    return "{\"student\":\"does not exist :(\"}";

            }*/


    }


        // This is a hack to turn an SQLException into a JSON string error message. No need to change.
    public static String getError(SQLException e){
        String message = e.getMessage();
        int ix = message.indexOf('\n');
        if (ix > 0) message = message.substring(0, ix);
        message = message.replace("\"","\\\"");
        return message;
    }

}