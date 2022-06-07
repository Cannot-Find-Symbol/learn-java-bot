package org.learn_java.bot.commands.delay;

import org.jetbrains.annotations.NotNull;
import org.learn_java.bot.commands.SlashCommand;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class DelayHandler {
	private final Map<Long, Map<String, List<OffsetDateTime>>> tracker;

	public DelayHandler() {
		this.tracker = new ConcurrentHashMap<>();
	}

	public void trackUse(Long memberId, String commandName, OffsetDateTime timeUsed) {
		var commandMap = tracker.computeIfAbsent(memberId, (k) -> new HashMap<>());
		commandMap.computeIfAbsent(commandName, (k) -> new ArrayList<>()).add(timeUsed);
		purgeOldCommands();
	}

	private void purgeOldCommands() {
		LocalDateTime now = LocalDateTime.now();
		tracker.forEach((k, commandMap) -> removeOldTimes(now, commandMap));
	}

	private void removeOldTimes(LocalDateTime now, Map<String, List<OffsetDateTime>> v) {
		v.values().removeIf(times -> areAllExpired(now, times));
	}

	private boolean areAllExpired(LocalDateTime now, List<OffsetDateTime> times) {
		return times.stream()
				.allMatch(time -> isExpired(now, time));
	}

	private boolean isExpired(LocalDateTime now, OffsetDateTime time) {
		return Duration.between(time, now.atOffset(time.getOffset())).toMinutes() >= 5;
	}

	public int secondsUntilNextUse(Long memberId, SlashCommand command) {
		if(!tracker.containsKey(memberId)) return 0;
		String commandName = command.getName();
		var memberCommandTimes = tracker.get(memberId);
		if(!memberCommandTimes.containsKey(commandName) || memberCommandTimes.get(commandName).size() < command.getQuickLimit()) return 0;

		Duration duration = timeBetweenLastUse(commandName, memberCommandTimes);

		if (duration.getSeconds() <= command.getDelay()) {
			return (int) Math.ceil((command.getDelay() - duration.getSeconds()));
		}

		return 0;
	}

	private Duration timeBetweenLastUse(String commandName, Map<String, List<OffsetDateTime>> memberCommandTimes) {
		OffsetDateTime lastUse = getLastUse(memberCommandTimes.get(commandName));
		OffsetDateTime currentTime = OffsetDateTime.now(lastUse.getOffset());
		return Duration.between(lastUse, currentTime);
	}

	@NotNull
	private OffsetDateTime getLastUse(List<OffsetDateTime> offsetDateTimes) {
		return offsetDateTimes.stream().max(Comparator.naturalOrder()).orElseThrow();
	}
}
