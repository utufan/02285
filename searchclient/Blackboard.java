package searchclient;

import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/*
    This class is used to store all of the information that the agents and boxes have access to.
    This includes the state of the world, the goals, and the agents and boxes. Somehow, it needs
    to include a temporal aspect so conflicts can be detected and resolved.
 */
public class Blackboard {
    // Because this is a WIP, I am going to use the domain model concepts to test how it would work. I think it should
    // be okay from a memory perspective, but testing will show.
    private static volatile Blackboard instance;
    private static boolean isInitialized = false;
    List<Agent> agents;
    List<Box> boxes;
    List<Goal> goals;
    int width;
    int height;
    int[][] intMap;
    double[][] dist;
    // TODO: Use the new map representation with adjacent vertices
    Graph mapRepresentation;
    Graph goalMapRepresentation;


    // TODO: Investigate how we want to handle vertices to move boxes/agents blocking others
    SortedSet<Vertex> reservedVertices = null;
    SortedSet<Vertex> unreservedVertices = new TreeSet<>();

    private Blackboard(List<Agent> agents, List<Box> boxes, List<Goal> goals,
                       int width, int height, int[][] intMap, double[][] dist, Graph mapRepresentation) {
        this.agents = agents;
        this.boxes = boxes;
        this.goals = goals;
        this.width = width;
        this.height = height;
        this.intMap = intMap;
        this.dist = dist;
        this.mapRepresentation = mapRepresentation;
    }

    public Box getBox(int row, int col) {
        for (var box : boxes) {
            if (box.row == row && box.col == col) {
                return box;
            }
        }
        return null;
    }

    public synchronized double getDistance(int startX, int startY, int endX, int endY) {
        int startVertex = intMap[startX][startY];
        int endVertex = intMap[endX][endY];

        // Fixed TODO
        // This should be solved with using the adjacent vertices because we no longer put walls as -1; confirm that
        // intMap won't give us this just be sure. Otherwise, we need a check for -1 here.
        // We investigated potential exceptions and we decided to throw en exception in case we missed a potential error (WALL -1)

        if(mapRepresentation.adjVertices.get(startVertex+","+endVertex) == null ){
           //write an exception
            try{
                throw new Exception("It is wall");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return dist[startVertex][endVertex];
    }

    public static void initialize(List<Agent> agents, List<Box> boxes, List<Goal> goals, int width, int height
            , int[][] intMap, double[][] dist, Graph mapRepresentation) {
        if (!isInitialized) {
            synchronized (Blackboard.class) {
                if (!isInitialized) {
                    instance = new Blackboard(agents, boxes, goals, width, height, intMap, dist, mapRepresentation);
                    isInitialized = true;
                }
            }
        }
    }

    // TODO: make this dynamic as tasks are completed and newly assigned
    public synchronized void reserveVertices(List<Vertex> verticesToBeReserved) {
        if (reservedVertices == null) {
            reservedVertices = new TreeSet<>();
        }
        reservedVertices.addAll(verticesToBeReserved);
    }

    // TODO: make this dynamic as tasks are completed and newly assigned
    public synchronized void verticesNotReserved() {
        for (var vertex : mapRepresentation.verticesMap.values()) {
            if (!this.reservedVertices.contains(vertex)) {
                this.unreservedVertices.add(vertex);
            }
        }

//        for (var vertices : this.mapRepresentation) {
//            for (var vertex : vertices) {
//                if (!this.reservedVertices.contains(vertex)) {
//                    this.unreservedVertices.add(vertex);
//                }
//            }
//        }
//        if (unreservedVertices == null){
//            unreservedVertices = new TreeSet<>();
//        }
//        for (var vertex : verticesToBeUnreserved) {
//            unreservedVertices.add(vertex);
//        }
    }

    public synchronized Vertex getVertex(int x, int y){

        return mapRepresentation.getVertex(x, y);

//        return mapRepresentation.get(x).get(y);
    }

    public static Blackboard getInstance() {
        if (!isInitialized) {
            throw new IllegalStateException("Blackboard not initialized");
        }
        return instance;
    }

    public synchronized String toString() {
        StringBuilder result = new StringBuilder("Agents: ");

        for (Agent agent : agents) {
            result.append(agent.toString()).append(", ");
        }

        result.append("Boxes: ");

        for (Box box : boxes) {
            result.append(box.toString()).append(", ");
        }

        result.append("Goals: ");

        for (Goal goal : goals) {
            result.append(goal.toString()).append(", ");
        }

        return result.toString();
    }

    // Just moves the agent
//    public synchronized void updateBlackboard(List<Agent> agents) {
//        // TODO: we need to include boxes in this update
//        // !!! We also need this to update the vertex information accordingly !!!
//        for (Agent agentAfterAction : agents) {
//            for (Agent agentBeforeAction : this.agents) {
//                if (Objects.equals(agentBeforeAction.id, agentAfterAction.id)) {
//                    getVertex(agentBeforeAction.row, agentBeforeAction.col).cellChar = null;
////                    agentBeforeAction.row = agentAfterAction.row;
////                    agentBeforeAction.col = agentAfterAction.col;
//                    // We need to update the vertex information here
////                    Vertex vertexBefore = getVertex(agentBeforeAction.row, agentBeforeAction.col);
////                    Vertex vertexAfter = getVertex(agentAfterAction.row, agentAfterAction.col);
//                    getVertex(agentAfterAction.row, agentAfterAction.col).cellChar = agentBeforeAction.id.charAt(0);
////                    vertexAfter.cellChar = agentBeforeAction.id.charAt(0);
//                }
//            }
//        }
//    }
}
