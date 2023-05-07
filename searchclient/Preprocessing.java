package searchclient;

import java.util.ArrayList;
import java.util.List;

public class Preprocessing {
    // This class is meant to be used to preprocess the map according to the domain model
    // It should not be copied nor passed into any expanded nodes

    // We are also going to have Preprocessing create the blackboard
    public State initialState;


    public Preprocessing(State initialState) {
        this.initialState = initialState;
    }

    public List<Agent> findAgents() {
        List<Agent> agents = new ArrayList<>();

        for (int i = 0; i < this.initialState.agentRows.length; i++) {
            agents.add(new Agent(Integer.toString(i), State.agentColors[i], initialState.agentRows[i], initialState.agentCols[i]));
        }
        return agents;
    }

    // TODO: Find boxes is looking at the initial state, sync with the preprocessing
    public List<Box> findBoxes() {
        List<Box> boxes = new ArrayList<>();

        for (int i = 0; i < initialState.boxes.length; i++) {
            for (int j = 0; j < initialState.boxes[0].length; j++) {
                if (Character.isLetter(initialState.boxes[i][j])) {
                    boxes.add(new Box(i, j, String.valueOf(initialState.boxes[i][j]), State.boxColors[Box.toNumeric(String.valueOf(initialState.boxes[i][j]))]));
                }
            }
        }

        return boxes;
    }

    // TODO: Make these goals ordered
    public List<Goal> findGoals() {
        List<Goal> goals = new ArrayList<>();

        // TODO: This marks goals ONLY as boxes, but not when they are agents moving to a location
        for (int i = 0; i < State.goals.length; i++) {
            for (int j = 0; j < State.goals[0].length; j++) {
                if (Character.isLetter(State.goals[i][j])) {
                    goals.add(new Goal(i, j, String.valueOf(State.goals[i][j]), State.boxColors[Box.toNumeric(String.valueOf(State.goals[i][j]))]));
                }
                if (Character.isDigit(State.goals[i][j])) {
                    goals.add(new Goal(i, j, String.valueOf(State.goals[i][j]), null));
                }
            }
        }
        return goals;
    }

    public int findWidth() {
        return State.walls[0].length;
    }

    public int findHeight() {
        return State.walls.length;
    }

    public Blackboard initializeBlackboard(int[][] intMap, double[][] dist, Graph mapRepresentation) {
        Blackboard.initialize(findAgents(), findBoxes(), findGoals(), findWidth(), findHeight(), intMap, dist, mapRepresentation);
        return Blackboard.getInstance();
    }
}
