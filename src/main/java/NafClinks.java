import eu.kyotoproject.kaf.*;
import eu.kyotoproject.util.FileProcessor;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class NafClinks {



        
        static HashMap<String, ArrayList<Clink>> getClinksFromNaf (String nafFolder) {
            HashMap<String, ArrayList<Clink>> map = new HashMap<String, ArrayList<Clink>>();
            ArrayList<File> nafFiles = FileProcessor.makeRecursiveFileArrayList(nafFolder, ".xml");
            for (int i = 0; i < nafFiles.size(); i++) {
                File nafFile = nafFiles.get(i);
                ArrayList<Clink> clinks = new ArrayList<Clink>();
                KafSaxParser kafSaxParser = new KafSaxParser();
                kafSaxParser.parseFile(nafFile);

                if (kafSaxParser.kafClinks.size() > 0) {
                    String fileName = nafFile.getName();
                    int idx = fileName.indexOf(".");
                    if (idx > -1) {
                        fileName = fileName.substring(0, idx);
                    }
                    for (int j = 0; j < kafSaxParser.kafClinks.size(); j++) {
                        KafEventRelation kafEventRelation = kafSaxParser.kafClinks.get(j);
                        KafWordForm causeFrom = getTokenIdFromPredId(kafSaxParser, kafEventRelation.getFrom());
                        KafWordForm causeTo = getTokenIdFromPredId(kafSaxParser, kafEventRelation.getTo());
                        Clink clink = new Clink();
                        clink.setCauseFrom(causeFrom);
                        clink.setCauseTo(causeTo);
                        clinks.add(clink);
    /*                    String str = j + "\t" + causeFrom.getWid() + ":" + causeFrom.getSent() + ":" + causeFrom.getWf() + "\t" +
                                causeTo.getWid() + ":" + causeTo.getSent() + ":" + causeTo.getWf() + "\tHCPE\n";
                        System.out.println("str = " + str);*/
                    }
                    map.put(fileName, clinks);
                }
            }
            return map;
        }

        static HashMap<String, ArrayList<Clink>> getClinks (String nafFolder) {
            HashMap<String, ArrayList<Clink>> map = new HashMap<String, ArrayList<Clink>>();
            ArrayList<File> clinkFiles = FileProcessor.makeRecursiveFileArrayList(nafFolder, ".xml");
            for (int i = 0; i < clinkFiles.size(); i++) {
                File file = clinkFiles.get(i);
                String fileName = file.getName();
                int idx = fileName.indexOf(".");
                if (idx > -1) {
                    fileName = fileName.substring(0, idx);
                }
                ArrayList<Clink> clinks = null;
                try {
                    clinks = getClinksFromFile (file);
                    map.put(fileName, clinks);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return map;
        }

        static ArrayList<Clink> getClinksFromFile (File file) throws IOException {
            ArrayList<Clink> clinkArrayList = new ArrayList<Clink>();
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine;
            while (in.ready() && (inputLine = in.readLine()) != null) {
                if (inputLine.trim().length() > 0) {
                    //11	w430:19:safety	w451:19:absence	HCPE
                    //12	w478:21:friends	w483:21:show	HCPE
                    String [] fields = inputLine.split("\t");
                    if (fields.length>1) {
                        String[] fromStrings = fields[1].split(":");
                        String[] toStrings = fields[2].split(":");
                        KafWordForm causeFrom = new KafWordForm();
                        causeFrom.setWid(fromStrings[0]);
                        causeFrom.setSent(fromStrings[1]);
                        causeFrom.setWf(fromStrings[2]);
                        KafWordForm causeTo = new KafWordForm();
                        causeTo.setWid(toStrings[0]);
                        causeTo.setSent(toStrings[1]);
                        causeTo.setWf(toStrings[2]);
                        Clink clink = new Clink();
                        clink.setCauseFrom(causeFrom);
                        clink.setCauseTo(causeTo);
                        clinkArrayList.add(clink);
                    }
                }
            }
            in.close();
            return clinkArrayList;
        }

        static KafWordForm getTokenIdFromPredId (KafSaxParser kafSaxParser, String predId) {
            KafWordForm tokenId = null;
            for (int i = 0; i < kafSaxParser.kafEventArrayList.size(); i++) {
                KafEvent kafEvent = kafSaxParser.kafEventArrayList.get(i);
                if (kafEvent.getId().equals(predId)) {
                    for (int j = 0; j < kafEvent.getSpanIds().size(); j++) {
                        KafTerm kafTerm = kafSaxParser.getTerm(kafEvent.getSpanIds().get(j));
                        if (kafTerm!=null) {
                            ArrayList<String> tokenIds = kafTerm.getSpans();
                            tokenId = kafSaxParser.getWordForm(tokenIds.get(0)); /// we take the first token ids as we cannot handle multiwords yet
                            break;
                        }
                    }
                }
            }
            return tokenId;
        }
}
