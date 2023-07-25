package com.text2phenotype.ctakes.rest.api.pipeline.helpers.mb;

import com.text2phenotype.ctakes.rest.api.pipeline.annotations.UmlsConceptFlex;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.CONST;
import com.text2phenotype.ctakes.rest.api.pipeline.model.ContentModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.IdentifiedAnnotationModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.SabConceptModel;
import com.text2phenotype.ctakes.rest.api.pipeline.model.UmlsConceptModel;
import static org.apache.ctakes.typesystem.type.constants.CONST.*;
import static com.text2phenotype.ctakes.rest.api.pipeline.helpers.CONST.*;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.ctakes.typesystem.type.util.Pair;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.cas.FSArray;

import java.util.*;

public class ResponseModelBuilderUtils {

    private ResponseModelBuilderUtils(){ throw new AssertionError(); }

    /**
     * Get aspect data by mention
     * @param mention Event mention
     * @return
     */
    public static String getAspect(IdentifiedAnnotation mention) {

        int annotationClass = mention.getTypeID();

        switch (annotationClass) {
            case NE_TYPE_ID_PROCEDURE:
                return "proc";
            case NE_TYPE_ID_FINDING:
                return "symp";
            case NE_TYPE_ID_DISORDER:
                return "prob";
            case NE_TYPE_ID_DRUG:
                return "device";
            case NE_TYPE_ID_ANATOMICAL_SITE:
                return "anat";
            case NE_TYPE_ID_ACTIVITY:
                return "activity";
            default:
                return "unknown";
        }
    }

    public static void createOntologyConcept(IdentifiedAnnotation mention, ContentModel contentModel) {
        FSArray umlses = mention.getOntologyConceptArr();
        if (umlses != null) {
            Map<String, UmlsConceptModel> umlsConcepts = new HashMap<>();

            for (int idx = 0; idx < umlses.size(); idx++) {
                FeatureStructure structure = umlses.get(idx);
                if (structure instanceof UmlsConcept) {
                    UmlsConcept umls = (UmlsConcept) structure;

                    String tty = null;
                    if (umls instanceof UmlsConceptFlex) {
                        UmlsConceptFlex umlsFlex = (UmlsConceptFlex) umls;
                        if (umlsFlex.getParams() != null) {
                            for (int i = 0; i < umlsFlex.getParams().size(); i++) {
                                Pair param = umlsFlex.getParams(i);
                                String paramName = param.getAttribute();
                                if (paramName.equals(TTY)) {
                                    tty = param.getValue();
                                    break;
                                }
                            }
                        }
                    }

                    String cui = umls.getCui();
                    if (!umlsConcepts.containsKey(cui)) {
                        UmlsConceptModel newUmlsModel = new UmlsConceptModel(
                                umls.getCui(),
                                umls.getPreferredText()
                        );

                        umlsConcepts.put(cui, newUmlsModel);
                    }

                    UmlsConceptModel newUmlsModel = umlsConcepts.get(cui);
                    newUmlsModel.setTui(umls.getTui());
                    newUmlsModel.setSabConcepts(new SabConceptModel(umls.getCodingScheme()));
                    newUmlsModel.setTty(umls.getCodingScheme(), umls.getCode(), tty);
                }
            }

            contentModel.getUmlsConcepts().addAll(umlsConcepts.values());
        }
    }

    public static <ANNOTATION_TYPE extends org.apache.uima.jcas.tcas.Annotation> void setSentenceSegmentData(
            Map<ANNOTATION_TYPE, Collection<Sentence>> sentenceIndex,
            Map<Sentence, Collection<Segment>> segmentIndex,
            IdentifiedAnnotationModel model,
            ANNOTATION_TYPE annotation
    ) {
        if (sentenceIndex.containsKey(annotation)) {
            Optional<Sentence> sent = sentenceIndex.get(annotation).stream().findFirst();
            if (sent.isPresent()) {
                Sentence sent_annotation = sent.get();
                model.setSentenceData(sent_annotation);
                // set segment data
                if (segmentIndex.containsKey(sent_annotation)) {
                    Optional<Segment> segment = segmentIndex.get(sent_annotation).stream().findFirst();
                    segment.ifPresent(model::setSectionOffset);
                }
            }
        }
    }
}
