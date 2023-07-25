package com.text2phenotype.ctakes.rest.api.pipeline.ae;

import com.text2phenotype.ctakes.rest.api.pipeline.annotations.PhoneNumberMention;
import net.openai.util.fsm.Machine;
import org.apache.ctakes.core.pipeline.PipeBitInfo;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PipeBitInfo(
        name = "Phone number Annotator",
        description = "Annotates phones/fax numbers",
        dependencies = { PipeBitInfo.TypeProduct.SENTENCE, PipeBitInfo.TypeProduct.BASE_TOKEN },
        products = { PipeBitInfo.TypeProduct.IDENTIFIED_ANNOTATION }
)
public class PhonesAnnotator extends JCasAnnotator_ImplBase {

    private final Pattern PHONES_PATERN = Pattern.compile("(?<=(\\s)|(^))\\+?\\d?[ -]*\\(?\\d{3}\\)?[- ]*\\d{2,3}[- ]*\\d{4}(?=(\\s)|($))");
    private Machine machine;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        Collection<Sentence> sents = JCasUtil.select(aJCas, Sentence.class);
        for (Sentence sent : sents) {
            String txt = sent.getCoveredText();
            Matcher mtcher = PHONES_PATERN.matcher(txt);
            while (mtcher.find()){
                StringBuilder numSeq = new StringBuilder();
                char[] chars = txt.toCharArray();
                int cnt = mtcher.end();
                while ((numSeq.length() < 10) && (cnt > mtcher.start())) {
                    if (Character.isDigit(chars[cnt - 1])) {
                        numSeq.append(chars[cnt - 1]);
                    }
                    cnt--;
                }
                if (numSeq.length() > 0) {
                    PhoneNumberMention mention = new PhoneNumberMention(
                            aJCas,
                            mtcher.start() + sent.getBegin(),
                            mtcher.end() + sent.getBegin()
                    );

                    mention.setNumber(Long.parseLong(numSeq.reverse().toString()));

                    mention.addToIndexes();
                }



            }
        }
    }
}
