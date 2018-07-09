import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created by piek on 2/18/15.
 */
public class FrameNetReader extends DefaultHandler {

    public ArrayList<Clink> clinks = new ArrayList<Clink>();
    String subFrame = "";
    String superFrame = "";
    String value = "";
    boolean CAUSE = false;

    public FrameNetReader() {
        init();
    }

    static public void main (String [] args) {
        String fnPath = "/Resources/FrameNet/fndata-1.7/frRelation.xml";
        FrameNetReader frameNetReader = new FrameNetReader();
        frameNetReader.parseFile(fnPath);
        for (int i = 0; i < frameNetReader.clinks.size(); i++) {
            Clink clink = frameNetReader.clinks.get(i);
            System.out.println("clink.causeFrom.toString() = " + clink.causeFrom.toString());
            System.out.println("clink.causeTo.toString() = " + clink.causeTo.toString());
        }
    }


    public void parseFile(String filePath) {
        String myerror = "";
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            SAXParser parser = factory.newSAXParser();
            InputSource inp = new InputSource (new FileReader(filePath));
            parser.parse(inp, this);
        } catch (SAXParseException err) {
            myerror = "\n** Parsing error" + ", line " + err.getLineNumber()
                    + ", uri " + err.getSystemId();
            myerror += "\n" + err.getMessage();
            System.out.println("myerror = " + myerror);
        } catch (SAXException e) {
            Exception x = e;
            if (e.getException() != null)
                x = e.getException();
            myerror += "\nSAXException --" + x.getMessage();
            System.out.println("myerror = " + myerror);
        } catch (Exception eee) {
            eee.printStackTrace();
            myerror += "\nException --" + eee.getMessage();
            System.out.println("myerror = " + myerror);
        }
}//--c

    public void init () {
        clinks = new ArrayList<Clink>();
        subFrame = "";
        superFrame = "";
        CAUSE = false;
    }

    //    <frameRelation subID="171" supID="82" subFrameName="Commerce_buy" superFrameName="Commerce_scenario" ID="360">

    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {

        if (qName.equalsIgnoreCase("frameRelationType")) {
            String name = attributes.getValue("name");
            if (name!=null) {
                if (name.equals("Causative_of")) {
                   CAUSE = true;
                }
                else if (name.equals("Inchoative_of")) {
                   CAUSE = true;
                }
                else {
                    CAUSE = false;
                }
            }
        }
        if (CAUSE) {
            if (qName.equalsIgnoreCase("frameRelation")) {
                subFrame = "";
                superFrame = "";
                for (int i = 0; i < attributes.getLength(); i++) {
                    String name = attributes.getQName(i);
                    if (name.equalsIgnoreCase("subFrameName")) {
                        subFrame = attributes.getValue(i).trim();
                    } else if (name.equalsIgnoreCase("superFrameName")) {
                        superFrame = attributes.getValue(i).trim();
                    }
                }
                if (!subFrame.isEmpty() && !superFrame.isEmpty()) {
                    Clink clink = new Clink();
                    clink.causeFrom.setWf(superFrame);
                    clink.causeTo.setWf(subFrame);
                    clinks.add(clink);
                }
            }
        }
        value = "";
    }//--startElement

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
    }

    public void characters(char ch[], int start, int length)
            throws SAXException {
        value += new String(ch, start, length);
        // System.out.println("tagValue:"+value);
    }

}
