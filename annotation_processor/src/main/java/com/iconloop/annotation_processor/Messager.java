package com.iconloop.annotation_processor;

import javax.tools.Diagnostic;

public class Messager {
    protected final javax.annotation.processing.Messager messager;
    protected final String prefix;

    public Messager(javax.annotation.processing.Messager messager, String prefix) {
        this.messager = messager;
        this.prefix = String.format("[%s]", prefix);
    }

    public void printMessage(Diagnostic.Kind kind, String format, Object... args) {
        messager.printMessage(
                kind, String.format(prefix + format, args));
    }

    public void noteMessage(String format, Object... args) {
        printMessage(Diagnostic.Kind.NOTE, format, args);
    }

    public void warningMessage(String format, Object... args) {
        printMessage(Diagnostic.Kind.WARNING, format, args);
    }

    public void mandatoryMessage(String format, Object... args) {
        printMessage(Diagnostic.Kind.MANDATORY_WARNING, format, args);
    }

    public void errorMessage(String format, Object... args) {
        printMessage(Diagnostic.Kind.ERROR, format, args);
    }

    public void otherMessage(String format, Object... args) {
        printMessage(Diagnostic.Kind.OTHER, format, args);
    }

}
