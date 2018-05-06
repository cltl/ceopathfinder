import java.util.ArrayList;

public class NarrativeChain {

    ArrayList<String> eventLemmas;
    String id;

    public NarrativeChain(String id, ArrayList<String> eventLemmas) {
        this.eventLemmas = eventLemmas;
        this.id = id;
    }

    public NarrativeChain() {
        this.eventLemmas = new ArrayList<String>();
        this.id = "";
    }

    public ArrayList<String> getEventLemmas() {
        return eventLemmas;
    }

    public void setEventLemmas(ArrayList<String> eventLemmas) {
        this.eventLemmas = eventLemmas;
    }

    public void addEventLemma(String lemma) {
        this.eventLemmas.add(lemma);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
