package searchclient;

import java.util.*;

public class Vertex implements Comparable<Vertex> {

    public int locRow;
    public int locCol;
    public Character goalChar = '\0';
    public Character boxChar = '\0';

    Vertex(int x,int y) {
        this.locRow = x;
        this.locCol = y;
    }



    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Vertex other))
            return false;

        return (this.locRow == other.locRow && this.locCol == other.locCol);
    }

    @Override
    public int hashCode() {
        // create a string based on the fields in this object
        return Objects.hash(String.format("%d,%d", locRow, locCol));
    }

//    @Override
//    public String toString() {
//        return String.format("Vertex{locRow=%d, locCol=%d, isWall=%b, isFree=%b, isBox=%b, isAgent=%b, goal=%s, agent=%s, box=%s, edgeN=%s, edgeNE=%s, edgeE=%s, edgeSE=%s, edgeS=%s, edgeSW=%s, edgeW=%s, edgeNW=%s}\n",
//                locRow, locCol, isWall, isFree, isBox, isAgent, goal, agent, box, edgeN, edgeNE, edgeE, edgeSE, edgeS, edgeSW, edgeW, edgeNW);
//    }

    @Override
    public String toString() {
        return String.format("Vertex {locRow=%d, locCol=%d}", locRow, locCol);
    }

    @Override
    public int compareTo(Vertex o) {
        if (this.locRow == o.locRow) {
            return this.locCol - o.locCol;
        }
        return this.locRow - o.locRow;
    }
}
