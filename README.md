# Route-Optimization-using-Tabu-Search-and-Dijkstra

# 🚚 Java-Based Delivery Route Optimizer

This Java project optimizes delivery routes across Indian cities using **Dijkstra’s algorithm**, **Tabu Search**, and **vehicle load balancing**. It takes into account the coordinates of cities, delivery orders (with weights), and available delivery vehicles (with capacities) to produce an efficient delivery strategy.

---

## 🧠 Features

- **Graph Construction** from CSV containing city coordinates.
- **Shortest Path Calculation** using Dijkstra’s Algorithm.
- **Vehicle Load Balancing** by intelligently assigning orders based on weight and vehicle capacity.
- **Route Optimization** using Tabu Search to minimize the total distance for each vehicle.
- **Estimated Travel Time** based on average vehicle speed.
- **Google Maps Link Generation** for real-time navigation from source to destination.

---

## 🗂️ Project Structure

- `Optimize_Route.java`: Main class containing logic for graph setup, route optimization, and user input.
- `indian_cities.csv`: Input CSV file containing city names with their latitude and longitude.

---

## 📥 Input Format

Ensure `indian_cities.csv` is structured like this (including header):


### User Input (during runtime)

1. Starting city name (must exist in CSV)
2. Number of delivery orders
3. For each order:
    - Destination city
    - Weight (in kg)
4. Number of available delivery vehicles
5. Capacity of each vehicle (in kg)

---

## 📦 How to Run

### Prerequisites:
- Java 8 or later
- A terminal/IDE to run the program

### Steps:
```bash
# 1. Clone the repository
git clone https://github.com/your-username/route-optimizer.git
cd route-optimizer

# 2. Make sure 'indian_cities.csv' is in the root directory

# 3. Compile and run the program
javac io/Optimize_Route.java
java io.Optimize_Route
