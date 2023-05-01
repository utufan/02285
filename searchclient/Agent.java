package searchclient;

import java.util.ArrayList;
import java.util.List;

public class Agent {
    public List<Goal> goals;
    public String id;
    public Color color;
    public int row,
                col;

    public Agent(String id, Color color) {
        this.id = id;
        this.color = color;

        // intialize goals to empty list
        this.goals = new ArrayList<>();
    }

    public Agent(String id, Color color, int row, int col) {
        this.id = id;
        this.color = color;
        this.row = row;
        this.col = col;

        // intialize goals to empty list
        this.goals = new ArrayList<>();
    }

    public String toString() {
        return "Agent " + this.id + " with color " + this.color + " at " + this.row + ", " + this.col;
    }

    public void addGoal(Goal goal) {
        this.goals.add(goal);
    }

    public void removeGoal(Goal goal) {
        this.goals.remove(goal);
    }

    public List<Goal> getGoals() {
        return this.goals;
    }
}
