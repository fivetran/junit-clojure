package com.fivetran.junit;

import junit.framework.Test;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

@RunWith(AllTests.class)
public class ExampleSpec {
    public static Test suite() {
        return Runner.suite("com.fivetran.junit.example-spec");
    }
}
