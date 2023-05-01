package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SearchClient
{
    public static State parseLevel(BufferedReader serverMessages)
            throws IOException
    {
        // We can assume that the level file is conforming to specification, since the server verifies this.
        // Read domain
        serverMessages.readLine(); // #domain
        serverMessages.readLine(); // hospital

        // Read Level name
        serverMessages.readLine(); // #levelname
        serverMessages.readLine(); // <name>

        // Read colors
        serverMessages.readLine(); // #colors
        Color[] agentColors = new Color[10];
        Color[] boxColors = new Color[26];
        String line = serverMessages.readLine();
        while (!line.startsWith("#"))
        {
            String[] split = line.split(":");
            Color color = Color.fromString(split[0].strip());
            String[] entities = split[1].split(",");
            for (String entity : entities)
            {
                char c = entity.strip().charAt(0);
                if ('0' <= c && c <= '9')
                {
                    agentColors[c - '0'] = color;
                }
                else if ('A' <= c && c <= 'Z')
                {
                    boxColors[c - 'A'] = color;
                }
            }
            line = serverMessages.readLine();
        }

        // Read initial state
        // line is currently "#initial"
        int numRows = 0;
        int numCols = 0;
        ArrayList<String> levelLines = new ArrayList<>(64);
        line = serverMessages.readLine();
        while (!line.startsWith("#"))
        {
            levelLines.add(line);
            numCols = Math.max(numCols, line.length());
            ++numRows;
            line = serverMessages.readLine();
        }
        int numAgents = 0;
        int[] agentRows = new int[10];
        int[] agentCols = new int[10];
        boolean[][] walls = new boolean[numRows][numCols];
        char[][] boxes = new char[numRows][numCols];
        for (int row = 0; row < numRows; ++row)
        {
            line = levelLines.get(row);
            for (int col = 0; col < line.length(); ++col)
            {
                char c = line.charAt(col);

                if ('0' <= c && c <= '9')
                {
                    agentRows[c - '0'] = row;
                    agentCols[c - '0'] = col;
                    ++numAgents;
                }
                else if ('A' <= c && c <= 'Z')
                {
                    boxes[row][col] = c;
                }
                else if (c == '+')
                {
                    walls[row][col] = true;
                }
            }
        }

        int numRows2 = levelLines.size();
        int numCols2 = levelLines.stream().mapToInt(String::length).max().orElse(0);

        int[][] intMap = new int[numRows2][numCols2];
        for (int i = 0; i < numRows2; i++) {
            String lineToRead = levelLines.get(i);
            int lineLength = lineToRead.length();

            for (int j = 0; j < numCols2; j++) {
                if (j >= lineLength || lineToRead.charAt(j) == '+') {
                    intMap[i][j] = -1; // wall or obstacle
                } else {
                    intMap[i][j] = i * numCols2 + j; // empty cell
                }
            }
        }

        int numVertices = numRows2 * numCols2;
        List<List<Edge>> graph = new ArrayList<>();

        for (int i = 0; i < numVertices; i++) {
            graph.add(new ArrayList<>());
        }

        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, 1, 0, -1};
        double[][] dist = new double[numVertices][numVertices];

        for (int i = 0; i < numRows2; i++) {
            for (int j = 0; j < numCols2; j++) {
                if (intMap[i][j] == -1) continue;

                for (int k = 0; k < 4; k++) {
                    int ni = i + dx[k];
                    int nj = j + dy[k];

                    if (ni >= 0 && ni < numRows2 && nj >= 0 && nj < numCols2 && intMap[ni][nj] != -1) {
                        double distVal = Math.sqrt((ni - i) * (ni - i) + (nj - j) * (nj - j)); // Euclidean distance
                        graph.get(intMap[i][j]).add(new Edge(intMap[ni][nj], distVal));
                    }
                }
            }
        }

        int INF = 1000000000; // a very large number to represent infinity

        // Compute the shortest distance between every pair of vertices using Floyd-Warshall algorithm
        for (int i = 0; i < numVertices; i++) {
            Arrays.fill(dist[i], INF);
            dist[i][i] = 0;
        }

        for (int u = 0; u < numVertices; u++) {
            int i = u / numCols2;
            int j = u % numCols2;

            for (Edge e : graph.get(u)) {
                int v = e.to;
                int ni = v / numCols2;
                int nj = v % numCols2;

                double distVal = Math.sqrt((ni - i) * (ni - i) + (nj - j) * (nj - j)); // Euclidean distance
                dist[u][v] = distVal;
            }
        }

        for (int k = 0; k < numVertices; k++) {
            for (int i = 0; i < numVertices; i++) {
                for (int j = 0; j < numVertices; j++) {
                    dist[i][j] = Math.min(dist[i][j], dist[i][k] + dist[k][j]);
                }
            }
        }

        System.err.println("dist: \n" + Arrays.deepToString(dist));


        printDistancesFromCell(intMap,dist,3,2);

        System.err.println(getDistance(intMap,dist,3,2,3,7));

        agentRows = Arrays.copyOf(agentRows, numAgents);
        agentCols = Arrays.copyOf(agentCols, numAgents);

        // Read goal state
        // line is currently "#goal"
        char[][] goals = new char[numRows][numCols];
        line = serverMessages.readLine();
        int row = 0;
        while (!line.startsWith("#"))
        {
            for (int col = 0; col < line.length(); ++col)
            {
                char c = line.charAt(col);

                if (('0' <= c && c <= '9') || ('A' <= c && c <= 'Z'))
                {
                    goals[row][col] = c;
                }
            }

            ++row;
            line = serverMessages.readLine();
        }

        // End
        // line is currently "#end"

        return new State(agentRows, agentCols, agentColors, walls, boxes, boxColors, goals);
    }


    public static double getDistance(int[][] intMap, double[][] dist, int startX, int startY, int endX, int endY) {
        int startVertex = intMap[startX][startY];
        int endVertex = intMap[endX][endY];
        return dist[startVertex][endVertex];
    }

    public static void printDistancesFromCell(int[][] intMap, double[][] dist, int startRow, int startCol) {
        int numRows = intMap.length;
        int numCols = intMap[0].length;

        // Print row labels and distances
        System.err.println("Distances from cell (" + startRow + "," + startCol + "):");

        // Print column labels
        System.err.print("       ");
        for (int j = 0; j < numCols; j++) {
            System.err.printf("%4d", j);
        }
        System.err.println();


        for (int i = 0; i < numRows; i++) {
            System.err.printf("%4d   ", i);
            for (int j = 0; j < numCols; j++) {
                if (intMap[i][j] == -1) {
                    System.err.printf("%4s", "####");
                } else {
                    double distance = dist[intMap[startRow][startCol]][intMap[i][j]];
                    System.err.printf("%4.0f", distance);
                }
            }
            System.err.println();
        }
    }





    public static Action[][] search(State initialState, Frontier frontier)
    {
        System.err.format("Starting %s.\n", frontier.getName());

        return GraphSearch.search(initialState, frontier);
    }

    public static void main(String[] args)
            throws IOException
    {
        // Use stderr to print to the console.
        System.err.println("SearchClient initializing. I am sending this using the error output stream.");

        // Send client name to server.
        System.out.println("SearchClient");

        // We can also print comments to stdout by prefixing with a #.
        System.out.println("#This is a comment.");

        // Parse the level.
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII));
        State initialState = SearchClient.parseLevel(serverMessages);

        // Select search strategy.
        Frontier frontier;
        if (args.length > 0)
        {
            switch (args[0].toLowerCase(Locale.ROOT))
            {
                case "-bfs":
                    frontier = new FrontierBFS();
                    break;
                case "-dfs":
                    frontier = new FrontierDFS();
                    break;
                case "-astar":
                    frontier = new FrontierBestFirst(new HeuristicAStar(initialState));
                    break;
                case "-wastar":
                    int w = 5;
                    if (args.length > 1)
                    {
                        try
                        {
                            w = Integer.parseUnsignedInt(args[1]);
                        }
                        catch (NumberFormatException e)
                        {
                            System.err.println("Couldn't parse weight argument to -wastar as integer, using default.");
                        }
                    }
                    frontier = new FrontierBestFirst(new HeuristicWeightedAStar(initialState, w));
                    break;
                case "-greedy":
                    frontier = new FrontierBestFirst(new HeuristicGreedy(initialState));
                    break;
                default:
                    frontier = new FrontierBFS();
                    System.err.println("Defaulting to BFS search. Use arguments -bfs, -dfs, -astar, -wastar, or " +
                            "-greedy to set the search strategy.");
            }
        }
        else
        {
            frontier = new FrontierBFS();
            System.err.println("Defaulting to BFS search. Use arguments -bfs, -dfs, -astar, -wastar, or -greedy to " +
                    "set the search strategy.");
        }

        // Search for a plan.
        Action[][] plan;
        try
        {
            plan = SearchClient.search(initialState, frontier);
        }
        catch (OutOfMemoryError ex)
        {
            System.err.println("Maximum memory usage exceeded.");
            plan = null;
        }

        // Print plan to server.
        if (plan == null)
        {
            System.err.println("Unable to solve level.");
            System.exit(0);
        }
        else
        {
            System.err.format("Found solution of length %,d.\n", plan.length);

            for (Action[] jointAction : plan)
            {
                System.out.print(jointAction[0].name);
                for (int action = 1; action < jointAction.length; ++action)
                {
                    System.out.print("|");
                    System.out.print(jointAction[action].name);
                }
                System.out.println();
                // We must read the server's response to not fill up the stdin buffer and block the server.
                serverMessages.readLine();
            }
        }
    }
}