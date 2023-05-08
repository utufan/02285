package searchclient;

import java.util.*;

public class CentralizedPlanner implements KnowledgeSource {
    // This class is going to be EXTREMELY fucking complex
    public Blackboard blackboard;
    // Potentially putting the logic of agents selecting between tasks delagating to the agents
    // based on new heuristic
    public HashMap<String, PriorityQueue<Task>> agentToTasks;
//    public HashMap<String, Box> boxesNotForGoals;
    // Will tie into potential future work for "ideal" path

//    public HashMap<Character, Task> agentToCurrentExecutingTask = new HashMap<>();

    // This needs to be the time step to the actions agents take in it
    public List<List<Action>> jointActions = new ArrayList<>();
    // for the task queue management work
//    HashMap<Character, PriorityQueue<Task>> agentQueues = new HashMap<>();


    public CentralizedPlanner(Blackboard blackboard) {
        this.blackboard = blackboard;
        // assign the initial tasks to the agents
        // TODO: The following function needs to be massively reworked
        assignInitialTasks();
    }

    public boolean isGoalRepresentation() {
        var blackboard = Blackboard.getInstance();
        int goalAchievedCount = 0;

        for (Goal goal : blackboard.goals) {
            var vertex = blackboard.getVertex(goal.row, goal.col);
            if (vertex.cellChar == goal.id.charAt(0)) {
                goalAchievedCount++;
            }
        }

        if (goalAchievedCount == blackboard.goals.size()) {
            return true;
        }

        return false;
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
        moves.add(currentVertex);
        System.err.print(currentVertex);
        for (var i = lengthOfSolution; i != 0; i--) {
            var nextLowestDistance = Integer.MAX_VALUE;
            Vertex nextVertex = null;
            for (var move : possibleMoves) {

                // TODO: isInMap is going to change based on new way to represent the map
                var possibleVertex = blackboard.getVertex(currentVertex.locRow + move.get(0), currentVertex.locCol + move.get(1));
                if (possibleVertex == null) {
                    continue;
                }
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
//        this.boxesNotForGoals = new HashMap<>();




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
                                this.agentToTasks.put(agent.id, new PriorityQueue<>());
                                this.agentToTasks.get(agent.id).add(task);
                            } else {
                                this.agentToTasks.get(agent.id).add(task);
                            }
                            var agentToBox = findPath(blackboard.intMap, blackboard.dist, agent.row, agent.col, box.row, box.col);
                            var boxToGoal = findPath(blackboard.intMap, blackboard.dist, box.row, box.col, goal.row, goal.col);
                            task.path = boxToGoal;

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
                            task.path = new ArrayList<>();
                        }
                        var temp = this.agentToTasks.get(agent.id);
                        if (temp == null) {
                            this.agentToTasks.put(agent.id, new PriorityQueue<>());
                            this.agentToTasks.get(agent.id).add(task);
                        } else {
                            this.agentToTasks.get(agent.id).add(task);
                        }
                        var agentToDest = findPath(blackboard.intMap, blackboard.dist, agent.row, agent.col, goal.row, goal.col);
                        printDistancesFromCell(blackboard.intMap, blackboard.dist, agent.row, agent.col);

                        // As part of the path to actions
                        task.path = agentToDest;

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

        // TODO: Something is fucked up with this, but I'm not sure what :thinking:
//        for (Agent agent : blackboard.agents) {
//            for (var entry : agentToTasks.keySet()) {
//                if (!entry.contains(agent.id)) {
//                    agentToTasks.put(agent.id, new PriorityQueue<>());
//                }
//            }
//        }

//        // find the boxes that are not for goals
//        for (var box : this.blackboard.boxes) {
//            boolean isForGoal = false;
//            for (var goal : this.blackboard.goals) {
//                if (Objects.equals(box.id, goal.id)) {
//                    isForGoal = true;
//                    break;
//                }
//            }
//            if (!isForGoal) {
//                this.boxesNotForGoals.put(box.id, box);
//            }
//        }

        // find agents that cannot move on turn 1
        // TODO: This needs to be a helper function that is dynamic as the maps change
        for (var agent : blackboard.agents) {
            // TODO: Look at the vertex that agent is on and see if adjacent vertices are free or not
            // If not, see if agent can move the box/agent blocking it or not
            // If not, it is stuck
            // TODO: Need a way to not have an agent cycle directions, but wait for help and then continue on

        }

        // TODO: Should we need to create tasks to move boxes turn 1, this needs fixed
        // Finds boxes on the reservedVertices that are not for goals and moves them out of the way
//        for (var box : boxesNotForGoals.keySet()) {
//            var boxVertex = new Vertex(boxesNotForGoals.get(box).row, boxesNotForGoals.get(box).col);
//            if (blackboard.reservedVertices.contains(boxVertex)) {
//                // find the agent that can move the box
//                findAgentMoveBox(boxesNotForGoals.get(box));
//            }
//        }

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
                    agentToTasks.put(agent.id, new PriorityQueue<>());
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

//        result.append("Boxes not for goals: ");
//        for (var box : boxesNotForGoals.keySet()) {
//            result.append(box).append(", ");
//        }
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


    public Action[][] execute() {
        List<List<Action>> allActions = new ArrayList<>();
        // TODO:
        // Keep track of the actions for each agent per time step
        // Continue to plan until all tasks are completed and goal map representation is reached
        // If we are not in the goal map representation, then we need to continue to plan, execute and resolve conflicts

        // Need a way to check if the goal map representation is reached via Graph
        // Need a way to check if all tasks are completed
        // TODO: The following one is one of the more needed ones outside of the Conflict Resolution
        // Need a way to generate and manage tasks
        // Detect conflicts between agents' actions
        // Need a way to resolve conflicts
        // Need a way to generate the god awful permutations of actions

        if (!isGoalRepresentation()) {
            System.err.println("Not in goal representation");
        }

        Action[][] act = null;

        while (true) {
            List<Action> agentTurnActions = new ArrayList<>();
            // makes sure an agents queue is not empty for a timestep
            for (String agentId : this.agentToTasks.keySet()) {
                Agent agent = blackboard.agents.get(Character.digit(agentId.charAt(0), 16));
                if (agentToTasks.get(agentId).isEmpty() && agent.currentTask == null)
                {
//                    Agent agent = blackboard.agents.get(Character.digit(agentId.charAt(0), 16));
//                    agentToTasks.get(agentId).add(new Task(Task.TaskType.NONE, Task.Priority.LOW, agentId, null, null, agent.row, agent.col, agent.row, agent.col, agent.row, agent.col));
//                    agent.currentTask = agentToTasks.get(agentId).poll();
                    agentTurnActions.add(Action.NoOp);
                }
                if (blackboard.agents.get(Character.digit(agentId.charAt(0), 16)).currentTask == null) {
                    var pendingTask = agentToTasks.get(agentId).poll();
                    if (pendingTask == null) {
//                        blackboard.agents.get(Character.digit(agentId.charAt(0), 16)).currentTask = new Task(Task.TaskType.NONE, Task.Priority.LOW, agentId, null, null, agent.row, agent.col, agent.row, agent.col, agent.row, agent.col);
//                        agentToTasks.get(agentId).add(new Task(Task.TaskType.NONE, Task.Priority.LOW, agentId, null, null, agent.row, agent.col, agent.row, agent.col, agent.row, agent.col));
                        continue;
                    } else {
                        blackboard.agents.get(Character.digit(agentId.charAt(0), 16)).currentTask = pendingTask;
                        var actions = PathToActionsTranslator.translatePath(blackboard.agents.get(Character.digit(agentId.charAt(0), 16)).currentTask);

                        blackboard.agents.get(Character.digit(agentId.charAt(0), 16)).currentTask.actionsForPath = actions;
                    }
                }
                if (agent.currentTask != null && agent.currentTask.actionsForPath != null && !agent.currentTask.actionsForPath.isEmpty()) {
                    Action currentTurnAction = agent.currentTask.actionsForPath.get(0).getRight();
                    Vertex currentVertex = agent.currentTask.actionsForPath.get(0).getLeft();
                    if (currentTurnAction != null && currentVertex != null) {
                        // add the turn for this move
                        agentTurnActions.add(currentTurnAction);
                        // set old value to a null character
                        blackboard.getVertex(currentVertex.locRow, currentVertex.locCol).cellChar = '\0';
                        // move agent on the blackboard
                        agent.row += currentTurnAction.agentRowDelta;
                        agent.col += currentTurnAction.agentColDelta;
                        // set new value to the agent's id
                        blackboard.getVertex(agent.row, agent.col).cellChar = agent.id.charAt(0);

                        // remove the action from the list for the agent
                        agent.currentTask.actionsForPath.remove(0);
//                        var temp = new ArrayList<Agent>();
//                        temp.add(agent);

//                        blackboard.updateBlackboard(temp);
//                        agent.currentTask.actionsForPath = agent.currentTask.actionsForPath;
                    }
                } else {
                    agent.currentTask = agentToTasks.get(agentId).poll();
                }
            }

//            for (var action : agentTurnActions) {
//                allActions.add(new ArrayList<>(Collections.singletonList(action)));
//            }

            allActions.add(agentTurnActions);

//            act = agentTurnActions.toArray(new Action[0][0]);
//            act = agentTurnActions.stream().map(l -> l.toArray(new Action[0])).toArray(Action[][]::new);

            if (isGoalRepresentation()) {
                act = allActions.stream().map(l -> l.toArray(new Action[0])).toArray(Action[][]::new);
                return act;
            }

//            break;

        }

        // TODO: CLEAN UP THIS CODE
        // Make actions a single list rather than a collection
//        HashMap<Character, List<Action>> actionsForAgents = new HashMap<>();
//        for(var agent: this.agentToTasks.entrySet()) {
//            // get the agent to update
//            Agent blackboardAgent = blackboard.agents.get(Character.digit(agent.getKey().charAt(0), 16));
//            actionsForAgents.putIfAbsent(agent.getKey().charAt(0), new ArrayList<>());
//            for (var task : agent.getValue()) {
//                var actions = PathToActionsTranslator.translatePath(task);
//                for (var action : actions) {
//                    actionsForAgents.get(agent.getKey().charAt(0)).add(action.getRight());
//                    // Update agent positioning based on row and the character on it
//                    blackboard.getVertex(blackboardAgent.row, blackboardAgent.col).cellChar = '\0';
//                    blackboardAgent.row += action.getRight().agentRowDelta;
//                    blackboardAgent.col += action.getRight().agentColDelta;
//                    var temp = new ArrayList<Agent>();
//                    temp.add(blackboardAgent);
//                    blackboard.updateBlackboard(temp);
//                }
//            }
//        }
//
//        for (Action action : actionsForAgents.get('0')) {
//            allActions.add(new ArrayList<>(Collections.singletonList(action)));
//        }
//
//        if (isGoalRepresentation()) {
//            System.err.println("In goal representation");
//        }
//
//        Action[][] act = allActions.stream().map(l -> l.toArray(new Action[0])).toArray(Action[][]::new);

//        Action[][] act = actionsForAgents.get('0').stream().map(l -> l.toArray(new Action[0])).toArray(Action[][]::new);

//        return act;

//        while (!isGoalRepresentation()) {
//
//            for (var agent : agentToCurrentExecutingTask.entrySet()) {
//                if (agent.getValue().path.size() == 0) {
//                    continue;
//                }
//            }
//        }


//        var blackboard = Blackboard.getInstance();
//        var numAgents = blackboard.agents.size();
//
//        List<List<Action>> actions = new ArrayList<>();

        // this is a naive implementation
        // You need to get the actions for each agent and execute them in order in the same time step
//        for (var agent : actionsForAgents.entrySet()) {
//            if (agent.getValue().size() == 0) {
//                continue;
//            }
//            for (var action : agent.getValue()) {
////                for (var action : action) {
//                    Agent blackboardAgent = blackboard.agents.get(Character.digit(agent.getKey(), 16));
//                    blackboardAgent.row += action.agentRowDelta;
//                    blackboardAgent.col += action.agentColDelta;
//                    // The scary part is now here
//                    var temp = new ArrayList<Agent>();
//                    temp.add(blackboardAgent);
//
//                    blackboard.updateBlackboard(temp);
//                    Action[] actionArray = new Action[]{action};
//                    actions.add(Collections.singletonList(actionArray[0]));
////                }
//
//                Action[][] act = actions.stream().map(l -> l.toArray(new Action[0])).toArray(Action[][]::new);
//
//                return act;
//
//            }
//
//        }
//        return null;
    }

    @Override
    public void updateBlackboard() {
        var blackboard = Blackboard.getInstance();

    }
}
