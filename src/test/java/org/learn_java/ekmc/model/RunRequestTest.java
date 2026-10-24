package org.learn_java.ekmc.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RunRequestTest {
    @Test
    void constructorUsesLanguageRuntimeAndCreatesOneSourceFile() {
        var language = new Language();
        language.setLanguage("java");
        language.setVersion("27.0.1");

        var request = new RunRequest("System.out.println(1);", language);

        assertEquals("java", request.getLanguage());
        assertEquals("27.0.1", request.getVersion());
        assertEquals(1, request.getFiles().size());
        assertEquals("System.out.println(1);", request.getFiles().getFirst().getContent());
    }

    @Test
    void defaultConstructorUsesDefaultRuntime() {
        var request = new RunRequest();

        assertEquals("java", request.getLanguage());
        assertEquals("15.0.2", request.getVersion());
        assertTrue(request.getFiles().isEmpty());
    }
}
