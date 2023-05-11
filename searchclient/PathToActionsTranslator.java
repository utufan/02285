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

        Agent agent = blackboard.agents.get(Integer.parseInt(task.agentId));

        Agent copyAgent = new Agent(agent.id, agent.color, agent.row, agent.col);

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
                // This is so it can stop before the box
                actions = actions.subList(0, actions.size() - 1);
                break;
            case MOVE_BOX_OUT_OF_WAY:
                break;
//            case MOVE_AGENT_OUT_OF_WAY:
//                break;
            // Now we are going to split it apart
            case PUSH_BOX:
                // The Push Box is going to be from the perspective of the box...?
                Box box = blackboard.getBox(task.taskRow, task.taskCol);
                if (box == null) {
                    throw new RuntimeException("Box is null");
                }
                for (int i = 0; i < task.path.size() - 1; i++) {
//                    if (i != task.path.size()) {
                    Vertex current = task.path.get(i);
                    Vertex next = task.path.get(i + 1);
                    // This is from the perspective of the agent
                    // NN Push
                    if (copyAgent.row - 1 == current.locRow && copyAgent.col == current.locCol && copyAgent.row - 2 == next.locRow && copyAgent.col == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PushNN));
                        copyAgent.row -= 1;
                    }
                    // NE Push
                    else if (copyAgent.row - 1 == current.locRow && copyAgent.col == current.locCol && copyAgent.row - 1 == next.locRow && copyAgent.col + 1 == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PushNE));
                        copyAgent.row -= 1;
                    }
                    // NW Push
                    else if (copyAgent.row - 1 == current.locRow && copyAgent.col == current.locCol && copyAgent.row - 1 == next.locRow && copyAgent.col - 1 == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PushNW));
                        copyAgent.row -= 1;
                    }
                    // WS Push
                    else if (copyAgent.row == current.locRow && copyAgent.col - 1 == current.locCol && copyAgent.row + 1 == next.locRow && copyAgent.col - 1 == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PushWS));
                        copyAgent.col -= 1;
                    }
                    // WN Push
                    else if (copyAgent.row == current.locRow && copyAgent.col - 1 == current.locCol && copyAgent.row - 1 == next.locRow && copyAgent.col - 1 == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PushWN));
                        copyAgent.col -= 1;
                    }
                    // WW Push
                    else if (copyAgent.row == current.locRow && copyAgent.col - 1 == current.locCol && copyAgent.row == next.locRow && copyAgent.col - 2 == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PushWW));
                        copyAgent.col -= 1;
                    }
                    // ES Push
                    else if (copyAgent.row == current.locRow && copyAgent.col + 1 == current.locCol && copyAgent.row + 1 == next.locRow && copyAgent.col + 1 == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PushES));
                        copyAgent.col += 1;
                    }
                    // EN Push
                    else if (copyAgent.row == current.locRow && copyAgent.col + 1 == current.locCol && copyAgent.row - 1 == next.locRow && copyAgent.col + 1 == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PushEN));
                        copyAgent.col += 1;
                    }
                    // EE Push
                    else if (copyAgent.row == current.locRow && copyAgent.col + 1 == current.locCol && copyAgent.row == next.locRow && copyAgent.col + 2 == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PushEE));
                        copyAgent.col += 1;
                    }
                    // SS Push
                    else if (copyAgent.row + 1 == current.locRow && copyAgent.col == current.locCol && copyAgent.row + 2 == next.locRow && copyAgent.col == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PushSS));
                        copyAgent.row += 1;
                    }
                    // SE Push
                    else if (copyAgent.row + 1 == current.locRow && copyAgent.col == current.locCol && copyAgent.row + 1 == next.locRow && copyAgent.col + 1 == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PushSE));
                        copyAgent.row += 1;
                    }
                    // SW Push
                    else if (copyAgent.row + 1 == current.locRow && copyAgent.col == current.locCol && copyAgent.row + 1 == next.locRow && copyAgent.col - 1 == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PushSW));
                        copyAgent.row += 1;
                    }


