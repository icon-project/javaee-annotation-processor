# ScoreClient

## ScoreInterfaceProcessor

### Gradle
Add dependency to build.gradle
````
dependencies {
    compileOnly 'foundation.icon:javaee-api:0.8.9'
    
    annotationProcessor 'com.iconloop.score:javaee-score-client:0.1.0-SNAPSHOT'
    implementation 'com.iconloop.score:javaee-score-client:0.1.0-SNAPSHOT'
}
````

### Usage
Annotate `@ScoreInterface` to interface. and annotate `@score.annotation.Payable` to payable method.
````java
@ScoreInterface
public interface Xxx {
    void externalMethod(String param);
    String readOnlyMethod(String param);
    @score.annotation.Payable void payableMethod(String param);
}
````

When java compile, implement class will be generated which has `@ScoreInterface.suffix()`.
Then you can use generated class as follows.

For payable method, overload method will be generated with the first parameter as `BigInteger valueForPayable`

````java
import score.Address;
import score.annotation.External;

import java.math.BigInteger;

public class Score {
    @External
    public void intercallExternal(Address address, String param) {
        XxxScoreInterface xxx = new XxxScoreInterface(address);
        xxx.externalMethod(param);
    }

    @External(readonly = true)
    public String intercallReadOnly(Address address, String param) {
        XxxScoreInterface xxx = new XxxScoreInterface(address);
        return xxx.readOnlyMethod(param);
    }

    @External
    public void intercallPayable(Address address, BigInteger valueForPayable, String param) {
        XxxScoreInterface xxx = new XxxScoreInterface(address);
        xxx.payableMethod(valueForPayable, param);
    }
}
````

## ScoreClientProcessor

### Gradle
Add dependency to build.gradle
````
dependencies {
    compileOnly 'foundation.icon:javaee-api:0.8.9'
    
    annotationProcessor 'com.iconloop.score:javaee-score-client:0.1.0-SNAPSHOT'
    implementation 'com.iconloop.score:javaee-score-client:0.1.0-SNAPSHOT'
    implementation 'foundation.icon:icon-sdk:2.0.0'
}
````

### Usage
`@ScoreClient` could annotate to interface or field.
For example, annotate `@ScoreClient` to interface.

````java
@ScoreClient
public interface Xxx {    
    void externalMethod(String param);
    
    String readOnlyMethod(String param);
    
    @score.annotation.Payable
    void payableMethod(String param);
}
````

When java compile, implement class will be generated which has `@ScoreClient.suffix()`.
Then you can use generated class as follows.

For payable method, overload method will be generated with the first parameter as `BigInteger valueForPayable`

````java
import com.iconloop.score.client.DefaultScoreClient;

import java.math.BigInteger;

public class Application {

    public static void main(String[] args) {
        Xxx xxx;
        try{
            xxx = new XxxScoreClient(new DefaultScoreClient(
                    "http://HOST:PORT/api/v3",
                    "NID",
                    "PASSWORD_OF_KEYSTORE", 
                    "/PATH/TO/KEYSTORE",
                    "cx..."));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        //external call
        xxx.externalMethod("PARAM");
        
        //call
        String result = xxx.readOnlyMethod("PARAM");
        
        //payable
        xxx.payableMethod(BigInteger.ONE, "PARAM");
    }    
}
````

## For integration test in SCORE development project

### Gradle

#### Dependencies
````
dependencies {
    ...
    
    testAnnotationProcessor 'com.iconloop.score:javaee-score-client:0.1.0-SNAPSHOT'
    testImplementation 'com.iconloop.score:javaee-score-client:0.1.0-SNAPSHOT'
    testImplementation 'foundation.icon:icon-sdk:2.0.0'
    testCompileOnly 'foundation.icon:javaee-api:0.8.9'
}
````

#### Set system properties of test environment
for initialize via `DefaultScoreClient.of(System.getProperties())`

````
test {
    useJUnitPlatform()
    options {
        systemProperty 'url', 'http://HOST:PORT/api/v3'
        systemProperty 'nid', 'NID'
        systemProperty 'keyStorePath', '/PATH/TO/KEYSTORE'
        systemProperty 'keyStorePass', 'PASSWORD_OF_KEYSTORE'
        //for exists contract
        systemProperty 'address', 'cx...'
        //for deploy
        systemProperty 'scoreFilePath', '/PATH/TO/SCORE_FILE'
        systemProperty 'params.'+'PARAM_NAME', 'PARAM_VALUE'
        systemProperty 'params.'+'PARAM_NAME2', 'PARAM_VALUE2'
        ...
    }
}
````

if foundation.icon:gradle-javaee-plugin applied
````
test {
    useJUnitPlatform()
    options {
        //if foundation.icon:gradle-javaee-plugin applied
        dependsOn optimizedJar
        systemProperty 'url', project.tasks.deployToLocal.uri.get()
        systemProperty 'nid', project.tasks.deployToLocal.nid.get()
        systemProperty 'keyStorePath', project.extensions.deployJar.keystore.get()
        systemProperty 'keyStorePass', project.extensions.deployJar.password.get()
        //for exists contract
        systemProperty 'address', 'cx...'
        //for deploy
        systemProperty 'scoreFilePath', tasks.optimizedJar.outputJarName
        project.extensions.deployJar.arguments.each {
            arg -> systemProperty 'params.'+arg.name, arg.value
        }
    }
}
````

### Usage
`@ScoreClient` could annotate to interface or field.
For example, annotate `@ScoreClient` to field.

Xxx interface in src/main/java/...
````java
public interface Xxx {
    void externalMethod(String param);
    String readOnlyMethod(String param);
    @score.annotation.Payable void payableMethod(String param);
}
````

XxxTest class in src/test/java/...
````java
import score.Address;
import score.annotation.External;

import java.math.BigInteger;

public class XxxTest {
    
    @ScoreClient
    static Xxx xxx;

    @BeforeAll
    static void beforeAll() {
        xxx = XxxScoreClient._of(System.getProperties());
    }

    @Test
    void test() {
        xxx.externalMethod("PARAM");
        
        xxx.readOnlyMethod("PARAM");
        
        ((XxxScoreClient)xxx).payableMethod(BigInteger.ONE, "PARAM");
    }
}
````