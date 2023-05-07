package searchclient;

/*
    This interface is used to update the blackboard with new information from the knowledge sources.
    I believe the knowledge sources are the agents and boxes, but not the goals since the goals are static.
    There might also be situations in which a goal is completed, but then the box on the goal needs to move
    because it is blocking other tasks from being accomplished.
 */
public interface KnowledgeSource {
    void updateBlackboard();

}
