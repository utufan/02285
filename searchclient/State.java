package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class State
{
    private static final Random RNG = new Random(1);

    /*
        The agent rows, columns, and colors are indexed by the agent number.
        For example, this.agentRows[0] is the row location of agent '0'.
    */
    public int[] agentRows;
    public int[] agentCols;
    public static Color[] agentColors;

    /*
        The walls, boxes, and goals arrays are indexed from the top-left of the level, row-major order (row, col).
               Col 0  Col 1  Col 2  Col 3
        Row 0: (0,0)  (0,1)  (0,2)  (0,3)  ...
        Row 1: (1,0)  (1,1)  (1,2)  (1,3)  ...
        Row 2: (2,0)  (2,1)  (2,2)  (2,3)  ...
        ...

        For example, this.walls[2] is an array of booleans for the third row.
        this.walls[row][col] is true if there's a wall at (row, col).

        this.boxes and this.char are two-dimensional arrays of chars. 
        this.boxes[1][2]='A' means there is an A box at (1,2). 
        If there is no box at (1,2), we have this.boxes[1][2]=0 (null character).
        Simiarly for goals. 

    */
    public static boolean[][] walls;
    public char[][] boxes;
    public static char[][] goals;

    /*
        The box colors are indexed alphabetically. So this.boxColors[0] is the color of A boxes, 
        this.boxColor[1] is the color of B boxes, etc.
    */
    public static Color[] boxColors;
 
    public final State parent;
    public final Action[] jointAction;
    private final int g;

    private int hash = 0;

//    public static int[][] intMap;
//    public static double[][] distMap;


    // Constructs an initial state.
    // Arguments are not copied, and therefore should not be modified after being passed in.
    public State(int[] agentRows, int[] agentCols, Color[] agentColors, boolean[][] walls,
                 char[][] boxes, Color[] boxColors, char[][] goals
    )
    {
        this.agentRows = agentRows;
        this.agentCols = agentCols;
        this.agentColors = agentColors;
        this.walls = walls;
        this.boxes = boxes;
        this.boxColors = boxColors;
        this.goals = goals;
        this.parent = null;
        this.jointAction = null;
        this.g = 0;
//        this.intMap = intMap;
//        this.distMap = distMap;
    }


    // Constructs the state resulting from applying jointAction in parent.
    // Precondition: Joint action must be applicable and non-conflicting in parent state.
    private State(State parent, Action[] jointAction)
    {
        // Copy parent
        this.agentRows = Arrays.copyOf(parent.agentRows, parent.agentRows.length);
        this.agentCols = Arrays.copyOf(parent.agentCols, parent.agentCols.length);
        this.boxes = new char[parent.boxes.length][];
        for (int i = 0; i < parent.boxes.length; i++)
        {
            this.boxes[i] = Arrays.copyOf(parent.boxes[i], parent.boxes[i].length);
        }

        // Set own parameters
        this.parent = parent;
        this.jointAction = Arrays.copyOf(jointAction, jointAction.length);
        this.g = parent.g + 1;


        // Apply each action
        int numAgents = this.agentRows.length;
        for (int agent = 0; agent < numAgents; ++agent)
        {
            Action action = jointAction[agent];
            char box;

            switch (action.type)
            {
                case NoOp:
                    break;

                case Move:
                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;
                    break;

                case Push:
                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;
                    box = this.boxes[this.agentRows[agent]][this.agentCols[agent]];
                    this.boxes[this.agentRows[agent]][this.agentCols[agent]] = 0;
                    this.boxes[this.agentRows[agent] + action.boxRowDelta][this.agentCols[agent] + action.boxColDelta] = box;
                    break;

                // TODO: Implement Pull
                case Pull:
                    box = this.boxes[this.agentRows[agent] - action.boxRowDelta][this.agentCols[agent] - action.boxColDelta];
                    this.boxes[this.agentRows[agent] - action.boxRowDelta][this.agentCols[agent] - action.boxColDelta] = 0;
                    this.boxes[this.agentRows[agent]][this.agentCols[agent]] = box;
                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;
                    break;
            }
        }
    }

    public int g()
    {
        return this.g;
    }

    public boolean isGoalState()
    {
        for (int row = 1; row < this.goals.length - 1; row++)
        {
            for (int col = 1; col < this.goals[row].length - 1; col++)
            {
                char goal = this.goals[row][col];

                if ('A' <= goal && goal <= 'Z' && this.boxes[row][col] != goal)
                {
                    return false;
                }
                else if ('0' <= goal && goal <= '9' &&
                         !(this.agentRows[goal - '0'] == row && this.agentCols[goal - '0'] == col))
                {
                    return false;
                }
            }
        }
        return true;
    }

    public ArrayList<State> getExpandedStates()
    {
        int numAgents = this.agentRows.length;

        // Determine list of applicable actions for each individual agent.
        Action[][] applicableActions = new Action[numAgents][];
        // TODO: The really stupid part about this, is that States will get generated that don't need to be so long as
        // they are considered "valid". But I don't know of a good way to restrict generation of these otherwise. :thinking:
        for (int agent = 0; agent < numAgents; ++agent)
        {
            ArrayList<Action> agentActions = new ArrayList<>(Action.values().length);
            for (Action action : Action.values())
            {
                if (this.isApplicable(agent, action))
                {
                    agentActions.add(action);
                }
            }
            applicableActions[agent] = agentActions.toArray(new Action[0]);
        }

        // Iterate over joint actions, check conflict and generate child states.
        Action[] jointAction = new Action[numAgents];
        int[] actionsPermutation = new int[numAgents];
        ArrayList<State> expandedStates = new ArrayList<>(16);
        while (true)
        {
            for (int agent = 0; agent < numAgents; ++agent)
            {
                jointAction[agent] = applicableActions[agent][actionsPermutation[agent]];
            }

            if (!this.isConflicting(jointAction))
            {
                expandedStates.add(new State(this, jointAction));
            }

            // Advance permutation
            boolean done = false;
            for (int agent = 0; agent < numAgents; ++agent)
            {
                if (actionsPermutation[agent] < applicableActions[agent].length - 1)
                {
                    ++actionsPermutation[agent];
                    break;
                }
                else
                {
                    actionsPermutation[agent] = 0;
                    if (agent == numAgents - 1)
                    {
                        done = true;
                    }
                }
            }

            // Last permutation?
            if (done)
            {
                break;
            }
        }

        // Might be to pre-seeded randomization of movement of other agents
        Collections.shuffle(expandedStates, State.RNG);
        return expandedStates;
    }

    private boolean isApplicable(int agent, Action action) {
        int agentRow = this.agentRows[agent];
        int agentCol = this.agentCols[agent];
        Color agentColor = State.agentColors[agent];
        int boxRow;
        int boxCol;
        char box;
        int destinationRow;
        int destinationCol;
        switch (action.type) {
            case NoOp:
                return true;

            case Move:
                destinationRow = agentRow + action.agentRowDelta;
                destinationCol = agentCol + action.agentColDelta;
                return this.cellIsFree(destinationRow, destinationCol);

            case Push:
                //Get the "old" coordinates of the box that has been pushed
                boxRow = agentRow + action.agentRowDelta;
                boxCol = agentCol + action.agentColDelta;

                //Get Name (char) of Box at the "old" position
                box = this.boxes[boxRow][boxCol];

                //Check if that position was marked with a box indicator and check if the agent is approved (colour check) to move the box.
                //convert box from letter into number A = 0, B = 1, etc.
                if (box == '\0' || State.boxColors[box - 'A'] != agentColor) {
                    return false;
                }

                //Confirm that this move was legal by checking the constraints of walls and other obstacles in the grid.
                return this.cellIsFree(boxRow + action.boxRowDelta, boxCol + action.boxColDelta);

            case Pull:
                //Get the "old" coordinates of the box that has been pulled
                boxRow = agentRow - action.boxRowDelta;
                boxCol = agentCol - action.boxColDelta;
                box = this.boxes[boxRow][boxCol];

                //Check if that position was marked with a box indicator and check if the agent is approved (colour check) to move the box.
                //convert box from letter into number A = 0, B = 1, etc.
                if (box == '\0' || State.boxColors[box - 'A'] != agentColor) {
                    return false;
                }

                //Calculate the new row position of the agent
                destinationRow = agentRow + action.agentRowDelta;
                destinationCol = agentCol + action.agentColDelta;

                //Confirm that this move is legal by checking the constraints of walls and other obstacles in the grid.
                return this.cellIsFree(destinationRow, destinationCol);
        }
        // Unreachable:
        return false;
    }

    private boolean isConflicting(Action[] jointAction)
    {
        int numAgents = this.agentRows.length;

        int[] destinationRows = new int[numAgents]; // row of new cell to become occupied by action
        int[] destinationCols = new int[numAgents]; // column of new cell to become occupied by action
        int[] boxRows = new int[numAgents]; // current row of box moved by action
        int[] boxCols = new int[numAgents]; // current column of box moved by action
        int[] destinationRowsBoxes = new int[numAgents]; // row of new cell to become occupied by action
        int[] destinationColsBoxes = new int[numAgents]; // row of new cell to become occupied by action

        // Collect cells to be occupied and boxes to be moved
        for (int agent = 0; agent < numAgents; ++agent)
        {
            Action action = jointAction[agent];
            int agentRow = this.agentRows[agent];
            int agentCol = this.agentCols[agent];
            int boxRow;
            int boxCol;

            destinationRows[agent] = agentRow + action.agentRowDelta;
            destinationCols[agent] = agentCol + action.agentColDelta;

            switch (action.type)
            {
                case NoOp:

                    break;

                case Move:
                    
                    boxRows[agent] = agentRow; // Distinct dummy value
                    boxCols[agent] = agentCol; // Distinct dummy value
                    destinationRowsBoxes[agent] = agentRow + action.agentRowDelta;
                    destinationColsBoxes[agent] = agentCol + action.agentColDelta;

                    break;

                case Push:
                    // Current row of the box is the destination of the agent
                    boxRows[agent] = destinationRows[agent];
                    boxCols[agent] = destinationCols[agent];
                    // destination row of the box is destionation of agent plus the action offset
                    destinationRowsBoxes[agent] = destinationRows[agent] + action.boxRowDelta;
                    destinationColsBoxes[agent] = destinationCols[agent] + action.boxColDelta;

                    break;

                case Pull:
                    //current row of the box is current row of the agent reversed with the actions
                    boxRows[agent] = agentRow + action.boxRowDelta;
                    boxCols[agent] = agentCol + action.boxColDelta;
                    //destination row of the box is the current row of the agent
                    destinationRowsBoxes[agent] = agentRow;
                    destinationColsBoxes[agent] = agentCol;

                    break;
           }
        }

        for (int a1 = 0; a1 < numAgents; ++a1)
        {
            if (jointAction[a1] == Action.NoOp)
            {
                continue;
            }

            for (int a2 = a1 + 1; a2 < numAgents; ++a2)
            {
                if (jointAction[a2] == Action.NoOp)
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
        return !this.walls[row][col] && this.boxes[row][col] == 0 && this.agentAt(row, col) == 0;
    }

    private char agentAt(int row, int col)
    {
        for (int i = 0; i < this.agentRows.length; i++)
        {
            if (this.agentRows[i] == row && this.agentCols[i] == col)
            {
                return (char) ('0' + i);
            }
        }
        return 0;
    }

    public Action[][] extractPlan()
    {
        Action[][] plan = new Action[this.g][];
        State state = this;
        while (state.jointAction != null)
        {
            plan[state.g - 1] = state.jointAction;
            state = state.parent;
        }
        return plan;
    }

    @Override
    public int hashCode()
    {
        if (this.hash == 0)
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(this.agentColors);
            result = prime * result + Arrays.hashCode(this.boxColors);
            result = prime * result + Arrays.deepHashCode(this.walls);
            result = prime * result + Arrays.deepHashCode(this.goals);
            result = prime * result + Arrays.hashCode(this.agentRows);
            result = prime * result + Arrays.hashCode(this.agentCols);
            for (int row = 0; row < this.boxes.length; ++row)
            {
                for (int col = 0; col < this.boxes[row].length; ++col)
                {
                    char c = this.boxes[row][col];
                    if (c != 0)
                    {
                        result = prime * result + (row * this.boxes[row].length + col) * c;
                    }
                }
            }
            this.hash = result;
        }
        return this.hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (this.getClass() != obj.getClass())
        {
            return false;
        }
        State other = (State) obj;
        return Arrays.equals(this.agentRows, other.agentRows) &&
               Arrays.equals(this.agentCols, other.agentCols) &&
               Arrays.equals(this.agentColors, other.agentColors) &&
               Arrays.deepEquals(this.walls, other.walls) &&
               Arrays.deepEquals(this.boxes, other.boxes) &&
               Arrays.equals(this.boxColors, other.boxColors) &&
               Arrays.deepEquals(this.goals, other.goals);
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        for (int row = 0; row < this.walls.length; row++)
        {
            for (int col = 0; col < this.walls[row].length; col++)
            {
                if (this.boxes[row][col] > 0)
                {
                    s.append(this.boxes[row][col]);
                }
                else if (this.walls[row][col])
                {
                    s.append("+");
                }
                else if (this.agentAt(row, col) != 0)
                {
                    s.append(this.agentAt(row, col));
                }
                else
                {
                    s.append(" ");
                }
            }
            s.append("\n");
        }
        return s.toString();
    }
}
