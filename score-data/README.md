# ScoreData

## ScoreDataObjectProcessor

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
