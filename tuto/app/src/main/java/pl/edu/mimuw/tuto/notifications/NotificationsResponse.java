package pl.edu.mimuw.tuto.notifications;

import java.util.List;

public class NotificationsResponse {

  private long serverTime;
  private List<Notification> notifications;

  public NotificationsResponse(long serverTime, List<Notification> list) {
    this.serverTime = serverTime;
    this.notifications = list;
  }

  public long getServerTime() {
    return serverTime;
  }

  public List<Notification> getNotifications() {
    return notifications;
  }
}
