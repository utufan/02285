package searchclient;

import jdk.jshell.execution.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

//import static searchclient.Utils.calculateEdges;

public class SearchClient
{
    public enum TypeOfAgentGoalsCondition {
        MoreAgentsThanAgentGoals,  // Could be useful to identify for the CP when to put agents in NOOP states
        EqualAgentsAndAgentGoals, // Agents = AgentsGoals
        NoAgentGoals // Could be useful to identify for the CP when to put agents in NOOP states
    }

    public enum TypeOfBoxGoalCondition{
        NoBoxGoals,  // There are boxes, but none of them have any goals
        NoBoxes, // No boxes at all in the map
        MoreBoxesThanBoxGoals,  // The CP might need to prioritize getting boxes out of the way in this situation
        EqualNumberOfBoxesAndBoxGoals // Boxes = BoxesGoals
    }

    public static State parseLevel(BufferedReader serverMessages)
            throws IOException
    {
        // We can assume that the level file is conforming to specification, since the server verifies this.
        // Read domain
        serverMessages.readLine(); // #domain
        serverMessages.readLine(); // hospital



//        List<Character> boxesWithoutMatchingAgents = new ArrayList<>();
        Utils.boxesNotForGoals = new ArrayList<>();

        // Read Level name
        serverMessages.readLine(); // #levelname
        serverMessages.readLine(); // <name>

        // Read colors
        serverMessages.readLine(); // #colors
        Color[] agentColors = new Color[10];
        Color[] boxColors = new Color[26];
        Map<Color, List<Character>> agentsByColor = new HashMap<>();
        String line = serverMessages.readLine();
        while (!line.startsWith("#"))
        {
            String[] split = line.split(":");
            Color color = Color.fromString(split[0].strip());
            String[] entities = split[1].split(",");
            for (String entity : entities)
            {
                char c = entity.strip().charAt(0);
                if ('0' <= c && c <= '9')
                {
                    if (agentsByColor.containsKey(color)){
                        var currentListOfAgents = agentsByColor.get(color);
                        currentListOfAgents.add(c);
                        agentsByColor.put(color, currentListOfAgents);
                    }else {
                        var newListOfAgents = new ArrayList<Character>();
                        newListOfAgents.add(c);
                        agentsByColor.put(color, newListOfAgents);
                    }
                    agentColors[c - '0'] = color;
                }
                else if ('A' <= c && c <= 'Z')
                {
                    boxColors[c - 'A'] = color;

                    if (!agentsByColor.containsKey(color)){
                        Utils.boxesNotForGoals.add(c);
                    }
                }
            }

            line = serverMessages.readLine();
        }

        // Read initial state
        // line is currently "#initial"
        int numRows = 0;
        int numCols = 0;
        ArrayList<String> levelLines = new ArrayList<>(64);
        line = serverMessages.readLine();
        while (!line.startsWith("#"))
        {
            levelLines.add(line);
            numCols = Math.max(numCols, line.length());
            ++numRows;
            line = serverMessages.readLine();
        }
        int numAgents = 0;
        int[] agentRows = new int[10];
        int[] agentCols = new int[10];
        boolean[][] walls = new boolean[numRows][numCols];
        char[][] boxes = new char[numRows][numCols];
        // Here is where we need the initial map representation

        List<Character> wallEquivalents = new ArrayList<>();
        wallEquivalents.add('+');
        wallEquivalents.addAll(Utils.boxesNotForGoals);
        var mapRep = Utils.initialMapRepresentation(levelLines, wallEquivalents);
        var initialMap = Utils.initialMapRepresentation(levelLines, wallEquivalents);

        printDistancesFromCell(Utils.intMap, Utils.dist, 1,1);

        int[][] agents = new int[numRows][numCols];
        List<Character> boxesIDs = new ArrayList<>();
        List<Character> agentsIDs = new ArrayList<>();
        Map<Character, Integer> NumberOfBoxesById = new HashMap<>();
        for (int row = 0; row < numRows; ++row)
        {
            line = levelLines.get(row);
            for (int col = 0; col < line.length(); ++col)
            {
                char c = line.charAt(col);

                if ('0' <= c && c <= '9')
                {
                    agentsIDs.add(c);
                    initialMap.getVertex(row, col).cellChar = c;
                    agents[row][col] = c;

                    agentRows[c - '0'] = row;
                    agentCols[c - '0'] = col;
                    ++numAgents;

                }
                else if ('A' <= c && c <= 'Z')
                {
                    boxesIDs.add(c);
                    initialMap.getVertex(row, col).boxChar = c;
                    boxes[row][col] = c;
                    if (NumberOfBoxesById.containsKey(c)){
                        int val = NumberOfBoxesById.get(c);
                        NumberOfBoxesById.put(c, val+1);
                    }else{
                        NumberOfBoxesById.put(c, 1);
                    }
                }
                else if (c == '+')
                {
                    walls[row][col] = true;
                }
            }
        }

        // To get the boxes and agents on initial map
        Utils.initialMapRepresentation = initialMap;


        agentRows = Arrays.copyOf(agentRows, numAgents);
        agentCols = Arrays.copyOf(agentCols, numAgents);

        // Read goal state
        // line is currently "#goal"

        List<Character> goalsForAgents = new ArrayList<>();
        Map<Character, Integer> goalsForBoxes = new HashMap<>();
        char[][] goals = new char[numRows][numCols];
        line = serverMessages.readLine();
        int row = 0;
        while (!line.startsWith("#"))
            while (!line.startsWith("#"))
            {
                for (int col = 0; col < line.length(); ++col)
                {
                    char c = line.charAt(col);

                    if (('0' <= c && c <= '9') || ('A' <= c && c <= 'Z'))
                    {
                        goals[row][col] = c;
                        mapRep.getVertex(row, col).goalChar = c;
                        if (('A' <= c && c <= 'Z')){
                            if (goalsForBoxes.containsKey(c) && !Utils.boxesNotForGoals.contains(c)){
                                int val = goalsForBoxes.get(c);
                                goalsForBoxes.put(c, val+1);
                            }else if (!Utils.boxesNotForGoals.contains(c)){
                                goalsForBoxes.put(c, 1);
                            }
                        }
                        if (('0' <= c && c <= '9')){
                            goalsForAgents.add(c);
                        }
                    }
                }

                ++row;
                line = serverMessages.readLine();
            }

        // save the goal map representation
        Utils.goalMapRepresentation = mapRep;
        System.err.println("Goal Ordering: " + Utils.goalMapRepresentation.determine_goal_ordering(Utils.goalMapRepresentation.adjVertices));

        List<Character> agentsWithoutGoals = new ArrayList<>();
        for (var agent : agentsIDs) {
            if (!goalsForAgents.contains(agent)){
                agentsWithoutGoals.add(agent);
            }
        }
        Utils.agentsWithoutGoals = agentsWithoutGoals;

        // Here we are comparing the number of original boxes by ID to the number of goals that we have available
        // in the final state. If there is a positive number it means that there are x number of boxes more than goals
        // for that respective ID.
        Utils.goalsVSActualBoxes = new HashMap<>();
        for (var boxChar : NumberOfBoxesById.keySet()) {
            if (goalsForBoxes.containsKey(boxChar) && !Utils.boxesNotForGoals.contains(boxChar)){
                var diff = NumberOfBoxesById.get(boxChar) - goalsForBoxes.get(boxChar);
                Utils.goalsVSActualBoxes.put(boxChar, diff);
            }else if(!Utils.boxesNotForGoals.contains(boxChar)) {
                Utils.goalsVSActualBoxes.put(boxChar, NumberOfBoxesById.get(boxChar));
            }

        }

        System.err.print("\n--------------------------Box and Agents counting-----------------------------\n");
        System.err.print("Agents without goals: " + Utils.agentsWithoutGoals + "\n");
        System.err.print("Boxes without Matching Agents: " + Utils.boxesNotForGoals + "\n");
        System.err.print("Boxes without goals: " + Utils.goalsVSActualBoxes + "\n");



        // End
        // line is currently "#end"


        Utils.typeOfAgentGoalsCondition = classifyAgentConditionsForLevel(agentsWithoutGoals, agentsIDs);
        Utils.typeOfBoxGoalCondition = classifyBoxConditionsForLevel(goalsForBoxes, boxesIDs, Utils.goalsVSActualBoxes);
        System.err.print("\n--------------------------Box and Agents Conditions-----------------------------\n");
        System.err.print("State of Boxes:" + Utils.typeOfBoxGoalCondition +"\n");
        System.err.print("State of agents:" + Utils.typeOfAgentGoalsCondition +"\n");

        // End
        // line is currently "#end"

        return new State(agentRows, agentCols, agentColors, walls, boxes, boxColors, goals);
    }

