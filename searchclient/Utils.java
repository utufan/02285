package searchclient;

import java.util.List;

public class Utils {

    public static List<List<Vertex>> goalMapRepresentation;

    public static void calculateEdges(List<List<Vertex>> map, Vertex vertex, int numRows, int numCols){
        int row = vertex.locRow;
        int col = vertex.locCol;

        vertex.edgeN  = (row > 0) ? getWeightForEdges(map.get(row - 1).get(col)) : new Edge(0, -1);
        vertex.edgeS  = (row < numRows - 1) ? getWeightForEdges(map.get(row + 1).get(col)) : new Edge(0, -1);
        vertex.edgeE  = (col < numCols - 1) ? getWeightForEdges(map.get(row).get(col + 1)) : new Edge(0, -1);
        vertex.edgeW  = (col > 0) ? getWeightForEdges(map.get(row).get(col - 1)) : new Edge(0, -1);

        vertex.edgeNE = (row > 0 && col < numCols - 1) ? getWeightForEdges(map.get(row - 1).get(col + 1)) : new Edge(0, -1);
        vertex.edgeNW = (row > 0 && col > 0) ? getWeightForEdges(map.get(row - 1).get(col - 1)) : new Edge(0, -1);
        vertex.edgeSE = (row < numRows - 1 && col < numCols - 1) ? getWeightForEdges(map.get(row + 1).get(col + 1)) : new Edge(0, -1);
        vertex.edgeSW = (row < numRows - 1 && col > 0) ? getWeightForEdges(map.get(row + 1).get(col - 1)) : new Edge(0, -1);
    }

    public static Edge getWeightForEdges(Vertex cell){

        if (cell.isAgent) {
            return new Edge(0, 5);
        } else if (cell.isBox) {
            return new Edge(0, 10);
        } else if (cell.isFree) {
            return new Edge(0, 0);
        } else if (cell.isWall) {
            return new Edge(0, -1);
        } else {
            // This is just a placeholder. You should change it.
            return new Edge(0, 0);
        }
    }
}
