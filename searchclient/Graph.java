package searchclient;

import java.util.*;

class Graph {
    public Map<Vertex, List<Vertex>> adjVertices = new LinkedHashMap<>();
    public Map<String, Vertex> verticesMap = new LinkedHashMap<>();

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

    public List<Vertex> determine_goal_ordering(Map<Vertex, List<Vertex>> adjVertices) {
        // Identify all goal vertices
        List<Vertex> goals = new ArrayList<>();
        for (Vertex v : adjVertices.keySet()) {
            if (v.isGoal) {
                goals.add(v);
            }
        }

        // Build goal dependency graph
        Map<Vertex, List<Vertex>> goalDependencies = new LinkedHashMap<>();
        for (Vertex goal1 : goals) {
            for (Vertex goal2 : goals) {
                if (goal1 != goal2 && pathBlocked(goal1, goal2, goals)) {
                    goalDependencies.computeIfAbsent(goal1, k -> new ArrayList<>()).add(goal2);
                }
            }
        }

        // Perform topological sort on goalDependencies
        List<Vertex> orderedGoals = topologicalSort(goalDependencies);

        return orderedGoals;
    }

    public boolean pathBlocked(Vertex goal1, Vertex goal2, List<Vertex> goals) {
        for (Vertex goal : goals) {
            if (goal == goal1 || goal == goal2) {
                continue;
            }
            // Check if 'goal' lies on the direct path from 'goal1' to 'goal2'
            if (isOnPath(goal1, goal2, goal)) {
                return true;
            }
        }
        return false;
    }

    public List<Vertex> topologicalSort(Map<Vertex, List<Vertex>> graph) {
        Stack<Vertex> stack = new Stack<>();
        Set<Vertex> visited = new HashSet<>();

        for (Vertex v : graph.keySet()) {
            if (!visited.contains(v)) {
                topologicalSortUtil(v, visited, stack, graph);
            }
        }

        List<Vertex> result = new ArrayList<>();
        while (!stack.empty()) {
            result.add(stack.pop());
        }
        return result;
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

    public boolean isOnPath(Vertex v1, Vertex v2, Vertex v) {
        int v1_v2_distance = Math.abs(v1.locRow - v2.locRow) + Math.abs(v1.locCol - v2.locCol);
        int v1_v_distance = Math.abs(v1.locRow - v.locRow) + Math.abs(v1.locCol - v.locCol);
        int v_v2_distance = Math.abs(v.locRow - v2.locRow) + Math.abs(v.locCol - v2.locCol);

        return v1_v2_distance == v1_v_distance + v_v2_distance;
    }




}