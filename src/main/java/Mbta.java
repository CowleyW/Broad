import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Mbta {
  private final CloseableHttpClient client;

  private static class RouteResponse {
    @SerializedName("data")
    List<Route> routes;
  }

  private static class StopResponse {
    @SerializedName("data")
    List<StopData> stops;
  }

  private static class StopData {
    @SerializedName("attributes")
    Stop stop;
  }

  Mbta() {
    this.client = HttpClients.createDefault();
  }

  /**
   * Queries the MBTA api for the list of all heavy and light rails
   * @return The list of all Heavy Rail and Light Rail routes
   * @throws IOException if the Http request fails
   */
  public List<Route> getAllRailRoutes() throws IOException {
    // Here I chose to allow the MBTA server to filter the data for me. Since we are using the filter
    // functionality in a very straight-forward manner, it seems more productive to re-use the api's
    // provided filtering, rather than to re-implement it ourselves. If we had a more complicated
    // way of filtering, I would choose to do it all client-side to avoid fragmenting filtering logic
    HttpGet request = new HttpGet("https://api-v3.mbta.com/routes?filter[type]=0,1");

    HttpResponse response = client.execute(request);

    String json = EntityUtils.toString(response.getEntity());

    return this.parseRoutes(json);
  }

  /**
   * Returns a list of all the stops along the route that matches the corresponding route id
   * @param id The route id used to query the stops for the desired route
   * @return The list of stops along the route
   * @throws IOException if the Http request fails
   */
  public List<Stop> getStopsAlong(String id) throws IOException {
    // Again, the same choice was made here to filter using the provided api query. In a more complex
    // scenario I may choose to implement the filtering client-side if the project needs demand it.
    HttpGet request = new HttpGet("https://api-v3.mbta.com/stops?filter[route]=" + id);

    HttpResponse response = client.execute(request);

    String json = EntityUtils.toString(response.getEntity());

    return this.parseStops(json);
  }

  /**
   * Get all the stops along each route using the MBTA api
   * @param routes The routes to get stops for
   * @return The route -> stops pairings
   * @throws IOException if the Http request fails
   */
  public HashMap<Route, List<Stop>> getRouteStops(List<Route> routes) throws IOException {
    HashMap<Route, List<Stop>> routeStops = new HashMap<>();

    for (Route route : routes) {
      List<Stop> stops = this.getStopsAlong(route.id);
      routeStops.put(route, stops);
    }

    return routeStops;
  }

  /**
   * Index the route -> stops pairings as stop -> routes pairings instead for convenience
   * @param routeStops The route -> stops pairings
   * @return The stop -> routes pairings
   */
  public HashMap<String, List<Route>> indexByStop(HashMap<Route, List<Stop>> routeStops) {
    HashMap<String, List<Route>> map = new HashMap<>();

    for (Route route : routeStops.keySet()) {
      List<Stop> stops = routeStops.get(route);
      for (Stop stop : stops) {
        map.computeIfAbsent(stop.name, k -> new ArrayList<>());
        List<Route> connections = map.get(stop.name);
        connections.add(route);
      }
    }

    return map;
  }

  /**
   * Connects the two stops using the available routes
   * @param beginStop The beginning stop
   * @param endStop The ending stop
   * @param stopsMap The stop -> routes pairings
   * @param routesMap The route -> stops pairings
   * @return A path that connects the two stops
   */
  public List<Route> connect(String beginStop, String endStop, HashMap<String, List<Route>> stopsMap, HashMap<Route, List<Stop>> routesMap) {
    if (!stopsMap.containsKey(beginStop)) {
      throw new IllegalArgumentException("Invalid beginning stop " + beginStop + "!");
    }
    if (!stopsMap.containsKey(endStop)) {
      throw new IllegalArgumentException("Invalid ending stop " + endStop + "!");
    }

    // Essentially a breadth-first search algorithm
    // We keep exploring the closest routes until we find a route that connects to our destination
    HashMap<Route, Route> cameFrom = new HashMap<>();

    List<Route> workList = new ArrayList<>(stopsMap.get(beginStop));
    while (!workList.isEmpty()) {
      Route next = workList.remove(0);

      // This serves a dual-purpose:
      // 1) When reconstructing the path, a route that "came from" itself is a starting route, so we
      //    can end the reconstruction
      // 2) This prevents an infinite loop if another route also connects back to the initial route.
      //    For example, Green Line B <-> Green Line C
      if (!cameFrom.containsKey(next)) {
        cameFrom.put(next, next);
      }

      for (Stop stop : routesMap.get(next)) {
        for (Route visitable : stopsMap.get(stop.name)) {
          if (!cameFrom.containsKey(visitable)) {
            cameFrom.put(visitable, next);
            workList.add(visitable);
          }
        }
      }

      // Once we've reached the destination, we backtrack to figure out what the path we took was
      if (stopsMap.get(endStop).contains(next)) {
        return this.reconstructPath(cameFrom, next);
      }
    }

    return new ArrayList<>();
  }

  /**
   * Reconstructs the path taken to reach the destination
   * @param cameFrom A map representing to -> from pairings
   * @param end The final route
   * @return The path taken to reach the destination
   */
  private List<Route> reconstructPath(HashMap<Route, Route> cameFrom, Route end) {
    List<Route> path = new ArrayList<>();
    path.add(end);

    Route prev = end;
    while (cameFrom.get(prev) != prev) {
      prev = cameFrom.get(prev);
      path.add(prev);
    }

    Collections.reverse(path);
    return path;
  }

  /**
   * Parses the list of routes from a JSON string
   * @param json the string encoding of the routes
   * @return the extracted list of routes
   */
  private List<Route> parseRoutes(String json) {
    RouteResponse data = new Gson().fromJson(json, RouteResponse.class);

    return data.routes;
  }

  /**
   * Parses the list of stops from a JSON string
   * @param json the string encoding of the stops
   * @return the extracted list of stops
   */
  private List<Stop> parseStops(String json) {
    StopResponse data = new Gson().fromJson(json, StopResponse.class);

    return data.stops.stream().map(s -> s.stop).toList();
  }
}
