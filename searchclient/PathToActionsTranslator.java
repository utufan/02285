package searchclient;

import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;

public class PathToActionsTranslator {

    /*
    public enum TaskType {
        MOVE_BOX_TO_GOAL,
        MOVE_BOX_OUT_OF_WAY,
        MOVE_AGENT_OUT_OF_WAY,
        // indicates it has no task
        MOVE_TO_DESTINATION,
        NONE
    }
     */

    public static List<Triple<Vertex, Vertex, Action>> translatePath(Task task) {
        List<Triple<Vertex, Vertex, Action>> actions = new ArrayList<>();
        Triple<Vertex, Vertex, Action> move = null;

        switch (task.type) {
            case MOVE_TO_DESTINATION, MOVE_AGENT_OUT_OF_WAY:
                for (int i = 0; i < task.path.size() - 1; i++) {
                    Vertex current = task.path.get(i);
                    Vertex next = task.path.get(i + 1);
                    if (current.locRow < next.locRow) {
                        actions.add(Triple.of(current, next, Action.MoveS));
//                        actions.add(Action.MoveS);
                    } else if (current.locRow > next.locRow) {
                        actions.add(Triple.of(current, next, Action.MoveN));
//                        actions.add(Action.MoveN);
                    } else if (current.locCol < next.locCol) {
                        actions.add(Triple.of(current, next, Action.MoveE));
//                        actions.add(Action.MoveE);
                    } else if (current.locCol > next.locCol) {
                        actions.add(Triple.of(current, next, Action.MoveW));
//                        actions.add(Action.MoveW);
                    }
                }
                break;
            case MOVE_BOX_OUT_OF_WAY:
                break;
//            case MOVE_AGENT_OUT_OF_WAY:
//                break;
            // TODO: Maybe we want to split this into two moves: one to move the agent to the box, and one to move the box to the goal
            case MOVE_BOX_TO_GOAL:
                break;
            case NONE:
                actions.add(Triple.of(new Vertex(task.agentRow, task.agentCol), new Vertex(task.agentRow, task.agentCol) , Action.NoOp));
                break;
        }
        return actions;
    }
}
