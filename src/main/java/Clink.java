import eu.kyotoproject.kaf.KafWordForm;

public class Clink {

    KafWordForm causeFrom;
    KafWordForm causeTo;

    public Clink() {
        causeFrom = new KafWordForm();
        causeTo = new KafWordForm();
    }

    public KafWordForm getCauseFrom() {
        return causeFrom;
    }

    public void setCauseFrom(KafWordForm causeFrom) {
        this.causeFrom = causeFrom;
    }

    public KafWordForm getCauseTo() {
        return causeTo;
    }

    public void setCauseTo(KafWordForm causeTo) {
        this.causeTo = causeTo;
    }
}
