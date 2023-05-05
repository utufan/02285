package searchclient;

import java.util.*;

class Graph {
    public Map<Vertex, List<Vertex>> adjVertices = new LinkedHashMap<>();
    public Map<String, Vertex> verticesMap = new LinkedHashMap<>();

    public static Map<Character, Vertex> boxMap = new LinkedHashMap<>();

    public void addVertex(int x, int y) {
        String key = x + "," + y;
        Vertex v = verticesMap.get(key);
        if (v == null) {
            v = new Vertex(x, y);
            verticesMap.put(key, v);
        }
        adjVertices.putIfAbsent(v, new ArrayList<>());
    }

    void removeVertex(int x, int y) {
        String key = x + "," + y;
        Vertex v = verticesMap.get(key);

        adjVertices.values().stream().forEach(e -> e.remove(v));
        adjVertices.remove(new Vertex(x, y));
    }

    public Vertex getVertex(int x, int y) {
        String key = x + "," + y;
        return verticesMap.get(key);
    }

    void addEdge(int x1, int y1,int x2, int y2) {
        String key1 = x1 + "," + y1;
        String key2 = x2 + "," + y2;
        Vertex v1 = verticesMap.get(key1);
        Vertex v2 = verticesMap.get(key2);
        adjVertices.putIfAbsent(v1, new ArrayList<>());
        adjVertices.putIfAbsent(v2, new ArrayList<>());
        // Avoid duplicate vertices
        if (!adjVertices.get(v1).contains(v2)) {
            adjVertices.get(v1).add(v2);
        }
        if (!adjVertices.get(v2).contains(v1)) {
            adjVertices.get(v2).add(v1);
        }
    }

    void removeEdge(int x1, int y1,int x2, int y2) {
        Vertex v1 = new Vertex(x1, y2);
        Vertex v2 = new Vertex(x2, y2);
        List<Vertex> eV1 = adjVertices.get(v1);
        List<Vertex> eV2 = adjVertices.get(v2);
        if (eV1 != null)
            eV1.remove(v2);
        if (eV2 != null)
            eV2.remove(v1);
    }

    List<Vertex> getAdjVertices(int x, int y) {
        String key = x + "," + y;
        Vertex v = verticesMap.get(key);
        return adjVertices.get(new Vertex(x, y));
    }

    public static void removeVertexIfNotInList(Map<Vertex, List<Vertex>> graph) {
        Iterator<Vertex> vertexIterator = graph.keySet().iterator();

        while (vertexIterator.hasNext()) {
            Vertex currentVertex = vertexIterator.next();
            boolean foundInLists = false;

            for (List<Vertex> verticesList : graph.values()) {
                if (verticesList.contains(currentVertex)) {
                    foundInLists = true;
                    break;
                }
            }

            if (!foundInLists && graph.get(currentVertex).isEmpty()) {
                vertexIterator.remove();
            }
        }
    }

    public List<Vertex> determine_goal_ordering(Map<Vertex, List<Vertex>> adjVertices) {
        // Identify all goal vertices
        Map<Character, Vertex> goalMap = new LinkedHashMap<>();
//        Map<Character, Vertex> boxMap = new LinkedHashMap<>();
        for (Vertex v : adjVertices.keySet()) {
            if (v.goalChar != '\0') {
                goalMap.put(v.goalChar, v);
            }
            else if (v.boxChar != '\0') {
                boxMap.put(v.boxChar, v);
            }
        }

        // Build goal dependency graph
        Map<Vertex, List<Vertex>> goalDependencies = new LinkedHashMap<>();

        for (var goal: goalMap.values()) {
            goalDependencies.put(goal, new ArrayList<>());
        }

        for (Vertex goal1 : goalMap.values()) {
            for (Vertex goal2 : goalMap.values()) {
                if (goal1 != goal2 && pathBlocked(goal1, goal2, boxMap)) {
                    goalDependencies.computeIfAbsent(goal1, k -> new ArrayList<>()).add(goal2);
                }
            }
        }

        removeVertexIfNotInList(goalDependencies);

        // Perform topological sort on goalDependencies
        List<Vertex> orderedGoals = topologicalSort(goalDependencies);

        return orderedGoals;
    }

    public boolean pathBlocked(Vertex goal1, Vertex goal2, Map<Character, Vertex> boxMap) {
        return isBlockingAccess(goal1, goal2, boxMap, adjVertices);
    }

    private static void dfs(Map<Vertex, List<Vertex>> graph, Vertex vertex, Set<Vertex> visited, Stack<Vertex> stack) {
        visited.add(vertex);
        List<Vertex> neighbors = graph.get(vertex);
        if (neighbors != null) {
            for (Vertex neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    dfs(graph, neighbor, visited, stack);
                }
            }
        }
        stack.push(vertex);
    }

    public static List<Vertex> topologicalSort(Map<Vertex, List<Vertex>> goalDependencies) {
        Set<Vertex> visited = new HashSet<>();
        Stack<Vertex> stack = new Stack<>();

        for (Vertex vertex : goalDependencies.keySet()) {
            if (!visited.contains(vertex)) {
                dfs(goalDependencies, vertex, visited, stack);
            }
        }

        List<Vertex> sortedGoals = new ArrayList<>();
        while (!stack.isEmpty()) {
            sortedGoals.add(stack.pop());
        }

        return sortedGoals;
    }


    private void topologicalSortUtil(Vertex v, Set<Vertex> visited, Stack<Vertex> stack, Map<Vertex, List<Vertex>> graph) {
        visited.add(v);
        for (Vertex neighbor : graph.get(v)) {
            if (!visited.contains(neighbor)) {
                topologicalSortUtil(neighbor, visited, stack, graph);
            }
        }
        stack.push(v);
    }

    public boolean isBlockingAccess(Vertex v1, Vertex v2, Map<Character, Vertex> boxMap, Map<Vertex, List<Vertex>> adjVertices) {
        Set<Vertex> visited = new HashSet<>();
        Queue<Vertex> queue = new LinkedList<>();

//        visited.add(v1);
        visited.add(v2); // Treat v2 as a blocking element
        Vertex v1_box = boxMap.get(v1.goalChar);


        //Should be the location of the box that needs to go to v1
        queue.add(v1_box);

        // Perform a breadth-first search (BFS) from v1, ignoring v2 as a blocking element
        while (!queue.isEmpty()) {
            Vertex current = queue.poll();
            List<Vertex> neighbors = adjVertices.get(current);

            if (neighbors != null) {
                for (Vertex neighbor : neighbors) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        var temp = visited.contains(v1);

        // Check if we can access our goal from our box
        return !visited.contains(v1);
    }
}