import java.util.ArrayList;
import java.util.HashMap;

public class BaseLineMatching {

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
         MentionReader.addResult(keyResults, mention1, mention2, str, key);
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
            result = MentionReader.makeResult (keyResults);
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
            result = MentionReader.makeResult (keyResults);
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
            result = MentionReader.makeResult (keyResults);
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
            result = MentionReader.makeResult (keyResults);
            return result;
        }

}
