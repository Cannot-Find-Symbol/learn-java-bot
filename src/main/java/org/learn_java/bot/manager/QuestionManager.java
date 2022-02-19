package org.learn_java.bot.manager;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class QuestionManager {
	private final Set<Long> recentlyUsed = ConcurrentHashMap.newKeySet();
	private final ConcurrentHashMap<Long, Set<Long>> userThreadTracker = new ConcurrentHashMap<>();
	private final ScheduledExecutorService executorService;

	public QuestionManager(ScheduledExecutorService executorService) {
		this.executorService = executorService;
	}

	public void register(@NotNull Long userId, @NotNull Long threadId) {
		recentlyUsed.add(userId);
		userThreadTracker.putIfAbsent(userId, new HashSet<>());
		userThreadTracker.get(userId).add(threadId);
		scheduleClearTimeout(userId);
	}

	public boolean doesUserOwnThread(@NotNull Long userId, @NotNull Long threadId) {
		if(userThreadTracker.containsKey(userId)){
			return userThreadTracker.get(userId).contains(threadId);
		}
		return false;
	}

	public boolean contains(@NotNull Long userId) {
		return recentlyUsed.contains(userId);
	}

	@NotNull
	private ScheduledFuture<?> scheduleClearTimeout(@NotNull Long userId) {
		return executorService.schedule(() -> {
			recentlyUsed.remove(userId);
		}, 10, TimeUnit.SECONDS);
	}

}
