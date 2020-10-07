package org.learn_java.bot.data.entities;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Warn {
  @Id @GeneratedValue private long id;
  private long userID;
  private String reason;
  private LocalDateTime date;

  protected Warn() {}

  public Warn(long userId, String reason, LocalDateTime date) {
    this.userID = userId;
    this.reason = reason;
    this.date = date;
  }

  public long getUserID() {
    return userID;
  }

  public String getReason() {
    return reason;
  }

  public LocalDateTime getDate() {
    return date;
  }
}
