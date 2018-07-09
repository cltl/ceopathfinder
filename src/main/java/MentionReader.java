import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @TODO
 * - read gold instead of the mention list to be able to do instance and mention based mapping
 * - output per heuristiek post-pre, post-dur, dur-pre
 * - narrative chains versie
 */


public class MentionReader {
    static boolean EXPAND = false;
    static boolean DEBUG = false;
    static int METHOD = -1;   // 0 = BASELINE, 1 = CEO, 2 = NarrativeChainMatching, 3 = FBK PRO, 4 = Framenet cause
    static HashMap<String, ArrayList<String>> mentionInstanceMap = null;
    static HashMap<String, ArrayList<String>> instancMentionMap = null;


    static String testCeoParameters = "--ceo-lexicon /Code/vu/ceopathfinder/pathfinder/resources/ceo-lexicon-ecb-v1.txt " +
            "--ceo-ontology /Code/vu/ceopathfinder/pathfinder/data/resources/CEO_version_1.owl " +
            "--ontology-depth 1 " +
            "--property-threshold 1 " +
            "--input /Code/vu/ceopathfinder/data/gold " +
            "--output /Code/vu/ceopathfinder/data/out " +
            "--intermediate 1 " +
            "--gold " +
            "--method 1 " +
            "--debug";

    static String testNarrativeChainParameters = "--chains /Code/vu/ceopathfinder/pathfinder/resources/narrativechains/EventChains_JurChamb_CEO.rtf " +
            "--input /Code/vu/ceopathfinder/pathfinder/data/gold " +
            "--output /Code/vu/ceopathfinder/pathfinder/data/out " +
            "--gold " +
            "--method 2 " +
            "--debug";

 static String testNafClinkParameters = "--clinks /Code/vu/ceopathfinder/pathfinder/data/nwr-clinks " +
            "--input /Code/vu/ceopathfinder/pathfinder/data/gold " +
            "--output /Code/vu/ceopathfinder/pathfinder/data/out " +
            "--gold " +
            "--method 3 " +
            "--debug";

 static String testFrameNetParameters = "--fn-relations /Resources/FrameNet/fndata-1.7/frRelation.xml " +
            "--fn-lexicon /Resources/FrameNet/fndata-1.7/luIndex.xml " +
            "--input /Code/vu/ceopathfinder/pathfinder/data/gold " +
            "--output /Code/vu/ceopathfinder/pathfinder/data/out " +
            "--gold " +
            "--method 4 " +
            "--debug";

