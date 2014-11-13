package com.fivetran.junit;

@org.junit.runner.RunWith(org.junit.runners.AllTests.class)
public class TestRunner {
    public static junit.framework.Test suite() {
        return TestNS.suites("com.fivetran.junit.example-spec");
    }
}
