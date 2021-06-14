# Annotation-processor for JAVA SCORE

## ScoreInterfaceProcessor

### Gradle
Add dependency to build.gradle
````
dependencies {
    compileOnly 'foundation.icon:javaee-api:0.8.9'
    
    compileOnly 'com.iconloop.score:javaee-annotation-processor:0.1.0-SNAPSHOT'
    annotationProcessor 'com.iconloop.score:javaee-annotation-processor:0.1.0-SNAPSHOT'
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

### JsonObjectProcessor

### Gradle
Add dependency to build.gradle
````
dependencies {
    compileOnly 'foundation.icon:javaee-api:0.8.7'
    implementation 'com.github.sink772:javaee-scorex:0.5.1'
    implementation 'com.github.sink772:minimal-json:0.9.6'
    
    compileOnly 'com.iconloop.score:javaee-annotation-processor:0.1.0-SNAPSHOT'
    annotationProcessor 'com.iconloop.score:javaee-annotation-processor:0.1.0-SNAPSHOT'
}
````

### Usage
Annotate `@JsonObject` to class. also you can annotate `@JsonProperty` to field.
````java
@JsonObject
public class Xxx {
    private String value;
    
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
````

When java compile, JSON convertable class will be generated which has `@JsonObject.suffix()`.  
Then you can use generated class as follows.
````java
import score.annotation.External;
import com.eclipsesource.json.JsonObject;

public class Score {
    @External(readonly = true)
    public String json(String jsonString) {
        // parse
        Xxx xxx = XxxJson.parse(jsonString);
        // toJsonObject
        JsonObject jsonObject = XxxJson.toJsonObject(xxx);
        // toJsonString 
        return jsonObject.toString();
    }
}
````

### ScoreDataObjectProcessor

### Gradle
Add dependency to build.gradle
````
dependencies {
    compileOnly 'foundation.icon:javaee-api:0.8.7'
    implementation 'com.github.sink772:javaee-scorex:0.5.1'
    
    compileOnly 'com.iconloop.score:javaee-annotation-processor:0.1.0-SNAPSHOT'
    annotationProcessor 'com.iconloop.score:javaee-annotation-processor:0.1.0-SNAPSHOT'
}
````

### Usage
Annotate `@ScoreDataObject` to class. also you can annotate `@ScoreDataProperty` to field.
````java
@ScoreDataObject
public class Xxx {
    private String value;
    
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
````

When java compile, serializable class will be generated which has `@ScoreDataObject.suffix()`.
Then you can use generated class as follows.
````java
import score.annotation.External;

public class Score {
    final VarDB<XxxSdo> db = Context.newVarDB("db", XxxSdo.class);
    
    @External
    public void set(Xxx xxx) {
        db.set(new XxxSdo(xxx));
    }

    @External(readonly = true)
    public Xxx get() {
        return db.get();
    }
}
````

### ScorePropertiesDBObjectProcessor

### Gradle
Add dependency to build.gradle
````
dependencies {
    compileOnly 'foundation.icon:javaee-api:0.8.7'
    implementation 'com.github.sink772:javaee-scorex:0.5.1'
    implementation 'foundation.icon:javaee-score-lib:0.1.0-SNAPSHOT'
    
    compileOnly 'com.iconloop.score:javaee-annotation-processor:0.1.0-SNAPSHOT'
    annotationProcessor 'com.iconloop.score:javaee-annotation-processor:0.1.0-SNAPSHOT'
}
````

### Usage
Annotate `@ScorePropertiesDBObject` to class. also you can annotate `@ScorePropertiesDBProperty` to field.
````java
@ScorePropertiesDBObject
public class Xxx {
    private String value;
    
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
````

When java compile, implement class of `PropertyDB` interface  will be generated which has `@ScorePropertiesDBObject.suffix()`.
Then you can use generated class as follows.
````java
import score.annotation.External;

public class Score {
    final XxxSpo xxx = new XxxSpo();
    
    public Score() {
        xxx.initialize("xxx");
    }
    
    @External
    public void set(Xxx xxx) {
        db.set(new XxxSdo(xxx));
    }

    @External(readonly = true)
    public Xxx get() {
        return db.get();
    }
}
````
