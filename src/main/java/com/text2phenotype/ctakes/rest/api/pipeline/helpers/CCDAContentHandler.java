package com.text2phenotype.ctakes.rest.api.pipeline.helpers;

import com.text2phenotype.ctakes.rest.api.pipeline.ae.CCDASectionAnnotator;
import org.apache.ctakes.typesystem.type.structured.Demographics;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

/**
 * Handles the XML parser events and collects data about segments
 */
public class CCDAContentHandler extends DefaultHandler {

    private static final Stack<String> PATIENT_DATA_ROOT_PATH = new Stack<String>(){{
        push("CLINICALDOCUMENT");
        push("RECORDTARGET");
        push("PATIENTROLE");
        push("PATIENT");
    }};

    private static final Stack<String> PATIENT_GENDER_PATH = new Stack<String>(){{
        addAll(PATIENT_DATA_ROOT_PATH);
        push("ADMINISTRATIVEGENDERCODE");
    }};

    private static final Stack<String> PATIENT_FIRST_NAME_PATH = new Stack<String>(){{
        addAll(PATIENT_DATA_ROOT_PATH);
        push("NAME");
        push("GIVEN");
    }};

    private static final Stack<String> PATIENT_SECOND_NAME_PATH = new Stack<String>(){{
        addAll(PATIENT_DATA_ROOT_PATH);
        push("NAME");
        push("FAMILY");
    }};

    /**
     * Parsing state
     */
    private Stack<String> processState;

    /**
     * patterns collection
     */
    private List<String> patterns;

    /**
     * Segments data
     */
    private List<SegmentData> segments;

    /**
     * Full CCDA text. Used for segments position detection
     */
    private StringBuilder builder = new StringBuilder();

    /**
     * Demographics
     */
    private DemographicsData demographics = new DemographicsData();
    /**
     * @param ccdaXml CCDA document text
     * @param patterns List of patterns
     */
    public CCDAContentHandler(String ccdaXml, List<String> patterns) {
        this.builder.append(ccdaXml);
        this.segments = new ArrayList<>();
        this.patterns = patterns;
    }

    public List<SegmentData> getSegments() {
        return segments;
    }
    public DemographicsData getDemographics() {
        return demographics;
    }

    /**
     * Check if current path is compatible with some pattern
     * @return
     */
    private boolean validatePath() {

        StringBuilder path = new StringBuilder(CCDASectionAnnotator.PATTERN_SEPARATOR);
        for (String statePart : processState) {
            path.append(CCDASectionAnnotator.PATTERN_SEPARATOR).append(statePart);
            if (patterns.contains(path + "/*"))
                return true;
        }
        return patterns.contains(path.toString());
    }

    // overrrides

    /**
     * Start document and reset state
     * @throws SAXException
     */
    @Override
    public void startDocument ()
            throws SAXException
    {
        processState = new Stack<>();
    }

    private boolean checkPath(Stack<String> path) {

        if (processState.size() == path.size()) {

            Iterator<String> processIterator = processState.iterator();
            Iterator<String> genderIterator = path.iterator();
            while (processIterator.hasNext()) {
                if (!Objects.equals(processIterator.next(), genderIterator.next()))
                    return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void startElement (String uri, String localName,
                              String qName, Attributes attributes)
            throws SAXException
    {
        processState.push(localName.toUpperCase());

        // check gender
        if (checkPath(PATIENT_GENDER_PATH)) {

            // fill gender
            String genderCode = attributes.getValue("code");
            if (genderCode != null) {
                demographics.setGender(Objects.equals(genderCode.toUpperCase(), "F") ? CONST.GENDER_FEMALE : CONST.GENDER_MALE);
            }
        }
    }

    @Override
    public void endElement (String uri, String localName, String qName)
            throws SAXException
    {
        processState.pop();
    }

    @Override
    public void characters (char ch[], int start, int length)
            throws SAXException
    {
        if (validatePath()) {

            // find global offset
            int offset = builder.indexOf(new String(ch));
            if (offset < 0)
                return;

            int startPos = offset + start;
            int endPos = startPos + length;

            // check if segment text is not empty
            if (!builder.substring(startPos, endPos).replaceAll("\\s*", "").isEmpty())
                segments.add(new SegmentData(offset + start, offset + start + length));

        } else {

            // check first name
            if (checkPath(PATIENT_FIRST_NAME_PATH)) {
                demographics.setFirstName(new String(ch, start, length));
            }

            // check second name
            if (checkPath(PATIENT_SECOND_NAME_PATH)) {
                demographics.setSecondName(new String(ch, start, length));
            }
        }
    }


    // class helpers

    /**
     * Describes the segment position in the CCDA XML text
     */
    public class SegmentData {
        private int mStart;
        private int mEnd;

        protected SegmentData(int start, int end) {
            mStart = start;
            mEnd = end;
        }


        public int getStart() {
            return mStart;
        }

        public int getEnd() {
            return mEnd;
        }
    }

    /**
     * Demographics
     */
    public class DemographicsData {
        private String firstName;
        private String secondName;
        private String gender;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getSecondName() {
            return secondName;
        }

        public void setSecondName(String secondName) {
            this.secondName = secondName;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }
    }

}
