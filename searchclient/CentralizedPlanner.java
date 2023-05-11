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
            // only looks at agents at goal
            if (vertex.cellChar == goal.id.charAt(0)) {
                goalAchievedCount++;
            }
            // only looks at boxes at goal
            if (vertex.boxChar == goal.id.charAt(0)) {
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
            var distance = (int) blackboard.getDistance(temp.locRow, temp.locCol, vertex.locRow, vertex.locCol);
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
                            // TODO: This is the scary part about the assignment. I think ordering is determined on
                            // queue insertion time, but I am not sure. If the next task doesn't line up well, we're fucked
                            // get the adjacent vertices around the box
                            Task boxTask = null;


                            // The idea here is that the new Task Type will tell you the Action calculations needed to be performed, but it does result in overhead
                            Task moveTask = new Task(Task.TaskType.MOVE_AGENT_TO_BOX, Task.Priority.MEDIUM, agent.id, "", goal.id, agent.row, agent.col, box.row, box.col, box.row, box.col);
                            // The problem on the follow task assignment is that we need to "peek" at the suspected path of the box to the goal, and determine if we need to push/pull it
                            List<Vertex> boxPath = findPath(Utils.intMap, Utils.dist, box.row, box.col, goal.row, goal.col);
                            Set boxTarget = Set.copyOf(boxPath);
                            // if the path contains the agent's vertex, do a pull, otherwise push
                            if (boxTarget.contains(blackBoard.getVertex(agent.row, agent.col))) {
                                boxTask = new Task(Task.TaskType.PULL_BOX, Task.Priority.MEDIUM, agent.id, box.id, goal.id, agent.row, agent.col, box.row, box.col, goal.row, goal.col);
                            } else {
                                boxTask = new Task(Task.TaskType.PUSH_BOX, Task.Priority.MEDIUM, agent.id, box.id, goal.id, agent.row, agent.col, box.row, box.col, goal.row, goal.col);
                            }

//                            Task task = new Task(Task.TaskType.MOVE_BOX_TO_GOAL, Task.Priority.MEDIUM, agent.id, box.id, goal.id, agent.row, agent.col, box.row, box.col, goal.row, goal.col);
                            var temp = this.agentToTasks.get(agent.id);
                            if (temp == null) {
                                this.agentToTasks.put(agent.id, new PriorityQueue<>());
                                this.agentToTasks.get(agent.id).add(moveTask);
                                this.agentToTasks.get(agent.id).add(boxTask);
                            } else {
                                this.agentToTasks.get(agent.id).add(moveTask);
                                this.agentToTasks.get(agent.id).add(boxTask);
                            }
                            var agentToBox = findPath(blackboard.intMap, blackboard.dist, agent.row, agent.col, box.row, box.col);
                            var boxToGoal = findPath(blackboard.intMap, blackboard.dist, box.row, box.col, goal.row, goal.col);
                            boxTask.path = boxToGoal;

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

        for (Agent agent : blackboard.agents) {
            // Checks if the agents that don't have tasks assigned to them from initial start get a queue created for them
            // This is extremely important for basic queue management
            if (!agentToTasks.containsKey(agent.id)) {
                agentToTasks.put(agent.id, new PriorityQueue<>());
            }
        }

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
                // If the queue is empty and the agent is not currently doing a task, then add a no op for the agent to the turn actions
                if (agentToTasks.get(agentId).isEmpty() && agent.currentTask == null) {
                    agentTurnActions.add(Action.NoOp);
                    System.err.println("Issuing NoOp for agent " + agentId);
                }
                // If the agent has no task but has one available in the queue, then add the task to the agent's current task
                else if (agent.currentTask == null && !agentToTasks.get(agentId).isEmpty()) {
                    agent.currentTask = agentToTasks.get(agentId).poll();
                    // Make sure the newly assigned task has a path
                    if (agent.currentTask != null) {
                        switch (agent.currentTask.type) {
                            case MOVE_TO_DESTINATION, MOVE_AGENT_OUT_OF_WAY, MOVE_AGENT_TO_BOX:
                                agent.currentTask.path = findPath(blackboard.intMap, blackboard.dist, agent.row, agent.col, agent.currentTask.destinationRow, agent.currentTask.destinationCol);
                                break;
                            case MOVE_BOX_OUT_OF_WAY:
                                break;
                            // Whose perspective are we wanting to look at for the PUSH/PULL box?
                            case PUSH_BOX:
                                Vertex box = blackboard.getVertex(agent.currentTask.taskRow, agent.currentTask.taskCol);
                                agent.currentTask.path = findPath(blackboard.intMap, blackboard.dist, box.locRow, box.locCol, agent.currentTask.destinationRow, agent.currentTask.destinationCol);
                                break;
                            case PULL_BOX:
                                Vertex box2 = blackboard.getVertex(agent.currentTask.taskRow, agent.currentTask.taskCol);
                                agent.currentTask.path = findPath(blackboard.intMap, blackboard.dist, box2.locRow, box2.locCol, agent.currentTask.destinationRow, agent.currentTask.destinationCol);
                            case MOVE_BOX_TO_GOAL:
                                agent.currentTask.path = findPath(blackboard.intMap, blackboard.dist, agent.row, agent.col, agent.currentTask.destinationRow, agent.currentTask.destinationCol);
                                break;
                            default:
                                break;
                        }
                        blackboard.agents.get(Character.digit(agentId.charAt(0), 16)).currentTask.agentRow = agent.row;
                        blackboard.agents.get(Character.digit(agentId.charAt(0), 16)).currentTask.agentCol = agent.col;
                        blackboard.agents.get(Character.digit(agentId.charAt(0), 16)).currentTask.actionsForPath = PathToActionsTranslator.translatePath(blackboard.agents.get(Character.digit(agentId.charAt(0), 16)).currentTask);
                        // take the first action for the new task and add it to the turn actions
//                        agentTurnActions.add(agent.currentTask.actionsForPath.get(0).getRight());
                        // remove the first action from the task's actions
//                        agent.currentTask.actionsForPath.remove(0);
                    }
                }
                if (agent.currentTask != null && agent.currentTask.actionsForPath != null && !agent.currentTask.actionsForPath.isEmpty()) {
                    Action currentTurnAction = agent.currentTask.actionsForPath.get(0).getRight();
                    Vertex currentVertex = agent.currentTask.actionsForPath.get(0).getLeft();
                    Vertex nextVertex = agent.currentTask.actionsForPath.get(0).getMiddle();

                    // TODO: None of the actions are checked if they are actually allowed or not, this should cause conflicts if not
                    // I think this is where we, once again, have to do a switch statement based on the agent's current task
                    switch (agent.currentTask.type) {
                        case MOVE_TO_DESTINATION, MOVE_AGENT_OUT_OF_WAY, MOVE_AGENT_TO_BOX:
                            // check if this action is allowed
                            boolean result = isApplicable(Integer.parseInt(agent.id), currentTurnAction);
                            if (!result) {
                                throw new RuntimeException("Action is not allowed");
                            }
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
                            break;
                        case MOVE_BOX_OUT_OF_WAY:
                            break;
                        // I am pretty sure the deltas also work the same here for both Pushing and Pulling
                        case MOVE_BOX_TO_GOAL, PUSH_BOX, PULL_BOX:
                            // check if this action is allowed
                            boolean result2 = isApplicable(Integer.parseInt(agent.id), currentTurnAction);
                            if (!result2) {
                                throw new RuntimeException("Action is not allowed");
                            }
                            // add the turn for this move
                            agentTurnActions.add(currentTurnAction);
                            // This is going to be a naive way to do this, but we need to get the box that is near the current agent
                            List<Vertex> verticesAdjacentToAgent = blackboard.mapRepresentation.getAdjVertices(agent.row, agent.col);
                            Vertex boxVertex = null;
                            for (Vertex v : verticesAdjacentToAgent) {
                                if (v.boxChar == agent.currentTask.boxId.charAt(0)) {
                                    boxVertex = v;
                                    break;
                                }
                            }
                            Box box = blackboard.getBox(boxVertex.locRow, boxVertex.locCol);
                            // set old value to a null character for the agent
                            blackboard.getVertex(currentVertex.locRow, currentVertex.locCol).cellChar = '\0';
                            // set old value to a null character for the box
                            blackboard.getVertex(box.row, box.col).boxChar = '\0';
                            // move agent on the blackboard
                            agent.row += currentTurnAction.agentRowDelta;
                            agent.col += currentTurnAction.agentColDelta;
                            // move the box on the blackboard
                            box.row += currentTurnAction.boxRowDelta;
                            box.col += currentTurnAction.boxColDelta;
                            // set new value to the agent's id
                            blackboard.getVertex(agent.row, agent.col).cellChar = agent.id.charAt(0);
                            // set new value to the box's id
                            blackboard.getVertex(box.row, box.col).boxChar = box.id.charAt(0);

                            // remove the action from the list for the agent
                            agent.currentTask.actionsForPath.remove(0);
                            break;
                        default:
                            break;
                    }
                }
                else if (agent.currentTask != null && agent.currentTask.actionsForPath != null && !agentToTasks.get(agentId).isEmpty())
                {
                    agent.currentTask = agentToTasks.get(agentId).poll();
                    switch (agent.currentTask.type) {
                        case MOVE_TO_DESTINATION, MOVE_AGENT_OUT_OF_WAY, MOVE_AGENT_TO_BOX:
                            agent.currentTask.path = findPath(blackboard.intMap, blackboard.dist, agent.row, agent.col, agent.currentTask.destinationRow, agent.currentTask.destinationCol);
                            agent.currentTask.actionsForPath = PathToActionsTranslator.translatePath(blackboard.agents.get(Character.digit(agentId.charAt(0), 16)).currentTask);
                            Action currentTurnAction = agent.currentTask.actionsForPath.get(0).getRight();
                            Vertex currentVertex = agent.currentTask.actionsForPath.get(0).getLeft();

                            boolean result = isApplicable(Integer.parseInt(agent.id), currentTurnAction);
                            if (!result) {
                                throw new RuntimeException("Action is not allowed");
                            }
                            agentTurnActions.add(currentTurnAction);

                            blackboard.getVertex(currentVertex.locRow, currentVertex.locCol).cellChar = '\0';
                            // move agent on the blackboard
                            agent.row += currentTurnAction.agentRowDelta;
                            agent.col += currentTurnAction.agentColDelta;
                            // set new value to the agent's id
                            blackboard.getVertex(agent.row, agent.col).cellChar = agent.id.charAt(0);

                            // remove the action from the list for the agent
                            agent.currentTask.actionsForPath.remove(0);
                            break;
                        case MOVE_BOX_OUT_OF_WAY:
                            break;
                        // Same thing here, should be the same
                        case PUSH_BOX:
                            Vertex box = blackboard.getVertex(agent.currentTask.taskRow, agent.currentTask.taskCol);
                            agent.currentTask.path = findPath(blackboard.intMap, blackboard.dist, box.locRow, box.locCol, agent.currentTask.destinationRow, agent.currentTask.destinationCol);
                            agent.currentTask.actionsForPath = PathToActionsTranslator.translatePath(blackboard.agents.get(Character.digit(agentId.charAt(0), 16)).currentTask);
                            Action currentTurnAction2 = agent.currentTask.actionsForPath.get(0).getRight();
                            Vertex currentVertex2 = agent.currentTask.actionsForPath.get(0).getLeft();

                            boolean result2 = isApplicable(Integer.parseInt(agent.id), currentTurnAction2);
                            if (!result2) {
                                throw new RuntimeException("Action is not allowed");
                            }

                            agentTurnActions.add(currentTurnAction2);
                            // This is going to be a naive way to do this, but we need to get the box that is near the current agent
                            List<Vertex> verticesAdjacentToAgent = blackboard.mapRepresentation.getAdjVertices(agent.row, agent.col);
                            Vertex boxVertex = null;
                            for (Vertex v : verticesAdjacentToAgent) {
                                if (v.boxChar == agent.currentTask.boxId.charAt(0)) {
                                    boxVertex = v;
                                    break;
                                }
                            }
                            Box blackboardBox = blackboard.getBox(boxVertex.locRow, boxVertex.locCol);
                            // set old value to a null character for the agent
                            blackboard.getVertex(currentVertex2.locRow, currentVertex2.locCol).cellChar = '\0';
                            // set old value to a null character for the box
                            blackboard.getVertex(blackboardBox.row, blackboardBox.col).boxChar = '\0';
                            // move agent on the blackboard
                            agent.row += currentTurnAction2.agentRowDelta;
                            agent.col += currentTurnAction2.agentColDelta;
                            // move the box on the blackboard
                            blackboardBox.row += currentTurnAction2.boxRowDelta;
                            blackboardBox.col += currentTurnAction2.boxColDelta;
                            // set new value to the agent's id
                            blackboard.getVertex(agent.row, agent.col).cellChar = agent.id.charAt(0);
                            // set new value to the box's id
                            blackboard.getVertex(blackboardBox.row, blackboardBox.col).boxChar = blackboardBox.id.charAt(0);

                            // remove the action from the list for the agent
                            agent.currentTask.actionsForPath.remove(0);
                            break;
                        case PULL_BOX:
                            Vertex box2 = blackboard.getVertex(agent.currentTask.taskRow, agent.currentTask.taskCol);
                            agent.currentTask.path = findPath(blackboard.intMap, blackboard.dist, box2.locRow, box2.locCol, agent.currentTask.destinationRow, agent.currentTask.destinationCol);
                            agent.currentTask.actionsForPath = PathToActionsTranslator.translatePath(blackboard.agents.get(Character.digit(agentId.charAt(0), 16)).currentTask);
                            Action currentTurnAction3 = agent.currentTask.actionsForPath.get(0).getRight();
                            Vertex currentVertex3 = agent.currentTask.actionsForPath.get(0).getLeft();

                            agentTurnActions.add(currentTurnAction3);
                            // This is going to be a naive way to do this, but we need to get the box that is near the current agent
                            List<Vertex> verticesAdjacentToAgent2 = blackboard.mapRepresentation.getAdjVertices(agent.row, agent.col);
                            Vertex boxVertex2 = null;
                            for (Vertex v : verticesAdjacentToAgent2) {
                                if (v.boxChar == agent.currentTask.boxId.charAt(0)) {
                                    boxVertex2 = v;
                                    break;
                                }
                            }
                            Box blackboardBox2 = blackboard.getBox(boxVertex2.locRow, boxVertex2.locCol);
                            // set old value to a null character for the agent
                            blackboard.getVertex(currentVertex3.locRow, currentVertex3.locCol).cellChar = '\0';
                            // set old value to a null character for the box
                            blackboard.getVertex(blackboardBox2.row, blackboardBox2.col).boxChar = '\0';
                            // move agent on the blackboard
                            agent.row += currentTurnAction3.agentRowDelta;
                            agent.col += currentTurnAction3.agentColDelta;
                            // move the box on the blackboard
                            blackboardBox2.row += currentTurnAction3.boxRowDelta;
                            blackboardBox2.col += currentTurnAction3.boxColDelta;
                            // set new value to the agent's id
                            blackboard.getVertex(agent.row, agent.col).cellChar = agent.id.charAt(0);
                            // set new value to the box's id
                            blackboard.getVertex(blackboardBox2.row, blackboardBox2.col).boxChar = blackboardBox2.id.charAt(0);

                            // remove the action from the list for the agent
                            agent.currentTask.actionsForPath.remove(0);
                            break;
//                        case MOVE_BOX_TO_GOAL:
//                            // This should involve the agents location
//                            // TODO: WE'VE REALLY FUCKED THINGS UP WITH THIS
//
//                            // We have to get the box here
//                            Vertex box = blackboard.getVertex(agent.currentTask.taskRow, agent.currentTask.taskCol);
//                            agent.currentTask.path = findPath(blackboard.intMap, blackboard.dist, box.locRow, box.locCol, agent.currentTask.destinationRow, agent.currentTask.destinationCol);
//                            agent.currentTask.actionsForPath = PathToActionsTranslator.translatePath(blackboard.agents.get(Character.digit(agentId.charAt(0), 16)).currentTask);
//                            Action currentTurnAction2 = agent.currentTask.actionsForPath.get(0).getRight();
//                            Vertex currentVertex2 = agent.currentTask.actionsForPath.get(0).getLeft();
//
//                            agentTurnActions.add(currentTurnAction2);
//                            // This is going to be a naive way to do this, but we need to get the box that is near the current agent
//                            List<Vertex> verticesAdjacentToAgent = blackboard.mapRepresentation.getAdjVertices(agent.row, agent.col);
//                            Vertex boxVertex = null;
//                            for (Vertex v : verticesAdjacentToAgent) {
//                                if (v.boxChar == agent.currentTask.boxId.charAt(0)) {
//                                    boxVertex = v;
//                                    break;
//                                }
//                            }
//                            Box blackboardBox = blackboard.getBox(boxVertex.locRow, boxVertex.locCol);
//                            // set old value to a null character for the agent
//                            blackboard.getVertex(currentVertex2.locRow, currentVertex2.locCol).cellChar = '\0';
//                            // set old value to a null character for the box
//                            blackboard.getVertex(blackboardBox.row, blackboardBox.col).boxChar = '\0';
//                            // move agent on the blackboard
//                            agent.row += currentTurnAction2.agentRowDelta;
//                            agent.col += currentTurnAction2.agentColDelta;
//                            // move the box on the blackboard
//                            blackboardBox.row += currentTurnAction2.boxRowDelta;
//                            blackboardBox.col += currentTurnAction2.boxColDelta;
//                            // set new value to the agent's id
//                            blackboard.getVertex(agent.row, agent.col).cellChar = agent.id.charAt(0);
//                            // set new value to the box's id
//                            blackboard.getVertex(blackboardBox.row, blackboardBox.col).boxChar = blackboardBox.id.charAt(0);
//
//                            // remove the action from the list for the agent
//                            agent.currentTask.actionsForPath.remove(0);
//                            break;
                        default:
                            break;
                    }

                    System.err.println("Agent " + agentId + " has a new task " + agent.currentTask.type);
                }
                // handles when an agent still has a notion of the task, but is complete and no tasks pending in the queue
                else if (agent.currentTask != null && agent.currentTask.actionsForPath != null && agent.currentTask.actionsForPath.isEmpty() && agentToTasks.get(agentId).isEmpty()) {
                    agent.currentTask = null;
                    agentTurnActions.add(Action.NoOp);
                }
            }
            allActions.add(agentTurnActions);
            if (isGoalRepresentation()) {
                act = allActions.stream().map(l -> l.toArray(new Action[0])).toArray(Action[][]::new);
                return act;
            }
        }
    }

    private boolean isApplicable(int agentId, Action action) {
        Blackboard blackboard = Blackboard.getInstance();
        Agent agent = blackboard.agents.get(agentId);
        // I want a copy of the agent and now the underlying agent
        Agent copyAgent = new Agent(agent.id, agent.color, agent.row, agent.col);


//        int agentRow = this.agentRows[agent];
//        int agentCol = this.agentCols[agent];
//        Color agentColor = State.agentColors[agent];
        int boxRow;
        int boxCol;
        char box;
        int destinationRow;
        int destinationCol;
        switch (action.type) {
            case NoOp:
                return true;

            case Move:
                destinationRow = copyAgent.row + action.agentRowDelta;
                destinationCol = copyAgent.col + action.agentColDelta;
                return this.cellIsFree(destinationRow, destinationCol);

            case Push:
                //Get the "old" coordinates of the box that has been pushed
                boxRow = copyAgent.row + action.agentRowDelta;
                boxCol = copyAgent.col + action.agentColDelta;

                //Get Name (char) of Box at the "old" position
                box = boxAt(boxRow, boxCol);

                //Check if that position was marked with a box indicator and check if the agent is approved (colour check) to move the box.
                //convert box from letter into number A = 0, B = 1, etc.
                if (box == '\0' || State.boxColors[box - 'A'] != copyAgent.color) {
                    return false;
                }

                //Confirm that this move was legal by checking the constraints of walls and other obstacles in the grid.
                return this.cellIsFree(boxRow + action.boxRowDelta, boxCol + action.boxColDelta);

            case Pull:
                //Get the "old" coordinates of the box that has been pulled
                boxRow = copyAgent.row - action.boxRowDelta;
                boxCol = copyAgent.col - action.boxColDelta;
                box = boxAt(boxRow, boxCol);

                //Check if that position was marked with a box indicator and check if the agent is approved (colour check) to move the box.
                //convert box from letter into number A = 0, B = 1, etc.
                if (box == '\0' || State.boxColors[box - 'A'] != copyAgent.color) {
                    return false;
                }

                //Calculate the new row position of the agent
                destinationRow = copyAgent.row + action.agentRowDelta;
                destinationCol = copyAgent.col + action.agentColDelta;

                //Confirm that this move is legal by checking the constraints of walls and other obstacles in the grid.
                return this.cellIsFree(destinationRow, destinationCol);
        }
        // Unreachable:
        return false;
    }

    private boolean isConflicting(List<Action> jointAction)
    {
//        int numAgents = this.agentRows.length;
        Blackboard blackboard = Blackboard.getInstance();
        int numAgents = blackboard.agents.size();

        int[] destinationRows = new int[numAgents]; // row of new cell to become occupied by action
        int[] destinationCols = new int[numAgents]; // column of new cell to become occupied by action
        int[] boxRows = new int[numAgents]; // current row of box moved by action
        int[] boxCols = new int[numAgents]; // current column of box moved by action
        int[] destinationRowsBoxes = new int[numAgents]; // row of new cell to become occupied by action
        int[] destinationColsBoxes = new int[numAgents]; // row of new cell to become occupied by action

        // Collect cells to be occupied and boxes to be moved
        for (int i = 0; i < numAgents; ++i)
        {
            Action action = jointAction.get(i);
            Agent agent = blackboard.agents.get(i);
            Agent copyAgent = new Agent(agent.id, agent.color, agent.row, agent.col);
            int boxRow;
            int boxCol;

            destinationRows[i] = copyAgent.row + action.agentRowDelta;
            destinationCols[i] = copyAgent.col + action.agentColDelta;

            switch (action.type)
            {
                case NoOp:

                    break;

                case Move:

                    boxRows[i] = copyAgent.row; // Distinct dummy value
                    boxCols[i] = copyAgent.col; // Distinct dummy value
                    destinationRowsBoxes[i] = copyAgent.row + action.agentRowDelta;
                    destinationColsBoxes[i] = copyAgent.col + action.agentColDelta;

                    break;

                case Push:
                    // Current row of the box is the destination of the agent
                    boxRows[i] = destinationRows[i];
                    boxCols[i] = destinationCols[i];
                    // destination row of the box is destionation of agent plus the action offset
                    destinationRowsBoxes[i] = destinationRows[i] + action.boxRowDelta;
                    destinationColsBoxes[i] = destinationCols[i] + action.boxColDelta;

                    break;

                case Pull:
                    //current row of the box is current row of the agent reversed with the actions
                    boxRows[i] = copyAgent.row + action.boxRowDelta;
                    boxCols[i] = copyAgent.col + action.boxColDelta;
                    //destination row of the box is the current row of the agent
                    destinationRowsBoxes[i] = copyAgent.row;
                    destinationColsBoxes[i] = copyAgent.col;

                    break;
            }
        }

        for (int a1 = 0; a1 < numAgents; ++a1)
        {
            if (jointAction.get(a1) == Action.NoOp)
            {
                continue;
            }

            for (int a2 = a1 + 1; a2 < numAgents; ++a2)
            {
                if (jointAction.get(a2) == Action.NoOp)
                {
                    continue;
                }

                // Moving into same cell? I think this works for both agent and the boxes so no need to write
                // another check for the boxes
                if (destinationRows[a1] == destinationRows[a2] && destinationCols[a1] == destinationCols[a2])
                {
                    return true;
                }
                // I disagree now with what I wrote above, below statement checks i
                if ((destinationRows[a1] == destinationRowsBoxes[a2] && destinationCols[a1] == destinationColsBoxes[a2]) || (destinationRowsBoxes[a1] == destinationRows[a2] && destinationColsBoxes[a1] == destinationCols[a2]))
                {
                    return true;
                }
                // checks if two boxes are moving into the same sell
                if (destinationRowsBoxes[a1] == destinationRowsBoxes[a2] && destinationColsBoxes[a1] == destinationColsBoxes[a2])
                {
                    return true;
                }
                /* Checks if agents are moving the same box
                boxrows current row of box moved by action
                boxcols current column of box moved by action
                */
                if(boxRows[a1] == boxRows[a2] && boxCols[a1] == boxCols[a2]){
                    return true;
                }
            }
        }

        return false;
    }

    private boolean cellIsFree(int row, int col)
    {
//        return !this.walls[row][col] && this.boxes[row][col] == 0 && this.agentAt(row, col) == 0;
         return boxAt(row, col) == '\0' && agentAt(row, col) == '\0';
    }

    private char agentAt(int row, int col)
    {
        Blackboard blackboard = Blackboard.getInstance();
        Vertex vertex = blackboard.getVertex(row, col);
        return vertex.cellChar;
    }

    private char boxAt(int row, int col)
    {
        Blackboard blackboard = Blackboard.getInstance();
        Vertex vertex = blackboard.getVertex(row, col);
        return vertex.boxChar;
    }

    @Override
    public void updateBlackboard() {
        var blackboard = Blackboard.getInstance();

    }
}
