package com.fivetran.junit;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

public abstract class Runner {
    public static Test suite(String namespace) {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("com.fivetran.junit.reporter"));
        require.invoke(Clojure.read(namespace));

        IFn testNs = Clojure.var("com.fivetran.junit.reporter", "test-ns");
        TestCase[] tests = (TestCase[]) testNs.invoke(Clojure.read(namespace));

        TestSuite suite = new TestSuite("namespace");

        for (TestCase test : tests)
            suite.addTest(test);

        return suite;
    }
}
