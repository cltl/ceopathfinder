import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class ResultTable {

    /*
    /Users/piek/Desktop/Roxane/Tommaso-v5/gold
    /Users/piek/Desktop/Roxane/Tommaso-v5/out-baseline
    .eval.bl1S
    Precision pairs-value strict: 0.7745617238860482
    Recall pairs-value strict: 0.7986254942572021
    F1 pairs-value strict: 0.786409567071475

    Precision pairs-only: 0.868882395909423
    Recall pairs-only: 0.8164736164736165
    F1 pairs-only: 0.8418631397354801
     */
    static public void main (String[] args) {
        String pathToResultFolder = "";
        String label = "";
        pathToResultFolder = "/Code/vu/ceopathfinder/pathfinder/";
        label = "test_noexpand";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            //System.out.println("parameter value = " + arg);

            if (arg.equalsIgnoreCase("--input") && args.length > (i + 1)) {
                pathToResultFolder = args[i + 1];
            } else if (arg.equalsIgnoreCase("--label") && args.length > (i + 1)) {
                label = args[i + 1];
            }
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        try {
            String header = "";
            ArrayList<String> resultList = new ArrayList<String>();
            ArrayList<String> pstrictList = new ArrayList<String>();
            ArrayList<String> rstrictList = new ArrayList<String>();
            ArrayList<String> fstrictList = new ArrayList<String>();
            ArrayList<String> plooseList = new ArrayList<String>();
            ArrayList<String> rlooseList = new ArrayList<String>();
            ArrayList<String> flooseList = new ArrayList<String>();
            resultList.add(dateFormat.format(date)+header);
            String pstrict ="Precision strict"; pstrictList.add(pstrict);
            String rstrict ="Recall strict"; rstrictList.add(rstrict);
            String fstrict ="F1 strict";  fstrictList.add(fstrict);
            String ploose ="Precision loose"; plooseList.add(ploose);
            String rloose ="Recall loose"; rlooseList.add(rloose);
            String floose ="F1 loose"; flooseList.add(floose);

            ArrayList<File> files = Util.makeRecursiveFileListStart(new File(pathToResultFolder), "out.eval.");
            Collections.sort(files);
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                if (file.exists()) {
                        String name = file.getName();
                        name = name.substring(name.lastIndexOf(".")+1);
                        resultList.add(name);
                        header += "\t"+name;
                        FileInputStream fis = new FileInputStream(file);
                        InputStreamReader isr = new InputStreamReader(fis);
                        BufferedReader in = new BufferedReader(isr);
                        String inputLine;
                        while (in.ready() && (inputLine = in.readLine()) != null) {
                            if (inputLine.trim().length() > 0) {
                                String [] fields = inputLine.split(":");
                                if (fields.length>1) {
                                    Double score = null;
                                    try {
                                        score = Double.parseDouble(fields[1].trim());
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                    }
                                    if (inputLine.startsWith("Precision pairs-value strict")) {
                                       pstrict+="\t"+score.toString();
                                       pstrictList.add(score.toString());
                                    }
                                    else if (inputLine.startsWith("Recall pairs-value strict")) {
                                       rstrict+="\t"+score.toString();
                                       rstrictList.add(score.toString());

                                    }
                                    else if (inputLine.startsWith("F1 pairs-value strict")) {
                                       fstrict+="\t"+score.toString();
                                       fstrictList.add(score.toString());

                                    }
                                    else if (inputLine.startsWith("Precision pairs-only")) {
                                       ploose+="\t"+score.toString();
                                       plooseList.add(score.toString());
                                    }
                                    else if (inputLine.startsWith("Recall pairs-only")) {
                                       rloose+="\t"+score.toString();
                                       rlooseList.add(score.toString());
                                    }
                                    else if (inputLine.startsWith("F1 pairs-only")) {
                                       floose+="\t"+score.toString();
                                       flooseList.add(score.toString());
                                    }
                                }
                            }
                        }
                        in.close();
                } else {
                    System.out.println("Cannot find file = " + file.getAbsolutePath());
                }
            }
            String result = dateFormat.format(date)+header+"\n";
            result += pstrict+"\n";
            result += rstrict+"\n";
            result += fstrict+"\n";
            result += ploose+"\n";
            result += rloose+"\n";
            result += floose+"\n";

            result += "\n";

            for (int i = 0; i < resultList.size(); i++) {
                result +=  resultList.get(i)+"\t";
                result +=  pstrictList.get(i)+"\t";
                result +=  rstrictList.get(i)+"\t";
                result +=  fstrictList.get(i)+"\t";
                result +=  plooseList.get(i)+"\t";
                result +=  rlooseList.get(i)+"\t";
                result +=  flooseList.get(i)+"\n";
            }
            OutputStream fos = new FileOutputStream(pathToResultFolder+"/"+label+"results.xls");
            fos.write(result.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