//                        // Agent Move South
//                        if (current.locRow < next.locRow && current.locCol == next.locCol) {
//                            actions.add(Triple.of(current, next, Action.PushSS));
//                            // Agent Move North
//                        } else if (current.locRow > next.locRow && current.locCol == next.locCol) {
//                            actions.add(Triple.of(current, next, Action.PushNN));
//                            // Agent Move East
//                        } else if (current.locCol < next.locCol && current.locRow == next.locRow) {
//                            actions.add(Triple.of(current, next, Action.PushEE));
//                            // Agent Move West
//                        } else if (current.locCol > next.locCol && current.locRow == next.locRow) {
//                            actions.add(Triple.of(current, next, Action.PushWW));
//                        }
//                    }
                }
                break;
            case PULL_BOX:
                // The Push Box is going to be from the perspective of the box...?
                Box box2 = blackboard.getBox(task.taskRow, task.taskCol);
                if (box2 == null) {
                    throw new RuntimeException("Box is null");
                }
                for (int i = 0; i < task.path.size() - 1; i++) {
//                    if (i != task.path.size() - 1) {
                    Vertex current = task.path.get(i);
                    Vertex next = task.path.get(i + 1);
                    // This is from the perspective of the agent
                    // NN Pull
                    if (copyAgent.row == current.locRow - 1 && copyAgent.col == current.locCol && copyAgent.row == next.locRow && copyAgent.col == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PullNN));
                        copyAgent.row -= 1;
                    }
                    // NE Pull
                    else if (copyAgent.row == current.locRow && copyAgent.col == current.locCol + 1 && copyAgent.row == next.locRow && copyAgent.col == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PullNE));
                        copyAgent.row -= 1;
                    }
                    // NW Pull
                    else if (copyAgent.row == current.locRow && copyAgent.col == current.locCol - 1 && copyAgent.row == next.locRow && copyAgent.col == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PullNW));
                        copyAgent.row -= 1;
                    }
                    // SS Pull
                    else if (copyAgent.row - 1 == current.locRow && copyAgent.col == current.locCol && copyAgent.row == next.locRow && copyAgent.col == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PullSS));
                        copyAgent.row += 1;
                    }
                    // SE Pull
                    else if (copyAgent.row == current.locRow && copyAgent.col == current.locCol + 1 && copyAgent.row == next.locRow && copyAgent.col == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PullSE));
                        copyAgent.row += 1;
                    }
                    // SW Pull
                    else if (copyAgent.row == current.locRow && copyAgent.col == current.locCol - 1 && copyAgent.row == next.locRow && copyAgent.col == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PullSW));
                        copyAgent.row += 1;
                    }
                    // EE Pull
                    else if (copyAgent.row == current.locRow && copyAgent.col - 1 == current.locCol && copyAgent.row == next.locRow && copyAgent.col == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PullEE));
                        copyAgent.col += 1;
                    }
                    // ES Pull
                    else if (copyAgent.row == current.locRow + 1 && copyAgent.col == current.locCol && copyAgent.row == next.locRow && copyAgent.col == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PullES));
                        copyAgent.col += 1;
                    }
                    // EN Pull
                    else if (copyAgent.row == current.locRow - 1 && copyAgent.col == current.locCol && copyAgent.row == next.locRow && copyAgent.col == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PullEN));
                        copyAgent.col += 1;
                    }
                    // WW Pull
                    else if (copyAgent.row == current.locRow && copyAgent.col == current.locCol - 1 && copyAgent.row == next.locRow && copyAgent.col == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PullWW));
                        copyAgent.col -= 1;
                    }
                    // WS Pull
                    else if (copyAgent.row == current.locRow + 1 && copyAgent.col == current.locCol && copyAgent.row == next.locRow && copyAgent.col == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PullWS));
                        copyAgent.col -= 1;
                    }
                    // WN Pull
                    else if (copyAgent.row == current.locRow - 1 && copyAgent.col == current.locCol && copyAgent.row == next.locRow && copyAgent.col == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PullWN));
                        copyAgent.col -= 1;
                    }

