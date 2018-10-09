package pl.edu.mimuw.tuto.notifications;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Notification implements Parcelable {
  public final static String TITLE_KEY = "title";
  public final static String CONTENTS_KEY = "contents";
  public final static String SERVER_TIME_KEY = "server_time";

  private String title;
  private String contents;
  private long serverTime;

  protected Notification(String title, String contents, long serverTime) {
    this.title = title;
    this.contents = contents;
    this.serverTime = serverTime;
  }

  public String getTitle() {
    return title;
  }

  public String getContents() {
    return contents;
  }

  public long getServerTime() {
    return serverTime;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Notification fromJSONObject(JSONObject jsonObject) throws JSONException {
    return newBuilder()
        .setTitle(jsonObject.getString(TITLE_KEY))
        .setContents(jsonObject.getString(CONTENTS_KEY))
        .setServerTime(jsonObject.getLong(SERVER_TIME_KEY))
        .build();
  }

  public static Notification[] fromJSONArray(JSONArray jsonArray) throws JSONException {
    Notification[] notifications = new Notification[jsonArray.length()];
    for (int i = 0; i < jsonArray.length(); ++i) {
      notifications[i] = fromJSONObject(jsonArray.getJSONObject(i));
    }
    return notifications;
  }

  /**
   * Parcelable part - used to save Tag instances.
   */
  private Notification(Parcel in) {
    this.title = in.readString();
    this.contents = in.readString();
    this.serverTime = in.readLong();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.title);
    dest.writeString(this.contents);
    dest.writeLong(this.serverTime);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
    public Notification createFromParcel(Parcel in) {
      return new Notification(in);
    }

    public Notification[] newArray(int size) {
      return new Notification[size];
    }
  };

  public static class Builder {
    private String title;
    private String contents;
    private long serverTime;

    protected Builder() {
      this.title = "";
      this.contents = "";
      this.serverTime = 0;
    }

    protected Builder(Notification notification) {
      this.title = notification.getTitle();
      this.contents = notification.getContents();
      this.serverTime = notification.getServerTime();
    }

    public String getTitle() {
      return title;
    }

    public Builder setTitle(String title) {
      this.title = title;
      return this;
    }

    public String getContents() {
      return contents;
    }

    public Builder setContents(String contents) {
      this.contents = contents;
      return this;
    }

    public long getServerTime() {
      return serverTime;
    }

    public Builder setServerTime(long serverTime) {
      this.serverTime = serverTime;
      return this;
    }

    public Notification build() {
      return new Notification(title, contents, serverTime);
    }
  }
}
