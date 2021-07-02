package com.iconloop.annotation_processor;

import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;

public class AnnotatedTypeElement<T extends Annotation> {
    private TypeElement element;
    private T annotation;

    public AnnotatedTypeElement(TypeElement element, T annotation) {
        if (element == null) {
            throw new IllegalArgumentException("element cannot be null");
        }
        if (annotation == null) {
            throw new IllegalArgumentException("annotation cannot be null");
        }
        this.element = element;
        this.annotation = annotation;
    }

    public TypeElement getElement() {
        return element;
    }

    public T getAnnotation() {
        return annotation;
    }
}
