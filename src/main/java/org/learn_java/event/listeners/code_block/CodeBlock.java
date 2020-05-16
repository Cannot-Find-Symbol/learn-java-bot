package org.learn_java.event.listeners.code_block;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public class CodeBlock {

  private Message original;
  private Message reformatted;
  private final String messageId;
  private final User owner;

  public CodeBlock(Message original, Message reformatted, User owner) {
    this.original = original;
    this.reformatted = reformatted;
    this.messageId = reformatted.getId();
    this.owner = owner;
  }

  @Override
  public String toString() {
    return "CodeBlock{"
        + "original="
        + original.getId()
        + ", reformatted="
        + reformatted.getId()
        + '}';
  }

  public String getMessageId() {
    return messageId;
  }

  public void setOriginal(Message original) {
    this.original = original;
  }

  public void setReformatted(Message reformatted) {
    this.reformatted = reformatted;
  }

  public Message getOriginal() {
    return original;
  }

  public Message getReformatted() {
    return reformatted;
  }

  public User getOwner() {
    return owner;
  }
}
