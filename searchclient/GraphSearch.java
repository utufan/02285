package searchclient;

import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

public class GraphSearch {

    public static Action[][] search(State initialState, Frontier frontier) {
        //Part 2:
        //Now try to implement the Graph-Search algorithm from R&N figure 3.7
        //In the case of "failure to find a solution" you should return null.
        //Some useful methods on the state class which you will need to use are:
        //state.isGoalState() - Returns true if the state is a goal state.
        //state.extractPlan() - Returns the Array of actions used to reach this state.
        //state.getExpandedStates() - Returns an ArrayList<State> containing the states reachable from the current state.
        //You should also take a look at Frontier.java to see which methods the Frontier interface exposes
        //
        //printSearchStates(expanded, frontier): As you can see below, the code will print out status
        //(#expanded states, size of the frontier, #generated states, total time used) for every 10000th node generated.
        //You should also make sure to print out these stats when a solution has been found, so you can keep
        //track of the exact total number of states generated!!


        //  This is where I think the preprocessing should be done because this is the initial state.
        // If we have some concept of a Planner, this is where it should first start.
        // THIS IS ONLY FOR THE INITIAL STATE, THEY BECOME STALE IF NOT UPDATED IN THE SEARCH
        List<Agent> agents = new ArrayList<>();
        List<Box> boxes = new ArrayList<>();
        List<Goal> initialGoals = new ArrayList<>();

        // Print everything in initialState
        System.err.println("Initial State: " + initialState);
        System.err.println("Goal Ordering: " + Utils.goalMapRepresentation.determine_goal_ordering(Utils.goalMapRepresentation.adjVertices));

        Preprocessing preprocessing = new Preprocessing(initialState);

        CentralizedPlanner planner = new CentralizedPlanner(preprocessing.initializeBlackboard(Utils.intMap, Utils.dist, Utils.initialMapRepresentation));

        System.err.println("Planner: " + planner);

//        System.err.println("Actions for agents: " + actionsForAgents);

        var temp = planner.execute();

        return temp;

//        System.err.println("Temp: " + temp);
//
////        for (var tasks : planner.agentToTasks.values()) {
////            for (var task : tasks) {
////                actionsForAgents.add(PathToActionsTranslator.translatePath(task));
////                System.err.println("Task: " + task);
////                System.err.println("Actions: " + actions);
////            }
////        }
//
//
//        for (int i = 0; i < initialState.agentRows.length; i++) {
//            // This is very backwards, but if we look at the Agent Rows/Cols length, that tells us how many agents
//            // there are on the map
//            // Each index corresponds to a unique agent, so you cannot have multiple Agent 0's, etc
//            // This index also seemingly corresponds to the AgentColors from the static variable in State; how lucky
//            // MORE SPAGHET BECAUSE OF IMPROPER STRUCTURE OF INPUT DATA
//            agents.add(new Agent(Integer.toString(i), State.agentColors[i], initialState.agentRows[i], initialState.agentCols[i]));
//        }
//
//        System.err.println("Agents: " + agents);
//
//        // Now that we get the individual box information, NOW we have to find their characters on the board...
//        // This is only on the initial state, I have no idea what should be done for subsequent states
//        // 30 Mar: This is some EXTREMELY spaghetti code for this. Essentially, it gets ALL the boxes on the map
//        // from initial state, gets the colors of them, their ID, and their initial location. I would recommend using these
//        // for the planner.
//        for (int i = 0; i < initialState.boxes.length; i++) {
//            for (int j = 0; j < initialState.boxes[0].length; j++) {
//                if (Character.isLetter(initialState.boxes[i][j])) {
//                    boxes.add(new Box(i, j, String.valueOf(initialState.boxes[i][j]), State.boxColors[Box.toNumeric(String.valueOf(initialState.boxes[i][j]))]));
//                }
//            }
//        }
//
//        System.err.println("Boxes: " + boxes);
//
//        // Now we need to find the goals on the map
//        for (int i = 0; i < State.goals.length; i++) {
//            for (int j = 0; j < State.goals[0].length; j++) {
//                if (Character.isLetter(State.goals[i][j])) {
//                    initialGoals.add(new Goal(i, j, String.valueOf(State.goals[i][j]), State.boxColors[Box.toNumeric(String.valueOf(State.goals[i][j]))]));
//                }
//            }
//        }
//
//        System.err.println("Goals: " + initialGoals);
//
//        // TODO: I can't help but keep coming back to thinking of a need to address a situation in which an agent
//        // cannot complete an objective. It could either be the result of a blocking box, agent, or otherwise. I would
//        // hope that all the levels given in the competition are truly solveable, but I just don't know. I would rather
//        // fail hard, fail fast than to have the planner continue to try to execute.
//
//        int iterations = 0;
//        // we need a difference between the cost function and the heuristic function
//        if (initialState.isGoalState()) {
//            return initialState.extractPlan();
//        }
//
//        frontier.add(initialState);
//        HashSet<State> expanded = new HashSet<>();
//
//
//        while (true) {
//
//            //Print a status message every 10000 iteration
//            if (++iterations % 10000 == 0) {
//                printSearchStatus(expanded, frontier);
//            }
//
//            //Your code here... Don't forget to print out the stats when a solution has been found (see above)
//            if (frontier.isEmpty()) {
//                return null;
//            }
//
//            var currentNode = frontier.pop();
//            expanded.add(currentNode);
////                System.err.println(LocalDateTime.now() + ": Path" + Arrays.toString(currentNode.agentRows) + Arrays.toString(currentNode.agentCols));
//
//            // TODO: This is the really scary part of the code. If we attempt to have the planner reevaluate
//            // every step inside that for loop, it will be computationally inefficient.
//            for (var child : currentNode.getExpandedStates()) {
//                // TODO: Remove me
//                //System.err.println(LocalDateTime.now() + ": Path" + Arrays.toString(child.agentRows) + Arrays.toString(child.agentCols));
//                if (!expanded.contains(child) && !frontier.contains(child)) {
////                        System.err.println(child);
//                    if (child.isGoalState()) {
//                        printSearchStatus(expanded, frontier);
//                        return child.extractPlan();
//                    }
//                    frontier.add(child);
//                }
//            }
//        }
    }

    private static long startTime = System.nanoTime();

    private static void printSearchStatus(HashSet<State> expanded, Frontier frontier) {
        String statusTemplate = "#Expanded: %,8d, #Frontier: %,8d, #Generated: %,8d, Time: %3.3f s\n%s\n";
        double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
        System.err.format(statusTemplate, expanded.size(), frontier.size(), expanded.size() + frontier.size(),
                elapsedTime, Memory.stringRep());
    }
}
