package searchclient;

import java.util.*;

public class CentralizedPlanner {
    // This class is going to be EXTREMELY fucking complex
    public Blackboard blackboard;
    // Potentially putting the logic of agents selecting between tasks delagating to the agents
    // based on new heuristic
    public HashMap<String, List<Task>> agentToTasks;
    public HashMap<String, Box> boxesNotForGoals;
    // Will tie into potential future work for "ideal" path


    public CentralizedPlanner(Blackboard blackboard) {
        this.blackboard = blackboard;
        // assign the initial tasks to the agents
        assignInitialTasks();
    }

//    public static List<Vertex> findPath(int[][] intMap, double[][] dist, int startX, int startY, int endX, int endY) {
//        int startNode = intMap[startX][startY];
//        int endNode = intMap[endX][endY];
//        int numNodes = intMap.length * intMap[0].length;
//
//        int[] dx = {0, 1, 0, -1}; // Only horizontal and vertical moves
//        int[] dy = {1, 0, -1, 0};
//
//    }

    public static List<Vertex> findPath(int[][] intMap, double[][] dist, int startX, int startY, int endX, int endY) {
        var blackboard = Blackboard.getInstance();
        int numRows = intMap.length;
        int numCols = intMap[0].length;
        List<List<Integer>> possibleMoves = new ArrayList<>() {{
            add(Arrays.asList(0, 1)); // move right
            add(Arrays.asList(1, 0)); // move down
            add(Arrays.asList(0, -1)); // move left
            add(Arrays.asList(-1, 0)); // move up
        }};
        var lengthOfSolution = (int) Utils.getDistance(intMap, dist, startX, startY, endX, endY);
        var moves = new ArrayList<Vertex>();
        var currentVertex = blackboard.getVertex(startX, startY);
        System.err.print(currentVertex);
        for (var i = lengthOfSolution; i != 0; i--) {
            var nextLowestDistance = Integer.MAX_VALUE;
            Vertex nextVertex = null;
            for (var move : possibleMoves) {
                // TODO: isInMap is going to change based on new way to represent the map
                var possibleVertex = blackboard.getVertex(currentVertex.locRow + move.get(0), currentVertex.locCol + move.get(1));
                if (isInMap(possibleVertex, numRows - 1, numCols - 1)) {
//                    System.err.println("endX: " + endX + " endY: " + endY);
                    var newDistance = (int) Utils.getDistance(intMap, dist, possibleVertex.locRow, possibleVertex.locCol, endX, endY);
                    if (newDistance <= nextLowestDistance) {
                        nextLowestDistance = newDistance;
                        nextVertex = possibleVertex;
                    }
                }
            }
            moves.add(nextVertex);
            currentVertex = nextVertex;
        }
        System.err.println("-----------------------\nMoves: \n" + moves);
        return moves;
    }

    // TODO: isInMap is going to change based on new way to represent the map
    public static boolean isInMap(Vertex vertex, int rows, int cols) {
//        System.err.println("rows: " + rows + " cols: " + cols + " vertex: " + vertex);
        return (vertex.locRow > 0 && vertex.locRow < rows) && (vertex.locCol > 0 && vertex.locCol < cols);
    }


    public Vertex getNearestFreeCell(int row, int col) {
        var blackboard = Blackboard.getInstance();
        var temp = blackboard.getVertex(row, col);

        int minDistance = Integer.MAX_VALUE;
        Vertex minVertex = null;
        // from the blackboard unreserved vertices, find the nearest free cell to temp
        for (var vertex : blackboard.unreservedVertices) {
            var distance = (int)blackboard.getDistance(temp.locRow, temp.locCol, vertex.locRow, vertex.locCol);
            if (distance < minDistance) {
                minDistance = distance;
                minVertex = vertex;
            }
        }
        return minVertex;
    }

