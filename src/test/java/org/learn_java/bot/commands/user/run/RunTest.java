package org.learn_java.bot.commands.user.run;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.junit.jupiter.api.Test;
import org.learn_java.ekmc.PistonService;
import org.learn_java.ekmc.model.Language;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RunTest {
    private final PistonService pistonService = mock(PistonService.class);
    private final Run command = new Run(pistonService);

    @Test
    void unavailableRuntimeLookupRepliesInsteadOfThrowing() throws IOException {
        @SuppressWarnings("unchecked")
        Call<List<Language>> call = mock(Call.class);
        var event = mock(MessageContextInteractionEvent.class, RETURNS_DEEP_STUBS);
        when(event.getName()).thenReturn("run");
        when(pistonService.getRuntimes()).thenReturn(call);
        when(call.execute()).thenThrow(new IOException("runtime service unavailable"));

        command.executeContextCommand(event);

        verify(event.getHook()).sendMessage("Sorry, the run service is unavailable right now. Please try again later.");
        verify(pistonService, never()).execute(any());
    }

    @Test
    void emptyRuntimeResponseRepliesInsteadOfThrowing() throws IOException {
        @SuppressWarnings("unchecked")
        Call<List<Language>> call = mock(Call.class);
        var event = mock(MessageContextInteractionEvent.class, RETURNS_DEEP_STUBS);
        when(event.getName()).thenReturn("run");
        when(pistonService.getRuntimes()).thenReturn(call);
        when(call.execute()).thenReturn(Response.success(null));

        command.executeContextCommand(event);

        verify(event.getHook()).sendMessage("Sorry, the run service is unavailable right now. Please try again later.");
        verify(pistonService, never()).execute(any());
    }

    @Test
    void runtimeLookupIsCachedAfterSuccess() throws IOException {
        @SuppressWarnings("unchecked")
        Call<List<Language>> call = mock(Call.class);
        when(pistonService.getRuntimes()).thenReturn(call);
        when(call.execute()).thenReturn(Response.success(List.of(language("java", "27", List.of("jdk")))));

        command.updateLanguages();
        command.updateLanguages();

        verify(pistonService, times(1)).getRuntimes();
        verify(call, times(1)).execute();
    }

    @Test
    void failedRuntimeLookupCanBeRetried() throws IOException {
        @SuppressWarnings("unchecked")
        Call<List<Language>> call = mock(Call.class);
        when(pistonService.getRuntimes()).thenReturn(call);
        when(call.execute()).thenThrow(new IOException("runtime service unavailable"))
                .thenReturn(Response.success(List.of(language("java", "27", List.of()))));

        command.updateLanguages();
        command.updateLanguages();
        command.updateLanguages();

        verify(pistonService, times(2)).getRuntimes();
        verify(call, times(2)).execute();
    }

    @Test
    void languageMapIncludesCanonicalNameAndAliases() {
        var java = language("java", "27", List.of("jdk", "Java"));
        var python = language("python", "3.14", List.of("py"));

        var runtimes = command.buildLanguageMap(List.of(java, python));

        assertSame(java, runtimes.get("java"));
        assertSame(java, runtimes.get("jdk"));
        assertSame(java, runtimes.get("Java"));
        assertSame(python, runtimes.get("py"));
        assertSame(python, runtimes.get("python"));
    }

    @Test
    void addsCommonImportsAndWrapsJavaSnippetInClassAndMain() {
        var result = command.addCommonImports("System.out.println(1);", language("java", "27", List.of()));

        assertTrue(result.startsWith("import java.util.*;"));
        assertTrue(result.contains("class Example {"));
        assertTrue(result.contains("public static void main(String[] args)"));
        assertTrue(result.contains("System.out.println(1);"));
    }

    @Test
    void preservesExistingClassAndMainMethod() {
        var code = "class Demo { public static void main(String[] args) {} }";

        assertEquals(code, command.addClass(code));
        assertEquals(code, command.addMainMethod(code));
    }

    @Test
    void nonJavaCodeIsReturnedWithoutJavaImportsOrWrapping() {
        var code = "print('hello')";

        assertEquals(code, command.addCommonImports(code, language("python", "3.14", List.of())));
    }

    @Test
    void existingJavaMainMethodIsWrappedOnlyInImports() {
        var code = "public class Demo { public static void main(String[] args) {} }";

        var result = command.addCommonImports(code, language("java", "27", List.of()));

        assertTrue(result.startsWith("import java.util.*;"));
        assertTrue(result.endsWith(code));
        assertEquals(1, result.split("public static void main", -1).length - 1);
    }

    private Language language(String name, String version, List<String> aliases) {
        var language = new Language();
        language.setLanguage(name);
        language.setVersion(version);
        language.setAliases(aliases);
        return language;
    }
}
