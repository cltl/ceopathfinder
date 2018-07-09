import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class NarrativeChainMatching {
    static CeoPathFinder ceoPathFinder = new CeoPathFinder();
    static HashMap<String,NarrativeChain> chains;
    static HashMap<String,ArrayList<String>> verbs;
    static boolean DEBUG = false;

    static String testParameters = "--ceo-lexicon /Users/piek/Desktop/Roxane/CEO-lexicon/ceo-lexicon-ecb-v1.txt " +
               "--ceo-ontology /Users/piek/Desktop/Roxane/CEO.v1.0/CEO_version_1.owl " +
               "--ontology-depth 1 " +
               "--property-threshold 1 " +
               "--chains /Code/vu/ceopathfinder/resources/narrativechains/EventChains_JurChamb_CEO.rtf " +
              // "--intermediate 1 " +
               "--debug";

    /**
     * Annotates the narrative chains with CEO relations
     * @param args
     */

    static public void main (String[] args) {
        String chainpath = "";
        String pathToCeo = "";
        String lexiconPath = "";

        /// CEO properties
        int rule = 0; // 0 = full assertion, 1 = property, 2 = subject-property, 3 = subject - property - object
        int intermediate = 0;
        Integer threshold = 1;
        int depth = 0;


        if (args.length==0) {
            args = testParameters.split(" ");
        }
        for (int i = 0; i < args.length; i++) {
               String arg = args[i];
               if (arg.equalsIgnoreCase("--ceo-lexicon") && args.length>(i+1)){
                   lexiconPath = args[i+1];
               }
               else if (arg.equalsIgnoreCase("--ceo-ontology") && args.length>(i+1)){
                   pathToCeo = args[i+1];
               }
               else if (arg.equalsIgnoreCase("--ontology-depth") && args.length>(i+1)){
                   depth = Integer.parseInt(args[i+1]);
               }
               else if (arg.equalsIgnoreCase("--property-threshold") && args.length>(i+1)){
                   threshold = Integer.parseInt(args[i+1]);
               }
               else if (arg.equalsIgnoreCase("--rule") && args.length>(i+1)){
                   rule = Integer.parseInt(args[i+1]);
               }
               else if (arg.equalsIgnoreCase("--chains") && args.length>(i+1)){
                   chainpath = args[i+1];
               }
               else if (arg.equalsIgnoreCase("--intermediate") && args.length>(i+1)){
                   intermediate = Integer.parseInt(args[i+1]);
               }
               else if (arg.equalsIgnoreCase("--debug")){
                   DEBUG = true;
               }
               else {
                   System.out.println("parameter value = " + arg);
               }
        }
        getNarrativeChains (chainpath);
        buildVerbIndex();
        ceoPathFinder.readLexiconFile(new File(lexiconPath));
        ceoPathFinder.readOwlFile(pathToCeo);
        ceoPathFinder.setDuring(true);
        ceoPathFinder.setRule(rule);
        ceoPathFinder.setInheritanceDepth(depth);
        ceoPathFinder.setIntermediate(intermediate);
        ceoPathFinder.setThreshold(threshold);
        annotateNarrativeChains(chainpath);
    }

    static void buildVerbIndex() {
        verbs = new HashMap<String, ArrayList<String>>();
        Set keySet = chains.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            NarrativeChain chain = chains.get(key);
            for (int i = 0; i < chain.getEventLemmas().size(); i++) {
                String lemma = chain.getEventLemmas().get(i);
                if (verbs.containsKey(lemma)) {
                    ArrayList<String> ids = verbs.get(lemma);
                    if (!ids.contains(lemma)) {
                        ids.add(chain.getId());
                        verbs.put(lemma, ids);
                    }
                }
                else {
                    ArrayList<String> ids = new ArrayList<String>();
                    ids.add(chain.getId());
                    verbs.put(lemma, ids);
                }
            }
        }
    }

    //EventChains_JurChamb_CEO.rtf
    public static void getNarrativeChains (String chainFilePath) {
        chains = new HashMap<String, NarrativeChain>();
        if (new File(chainFilePath).exists() ) {
            try {
                FileInputStream fis = new FileInputStream(chainFilePath);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                Integer chainId = 0;
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    //System.out.println(inputLine);
                    if (inputLine.trim().startsWith("Events:")) {
                        chainId++;
                        ArrayList<String> verbsArrayList = new ArrayList<String>();
                        String [] verbs = inputLine.split(" ");
                        for (int i = 1; i < verbs.length; i++) {
                            String verb = verbs[i];
                            if (verb.endsWith("\\")) verb = verb.substring(0, verb.length()-1);
                            verbsArrayList.add(verb);
                        }
                        NarrativeChain narrativeChain = new NarrativeChain(chainId.toString(), verbsArrayList);
                        chains.put(chainId.toString(), narrativeChain);
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void annotateNarrativeChains (String chainFilePath) {
        if (new File(chainFilePath).exists() ) {
            try {
                OutputStream fos = new FileOutputStream(chainFilePath+".ceo");
                FileInputStream fis = new FileInputStream(chainFilePath);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                Integer chainId = 0;
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    //System.out.println(inputLine);
                    if (inputLine.trim().startsWith("Events:")) {
                        chainId++;
                        String [] verbs = inputLine.split(" ");
                        inputLine += "\n";
                        fos.write(inputLine.getBytes());
                        for (int i = 0; i < verbs.length; i++) {
                            String verb1 = verbs[i];
                            for (int j = i+1; j < verbs.length; j++) {
                                String verb2 = verbs[j];
                                if (ceoPathFinder.areCircumstantial(verb1, verb2)) {
                                   String str = "\t"+getMatchString(verb1, verb2)+"\n";
                                   fos.write(str.getBytes());
                                }

                            }
                        }
                    }
                    else {
                       inputLine += "\n";
                       fos.write(inputLine.getBytes());
                    }
                }
                fos.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static String getMatchString (String verb1, String verb2) {
        ArrayList<String> mention1Classes = ceoPathFinder.ceoLexicon.get(verb1.toLowerCase());
        ArrayList<String> mention2Classes = ceoPathFinder.ceoLexicon.get(verb2.toLowerCase());
        ArrayList<String> matches1 = ceoPathFinder.pathValuesForTypes(mention1Classes, mention2Classes);
        ArrayList<String> matches2 = ceoPathFinder.pathValuesForTypes(mention2Classes, mention1Classes);
        String str = "";
       // System.out.println("DEBUG = " + DEBUG);
        if (matches1.size() > matches2.size()) {
            str = verb1 + "\t" + verb2 + "\tHCPE";
            if (DEBUG) str += "\t"  + matches1.toString();
        }
        else if (matches2.size() > matches1.size()) {
            str = verb2 + "\t" + verb1 + "\tHCPE";
            if (DEBUG) str += "\t" + matches2.toString();
        }
        return str;
    }


    static String anyMentionNarrativeChainMatch (ArrayList<Mention> mentions) {
        String result = "";
        HashMap<String, ArrayList<String>> keyResults = new HashMap<String, ArrayList<String>>();
        for (int i = 0; i < mentions.size(); i++) {
            Mention mention1 = mentions.get(i);
            if (verbs.containsKey(mention1.getWord().toLowerCase())) {
                for (int j = 0; j < mentions.size(); j++) {
                    Mention mention2 = mentions.get(j);
                    if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                            !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                        if (verbs.containsKey(mention2.getWord().toLowerCase())) {
                            ArrayList<String> ids1 = verbs.get(mention1.getWord().toLowerCase());
                            ArrayList<String> ids2 = verbs.get(mention2.getWord().toLowerCase());
                            for (int k = 0; k < ids1.size(); k++) {
                                String id = ids1.get(k);
                                if (ids2.contains(id)) {
                                    addNarrativeChainResult(keyResults, mention1, mention2, id);
                                }
                            }
                        }
                    }
                }
            }
        }

        result =  MentionReader.makeResult (keyResults);

        return result;
    }

    static String sameSentenceMentionNarrativeChainMatch (ArrayList<Mention> mentions) {
        String result = "";
        HashMap<String, ArrayList<String>> keyResults = new HashMap<String, ArrayList<String>>();
        for (int i = 0; i < mentions.size(); i++) {
            Mention mention1 = mentions.get(i);
            if (verbs.containsKey(mention1.getWord().toLowerCase())) {
                for (int j = 0; j < mentions.size(); j++) {
                    Mention mention2 = mentions.get(j);
                    if (mention1.getSentence()==mention2.getSentence()) {
                        if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                                !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                            if (verbs.containsKey(mention2.getWord().toLowerCase())) {
                                ArrayList<String> ids1 = verbs.get(mention1.getWord().toLowerCase());
                                ArrayList<String> ids2 = verbs.get(mention2.getWord().toLowerCase());
                                for (int k = 0; k < ids1.size(); k++) {
                                    String id = ids1.get(k);
                                    if (ids2.contains(id)) {
                                        addNarrativeChainResult(keyResults, mention1, mention2, id);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        result =  MentionReader.makeResult (keyResults);
        return result;
    }

    static String twoSentenceMentionNarrativeChainMatch (ArrayList<Mention> mentions) {
        String result = "";
        HashMap<String, ArrayList<String>> keyResults = new HashMap<String, ArrayList<String>>();
        for (int i = 0; i < mentions.size(); i++) {
            Mention mention1 = mentions.get(i);
            if (verbs.containsKey(mention1.getWord().toLowerCase())) {
                for (int j = 0; j < mentions.size(); j++) {
                    Mention mention2 = mentions.get(j);
                    if (mention1.getSentence()==mention2.getSentence() ||
                            mention1.getSentence()==mention2.getSentence()-1 ||
                            mention1.getSentence()==mention2.getSentence()+1
                            ) {
                        if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                                !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                            if (verbs.containsKey(mention2.getWord().toLowerCase())) {
                                ArrayList<String> ids1 = verbs.get(mention1.getWord().toLowerCase());
                                ArrayList<String> ids2 = verbs.get(mention2.getWord().toLowerCase());
                                for (int k = 0; k < ids1.size(); k++) {
                                    String id = ids1.get(k);
                                    if (ids2.contains(id)) {
                                        addNarrativeChainResult(keyResults, mention1, mention2, id);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        result = MentionReader.makeResult (keyResults);
        return result;
    }

    static String fourSentenceMentionNarrativeChainMatch (ArrayList<Mention> mentions) {
         String result = "";
         HashMap<String, ArrayList<String>> keyResults = new HashMap<String, ArrayList<String>>();
         for (int i = 0; i < mentions.size(); i++) {
             Mention mention1 = mentions.get(i);
             if (verbs.containsKey(mention1.getWord().toLowerCase())) {
                 for (int j = 0; j < mentions.size(); j++) {
                     Mention mention2 = mentions.get(j);
                     if (mention1.getSentence()==mention2.getSentence() ||
                             mention1.getSentence()==mention2.getSentence()-1 ||
                             mention1.getSentence()==mention2.getSentence()+1 ||
                             mention1.getSentence()==mention2.getSentence()-2 ||
                             mention1.getSentence()==mention2.getSentence()+2 ||
                             mention1.getSentence()==mention2.getSentence()-3 ||
                             mention1.getSentence()==mention2.getSentence()+3

                             ) {
                         if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                                 !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                             if (verbs.containsKey(mention2.getWord().toLowerCase())) {
                                 ArrayList<String> ids1 = verbs.get(mention1.getWord().toLowerCase());
                                 ArrayList<String> ids2 = verbs.get(mention2.getWord().toLowerCase());
                                 for (int k = 0; k < ids1.size(); k++) {
                                     String id = ids1.get(k);
                                     if (ids2.contains(id)) {
                                         addNarrativeChainResult(keyResults, mention1, mention2, id);
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
         result =  MentionReader.makeResult (keyResults);
         return result;
     }
    static void addNarrativeChainResult(HashMap<String, ArrayList<String>> keyResults,
                                           Mention mention1,
                                           Mention mention2,
                                           String chainId) {
           String str = "";
           String key = "";
           String chain = chains.get(chainId).eventLemmas.toString();
          // System.out.println("DEBUG = " + DEBUG);
           //// token fallback
           if (mention1.getToken() < mention2.getToken()) {
               key = mention1.toString() + mention2.toString();
               str = mention1.toString() + "\t" + mention2.toString() + "\tHCPE";
               str += "\t" + chain;
           }
           else if (mention2.getToken() < mention1.getToken()) {
               key = mention2.toString() + mention1.toString();
               str = mention2.toString() + "\t" + mention1.toString() + "\tHCPE";
               str += "\t" + chain;
           }
        MentionReader.addResult(keyResults, mention1, mention2, str, key);
       }
}
