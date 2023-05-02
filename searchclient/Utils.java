package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {

    public static Graph goalMapRepresentation;
    public static int[][] intMap;
    public static double[][] dist;

    public static Graph initialMapRepresentation(List<String> levelLines) {
        Graph graph = new Graph();
        int numRows = levelLines.size();
        int numCols = levelLines.stream().mapToInt(String::length).max().orElse(0);

        int[][] intMap2 = new int[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            String lineToRead = levelLines.get(i);
            int lineLength = lineToRead.length();
//            var rowRepresentation = new ArrayList<Vertex>();
            for (int j = 0; j < numCols; j++) {
                // Here, we can find out if isWall, isAgent, isBox are true or false, but the others are needed by
                // preprocessing
                if (j >= lineLength || lineToRead.charAt(j) == '+') {
                    intMap2[i][j] = -1; // wall or obstacle
//                    rowRepresentation.add(cell);
                } else {
                    graph.addVertex(i, j);

                    intMap2[i][j] = i * numCols + j; // empty cell
//                    rowRepresentation.add(cell);
                }
            }
//            initMapRep.add(rowRepresentation); // Move this line outside the inner loop
        }

        /*// This is a hack
        var mapAsArray = initMapRep.stream()
                .map(l -> l.toArray(Vertex[]::new))
                .toArray(Vertex[][]::new);

        for (var row : initMapRep) {
            for (var cell : row) {
                calculateEdges(initMapRep, cell, mapAsArray.length, mapAsArray[0].length);
            }
        }*/

        int numVertices = numRows * numCols;
        List<List<Edge>> graph2 = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            graph2.add(new ArrayList<>());
        }

        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, 1, 0, -1};
        double[][] dist = new double[numVertices][numVertices];

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                if (intMap2[i][j] == -1) continue;

                for (int k = 0; k < 4; k++) {
                    int ni = i + dx[k];
                    int nj = j + dy[k];

                    if (ni >= 0 && ni < numRows && nj >= 0 && nj < numCols && intMap2[ni][nj] != -1) {
                        double distVal = Math.sqrt((ni - i) * (ni - i) + (nj - j) * (nj - j)); // Euclidean distance
                        graph2.get(intMap2[i][j]).add(new Edge(intMap2[ni][nj], distVal));
                        if(levelLines.get(i).charAt(j)!= '+' && levelLines.get(ni).charAt(nj)!='+') {
                            graph.addEdge(i, j, ni, nj);
                        }
                    }
                }
            }
        }

        int INF = 1000000000; // a very large number to represent infinity

        // Compute the shortest distance between every pair of vertices using Floyd-Warshall algorithm
        for (int i = 0; i < numVertices; i++) {
            Arrays.fill(dist[i], INF);
            dist[i][i] = 0;
        }

        for (int u = 0; u < numVertices; u++) {
            int i = u / numCols;
            int j = u % numCols;

            for (Edge e : graph2.get(u)) {
                int v = e.to;
                int ni = v / numCols;
                int nj = v % numCols;

                double distVal = Math.sqrt((ni - i) * (ni - i) + (nj - j) * (nj - j)); // Euclidean distance
                dist[u][v] = distVal;
            }
        }

        for (int k = 0; k < numVertices; k++) {
            for (int i = 0; i < numVertices; i++) {
                for (int j = 0; j < numVertices; j++) {
                    dist[i][j] = Math.min(dist[i][j], dist[i][k] + dist[k][j]);
                }
            }
        }

        // to ensure that things are synced for static vars
        Utils.intMap = intMap2;
        Utils.dist = dist;

        return graph;
    }

//    public static void calculateEdges(List<List<Vertex>> map, Vertex vertex, int numRows, int numCols){
//        int row = vertex.locRow;
//        int col = vertex.locCol;
//
//        vertex.edgeN  = (row > 0) ? getWeightForEdges(map.get(row - 1).get(col)) : new Edge(0, -1);
//        vertex.edgeS  = (row < numRows - 1) ? getWeightForEdges(map.get(row + 1).get(col)) : new Edge(0, -1);
//        vertex.edgeE  = (col < numCols - 1) ? getWeightForEdges(map.get(row).get(col + 1)) : new Edge(0, -1);
//        vertex.edgeW  = (col > 0) ? getWeightForEdges(map.get(row).get(col - 1)) : new Edge(0, -1);
//
//        vertex.edgeNE = (row > 0 && col < numCols - 1) ? getWeightForEdges(map.get(row - 1).get(col + 1)) : new Edge(0, -1);
//        vertex.edgeNW = (row > 0 && col > 0) ? getWeightForEdges(map.get(row - 1).get(col - 1)) : new Edge(0, -1);
//        vertex.edgeSE = (row < numRows - 1 && col < numCols - 1) ? getWeightForEdges(map.get(row + 1).get(col + 1)) : new Edge(0, -1);
//        vertex.edgeSW = (row < numRows - 1 && col > 0) ? getWeightForEdges(map.get(row + 1).get(col - 1)) : new Edge(0, -1);
//    }

//    public static Edge getWeightForEdges(Vertex cell){
//
//        if (cell.isAgent) {
//            return new Edge(0, 5);
//        } else if (cell.isBox) {
//            return new Edge(0, 10);
//        } else if (cell.isFree) {
//            return new Edge(0, 0);
//        } else if (cell.isWall) {
//            return new Edge(0, -1);
//        } else {
//            // This is just a placeholder. You should change it.
//            return new Edge(0, 0);
//        }
//    }

    public static double getDistance(int[][] intMap, double[][] dist, int startX, int startY, int endX, int endY) {
        int startVertex = intMap[startX][startY];
        int endVertex = intMap[endX][endY];
        return dist[startVertex][endVertex];
    }

    public static void printDistancesFromCell(int[][] intMap, double[][] dist, int startRow, int startCol) {
        int numRows = intMap.length;
        int numCols = intMap[0].length;

        // Print row labels and distances
        System.err.println("Distances from cell (" + startRow + "," + startCol + "):");

        // Print column labels
        System.err.print("       ");
        for (int j = 0; j < numCols; j++) {
            System.err.printf("%4d", j);
        }
        System.err.println();


        for (int i = 0; i < numRows; i++) {
            System.err.printf("%4d   ", i);
            for (int j = 0; j < numCols; j++) {
                if (intMap[i][j] == -1) {
                    System.err.printf("%4s", "####");
                } else {
                    double distance = dist[intMap[startRow][startCol]][intMap[i][j]];
                    System.err.printf("%4.0f", distance);
                }
            }
            System.err.println();
        }
    }
}
