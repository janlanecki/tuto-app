package pl.edu.mimuw.tuto.modules.tag_picker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.mimuw.tuto.common.object.tag.Tag;

public class TagWithParent extends Tag {
  public final static String PARENT_KEY = "parent";

  private int parent;


  protected TagWithParent(int id, String name, int parent) {
    super(id, name);
    this.parent = parent;
  }

  public int getParent() {
    return this.parent;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public Tag.Builder toBuilder() {
    return new Builder(this);
  }

  public static TagWithParent fromJSONObject(JSONObject jsonObject) throws JSONException {
    return new Builder(Tag.fromJSONObject(jsonObject))
        .setParent(jsonObject.getInt(PARENT_KEY))
        .build();
  }

  public static TagWithParent[] fromJSONArray(JSONArray jsonArray) throws JSONException {
    TagWithParent[] tags = new TagWithParent[jsonArray.length()];
    for (int i = 0; i < jsonArray.length(); ++i) {
      tags[i] = fromJSONObject(jsonArray.getJSONObject(i));
    }
    return tags;
  }

  public static class Builder extends Tag.Builder {
    private int parent;

    protected Builder() {
      super();
      this.parent = 0;
    }

    protected Builder(Tag tag) {
      super(tag);
      this.parent = 0;
    }

    protected Builder(TagWithParent tag) {
      super(tag);
      this.parent = tag.getParent();
    }

    public Builder setParent(int parent) {
      this.parent = parent;
      return this;
    }

    public TagWithParent build() {
      return new TagWithParent(getId(), getName(), parent);
    }
  }
}
