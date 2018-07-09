import java.util.ArrayList;
import java.util.HashMap;

public class NafClinkMatching {

    static HashMap<String, ArrayList<Clink>> clinks = new HashMap<String, ArrayList<Clink>>();

    static String anyMentionNafClinkMatch (ArrayList<Mention> mentions, String fileName) {
           String result = "";
           HashMap<String, ArrayList<String>> keyResults = new HashMap<String, ArrayList<String>>();
           String nameIndex = fileName;
           int idx = fileName.indexOf(".");
           if (idx>-1) nameIndex = fileName.substring(0, idx);
           if (clinks.containsKey(nameIndex)) {
               ArrayList<Clink> fileClinks = clinks.get(nameIndex);
               for (int i = 0; i < mentions.size(); i++) {
                   Mention mention1 = mentions.get(i);
                   for (int j = 0; j < mentions.size(); j++) {
                       Mention mention2 = mentions.get(j);
                       if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                               !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                           System.out.println("mention1 = " + mention1.toString());
                           System.out.println("mention2 = " + mention2.toString());
                           for (int k = 0; k < fileClinks.size(); k++) {
                               Clink clink = fileClinks.get(k);
                               if ((clink.causeFrom.getWf().equals(mention1.getWord()) || clink.causeFrom.getWf().equals(mention2.getWord())) &&
                                   (clink.causeTo.getWf().equals(mention1.getWord()) || clink.causeTo.getWf().equals(mention2.getWord()))
                               ){

                                   System.out.println("clink.causeFrom.toSimpleString() = " + clink.causeFrom.toSimpleString());
                                   System.out.println("clink.causeTo.toSimpleString() = " + clink.causeTo.toSimpleString());
                                   addNafClinkResult(keyResults, mention1, mention2);
                                   break;
                                }
                            }
                        }
                    }
               }
           }
           result = MentionReader.makeResult (keyResults);
           
           return result;
       }

       static String sameSentenceMentionNarrativeChainMatch (ArrayList<Mention> mentions, String fileName) {
           String result = "";
           HashMap<String, ArrayList<String>> keyResults = new HashMap<String, ArrayList<String>>();
           String nameIndex = fileName;
           int idx = fileName.indexOf(".");
           if (idx>-1) nameIndex = fileName.substring(0, idx);
           if (clinks.containsKey(nameIndex)) {
               ArrayList<Clink> fileClinks = clinks.get(nameIndex);
               for (int i = 0; i < mentions.size(); i++) {
                   Mention mention1 = mentions.get(i);
                       for (int j = 0; j < mentions.size(); j++) {
                           Mention mention2 = mentions.get(j);
                           if (mention1.getSentence()==mention2.getSentence()) {
                               if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                                       !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                                   for (int k = 0; k < fileClinks.size(); k++) {
                                       Clink clink = fileClinks.get(k);
                                       if (clink.causeFrom.getWf().equals(mention1.getWord()) &&
                                               clink.causeTo.getWf().equals(mention2.getWord())) {
                                           addNafClinkResult(keyResults, mention1, mention2);
                                           break;
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

       static String twoSentenceMentionNarrativeChainMatch (ArrayList<Mention> mentions, String fileName) {
           String result = "";
           HashMap<String, ArrayList<String>> keyResults = new HashMap<String, ArrayList<String>>();
           String nameIndex = fileName;
           int idx = fileName.indexOf(".");
           if (idx>-1) nameIndex = fileName.substring(0, idx);
           if (clinks.containsKey(nameIndex)) {
               ArrayList<Clink> fileClinks = clinks.get(nameIndex);
               for (int i = 0; i < mentions.size(); i++) {
                   Mention mention1 = mentions.get(i);
                       for (int j = 0; j < mentions.size(); j++) {
                           Mention mention2 = mentions.get(j);
                           if (mention1.getSentence()==mention2.getSentence() ||
                               mention1.getSentence()==mention2.getSentence()-1 ||
                               mention1.getSentence()==mention2.getSentence()+1
                               ) {                               if (!mention1.getTokenString().equals(mention2.getTokenString()) &&
                                       !mention1.getWord().equalsIgnoreCase(mention2.getWord())) {
                                   for (int k = 0; k < fileClinks.size(); k++) {
                                       Clink clink = fileClinks.get(k);
                                       if (clink.causeFrom.getWf().equals(mention1.getWord()) &&
                                               clink.causeTo.getWf().equals(mention2.getWord())) {
                                           addNafClinkResult(keyResults, mention1, mention2);
                                           break;
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

       static String fourSentenceMentionNarrativeChainMatch (ArrayList<Mention> mentions, String fileName) {
           String result = "";
           HashMap<String, ArrayList<String>> keyResults = new HashMap<String, ArrayList<String>>();
           String nameIndex = fileName;
           int idx = fileName.indexOf(".");
           if (idx>-1) nameIndex = fileName.substring(0, idx);
           if (clinks.containsKey(nameIndex)) {
               ArrayList<Clink> fileClinks = clinks.get(nameIndex);
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
                                   for (int k = 0; k < fileClinks.size(); k++) {
                                       Clink clink = fileClinks.get(k);
                                       if (clink.causeFrom.getWf().equals(mention1.getWord()) &&
                                               clink.causeTo.getWf().equals(mention2.getWord())) {
                                           addNafClinkResult(keyResults, mention1, mention2);
                                           break;
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


    static void addNafClinkResult(HashMap<String, ArrayList<String>> keyResults,
                                           Mention mention1,
                                           Mention mention2) {
           String str = "";
           String key = "";
          // System.out.println("DEBUG = " + DEBUG);
           //// token fallback
           if (mention1.getToken() < mention2.getToken()) {
               key = mention1.toString() + mention2.toString();
               str = mention1.toString() + "\t" + mention2.toString() + "\tHCPE";
           }
           else if (mention2.getToken() < mention1.getToken()) {
               key = mention2.toString() + mention1.toString();
               str = mention2.toString() + "\t" + mention1.toString() + "\tHCPE";
           }
           MentionReader.addResult(keyResults, mention1, mention2, str, key);
       }
}
