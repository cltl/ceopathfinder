import java.util.ArrayList;
import java.util.HashMap;

public class CeoMatching {

    static CeoPathFinder ceoPathFinder = new CeoPathFinder();

    static String anyMentionCeoMatch (ArrayList<Mention> mentions) {
        String result = "";
        HashMap<String, ArrayList<String>> keyResults = new HashMap<String, ArrayList<String>>();
        HashMap<String, Integer> OOV = new HashMap<String, Integer>();
        for (int i = 0; i < mentions.size(); i++) {
            Mention mention1 = mentions.get(i);
            if (ceoPathFinder.ceoLexicon.containsKey(mention1.getWord().toLowerCase())) {
                for (int j = 0; j < mentions.size(); j++) {
                    Mention mention2 = mentions.get(j);
                    if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                            !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                        if (ceoPathFinder.areCircumstantial(mention1.getWord(), mention2.getWord())) {
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

/*        for (Map.Entry<String, Integer> entry : OOV.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
         }*/

        result = MentionReader.makeResult (keyResults);

        return result;
    }

    static String sameSentenceMentionCeoMatch (ArrayList<Mention> mentions) {
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
                            if (ceoPathFinder.areCircumstantial(mention1.getWord(), mention2.getWord())) {
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
        result = MentionReader.makeResult (keyResults);
        return result;
    }

    static String twoSentenceMentionCeoMatch (ArrayList<Mention> mentions) {
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
                            if (ceoPathFinder.areCircumstantial(mention1.getWord(), mention2.getWord())) {
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
        result = MentionReader.makeResult (keyResults);
        return result;
    }

    static String fourSentenceMentionCeoMatch (ArrayList<Mention> mentions) {
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
                             if (ceoPathFinder.areCircumstantial(mention1.getWord(), mention2.getWord())) {
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
         result = MentionReader.makeResult (keyResults);
         return result;
     }

    //22	6:0:died	921:47:buried	HCPE	112	56	[false#undergoer#exist, true#part#isDamaged, undergoer#hasPart#part, damage#hasNegativeEffectOn#activity, undergoer#hasDamage#damage, true#undergoer#isDamaged, side_1#inConflictWith#side_2, side_1#hasIssue#issue, side_1#atPlace#place, true#side_1#inMeeting, true#side_2#inConflict, true#side_1#inConflict, side_1#hasPurpose#purpose, true#translocation-theme#inMotion, translocation-theme#uses#place, false#place#inFunction, translocation-theme#atPlace#place, false#translocation-theme#inMotion, true#place#isBlocked, entity#atPlace#place, translocation-theme#notAtPlace#place, convict#committedOffense#offense, convict#hasConviction#conviction, true#convict#isConvicted, true#convict#inCaptivity, value-attribute#hasValue#value, true#entity#exist, false#place#isBlocked, true#place#inFunction, translocation-theme#notUses#place, agent#blocks#place, agent#hasPurpose#purpose, true#partners#inCollaboration, partners#hasProject#project, partner_1#collaboratesWith#partner_2, true#partners#inRelationship, partner_1#inRelationshipWith#partner_2, true#employee#isEmployed, employment-attribute#hasValue#employment-value, employee#hasAttribute#employment-attribute, employee#hasTask#employment-task, employee#hasFunction#employment-function, employee#employedAt#employer, suspect#suspectedOfOffense#offense, agent#examines#suspect, agent#examines#offense, true#leader-entity#isLeader, leader-entity#hasFunction#leader-function, leader-entity#isLeaderOf#leader-governed_entity, meeting-participant#atPlace#place, true#meeting-participant#inMeeting, suspect#notAtPlace#place, suspect#isChargedOf#offense, true#suspect#inCaptivity, suspect#atPlace#place]
     //32	921:47:buried	6:0:died	HCPE	56	112	[false#undergoer#exist, true#part#isDamaged, undergoer#hasPart#part, damage#hasNegativeEffectOn#activity, undergoer#hasDamage#damage, true#undergoer#isDamaged, side_1#inConflictWith#side_2, side_1#hasIssue#issue, side_1#atPlace#place, true#side_1#inMeeting, true#side_2#inConflict, true#side_1#inConflict, side_1#hasPurpose#purpose, true#translocation-theme#inMotion, translocation-theme#uses#place, false#place#inFunction, translocation-theme#atPlace#place, false#translocation-theme#inMotion, true#place#isBlocked, entity#atPlace#place, translocation-theme#notAtPlace#place, convict#committedOffense#offense, convict#hasConviction#conviction, true#convict#isConvicted, true#convict#inCaptivity, value-attribute#hasValue#value, true#entity#exist, false#place#isBlocked, true#place#inFunction, translocation-theme#notUses#place, agent#blocks#place, agent#hasPurpose#purpose, true#partners#inCollaboration, partners#hasProject#project, partner_1#collaboratesWith#partner_2, true#partners#inRelationship, partner_1#inRelationshipWith#partner_2, true#employee#isEmployed, employment-attribute#hasValue#employment-value, employee#hasAttribute#employment-attribute, employee#hasTask#employment-task, employee#hasFunction#employment-function, employee#employedAt#employer, suspect#suspectedOfOffense#offense, agent#examines#suspect, agent#examines#offense, true#leader-entity#isLeader, leader-entity#hasFunction#leader-function, leader-entity#isLeaderOf#leader-governed_entity, meeting-participant#atPlace#place, true#meeting-participant#inMeeting, suspect#notAtPlace#place, suspect#isChargedOf#offense, true#suspect#inCaptivity, suspect#atPlace#place]

    static void addCeoResult(HashMap<String, ArrayList<String>> keyResults, Mention mention1, Mention mention2) {
        ArrayList<String> mention1Classes = ceoPathFinder.ceoLexicon.get(mention1.getWord().toLowerCase());
        ArrayList<String> mention2Classes = ceoPathFinder.ceoLexicon.get(mention2.getWord().toLowerCase());
        ArrayList<String> matches1 = ceoPathFinder.pathValuesForTypes(mention1Classes, mention2Classes);
        ArrayList<String> matches2 = ceoPathFinder.pathValuesForTypes(mention2Classes, mention1Classes);
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
        MentionReader.addResult(keyResults, mention1, mention2, str, key);
    }
}
