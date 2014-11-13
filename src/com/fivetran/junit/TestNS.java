package com.fivetran.junit;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import junit.framework.TestSuite;

public abstract class TestNS {
    public static TestSuite suite(String namespace) {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("com.fivetran.junit.reporter"));
        require.invoke(Clojure.read(namespace));

        IFn testNs = Clojure.var("com.fivetran.junit.reporter", "test-ns");
        TestSuite suite = (TestSuite) testNs.invoke(Clojure.read(namespace));

        return suite;
    }
}
