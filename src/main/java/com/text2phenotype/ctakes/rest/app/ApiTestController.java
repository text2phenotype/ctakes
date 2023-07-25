package com.text2phenotype.ctakes.rest.app;

import com.text2phenotype.ctakes.rest.api.pipeline.controllers.Pipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Controller for test API page
 */
@Controller
public class ApiTestController {

    private String pipelinesCtxPath = "rest";

    public String getPipelinesCtxPath() {
        return pipelinesCtxPath;
    }

    public void setPipelinesCtxPath(String pipelinesCtxPath) {
        this.pipelinesCtxPath = pipelinesCtxPath;
    }

    @Value("#{endpoints}")
    private Map<String, String> endpointsData;

    @Autowired
    private List<Pipeline> pipelineDescriptions;

    @RequestMapping(method = RequestMethod.GET,path = "apiTest.jsp")
    public String getPage(Map<String, Object> model) {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = sra.getRequest();

        model.put("baseUrl", String.format("/%s/%s", req.getContextPath(), pipelinesCtxPath).replaceAll("//", "/"));

        Map<String, List<String>> endpointsList = new TreeMap<>();
        if (pipelineDescriptions != null && pipelineDescriptions.size() > 0) {

            for (Pipeline pipeline : pipelineDescriptions) {
                String name = pipeline.getName();
                if (endpointsData != null && endpointsData.containsKey(name)) {
                    String pipelineName = endpointsData.get(name);
                    List<String> values= endpointsList.getOrDefault(pipelineName, new ArrayList<>());
                    values.add(String.format("/%s/%s", pipeline.getDict(), pipeline.getName()));
                    endpointsList.put(pipelineName, values);
                }
            }
        }

        model.put("endpoints", endpointsList);

        return "apiTest";

    }
}
