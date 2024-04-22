import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Main {
  /**
   * Finds the route with the fewest number of stops
   * @param routesMap A pairing of routes to stops
   * @return the route with the fewest number of stops
   */
  public static Route findShortestRoute(HashMap<Route, List<Stop>> routesMap) {
    int fewestStops = Integer.MAX_VALUE;
    Route shortestRoute = null;
    for (Route route : routesMap.keySet()) {
      List<Stop> stops = routesMap.get(route);

      if (stops.size() < fewestStops) {
        fewestStops = stops.size();
        shortestRoute = route;
      }
    }

    return shortestRoute;
  }

  /**
   * Finds the route with the greatest number of stops
   * @param routesMap A paring of routes to stops
   * @return the route with the greatest number of stops
   */
  public static Route findLongestRoute(HashMap<Route, List<Stop>> routesMap) {
    int mostStops = -1;
    Route longestRoute = null;
    for (Route route : routesMap.keySet()) {
      List<Stop> stops = routesMap.get(route);

      if (stops.size() > mostStops) {
        mostStops = stops.size();
        longestRoute = route;
      }
    }

    return longestRoute;
  }

  /**
   * Prints all the stops that are connections (join two routes) to the console
   * @param stopsMap The parings of stops to the routes they are on
   */
  public static void printConnectionStops(HashMap<String, List<Route>> stopsMap) {
    for (String stop : stopsMap.keySet()) {
      List<Route> connections = stopsMap.get(stop);
      if (connections.size() > 1) {
        System.out.print(stop + ": ");

        for (int i = 0; i < connections.size(); i += 1) {
          System.out.print(connections.get(i).name());

          if (i != connections.size() - 1) {
            System.out.print(", ");
          }
        }

        System.out.println();
      }
    }
  }

  public static void main(String[] args) throws IOException {
    Mbta mbta = new Mbta();
    List<Route> routes = mbta.getAllRailRoutes();

    // Part 1
    System.out.println("Part 1");
    System.out.println("###########################################");

    for (Route route : routes) {
      System.out.println(route.name());
    }

    // Part 2
    System.out.println("Part 2");
    System.out.println("###########################################");
    HashMap<Route, List<Stop>> routesMap = mbta.getRouteStops(routes);
    HashMap<String, List<Route>> stopsMap = mbta.indexByStop(routesMap);

    Route shortestRoute = findShortestRoute(routesMap);
    Route longestRoute = findLongestRoute(routesMap);

    System.out.printf("The route with the most stops is %s, with %d stops.\n",
            longestRoute.name(), routesMap.get(longestRoute).size());
    System.out.printf("The route with the fewest stops is %s, with %d stops.\n",
            shortestRoute.name(), routesMap.get(shortestRoute).size());

    printConnectionStops(stopsMap);

    // Part 3
    Scanner scanner = new Scanner(System.in);

    // Continually ask the user to enter two stops until the type "quit"
    while (true) {
      System.out.println("Enter two stops ('quit' to exit):");

      String s1 = scanner.nextLine();
      if (s1.equals("quit")) {
        break;
      }
      String s2 = scanner.nextLine();
      if (s2.equals("quit")) {
        break;
      }

      try {
        List<Route> path = mbta.connect(s1, s2, stopsMap, routesMap);
        System.out.print(s1 + " -> ");
        for (Route route : path) {
          System.out.print(route.name() + " -> ");
        }
        System.out.println(s2);
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
    }
  }
}
