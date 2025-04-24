package io;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Optimize_Route {

    private static final double AVERAGE_SPEED = 60.0; // average speed in km/h for estimated travel time

    // Graph representation with adjacency list
    static class Graph {
        private final Map<String, List<Edge>> adjacencyList = new HashMap<>();

        public void addEdge(String source, String destination, double weight) {
            adjacencyList.putIfAbsent(source, new ArrayList<>());
            adjacencyList.putIfAbsent(destination, new ArrayList<>());
            adjacencyList.get(source).add(new Edge(destination, weight));
            adjacencyList.get(destination).add(new Edge(source, weight));
        }

        public Map<String, Double> dijkstra(String start, Set<String> targets, Map<String, String> previous) {
            Map<String, Double> distances = new HashMap<>();
            PriorityQueue<Edge> queue = new PriorityQueue<>(Comparator.comparingDouble(edge -> edge.weight));

            for (String vertex : adjacencyList.keySet()) {
                distances.put(vertex, Double.MAX_VALUE);
                previous.put(vertex, null);
            }
            distances.put(start, 0.0);
            queue.add(new Edge(start, 0.0));

            while (!queue.isEmpty()) {
                Edge currentEdge = queue.poll();
                String currentNode = currentEdge.destination;

                if (!adjacencyList.containsKey(currentNode)) continue; // Skip nodes with no edges
                
                for (Edge edge : adjacencyList.getOrDefault(currentNode, Collections.emptyList())) {
                    double newDist = distances.get(currentNode) + edge.weight;
                    if (newDist < distances.get(edge.destination)) {
                        distances.put(edge.destination, newDist);
                        previous.put(edge.destination, currentNode);
                        queue.add(new Edge(edge.destination, newDist));
                    }
                }
            }
            return distances;
        }

        static class Edge {
            String destination;
            double weight;

            Edge(String destination, double weight) {
                this.destination = destination;
                this.weight = weight;
            }
        }
    }

    // Vehicle balancing based on weight of orders and vehicle capacity
    static class VehicleBalancing {
        public List<List<String>> balanceOrders(List<String> orders, List<Double> orderWeights, int numberOfVehicles, List<Double> vehicleCapacities) {
            // Initialize the result list for each vehicle's orders
            List<List<String>> optimizedRoutes = new ArrayList<>();
            for (int i = 0; i < numberOfVehicles; i++) {
                optimizedRoutes.add(new ArrayList<>());
            }

            // Sort orders based on weights in descending order (heavier orders first)
            List<Map.Entry<String, Double>> orderList = new ArrayList<>();
            for (int i = 0; i < orders.size(); i++) {
                orderList.add(new AbstractMap.SimpleEntry<>(orders.get(i), orderWeights.get(i)));
            }
            orderList.sort((o1, o2) -> Double.compare(o2.getValue(), o1.getValue()));  // Sort in descending order

            // Distribute orders to vehicles while respecting weight capacity
            double[] currentWeight = new double[numberOfVehicles]; // Current weight assigned to each vehicle
            for (Map.Entry<String, Double> order : orderList) {
                // Find the vehicle with the least current weight that can accommodate this order
                int bestVehicle = -1;
                double minWeight = Double.MAX_VALUE;
                for (int i = 0; i < numberOfVehicles; i++) {
                    if (currentWeight[i] + order.getValue() <= vehicleCapacities.get(i) && currentWeight[i] < minWeight) {
                        minWeight = currentWeight[i];
                        bestVehicle = i;
                    }
                }

                // Assign the order to the best vehicle
                if (bestVehicle != -1) {
                    optimizedRoutes.get(bestVehicle).add(order.getKey());
                    currentWeight[bestVehicle] += order.getValue();
                }
            }

            return optimizedRoutes;
        }
    }

    static class TabuSearch {
        private final Graph graph;
        private final String startLocation;

        public TabuSearch(Graph graph, String startLocation) {
            this.graph = graph;
            this.startLocation = startLocation;
        }

        public List<String> optimizeRoute(List<String> orders) {
            // Start with an initial route and optimize using Tabu Search
            List<String> bestRoute = new ArrayList<>(orders);
            double bestCost = calculateCost(bestRoute);

            List<String> tabuList = new ArrayList<>();
            int iterations = 100;

            for (int i = 0; i < iterations; i++) {
                List<String> neighbor = generateNeighbor(bestRoute);
                double neighborCost = calculateCost(neighbor);

                if (neighborCost < bestCost && !tabuList.contains(String.join(",", neighbor))) {
                    bestRoute = neighbor;
                    bestCost = neighborCost;
                    tabuList.add(String.join(",", neighbor));
                }
            }
            return bestRoute;
        }

        private double calculateCost(List<String> route) {
            double totalDistance = 0.0;
            String currentLocation = startLocation;

            for (String destination : route) {
                Map<String, String> previous = new HashMap<>();
                Map<String, Double> distances = graph.dijkstra(currentLocation, new HashSet<>(Collections.singletonList(destination)), previous);
                totalDistance += distances.getOrDefault(destination, Double.MAX_VALUE);
                currentLocation = destination;
            }
            return totalDistance;
        }

        private List<String> generateNeighbor(List<String> route) {
            List<String> neighbor = new ArrayList<>(route);
            Random random = new Random();
            int idx1 = random.nextInt(neighbor.size());
            int idx2 = random.nextInt(neighbor.size());
            Collections.swap(neighbor, idx1, idx2);
            return neighbor;
        }
    }

    public static void main(String[] args) {
        Graph graph = new Graph();
        Map<String, Coordinates> cityCoordinates = loadCitiesFromCSV("indian_cities.csv");
        createGraphEdges(graph, cityCoordinates);

        Scanner scanner = new Scanner(System.in);

        System.out.println("Available locations in the dataset:");
        cityCoordinates.keySet().forEach(System.out::println);

        System.out.println("Enter starting location:");
        String startLocation = scanner.nextLine();

        System.out.println("Enter the number of orders:");
        int numberOfOrders = scanner.nextInt();
        scanner.nextLine();

        List<String> orders = new ArrayList<>();
        List<Double> orderWeights = new ArrayList<>();
        for (int i = 0; i < numberOfOrders; i++) {
            System.out.println("Enter order location " + (i + 1) + ":");
            orders.add(scanner.nextLine());
            System.out.println("Enter weight for order " + (i + 1) + ":");
            orderWeights.add(scanner.nextDouble());
            scanner.nextLine(); // consume newline
        }

        System.out.println("Enter number of vehicles:");
        int numberOfVehicles = scanner.nextInt();
        scanner.nextLine();

        List<Double> vehicleCapacities = new ArrayList<>();
        for (int i = 0; i < numberOfVehicles; i++) {
            System.out.println("Enter weight capacity for Vehicle " + (i + 1) + ":");
            vehicleCapacities.add(scanner.nextDouble());
        }

        VehicleBalancing vehicleBalancing = new VehicleBalancing();
        List<List<String>> optimizedRoutes = vehicleBalancing.balanceOrders(orders, orderWeights, numberOfVehicles, vehicleCapacities);

        TabuSearch tabuSearch = new TabuSearch(graph, startLocation);
        List<String> optimizedRouteForVehicle1 = tabuSearch.optimizeRoute(optimizedRoutes.get(0));

        System.out.println("\nFinal Optimized Routes Summary:");
        for (int i = 0; i < optimizedRoutes.size(); i++) {
            List<String> currentRoute = (i == 0) ? optimizedRouteForVehicle1 : optimizedRoutes.get(i);
            System.out.println("\nVehicle " + (i + 1) + " handles the following orders: " + currentRoute);

            if (!currentRoute.isEmpty()) {
                Map<String, String> previous = new HashMap<>();
                Map<String, Double> distances = graph.dijkstra(startLocation, new HashSet<>(currentRoute), previous);
                double totalDistance = 0;
                List<String> orderPaths = new ArrayList<>();

                String previousLocation = startLocation;
                for (String order : currentRoute) {
                    double distance = distances.get(order);
                    totalDistance += distance;
                    List<String> path = getPath(previous, order);
                    orderPaths.add("Path to " + order + ": " + String.join(" -> ", path) + " (Distance: " + distance + " km)");

                    // Google Maps Link for shortest path
                    String googleMapsLink = generateGoogleMapsLink(previousLocation, order);
                    orderPaths.add("Google Maps link: " + googleMapsLink);

                    previousLocation = order;
                }

                double estimatedTotalTime = totalDistance / AVERAGE_SPEED;
                System.out.println("Estimated total distance for Vehicle " + (i + 1) + ": " + totalDistance + " km");
                System.out.println("Estimated travel time for Vehicle " + (i + 1) + ": " + estimatedTotalTime + " hours");
                orderPaths.forEach(System.out::println);
            }
        }
    }

    private static Map<String, Coordinates> loadCitiesFromCSV(String filePath) {
        Map<String, Coordinates> cityCoordinates = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false; // Skip the header row
                    continue;
                }
                String[] data = line.split(",");
                if (data.length == 3) {
                    try {
                        String cityName = data[0].trim();
                        double latitude = Double.parseDouble(data[1].trim());
                        double longitude = Double.parseDouble(data[2].trim());
                        cityCoordinates.put(cityName, new Coordinates(latitude, longitude));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing coordinates for line: " + line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityCoordinates;
    }

    private static void createGraphEdges(Graph graph, Map<String, Coordinates> cityCoordinates) {
        for (String city1 : cityCoordinates.keySet()) {
            for (String city2 : cityCoordinates.keySet()) {
                if (!city1.equals(city2)) {
                    double distance = calculateDistance(cityCoordinates.get(city1), cityCoordinates.get(city2));
                    graph.addEdge(city1, city2, distance);
                }
            }
        }
    }

    private static double calculateDistance(Coordinates coord1, Coordinates coord2) {
        double lat1 = Math.toRadians(coord1.latitude);
        double lon1 = Math.toRadians(coord1.longitude);
        double lat2 = Math.toRadians(coord2.latitude);
        double lon2 = Math.toRadians(coord2.longitude);

        double deltaLat = lat2 - lat1;
        double deltaLon = lon2 - lon1;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371 * c; // radius of the Earth in km
    }

    private static String generateGoogleMapsLink(String startLocation, String destination) {
        return "https://www.google.com/maps/dir/" + startLocation.replace(" ", "+") + "/" + destination.replace(" ", "+");
    }

    private static List<String> getPath(Map<String, String> previous, String destination) {
        List<String> path = new ArrayList<>();
        for (String at = destination; at != null; at = previous.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }

    static class Coordinates {
        double latitude;
        double longitude;

        Coordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
