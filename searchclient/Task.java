package searchclient;


import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

// TODO make the centralized planner to force the agents to update the agentRow and agentCol
public class Task implements Comparable<Task> {
    @Override
    public int compareTo(Task o) {
        if (this.priority == o.priority) {
            var blackboard = Blackboard.getInstance();
            switch (this.type){
                case MOVE_BOX_TO_GOAL, MOVE_BOX_OUT_OF_WAY :
                    var thisDistanceFromPosTo = blackboard.getDistance(this.agentRow, this.agentCol,
                            this.taskRow, this.taskCol) +
                            blackboard.getDistance(this.taskRow, this.taskCol,
                                    this.destinationRow, this.destinationCol);
                    var oDistanceFromPosTo = blackboard.getDistance(o.agentRow, o.agentCol,
                            o.taskRow, o.taskCol) +
                            blackboard.getDistance(o.taskRow, o.taskCol,
                                    o.destinationRow, o.destinationCol);
                    return (int)thisDistanceFromPosTo - (int)oDistanceFromPosTo;
                case NONE:
                    return 0; 
                    
                case MOVE_AGENT_OUT_OF_WAY, MOVE_TO_DESTINATION:
                    var thisDistanceFromPosToMAOFW = blackboard.getDistance(this.agentRow, this.agentCol,
                            this.destinationRow, this.destinationCol);
                            
                    var oDistanceFromPosToMAOFW = blackboard.getDistance(o.agentRow, o.agentCol,
                            o.taskRow, o.taskCol) +
                            blackboard.getDistance(o.taskRow, o.taskCol,
                                    o.destinationRow, o.destinationCol);
                    return (int)thisDistanceFromPosToMAOFW - (int)oDistanceFromPosToMAOFW;
                default:
                    throw new NotImplementedException();
            }
        } else if (this.priority == Priority.HIGH) {
            return -1;
        } else if (this.priority == Priority.MEDIUM && o.priority == Priority.LOW) {
            return -1;
        } else {
            return 1;
        }
    }

    public enum TaskType {
        MOVE_BOX_TO_GOAL,
        MOVE_BOX_OUT_OF_WAY,
        MOVE_AGENT_OUT_OF_WAY,
        // indicates it has no task
        MOVE_TO_DESTINATION,
        NONE
    }

    enum Priority {
        HIGH,
        MEDIUM,
        LOW
    }

    public TaskType type;
    public Priority priority;
    public String agentId;
    public String boxId;
    public String goalId;
    // WARNING: THESE VALUES CAN BE NULL, BUT JAVA DOESN'T HAVE THE NOTION OF OPTIONAL TYPES FOR PRIMATIVES
    public int agentRow,
                agentCol,
                taskRow,
                taskCol,
                destinationRow,
                destinationCol;
    public List<Vertex> path;
    public List<Triple<Vertex, Vertex, Action>> actionsForPath;

    public Task(TaskType type, Priority priority, String agentId, String boxId, String goalId, int agentRow, int agentCol, int taskRow, int taskCol, int destinationRow, int destinationCol) {
        this.type = type;
        this.priority = priority;
        this.agentId = agentId;
        this.boxId = boxId;
        this.goalId = goalId;
        this.agentRow = agentRow;
        this.agentCol = agentCol;
        this.taskRow = taskRow;
        this.taskCol = taskCol;
        this.destinationRow = destinationRow;
        this.destinationCol = destinationCol;
    }

    public String toString() {
        return String.format("Task: %s, %s, %s, %s, %s, %d, %d, %d, %d, %d, %d", this.type, this.priority, this.agentId, this.boxId, this.goalId, this.agentRow, this.agentCol, this.taskRow, this.taskCol, this.destinationRow, this.destinationCol);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Task)) {
            return false;
        }
        Task other = (Task) obj;
        return this.type == other.type &&
                this.priority == other.priority &&
                this.agentId.equals(other.agentId) &&
                this.boxId.equals(other.boxId) &&
                this.goalId.equals(other.goalId) &&
                this.agentRow == other.agentRow &&
                this.agentCol == other.agentCol &&
                this.taskRow == other.taskRow &&
                this.taskCol == other.taskCol &&
                this.destinationRow == other.destinationRow &&
                this.destinationCol == other.destinationCol;
    }
}
