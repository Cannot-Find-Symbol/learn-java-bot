package org.learn_java.bot.data.entities;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Ban {
  @Id @GeneratedValue private long id;
  private long userID;
  private String reason;
  private boolean isPermanent;
  private LocalDateTime unbanDate;

  protected Ban() {}

  public Ban(long id, String reason, LocalDateTime unban_date) {
    this.userID = id;
    this.reason = reason;
    this.isPermanent = false;
    this.unbanDate = unban_date;
  }

  public Ban(long id, String reason) {
    this.userID = id;
    this.reason = reason;
    this.isPermanent = true;
  }

  public long getUserID() {
    return userID;
  }

  public String getReason() {
    return reason;
  }

  public LocalDateTime getDate() {
    return unbanDate;
  }

  public boolean isPermanent() {
    return isPermanent;
  }
}
