import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.Filter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 11/07/2017.
 */
public class CeoPathFinder {
    final static public String owl= "http://www.w3.org/2002/07/owl#";
    final static public String nwr= "http://www.newsreader-project.eu/domain-ontology#";
    private HashMap<String, ArrayList<String>> preMap;
    private HashMap<String, ArrayList<String>> durMap;
    private HashMap<String, ArrayList<String>> posMap;

    private OntModel ontologyModel;

/*    static public String createSparql(String variable, String typeUri) {
        String q = "SELECT ?"+variable+" " +
                "WHERE{" +
                "?"+variable+" a  "+typeUri +
                "}";
        return q;
    }

    static public String createSparqlTriple(String variable, String subj, String pred, String obj) {
        String q = "SELECT ?"+variable+" \n" +
                "WHERE { " +
                "  "+subj+ " \n"+
                "  "+pred+ " \n"+
                "  "+obj+
                "}";
        return q;
    }*/

    public CeoPathFinder() {
         ontologyModel =
                ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
         preMap = new HashMap<String, ArrayList<String>>();
         posMap = new HashMap<String, ArrayList<String>>();
         durMap = new HashMap<String, ArrayList<String>>();
    }

    public void readOwlFile (String pathToOwlFile) {
        InputStream in = FileManager.get().open(pathToOwlFile);
        ontologyModel.read(in, "RDF/XML-ABBREV");
    }

    public void printHierarchy (OntClass ontClass, int level) {
        String tab = "";
        for (int i = 0; i < level; i++) {
             tab += " ";
        }
        System.out.println(tab + ontClass.getLocalName());
        StmtIterator pI = ontClass.listProperties();
        while (pI.hasNext()) {
            Statement statement = pI.next();
            RDFNode object = statement.getObject();
            if (object.isAnon()) {
                /// Restriction is a blank node
                Property predicate = object.getModel().createProperty(owl, "hasValue");
                Statement valueStatement = object.asResource().getProperty(predicate);

                if (valueStatement != null) {
                    RDFNode valueNode = valueStatement.getObject();
                    if (valueNode instanceof Resource) {
                        String ns = valueNode.asResource().getNameSpace();
                        String localName = valueNode.asResource().getLocalName();
                        //System.out.println(tab + tab + "restriction = " + localName);
                        getRule(valueNode.asResource());
                    } else {
                        // object is a literal
                        System.out.println("Literal: \"" + object.toString() + "\"");
                    }

                }
            }

        }
        // Go deep
        for (Iterator j = ontClass.listSubClasses(); j.hasNext();) {
            OntClass c = (OntClass) j.next();
            printHierarchy(c, level+1);
        }
    }


    public void readHierarchy (OntClass ontClass) {
        StmtIterator pI = ontClass.listProperties();
        while (pI.hasNext()) {
            Statement statement = pI.next();
            RDFNode object = statement.getObject();
            if (object.isAnon()) {
                /// Restriction is a blank node
                Property predicate = object.getModel().createProperty(owl, "hasValue");
                Statement valueStatement = object.asResource().getProperty(predicate);

                if (valueStatement != null) {
                    RDFNode valueNode = valueStatement.getObject();
                    if (valueNode instanceof Resource) {
                        storeRuleEffect(valueNode.asResource(), ontClass.getLocalName());
                    } else {
                        // object is a literal
                        System.out.println("Literal: \"" + object.toString() + "\"");
                    }

                }
            }

        }
        // Go deep
        for (Iterator j = ontClass.listSubClasses(); j.hasNext();) {
            OntClass c = (OntClass) j.next();
            readHierarchy(c);
        }
    }

    public ArrayList<String> getRuleEffects (RDFNode rdfNode) {
        ArrayList<String> effects = new ArrayList<String>();
        Individual resourceClass = ontologyModel.getIndividual(rdfNode.toString());
        if (resourceClass!=null) {
            //System.out.println("rdfNode.toString() = " + rdfNode.toString());
            StmtIterator pI = resourceClass.listProperties();
            while (pI.hasNext()) {
                Statement statement = pI.next();
                RDFNode object = statement.getObject();
                String effect = getRuleEffect(object);
                if (!effects.contains(effect)) {
                    effects.add(effect);
                }
            }
        }
        return effects;
    }