    // TODO: Part of the Task management needs to be addressed with refactoring this function
    // Assign tasks to Agents; move box to goal, help agent become unstuck, move out of way
    public void assignInitialTasks() {
        // TODO: This will throw a NullPointer if we forget to call it!!!
        this.agentToTasks = new HashMap<>();
        this.boxesNotForGoals = new HashMap<>();


        var blackBoard = Blackboard.getInstance();

        // for each goal find the box that needs to go there
        // from the box determine the agent that can move it to the goal

        //TODO: Task assigning is not working correctly
        for (var goal : blackboard.goals) {
            // if boxes need to go to goals, do this
            if (Character.isLetter(goal.id.charAt(0))) {
                for (var box : blackboard.boxes) {
                    for (var agent : blackboard.agents) {
                        if (agent.color == box.color && Objects.equals(box.id, goal.id)) {
                            Task task = new Task(Task.TaskType.MOVE_BOX_TO_GOAL, Task.Priority.MEDIUM, agent.id, box.id, goal.id, agent.row, agent.col, box.row, box.col, goal.row, goal.col);
                            var temp = this.agentToTasks.get(agent.id);
                            if (temp == null) {
                                this.agentToTasks.put(agent.id, new ArrayList<>() {
                                });
                                this.agentToTasks.get(agent.id).add(task);
                            } else {
                                this.agentToTasks.get(agent.id).add(task);
                            }
                            var agentToBox = findPath(blackboard.intMap, blackboard.dist, agent.row, agent.col, box.row, box.col);
                            var boxToGoal = findPath(blackboard.intMap, blackboard.dist, box.row, box.col, goal.row, goal.col);

                            printDistancesFromCell(blackboard.intMap, blackboard.dist, agent.row, agent.col);

                            printPathOnMap(agentToBox, blackboard.intMap);

//                            System.err.println("Searching For Tunnels:");
//                            var tunnels =  findTunnels(blackBoard.mapRepresentation);
//                            for (var tunnel : tunnels) {
//                                printPathOnMap(tunnel, blackboard.intMap);
//                            }

                            blackboard.reserveVertices(agentToBox);
                            blackboard.reserveVertices(boxToGoal);
                            // This is meant to get the vertices that are not reserved
                            blackboard.verticesNotReserved();
                            blackboard.verticesNotReserved();
                            System.err.println("Agent to Box :  " + agentToBox);
                            System.err.println("Box to Goal :  " + boxToGoal);

//                            printPath(agentToBox, blackboard.intMap);
//                            printPath(BoxToGoal, blackboard.intMap);
                        }
                    }
                }
            }
            if (Character.isDigit(goal.id.charAt(0))) {
                for (var agent : blackboard.agents) {
                    if (Objects.equals(agent.id, goal.id)) {
                        // On the test_constraints2.lvl, this raises the issue that the task assignment is not dynamic
                        Task task = new Task(Task.TaskType.MOVE_TO_DESTINATION, Task.Priority.MEDIUM, agent.id, "", goal.id, agent.row, agent.col, goal.row, goal.col, goal.row, goal.col);
                        // do a logic check on the Task assigned to see if, by chance, an agent is already starting on the goal
                        if (agent.row == goal.row && agent.col == goal.col) {
                            task.type = Task.TaskType.NONE;
                        }
                        var temp = this.agentToTasks.get(agent.id);
                        if (temp == null) {
                            this.agentToTasks.put(agent.id, new ArrayList<>() {
                            });
                            this.agentToTasks.get(agent.id).add(task);
                        } else {
                            this.agentToTasks.get(agent.id).add(task);
                        }
                        var agentToDest = findPath(blackboard.intMap, blackboard.dist, agent.row, agent.col, goal.row, goal.col);
                        printDistancesFromCell(blackboard.intMap, blackboard.dist, agent.row, agent.col);
                        printPathOnMap(agentToDest, blackboard.intMap);

                        blackboard.reserveVertices(agentToDest);
                        // This is meant to get the vertices that are not reserved
                        blackboard.verticesNotReserved();

                        System.err.print("Agent to Destination :  " + agentToDest);

//                        printPath(agentToDest, blackboard.intMap);
//
//                        printPath(agentToDest, blackboard.intMap);
                    }
                }
            }
        }

        // find the boxes that are not for goals
        for (var box : this.blackboard.boxes) {
            boolean isForGoal = false;
            for (var goal : this.blackboard.goals) {
                if (Objects.equals(box.id, goal.id)) {
                    isForGoal = true;
                    break;
                }
            }
            if (!isForGoal) {
                this.boxesNotForGoals.put(box.id, box);
            }
        }

        // find agents that cannot move on turn 1
        // TODO: This needs to be a helper function that is dynamic as the maps change
        for (var agent : blackboard.agents) {
            // TODO: Look at the vertex that agent is on and see if adjacent vertices are free or not
            // If not, see if agent can move the box/agent blocking it or not
            // If not, it is stuck
            // TODO: Need a way to not have an agent cycle directions, but wait for help and then continue on

        }

        // Finds boxes on the reservedVertices that are not for goals and moves them out of the way
        for (var box : boxesNotForGoals.keySet()) {
            var boxVertex = new Vertex(boxesNotForGoals.get(box).row, boxesNotForGoals.get(box).col);
            if (blackboard.reservedVertices.contains(boxVertex)) {
                // find the agent that can move the box
                findAgentMoveBox(boxesNotForGoals.get(box));
            }
        }

        // TODO: I think this needs to be taken out to reflect clearer intent
//        // ASSIGN TASKS TO AGENTS
//        for (var agent: blackboard.agents) {
//            if (agentToTasks.get(agent.id) == null || agentToTasks.get(agent.id).isEmpty()) {
//                System.err.println("Agent " + agent.id + " has no tasks");
//                continue;
//            }
//            var task = agentToTasks.get(agent.id);
//            agent.tasks = task;
//            System.err.println("Agent " + agent.id + " has task " + task);
//        }

        System.err.print("Reserved vertices : \n" + blackboard.reservedVertices);

    }