    public static TypeOfAgentGoalsCondition classifyAgentConditionsForLevel(List<Character> agentsWithoutGoals, List<Character> originalAgentsList ){
        if(originalAgentsList.size() == agentsWithoutGoals.size()){
            return TypeOfAgentGoalsCondition.NoAgentGoals;
        }
        if(agentsWithoutGoals.size()>0){
            return TypeOfAgentGoalsCondition.MoreAgentsThanAgentGoals;
        }
        return TypeOfAgentGoalsCondition.EqualAgentsAndAgentGoals;

    }


    public static TypeOfBoxGoalCondition classifyBoxConditionsForLevel(Map<Character, Integer> goalsForBoxes,
                                                                       List<Character> boxesIDs,
                                                                       Map<Character,Integer> goalsVSBoxes)
    {
        if (boxesIDs.isEmpty()){
            return TypeOfBoxGoalCondition.NoBoxes;
        }
        if (goalsForBoxes.isEmpty()){
            return TypeOfBoxGoalCondition.NoBoxGoals;
        }
        boolean anyBoxIdWithMoreBoxesThanGoals = false;
        System.err.println("goalsVSBoxes: " + goalsVSBoxes);
        for (var goalDiff: goalsVSBoxes.values()) {
            if (goalDiff > 0){
                anyBoxIdWithMoreBoxesThanGoals = true;
            }
        }
        if (!anyBoxIdWithMoreBoxesThanGoals){
            return TypeOfBoxGoalCondition.EqualNumberOfBoxesAndBoxGoals;
        }
        else{
            return TypeOfBoxGoalCondition.MoreBoxesThanBoxGoals;
        }

    }

