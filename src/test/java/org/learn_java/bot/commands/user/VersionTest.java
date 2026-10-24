package org.learn_java.bot.commands.user;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VersionTest {
    private final BuildProperties buildProperties = mock(BuildProperties.class);
    private final GitProperties gitProperties = mock(GitProperties.class);
    private final Version command = new Version(buildProperties, gitProperties);
    private final SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, RETURNS_DEEP_STUBS);

    @Test
    void repliesWithReleaseTagAndCommit() {
        when(buildProperties.getVersion()).thenReturn("1.1.0");
        when(gitProperties.getShortCommitId()).thenReturn("abc1234");

        command.executeSlash(event);

        verify(event).reply("Version: `v1.1.0`\nCommit: `abc1234`");
    }

    @Test
    void labelsLocalBuildAsSnapshot() {
        when(buildProperties.getVersion()).thenReturn("0.0.0-SNAPSHOT");
        when(gitProperties.getShortCommitId()).thenReturn("abc1234");

        command.executeSlash(event);

        verify(event).reply("Version: `0.0.0-SNAPSHOT`\nCommit: `abc1234`");
    }
}
