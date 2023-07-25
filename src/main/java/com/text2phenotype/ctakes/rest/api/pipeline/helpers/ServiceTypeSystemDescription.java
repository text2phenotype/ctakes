package com.text2phenotype.ctakes.rest.api.pipeline.helpers;


import com.text2phenotype.ctakes.rest.api.pipeline.annotations.ActivityMention;
import com.text2phenotype.ctakes.rest.api.pipeline.annotations.UnitAnnotation;
import com.text2phenotype.ctakes.rest.api.pipeline.annotations.UnitRelation;
import com.text2phenotype.ctakes.rest.api.pipeline.annotations.SpecialLabValueWord;
import com.text2phenotype.ctakes.rest.api.pipeline.annotations.token.UnitToken;
import org.apache.ctakes.typesystem.type.relation.BinaryTextRelation;
import org.apache.ctakes.typesystem.type.syntax.WordToken;
import org.apache.ctakes.typesystem.type.textsem.EventMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Type system of the service
 */
@Component
public class ServiceTypeSystemDescription {

    @Bean(autowire = Autowire.BY_TYPE)
    public static TypeSystemDescription createInstance() throws ResourceInitializationException, ClassNotFoundException {
        TypeSystemDescription result = TypeSystemDescriptionFactory.createTypeSystemDescription();

        Class.forName(UnitAnnotation.class.getName());

        result.addType(UnitAnnotation.class.getName(), "UnitAnnotation", IdentifiedAnnotation.class.getName());
        result.addType(UnitRelation.class.getName(), "UnitRelation", BinaryTextRelation.class.getName());
        result.addType(UnitToken.class.getName(), "UnitToken", WordToken.class.getName());
        result.addType(SpecialLabValueWord.class.getName(), "SpecialLabValueWord", IdentifiedAnnotation.class.getName());

        result.addType(ActivityMention.class.getName(), "ActivityMention", EventMention.class.getName());
        return result;
    }
}
