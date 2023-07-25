package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import com.text2phenotype.ctakes.rest.api.pipeline.helpers.CCDAContentHandler;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.typesystem.type.structured.Demographics;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


/**
 * Creates segments according to data in the sections path file
 */
public class CCDASectionAnnotator extends JCasAnnotator_ImplBase {

    public static final String PATTERN_SEPARATOR = "/";
    public static final String PARAM_COMMENT = "#";
    public static final String DEFAULT_PATTERNS_PATH = "com/text2phenotype/ctakes/resources/ccda/path_patterns.txt";
    public static final String PATTERNS_PARAM_NAME = "pathPatternsFile";

    private Logger logger = Logger.getLogger(this.getClass());

    @ConfigurationParameter(name = PATTERNS_PARAM_NAME,
            description = "Path to file that contains sections path patterns",
            defaultValue=DEFAULT_PATTERNS_PATH,
            mandatory=false)
    private String pathPatternsFile;

    private List<String> patterns = new ArrayList<>();

    /**
     * Init and load the section paths file
     */
    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        try {
            Object paramValue = aContext.getConfigParameterValue(PATTERNS_PARAM_NAME);
            if (paramValue == null) {
                pathPatternsFile = DEFAULT_PATTERNS_PATH;
            } else {
                pathPatternsFile = paramValue.toString();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(FileLocator.getAsStream(pathPatternsFile)));

            logger.info("Reading path patterns " + pathPatternsFile);
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().startsWith(PARAM_COMMENT)) {
                    if (!line.isEmpty()) {
                        patterns.add(line.trim().toUpperCase());
                    }
                }
            }
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {

        // init content handler
        CCDAContentHandler handler = new CCDAContentHandler(aJCas.getDocumentText(), patterns);

        // create XML parser
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);

        try {
            SAXParser parser = saxFactory.newSAXParser();
            XMLReader reader = parser.getXMLReader();

            // set handler
            reader.setContentHandler(handler);
            reader.parse(new InputSource(new StringReader(aJCas.getDocumentText())));
            
            // creating segments after parsing
            List<CCDAContentHandler.SegmentData> segmentsData = handler.getSegments();
            for (CCDAContentHandler.SegmentData segmentData: segmentsData) {
                Segment newSegment = new Segment(aJCas);
                newSegment.setBegin(segmentData.getStart());
                newSegment.setEnd(segmentData.getEnd());
                newSegment.setId("SIMPLE_SEGMENT");
                newSegment.addToIndexes();
            }

            // add demographics data
            CCDAContentHandler.DemographicsData demographics = handler.getDemographics();
            Demographics demographicsAnnotation = new Demographics(aJCas);
            demographicsAnnotation.setGender(demographics.getGender());
            demographicsAnnotation.setFirstName(demographics.getFirstName());
            demographicsAnnotation.setLastName(demographics.getSecondName());
            demographicsAnnotation.addToIndexes();

        } catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