    public void findAgentMoveBox(Box box) {
        for (var agent : blackboard.agents) {
            if (agent.color == box.color) {
                // TODO: We need to find the closest open cell that is not a goal or obstacle and doesn't block other agents
                var vertex = getNearestFreeCell(box.row, box.col);
                System.err.println("Nearest Vertex: " + vertex);
                if (vertex == null) {
                    System.err.println("No free cell found");
                    return;
                }
                // If an agent is surrounded by more than one box of the same color, the central planner should assign the agent to move one of the boxes out of the way.
                Task task = new Task(Task.TaskType.MOVE_BOX_OUT_OF_WAY, Task.Priority.HIGH, agent.id, box.id, "", agent.row, agent.col, box.row, box.col, vertex.locRow, vertex.locCol);
                var temp = agentToTasks.get(agent.id);
                if (temp == null) {
                    // check if the agent has this task already
                    agentToTasks.put(agent.id, new ArrayList<>());
                    agentToTasks.get(agent.id).add(task);
                } else {
                    // check if the agent has this task already
                    if (!temp.contains(task)) {
                        agentToTasks.get(agent.id).add(task);
                    }
//                    agentToTasks.get(agent.id).add(task);
                }
            }
        }
    }


    public String toString() {
        StringBuilder result = new StringBuilder("Agent to Tasks: ");

        for (var agent : agentToTasks.keySet()) {
            result.append(agent).append(": ");
            for (var task : agentToTasks.get(agent)) {
                result.append(task.toString()).append(", ");
            }
        }
        result.append("\n");

        result.append("Boxes not for goals: ");
        for (var box : boxesNotForGoals.keySet()) {
            result.append(box).append(", ");
        }
        return result.toString();
    }


    // TODO: Change this to use the Adjacent Vertices list
    // DO NOT DELETE THE FOLLOWING
//    public static List<List<Vertex>> findTunnels(List<List<Vertex>> map) {
//        int numRows = map.size();
//        int numCols = map.get(0).size();
//
//        List<List<Vertex>> tunnels = new ArrayList<>();
//
//        boolean[][] visited = new boolean[numRows][numCols];
//
//        // Traverse rows
//        for (int i = 0; i < numRows; i++) {
//            for (int j = 0; j < numCols; j++) {
//                if (visited[i][j] || map.get(i).get(j).isWall) {
//                    continue;
//                }
//
//                // Find the end of the tunnel in this row
//                int endCol = -1;
//                for (int k = j + 1; k < numCols; k++) {
//                    if (visited[i][k] || map.get(i).get(k).isWall) {
//                        endCol = k - 1;
//                        break;
//                    }
//                }
//
//                // No tunnel found in this row
//                if (endCol == -1) {
//                    continue;
//                }
//
//                // Add cells to the tunnel
//                List<Vertex> tunnel = new ArrayList<>();
//                for (int k = j; k <= endCol; k++) {
//                    visited[i][k] = true;
//                    tunnel.add(map.get(i).get(k));
//                }
//
//                tunnels.add(tunnel);
//            }
//        }
//
//        // Traverse columns
//        for (int j = 0; j < numCols; j++) {
//            for (int i = 0; i < numRows; i++) {
//                if (visited[i][j] || map.get(i).get(j).isWall) {
//                    continue;
//                }
//
//                // Find the end of the tunnel in this column
//                int endRow = -1;
//                for (int k = i + 1; k < numRows; k++) {
//                    if (visited[k][j] || map.get(k).get(j).isWall) {
//                        endRow = k - 1;
//                        break;
//                    }
//                }
//
//                // No tunnel found in this column
//                if (endRow == -1) {
//                    continue;
//                }
//
//                // Add cells to the tunnel
//                List<Vertex> tunnel = new ArrayList<>();
//                for (int k = i; k <= endRow; k++) {
//                    visited[k][j] = true;
//                    tunnel.add(map.get(k).get(j));
//                }
//
//                tunnels.add(tunnel);
//            }
//        }
//
//        return tunnels;
//    }


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


    public static void printPathOnMap(List<Vertex> path, int[][] intMap) {
        int numRows = intMap.length;
        int numCols = intMap[0].length;

        // Create a new map with the path marked
        String[][] markedMap = new String[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                if (intMap[i][j] == -1) {
                    markedMap[i][j] = "####";
                } else {
                    markedMap[i][j] = "    ";
                }
            }
        }

        // Mark the cells in the path
        for (Vertex v : path) {
            markedMap[v.locRow][v.locCol] = "___ ";
        }

        // Print the marked map
        System.err.println("Path:");

        // Print column labels
        System.err.print("   ");
        for (int j = 0; j < numCols; j++) {
            System.err.printf("%4d", j);
        }
        System.err.println();

        for (int i = 0; i < numRows; i++) {
            System.err.printf("%3d", i);
            for (int j = 0; j < numCols; j++) {
                System.err.print(markedMap[i][j]);
            }
            System.err.println();
        }
    }

}
