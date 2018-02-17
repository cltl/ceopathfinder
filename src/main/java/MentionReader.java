import java.io.*;
import java.util.*;

public class MentionReader {
    static HashMap<String, ArrayList<String>> ceoLexicon = new HashMap<String, ArrayList<String>>();
    static CeoPathFinder ceoPathFinder = new CeoPathFinder();
    static Integer threshold = 1;
    static int deep = 0;
    static boolean BASELINE = false;
    static boolean DEBUG = false;
    static public int rule = 0; // 0 = full assertion, 1 = property, 2 = subject-property, 3 = subject - property - object

    static HashMap<String, Integer> OOV = new HashMap<String, Integer>();
    ///Users/piek/Desktop/Roxane/Tommaso-v3/not-connected-events/3_1ecb.xml_not-connected-events.eval
    static public void main (String[] args) {
         String lexiconPath = "/Users/piek/Desktop/Roxane/CEO-lexicon/ceo-lexicon-ecb-v1.txt";
         ceoLexicon = readLexiconFile(new File(lexiconPath));
         String pathToCeo = "/Users/piek/Desktop/Roxane/CEO.v1.0/CEO_version_1.owl";
         ceoPathFinder.readOwlFile(pathToCeo);

        ceoPathFinder.setDuring(true);
        ceoPathFinder.setRule(0); //// property predicate only

         if (deep==2) {
            ceoPathFinder.interpretOntologyWithInheritance("Physical");
         }
         else if (deep==1){
            ceoPathFinder.interpretOntologyWithParent("Physical");
         }
         else {
            ceoPathFinder.interpretOntology("Physical");
         }
         String mentionFolder = "/Users/piek/Desktop/Roxane/Tommaso-v4/all_event_mentions";
         ArrayList<File> files = makeRecursiveFileList(new File(mentionFolder), ".eval");
         for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            //if (!file.getName().equals("19_1ecb.xml_all_event_mentions.eval"))  continue;
            System.out.println("file.getName() = " + file.getName());

            ArrayList<Mention> mentions = readFileToMentionList(file);

            /////// BASELINE //////////////
            if (BASELINE) {
                String ceoResultB1 = sameSentenceMentionBaselineMatch(mentions);
                try {
                    OutputStream fos = new FileOutputStream(file.getAbsoluteFile() + ".bl1");
                    fos.write(ceoResultB1.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String ceoResultB3 = twoSentenceMentionBaselineMatch(mentions);
                try {
                    OutputStream fos = new FileOutputStream(file.getAbsoluteFile() + ".bl3");
                    fos.write(ceoResultB3.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String ceoResultB5 = fourSentenceMentionBaselineMatch(mentions);
                try {
                    OutputStream fos = new FileOutputStream(file.getAbsoluteFile() + ".bl5");
                    fos.write(ceoResultB5.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String ceoResultBAny = allMentionBaselineMatch(mentions);
                try {
                    OutputStream fos = new FileOutputStream(file.getAbsoluteFile() + ".blany");
                    fos.write(ceoResultBAny.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
             }
             String ceoResultAny = anyMentionCeoMatch(mentions, threshold);
             try {
                  OutputStream fos = new FileOutputStream(file.getAbsoluteFile()+".ceoANY");
                  fos.write(ceoResultAny.getBytes());
                  fos.close();
             } catch (IOException e) {
                  e.printStackTrace();
             }

             String ceoResult1 = sameSentenceMentionCeoMatch(mentions, threshold);
             try {
                  OutputStream fos = new FileOutputStream(file.getAbsoluteFile()+".ceo1S");
                  fos.write(ceoResult1.getBytes());
                  fos.close();
             } catch (IOException e) {
                  e.printStackTrace();
             }

             String ceoResult3 = twoSentenceMentionCeoMatch(mentions, threshold);
             try {
                  OutputStream fos = new FileOutputStream(file.getAbsoluteFile()+".ceo3S");
                  fos.write(ceoResult3.getBytes());
                  fos.close();
             } catch (IOException e) {
                  e.printStackTrace();
             }
             String ceoResult5 = fourSentenceMentionCeoMatch(mentions, threshold);
             try {
                  OutputStream fos = new FileOutputStream(file.getAbsoluteFile()+".ceo5S");
                  fos.write(ceoResult5.getBytes());
                  fos.close();
             } catch (IOException e) {
                  e.printStackTrace();
             }
            // break;
         }

/*        for (Map.Entry<String, Integer> entry : OOV.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
         }*/

    }
 
    //22	6:0:died	921:47:buried	HCPE	112	56	[false#undergoer#exist, true#part#isDamaged, undergoer#hasPart#part, damage#hasNegativeEffectOn#activity, undergoer#hasDamage#damage, true#undergoer#isDamaged, side_1#inConflictWith#side_2, side_1#hasIssue#issue, side_1#atPlace#place, true#side_1#inMeeting, true#side_2#inConflict, true#side_1#inConflict, side_1#hasPurpose#purpose, true#translocation-theme#inMotion, translocation-theme#uses#place, false#place#inFunction, translocation-theme#atPlace#place, false#translocation-theme#inMotion, true#place#isBlocked, entity#atPlace#place, translocation-theme#notAtPlace#place, convict#committedOffense#offense, convict#hasConviction#conviction, true#convict#isConvicted, true#convict#inCaptivity, value-attribute#hasValue#value, true#entity#exist, false#place#isBlocked, true#place#inFunction, translocation-theme#notUses#place, agent#blocks#place, agent#hasPurpose#purpose, true#partners#inCollaboration, partners#hasProject#project, partner_1#collaboratesWith#partner_2, true#partners#inRelationship, partner_1#inRelationshipWith#partner_2, true#employee#isEmployed, employment-attribute#hasValue#employment-value, employee#hasAttribute#employment-attribute, employee#hasTask#employment-task, employee#hasFunction#employment-function, employee#employedAt#employer, suspect#suspectedOfOffense#offense, agent#examines#suspect, agent#examines#offense, true#leader-entity#isLeader, leader-entity#hasFunction#leader-function, leader-entity#isLeaderOf#leader-governed_entity, meeting-participant#atPlace#place, true#meeting-participant#inMeeting, suspect#notAtPlace#place, suspect#isChargedOf#offense, true#suspect#inCaptivity, suspect#atPlace#place]
     //32	921:47:buried	6:0:died	HCPE	56	112	[false#undergoer#exist, true#part#isDamaged, undergoer#hasPart#part, damage#hasNegativeEffectOn#activity, undergoer#hasDamage#damage, true#undergoer#isDamaged, side_1#inConflictWith#side_2, side_1#hasIssue#issue, side_1#atPlace#place, true#side_1#inMeeting, true#side_2#inConflict, true#side_1#inConflict, side_1#hasPurpose#purpose, true#translocation-theme#inMotion, translocation-theme#uses#place, false#place#inFunction, translocation-theme#atPlace#place, false#translocation-theme#inMotion, true#place#isBlocked, entity#atPlace#place, translocation-theme#notAtPlace#place, convict#committedOffense#offense, convict#hasConviction#conviction, true#convict#isConvicted, true#convict#inCaptivity, value-attribute#hasValue#value, true#entity#exist, false#place#isBlocked, true#place#inFunction, translocation-theme#notUses#place, agent#blocks#place, agent#hasPurpose#purpose, true#partners#inCollaboration, partners#hasProject#project, partner_1#collaboratesWith#partner_2, true#partners#inRelationship, partner_1#inRelationshipWith#partner_2, true#employee#isEmployed, employment-attribute#hasValue#employment-value, employee#hasAttribute#employment-attribute, employee#hasTask#employment-task, employee#hasFunction#employment-function, employee#employedAt#employer, suspect#suspectedOfOffense#offense, agent#examines#suspect, agent#examines#offense, true#leader-entity#isLeader, leader-entity#hasFunction#leader-function, leader-entity#isLeaderOf#leader-governed_entity, meeting-participant#atPlace#place, true#meeting-participant#inMeeting, suspect#notAtPlace#place, suspect#isChargedOf#offense, true#suspect#inCaptivity, suspect#atPlace#place]

    static String anyMentionCeoMatch (ArrayList<Mention> mentions, Integer threshold) {
        String result = "";
        HashMap<String, String> keyResults = new HashMap<String, String>();
        for (int i = 0; i < mentions.size(); i++) {
            Mention mention1 = mentions.get(i);
            if (ceoLexicon.containsKey(mention1.getWord().toLowerCase())) {
                ArrayList<String> mention1Classes = ceoLexicon.get(mention1.getWord().toLowerCase());
                for (int j = 0; j < mentions.size(); j++) {
                    Mention mention2 = mentions.get(j);
                    if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                            !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                        if (ceoLexicon.containsKey(mention2.getWord().toLowerCase())) {
                            ArrayList<String> mention2Classes = ceoLexicon.get(mention2.getWord().toLowerCase());
                            ArrayList<String> matches1 = ceoPathFinder.pathValuesForTypes(mention1Classes, mention2Classes);
                            ArrayList<String> matches2 = ceoPathFinder.pathValuesForTypes(mention2Classes, mention1Classes);
                            if (matches1.size()>=threshold || matches2.size()>=threshold) {
                                String str = "";
                                String key = "";
                                if (matches1.size() > matches2.size()) {
                                    key = mention1.toString()+mention2.toString();
                                    str = mention1.toString() + "\t" + mention2.toString() + "\tHCPE";
                                    if (DEBUG) str += "\t"+matches1.size()+"\t"+matches2.size()+"\t"+matches1.toString();
                                } else if (matches2.size() > matches1.size()) {
                                    key = mention2.toString()+mention1.toString();
                                    str = mention2.toString() + "\t" + mention1.toString() + "\tHCPE";
                                    if (DEBUG) str += "\t"+matches1.size()+"\t"+matches2.size()+"\t"+matches2.toString();
                                }
                                //// token fallback
                                else if (mention1.getToken()<mention2.getToken()){
                                    key = mention1.toString()+mention2.toString();
                                    str = mention1.toString() + "\t" + mention2.toString() + "\tHCPE";
                                    if (DEBUG) str += "\t"+matches1.size()+"\t"+matches2.size()+"\t"+matches1.toString();
                                }
                                else if (mention2.getToken()<mention1.getToken()) {
                                    key = mention2.toString()+mention1.toString();
                                    str = mention2.toString() + "\t" + mention1.toString() + "\tHCPE";
                                    if (DEBUG) str += "\t"+matches1.size()+"\t"+matches2.size()+"\t"+matches2.toString();
                                }
                                if (!str.isEmpty() && !key.isEmpty()) {
                                    keyResults.put(key, str);
                                }
                            }
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

        for (Map.Entry<String, String> entry : keyResults.entrySet()) {
            result +=  "1" + "\t"+entry.getValue()+"\n";
        }
        return result;
    }

    static String sameSentenceMentionCeoMatch (ArrayList<Mention> mentions, Integer threshold) {
        String result = "";
        HashMap<String, String> keyResults = new HashMap<String, String>();
        for (int i = 0; i < mentions.size(); i++) {
            Mention mention1 = mentions.get(i);
            if (ceoLexicon.containsKey(mention1.getWord().toLowerCase())) {
                ArrayList<String> mention1Classes = ceoLexicon.get(mention1.getWord().toLowerCase());
                for (int j = 0; j < mentions.size(); j++) {
                    Mention mention2 = mentions.get(j);
                    if (mention1.getSentence()==mention2.getSentence()) {
                        if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                                !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                            if (ceoLexicon.containsKey(mention2.getWord().toLowerCase())) {
                                ArrayList<String> mention2Classes = ceoLexicon.get(mention2.getWord().toLowerCase());
                                ArrayList<String> matches1 = ceoPathFinder.pathValuesForTypes(mention1Classes, mention2Classes);
                                ArrayList<String> matches2 = ceoPathFinder.pathValuesForTypes(mention2Classes, mention1Classes);
                                if (matches1.size()>=threshold || matches2.size()>=threshold) {
                                    String str = "";
                                    String key = "";
                                    if (matches1.size() > matches2.size()) {
                                        key = mention1.toString()+mention2.toString();
                                        str = mention1.toString() + "\t" + mention2.toString() + "\tHCPE";
                                        if (DEBUG) str += "\t"+matches1.size()+"\t"+matches2.size()+"\t"+matches1.toString();
                                    } else if (matches2.size() > matches1.size()) {
                                        key = mention2.toString()+mention1.toString();
                                        str = mention2.toString() + "\t" + mention1.toString() + "\tHCPE";
                                        if (DEBUG) str += "\t"+matches1.size()+"\t"+matches2.size()+"\t"+matches2.toString();
                                    }
                                    else if (mention1.getToken()<mention2.getToken()){
                                        key = mention1.toString()+mention2.toString();
                                        str = mention1.toString() + "\t" + mention2.toString() + "\tHCPE";
                                        if (DEBUG) str += "\t"+matches1.size()+"\t"+matches2.size()+"\t"+matches1.toString();
                                    }
                                    else if (mention2.getToken()<mention1.getToken()) {
                                        key = mention2.toString()+mention1.toString();
                                        str = mention2.toString() + "\t" + mention1.toString() + "\tHCPE";
                                        if (DEBUG) str += "\t"+matches1.size()+"\t"+matches2.size()+"\t"+matches2.toString();
                                    }
                                    if (!str.isEmpty() && !key.isEmpty()) {
                                        keyResults.put(key, str);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Map.Entry<String, String> entry : keyResults.entrySet()) {
            result +=  "1" + "\t"+entry.getValue()+"\n";
        }
        return result;
    }

    static String twoSentenceMentionCeoMatch (ArrayList<Mention> mentions, Integer threshold) {
        String result = "";
        HashMap<String, String> keyResults = new HashMap<String, String>();
        for (int i = 0; i < mentions.size(); i++) {
            Mention mention1 = mentions.get(i);
            if (ceoLexicon.containsKey(mention1.getWord().toLowerCase())) {
                ArrayList<String> mention1Classes = ceoLexicon.get(mention1.getWord().toLowerCase());
                for (int j = 0; j < mentions.size(); j++) {
                    Mention mention2 = mentions.get(j);
                    if (mention1.getSentence()==mention2.getSentence() ||
                            mention1.getSentence()==mention2.getSentence()-1 ||
                            mention1.getSentence()==mention2.getSentence()+1
                            ) {
                        if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                                !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                            if (ceoLexicon.containsKey(mention2.getWord().toLowerCase())) {
                                ArrayList<String> mention2Classes = ceoLexicon.get(mention2.getWord().toLowerCase());
                                ArrayList<String> matches1 = ceoPathFinder.pathValuesForTypes(mention1Classes, mention2Classes);
                                ArrayList<String> matches2 = ceoPathFinder.pathValuesForTypes(mention2Classes, mention1Classes);
                                if (matches1.size()>=threshold || matches2.size()>=threshold) {
                                    String str = "";
                                    String key = "";
                                    if (matches1.size() > matches2.size()) {
                                        key = mention1.toString()+mention2.toString();
                                        str = mention1.toString() + "\t" + mention2.toString() + "\tHCPE";
                                        if (DEBUG) str += "\t"+matches1.size()+"\t"+matches2.size()+"\t"+matches1.toString();
                                    } else if (matches2.size() > matches1.size()) {
                                        key = mention2.toString()+mention1.toString();
                                        str = mention2.toString() + "\t" + mention1.toString() + "\tHCPE";
                                        if (DEBUG) str += "\t"+matches1.size()+"\t"+matches2.size()+"\t"+matches2.toString();
                                    }
                                    else if (mention1.getToken()<mention2.getToken()){
                                        key = mention1.toString()+mention2.toString();
                                        str = mention1.toString() + "\t" + mention2.toString() + "\tHCPE";
                                        if (DEBUG) str += "\t"+matches1.size()+"\t"+matches2.size()+"\t"+matches1.toString();
                                    }
                                    else if (mention2.getToken()<mention1.getToken()) {
                                        key = mention2.toString()+mention1.toString();
                                        str = mention2.toString() + "\t" + mention1.toString() + "\tHCPE";
                                        if (DEBUG) str += "\t"+matches1.size()+"\t"+matches2.size()+"\t"+matches2.toString();
                                    }
                                    if (!str.isEmpty() && !key.isEmpty()) {
                                        keyResults.put(key, str);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Map.Entry<String, String> entry : keyResults.entrySet()) {
            result +=  "1" + "\t"+entry.getValue()+"\n";
        }
        return result;
    }

    static String fourSentenceMentionCeoMatch (ArrayList<Mention> mentions, Integer threshold) {
         String result = "";
         HashMap<String, String> keyResults = new HashMap<String, String>();
         for (int i = 0; i < mentions.size(); i++) {
             Mention mention1 = mentions.get(i);
             if (ceoLexicon.containsKey(mention1.getWord().toLowerCase())) {
                 ArrayList<String> mention1Classes = ceoLexicon.get(mention1.getWord().toLowerCase());
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
                             if (ceoLexicon.containsKey(mention2.getWord().toLowerCase())) {
                                 ArrayList<String> mention2Classes = ceoLexicon.get(mention2.getWord().toLowerCase());
                                 ArrayList<String> matches1 = ceoPathFinder.pathValuesForTypes(mention1Classes, mention2Classes);
                                 ArrayList<String> matches2 = ceoPathFinder.pathValuesForTypes(mention2Classes, mention1Classes);
                                 if (matches1.size()>=threshold || matches2.size()>=threshold) {
                                     String str = "";
                                     String key = "";
                                     if (matches1.size() > matches2.size()) {
                                         key = mention1.toString()+mention2.toString();
                                         str = mention1.toString() + "\t" + mention2.toString() + "\tHCPE";
                                             str += "\t"+matches1.size()+"\t"+matches2.size()+"\t"+matches1.toString();
                                     } else if (matches2.size() > matches1.size()) {
                                         key = mention2.toString()+mention1.toString();
                                         str = mention2.toString() + "\t" + mention1.toString() + "\tHCPE";
                                              str += "\t"+matches1.size()+"\t"+matches2.size()+"\t"+matches2.toString();
                                     }
                                     else if (mention1.getToken()<mention2.getToken()){
                                         key = mention1.toString()+mention2.toString();
                                         str = mention1.toString() + "\t" + mention2.toString() + "\tHCPE";
                                             str += "\t"+matches1.size()+"\t"+matches2.size()+"\t"+matches1.toString();
                                     }
                                     else if (mention2.getToken()<mention1.getToken()) {
                                         key = mention2.toString()+mention1.toString();
                                         str = mention2.toString() + "\t" + mention1.toString() + "\tHCPE";
                                              str += "\t"+matches1.size()+"\t"+matches2.size()+"\t"+matches2.toString();
                                     }
                                     if (!str.isEmpty() && !key.isEmpty()) {
                                         keyResults.put(key, str);
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
         for (Map.Entry<String, String> entry : keyResults.entrySet()) {
             result +=  "1" + "\t"+entry.getValue()+"\n";
         }
         return result;
     }


    static String sameSentenceMentionBaselineMatch (ArrayList<Mention> mentions) {
        String result = "";
        ArrayList<String> results = new ArrayList<String>();
        for (int i = 0; i < mentions.size(); i++) {
            Mention mention1 = mentions.get(i);
            for (int j = 0; j < mentions.size(); j++) {
                Mention mention2 = mentions.get(j);
                if (mention1.getSentence()==mention2.getSentence()
                        ) {
                    if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                            !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                        String str = "";
                        if (mention1.getToken()<mention2.getToken()){
                            str = mention1.toString() + "\t" + mention2.toString() + "\tHCPE";
                        }
                        else if (mention2.getToken()<mention1.getToken()) {
                            str = mention2.toString() + "\t" + mention1.toString() + "\tHCPE";
                        }
                        if (!str.isEmpty()) {
                            if (!results.contains(str)) results.add(str);
                        }
                    }
                }
            }
        }
        for (int i = 0; i < results.size(); i++) {
            String r = results.get(i);
            result += i + "\t"+r+"\n";
        }
        return result;
    }

    static String twoSentenceMentionBaselineMatch (ArrayList<Mention> mentions) {
        String result = "";
        ArrayList<String> results = new ArrayList<String>();
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
                        String str = "";
                        if (mention1.getToken()<mention2.getToken()){
                            str = mention1.toString() + "\t" + mention2.toString() + "\tHCPE";
                        }
                        else if (mention2.getToken()<mention1.getToken()) {
                            str = mention2.toString() + "\t" + mention1.toString() + "\tHCPE";
                        }
                        if (!str.isEmpty()) {
                            if (!results.contains(str)) results.add(str);
                        }
                    }
                }
            }
        }
        for (int i = 0; i < results.size(); i++) {
            String r = results.get(i);
            result += i + "\t"+r+"\n";
        }
        return result;
    }

    static String fourSentenceMentionBaselineMatch (ArrayList<Mention> mentions) {
        String result = "";
        ArrayList<String> results = new ArrayList<String>();
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
                        String str = "";
                        if (mention1.getToken()<mention2.getToken()){
                            str = mention1.toString() + "\t" + mention2.toString() + "\tHCPE";
                        }
                        else if (mention2.getToken()<mention1.getToken()) {
                            str = mention2.toString() + "\t" + mention1.toString() + "\tHCPE";
                        }
                        if (!str.isEmpty()) {
                            if (!results.contains(str)) results.add(str);
                        }
                    }
                }
            }
        }
        for (int i = 0; i < results.size(); i++) {
            String r = results.get(i);
            result += i + "\t"+r+"\n";
        }
        return result;
    }

    static String allMentionBaselineMatch (ArrayList<Mention> mentions) {
        String result = "";
        ArrayList<String> results = new ArrayList<String>();
        for (int i = 0; i < mentions.size(); i++) {
            Mention mention1 = mentions.get(i);
            for (int j = 0; j < mentions.size(); j++) {
                Mention mention2 = mentions.get(j);
                if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                        !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                    String str = "";
                    if (mention1.getToken()<mention2.getToken()){
                        str = mention1.toString() + "\t" + mention2.toString() + "\tHCPE";
                    }
                    else if (mention2.getToken()<mention1.getToken()) {
                        str = mention2.toString() + "\t" + mention1.toString() + "\tHCPE";
                    }
                    if (!str.isEmpty()) {
                        if (!results.contains(str)) results.add(str);
                    }
                }
            }
        }
        for (int i = 0; i < results.size(); i++) {
            String r = results.get(i);
            result += i + "\t"+r+"\n";
        }
        return result;
    }



    static HashMap<String, ArrayList<String>> readLexiconFile (File file) {
        HashMap<String, ArrayList<String>> lexicon = new HashMap<String, ArrayList<String>> ();
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                while (in.ready() && (inputLine = in.readLine()) != null) {
                    if (inputLine.trim().length() > 0) {
                        String[] fields = inputLine.split("\t");
                        ArrayList<String> types = new ArrayList<String>();
                        String word = "";
                        for (int i = 0; i < fields.length; i++) {
                            String field = fields[i];
                            if (i == 0) word = field.trim().toLowerCase();
                            else {
                                types.add(field.trim());
                            }
                        }
                        if (!types.isEmpty()) {
                            lexicon.put(word, types);
                        }
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Cannot find file = " + file.getAbsolutePath());
        }
        System.out.println("lexicon.size() = " + lexicon.size());
        return lexicon;
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


    static public ArrayList<File> makeRecursiveFileList(File inputFile, String theFilter) {
        ArrayList<File> acceptedFileList = new ArrayList<File>();
        File[] theFileList = null;
        if ((inputFile.canRead())) {
            theFileList = inputFile.listFiles();
            for (int i = 0; i < theFileList.length; i++) {
                File newFile = theFileList[i];
                if (newFile.isDirectory()) {
                    ArrayList<File> nextFileList = makeRecursiveFileList(newFile, theFilter);
                    acceptedFileList.addAll(nextFileList);
                } else {
                    if (newFile.getName().endsWith(theFilter)) {
                        acceptedFileList.add(newFile);
                    }
                }
               // break;
            }
        } else {
            System.out.println("Cannot access file:" + inputFile + "#");
            if (!inputFile.exists()) {
                System.out.println("File/folder does not exist!");
            }
        }
        return acceptedFileList;
    }
}
