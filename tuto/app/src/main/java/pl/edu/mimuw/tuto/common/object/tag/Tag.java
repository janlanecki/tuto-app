package pl.edu.mimuw.tuto.common.object.tag;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Tag implements Parcelable {
  public final static String ID_KEY = "id";
  public final static String NAME_KEY = "name";

  private int id;
  private String name;

  protected Tag(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Tag fromJSONObject(JSONObject jsonObject) throws JSONException {
    return newBuilder()
        .setId(jsonObject.getInt(ID_KEY))
        .setName(jsonObject.getString(NAME_KEY))
        .build();
  }

  public static Tag[] fromJSONArray(JSONArray jsonArray) throws JSONException {
    Tag[] tags = new Tag[jsonArray.length()];
    for (int i = 0; i < jsonArray.length(); ++i) {
      tags[i] = fromJSONObject(jsonArray.getJSONObject(i));
    }
    return tags;
  }

  @Override
  public String toString() {
    return "Tag{" +
        "id=" + id +
        ", name='" + name + '\'' +
        '}';
  }

  public boolean equals(Tag tag) {
    return getId() == tag.getId();
  }

  public boolean equals(Object o) {
    return o instanceof Tag && ((Tag) o).getId() == getId();
  }

  /**
   * Parcelable part - used to save Tag instances.
   */
  private Tag(Parcel in) {
    this.id = in.readInt();
    this.name = in.readString();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(this.id);
    dest.writeString(this.name);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
    public Tag createFromParcel(Parcel in) {
      return new Tag(in);
    }

    public Tag[] newArray(int size) {
      return new Tag[size];
    }
  };

  public static class Builder {
    private int id;
    private String name;

    protected Builder() {
      this.id = 0;
      this.name = "";
    }

    protected Builder(Tag session) {
      this.id = session.getId();
      this.name = session.getName();
    }

    public Builder setId(int id) {
      this.id = id;
      return this;
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public int getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public Tag build() {
      return new Tag(id, name);
    }
  }
}