    static public void main (String[] args) {
        String lexiconPath = "";
        String pathToCeo = "";
        String mentionFolder = "";
        String outputFolder = "";
        String chainpath = "";
        String clinkpath = "";
        String fnRelationsPath = "";
        String fnLexiconPath = "";

        //// CEO properties depth and rule
        Integer depth = 0;
        Integer rule = 0; // 0 = full assertion, 1 = property, 2 = subject-property, 3 = subject - property - object
        Integer threshold = 1;
        Integer intermediate = 0;

        if (args.length==0) {
            //args = testCeoParameters.split(" ");
            //args = testNarrativeChainParameters.split(" ");
            //args = testNafClinkParameters.split(" ");
            args = testFrameNetParameters.split(" ");
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
            else if (arg.equalsIgnoreCase("--ontology-depth") && args.length>(i+1)){
                depth = Integer.parseInt(args[i+1]);
            }
            else if (arg.equalsIgnoreCase("--rule") && args.length>(i+1)){
                rule = Integer.parseInt(args[i+1]);
            }
            else if (arg.equalsIgnoreCase("--property-threshold") && args.length>(i+1)){
                threshold = Integer.parseInt(args[i+1]);
            }

            else if (arg.equalsIgnoreCase("--intermediate") && args.length>(i+1)){
                intermediate = Integer.parseInt(args[i+1]);
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
            else if (arg.equalsIgnoreCase("--chains") && args.length>(i+1)){
                chainpath = args[i+1];
            }
            else if (arg.equalsIgnoreCase("--clinks") && args.length>(i+1)){
                clinkpath = args[i+1];
            }
            else if (arg.equalsIgnoreCase("--fn-relations") && args.length>(i+1)){
                fnRelationsPath = args[i+1];
            }
            else if (arg.equalsIgnoreCase("--fn-lexicon") && args.length>(i+1)){
                fnLexiconPath = args[i+1];
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
                CeoMatching.ceoPathFinder.readLexiconFile(new File(lexiconPath));
                CeoMatching.ceoPathFinder.readOwlFile(pathToCeo);
                CeoMatching.ceoPathFinder.setDuring(true);
                CeoMatching.ceoPathFinder.setRule(rule);
                CeoMatching.ceoPathFinder.setInheritanceDepth(depth);
                CeoMatching.ceoPathFinder.setIntermediate(intermediate);
                CeoMatching.ceoPathFinder.setThreshold(threshold);
            }
            else {
                if (pathToCeo.isEmpty()) System.err.println("No CEO ontology provided");
                if (lexiconPath.isEmpty()) System.err.println("No CEO lexicon provided");
            }
        }
        else if (METHOD==2) {
            if (!chainpath.isEmpty() && METHOD == 2) {
                NarrativeChainMatching.getNarrativeChains(chainpath);
                NarrativeChainMatching.buildVerbIndex();
                System.out.println("chains.size() = " + NarrativeChainMatching.chains.size());
                System.out.println("verbs.size() = " + NarrativeChainMatching.verbs.size());
            }
            else {
                System.err.println("No narrative chains provided");
            }
        }
        else if (METHOD==3) {
            if (!clinkpath.isEmpty()) {
                NafClinkMatching.clinks = NafClinks.getClinks(clinkpath);
                System.out.println("clinks.size() = " + NafClinkMatching.clinks.size());
            }
            else {
                System.err.println("No clinks folder provided");
            }
        }
        else if (METHOD==4) {
            if (fnRelationsPath.isEmpty()) {
                System.out.println("No FrameNet relation provided");
            }
            if (fnLexiconPath.isEmpty()) {
                System.out.println("No FrameNet relation provided");
            }
            if (!fnRelationsPath.isEmpty() && !fnLexiconPath.isEmpty()) {
                FrameNetMatching.frameNetReader.parseFile(fnRelationsPath);
                FrameNetMatching.frameNetLuReader.parseFile(fnLexiconPath);
                System.out.println("FrameNetMatching.frameNetReader.clinks.size() = " + FrameNetMatching.frameNetReader.clinks.size());
                System.out.println("FrameNetMatching.frameNetLuReader.lexicalUnitFrameMap.size() = " + FrameNetMatching.frameNetLuReader.lexicalUnitFrameMap.size());
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
                String ceoResultB1 = BaseLineMatching.sameSentenceMentionBaselineMatch(mentions);
                try {
                    OutputStream fos = new FileOutputStream(outputFolder+"/"+file.getName() + ".bl1S");
                    fos.write(ceoResultB1.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String ceoResultB3 = BaseLineMatching.twoSentenceMentionBaselineMatch(mentions);
                try {
                    OutputStream fos = new FileOutputStream(outputFolder+"/"+file.getName() + ".bl3S");
                    fos.write(ceoResultB3.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String ceoResultB5 = BaseLineMatching.fourSentenceMentionBaselineMatch(mentions);
                try {
                    OutputStream fos = new FileOutputStream(outputFolder+"/"+file.getName() + ".bl5S");
                    fos.write(ceoResultB5.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String ceoResultBAny = BaseLineMatching.allMentionBaselineMatch(mentions);
                try {
                    OutputStream fos = new FileOutputStream(outputFolder+"/"+file.getName() + ".blANY");
                    fos.write(ceoResultBAny.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
             }
             else if (METHOD==1) {  /// CEO
                 String ceoResultAny = CeoMatching.anyMentionCeoMatch(mentions);
                 try {
                     OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".ceoANY");
                     fos.write(ceoResultAny.getBytes());
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }

                 String ceoResult1 = CeoMatching.sameSentenceMentionCeoMatch(mentions);
                 try {
                     OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".ceo1S");
                     fos.write(ceoResult1.getBytes());
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }

                 String ceoResult3 = CeoMatching.twoSentenceMentionCeoMatch(mentions);
                 try {
                     OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".ceo3S");
                     fos.write(ceoResult3.getBytes());
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 String ceoResult5 = CeoMatching.fourSentenceMentionCeoMatch(mentions);
                 try {
                     OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".ceo5S");
                     fos.write(ceoResult5.getBytes());
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
             else if (METHOD == 2) {  /// NarrativeChainMatching
                 String ceoResultAny = NarrativeChainMatching.anyMentionNarrativeChainMatch(mentions);
                 try {
                     OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".ncANY");
                     fos.write(ceoResultAny.getBytes());
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }

                 String ceoResult1 = NarrativeChainMatching.sameSentenceMentionNarrativeChainMatch(mentions);
                 try {
                     OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".nc1S");
                     fos.write(ceoResult1.getBytes());
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }

                 String ceoResult3 = NarrativeChainMatching.twoSentenceMentionNarrativeChainMatch(mentions);
                 try {
                     OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".nc3S");
                     fos.write(ceoResult3.getBytes());
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 String ceoResult5 = NarrativeChainMatching.fourSentenceMentionNarrativeChainMatch(mentions);
                 try {
                     OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".nc5S");
                     fos.write(ceoResult5.getBytes());
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
             else if (METHOD==3) { //FBK PRO cause
                 String ceoResultAny = NafClinkMatching.anyMentionNafClinkMatch(mentions, file.getName());
                 try {
                     OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".nafclANY");
                     fos.write(ceoResultAny.getBytes());
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }

                 String ceoResult1 = NafClinkMatching.sameSentenceMentionNarrativeChainMatch(mentions, file.getName());
                 try {
                     OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".nafcl1S");
                     fos.write(ceoResult1.getBytes());
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }

                 String ceoResult3 = NafClinkMatching.twoSentenceMentionNarrativeChainMatch(mentions, file.getName());
                 try {
                     OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".nafcl3S");
                     fos.write(ceoResult3.getBytes());
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 String ceoResult5 = NafClinkMatching.fourSentenceMentionNarrativeChainMatch( mentions, file.getName());
                 try {
                     OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".nafcl5S");
                     fos.write(ceoResult5.getBytes());
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
             else if (METHOD==4) { //FrameNet cause
                String ceoResultAny = FrameNetMatching.anyMentionFrameNetMatch( mentions);
                try {
                    OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".fnANY");
                    fos.write(ceoResultAny.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String ceoResult1 = FrameNetMatching.sameSentenceMentionFrameNetMatch( mentions);
                try {
                    OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".fn1S");
                    fos.write(ceoResult1.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String ceoResult3 = FrameNetMatching.twoSentenceMentionFrameNetMatch( mentions);
                try {
                    OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".fn3S");
                    fos.write(ceoResult3.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String ceoResult5 = FrameNetMatching.fourSentenceMentionFrameNetMatch( mentions);
                try {
                    OutputStream fos = new FileOutputStream(outputFolder + "/" + file.getName() + ".fn5S");
                    fos.write(ceoResult5.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
             }
         }
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
