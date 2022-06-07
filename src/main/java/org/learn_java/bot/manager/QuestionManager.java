package org.learn_java.bot.manager;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class QuestionManager {
	private final Set<Long> recentlyUsed = ConcurrentHashMap.newKeySet();
	// TODO probably don't need a list of threads, just 1 per user per 10 minutes, after that no editing.
	private final ConcurrentHashMap<Long, Set<Long>> userThreadTracker = new ConcurrentHashMap<>();
	private final ScheduledExecutorService executorService;

	public QuestionManager(ScheduledExecutorService executorService) {
		this.executorService = executorService;
	}

	public void register(@NotNull Long userId, @NotNull Long threadId) {
		recentlyUsed.add(userId);
		userThreadTracker.putIfAbsent(userId, new HashSet<>());
		userThreadTracker.get(userId).add(threadId);
		scheduleClearJobs(userId, threadId);
	}

	public boolean doesUserOwnThread(@NotNull Long userId, @NotNull Long threadId) {
		if (userThreadTracker.containsKey(userId)) {
			return userThreadTracker.get(userId).contains(threadId);
		}
		return false;
	}

	public boolean contains(@NotNull Long userId) {
		return recentlyUsed.contains(userId);
	}

	private void scheduleClearJobs(@NotNull Long userId, @NotNull Long threadId) {
		executorService.schedule(() -> {
			recentlyUsed.remove(userId);
		}, 5, TimeUnit.MINUTES);
		executorService.schedule(() -> {
			userThreadTracker.get(userId).remove(threadId);
		}, 1, TimeUnit.HOURS);
	}

}
