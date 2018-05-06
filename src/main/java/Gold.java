public class Gold {


    /**
     9	820:34:things	828:34:pain	HCPE
     9	18:0:killed	828:34:pain	HCPE
     9	196:7:murdering	828:34:pain	HCPE
     9	788:33:killings	828:34:pain	HCPE
     */

    String id;
    Mention mention1;
    Mention mention2;

    public Gold() {
        this.id = "";
        this.mention1 = new Mention();
        this.mention2 = new Mention();
    }

    public Gold(String str) {
        this.id = "";
        this.mention1 = new Mention();
        this.mention2 = new Mention();
        String [] fields = str.split("\t");
        if (fields.length>2) {
            this.id = fields[0].trim();
            mention1 = new Mention(fields[1]);
            mention2 = new Mention(fields[2]);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Mention getMention1() {
        return mention1;
    }

    public void setMention1(Mention mention1) {
        this.mention1 = mention1;
    }

    public Mention getMention2() {
        return mention2;
    }

    public void setMention2(Mention mention2) {
        this.mention2 = mention2;
    }
}
