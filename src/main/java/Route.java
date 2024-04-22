import com.google.gson.annotations.SerializedName;

public class Route {
  private static class Info {
    @SerializedName("long_name")
    String longName;
  }

  @SerializedName("attributes")
  Info info;
  @SerializedName("id")
  String id;

  public String name() {
    return this.info.longName;
  }
}