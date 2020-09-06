package org.learn_java.bot.data.entities;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Ban {
  @Id @GeneratedValue private long id;
  private final long user_id;
  private final String reason;
  private final boolean is_premanent;
  private LocalDateTime unban_date;

  public Ban(long id, String reason, LocalDateTime unban_date) {
    this.user_id = id;
    this.reason = reason;
    this.is_premanent = false;
    this.unban_date = unban_date;
  }

  public Ban(long id, String reason) {
    this.user_id = id;
    this.reason = reason;
    this.is_premanent = true;
  }

  public long getId() {
    return user_id;
  }

  public String getReason() {
    return reason;
  }

  public boolean isPremanent() {
    return is_premanent;
  }

  public LocalDateTime getDate() {
    return unban_date;
  }
}
