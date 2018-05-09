import java.io.*;
import java.util.*;

/**
 * @TODO
 * - read gold instead of the mention list to be able to do instance and mention based mapping
 * - output per heuristiek post-pre, post-dur, dur-pre
 * - narrative chains versie
 */


public class MentionReader {
    static CeoPathFinder ceoPathFinder = new CeoPathFinder();
    static Integer threshold = 1;
    static int deep = 0;
    static boolean EXPAND = false;
    static boolean DEBUG = false;
    static int METHOD = -1;   // 0 = BASELINE, 1 = CEO, 2 = NarrativeChains, 3 = FBK PRO, 4 = Framenet cause
    static public int rule = 0; // 0 = full assertion, 1 = property, 2 = subject-property, 3 = subject - property - object
    static int intermediate = 0;
    static HashMap<String, ArrayList<String>> mentionInstanceMap = null;
    static HashMap<String, ArrayList<String>> instancMentionMap = null;
    static HashMap<String,NarrativeChain> chains = null;
    static HashMap<String,ArrayList<String>> verbs = null;

    static HashMap<String, Integer> OOV = new HashMap<String, Integer>();
    ///Users/piek/Desktop/Roxane/Tommaso-v3/not-connected-events/3_1ecb.xml_not-connected-events.eval

    static String testCeoParameters = "--ceo-lexicon /Users/piek/Desktop/Roxane/CEO-lexicon/ceo-lexicon-ecb-v1.txt " +
            "--ceo-ontology /Users/piek/Desktop/Roxane/CEO.v1.0/CEO_version_1.owl " +
            "--ontology-depth 1 " +
            "--property-threshold 1 " +
            "--input /Users/piek/Desktop/Roxane/Tommaso-v5/gold " +
            "--output /Users/piek/Desktop/Roxane/Tommaso-v5/out " +
            "--intermediate 1 " +
            "--gold " +
            "--debug";

    static String testNCParameters = "--chains /Code/vu/ceopathfinder/resources/narrativechains/EventChains_JurChamb_CEO.rtf " +
            "--input /Users/piek/Desktop/Roxane/Tommaso-v5/gold " +
            "--output /Users/piek/Desktop/Roxane/Tommaso-v5/out " +
            "--gold " +
            "--debug";

    static public void main (String[] args) {
        String lexiconPath = "";
        String pathToCeo = "";
        String mentionFolder = "";
        String outputFolder = "";
        String chainpath = "";
        if (args.length==0) {
            //args = testCeoParameters.split(" ");
            args = testNCParameters.split(" ");
        }
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            //System.out.println("parameter value = " + arg);

            if (arg.equalsIgnoreCase("--method") && args.length>(i+1)){
                METHOD = Integer.parseInt(args[i+1]);
            }
            else if (arg.equalsIgnoreCase("--ceo-lexicon") && args.length>(i+1)){
                lexiconPath = args[i+1];
            }
            else if (arg.equalsIgnoreCase("--ceo-ontology") && args.length>(i+1)){
                pathToCeo = args[i+1];
            }
            else if (arg.equalsIgnoreCase("--chains") && args.length>(i+1)){
                chainpath = args[i+1];
            }
            else if (arg.equalsIgnoreCase("--ontology-depth") && args.length>(i+1)){
                deep = Integer.parseInt(args[i+1]);
            }
            else if (arg.equalsIgnoreCase("--rule") && args.length>(i+1)){
                rule = Integer.parseInt(args[i+1]);
            }
            else if (arg.equalsIgnoreCase("--property-threshold") && args.length>(i+1)){
                threshold = Integer.parseInt(args[i+1]);
            }
            else if (arg.equalsIgnoreCase("--input") && args.length>(i+1)){
                mentionFolder = args[i+1];
            }
            else if (arg.equalsIgnoreCase("--output") && args.length>(i+1)){
                outputFolder = args[i+1];
                File out = new File(outputFolder);
                if (!out.exists()) {
                    out.mkdir();
                }
            }
            else if (arg.equalsIgnoreCase("--intermediate") && args.length>(i+1)){
                intermediate = Integer.parseInt(args[i+1]);
            }
            else if (arg.equalsIgnoreCase("--debug")){
                DEBUG = true;
            }
            else if (arg.equalsIgnoreCase("--expand")){
                EXPAND = true;
            }
            else {
            }
        }

