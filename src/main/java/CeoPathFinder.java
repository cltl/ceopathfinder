import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.Filter;

import java.io.File;
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
    private HashMap<String, ArrayList<String>> classPreMap;
    private HashMap<String, ArrayList<String>> classDurMap;
    private HashMap<String, ArrayList<String>> classPosMap;
    static boolean during = false;
    static boolean deep = false;

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

/*

 */

    public CeoPathFinder() {
         ontologyModel =
                ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
         preMap = new HashMap<String, ArrayList<String>>();
         posMap = new HashMap<String, ArrayList<String>>();
         durMap = new HashMap<String, ArrayList<String>>();
         classPreMap = new HashMap<String, ArrayList<String>>();
         classPosMap = new HashMap<String, ArrayList<String>>();
         classDurMap = new HashMap<String, ArrayList<String>>();
    }

    public static boolean isDuring() {
        return during;
    }

    public static void setDuring(boolean during) {
        CeoPathFinder.during = during;
    }

    public void readOwlFile (String pathToOwlFile) {
        InputStream in = FileManager.get().open(pathToOwlFile);
        File owlFile = new File(pathToOwlFile);
        if (!owlFile.exists()) {
            System.out.println("Cannot find pathToOwlFile = " + pathToOwlFile);
        }
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

    public void interpretOntology (OntClass ontClass) {
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
                    if (!ontClass.getLocalName().isEmpty()) {
                        if (valueNode instanceof Resource) {
                            storeRuleEffect(valueNode.asResource(), ontClass.getLocalName());
                        } else {
                            // object is a literal
                            System.out.println("Literal: \"" + object.toString() + "\"");
                        }
                    }
                }
            }

        }
        // Go deep
        for (Iterator j = ontClass.listSubClasses(); j.hasNext();) {
            OntClass c = (OntClass) j.next();
            interpretOntology(c);
        }
    }

    public void interpretOntology (OntClass ontClass, ArrayList<String> superEffects) {
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
                    if (!ontClass.getLocalName().isEmpty()) {
                        if (valueNode instanceof Resource) {
                            storeRuleEffect(valueNode.asResource(), ontClass.getLocalName(), superEffects);
                        } else {
                            // object is a literal
                            System.out.println("Literal: \"" + object.toString() + "\"");
                        }
                    }
                }
            }

        }
        // Go deep
        for (Iterator j = ontClass.listSubClasses(); j.hasNext();) {
            OntClass c = (OntClass) j.next();
            interpretOntology(c, superEffects);
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
                if (!effects.contains(effect) && !effect.isEmpty()) {
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
                        if (classPosMap.containsKey(className)) {
                            ArrayList<String> classEffects = classPosMap.get(className);
                            for (int j = 0; j < effects.size(); j++) {
                                String e = effects.get(j);
                                if (!classEffects.contains(e)) {
                                    classEffects.add(e);
                                }
                            }
                            classPosMap.put(className, classEffects);
                        }
                        else {
                            classPosMap.put(className, effects);
                        }
                    }
                    else if (rdfNode.toString().indexOf("#pre_") > -1) {
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
                        if (classPreMap.containsKey(className)) {
                            ArrayList<String> classEffects = classPreMap.get(className);
                            for (int j = 0; j < effects.size(); j++) {
                                String e = effects.get(j);
                                if (!classEffects.contains(e)) {
                                    classEffects.add(e);
                                }
                            }
                            classPreMap.put(className, classEffects);
                        }
                        else {
                            classPreMap.put(className, effects);
                        }
                    }
                    else if (rdfNode.toString().indexOf("#during_") > -1) {
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
                        if (classDurMap.containsKey(className)) {
                            ArrayList<String> classEffects = classDurMap.get(className);
                            for (int j = 0; j < effects.size(); j++) {
                                String e = effects.get(j);
                                if (!classEffects.contains(e)) {
                                    classEffects.add(e);
                                }
                            }
                            classDurMap.put(className, classEffects);
                        }
                        else {
                            classDurMap.put(className, effects);
                        }
                    }
                }
            }
        }
    }

    public void storeRuleEffect (RDFNode rdfNode, String className, ArrayList<String> supereffects) {
        ArrayList<String> effects = getRuleEffects(rdfNode);
        for (int i = 0; i < effects.size(); i++) {
            String e = effects.get(i);
            if (!supereffects.contains(e)) {
                supereffects.add(e);
            }
        }
        effects.addAll(supereffects);
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
                        if (classPosMap.containsKey(className)) {
                            ArrayList<String> classEffects = classPosMap.get(className);
                            for (int j = 0; j < effects.size(); j++) {
                                String e = effects.get(j);
                                if (!classEffects.contains(e)) {
                                    classEffects.add(e);
                                }
                            }
                            classPosMap.put(className, classEffects);
                        }
                        else {
                            classPosMap.put(className, effects);
                        }
                    }
                    else if (rdfNode.toString().indexOf("#pre_") > -1) {
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
                        if (classPreMap.containsKey(className)) {
                            ArrayList<String> classEffects = classPreMap.get(className);
                            for (int j = 0; j < effects.size(); j++) {
                                String e = effects.get(j);
                                if (!classEffects.contains(e)) {
                                    classEffects.add(e);
                                }
                            }
                            classPreMap.put(className, classEffects);
                        }
                        else {
                            classPreMap.put(className, effects);
                        }
                    }
                    else if (rdfNode.toString().indexOf("#during_") > -1) {
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
                        if (classDurMap.containsKey(className)) {
                            ArrayList<String> classEffects = classDurMap.get(className);
                            for (int j = 0; j < effects.size(); j++) {
                                String e = effects.get(j);
                                if (!classEffects.contains(e)) {
                                    classEffects.add(e);
                                }
                            }
                            classDurMap.put(className, classEffects);
                        }
                        else {
                            classDurMap.put(className, effects);
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

    public void interpretOntology (String classString) {
        OntClass myClass = ontologyModel.getOntClass(nwr+classString);
        interpretOntology(myClass);
    }

    public void interpretOntologyWithInheritance (String classString) {
        OntClass myClass = ontologyModel.getOntClass(nwr+classString);
        ArrayList<String> supereffects = new ArrayList<String>();
        interpretOntology(myClass,supereffects);
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

    public Integer countEventMatchesDirect (String event1, String event2) {
        Integer matchCount = 0;
        ArrayList<String> pos1 = new ArrayList<String>();
        ArrayList<String> dur1 = new ArrayList<String>();
        ArrayList<String> pre2 = new ArrayList<String>();
        ArrayList<String> dur2 = new ArrayList<String>();
        if (classPosMap.containsKey(event1)) pos1 = classPosMap.get(event1);
        if (classDurMap.containsKey(event1)) dur1 = classDurMap.get(event1);
        if (classPreMap.containsKey(event2)) pre2 = classPreMap.get(event2);
        if (classDurMap.containsKey(event2)) dur2 = classDurMap.get(event2);

/*
        System.out.println("event1 = " + event1);
        System.out.println("event2 = " + event2);
        System.out.println("pos1.size() = " + pos1.size());
        System.out.println("dur1.size() = " + dur1.size());
        System.out.println("pre2.size() = " + pre2.size());
        System.out.println("dur2.size() = " + dur2.size());
*/

        for (int i = 0; i < pos1.size(); i++) {
            String p = pos1.get(i);
            if (!p.isEmpty()) {
                if (pre2.contains(p)) {
                    matchCount++;
                } else if (dur2.contains(p)) {
                   matchCount++;
                }
            }
        }

        if (during) {
            for (int i = 0; i < dur1.size(); i++) {
                String p = dur1.get(i);
                if (!p.isEmpty()) {
                    if (pre2.contains(p)) {
                       matchCount++;
                    } else if (dur2.contains(p)) {
                       matchCount++;
                    }
                }
            }
        }
       // System.out.println("matchCount = " + matchCount);
        return matchCount;
    }

    public Integer countEventMatchesInDirect (String event1, String event2) {
        Integer matchCount = 0;

        ArrayList<String> pos1 = new ArrayList<String>();
        ArrayList<String> dur1 = new ArrayList<String>();
        ArrayList<String> pre2 = new ArrayList<String>();
        ArrayList<String> dur2 = new ArrayList<String>();
        if (classPosMap.containsKey(event1)) pos1 = classPosMap.get(event1);
        if (classDurMap.containsKey(event1)) dur1 = classDurMap.get(event1);
        if (classPreMap.containsKey(event2)) pre2 = classPreMap.get(event2);
        if (classDurMap.containsKey(event2)) dur2 = classDurMap.get(event2);
        /// We start from the pre conditions of the last event and reason back, considering this as a bridge condition
        for (int i = 0; i < pre2.size(); i++) {
            String bridgeCondition = pre2.get(i);
            if (posMap.containsKey(bridgeCondition)) {
                //// get all classes that have the bridgeCondition as a post condition
                ArrayList<String> postClasses = posMap.get(bridgeCondition);
                for (int j = 0; j < postClasses.size(); j++) {
                    String postClass = postClasses.get(j);
                    if (!postClass.equals(event1)) {
                        if (classPreMap.containsKey(postClass)) {
                            /// the postClass has preconditions,
                            // we are going to check if any of these is a post condition of event1
                            ArrayList<String> preConditions = classPreMap.get(postClass);
                            for (int k = 0; k < preConditions.size(); k++) {
                                String preConditionPostclass = preConditions.get(k);
                                if (pos1.contains(preConditionPostclass)) {
                                    matchCount++;
                                }
                            }
                        }
                    }
                    else {
                        //// this is event1 and therefore a direct mapping
                    }
                }
            }
        }
        if (during) {
            /// in the next loop we consider during relations of the last event2 and reason back
            for (int i = 0; i < dur2.size(); i++) {
                String bridgeCondition = dur2.get(i);
                if (posMap.containsKey(bridgeCondition)) {
                    //// get all classes that have the bridgeCondition as a post condition
                    ArrayList<String> postClasses = posMap.get(bridgeCondition);
                    for (int j = 0; j < postClasses.size(); j++) {
                        String postClass = postClasses.get(j);
                        if (!postClass.equals(event1)) {
                            if (classPreMap.containsKey(postClass)) {
                                ArrayList<String> preConditions = classPreMap.get(postClass);
                                for (int k = 0; k < preConditions.size(); k++) {
                                    String preConditionPostclass = preConditions.get(k);
                                    if (pos1.contains(preConditionPostclass)) {
                                        matchCount++;
                                    }
                                }
                            }
                        } else {
                            //// this is event1 and therefore a direct mapping
                        }
                    }
                }
            }
            /// in the next loop we consider during relations of event1 when we reason back from pre conditions of event2
            for (int i = 0; i < pre2.size(); i++) {
                String bridgeCondition = pre2.get(i);
                if (posMap.containsKey(bridgeCondition)) {
                    //// get all classes that have the bridgeCondition as a post condition
                    ArrayList<String> postClasses = posMap.get(bridgeCondition);
                    for (int j = 0; j < postClasses.size(); j++) {
                        String postClass = postClasses.get(j);
                        if (!postClass.equals(event1)) {
                            if (classPreMap.containsKey(postClass)) {
                                ArrayList<String> preConditions = classPreMap.get(postClass);
                                for (int k = 0; k < preConditions.size(); k++) {
                                    String preConditionPostclass = preConditions.get(k);
                                    if (dur1.contains(preConditionPostclass)) {
                                        matchCount++;
                                    }
                                }
                            }
                        } else {
                            //// this is event1 and therefore a direct mapping
                        }
                    }
                }
            }
        }
        return matchCount;
    }

    public void checkEventsDirect (String event1, String event2) {
        System.out.println("event1 = " + event1);
        System.out.println("event2 = " + event2);
        System.out.println("=====================");
        ArrayList<String> pos1 = new ArrayList<String>();
        ArrayList<String> dur1 = new ArrayList<String>();
        ArrayList<String> pre2 = new ArrayList<String>();
        ArrayList<String> dur2 = new ArrayList<String>();
        if (classPosMap.containsKey(event1)) pos1 = classPosMap.get(event1);
        if (classDurMap.containsKey(event1)) dur1 = classDurMap.get(event1);
        if (classPreMap.containsKey(event2)) pre2 = classPreMap.get(event2);
        if (classDurMap.containsKey(event2)) dur2 = classDurMap.get(event2);


        for (int i = 0; i < pos1.size(); i++) {
            String p = pos1.get(i);
            if (!p.isEmpty()) {
                if (pre2.contains(p)) {
                    System.out.println("circumstantial post-pre = " + p);
                } else if (dur2.contains(p)) {
                    System.out.println("circumstantial post-dur = " + p);
                }
            }
        }

        if (during) {
            for (int i = 0; i < dur1.size(); i++) {
                String p = dur1.get(i);
                if (!p.isEmpty()) {
                    if (pre2.contains(p)) {
                        System.out.println("circumstantial dur-pre = " + p);
                    } else if (dur2.contains(p)) {
                        System.out.println("circumstantial dur-dur = " + p);
                    }
                }
            }
        }
    }
    
    public void checkEventsInDirect (String event1, String event2) {
        System.out.println("event1 = " + event1);
        System.out.println("event2 = " + event2);
        System.out.println("=====================");
        ArrayList<String> pos1 = new ArrayList<String>();
        ArrayList<String> dur1 = new ArrayList<String>();
        ArrayList<String> pre2 = new ArrayList<String>();
        ArrayList<String> dur2 = new ArrayList<String>();
        if (classPosMap.containsKey(event1)) pos1 = classPosMap.get(event1);
        if (classDurMap.containsKey(event1)) dur1 = classDurMap.get(event1);
        if (classPreMap.containsKey(event2)) pre2 = classPreMap.get(event2);
        if (classDurMap.containsKey(event2)) dur2 = classDurMap.get(event2);
        /// We start from the pre conditions of the last event and reason back, considering this as a bridge condition
        for (int i = 0; i < pre2.size(); i++) {
            String bridgeCondition = pre2.get(i);
            if (posMap.containsKey(bridgeCondition)) {
                //// get all classes that have the bridgeCondition as a post condition
                ArrayList<String> postClasses = posMap.get(bridgeCondition);
                for (int j = 0; j < postClasses.size(); j++) {
                    String postClass = postClasses.get(j);
                    if (!postClass.equals(event1)) {
                        if (classPreMap.containsKey(postClass)) {
                            /// the postClass has preconditions,
                            // we are going to check if any of these is a post condition of event1
                            ArrayList<String> preConditions = classPreMap.get(postClass);
                            for (int k = 0; k < preConditions.size(); k++) {
                                String preConditionPostclass = preConditions.get(k);
                                if (pos1.contains(preConditionPostclass)) {
                                    System.out.println("\nevent1 = " + event1);
                                    System.out.println("post event 1 pre bridge condition = " + preConditionPostclass);
                                    System.out.println("bridging event = " + postClass);
                                    System.out.println("post bridge pre event2 bridgeCondition = " + bridgeCondition);
                                    System.out.println("event2 = " + event2);
                                }
                            }
                        }
                    }
                    else {
                        //// this is event1 and therefore a direct mapping
                    }
                }
            }
        }
        if (during) {
            /// in the next loop we consider during relations of the last event2 and reason back
            for (int i = 0; i < dur2.size(); i++) {
                String bridgeCondition = dur2.get(i);
                if (posMap.containsKey(bridgeCondition)) {
                    //// get all classes that have the bridgeCondition as a post condition
                    ArrayList<String> postClasses = posMap.get(bridgeCondition);
                    for (int j = 0; j < postClasses.size(); j++) {
                        String postClass = postClasses.get(j);
                        if (!postClass.equals(event1)) {
                            if (classPreMap.containsKey(postClass)) {
                                ArrayList<String> preConditions = classPreMap.get(postClass);
                                for (int k = 0; k < preConditions.size(); k++) {
                                    String preConditionPostclass = preConditions.get(k);
                                    if (pos1.contains(preConditionPostclass)) {
                                        System.out.println("\nevent1 = " + event1);
                                        System.out.println("post event 1 pre bridge condition = " + preConditionPostclass);
                                        System.out.println("bridging event = " + postClass);
                                        System.out.println("post bridge during event2 bridgeCondition = " + bridgeCondition);
                                        System.out.println("event2 = " + event2);
                                    }
                                }
                            }
                        } else {
                            //// this is event1 and therefore a direct mapping
                        }
                    }
                }
            }
            /// in the next loop we consider during relations of event1 when we reason back from pre conditions of event2
            for (int i = 0; i < pre2.size(); i++) {
                String bridgeCondition = pre2.get(i);
                if (posMap.containsKey(bridgeCondition)) {
                    //// get all classes that have the bridgeCondition as a post condition
                    ArrayList<String> postClasses = posMap.get(bridgeCondition);
                    for (int j = 0; j < postClasses.size(); j++) {
                        String postClass = postClasses.get(j);
                        if (!postClass.equals(event1)) {
                            if (classPreMap.containsKey(postClass)) {
                                ArrayList<String> preConditions = classPreMap.get(postClass);
                                for (int k = 0; k < preConditions.size(); k++) {
                                    String preConditionPostclass = preConditions.get(k);
                                    if (dur1.contains(preConditionPostclass)) {
                                        System.out.println("\nevent1 = " + event1);
                                        System.out.println("during event 1 pre bridge condition = " + preConditionPostclass);
                                        System.out.println("bridging event = " + postClass);
                                        System.out.println("post bridge pre event2 bridgeCondition = " + bridgeCondition);
                                        System.out.println("event2 = " + event2);
                                    }
                                }
                            }
                        } else {
                            //// this is event1 and therefore a direct mapping
                        }
                    }
                }
            }
        }
    }


    public void checkMaps () {
        Set keySetPre = posMap.keySet();
        Iterator<String> pos = keySetPre.iterator();
        while (pos.hasNext()) {
            String p = pos.next();
            ArrayList<String> postClasses = posMap.get(p);
            if (preMap.containsKey(p)) {
                ArrayList<String> preClasses = preMap.get(p);
                for (int i = 0; i < postClasses.size(); i++) {
                    String pClass = postClasses.get(i);
                    String str = pClass+" with post-situation: "+p+", can result in pre-situations:";
                    for (int j = 0; j < preClasses.size(); j++) {
                        String preClass = preClasses.get(j);
                        str += preClass+";";
                    }
                    System.out.println(str);
                }
            }
            else if (durMap.containsKey(p)) {
                ArrayList<String> durClasses = durMap.get(p);
                for (int i = 0; i < postClasses.size(); i++) {
                    String pClass = postClasses.get(i);
                    String str = pClass+" with post-situation: "+p+", can result in during-situations:";
                    for (int j = 0; j < durClasses.size(); j++) {
                        String durClass = durClasses.get(j);
                        str += durClass+";";
                    }
                    System.out.println(str);
                }
            }

        }
    }

    public Integer pathForTypes (ArrayList<String> mention1Types, ArrayList<String> mention2Types) {
        Integer bestPath = 0;
        for (int i = 0; i < mention1Types.size(); i++) {
            String m1Type = mention1Types.get(i);
            for (int j = 0; j < mention2Types.size(); j++) {
                String m2Type = mention2Types.get(j);
                Integer m1m2Direct = countEventMatchesDirect(m1Type, m2Type);
                Integer m1m2InDirect = countEventMatchesInDirect(m1Type, m2Type);
                Integer path = m1m2Direct+m1m2InDirect;
                if (path>bestPath) {
                    bestPath = path;
                }
            }
        }
        return bestPath;
    }

    public void close () {
        ontologyModel.close();
    }

    static String testparameter = "--e1 Fire --e2 Surgery --match 2 --printTree " +
            "--ont-file /Users/piek/Desktop/Roxane/CEO.v1.0/CEO_version_1.owl";
/*
    static String testparameter = "--e1 Fire --e2 Surgery --match 2 --printTree " +
            "--ont-file /Users/piek/Desktop/Roxane/CEO.v0.7/CEO_version_07.owl";
*/



    static public void main (String [] args) {
        String pathToOwlOntology = "";
        String e1 = "";
        String e2 = "";
        String match = "";
        boolean printConditionMaps = false;
        boolean printHierarchy = false;
        boolean printChain = false;

        if (args.length==0) {
            args = testparameter.split(" ");
        }
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
            else if (arg.equals("--printChain")) {
                printChain = true;
            }
            else if (arg.equals("--deep")) {
                deep = true;
            }
            else if (arg.equals("--during")) {
                during = true;
            }
        }
        if (!new File(pathToOwlOntology).exists()) {
            System.out.println("cannot find pathToOwlOntology = " + pathToOwlOntology);
            return;
        }
        CeoPathFinder ceoPathFinder = new CeoPathFinder();
        ceoPathFinder.readOwlFile(pathToOwlOntology);
        if (deep) {
            ceoPathFinder.interpretOntologyWithInheritance("Physical");
        }
        else {
            ceoPathFinder.interpretOntology("Physical");
        }
        if (printHierarchy) {
            ceoPathFinder.printOntology("Physical");
        }
        if (printConditionMaps) {
            ceoPathFinder.printHashMaps();
        }
        if (printChain) {
            ceoPathFinder.checkMaps();
        }
        if (!e1.isEmpty() && !e2.isEmpty()) {
            if (match.equalsIgnoreCase("0")) {
                ceoPathFinder.checkEventsDirect(e1, e2);
            }
            else if (match.equalsIgnoreCase("1")) {
                ceoPathFinder.checkEventsInDirect(e1, e2);
            }
        }
        ceoPathFinder.close();

    }
}
