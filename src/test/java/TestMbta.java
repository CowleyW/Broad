import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestMbta {

  private static Mbta mbta;
  private static List<Route> routes;
  private static HashMap<Route, List<Stop>> stopsPerRoute;

  @BeforeAll
  public static void setupTests() throws IOException {
    mbta = new Mbta();
    routes = mbta.getAllRailRoutes();
    stopsPerRoute = mbta.getRouteStops(routes);
  }

  @Test
  public void testGetRailRoutes() {
    assert (routes.size() == 8);
  }

  @Test
  public void testGetStops() throws IOException {
    Mbta api = new Mbta();

    List<Stop> stops = api.getStopsAlong("Red");
    // https://www.mbta.com/schedules/Red/line
    assert (stops.size() == 22);
  }

  @Test
  public void testGraphSearch() {
    HashMap<String, List<Route>> routesPerStop = mbta.indexByStop(stopsPerRoute);

    List<Route> path = mbta.connect("Heath Street", "Government Center", routesPerStop, stopsPerRoute);
    assertEquals(path.size(), 1);
    assertEquals(path.get(0).name(), "Green Line E");

    path = mbta.connect("Brookline Village", "Ruggles", routesPerStop, stopsPerRoute);
    assertEquals(path.size(), 2);
    assertEquals(path.get(0).name(), "Green Line D");
    assertEquals(path.get(1).name(), "Orange Line");

    path = mbta.connect("Kendall/MIT", "Maverick", routesPerStop, stopsPerRoute);
    assertEquals(path.size(), 3);
    assertEquals(path.get(0).name(), "Red Line");
    assertEquals(path.get(2).name(), "Blue Line");
  }
}
