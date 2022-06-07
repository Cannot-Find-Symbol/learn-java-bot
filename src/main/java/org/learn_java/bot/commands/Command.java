package org.learn_java.bot.commands;

// TODO add cooldown feature
public abstract class Command implements SlashCommand {
	private final String name;
	private final CommandType commandType;

	public Command(String name, CommandType commandType) {
		this.name = name;
		this.commandType = commandType;
	}

	@Override
	public String getName() {
		return name;
	}

	public CommandType getCommandType() {
		return commandType;
	}
}
