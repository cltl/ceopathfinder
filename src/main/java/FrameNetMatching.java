import java.util.ArrayList;
import java.util.HashMap;

public class FrameNetMatching {

    static FrameNetReader frameNetReader = new FrameNetReader();
    static FrameNetLuReader frameNetLuReader = new FrameNetLuReader();


    static String causalMatch (Mention mention1, Mention mention2) {
        ArrayList<String> f1ArrayList = frameNetLuReader.getFramesForWord(mention1.getWord());
        ArrayList<String> f2ArrayList = frameNetLuReader.getFramesForWord(mention2.getWord());
        if (f1ArrayList.size()>0 && f2ArrayList.size()>0) {
           // System.out.println("f1ArrayList = " + f1ArrayList.toString());
           // System.out.println("f2ArrayList = " + f2ArrayList.toString());
            for (int k = 0; k < f1ArrayList.size(); k++) {
                String f1 = f1ArrayList.get(k);
                for (int l = 0; l < f2ArrayList.size(); l++) {
                    String f2 = f2ArrayList.get(l);
                   // System.out.println(f1+":"+f2);
                    for (int m = 0; m < frameNetReader.clinks.size(); m++) {
                        Clink clink = frameNetReader.clinks.get(m);
                        if (clink.causeFrom.getWf().equals(f1) && clink.causeTo.getWf().equals(f2)) {
                          //  System.out.println("clink = " + clink.causeFrom.toSimpleString() + ":" + clink.causeTo.toSimpleString());
                            String match = f1+":"+f2;
                            return match;
                        }
                    }
                }
            }
            return "";
        }
        else {
            if (f1ArrayList.size()==0 ) {
               // System.out.println("No FN entry for mention1 = " + mention1.toString());
            }
            if (f2ArrayList.size()==0) {
               // System.out.println("No FN entry for mention2 = " + mention2.toString());
            }
            return "";
        }
    }

    static String anyMentionFrameNetMatch (ArrayList<Mention> mentions) {
        String result = "";
        HashMap<String, ArrayList<String>> keyResults = new HashMap<String, ArrayList<String>>();

        for (int i = 0; i < mentions.size(); i++) {
            Mention mention1 = mentions.get(i);
            for (int j = 0; j < mentions.size(); j++) {
                Mention mention2 = mentions.get(j);
                if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                        !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                     String match = causalMatch(mention1, mention2);
                     if (!match.isEmpty()) {
                         addFrameNetResult(keyResults, mention1, mention2, match);
                     }
                     else {
                         match = causalMatch(mention2, mention1);
                          if (!match.isEmpty()) {
                              addFrameNetResult(keyResults, mention2, mention1, match);
                          }
                     }
                 }
             }
        }
        result = MentionReader.makeResult (keyResults);

        return result;
    }

        static String sameSentenceMentionFrameNetMatch (ArrayList<Mention> mentions) {
            String result = "";
            HashMap<String, ArrayList<String>> keyResults = new HashMap<String, ArrayList<String>>();

            for (int i = 0; i < mentions.size(); i++) {
                Mention mention1 = mentions.get(i);
                for (int j = 0; j < mentions.size(); j++) {
                    Mention mention2 = mentions.get(j);
                    if (mention1.getSentence()==mention2.getSentence()) {
                        if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                                !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                            String match = causalMatch(mention1, mention2);
                            if (!match.isEmpty()) {
                                addFrameNetResult(keyResults, mention1, mention2, match);
                            } else {
                                match = causalMatch(mention2, mention1);
                                if (!match.isEmpty()) {
                                    addFrameNetResult(keyResults, mention2, mention1, match);
                                }
                            }
                        }
                    }
                 }
            }

            result = MentionReader.makeResult (keyResults);

            return result;
        }

        static String twoSentenceMentionFrameNetMatch (ArrayList<Mention> mentions) {
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
                            String match = causalMatch(mention1, mention2);
                            if (!match.isEmpty()) {
                                addFrameNetResult(keyResults, mention1, mention2, match);
                            } else {
                                match = causalMatch(mention2, mention1);
                                if (!match.isEmpty()) {
                                    addFrameNetResult(keyResults, mention2, mention1, match);
                                }
                            }
                        }
                    }
                 }
            }

            result = MentionReader.makeResult (keyResults);

            return result;
        }

        static String fourSentenceMentionFrameNetMatch (ArrayList<Mention> mentions) {
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
                         mention1.getSentence()==mention2.getSentence()+2 ||
                         mention1.getSentence()==mention2.getSentence()-3 ||
                         mention1.getSentence()==mention2.getSentence()+3

                         ) {
                        if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                                !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                            String match = causalMatch(mention1, mention2);
                            if (!match.isEmpty()) {
                                addFrameNetResult(keyResults, mention1, mention2, match);
                            } else {
                                match = causalMatch(mention2, mention1);
                                if (!match.isEmpty()) {
                                    addFrameNetResult(keyResults, mention2, mention1, match);
                                }
                            }
                        }
                    }
                 }
            }

            result = MentionReader.makeResult (keyResults);

            return result;
        }


     static void addFrameNetResult(HashMap<String, ArrayList<String>> keyResults,
                                            Mention mention1,
                                            Mention mention2, String match) {
            String str = "";
            String key = "";
           // System.out.println("DEBUG = " + DEBUG);
            //// token fallback
            if (mention1.getToken() < mention2.getToken()) {
                key = mention1.toString() + mention2.toString();
                str = mention1.toString() + "\t" + mention2.toString() + "\tHCPE";
                str += "\t" + match;
            }
            else if (mention2.getToken() < mention1.getToken()) {
                key = mention2.toString() + mention1.toString();
                str = mention2.toString() + "\t" + mention1.toString() + "\tHCPE";
                str += "\t" + match;
            }
            MentionReader.addResult(keyResults, mention1, mention2, str, key);
        }
}
