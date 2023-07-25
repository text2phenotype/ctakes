package com.text2phenotype.ctakes.rest.utils;

import com.text2phenotype.ctakes.rest.api.pipeline.ae.LabValuesAnnotatorSequence;
import com.text2phenotype.ctakes.rest.api.pipeline.ae.TokenAdjuster;
import com.text2phenotype.ctakes.rest.api.pipeline.annotations.SpecialLabValueWord;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ServiceTypeSystemDescription;
import org.apache.ctakes.core.ae.TokenizerAnnotatorPTB;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.postagger.POSTagger;
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.relation.RelationArgument;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.LabMention;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.jar.DefaultSequenceDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.JarClassifierBuilder;
import org.cleartk.ml.jar.JarClassifierFactory;
import org.cleartk.ml.mallet.MalletCrfStringOutcomeDataWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LabValuesTrainer {

    private final String LAB_MENTION_TYPE = "t";
    private final String VALUE_MENTION_TYPE = "v";

    private class RelationData {
        private IdentifiedAnnotation lab;
        private IdentifiedAnnotation value;

        public IdentifiedAnnotation getLab() {
            return lab;
        }

        public void setLab(IdentifiedAnnotation lab) {
            this.lab = lab;
        }

        public IdentifiedAnnotation getValue() {
            return value;
        }

        public void setValue(IdentifiedAnnotation value) {
            this.value = value;
        }
    }

    public static void main(String[] args){
        //Assert.isTrue(args.length > 0, "Training data file is not defined");

        //LabValuesTrainer trainer = new LabValuesTrainer(args[0]);
        LabValuesTrainer trainer = new LabValuesTrainer("/Users/antonvasin/Documents/LabValuesDataSet/model.txt");
        trainer.train();

    }

    private List<String> lines = new ArrayList<>();

    public LabValuesTrainer(String filePath){
        try {
            File file = new File(FileLocator.getFullPath(filePath));
            byte[] data = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);

            try {
                fis.read(data);
                String str = new String(data, "UTF-8");
                int index = 0;
                while(index!=-1) {
                    // search beginning of a sentence
                    int begin = str.indexOf("<s>", index);
                    if (begin == -1) {
                        break;
                    }

                    // search end
                    int end = str.indexOf("</s>", begin);
                    if (end == -1) {
                        end = str.length()-1;
                    }

                    lines.add(str.substring(begin+3, end));
                    index = end;
                }
            } finally {
                fis.close();
            }




        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void train() {
        try {
            JCas jcas = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());

            Map<String, RelationData> relations = new HashMap<>();

            Pattern annotationBeginPattern = Pattern.compile("<(t|v)(:r[0-9_]+)?>");
            Pattern annotationEndPattern = Pattern.compile("</(t|v)(:r[0-9_]+)?>");

            StringBuilder resultText = new StringBuilder();

            int sentShift = 0;
            for (String line: lines) {
                Matcher annotationBeginMatcher = annotationBeginPattern.matcher(line);
                Matcher annotationEndMatcher = annotationEndPattern.matcher(line);
                int shift = 0;
                while (annotationBeginMatcher.find()) {
                    if (annotationEndMatcher.find()) {
                        shift += annotationBeginMatcher.end()-annotationBeginMatcher.start();
                        int annotationBegin = annotationBeginMatcher.end() - shift + sentShift;
                        int annotationEnd = annotationEndMatcher.start() - shift + sentShift;
                        String relationName = annotationBeginMatcher.group(2);
                        String annotionType = annotationBeginMatcher.group(1);
                        if (LAB_MENTION_TYPE.equals(annotionType)) {
                            LabMention lab = new LabMention(jcas, annotationBegin, annotationEnd);
                            lab.addToIndexes();
                            if (relationName != null) {
                                RelationData data = relations.getOrDefault(relationName, new RelationData());
                                data.setLab(lab);
                                relations.put(relationName, data);
                            }
                        } else {
                            if (VALUE_MENTION_TYPE.equals(annotionType)) {
                                SpecialLabValueWord value = new SpecialLabValueWord(jcas, annotationBegin, annotationEnd);
                                value.addToIndexes();
                                if (relationName != null) {
                                    RelationData data = relations.getOrDefault(relationName, new RelationData());
                                    data.setValue(value);
                                    relations.put(relationName, data);
                                }
                            }
                        }
                        shift += annotationEndMatcher.end()-annotationEndMatcher.start();


                    }
                }

                line = line.replaceAll(annotationBeginPattern.pattern(), "");
                line = line.replaceAll(annotationEndPattern.pattern(), "");
                resultText.append(line);
                Sentence sent = new Sentence(jcas, sentShift, sentShift + line.length());
                sent.addToIndexes();
                sentShift += line.length();

            }

            for (RelationData data: relations.values()) {
                BinaryTextRelation rel = new BinaryTextRelation(jcas);

                RelationArgument arg1 = new RelationArgument(jcas);
                arg1.setArgument(data.getLab());
                rel.setArg1(arg1);

                RelationArgument arg2 = new RelationArgument(jcas);
                arg2.setArgument(data.getValue());
                rel.setArg2(arg2);

                rel.addToIndexes();
            }
            jcas.setDocumentText(resultText.toString());

            // create segment
            Segment seg = new Segment(jcas, 0, jcas.getDocumentText().length());
            seg.addToIndexes();
            AnalysisEngineDescription tokenizerDesc = AnalysisEngineFactory.createEngineDescription(TokenizerAnnotatorPTB.class);
            AnalysisEngineDescription tokenAdjusterDesc = AnalysisEngineFactory.createEngineDescription(TokenAdjuster.class);

            AnalysisEngineDescription labValuesAnnotatorDesc = AnalysisEngineFactory.createEngineDescription(LabValuesAnnotatorSequence.class,
                    CleartkSequenceAnnotator.PARAM_IS_TRAINING,
                    true,
                    "classifierFactoryClassName",
                    JarClassifierFactory.class,
                    "dataWriterFactoryClassName",
                    DefaultSequenceDataWriterFactory.class,
                    DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
                    "/Users/antonvasin/Documents/LabValuesDataSet/files",
                    DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
                    MalletCrfStringOutcomeDataWriter.class,
                    "classifierJarPath",
                    "/com/text2phenotype/ctakes/resources/lab_values/model.jar"

            );
            //AnalysisEngineDescription posTaggerDesc = POSTagger.createAnnotatorDescription();
            AnalysisEngineDescription posTaggerDesc = AnalysisEngineFactory.createEngineDescription(POSTagger.class,
                    POSTagger.PARAM_POS_MODEL_FILE,
                    "org/apache/ctakes/postagger/models/mayo-pos.zip");

            final AggregateBuilder builder = new AggregateBuilder();
            builder.add(tokenizerDesc);
            builder.add(tokenAdjusterDesc);
            builder.add(posTaggerDesc);
            builder.add(labValuesAnnotatorDesc);

            AnalysisEngine engine = builder.createAggregate();
            engine.process(jcas);
            engine.collectionProcessComplete();

            File dir = new File("/Users/antonvasin/Documents/LabValuesDataSet/files");
            JarClassifierBuilder<?> classifierBuilder = JarClassifierBuilder.fromTrainingDirectory(dir);
//            classifierBuilder.trainClassifier(dir, "-s", "2");
            classifierBuilder.trainClassifier(dir, "--default-label", "NONE", "--random-seed", "1");
            classifierBuilder.packageClassifier(dir);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
