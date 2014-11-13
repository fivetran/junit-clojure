Allows you to run clojure tests from JUnit like this:

```java
package com.fivetran.junit;

import com.fivetran.junit.TestNS;
import junit.framework.Test;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

@RunWith(AllTests.class)
public class TestRunner {
    public static Test suite() {
        return TestNS.suites("com.fivetran.junit.example-spec");
    }
}
```

You need to be sure your source and test directories are on the classpath so clojure can find them.
In maven, you do this:

```xml
<project>
    ...
    <build>
        ...
        <plugins>
            ...
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>2.18</version>
                <configuration>
                    <additionalClasspathElements>
                        <additionalClasspathElement>${basedir}/src</additionalClasspathElement>
                        <additionalClasspathElement>${basedir}/test</additionalClasspathElement>
                    </additionalClasspathElements>
                </configuration>
            </plugin>
            ...
        </plugins>
        ...
    </build>
    ...
</project>
```