        if (METHOD==1) {
            if (!pathToCeo.isEmpty() && !lexiconPath.isEmpty()) {
                ceoPathFinder.readLexiconFile(new File(lexiconPath));
                ceoPathFinder.readOwlFile(pathToCeo);
                ceoPathFinder.setDuring(true);
                ceoPathFinder.setRule(0); //// property predicate only
                ceoPathFinder.setInheritanceDepth(deep);
            }
            else {
                if (pathToCeo.isEmpty()) System.err.println("No CEO ontology provided");
                if (lexiconPath.isEmpty()) System.err.println("No CEO lexicon provided");
            }
        }
        else if (METHOD==2) {
            if (!chainpath.isEmpty() && METHOD == 2) {
                chains = NarrativeChains.getNarrativeChains(chainpath);
                verbs = NarrativeChains.buildVerbIndex(chains);
                System.out.println("chains.size() = " + chains.size());
                System.out.println("verbs.size() = " + verbs.size());
            }
            else {
                System.err.println("No narrative chains provided");
            }
        }
        else {

        }

         ArrayList<File> files = Util.makeRecursiveFileList(new File(mentionFolder), ".eval");
         for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            //if (!file.getName().equals("19_1ecb.xml_all_event_mentions.eval"))  continue;
            //System.out.println("file.getName() = " + file.getName());

            ArrayList<Gold> gold = new ArrayList<Gold>();
            ArrayList<Mention> mentions = new ArrayList<Mention>();
            mentionInstanceMap = new HashMap<String, ArrayList<String>>();
            instancMentionMap = new HashMap<String, ArrayList<String>>();
            gold = readFileToGoldList(file);
            mentions = getMentionsFromGold(gold);

            //  mentions = readFileToMentionList(file);  /// @deprecated

