package searchclient;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Stack;

public interface Frontier
{
    void add(State state);
    State pop();
    boolean isEmpty();
    int size();
    boolean contains(State state);
    String getName();
}

class FrontierBFS
        implements Frontier
{
    private final ArrayDeque<State> queue = new ArrayDeque<>(65536);
    private final HashSet<State> set = new HashSet<>(65536);

    @Override
    public void add(State state)
    {
        this.queue.addLast(state);
        this.set.add(state);
    }

    @Override
    public State pop()
    {
        State state = this.queue.pollFirst();
        this.set.remove(state);
        return state;
    }

    @Override
    public boolean isEmpty()
    {
        return this.queue.isEmpty();
    }

    @Override
    public int size()
    {
        return this.queue.size();
    }

    @Override
    public boolean contains(State state)
    {
        return this.set.contains(state);
    }

    @Override
    public String getName()
    {
        return "breadth-first search";
    }
}

class FrontierDFS
        implements Frontier
{
    private final Stack<State> stack = new Stack<>();
    private final HashSet<State> set = new HashSet<>(65536);

    @Override
    public void add(State state)
    {
        this.stack.push(state);
        this.set.add(state);
    }

    @Override
    public State pop()
    {
        State state = this.stack.pop();
        this.set.remove(state);
        return state;
    }

    @Override
    public boolean isEmpty()
    {
        return this.stack.isEmpty();
    }

    @Override
    public int size()
    {
        return this.stack.size();
    }

    @Override
    public boolean contains(State state)
    {
        return this.set.contains(state);
    }

    @Override
    public String getName()
    {
        return "depth-first search";
    }
}

class FrontierBestFirst
        implements Frontier
{
    private Heuristic heuristic;
    private PriorityQueue<State> queue;
    private final HashSet<State> set = new HashSet<>(65536);

    public FrontierBestFirst(Heuristic h)
    {
        this.heuristic = h;
        this.queue = new PriorityQueue<>(65536, h);
    }

    @Override
    public void add(State state)
    {
        this.queue.add(state);
        this.set.add(state);
    }

    @Override
    public State pop()
    {
        // This is where we want to minimize the heuristic value.
        State state = this.queue.poll();
        this.set.remove(state);
        return state;
    }

    @Override
    public boolean isEmpty()
    {
        return this.queue.isEmpty();
    }

    @Override
    public int size()
    {
        return this.queue.size();
    }

    @Override
    public boolean contains(State state)
    {
        return this.set.contains(state);
    }

    @Override
    public String getName()
    {
        return String.format("best-first search using %s", this.heuristic.toString());
    }
}
