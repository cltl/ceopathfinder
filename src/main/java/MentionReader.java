import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class MentionReader {
    static HashMap<String, ArrayList<String>> ceoLexicon = new HashMap<String, ArrayList<String>>();
    static CeoPathFinder ceoPathFinder = new CeoPathFinder();
    static Integer threshold = 1;
    static boolean deep = false;
    ///Users/piek/Desktop/Roxane/Tommaso-v3/not-connected-events/3_1ecb.xml_not-connected-events.eval
    static public void main (String[] args) {
         String lexiconPath = "/Users/piek/Desktop/Roxane/CEO-lexicon/ceo-lexicon-ecb-v1.txt";
         ceoLexicon = readLexiconFile(new File(lexiconPath));
         String pathToCeo = "/Users/piek/Desktop/Roxane/CEO.v1.0/CEO_version_1.owl";
         ceoPathFinder.readOwlFile(pathToCeo);
         if (deep) {
            ceoPathFinder.interpretOntologyWithInheritance("Physical");
         }
         else {
            ceoPathFinder.interpretOntology("Physical");
         }
         ceoPathFinder.setDuring(true);
         String mentionFolder = "/Users/piek/Desktop/Roxane/Tommaso-v4/all_event_mentions";
         ArrayList<File> files = makeRecursiveFileList(new File(mentionFolder), ".eval");
         for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
             System.out.println("file.getName() = " + file.getName());
            ArrayList<Mention> mentions = readFileToMentionList(file);
            String ceoResult = baseline(mentions);
             try {
                 OutputStream fos = new FileOutputStream(file.getAbsoluteFile()+".bl");
                 fos.write(ceoResult.getBytes());
                 fos.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }

             ceoResult = anyMentionCeoMatch(mentions, threshold);
             try {
                  OutputStream fos = new FileOutputStream(file.getAbsoluteFile()+".ceoANY");
                  fos.write(ceoResult.getBytes());
                  fos.close();
             } catch (IOException e) {
                  e.printStackTrace();
             }

             ceoResult = twoSentenceMentionCeoMatch(mentions, threshold);
             try {
                  OutputStream fos = new FileOutputStream(file.getAbsoluteFile()+".ceo2S");
                  fos.write(ceoResult.getBytes());
                  fos.close();
             } catch (IOException e) {
                  e.printStackTrace();
             }
             //break;
         }
    }
    /*
    3	331:13:slaying	325:13:trial	HCPE
    9	820:34:things	828:34:pain	HCPE
    9	18:0:killed	828:34:pain	HCPE
     */
    static String baseline (ArrayList<Mention> mentions) {
        String str = "";
        HashMap<Integer, ArrayList<Mention>> sentenceToMentions = getSentenceMap(mentions);
       // System.out.println("sentenceToMentions.size() = " + sentenceToMentions.size());
        Set keySet = sentenceToMentions.keySet();
        Iterator<Integer> keys = keySet.iterator();
        while (keys.hasNext()) {
            Integer sentenceCurrent = keys.next();
           // System.out.println("sentenceCurrent = " + sentenceCurrent);
            Integer sentenceNext = sentenceCurrent+1;
           // System.out.println("sentenceNext = " + sentenceNext);
            if (sentenceToMentions.containsKey(sentenceCurrent)) {
                ArrayList<Mention> sentenceMentions = sentenceToMentions.get(sentenceCurrent);
                ArrayList<Mention> nextSentenceMentions = new ArrayList<Mention>();
                if (sentenceToMentions.containsKey(sentenceNext)) {
                    nextSentenceMentions = sentenceToMentions.get(sentenceNext);
                }
                for (int i = 0; i < sentenceMentions.size(); i++) {
                    Mention mention = sentenceMentions.get(i);
                    for (int j = 0; j < sentenceMentions.size(); j++) {
                        Mention sentenceMention = sentenceMentions.get(j);
                        if (mention.getToken() < sentenceMention.getToken()) {
                            str += i + "\t"+mention.toString() + "\t" + sentenceMention.toString() + "\tHCPE\n";
                        }
                   /*else if (mention.getToken()>sentenceMention.getToken()) {
                       str += mention.toString()+"\t"+sentenceMention.toString()+"\tHCPE\n";
                   } */
                    }
                    for (int j = 0; j < nextSentenceMentions.size(); j++) {
                        Mention sentenceMention = nextSentenceMentions.get(j);
                        str += i + "\t" + mention.toString() + "\t" + sentenceMention.toString() + "\tHCPE\n";
                    }
                }
            }
        }
        return str;
    }

    static String anyMentionCeoMatch (ArrayList<Mention> mentions, Integer threshold) {
        String result = "";
        ArrayList<String> results = new ArrayList<String>();
        for (int i = 0; i < mentions.size(); i++) {
            Mention mention1 = mentions.get(i);
            if (ceoLexicon.containsKey(mention1.getWord())) {
                ArrayList<String> mention1Classes = ceoLexicon.get(mention1.getWord());
                for (int j = 0; j < mentions.size(); j++) {
                    Mention mention2 = mentions.get(j);
                    if (!mention1.getTokenString().equals(mention2.getTokenString())) {
                        if (ceoLexicon.containsKey(mention2.getWord())) {
                            ArrayList<String> mention2Classes = ceoLexicon.get(mention2.getWord());
                            Integer match1 = ceoPathFinder.pathForTypes(mention1Classes, mention2Classes);
                            Integer match2 = ceoPathFinder.pathForTypes(mention2Classes, mention1Classes);
                            if (match1>=threshold && match1>=match2) {
                                String str =  mention1.toString() + "\t" + mention2.toString() + "\tHCPE";
                               // str += "\t"+match1+"\t"+match2;
                                if (!result.contains(str)) results.add(str);
                            }
                            else if (match2>=threshold) {
                                String str = mention2.toString() + "\t" + mention1.toString() + "\tHCPE";
                              //  str += "\t"+match1+"\t"+match2;
                                if (!result.contains(str)) results.add(str);
                            }
                        }
                        else {
                          //  System.out.println("OoV mention2.getWord() = " + mention2.getWord());

                        }
                    }
                }
            }
            else {
              //  System.out.println("OoV mention1.getWord() = " + mention1.getWord());
            }
        }
        for (int i = 0; i < results.size(); i++) {
            String r = results.get(i);
            result +=  i + "\t"+r+"\n";
        }
        return result;
    }

    static String twoSentenceMentionCeoMatch (ArrayList<Mention> mentions, Integer threshold) {
        String result = "";
        ArrayList<String> results = new ArrayList<String>();
        for (int i = 0; i < mentions.size(); i++) {
            Mention mention1 = mentions.get(i);
            if (ceoLexicon.containsKey(mention1.getWord())) {
                ArrayList<String> mention1Classes = ceoLexicon.get(mention1.getWord());
                for (int j = 0; j < mentions.size(); j++) {
                    Mention mention2 = mentions.get(j);
                    if (mention1.getSentence()==mention2.getSentence() ||
                            mention1.getSentence()==mention2.getSentence()-1 ||
                            mention1.getSentence()==mention2.getSentence()+1
                            ) {
                        if (!mention1.getTokenString().equals(mention2.getTokenString())) {
                            if (ceoLexicon.containsKey(mention2.getWord())) {
                                ArrayList<String> mention2Classes = ceoLexicon.get(mention2.getWord());
                                Integer match1 = ceoPathFinder.pathForTypes(mention1Classes, mention2Classes);
                                Integer match2 = ceoPathFinder.pathForTypes(mention2Classes, mention1Classes);
                                if (match1 >= threshold && match1 >= match2) {
                                    String str =  mention1.toString() + "\t" + mention2.toString() + "\tHCPE";
                                   // str += "\t"+match1+"\t"+match2;
                                    if (!result.contains(str)) results.add(str);
                                } else if (match2 >= threshold) {
                                    String str = mention2.toString() + "\t" + mention1.toString() + "\tHCPE";
                                   // str += "\t"+match1+"\t"+match2;
                                    if (!result.contains(str)) results.add(str);
                                }
                            }
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

   /* static String twoSentenceCeo (ArrayList<Mention> mentions) {
        String str = "";

        HashMap<Integer, ArrayList<Mention>> sentenceToMentions = getSentenceMap(mentions);
        Set keySet = sentenceToMentions.keySet();
        Iterator<Integer> keys = keySet.iterator();
        while (keys.hasNext()) {
            Integer sentenceCurrent = keys.next();
            Integer sentenceNext = sentenceCurrent+1;
            if (sentenceToMentions.containsKey(sentenceCurrent)) {
                ArrayList<Mention> sentenceMentions = sentenceToMentions.get(sentenceCurrent);
                ArrayList<Mention> nextSentenceMentions = new ArrayList<Mention>();
                if (sentenceToMentions.containsKey(sentenceNext)) {
                    nextSentenceMentions = sentenceToMentions.get(sentenceNext);
                }
                for (int i = 0; i < sentenceMentions.size(); i++) {
                    Mention mention = sentenceMentions.get(i);
                    for (int j = 0; j < sentenceMentions.size(); j++) {
                        Mention sentenceMention = sentenceMentions.get(j);
                        if (mention.getToken() < sentenceMention.getToken()) {
                            str += i + "\t"+mention.toString() + "\t" + sentenceMention.toString() + "\tHCPE\n";
                        }
                   *//*else if (mention.getToken()>sentenceMention.getToken()) {
                       str += mention.toString()+"\t"+sentenceMention.toString()+"\tHCPE\n";
                   } *//*
                    }
                    for (int j = 0; j < nextSentenceMentions.size(); j++) {
                        Mention sentenceMention = nextSentenceMentions.get(j);
                        str += i + "\t" + mention.toString() + "\t" + sentenceMention.toString() + "\tHCPE\n";
                    }
                }
            }
        }
        return str;
    }
*/

    static HashMap<Integer, ArrayList<Mention>> getSentenceMap (ArrayList<Mention> mentions) {
        HashMap<Integer, ArrayList<Mention>> sentenceToMentions = new HashMap<Integer, ArrayList<Mention>>();
        for (int i = 0; i < mentions.size(); i++) {
            Mention mention = mentions.get(i);
           // System.out.println("mention.toString() = " + mention.toString());
            if (sentenceToMentions.containsKey(mention.getSentence())) {
                ArrayList<Mention> sentenceMentions = sentenceToMentions.get(mention.getSentence());
                sentenceMentions.add(mention);
                sentenceToMentions.put(mention.getSentence(), sentenceMentions);
            }
            else {
                ArrayList<Mention> sentenceMentions = new ArrayList<Mention>();
                sentenceMentions.add(mention);
                sentenceToMentions.put(mention.getSentence(), sentenceMentions);
            }
        }
        return sentenceToMentions;
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
                            if (i == 0) word = field.trim();
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
