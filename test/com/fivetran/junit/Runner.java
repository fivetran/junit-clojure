package com.fivetran.junit;

import clojure.lang.Keyword;
import com.google.common.collect.Lists;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import java.util.List;

@RunWith(AllTests.class)
public class Runner {
    private static Keyword FILE= Keyword.intern("file");
    private static Keyword LINE= Keyword.intern("line");

    public static Test suite() {
        TestSuite rootSuite = new TestSuite();
        List<String> namespaces = Lists.newArrayList("namespace");

        for (String ns : namespaces) {
            TestSuite nsSuite = new TestSuite(ns);
            List<String> vars = Lists.newArrayList("var");

            for (String var : vars) {
                TestSuite varSuite = new TestSuite(var);
//                Var testVar = RT.var(ns, var);
//                Map<Keyword, Object> meta = (Map<Keyword, Object>) Clojure.META.invoke(testVar);
//                String file = meta.get(FILE).toString();
//                String line = meta.get(LINE).toString();
//                Description description = Description.createSuiteDescription(var + "  <" + file + ":" + line + ">");

                varSuite.addTest(new TestCase("testing") {
                    @Override
                    protected void runTest() throws Throwable {

                    }
                });

                nsSuite.addTest(varSuite);
            }

            rootSuite.addTest(nsSuite);
        }

        return rootSuite;
    }
}
