public class TestPortal {

    // enable this to make pretty printing a bit more compact
    private static final boolean COMPACT_OBJECTS = false;

    // This class creates a portal connection and runs a few operation

    public static void main(String[] args) {
        try {
            PortalConnection c = new PortalConnection();

            // Write your tests here. Add/remove calls to pause() as desired.
            // Use println instead of prettyPrint to get more compact output (if your raw JSON is already readable)

            //List info for a student.
            prettyPrint(c.getInfo("2222222222"));
            pause();
            //Register the student for an unrestricted course, and show that they end up registered (show info again).
            System.out.println(c.register("2222222222", "CCC111"));
            prettyPrint(c.getInfo("2222222222"));
            pause();
            //Register the same student for the same course again, and show that you get an error response.
            System.out.println(c.register("2222222222", "CCC111"));
            pause();
            //Unregister the student from the course, and then unregister again from the same course. Show that the student is no longer registered and the second unregistration gives an error response.
            System.out.println(c.unregister("2222222222", "CCC111"));
            System.out.println(c.unregister("2222222222", "CCC111"));
            pause();

            //Register the student for a course that they don't have the prerequisites for, and show that an error is generated.
            System.out.println(c.unregister("2222222222", "CCC333"));
            pause();

            //Unregister a student from a restricted course that they are registered to, and which has at least two students in the queue. Register again to the same course and show that the student gets the correct (last) position in the waiting list.

            System.out.println(c.unregister("2222222222", "CCC888"));
            System.out.println(c.register("2222222222", "CCC888"));
            pause();

            //Unregister and re-register the same student for the same restricted course, and show that the student is first removed and then ends up in the same position as before (last).
            System.out.println(c.unregister("2222222222", "CCC888"));
            System.out.println(c.register("2222222222", "CCC888"));
            pause();

            //Unregister a student from an overfull course, i.e. one with more students registered than there are places on the course (you need to set this situation up in the database directly). Show that no student was moved from the queue to being registered as a result.
            System.out.println(c.unregister("2222222222", "CCC222"));
            System.out.println(c.register("2222222222", "CCC222"));


        } catch (ClassNotFoundException e) {
            System.err.println("ERROR!\nYou do not have the Postgres JDBC driver (e.g. postgresql-42.2.8.jar) in your runtime classpath!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void pause() throws Exception {
        System.out.println("PRESS ENTER");
        while (System.in.read() != '\n') ;
    }

    // This is a truly horrible and bug-riddled hack for printing JSON.
    // It is used only to avoid relying on additional libraries.
    // If you are a student, please avert your eyes.
    public static void prettyPrint(String json) {
        System.out.print("Raw JSON:");
        System.out.println(json);
        System.out.println("Pretty-printed (possibly broken):");

        int indent = 0;
        json = json.replaceAll("\\r?\\n", " ");
        json = json.replaceAll(" +", " "); // This might change JSON string values :(
        json = json.replaceAll(" *, *", ","); // So can this

        for (char c : json.toCharArray()) {
            if (c == '}' || c == ']') {
                indent -= 2;
                breakline(indent); // This will break string values with } and ]
            }

            System.out.print(c);

            if (c == '[' || c == '{') {
                indent += 2;
                breakline(indent);
            } else if (c == ',' && !COMPACT_OBJECTS)
                breakline(indent);
        }

        System.out.println();
    }

    public static void breakline(int indent) {
        System.out.println();
        for (int i = 0; i < indent; i++)
            System.out.print(" ");
    }
}