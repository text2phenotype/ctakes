package com.text2phenotype.ctakes.rest.api.pipeline.helpers.mb;

import com.text2phenotype.ctakes.rest.api.pipeline.model.response.ResponseData;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public abstract class ResponseModelBuilder_Impl<T extends ResponseData> implements ResponseModelBuilder<T>, ApplicationContextAware {

    protected T resultModel;

    private final Class<T> clazz;

    public ResponseModelBuilder_Impl(Class<T> clazz){
        this.clazz = clazz;
    }

    @Override
    public void initRequestModel() {

        this.resultModel = beanFactory.getBean(clazz);
    }

    public void setResultModel(T model) {
        resultModel = model;
    }

    private BeanFactory beanFactory;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.beanFactory = applicationContext.getAutowireCapableBeanFactory();
    }
}