            /////// BASELINE //////////////
            if (METHOD ==0) {
                String ceoResultB1 = sameSentenceMentionBaselineMatch(mentions);
                try {
                    OutputStream fos = new FileOutputStream(outputFolder+"/"+file.getName() + ".bl1S");
                    fos.write(ceoResultB1.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String ceoResultB3 = twoSentenceMentionBaselineMatch(mentions);
                try {
                    OutputStream fos = new FileOutputStream(outputFolder+"/"+file.getName() + ".bl3S");
                    fos.write(ceoResultB3.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String ceoResultB5 = fourSentenceMentionBaselineMatch(mentions);
                try {
                    OutputStream fos = new FileOutputStream(outputFolder+"/"+file.getName() + ".bl5S");
                    fos.write(ceoResultB5.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String ceoResultBAny = allMentionBaselineMatch(mentions);
                try {
                    OutputStream fos = new FileOutputStream(outputFolder+"/"+file.getName() + ".blANY");
                    fos.write(ceoResultBAny.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
             }
             else if (METHOD==1) {  /// CEO
                 String ceoResultAny = anyMentionCeoMatch(mentions, threshold);
                 try {
                     OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".ceoANY");
                     fos.write(ceoResultAny.getBytes());
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }

                 String ceoResult1 = sameSentenceMentionCeoMatch(mentions, threshold);
                 try {
                     OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".ceo1S");
                     fos.write(ceoResult1.getBytes());
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }

                 String ceoResult3 = twoSentenceMentionCeoMatch(mentions, threshold);
                 try {
                     OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".ceo3S");
                     fos.write(ceoResult3.getBytes());
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 String ceoResult5 = fourSentenceMentionCeoMatch(mentions, threshold);
                 try {
                     OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".ceo5S");
                     fos.write(ceoResult5.getBytes());
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
             else if (METHOD == 2) {  /// NarrativeChains
                 String ceoResultAny = anyMentionNarrativeChainMatch(mentions, threshold);
                 try {
                     OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".ncANY");
                     fos.write(ceoResultAny.getBytes());
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }

                 String ceoResult1 = sameSentenceMentionNarrativeChainMatch(mentions, threshold);
                 try {
                     OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".nc1S");
                     fos.write(ceoResult1.getBytes());
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }

                 String ceoResult3 = twoSentenceMentionNarrativeChainMatch(mentions, threshold);
                 try {
                     OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".nc3S");
                     fos.write(ceoResult3.getBytes());
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 String ceoResult5 = fourSentenceMentionNarrativeChainMatch(mentions, threshold);
                 try {
                     OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".nc5S");
                     fos.write(ceoResult5.getBytes());
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
             else if (METHOD==3) { //FBK PRO cause

             }
             else if (METHOD==4) { //FrameNet cause

             }
         }

/*        for (Map.Entry<String, Integer> entry : OOV.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
         }*/

    }
 
    //22	6:0:died	921:47:buried	HCPE	112	56	[false#undergoer#exist, true#part#isDamaged, undergoer#hasPart#part, damage#hasNegativeEffectOn#activity, undergoer#hasDamage#damage, true#undergoer#isDamaged, side_1#inConflictWith#side_2, side_1#hasIssue#issue, side_1#atPlace#place, true#side_1#inMeeting, true#side_2#inConflict, true#side_1#inConflict, side_1#hasPurpose#purpose, true#translocation-theme#inMotion, translocation-theme#uses#place, false#place#inFunction, translocation-theme#atPlace#place, false#translocation-theme#inMotion, true#place#isBlocked, entity#atPlace#place, translocation-theme#notAtPlace#place, convict#committedOffense#offense, convict#hasConviction#conviction, true#convict#isConvicted, true#convict#inCaptivity, value-attribute#hasValue#value, true#entity#exist, false#place#isBlocked, true#place#inFunction, translocation-theme#notUses#place, agent#blocks#place, agent#hasPurpose#purpose, true#partners#inCollaboration, partners#hasProject#project, partner_1#collaboratesWith#partner_2, true#partners#inRelationship, partner_1#inRelationshipWith#partner_2, true#employee#isEmployed, employment-attribute#hasValue#employment-value, employee#hasAttribute#employment-attribute, employee#hasTask#employment-task, employee#hasFunction#employment-function, employee#employedAt#employer, suspect#suspectedOfOffense#offense, agent#examines#suspect, agent#examines#offense, true#leader-entity#isLeader, leader-entity#hasFunction#leader-function, leader-entity#isLeaderOf#leader-governed_entity, meeting-participant#atPlace#place, true#meeting-participant#inMeeting, suspect#notAtPlace#place, suspect#isChargedOf#offense, true#suspect#inCaptivity, suspect#atPlace#place]
     //32	921:47:buried	6:0:died	HCPE	56	112	[false#undergoer#exist, true#part#isDamaged, undergoer#hasPart#part, damage#hasNegativeEffectOn#activity, undergoer#hasDamage#damage, true#undergoer#isDamaged, side_1#inConflictWith#side_2, side_1#hasIssue#issue, side_1#atPlace#place, true#side_1#inMeeting, true#side_2#inConflict, true#side_1#inConflict, side_1#hasPurpose#purpose, true#translocation-theme#inMotion, translocation-theme#uses#place, false#place#inFunction, translocation-theme#atPlace#place, false#translocation-theme#inMotion, true#place#isBlocked, entity#atPlace#place, translocation-theme#notAtPlace#place, convict#committedOffense#offense, convict#hasConviction#conviction, true#convict#isConvicted, true#convict#inCaptivity, value-attribute#hasValue#value, true#entity#exist, false#place#isBlocked, true#place#inFunction, translocation-theme#notUses#place, agent#blocks#place, agent#hasPurpose#purpose, true#partners#inCollaboration, partners#hasProject#project, partner_1#collaboratesWith#partner_2, true#partners#inRelationship, partner_1#inRelationshipWith#partner_2, true#employee#isEmployed, employment-attribute#hasValue#employment-value, employee#hasAttribute#employment-attribute, employee#hasTask#employment-task, employee#hasFunction#employment-function, employee#employedAt#employer, suspect#suspectedOfOffense#offense, agent#examines#suspect, agent#examines#offense, true#leader-entity#isLeader, leader-entity#hasFunction#leader-function, leader-entity#isLeaderOf#leader-governed_entity, meeting-participant#atPlace#place, true#meeting-participant#inMeeting, suspect#notAtPlace#place, suspect#isChargedOf#offense, true#suspect#inCaptivity, suspect#atPlace#place]

    static void addCeoResult(HashMap<String, ArrayList<String>> keyResults, Mention mention1, Mention mention2) {
        ArrayList<String> mention1Classes = ceoPathFinder.ceoLexicon.get(mention1.getWord().toLowerCase());
        ArrayList<String> mention2Classes = ceoPathFinder.ceoLexicon.get(mention2.getWord().toLowerCase());
        ArrayList<String> matches1 = ceoPathFinder.pathValuesForTypes(mention1Classes, mention2Classes, intermediate);
        ArrayList<String> matches2 = ceoPathFinder.pathValuesForTypes(mention2Classes, mention1Classes, intermediate);
        String str = "";
        String key = "";
       // System.out.println("DEBUG = " + DEBUG);
        if (matches1.size() > matches2.size()) {
            key = mention1.toString() + mention2.toString();
            str = mention1.toString() + "\t" + mention2.toString() + "\tHCPE";
            str += "\t"  + matches1.toString();
        }
        else if (matches2.size() > matches1.size()) {
            key = mention2.toString() + mention1.toString();
            str = mention2.toString() + "\t" + mention1.toString() + "\tHCPE";
            str += "\t" + matches2.toString();
        }
        //// token fallback
        else if (mention1.getToken() < mention2.getToken()) {
            key = mention1.toString() + mention2.toString();
            str = mention1.toString() + "\t" + mention2.toString() + "\tHCPE";
            str += "\t" + matches1.toString();
        }
        else if (mention2.getToken() < mention1.getToken()) {
            key = mention2.toString() + mention1.toString();
            str = mention2.toString() + "\t" + mention1.toString() + "\tHCPE";
            str += "\t" + matches2.toString();
        }
        addResult(keyResults, mention1, mention2, str, key);
    }

    static void addResult (HashMap<String, ArrayList<String>> keyResults,
                           Mention mention1,
                           Mention mention2,
                           String result,
                           String key) {

        String instance  = getSharedInstance(mention1.toString(), mention2.toString());
        if (!result.isEmpty() && !instance.isEmpty()) {
            if (keyResults.containsKey(instance)) {
                ArrayList<String> results = keyResults.get(instance);
                boolean match = false;
                for (int i = 0; i < results.size(); i++) {
                    String s =  results.get(i);
                    if (s.startsWith(key)) {
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    results.add(result);
                    keyResults.put(instance, results);
                }
            }
            else {
                ArrayList<String> results = new ArrayList<String>();
                results.add(result);
                keyResults.put(instance, results);
            }
        }
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
        addResult(keyResults, mention1, mention2, str, key);
    }

    static void addBaselineResult(HashMap<String, ArrayList<String>> keyResults,
                                        Mention mention1,
                                        Mention mention2) {
        String str = "";
        String key = "";
       // System.out.println("DEBUG = " + DEBUG);
        //// token fallback
        if (mention1.getToken() < mention2.getToken()) {
            key = mention1.toString() + mention2.toString();
            str = mention1.toString() + "\t" + mention2.toString() + "\tHCPE";
            str += "\t" + "BASELINE";
        }
        else if (mention2.getToken() < mention1.getToken()) {
            key = mention2.toString() + mention1.toString();
            str = mention2.toString() + "\t" + mention1.toString() + "\tHCPE";
            str += "\t" + "BASELINE";
        }
        addResult(keyResults, mention1, mention2, str, key);
    }

    static String getSharedInstance (String m1, String m2) {
        String instance = "-1";
        if (mentionInstanceMap.containsKey(m1) && mentionInstanceMap.containsKey(m2)) {
            ArrayList<String> i1 = mentionInstanceMap.get(m1);
            ArrayList<String> i2 = mentionInstanceMap.get(m2);
            for (int i = 0; i < i1.size(); i++) {
                String s =  i1.get(i);
                if (i2.contains(s)) {
                    instance = s;
                    break;
                }
            }
        }
        return instance;
    }

    /**
     * We create the result by combining the instance key with the result,
     * Next we check if there are other mentions missed which have that key too
     * These are added as well.
     * @param keyResults
     * @return
     */
    static String makeResult (HashMap<String, ArrayList<String>> keyResults) {
       String total = "";
        for (Map.Entry<String, ArrayList<String>> entry : keyResults.entrySet()) {
            //result +=  "1" + "\t"+entry.getValue()+"\n";
            ArrayList<String>  results = entry.getValue();
            for (int i = 0; i < results.size(); i++) {
                String s =  results.get(i);
                String result =  entry.getKey() + "\t"+s+"\n";
                if (total.indexOf(result)==-1) {
                    total += result;
                }
            }
            if (EXPAND) {
                //// Get all the gold results for this instance
                if (instancMentionMap.containsKey(entry.getKey())) {
                    ArrayList<String> expand = instancMentionMap.get(entry.getKey());
                    for (int i = 0; i < expand.size(); i++) {
                        String s = expand.get(i);
                        boolean match = false;
                        for (int j = 0; j < results.size(); j++) {
                            String s1 = entry.getKey() + "\t" + results.get(j);
                            if (s1.startsWith(s)) {
                                match = true;
                                break;
                            }
                        }
                        if (!match) {
                            /// if there is not match with the system results
                            /// this pair is added
                            String result = s + "\t" + "[coreferential match]" + "\n";
                            if (total.indexOf(result) == -1) {
                                total += result;
                            }
                        }
                    }
                }
            }
        }
       return total;
    }


    static String anyMentionCeoMatch (ArrayList<Mention> mentions, Integer threshold) {
        String result = "";
        HashMap<String, ArrayList<String>> keyResults = new HashMap<String, ArrayList<String>>();
        for (int i = 0; i < mentions.size(); i++) {
            Mention mention1 = mentions.get(i);
            if (ceoPathFinder.ceoLexicon.containsKey(mention1.getWord().toLowerCase())) {
                for (int j = 0; j < mentions.size(); j++) {
                    Mention mention2 = mentions.get(j);
                    if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                            !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                        if (ceoPathFinder.areCircumstantial(mention1.getWord(), mention2.getWord(), intermediate, threshold)) {
                            addCeoResult(keyResults, mention1, mention2);
                        }
                        else {
                          //  System.out.println("OoV mention2.getWord() = " + mention2.getWord());
                        }
                    }
                }
            }
            else {
                if (OOV.containsKey(mention1.getWord())) {
                    Integer cnt = OOV.get(mention1.getWord());
                    cnt++;
                    OOV.put(mention1.getWord(), cnt);
                }
                else {
                    OOV.put(mention1.getWord(), 1);
                }
              //  System.out.println("OoV mention1.getWord() = " + mention1.getWord());
            }
        }

        result = makeResult (keyResults);

        return result;
    }

    static String sameSentenceMentionCeoMatch (ArrayList<Mention> mentions, Integer threshold) {
        String result = "";
        HashMap<String, ArrayList<String>> keyResults = new HashMap<String, ArrayList<String>>();
        for (int i = 0; i < mentions.size(); i++) {
            Mention mention1 = mentions.get(i);
            if (ceoPathFinder.ceoLexicon.containsKey(mention1.getWord().toLowerCase())) {
                for (int j = 0; j < mentions.size(); j++) {
                    Mention mention2 = mentions.get(j);
                    if (mention1.getSentence()==mention2.getSentence()) {
                        if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                                !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                            if (ceoPathFinder.areCircumstantial(mention1.getWord(), mention2.getWord(), intermediate, threshold)) {
                                addCeoResult(keyResults, mention1, mention2);
                            }
                            else {
                              //  System.out.println("OoV mention2.getWord() = " + mention2.getWord());
                            }
                        }
                    }
                }
            }
        }
        result = makeResult (keyResults);
        return result;
    }

    static String twoSentenceMentionCeoMatch (ArrayList<Mention> mentions, Integer threshold) {
        String result = "";
        HashMap<String, ArrayList<String>> keyResults = new HashMap<String, ArrayList<String>>();
        for (int i = 0; i < mentions.size(); i++) {
            Mention mention1 = mentions.get(i);
            if (ceoPathFinder.ceoLexicon.containsKey(mention1.getWord().toLowerCase())) {
                for (int j = 0; j < mentions.size(); j++) {
                    Mention mention2 = mentions.get(j);
                    if (mention1.getSentence()==mention2.getSentence() ||
                            mention1.getSentence()==mention2.getSentence()-1 ||
                            mention1.getSentence()==mention2.getSentence()+1
                            ) {
                        if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                                !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                            if (ceoPathFinder.areCircumstantial(mention1.getWord(), mention2.getWord(), intermediate, threshold)) {
                                addCeoResult(keyResults, mention1, mention2);
                            }
                            else {
                              //  System.out.println("OoV mention2.getWord() = " + mention2.getWord());
                            }
                        }
                    }
                }
            }
        }
        result = makeResult (keyResults);
        return result;
    }

    static String fourSentenceMentionCeoMatch (ArrayList<Mention> mentions, Integer threshold) {
         String result = "";
        HashMap<String, ArrayList<String>> keyResults = new HashMap<String, ArrayList<String>>();
         for (int i = 0; i < mentions.size(); i++) {
             Mention mention1 = mentions.get(i);
             if (ceoPathFinder.ceoLexicon.containsKey(mention1.getWord().toLowerCase())) {
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
                             if (ceoPathFinder.areCircumstantial(mention1.getWord(), mention2.getWord(), intermediate, threshold)) {
                                 addCeoResult(keyResults, mention1, mention2);
                             }
                             else {
                               //  System.out.println("OoV mention2.getWord() = " + mention2.getWord());
                             }
                         }
                     }
                 }
             }
         }
         result = makeResult (keyResults);
         return result;
     }

     static String anyMentionNarrativeChainMatch (ArrayList<Mention> mentions, Integer threshold) {
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

        result = makeResult (keyResults);

        return result;
    }

    static String sameSentenceMentionNarrativeChainMatch (ArrayList<Mention> mentions, Integer threshold) {
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
        result = makeResult (keyResults);
        return result;
    }

    static String twoSentenceMentionNarrativeChainMatch (ArrayList<Mention> mentions, Integer threshold) {
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
        result = makeResult (keyResults);
        return result;
    }

    static String fourSentenceMentionNarrativeChainMatch (ArrayList<Mention> mentions, Integer threshold) {
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
         result = makeResult (keyResults);
         return result;
     }

    static String sameSentenceMentionBaselineMatch (ArrayList<Mention> mentions) {
        String result = "";
        HashMap<String, ArrayList<String>> keyResults = new HashMap<String, ArrayList<String>>();
        for (int i = 0; i < mentions.size(); i++) {
            Mention mention1 = mentions.get(i);
            for (int j = 0; j < mentions.size(); j++) {
                Mention mention2 = mentions.get(j);
                if (mention1.getSentence()==mention2.getSentence()
                        ) {
                    if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                            !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                        addBaselineResult(keyResults, mention1, mention2);
                    }
                }
            }
        }
        result = makeResult (keyResults);
        return result;
    }

    static String twoSentenceMentionBaselineMatch (ArrayList<Mention> mentions) {
        String result = "";
        HashMap<String, ArrayList<String>> keyResults = new HashMap<String, ArrayList<String>>();
        for (int i = 0; i < mentions.size(); i++) {
            Mention mention1 = mentions.get(i);
            for (int j = 0; j < mentions.size(); j++) {
                Mention mention2 = mentions.get(j);
                if (mention1.getSentence()==mention2.getSentence() ||
                        mention1.getSentence()==mention2.getSentence()-1 ||
                        mention1.getSentence()==mention2.getSentence()+1
                        ) {
                    if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                            !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                        addBaselineResult(keyResults, mention1, mention2);
                    }
                }
            }
        }
        result = makeResult (keyResults);
        return result;
    }

    static String fourSentenceMentionBaselineMatch (ArrayList<Mention> mentions) {
        String result = "";
        HashMap<String, ArrayList<String>> keyResults = new HashMap<String, ArrayList<String>>();
        for (int i = 0; i < mentions.size(); i++) {
            Mention mention1 = mentions.get(i);
            for (int j = 0; j < mentions.size(); j++) {
                Mention mention2 = mentions.get(j);
                if (mention1.getSentence()==mention2.getSentence() ||
                        mention1.getSentence()==mention2.getSentence()-1 ||
                        mention1.getSentence()==mention2.getSentence()+1 ||
                        mention1.getSentence()==mention2.getSentence()-2 ||
                        mention1.getSentence()==mention2.getSentence()+2
                        ) {
                    if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                            !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                        addBaselineResult(keyResults, mention1, mention2);
                    }
                }
            }
        }
        result = makeResult (keyResults);
        return result;
    }

    static String allMentionBaselineMatch (ArrayList<Mention> mentions) {
        String result = "";
        HashMap<String, ArrayList<String>> keyResults = new HashMap<String, ArrayList<String>>();
        for (int i = 0; i < mentions.size(); i++) {
            Mention mention1 = mentions.get(i);
            for (int j = 0; j < mentions.size(); j++) {
                Mention mention2 = mentions.get(j);
                if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                        !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                    addBaselineResult(keyResults, mention1, mention2);
                }
            }
        }
        result = makeResult (keyResults);
        return result;
    }





    static ArrayList<Mention> readFileToMentionList (File file) {
        ArrayList<Mention> mentionArrayList = new ArrayList<Mention>();
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                while (in.ready() && (inputLine = in.readLine()) != null) {
                    if (inputLine.trim().length() > 0) {
                        Mention mention = new Mention(inputLine);
                        mentionArrayList.add(mention);
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Cannot find file = " + file.getAbsolutePath());
        }
        return mentionArrayList;
    }

    static void addInstance(String instance, Mention mention) {
        if (mentionInstanceMap.containsKey(mention.toString())) {
            ArrayList<String> instances = mentionInstanceMap.get(mention.toString());
            if (!instances.contains(instance)) {
                instances.add(instance);
                mentionInstanceMap.put(mention.toString(), instances);
            }
        }
        else {
            ArrayList<String> instances = new ArrayList<String>();
            instances.add(instance);
            mentionInstanceMap.put(mention.toString(), instances);
        }
    }

    static void addMentions(String instance, String pair) {
        if (instancMentionMap.containsKey(instance)) {
            ArrayList<String> mentions = instancMentionMap.get(instance);
            if (!mentions.contains(pair)) {
                mentions.add(pair);
                instancMentionMap.put(instance, mentions);
            }
        }
        else {
            ArrayList<String> mentions = new ArrayList<String>();
            mentions.add(pair);
            instancMentionMap.put(instance, mentions);
        }
    }

    static ArrayList<Gold> readFileToGoldList (File file) {
        ArrayList<Gold> goldArrayList = new ArrayList<Gold>();
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                while (in.ready() && (inputLine = in.readLine()) != null) {
                    if (inputLine.trim().length() > 0) {
                        Gold gold = new Gold(inputLine);
                        addMentions(gold.id, inputLine.trim());
                        addInstance(gold.id, gold.mention1);
                        addInstance(gold.id, gold.mention2);
                        goldArrayList.add(gold);
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Cannot find file = " + file.getAbsolutePath());
        }
        return goldArrayList;
    }

    static ArrayList<Mention> getMentionsFromGold (ArrayList<Gold> goldList) {
        ArrayList<Mention> mentions = new ArrayList<Mention>();
        for (int i = 0; i < goldList.size(); i++) {
            Gold gold = goldList.get(i);
            addmention(mentions, gold.mention1);
            addmention(mentions, gold.mention2);

        }
        return mentions;
    }

    static void addmention (ArrayList<Mention> mentions, Mention aMention)  {
        boolean match = false;
        for (int j = 0; j < mentions.size(); j++) {
            Mention mention = mentions.get(j);
            if (mention.toString().equals(aMention.toString())) {
              match = true; break;
            }
        }
        if (!match) mentions.add(aMention);
    }


}
