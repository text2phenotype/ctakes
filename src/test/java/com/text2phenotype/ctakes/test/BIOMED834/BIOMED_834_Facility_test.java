package com.text2phenotype.ctakes.test.BIOMED834;

import com.text2phenotype.ctakes.rest.api.pipeline.ae.NPIAnnotator;
import com.text2phenotype.ctakes.rest.api.pipeline.annotations.NationalProviderMention;
import com.text2phenotype.ctakes.rest.api.pipeline.helpers.ServiceTypeSystemDescription;
import com.text2phenotype.ctakes.test.utils.JCasSerializer;
import com.text2phenotype.ctakes.test.utils.PipelineFactory;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class BIOMED_834_Facility_test {

    @Parameterized.Parameter()
    public int Idx;

    @Parameterized.Parameter(1)
    public String testName;

    @Parameterized.Parameter(2)
    public boolean isCorrect;

    @Parameterized.Parameters(name = "{index}: {1}")
    public static Iterable<Object[]> samples() throws Exception  {

        List<Object[]> result = new ArrayList<>();

        Path samplesPath = Paths.get(FileLocator.getFullPath("BIOMED-834/facility/samples.bsv"));
        Files.lines(samplesPath).forEach(line -> {
            if (!line.startsWith("//")) {
                String[] parts = line.split("\\|");
                int idx = Integer.parseInt(parts[0]);
                String testName = parts[1];
                boolean isCorrect = Boolean.parseBoolean(parts[3]);
                result.add(new Object[] {idx, testName, isCorrect});
            }
        });

        return result;
    }

    private static JCas JCAS;
    private static AnalysisEngine AE;

    @BeforeClass
    public static void prebuild() throws Exception {
        JCAS = JCasFactory.createJCas(ServiceTypeSystemDescription.createInstance());
        AE = PipelineFactory
                .create()
                .add(AnalysisEngineFactory.createEngineDescription(
                        NPIAnnotator.class,
                        "dict",
                        Paths.get(FileLocator.getFullPath("BIOMED-834/dict/npi.script")).getParent().resolve("npi").toString()
                ))
                .build();
    }

    @Test
    public void doTest() throws Exception {
        JCAS.reset();
        JCasSerializer.Load(JCAS, FileLocator.getFullPath("BIOMED-834/facility/dumps/" + Idx + ".xmi"));
        AE.process(JCAS);

        Collection<NationalProviderMention> mentions = JCasUtil.select(JCAS, NationalProviderMention.class);
        if (!isCorrect) {
            Assert.assertEquals(0, mentions.size());
        } else {
            Assert.assertEquals(1, mentions.size());

            NationalProviderMention mention = mentions.stream().findFirst().get();

            Assert.assertEquals("1326460981", mention.getCode());
        }
    }

}
