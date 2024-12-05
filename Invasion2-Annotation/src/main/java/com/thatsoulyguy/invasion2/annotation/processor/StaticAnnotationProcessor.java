package com.thatsoulyguy.invasion2.annotation.processor;

import com.thatsoulyguy.invasion2.annotation.Static;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_23)
@SupportedAnnotationTypes("com.thatsoulyguy.invasion2.annotation.Static")
public class StaticAnnotationProcessor extends AbstractProcessor
{
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment environment)
    {
        for (Element element : environment.getElementsAnnotatedWith(Static.class))
        {
            if (element.getKind() != ElementKind.CLASS)
            {
                processingEnv.getMessager().printMessage
                (
                    Diagnostic.Kind.ERROR,
                    "@Static can only be applied to classes",
                    element
                );

                continue;
            }

            TypeElement typeElement = (TypeElement) element;

            boolean hasNonStaticMethods = false;
            boolean hasNonPrivateConstructor = false;
            boolean hasNonStaticFields = false;

            for (Element enclosed : typeElement.getEnclosedElements())
            {
                if (enclosed.getKind() == ElementKind.FIELD)
                {
                    VariableElement field = (VariableElement) enclosed;

                    if (!field.getModifiers().contains(Modifier.STATIC))
                    {
                        hasNonStaticFields = true;

                        processingEnv.getMessager().printMessage
                        (
                            Diagnostic.Kind.ERROR,
                            "All fields in a @Static class must be static.",
                            field
                        );
                    }
                }

                if (enclosed.getKind() == ElementKind.METHOD)
                {
                    ExecutableElement method = (ExecutableElement) enclosed;

                    if (!method.getModifiers().contains(Modifier.STATIC))
                    {
                        hasNonStaticMethods = true;

                        processingEnv.getMessager().printMessage
                        (
                            Diagnostic.Kind.ERROR,
                            "All methods in a @Static class must be static.",
                            method
                        );
                    }
                }

                if (enclosed.getKind() == ElementKind.CONSTRUCTOR)
                {
                    ExecutableElement constructor = (ExecutableElement) enclosed;

                    if (!constructor.getModifiers().contains(Modifier.PRIVATE) || !constructor.getParameters().isEmpty())
                    {
                        hasNonPrivateConstructor = true;

                        processingEnv.getMessager().printMessage
                        (
                            Diagnostic.Kind.ERROR,
                            "A @Static class cannot have public or protected constructors with parameters.",
                            constructor
                        );
                    }
                }
            }

            if (hasNonStaticFields || hasNonStaticMethods || hasNonPrivateConstructor)
            {
                processingEnv.getMessager().printMessage
                (
                    Diagnostic.Kind.ERROR,
                    "The class does not conform to @Static class rules.",
                    typeElement
                );
            }
        }

        return true;
    }
}