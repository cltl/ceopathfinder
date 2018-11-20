import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GetRelation {
    static String testparameter = "--ont-file /Users/piek/Desktop/Deloitte/CLTL_CEO_version_1_sameas.owl";

    final static public String owl= "http://www.w3.org/2002/07/owl#";
    final static public String cltl= "http://cltl.nl/ontology#";
    static OntModel ontologyModel;
    static HashMap<String, ArrayList<String>> mappingsToClass = new HashMap<String, ArrayList<String>>();

    static public void main (String [] args) {
        String pathToOwlOntology = "";
        String predicateName = "sameAs";
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
        ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        readOwlFile(pathToOwlOntology);
        OntClass myClass = ontologyModel.getOntClass(cltl+"Physical");
        descendHierarchyForValues(myClass, predicateName);
        for (Map.Entry<String,ArrayList<String>> entry : mappingsToClass.entrySet()) {
            ArrayList<String> classes = entry.getValue();
            for (int i = 0; i < classes.size(); i++) {
                String s = classes.get(i);
                System.out.println(entry.getKey()+"\t"+s);
            }
        }
    }


    static void readOwlFile (String pathToOwlFile) {
           InputStream in = FileManager.get().open(pathToOwlFile);
           File owlFile = new File(pathToOwlFile);
           if (!owlFile.exists()) {
               System.out.println("Cannot find pathToOwlFile = " + pathToOwlFile);
           }
           ontologyModel.read(in, "RDF/XML-ABBREV");
   }

    static String getPredicateValue (Statement statement, String predicateName) {
        String value = "";
        if (statement.getPredicate().getLocalName().equals(predicateName)) {
            value = statement.getObject().toString();
            int idx =  value.indexOf("^^");
            if (idx>-1) value = value.substring(0, idx);
        }
        return value;
    }

    public ArrayList<String> getValues (RDFNode rdfNode, String predicateName) {
        ArrayList<String> values = new ArrayList<String>();
        //OntClass resourceClass = ontologyModel.getOntClass(rdfNode.toString());
        Individual resourceClass = ontologyModel.getIndividual(rdfNode.toString());
        if (resourceClass!=null) {
            //System.out.println("rdfNode.toString() = " + rdfNode.toString());
            StmtIterator pI = resourceClass.listProperties();
            while (pI.hasNext()) {
                Statement statement = pI.next();
                String effect = getPredicateValue(statement, predicateName);
                if (!values.contains(effect)) values.add(effect);
               // System.out.println("effect = " + effect);
            }
        }
        return values;
    }

    static void descendHierarchyForValues (OntClass ontClass, String predicateName) {
        StmtIterator pI = ontClass.listProperties();
        while (pI.hasNext()) {
            Statement statement = pI.next();
            String value = getPredicateValue(statement, predicateName);
            if (mappingsToClass.containsKey(value)) {
                ArrayList<String> classes = mappingsToClass.get(value);
                classes.add(ontClass.getLocalName());
                mappingsToClass.put(value, classes);
            }
            else {
                ArrayList<String> classes = new ArrayList<String>();
                classes.add(ontClass.getLocalName());
                mappingsToClass.put(value, classes);
            }

        }
        // Go deep
        for (Iterator j = ontClass.listSubClasses(); j.hasNext();) {
            OntClass c = (OntClass) j.next();
            descendHierarchyForValues(c, predicateName);
        }
    }
}
