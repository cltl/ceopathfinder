import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class NarrativeChains {
    static CeoPathFinder ceoPathFinder = new CeoPathFinder();
    static HashMap<String,NarrativeChain> chains;
    static HashMap<String,ArrayList<String>> verbs;
    static Integer threshold = 1;
    static int deep = 0;
    static boolean BASELINE = true;
    static boolean DEBUG = false;
    static public int rule = 0; // 0 = full assertion, 1 = property, 2 = subject-property, 3 = subject - property - object
    static int intermediate = 0;

    static String testParameters = "--ceo-lexicon /Users/piek/Desktop/Roxane/CEO-lexicon/ceo-lexicon-ecb-v1.txt " +
               "--ceo-ontology /Users/piek/Desktop/Roxane/CEO.v1.0/CEO_version_1.owl " +
               "--ontology-depth 1 " +
               "--property-threshold 1 " +
               "--chains /Code/vu/ceopathfinder/resources/narrativechains/EventChains_JurChamb_CEO.rtf " +
              // "--intermediate 1 " +
               "--debug";

    //public HashMap<Integer, ArrayList<String>> verbMap;
    static public void main (String[] args) {
        String chainpath = "";
        String pathToCeo = "";
        String lexiconPath = "";
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
                   deep = Integer.parseInt(args[i+1]);
               }
               else if (arg.equalsIgnoreCase("--property-threshold") && args.length>(i+1)){
                   threshold = Integer.parseInt(args[i+1]);
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
        chains = getNarrativeChains (chainpath);
        verbs = buildVerbIndex(chains);
        ceoPathFinder.readLexiconFile(new File(lexiconPath));
        ceoPathFinder.readOwlFile(pathToCeo);
        ceoPathFinder.setDuring(true);
        ceoPathFinder.setRule(0); //// property predicate only
        ceoPathFinder.setInheritanceDepth(deep);
        annotateNarrativeChains(chainpath);
    }

    static HashMap<String,ArrayList<String>> buildVerbIndex(HashMap<String, NarrativeChain> chains) {
        HashMap<String,ArrayList<String>> verbChainMap = new HashMap<String, ArrayList<String>>();
        Set keySet = chains.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            NarrativeChain chain = chains.get(key);
            for (int i = 0; i < chain.getEventLemmas().size(); i++) {
                String lemma = chain.getEventLemmas().get(i);
                if (verbChainMap.containsKey(lemma)) {
                    ArrayList<String> ids = verbChainMap.get(lemma);
                    if (!ids.contains(lemma)) {
                        ids.add(chain.getId());
                        verbChainMap.put(lemma, ids);
                    }
                }
                else {
                    ArrayList<String> ids = new ArrayList<String>();
                    ids.add(chain.getId());
                    verbChainMap.put(lemma, ids);
                }
            }
        }
        return verbChainMap;
    }

    //EventChains_JurChamb_CEO.rtf
    public static HashMap<String, NarrativeChain> getNarrativeChains (String chainFilePath) {
        HashMap<String, NarrativeChain> chains = new HashMap<String, NarrativeChain>();
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
                        for (int i = 0; i < verbs.length; i++) {
                            String verb = verbs[i];
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
        return chains;
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
                                if (ceoPathFinder.areCircumstantial(verb1, verb2, intermediate, threshold)) {
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
        ArrayList<String> matches1 = ceoPathFinder.pathValuesForTypes(mention1Classes, mention2Classes, intermediate);
        ArrayList<String> matches2 = ceoPathFinder.pathValuesForTypes(mention2Classes, mention1Classes, intermediate);
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


    /**
     * for (int i = 1; i < verbs.length; i++) { /// skipping Events:
                                 String verb = verbs[i];
                                 verbsArrayList.add(verb);
                                 if (chainMap.containsKey(verb)) {
                                     ArrayList<Integer> chains = chainMap.get(verb);
                                     chains.add(chainId);
                                     chainMap.put(verb, chains);
                                 }
                                 else {
                                     ArrayList<Integer> chains = new ArrayList<Integer>();
                                     chains.add(chainId);
                                     chainMap.put(verb, chains);
                                 }
                             }
                             verbMap.put(chainId, verbsArrayList);
     */
}