    public static void printDistancesFromCell(int[][] intMap, double[][] dist, int startRow, int startCol) {
        int numRows = intMap.length;
        int numCols = intMap[0].length;

        System.err.println("Distances from cell (" + startRow + "," + startCol + "):");
        for (int i = 0; i < numRows; i++) {
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


    public static Action[][] search(State initialState, Frontier frontier)
    {
        System.err.format("Starting %s.\n", frontier.getName());

        return GraphSearch.search(initialState, frontier);
    }

    public static void main(String[] args)
            throws IOException
    {
        // Use stderr to print to the console.
        System.err.println("SearchClient initializing. I am sending this using the error output stream.");

        // Send client name to server.
        System.out.println("SearchClient");

        // We can also print comments to stdout by prefixing with a #.
        System.out.println("#This is a comment.");

        // Parse the level.
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII));
        State initialState = SearchClient.parseLevel(serverMessages);

        System.err.println(new HeuristicWeightedAStar(initialState, 5).h(initialState));

        // Select search strategy.
        Frontier frontier;
        if (args.length > 0)
        {
            switch (args[0].toLowerCase(Locale.ROOT))
            {
                case "-bfs":
                    frontier = new FrontierBFS();
                    break;
                case "-dfs":
                    frontier = new FrontierDFS();
                    break;
                case "-astar":
                    frontier = new FrontierBestFirst(new HeuristicAStar(initialState));
                    break;
                case "-wastar":
                    int w = 5;
                    if (args.length > 1)
                    {
                        try
                        {
                            w = Integer.parseUnsignedInt(args[1]);
                        }
                        catch (NumberFormatException e)
                        {
                            System.err.println("Couldn't parse weight argument to -wastar as integer, using default.");
                        }
                    }
                    frontier = new FrontierBestFirst(new HeuristicWeightedAStar(initialState, w));
                    break;
                case "-greedy":
                    frontier = new FrontierBestFirst(new HeuristicGreedy(initialState));
                    break;
                default:
                    frontier = new FrontierBFS();
                    System.err.println("Defaulting to BFS search. Use arguments -bfs, -dfs, -astar, -wastar, or " +
                            "-greedy to set the search strategy.");
            }
        }
        else
        {
            frontier = new FrontierBFS();
            System.err.println("Defaulting to BFS search. Use arguments -bfs, -dfs, -astar, -wastar, or -greedy to " +
                    "set the search strategy.");
        }

        // Search for a plan.
        Action[][] plan;
        try
        {
            plan = SearchClient.search(initialState, frontier);
        }
        catch (OutOfMemoryError ex)
        {
            System.err.println("Maximum memory usage exceeded.");
            plan = null;
        }

        // Print plan to server.
        if (plan == null)
        {
            System.err.println("Unable to solve level.");
            System.exit(0);
        }
        else
        {
            System.err.format("Found solution of length %,d.\n", plan.length);

            for (Action[] jointAction : plan)
            {
                System.out.print(jointAction[0].name);
                for (int action = 1; action < jointAction.length; ++action)
                {
                    System.out.print("|");
                    System.out.print(jointAction[action].name);
                }
                System.out.println();
                // We must read the server's response to not fill up the stdin buffer and block the server.
                serverMessages.readLine();
            }
        }
    }
}