//                        // Agent Move South
//                        if (current.locRow < next.locRow && current.locCol == next.locCol) {
//                            actions.add(Triple.of(current, next, Action.PullSS));
//                            // Agent Move North
//                        } else if (current.locRow > next.locRow && current.locCol == next.locCol) {
//                            actions.add(Triple.of(current, next, Action.PullNN));
//                            // Agent Move East
//                        } else if (current.locCol < next.locCol && current.locRow == next.locRow) {
//                            actions.add(Triple.of(current, next, Action.PullEE));
//                            // Agent Move West
//                        } else if (current.locCol > next.locCol && current.locRow == next.locRow) {
//                            actions.add(Triple.of(current, next, Action.PullWW));
//                        }
//                    }
                }
                break;
            // TODO: Maybe we want to split this into two moves: one to move the agent to the box, and one to move the box to the goal
            case MOVE_BOX_TO_GOAL:
                // we need a case to handle if the path size is one or not
                // The problem here is that the agent needs to move to the box, agent -> task, and then from task->destination
                Box box3 = blackboard.getBox(task.taskRow, task.taskCol);
                if (box3 == null) {
                    throw new RuntimeException("Box is null");
                }

                for (int i = 0; i < task.path.size() - 2; i++) {
                    Vertex current = task.path.get(i);
                    Vertex next = task.path.get(i + 1);
//                    Vertex boxVertex = blackboard.getVertex(task.taskRow, task.taskCol);
//                    if (next.locRow == task.taskRow && next.locCol == task.taskCol){
                    // I am fairly certain this is the pain point
                    // This is from the perspective of the agent

                    // Donde esta el box?
                    // Agent Move South
                    if (current.locRow < next.locRow && current.locCol == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PushSS));
                        // Agent Move North
                    } else if (current.locRow > next.locRow && current.locCol == next.locCol) {
                        actions.add(Triple.of(current, next, Action.PushNN));
                        // Agent Move East
                    } else if (current.locCol < next.locCol && current.locRow == next.locRow) {
                        actions.add(Triple.of(current, next, Action.PushEE));
                        // Agent Move West
                    } else if (current.locCol > next.locCol && current.locRow == next.locRow) {
                        actions.add(Triple.of(current, next, Action.PushWW));
                    }

//                    if (current.locRow < next.locRow && box.row < next.locRow) {
//                            actions.add(Triple.of(current, next, Action.PullNN));
//                    } else if (current.locRow > next.locRow && box.row > next.locRow) {
//                        actions.add(Triple.of(current, next, Action.PullSS));
//                    } else if (current.locCol < next.locCol && box.col < next.locCol) {
//                        actions.add(Triple.of(current, next, Action.PullEE));
//                    } else if (current.locCol > next.locCol && box.col > next.locCol) {
//                        actions.add(Triple.of(current, next, Action.PullWW));
//                    } else if (current.locRow < next.locRow && box.row > next.locRow) {
//                        actions.add(Triple.of(current, next, Action.PushNN));
//                    } else if (current.locRow > next.locRow && box.row < next.locRow) {
//                        actions.add(Triple.of(current, next, Action.PushSS));
//                    } else if (current.locCol < next.locCol && box.col > next.locCol) {
//                        actions.add(Triple.of(current, next, Action.PushEE));
//                    } else if (current.locCol > next.locCol && box.col < next.locCol) {
//                        actions.add(Triple.of(current, next, Action.PushWW));
//                    }
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
                actions.add(Triple.of(new Vertex(task.agentRow, task.agentCol), new Vertex(task.agentRow, task.agentCol), Action.NoOp));
                break;
        }
        return actions;
    }
}
