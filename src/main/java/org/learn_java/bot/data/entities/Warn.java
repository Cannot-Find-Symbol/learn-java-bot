package org.learn_java.bot.data.entities;

import java.time.LocalDate;
import javax.persistence.*;

@Entity
public class Warn {
  @Id @GeneratedValue private long id;
  private String username;
  private String reason;
  private LocalDate date;

  public Warn(String username, String reason, LocalDate date) {
    this.username = username;
    this.reason = reason;
    this.date = date;
  }

  public Warn() {}

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }
}
