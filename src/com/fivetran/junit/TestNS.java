package com.fivetran.junit;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import junit.framework.TestSuite;

public abstract class TestNS {
    public static TestSuite suites(String... namespaces) {
        TestSuite rootSuite = new TestSuite();

        for (String namespace : namespaces) {
            IFn require = Clojure.var("clojure.core", "require");
            require.invoke(Clojure.read("com.fivetran.junit.reporter"));
            require.invoke(Clojure.read(namespace));

            IFn testNs = Clojure.var("com.fivetran.junit.reporter", "test-ns");
            TestSuite nsSuite = (TestSuite) testNs.invoke(Clojure.read(namespace));

            rootSuite.addTest(nsSuite);
        }

        return rootSuite;
    }
}
