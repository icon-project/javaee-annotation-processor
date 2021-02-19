# Annotation-processor for JAVA SCORE

## InterfaceScoreProcessor

### Getting Started
Add dependency
````
# case of build.gradle

dependencies {
    ...
    
    compileOnly project(':annotation_processor')
    annotationProcessor project(':annotation_processor')
}
````

Create `interface` with `@InterfaceScore`.  
When compile java sources, `XXXInterfaceImpl` class will be generated.
````java
import foundation.icon.ee.annotation_processor.InterfaceScore;
import score.annotation.Payable;

@InterfaceScore
public interface XXXInterface {
    void externalMethod(String param, ...);
    @Payable void payableMethod(String param, ...);
    ...
}
````

Using generated class
````java
XXXInterfaceImpl score = new XXXInterfaceImpl(Address.fromString("cx..."));
score.externalMethod(param, ...);
# intercall payable method
score._setICX(value).externalMethod(param, ...);
````
