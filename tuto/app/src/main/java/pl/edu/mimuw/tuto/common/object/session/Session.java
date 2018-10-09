package pl.edu.mimuw.tuto.common.object.session;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.edu.mimuw.tuto.common.object.tag.Tag;

public class Session implements Parcelable {
  private final static String ID_KEY = "id";
  private final static String LABEL_KEY = "label";
  private final static String DATE_KEY = "date";
  private final static String TIME_KEY = "time2";
  private final static String PLACE_KEY = "place";
  private final static String AUTHOR_KEY = "author";
  private final static String OWN_KEY = "own";
  private final static String TAGS_KEY = "tags";
  private final static String SUPER_TAGS_KEY = "super_tags";

  private int id;
  private String date;
  private String time;
  private String label;
  private String place;
  private String author;
  private String own;
  private List<Tag> tags;
  private List<Tag> superTags;

  private Session(int id, String label, String date, String time, String place, String author, String own, List<Tag> tags, List<Tag> superTags) {
    this.id = id;
    this.label = label;
    this.date = date;
    this.time = time;
    this.place = place;
    this.author = author;
    this.own = own;
    this.tags = tags;
    this.superTags = superTags;
  }

  public int getId() {
    return this.id;
  }

  public String getDate() {
    return this.date;
  }

  public String getTime() {
    return this.time;
  }

  public String getLabel() {
    return this.label;
  }

  public String getPlace() {
    return this.place;
  }

  public String getAuthor() {
    return this.author;
  }

  public String getOwn() { return this.own; }

  public List<Tag> getTags() {
    return new ArrayList<>(this.tags);
  }

  public List<Tag> getSuperTags() {
    return new ArrayList<>(this.superTags);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Session fromJSONObject(JSONObject jsonObject) throws JSONException {
    return newBuilder()
        .setId(jsonObject.getInt(ID_KEY))
        .setLabel(jsonObject.getString(LABEL_KEY))
        .setDate(jsonObject.getString(DATE_KEY))
        .setTime(jsonObject.getString(TIME_KEY))
        .setPlace(jsonObject.getString(PLACE_KEY))
        .setAuthor(jsonObject.getString(AUTHOR_KEY))
        .setOwn(jsonObject.getString(OWN_KEY))
        .setTags(Arrays.asList(Tag.fromJSONArray(jsonObject.getJSONArray(TAGS_KEY))))
        .setSuperTags(Arrays.asList(Tag.fromJSONArray(jsonObject.getJSONArray(SUPER_TAGS_KEY))))
        .build();
  }

  public static Session[] fromJSONArray(JSONArray jsonArray) throws JSONException {
    Session[] sessions = new Session[jsonArray.length()];
    for (int i = 0; i < jsonArray.length(); ++i) {
      sessions[i] = fromJSONObject(jsonArray.getJSONObject(i));
    }
    return sessions;
  }

  @Override
  public String toString() {
    return "Session{" +
        "id=" + id +
        ", date=" + date +
        ", time=" + time +
        ", label='" + label + '\'' +
        ", place='" + place + '\'' +
        ", author='" + author + '\'' +
        ", own='" + own + '\'' +
        ", tags=" + tags +
        ", superTags=" + superTags +
        '}';
  }

  /**
   * Parcelable part - used to save Session instances.
   */
  private Session(Parcel in) {
    this.id = in.readInt();
    this.label = in.readString();
    this.time = in.readString();
    this.place = in.readString();
    this.author = in.readString();
    this.own = in.readString();
    Tag[] tags = (Tag[]) in.readParcelableArray(Tag.class.getClassLoader());
    this.tags = new ArrayList<>(Arrays.asList(tags));
    Tag[] superTags = (Tag[]) in.readParcelableArray(Tag.class.getClassLoader());
    this.superTags = new ArrayList<>(Arrays.asList(superTags));
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(this.id);
    dest.writeString(this.label);
    dest.writeString(this.time);
    dest.writeString(this.place);
    dest.writeString(this.author);
    dest.writeString(this.own);
    dest.writeParcelableArray(this.tags.toArray(new Tag[0]), 0);
    dest.writeParcelableArray(this.superTags.toArray(new Tag[0]), 0);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
    public Session createFromParcel(Parcel in) {
      return new Session(in);
    }

    public Session[] newArray(int size) {
      return new Session[size];
    }
  };

  public static class Builder {
    private int id;
    private String label;
    private String date;
    private String time;
    private String place;
    private String author;
    private String own;
    private List<Tag> tags;
    private List<Tag> superTags;

    private Builder() {
      this.id = 0;
      this.label = "";
      this.date = "";
      this.time = "";
      this.place = "";
      this.author = "";
      this.own = "";
      this.tags = new ArrayList<>();
      this.superTags = new ArrayList<>();
    }

    private Builder(Session session) {
      this.id = session.getId();
      this.label = session.getLabel();
      this.date = session.getDate();
      this.time = session.getTime();
      this.place = session.getPlace();
      this.author = session.getAuthor();
      this.own = session.getOwn();
      this.tags = session.getTags();
      this.superTags = session.getSuperTags();
      superTags.add(Tag.newBuilder().setName("Lorem ipsum").build());
    }

    public Builder setId(int id) {
      this.id = id;
      return this;
    }

    public Builder setLabel(String label) {
      this.label = label;
      return this;
    }

    public Builder setDate(String date) {
      this.date = date;
      return this;
    }

    public Builder setTime(String time) {
      this.time = time;
      return this;
    }

    public Builder setPlace(String place) {
      this.place = place;
      return this;
    }

    public Builder setAuthor(String author) {
      this.author = author;
      return this;
    }

    public Builder setOwn(String own) {
      this.own = own;
      return this;
    }

    public Builder setTags(List<Tag> tags) {
      this.tags = new ArrayList<>(tags);
      return this;
    }
    public Builder setSuperTags(List<Tag> tags) {
      this.superTags = new ArrayList<>(tags);
      return this;
    }

    public Session build() {
      return new Session(id, label, date, time, place, author, own, tags, superTags);
    }
  }
}
