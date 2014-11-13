package com.fivetran.junit;

import junit.framework.Test;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

@RunWith(AllTests.class)
public class TestRunner {
    public static Test suite() {
        return TestNS.suites("com.fivetran.junit.example-spec");
    }
}
