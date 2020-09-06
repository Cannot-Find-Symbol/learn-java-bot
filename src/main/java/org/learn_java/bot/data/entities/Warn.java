package org.learn_java.bot.data.entities;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Warn {
  @Id @GeneratedValue private long id;
  private long user_id;
  private final String reason;
  private final LocalDateTime date;

  public Warn(long username, String reason, LocalDateTime date) {
    this.id = username;
    this.reason = reason;
    this.date = date;
  }

  public long getId() {
    return user_id;
  }

  public String getReason() {
    return reason;
  }

  public LocalDateTime getDate() {
    return date;
  }
}
