import java.io.File;

public class PrintOntologyTree {
    static String testparameter = "--ont-file /Users/piek/Desktop/Deloitte/CLTL_CEO_version_1_sameas.owl";

    final static public String owl= "http://www.w3.org/2002/07/owl#";
    final static public String cltl= "http://cltl.nl/ontology#";

    static public void main (String [] args) {
        String pathToOwlOntology = "";
        CeoPathFinder ceoPathFinder = new CeoPathFinder();

        if (args.length==0) {
            args = testparameter.split(" ");
        }
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--ont-file") && args.length>(i+1)) {
               pathToOwlOntology = args[i+1];
            }
        }
        if (!new File(pathToOwlOntology).exists()) {
            System.out.println("cannot find pathToOwlOntology = " + pathToOwlOntology);
            return;
        }
        ceoPathFinder.readOwlFile(pathToOwlOntology);
        ceoPathFinder.printOntology("Physical", cltl);
    }

}
