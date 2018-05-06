import java.io.*;
import java.util.ArrayList;

public class GoldMentions {

    static public void main (String[] args) {
         String pathToGold = "/Users/piek/Desktop/Roxane/CEO_evaluation/gold";
         String mentionFolder = "/Users/piek/Desktop/Roxane/Tommaso-v4/all_event_mentions";
         ArrayList<File> files = MentionReader.makeRecursiveFileList(new File(pathToGold), ".eval");
         for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            if (!file.getName().equals("19_1ecb.xml.eval"))  continue;
             System.out.println("file.getName() = " + file.getName());
             String mentionFileName = file.getName().substring(0, file.getName().lastIndexOf("."))+"_all_event_mentions.eval";
             String systemFileName = file.getName().substring(0, file.getName().lastIndexOf("."))+"_all_event_mentions.eval.bl";
             System.out.println("mentionFileName = " + mentionFileName);
             System.out.println("systemFileName = " + systemFileName);
             File mentionFile = new File (mentionFolder+"/"+mentionFileName);
             File systemFile = new File (mentionFolder+"/"+systemFileName);
             ArrayList<Mention> mentions = readFileToMentionList(mentionFile);
             ArrayList<Mention> goldmentions = readGoldFileToMentionList(file);
             ArrayList<Mention> systemmentions = readGoldFileToMentionList(systemFile);
             System.out.println("mentions.size() = " + mentions.size());
             System.out.println("goldmentions.size() = " + goldmentions.size());
             System.out.println("systemmentions.size() = " + systemmentions.size());
             for (int j = 0; j < goldmentions.size(); j++) {
                 Mention goldmention = goldmentions.get(j);
                 if (!hasMention(mentions, goldmention)) {
                     System.out.println("Goldmention missing in eval = " + goldmention.toString());
                 }
                 else if (!hasMention(systemmentions, goldmention)) {
                      System.out.println("Goldmention missing in system out = " + goldmention.toString());
                 }
             }
            // break;
         }

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
    static ArrayList<Mention> readGoldFileToMentionList (File file) {
        ArrayList<Mention> mentionArrayList = new ArrayList<Mention>();
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                while (in.ready() && (inputLine = in.readLine()) != null) {
                    if (inputLine.trim().length() > 0) {
                        String [] fields = inputLine.split("\t");
                        if (fields.length==4) {
                            Mention mention1 = new Mention(fields[1]);
                            Mention mention2 = new Mention(fields[2]);
                            if (!hasMention(mentionArrayList, mention1)) mentionArrayList.add(mention1);
                            if (!hasMention(mentionArrayList, mention2)) mentionArrayList.add(mention2);
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
        return mentionArrayList;
    }

    static boolean hasMention (ArrayList<Mention> list, Mention mention) {
        boolean match = false;
        for (int i = 0; i < list.size(); i++) {
            Mention mention1 = list.get(i);
            if (mention1.toString().equals(mention.toString())) {
                return true;
            }
        }
        return match;
    }

}