    public void storeRuleEffect (RDFNode rdfNode, String className) {
        ArrayList<String> effects = getRuleEffects(rdfNode);
        if (!effects.isEmpty()) {
            for (int i = 0; i < effects.size(); i++) {
                String effect = effects.get(i);
                if (!effect.isEmpty()) {
                    if (rdfNode.toString().indexOf("#post_") > -1) {
                        if (posMap.containsKey(effect)) {
                            ArrayList<String> classNames = posMap.get(effect);
                            if (!classNames.contains(className)) {
                                classNames.add(className);
                                posMap.put(effect, classNames);
                            }
                        } else {
                            ArrayList<String> classNames = new ArrayList<String>();
                            classNames.add(className);
                            posMap.put(effect, classNames);
                        }
                    } else if (rdfNode.toString().indexOf("#pre_") > -1) {
                        if (preMap.containsKey(effect)) {
                            ArrayList<String> classNames = preMap.get(effect);
                            if (!classNames.contains(className)) {
                                classNames.add(className);
                                preMap.put(effect, classNames);
                            }
                        } else {
                            ArrayList<String> classNames = new ArrayList<String>();
                            classNames.add(className);
                            preMap.put(effect, classNames);
                        }
                    } else if (rdfNode.toString().indexOf("#during_") > -1) {
                        if (durMap.containsKey(effect)) {
                            ArrayList<String> classNames = durMap.get(effect);
                            if (!classNames.contains(className)) {
                                classNames.add(className);
                                durMap.put(effect, classNames);
                            }
                        } else {
                            ArrayList<String> classNames = new ArrayList<String>();
                            classNames.add(className);
                            durMap.put(effect, classNames);
                        }
                    }
                }
            }
        }
    }

    public void printHashMaps () {
        System.out.println("PRE");
        printHashMaps(preMap);
        System.out.println("POST");
        printHashMaps(posMap);
        System.out.println("DURING");
        printHashMaps(durMap);
    }

