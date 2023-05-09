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
        Blackboard blackboard = Blackboard.getInstance();

        switch (task.type) {
            case MOVE_TO_DESTINATION, MOVE_AGENT_OUT_OF_WAY:
                for (int i = 0; i < task.path.size() - 1; i++) {
                    Vertex current = task.path.get(i);
                    Vertex next = task.path.get(i + 1);
                    if (next.cellChar == '\0') {

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
                    } else {
                        // This is really fucking bad because why can't it move? WHO KNOWS
                        actions.add(Triple.of(current, next, Action.NoOp));
                    }
                }
                break;
            case MOVE_AGENT_TO_BOX:
                for (int i = 0; i < task.path.size() - 1; i++) {
                    Vertex current = task.path.get(i);
                    Vertex next = task.path.get(i + 1);
//                    if (next.boxChar != task.boxId.charAt(0)) {
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
//                    }
                }
                // we naively assume the next char is a box, and can't POSSIBLY be blocked by another agent or box
                // NO WAY, THAT WOULD NEVER HAPPEN /s
                actions = actions.subList(0, actions.size() - 1);
                break;
            case MOVE_BOX_OUT_OF_WAY:
                break;
//            case MOVE_AGENT_OUT_OF_WAY:
//                break;
            // TODO: Maybe we want to split this into two moves: one to move the agent to the box, and one to move the box to the goal
            case MOVE_BOX_TO_GOAL:
                // we need a case to handle if the path size is one or not
                // The problem here is that the agent needs to move to the box, agent -> task, and then from task->destination
                for (int i = 0; i < task.path.size() - 1; i++) {
                    Vertex current = task.path.get(i);
                    Vertex next = task.path.get(i + 1);
//                    Vertex boxVertex = blackboard.getVertex(task.taskRow, task.taskCol);
                    Box box = blackboard.getBox(task.taskRow, task.taskCol);
                    if (box == null) {
                        throw new RuntimeException("Box is null");
                    }
//                    if (next.locRow == task.taskRow && next.locCol == task.taskCol){
                        if (current.locRow < next.locRow && box.row < next.locRow) {
                            actions.add(Triple.of(current, next, Action.PullNN));
                        } else if (current.locRow > next.locRow && box.row > next.locRow) {
                            actions.add(Triple.of(current, next, Action.PullSS));
                        } else if (current.locCol < next.locCol && box.col < next.locCol) {
                            actions.add(Triple.of(current, next, Action.PullEE));
                        } else if (current.locCol > next.locCol && box.col > next.locCol) {
                            actions.add(Triple.of(current, next, Action.PullWW));
                        } else if (current.locRow < next.locRow && box.row > next.locRow) {
                            actions.add(Triple.of(current, next, Action.PushNN));
                        } else if (current.locRow > next.locRow && box.row < next.locRow) {
                            actions.add(Triple.of(current, next, Action.PushSS));
                        } else if (current.locCol < next.locCol && box.col > next.locCol) {
                            actions.add(Triple.of(current, next, Action.PushEE));
                        } else if (current.locCol > next.locCol && box.col < next.locCol) {
                            actions.add(Triple.of(current, next, Action.PushWW));
                        }
//                    }
//                    else {
//                        if (current.locRow < next.locRow) {
//                            actions.add(Triple.of(current, next, Action.MoveS));
//                        } else if (current.locRow > next.locRow) {
//                            actions.add(Triple.of(current, next, Action.MoveN));
//                        } else if (current.locCol < next.locCol) {
//                            actions.add(Triple.of(current, next, Action.MoveE));
//                        } else if (current.locCol > next.locCol) {
//                            actions.add(Triple.of(current, next, Action.MoveW));
//                        }
//                    }
                }
                break;
            case NONE:
                actions.add(Triple.of(new Vertex(task.agentRow, task.agentCol), new Vertex(task.agentRow, task.agentCol) , Action.NoOp));
                break;
        }
        return actions;
    }
}
