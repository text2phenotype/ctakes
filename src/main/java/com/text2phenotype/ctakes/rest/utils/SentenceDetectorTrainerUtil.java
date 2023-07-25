package com.text2phenotype.ctakes.rest.utils;

import opennlp.tools.cmdline.sentdetect.SentenceDetectorTrainerTool;
import opennlp.tools.sentdetect.*;
import opennlp.tools.util.*;
import opennlp.uima.sentdetect.SentenceDetectorTrainer;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class SentenceDetectorTrainerUtil {

    private static char[] eosChars = new char[] {'\n', '\r'};

    public static void main(String[] args) {
//        try {

            SentenceDetectorTrainerTool tool = new SentenceDetectorTrainerTool();
            tool.run(null, args);

//            String languageCode = "en";
//            if (args.length < 2) {
//                throw new AssertionError("Not all parameters are defined");
//            }
//
//            String outputModelFile = args[0];
//            String trainFile = args[1];
//
//            if (args.length > 2) {
//                languageCode = args[2];
//            }
//
//            File f = new File(trainFile);
//            InputStreamFactory isFactory = new MarkableFileInputStreamFactory(f);
//            ObjectStream<String> lineStream = new PlainTextByLineStream(isFactory, StandardCharsets.UTF_8);
//
//            SentenceModel model;
//
//            try (ObjectStream<SentenceSample> sampleStream = new SentenceSampleStream(lineStream)) {
//                SentenceDetectorFactory sdFactory = new SentenceDetectorFactory(languageCode, true, null, eosChars);
//                model = SentenceDetectorME.train(languageCode, sampleStream, sdFactory,TrainingParameters.defaultParams());
//            }
//
//            try (OutputStream modelOut = new BufferedOutputStream(new FileOutputStream(outputModelFile))) {
//                model.serialize(modelOut);
//            }
//        } catch (Exception e) {
//            System.out.print(e);
//        }
    }
}