    void printHashMaps (HashMap<String, ArrayList<String>> map) {
        Set keySet = map.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            String str = key+":\n\t";
            ArrayList<String> classNames = map.get(key);
            for (int i = 0; i < classNames.size(); i++) {
                String className = classNames.get(i);
                str += className+";";
            }
            System.out.println(str);

        }
    }

    public void getRule (RDFNode rdfNode) {
        //OntClass resourceClass = ontologyModel.getOntClass(rdfNode.toString());
        Individual resourceClass = ontologyModel.getIndividual(rdfNode.toString());
        if (resourceClass!=null) {
            //System.out.println("rdfNode.toString() = " + rdfNode.toString());
            StmtIterator pI = resourceClass.listProperties();
            while (pI.hasNext()) {
                Statement statement = pI.next();
                RDFNode object = statement.getObject();
                String effect = getRuleEffect(object);

            }
        }
    }


    private String getUnarySituationRuleValue (Statement statement) {
        String value = "";
        if (statement.getPredicate().getLocalName().equals("hasSituationAssertionObjectValue")) {
            value = statement.getObject().toString();
            int idx =  value.indexOf("^^");
            if (idx>-1) value = value.substring(0, idx);
        }
        return value;
    }

    private String getSituationAssertionProperty (Statement statement) {
        String value = "";
        if (statement.getPredicate().getLocalName().equals("hasSituationAssertionProperty")) {
            value += statement.getObject().toString();
            int idx = value.indexOf("#");
            if (idx>-1) value = value.substring(idx);
            idx = value.indexOf("^^");
            if (idx>-1) value = value.substring(0, idx);
        }
        return value;
    }

    private String getSituationAssertionSubject (Statement statement) {
        String value = "";
        if (statement.getPredicate().getLocalName().equals("hasSituationAssertionSubject")) {
            value += statement.getObject().toString();
            int idx = value.indexOf("#");
            if (idx>-1) value = value.substring(idx);
            idx = value.indexOf("^^");
            if (idx>-1) value = value.substring(0, idx);
        }
        return value;
    }

    private String getSituationAssertionObject (Statement statement) {
        String value = "";
        if (statement.getPredicate().getLocalName().equals("hasSituationAssertionObject")) {
            value += statement.getObject().toString();
            int idx = value.indexOf("#");
            if (idx>-1) value = value.substring(idx);
            idx = value.indexOf("^^");
            if (idx>-1) value = value.substring(0, idx);
        }
        return value;
    }


    public String getRuleEffect (RDFNode rdfNode) {
        String ruleEffect = "";
        //OntClass resourceClass = ontologyModel.getOntClass(rdfNode.toString());
        Individual resourceClass = ontologyModel.getIndividual(rdfNode.toString());

        String valueString = "";
        String propertyString = "";
        String subjectString = "";
        String objectString = "";
        if (resourceClass!=null) {
            //System.out.println("rdfNode.toString() = " + rdfNode.toString());
            StmtIterator pI = resourceClass.listProperties();
            while (pI.hasNext()) {
                Statement statement = pI.next();
                valueString += getUnarySituationRuleValue(statement);
                propertyString += getSituationAssertionProperty(statement);
                subjectString += getSituationAssertionSubject(statement);
                objectString += getSituationAssertionObject(statement);
            }
        }
        if (!valueString.isEmpty()) ruleEffect += valueString;
        if (!subjectString.isEmpty()) ruleEffect += subjectString;
        if (!propertyString.isEmpty()) ruleEffect += propertyString;
        if (!objectString.isEmpty()) ruleEffect += objectString;
        if (ruleEffect.startsWith("#")) ruleEffect = ruleEffect.substring(1);
        return ruleEffect;
    }

    public void printOntology () {
        Iterator i = ontologyModel.listHierarchyRootClasses()
                .filterDrop( new Filter() {
                    public boolean accept( Object o ) {
                        return ((Resource) o).isAnon();
                    }} ); ///get all top nodes and excludes anonymous classes
        System.out.println("i.hasNext() = " + i.hasNext());
        while (i.hasNext()) {
            OntClass oClass = (OntClass) i.next();
            printHierarchy(oClass, 0);
        }
    }

    public void printOntology (String classString) {
        OntClass myClass = ontologyModel.getOntClass(nwr+classString);
        printHierarchy(myClass, 0);
    }

    public void readOntology (String classString) {
        OntClass myClass = ontologyModel.getOntClass(nwr+classString);
        readHierarchy(myClass);
    }

    public ArrayList<String> getPostConditions (OntClass myClass) {
        ArrayList<String> conditions = new ArrayList<String>();
        StmtIterator pI = myClass.listProperties();
        while (pI.hasNext()) {
            Statement statement = pI.next();
            RDFNode object = statement.getObject();
            if (object.isAnon()) {
                /// Restriction is a blank node
                Property predicate = object.getModel().createProperty(owl, "hasValue");
                Statement valueStatement = object.asResource().getProperty(predicate);

                if (valueStatement != null) {
                    RDFNode valueNode = valueStatement.getObject();
                    if (valueNode instanceof Resource) {
                        if (valueNode.toString().indexOf("#post_") > -1) {
                            ArrayList<String> someConditions = getRuleEffects(valueNode);
                            for (int i = 0; i < someConditions.size(); i++) {
                                String sCondition = someConditions.get(i);
                                if (!conditions.contains(sCondition)) conditions.add(sCondition);
                            }
                        }
                    }
                }
            }

        }
        return conditions;
    }

    public ArrayList<String> getPreConditions (OntClass myClass) {
        ArrayList<String> conditions = new ArrayList<String>();
        StmtIterator pI = myClass.listProperties();
        while (pI.hasNext()) {
            Statement statement = pI.next();
            RDFNode object = statement.getObject();
            if (object.isAnon()) {
                /// Restriction is a blank node
                Property predicate = object.getModel().createProperty(owl, "hasValue");
                Statement valueStatement = object.asResource().getProperty(predicate);

                if (valueStatement != null) {
                    RDFNode valueNode = valueStatement.getObject();
                    if (valueNode instanceof Resource) {
                        if (valueNode.toString().indexOf("#pre_") > -1) {
                            ArrayList<String> someConditions = getRuleEffects(valueNode);
                            for (int i = 0; i < someConditions.size(); i++) {
                                String sCondition = someConditions.get(i);
                                if (!conditions.contains(sCondition)) conditions.add(sCondition);
                            }
                        }
                    }
                }
            }

        }
        return conditions;
    }

    public ArrayList<String> getDuringConditions (OntClass myClass) {
        ArrayList<String> conditions = new ArrayList<String>();
        StmtIterator pI = myClass.listProperties();
        while (pI.hasNext()) {
            Statement statement = pI.next();
            RDFNode object = statement.getObject();
            if (object.isAnon()) {
                /// Restriction is a blank node
                Property predicate = object.getModel().createProperty(owl, "hasValue");
                Statement valueStatement = object.asResource().getProperty(predicate);

                if (valueStatement != null) {
                    RDFNode valueNode = valueStatement.getObject();
                    if (valueNode instanceof Resource) {
                        if (valueNode.toString().indexOf("#during_") > -1) {
                            ArrayList<String> someConditions = getRuleEffects(valueNode);
                            for (int i = 0; i < someConditions.size(); i++) {
                                String sCondition = someConditions.get(i);
                                if (!conditions.contains(sCondition)) conditions.add(sCondition);
                            }
                        }
                    }
                }
            }

        }
        return conditions;
    }

    public void checkEventsDirect (String event1, String event2) {
        System.out.println("event1 = " + event1);
        System.out.println("event2 = " + event2);
        OntClass myClass1 = ontologyModel.getOntClass(nwr+event1);
        OntClass myClass2 = ontologyModel.getOntClass(nwr+event2);
        ArrayList<String> pos1 = getPostConditions(myClass1);
        ArrayList<String> dur1 = getDuringConditions(myClass1);
        ArrayList<String> pre2 = getPreConditions(myClass2);
        ArrayList<String> dur2 = getDuringConditions(myClass2);
        for (int i = 0; i < pos1.size(); i++) {
            String p = pos1.get(i);
            if (pre2.contains(p)) {
                System.out.println("circumstantial post-pre = " + p);
            }
            else if (dur2.contains(p)) {
                System.out.println("circumstantial post-dur = " + p);
            }
        }
        for (int i = 0; i < dur1.size(); i++) {
            String p = dur1.get(i);
            if (pre2.contains(p)) {
                System.out.println("circumstantial dur-pre = " + p);
            }
            else if (dur2.contains(p)) {
                System.out.println("circumstantial dur-dur = " + p);
            }
        }
    }

    public void close () {
        ontologyModel.close();
    }

    static public void main (String [] args) {
        String pathToOwlOntology = "";
        String e1 = "";
        String e2 = "";
        String match = "";
        boolean printConditionMaps = false;
        boolean printHierarchy = false;
        pathToOwlOntology = "//Users/piek/Desktop/CEO/CEO_ESO.owl";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--ont-file") && args.length>(i+1)) {
               pathToOwlOntology = args[i+1];
            }
            else if (arg.equals("--e1") && args.length>(i+1)) {
               e1 = args[i+1];
            }
            else if (arg.equals("--e2") && args.length>(i+1)) {
               e2 = args[i+1];
            }
            else if (arg.equals("--match") && args.length>(i+1)) {
                match = args[i+1];
            }
            else if (arg.equals("--printMap")) {
                printConditionMaps = true;
            }
            else if (arg.equals("--printTree")) {
               printHierarchy = true;
            }
        }
        CeoPathFinder ceoPathFinder = new CeoPathFinder();
        ceoPathFinder.readOwlFile(pathToOwlOntology);
        if (printHierarchy) {
            ceoPathFinder.printOntology("Physical");
        }
        if (printConditionMaps) {
            ceoPathFinder.readOntology("Physical");
            ceoPathFinder.printHashMaps();
        }
        if (!e1.isEmpty() && !e2.isEmpty()) {
            ceoPathFinder.readOntology("Physical");
            if (match.equalsIgnoreCase("0")) {
                ceoPathFinder.checkEventsDirect(e1, e2);
            }
        }
        ceoPathFinder.close();

    }
}
