package com.text2phenotype.ctakes.rest.api.pipeline.helpers;

import gov.nih.nlm.nls.lvg.Util.Str;
import org.apache.ctakes.core.pipeline.CliOptionals;

import java.util.HashMap;
import java.util.Map;

/**
 * Cli option for pipeline controller configuration
 */
public class Text2phenotypeCliOptionals implements CliOptionals {

    private Map<String, Object> params;

    public void setParams(Map<String, String> params) {
        this.params = new HashMap<>();
        for (String paramName: params.keySet()) {
            String paramValue = params.get(paramName);
            try {
                this.params.put(paramName, Integer.parseInt(paramValue));
            } catch (NumberFormatException nfe) {
                this.params.put(paramName, paramValue);
            }
        }

    }

    private Object getParamByName(String name){
        if (params != null && params.containsKey(name))
            return params.get(name);

        return null;
    }
    
    private String getStringParameter(String name) {
        return getParamByName(name).toString();
    }

    public Map<String, Object> getParams() {
        return params;
    }

    @Override
    public String getOption_a() { return getStringParameter("a"); }

    @Override
    public String getOption_b() { return getStringParameter("b"); }

    @Override
    public String getOption_c() { return getStringParameter("c"); }

    @Override
    public String getOption_d() { return getStringParameter("d"); }

    @Override
    public String getOption_e() { return getStringParameter("e"); }

    @Override
    public String getOption_f() { return getStringParameter("f"); }

    @Override
    public String getOption_g() { return getStringParameter("g"); }

    @Override
    public String getOption_h() { return getStringParameter("h"); }

    @Override
    public String getOption_j() { return getStringParameter("j"); }

    @Override
    public String getOption_k() { return getStringParameter("k"); }

    @Override
    public String getOption_m() { return getStringParameter("m"); }

    @Override
    public String getOption_n() { return getStringParameter("n"); }

    @Override
    public String getOption_q() { return getStringParameter("q"); }

    @Override
    public String getOption_r() { return getStringParameter("r"); }

    @Override
    public String getOption_t() { return getStringParameter("t"); }

    @Override
    public String getOption_u() { return getStringParameter("u"); }

    @Override
    public String getOption_v() { return getStringParameter("v"); }

    @Override
    public String getOption_w() { return getStringParameter("w"); }

    @Override
    public String getOption_x() { return getStringParameter("x"); }

    @Override
    public String getOption_y() { return getStringParameter("y"); }

    @Override
    public String getOption_z() { return getStringParameter("z"); }

    @Override
    public String getOption_0() { return getStringParameter("0"); }

    @Override
    public String getOption_1() { return getStringParameter("1"); }

    @Override
    public String getOption_2() { return getStringParameter("2"); }

    @Override
    public String getOption_3() { return getStringParameter("3"); }

    @Override
    public String getOption_4() { return getStringParameter("4"); }

    @Override
    public String getOption_5() { return getStringParameter("5"); }

    @Override
    public String getOption_6() { return getStringParameter("6"); }

    @Override
    public String getOption_7() { return getStringParameter("7"); }

    @Override
    public String getOption_8() { return getStringParameter("8"); }

    @Override
    public String getOption_9() { return getStringParameter("9"); }

    @Override
    public String getOption_A() { return getStringParameter("A"); }

    @Override
    public String getOption_B() { return getStringParameter("B"); }

    @Override
    public String getOption_C() { return getStringParameter("C"); }

    @Override
    public String getOption_D() { return getStringParameter("D"); }

    @Override
    public String getOption_E() { return getStringParameter("E"); }

    @Override
    public String getOption_F() { return getStringParameter("F"); }

    @Override
    public String getOption_G() { return getStringParameter("G"); }

    @Override
    public String getOption_H() { return getStringParameter("H"); }

    @Override
    public String getOption_J() { return getStringParameter("J"); }

    @Override
    public String getOption_K() { return getStringParameter("K"); }

    @Override
    public String getOption_M() { return getStringParameter("M"); }

    @Override
    public String getOption_N() { return getStringParameter("N"); }

    @Override
    public String getOption_Q() { return getStringParameter("Q"); }

    @Override
    public String getOption_R() { return getStringParameter("R"); }

    @Override
    public String getOption_T() { return getStringParameter("T"); }

    @Override
    public String getOption_U() { return getStringParameter("U"); }

    @Override
    public String getOption_V() { return getStringParameter("V"); }

    @Override
    public String getOption_W() { return getStringParameter("W"); }

    @Override
    public String getOption_X() { return getStringParameter("X"); }

    @Override
    public String getOption_Y() { return getStringParameter("Y"); }

    @Override
    public String getOption_Z() { return getStringParameter("Z"); }

    @Override
    public String getPiperPath() {
        return getStringParameter("p");
    }

    @Override
    public String getInputDirectory() {
        return getStringParameter("i");
    }

    @Override
    public String getOutputDirectory() {
        return getStringParameter("o");
    }

    @Override
    public String getSubDirectory() {
        return getStringParameter("s");
    }

    @Override
    public String getXmiOutDirectory() {
        return getStringParameter("xmiOut");
    }

    @Override
    public String getLookupXml() {
        return getStringParameter("l");
    }

    @Override
    public String getUmlsUserName() {
        return getStringParameter("user");
    }

    @Override
    public String getUmlsPassword() {
        return getStringParameter("pass");
    }

    @Override
    public boolean isHelpWanted() {
        return false;
    }
}
