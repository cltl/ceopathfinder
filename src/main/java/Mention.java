import java.util.Comparator;

public class Mention {

    /**
     221:8:deliberating
     708:31:penalty
     343:14:said
     */

    private Integer token;
    private String tokenString;
    private Integer sentence;
    private String word;

    public Mention() {
        this.tokenString = "";
        this.token = 0;
        this.sentence = 0;
        this.word = "";
    }

    public Mention(String str) {
        this.token = 0;
        this.sentence = 0;
        this.word = "";
        this.tokenString = "";
        String[] fields= str.split(":");
        ///str = 66_67_68_69:3:took_to_the_streets
        if (fields.length==3) {
            try {
                tokenString = fields[0];
                String [] subfields = tokenString.split("_");
                token=Integer.parseInt(subfields[0]);
                sentence = Integer.parseInt(fields[1]);
            } catch (NumberFormatException e) {
                System.out.println("str = " + str);
                e.printStackTrace();
            }
            word = fields[2];
        }
    }

    public Integer getToken() {
        return token;
    }

    public void setToken(Integer token) {
        this.token = token;
    }

    public Integer getSentence() {
        return sentence;
    }

    public void setSentence(Integer sentence) {
        this.sentence = sentence;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getTokenString() {
        return tokenString;
    }

    public void setTokenString(String tokenString) {
        this.tokenString = tokenString;
    }

    public String toString () {
       String str = tokenString+":"+sentence+":"+word;
       return str;
    }


    static public class sentenceComparator implements Comparator {
        public int compare (Object aa, Object bb) {
            Integer a = ((Mention) aa).getSentence();
            Integer b = ((Mention)bb).getSentence();
            if (a <= b) {
                return 1;
            }
            else {
                return -1;
            }

        }
    }

    static public class tokenComparator implements Comparator {
        public int compare (Object aa, Object bb) {
            Integer a = ((Mention) aa).getToken();
            Integer b = ((Mention)bb).getToken();
            if (a <= b) {
                return 1;
            }
            else {
                return -1;
            }
        }
    }
}
