package searchclient;

import java.util.*;

public abstract class Heuristic
        implements Comparator<State>
{
   
    private final ArrayList<Integer> goalRows = new ArrayList<Integer>();
    private final ArrayList<Integer> goalCols = new ArrayList<Integer>();
    private final ArrayList<Character> goalChars = new ArrayList<Character>();

    public Heuristic(State s)
    {
        for (int row = 0; row < s.goals.length; row++)
        {
            for (int col = 0; col < s.goals[row].length; col++)
            {
                char goal = s.goals[row][col];

                if (goal >= 'A' && goal <= 'Z')
                {
                    this.goalChars.add(goal);
                    this.goalRows.add(row);
                    this.goalCols.add(col);
                }
            }
        }
//        List<Character> characters = determineGoalOrder(s.boxes, s.goals);
//        for (char c : characters) {
//            System.err.print(c + " ");
//        }
    }

    // TODO Research: difference between edge and node representation

    // Contraction hierarchies: https://en.wikipedia.org/wiki/Contraction_hierarchies
    // Unsuitable for real-time updates to the edges of the nodes for weights because the entire map would need to be regenerated
    // and processed.
    // Be careful with A* (Djikstra's with heuristics) for long distance paths because it can be very slow

    // ALT algorithm

    // Highway hierarchies: http://algo2.iti.kit.edu/schultes/hwy/esa06HwyHierarchies.pdf
    //
    public int euclideanDistance(int agentRow, int agentCol, int agentGoalRow, int agentGoalCol) {
        int totalDistance = 0;
        totalDistance += Math.sqrt(Math.pow(agentRow - agentGoalRow, 2) + Math.pow(agentCol - agentGoalCol, 2));
        return totalDistance;
    }

    public int manhattanDistance(int agentRow, int agentCol, int agentGoalRow, int agentGoalCol) {
        int totalDistance = 0;
        totalDistance += Math.abs(agentRow - agentGoalRow) + Math.abs(agentCol - agentGoalCol);
        return totalDistance;
    }

    public List<Character> determineGoalOrder(char[][] boxes, char[][] goals) {
        List<Character> goalOrder = new ArrayList<>();

        // Identify goals and their positions
        Map<Character, int[]> goalPositions = new HashMap<>();
        for (int i = 0; i < goals.length; i++) {
            for (int j = 0; j < goals[0].length; j++) {
                if (goals[i][j] != 0) {
                    char goal = goals[i][j];
                    int[] position = {i, j};
                    goalPositions.put(goal, position);
                    goalOrder.add(goal);
                }
            }
        }

        // Sort goals by position
        Collections.sort(goalOrder, (c1, c2) -> {
            int[] p1 = goalPositions.get(c1);
            int[] p2 = goalPositions.get(c2);
            if (p1[1] != p2[1]) {
                return Integer.compare(p1[1], p2[1]);
            }
            return Integer.compare(p1[0], p2[0]);
        });

        // Reorder goals based on box positions
        Set<Character> visitedGoals = new HashSet<>();
        for (int i = 0; i < goalOrder.size(); i++) {
            char goal = goalOrder.get(i);
            if (!visitedGoals.contains(goal)) {
                int[] goalPosition = goalPositions.get(goal);
                Character box = getBoxAtPosition(goalPosition[0], goalPosition[1], boxes);
                if (box != null) {
                    visitedGoals.add(goal);
                    visitedGoals.add(box);
                    goalOrder.remove(Character.valueOf(goal));
                    goalOrder.remove(box);
                    int index = goalOrder.indexOf(box);
                    goalOrder.add(index, box);
                    goalOrder.add(index + 1, goal);
                    i = -1;
                }
            }
        }

        return goalOrder;
    }

    private Character getBoxAtPosition(int x, int y, char[][] boxes) {
        for (int i = 0; i < boxes.length; i++) {
            for (int j = 0; j < boxes[0].length; j++) {
                if (boxes[i][j] != 0 && i == x && j == y) {
                    return boxes[i][j];
                }
            }
        }
        return null;
    }

    // Verify when this gets executed by the searchclient and you need to add the following for the command being
    // executed to tap into the execution of the code
    // -agentlib:jdwp=transport=dt_socket,server=y,address=*:8000,suspend=y
    public int h(State s)
    {
        // Exercise 6.3:  Add Manhattan Distance for box goal 

        ArrayList<Integer> boxRows = new ArrayList<Integer>();
        ArrayList<Integer> boxCols = new ArrayList<Integer>();
        ArrayList<Character> boxes = new ArrayList<Character>();
        ArrayList<Color> boxesColor = new ArrayList<Color>();

        for (int row = 1; row < s.boxes.length - 1; row++) {
            for (int col = 1; col < s.boxes[row].length - 1; col++) {
                char goal = s.boxes[row][col];
                if ('A' <= goal && goal <= 'Z'){
                    boxes.add(goal);
                    boxRows.add(row);
                    boxCols.add(col);
                    boxesColor.add(s.boxColors[(int)goal - 65]);
                }

            }
        }

        // take an agent and a goal it is looking if it can complete
        // check if agent can reach the box or not
        // if it can reach the box, check if the box can reach the goal (goal is not blocked on a path)


        int cost = 0;
        // Assuming all boxes have one goal we dont need a hashmap
        for (int row = 0; row < this.goalChars.size(); row++) {
            int goalRow = this.goalRows.get(row);
            int goalCol = this.goalCols.get(row);
            char goal = this.goalChars.get(row);
            for(int col = 0; col < boxes.size(); col++) {
                char box = boxes.get(col);
                if (goal == box) {
                    int boxRow = boxRows.get(col);
                    int boxCol = boxCols.get(col);
                    int distance = (int) Utils.getDistance(Utils.intMap, Utils.dist, goalRow, goalCol, boxRow, boxCol);

//                    if (goalRow == boxRow && goalCol == boxCol) {
//                        System.err.println("Goal: " + goal + " is already at the goal");
//                    }

                    //System.err.println("distance goal: " + distance);
                    cost += distance;
                }
            }
        }
        // hashmap to keep track of closest box
        Map<Color, Integer> agentBoxDistance = new HashMap<>();
//        for (int row = 0; row < boxes.size(); row++) {
//            Color boxColor = boxesColor.get(row);
//            int boxRow = boxRows.get(row);
//            int boxCol = boxCols.get(row);
//            for (int col = 0; col < s.agentRows.length; col++) {
//                Color agentColor = s.agentColors[col];
//                if(agentColor == boxColor) {
//                    int agentRow = s.agentRows[col];
//                    int agentCol = s.agentCols[col];
//                    int distance = (int) SearchClient.getDistance(State.intMap, State.distMap, agentRow, agentCol, boxRow, boxCol);
//                    char goal = s.goals[boxRow][boxCol];
//                    // check if the box is already at the goal, if so, do not add the cost of that
//                    if ('Z' >= goal && goal >= 'A' && s.boxes[row][col] == goal)
//                    {
//                        continue;
//                    }
//                    if(agentBoxDistance.containsKey(agentColor)){
//                        Integer entry = agentBoxDistance.get(agentColor);
//                        if(entry > distance){
//                            agentBoxDistance.put(agentColor, distance);
//                        }
//                    }
//                    else{
//                        agentBoxDistance.put(agentColor, distance);
//                    }
//                    //System.err.println("agentRow: " + agentRow + "agentCol: " + agentCol + "boxRow: " + boxRow + "boxCol: " + boxCol);
//                    //System.err.println("distance box: " + distance);
//                }
//            }
//        }

        // Iterating HashMap through for loop
//        for (Map.Entry<Color, Integer> set :
//             agentBoxDistance.entrySet()) {
//
//             cost += set.getValue();
//        }

//        System.err.println("h(n): " + cost);


        return cost;
    }
    public abstract int f(State s);

    @Override
    public int compare(State s1, State s2)
    {
        return this.f(s1) - this.f(s2);
    }
}

class HeuristicAStar
        extends Heuristic
{
    public HeuristicAStar(State initialState)
    {
        super(initialState);
    }

    @Override
    public int f(State s)
    {
        return s.g() + this.h(s);
    }

    @Override
    public String toString()
    {
        return "A* evaluation";
    }
}

class HeuristicWeightedAStar
        extends Heuristic
{
    private int w;

    public HeuristicWeightedAStar(State initialState, int w)
    {
        super(initialState);
        this.w = w;
    }

    @Override
    public int f(State s)
    {
        return s.g() + this.w * this.h(s);
//        return s.g() < this.h(s) ? s.g() + this.w * h(s) : (s.g() + (2 * this.w - 1) * this.h(s)) / this.w;
    }

    @Override
    public String toString()
    {
        return String.format("WA*(%d) evaluation", this.w);
    }
}

class HeuristicGreedy
        extends Heuristic
{
    public HeuristicGreedy(State initialState)
    {
        super(initialState);
    }

    @Override
    public int f(State s)
    {
        return this.h(s);
    }

    @Override
    public String toString()
    {
        return "greedy evaluation";
    }
}
