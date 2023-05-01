package searchclient;

public class Goal {
    public int row;
    public int col;
    public String id;
    public Color color;
    // TODO: I need to think about this more. If goal is static, i.e. the starting goals on the map, I think it should
    // have one value versus one that is the result of a change, or response to a "help" request. But I don't know if
    // this is the Goal's responsibility, or if it is another class's.
    public int importanceValue;

    // TODO: I don't know how we want to handle goals that might either be agent locations or box locations
    // For now, I am just assuming box locations, which might also stand to reason that the goal should know about
    // the box it is associated with
    public Goal(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public Goal(int row, int col, String id, Color color) {
        this.row = row;
        this.col = col;
        this.id = id;
        this.color = color;
    }

    public void updateGoalLocation(int row, int col) {
        this.row = row;
        this.col = col;
        // This is just for debugging purposes
        System.out.println("New Goal at " + this.row + ", " + this.col);
    }

    public String toString() {
        return "Goal at " + this.row + ", " + this.col;
    }
}
