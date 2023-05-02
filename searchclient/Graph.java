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
}