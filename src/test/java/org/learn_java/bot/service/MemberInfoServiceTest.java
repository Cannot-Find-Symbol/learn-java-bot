package org.learn_java.bot.service;

import org.junit.jupiter.api.Test;
import org.learn_java.bot.data.entities.MemberInfo;
import org.learn_java.bot.data.repositories.MemberInfoRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MemberInfoServiceTest {
    private final MemberInfoRepository repository = mock(MemberInfoRepository.class);
    private final MemberInfoService service = new MemberInfoService(repository);

    @Test
    void thankingExistingMemberIncrementsBothCounters() {
        var member = new MemberInfo(42L, 20, 3);
        when(repository.findById(42L)).thenReturn(Optional.of(member));

        var updated = service.updateThankCountForMember(42L);

        assertEquals(21, updated.getTotalThankCount());
        assertEquals(4, updated.getMonthThankCount());
        verify(repository, never()).save(any());
    }

    @Test
    void thankingNewMemberCreatesMemberAndCountsFirstThank() {
        when(repository.findById(42L)).thenReturn(Optional.empty());
        when(repository.save(any(MemberInfo.class))).thenAnswer(call -> call.getArgument(0));

        var updated = service.updateThankCountForMember(42L);

        assertEquals(42L, updated.getId());
        assertEquals(1, updated.getTotalThankCount());
        assertEquals(1, updated.getMonthThankCount());
        verify(repository).save(updated);
    }

    @Test
    void missingMemberHasSentinelCountsAndNoRecord() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertEquals(-1, service.getMonthThankCountByMemberId(42L));
        assertEquals(-1, service.getAllTimeThankCountByMemberId(42L));
        assertNull(service.findById(42L));
    }

    @Test
    void countQueriesReturnTheRequestedPeriod() {
        when(repository.findById(42L)).thenReturn(Optional.of(new MemberInfo(42L, 20, 3)));

        assertEquals(3, service.getMonthThankCountByMemberId(42L));
        assertEquals(20, service.getAllTimeThankCountByMemberId(42L));
    }

    @Test
    void leaderboardsRankByTheirOwnPeriodAndLimitToTen() {
        var members = LongStream.rangeClosed(1, 12)
                .mapToObj(id -> new MemberInfo(id, (int) id, 13 - (int) id))
                .toList();
        when(repository.findAll()).thenReturn(members);

        assertEquals(LongStream.rangeClosed(1, 10).boxed().toList(),
                service.findTop10ForMonth().stream().map(MemberInfo::getId).toList());
        assertEquals(LongStream.iterate(12, id -> id - 1).limit(10).boxed().toList(),
                service.findTop10AllTime().stream().map(MemberInfo::getId).toList());
    }

    @Test
    void emptyLeaderboardsReturnEmptyLists() {
        when(repository.findAll()).thenReturn(List.of());

        assertTrue(service.findTop10ForMonth().isEmpty());
        assertTrue(service.findTop10AllTime().isEmpty());
    }

    @Test
    void monthlyResetPreservesAllTimeCounts() {
        var first = new MemberInfo(1L, 20, 3);
        var second = new MemberInfo(2L, 8, 2);
        when(repository.findAll()).thenReturn(List.of(first, second));

        service.resetForMonth();

        assertAll(
                () -> assertEquals(0, first.getMonthThankCount()),
                () -> assertEquals(0, second.getMonthThankCount()),
                () -> assertEquals(20, first.getTotalThankCount()),
                () -> assertEquals(8, second.getTotalThankCount()));
    }
}
