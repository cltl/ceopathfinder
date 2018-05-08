import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
        pathToResultFolder = args[0];
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        try {
            String header = "";
            String pstrict ="Precision strict";
            String rstrict ="Recall strict";
            String fstrict ="F1 strict";
            String ploose ="Precision loose";
            String rloose ="Recall loose";
            String floose ="F1 loose";
            ArrayList<File> files = Util.makeRecursiveFileListStart(new File(pathToResultFolder), "out.eval.");
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                if (file.exists()) {
                        header = "\t"+file.getName();
                        FileInputStream fis = new FileInputStream(file);
                        InputStreamReader isr = new InputStreamReader(fis);
                        BufferedReader in = new BufferedReader(isr);
                        String inputLine;
                        while (in.ready() && (inputLine = in.readLine()) != null) {
                            if (inputLine.trim().length() > 0) {
                                String [] fields = inputLine.split(":");
                                if (fields.length==2) {
                                    Double score = Double.parseDouble(fields[1].trim());
                                    if (inputLine.startsWith("Precision pairs-value strict")) {
                                       pstrict+="\t"+score.toString();
                                    }
                                    else if (inputLine.startsWith("Recall pairs-value strict")) {
                                       rstrict+="\t"+score.toString();
                                    }
                                    else if (inputLine.startsWith("F1 pairs-value strict")) {
                                       fstrict+="\t"+score.toString();
                                    }
                                    else if (inputLine.startsWith("Precision pairs-only")) {
                                       ploose+="\t"+score.toString();
                                    }
                                    else if (inputLine.startsWith("Recall pairs-only")) {
                                       rloose+="\t"+score.toString();
                                    }
                                    else if (inputLine.startsWith("F1 pairs-only")) {
                                       floose+="\t"+score.toString();
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
            OutputStream fos = new FileOutputStream(pathToResultFolder+"/"+"results.csv");
            fos.write(result.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
