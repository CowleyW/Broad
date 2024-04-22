import com.google.gson.Gson;

import org.junit.jupiter.api.Test;

public class TestDeserialization {

  @Test
  public void testDeserialization() {
    String jsonStruct = '{' +
            "\"attributes\":" +
            "{\"color\":\"FFC72C\",\"description\":\"Local Bus\"," +
            "\"direction_destinations\":[\"Salem Depot\",\"Wonderland or Haymarket Station\"]," +
            "\"direction_names\":[\"Outbound\",\"Inbound\"]," +
            "\"fare_class\":\"Inner Express\"," +
            "\"long_name\":\"Salem Depot - Wonderland or Haymarket Station\"," +
            "\"short_name\":\"450\",\"sort_order\":54500,\"text_color\":\"000000\"," +
            "\"type\":3}," +
            "\"id\":\"450\"," +
            "\"links\":{\"self\":\"/routes/450\"}," +
            "\"relationships\":{\"line\":{\"data\":{\"id\":\"line-424450456\"," +
            "\"type\":\"line\"}}},\"type\":\"route\"}";
    Route route = new Gson().fromJson(jsonStruct, Route.class);

    assert(route.name().equals("Salem Depot - Wonderland or Haymarket Station"));
    assert(route.id.equals("450"));
  }
}
