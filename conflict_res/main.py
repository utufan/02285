from enum import Enum
from collections import namedtuple
import heapq

class State:
    def __init__(self, positions):
        self.positions = positions

    def __getitem__(self, key):
        return self.positions[key]

    def __eq__(self, other):
        if isinstance(other, State):
            return self.positions == other.positions
        return False

    def __hash__(self):
        return hash(tuple(sorted(self.positions.items())))

    def __lt__(self, other):
        return self.h < other.h




    def copy(self):
        return State(self.positions.copy())




class ActionType(Enum):
    NoOp = 0
    Move = 1
    Push = 2
    Pull = 3

Action = namedtuple('Action', ['name', 'action_type', 'agent_dx', 'agent_dy', 'box_dx', 'box_dy'])

ACTIONS = [
    Action("NoOp", ActionType.NoOp, 0, 0, 0, 0),
    Action("Move(N)", ActionType.Move, -1, 0, 0, 0),
    Action("Move(S)", ActionType.Move, 1, 0, 0, 0),
    Action("Move(E)", ActionType.Move, 0, 1, 0, 0),
    Action("Move(W)", ActionType.Move, 0, -1, 0, 0),
    Action("Push(W,S)", ActionType.Push, 0, -1, 1, 0),
    Action("Push(W,N)", ActionType.Push, 0, -1, -1, 0),
    Action("Push(W,W)", ActionType.Push, 0, -1, 0, -1),
    Action("Push(E,S)", ActionType.Push, 0, 1, 1, 0),
    Action("Push(E,N)", ActionType.Push, 0, 1, -1, 0),
    Action("Push(E,E)", ActionType.Push, 0, 1, 0, 1),
    Action("Push(N,N)", ActionType.Push, -1, 0, -1, 0),
    Action("Push(N,E)", ActionType.Push, -1, 0, 0, 1),
    Action("Push(N,W)", ActionType.Push, -1, 0, 0, -1),
    Action("Push(S,S)", ActionType.Push, 1, 0, 1, 0),
    Action("Push(S,E)", ActionType.Push, 1, 0, 0, 1),
    Action("Push(S,W)", ActionType.Push, 1, 0, 0, -1),
    Action("Pull(N,N)", ActionType.Pull, -1, 0, -1, 0),
    Action("Pull(N,E)", ActionType.Pull, -1, 0, 0, 1),
    Action("Pull(N,W)", ActionType.Pull, -1, 0, 0, -1),
    Action("Pull(S,S)", ActionType.Pull, 1, 0, 1, 0),
    Action("Pull(S,W)", ActionType.Pull, 1, 0, 0, -1),
    Action("Pull(S,E)", ActionType.Pull, 1, 0, 0, 1),
    Action("Pull(E,S)", ActionType.Pull, 0, 1, 1, 0),
    Action("Pull(E,E)", ActionType.Pull, 0, 1, 0, 1),
    Action("Pull(E,N)", ActionType.Pull, 0, 1, -1, 0),
    Action("Pull(W,N)", ActionType.Pull, 0, -1, -1, 0),
    Action("Pull(W,S)", ActionType.Pull, 0, -1, 1, 0),
    Action("Pull(W,W)", ActionType.Pull, 0, -1, 0, -1),
]



def parse_level_file(file_path):
    with open(file_path, 'r') as f:
        lines = f.readlines()

    domain = None
    level_name = None
    colors = {}
    initial_state = []
    goal_state = []

    mode = None
    for line in lines:
        line = line.strip()
        if line.startswith('#'):
            if line == '#domain':
                mode = 'domain'
            elif line == '#levelname':
                mode = 'level_name'
            elif line == '#colors':
                mode = 'colors'
            elif line == '#initial':
                mode = 'initial'
            elif line == '#goal':
                mode = 'goal'
        else:
            if mode == 'domain':
                domain = line
            elif mode == 'level_name':
                level_name = int(line)
            elif mode == 'colors':
                color, items = line.split(':')
                colors[color.strip()] = [item.strip() for item in items.split(',')]
            elif mode == 'initial':
                initial_state.append(line)
            elif mode == 'goal':
                goal_state.append(line)

    return {
        'domain': domain,
        'level_name': level_name,
        'colors': colors,
        'initial_state': initial_state,
        'goal_state': goal_state,
    }






level_data = [
    "++++++++++++",
    "+        10+",
    "+  + +++++++",
    "+  +       +",
    "+  +       +",
    "+BD+++++++ +",
    "+CA+       +",
    "++++++++++++"
]





start = State({
    'agent0': (1, 9),
    'agent1': (1, 10),
    'boxA': (5, 1),
    'boxB': (5, 0),
    'boxC': (6, 1),
    'boxD': (6, 0)
})

goal = State({
    'agent0': (6, 9),
    'agent1': (6, 10),
    'boxA': (6, 2),
    'boxB': (6, 3),
    'boxC': (6, 4),
    'boxD': (6, 5)
})

def get_neighbors(coord, level_data):
    x, y = coord
    neighbors = []

    if x > 0 and level_data[y][x-1] != "+":
        neighbors.append((x-1, y))
    if x < len(level_data[0]) - 1 and level_data[x+1][y] != "+":
        neighbors.append((x+1, y))
    if y > 0 and level_data[x][y-1] != "+":
        neighbors.append((x, y-1))
    if y < len(level_data) - 1 and level_data[x][y+1] != "+":
        neighbors.append((x, y+1))

    return neighbors

def heuristic(a, b):
    return abs(a[0] - b[0]) + abs(a[1] - b[1])



def a_star_search(level_data, start, end):
    frontier = []
    heapq.heappush(frontier, (0, start))
    came_from = {start: None}
    cost_so_far = {start: 0}

    while frontier:
        _, current = heapq.heappop(frontier)

        if current == end:
            break

        for next_node in get_neighbors(current, level_data):
            new_cost = cost_so_far[current] + 1
            if next_node not in cost_so_far or new_cost < cost_so_far[next_node]:
                cost_so_far[next_node] = new_cost
                priority = new_cost + heuristic(end, next_node)
                heapq.heappush(frontier, (priority, next_node))
                came_from[next_node] = current

    if end not in came_from:
        return None

    # Reconstruct the path
    path = [end]
    while path[-1] != start:
        path.append(came_from[path[-1]])
    path.reverse()

    return path

def resolve_conflicts(paths, level_data):
    conflicts = True

    while conflicts:
        conflicts = False
        timesteps = max([len(path) for path in paths.values()])

        for t in range(timesteps):
            agents_positions = {}
            for agent, path in paths.items():
                if t < len(path):
                    position = path[t]
                    if position in agents_positions:
                        other_agent = agents_positions[position]
                        paths[other_agent].insert(t, paths[other_agent][t - 1])
                        conflicts = True
                        break
                    else:
                        agents_positions[position] = agent

            if conflicts:
                break

    return paths


level_data2 = [
    "++++++++++++",
    "+          +",
    "+  + +++++++",
    "+  +   01  +",
    "+  +       +",
    "+  +++++++ +",
    "+  +       +",
    "++++++++++++"
]

solution_paths = {
    'agent0': [(1, 1), (1, 2), (1, 3), (1, 4), (2, 4), (3, 4), (3, 5), (3, 6), (3, 7)],
    'agent1': [(1, 7), (1, 6), (1, 5), (1, 4), (2, 4), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8)]
}

{'agent0': [(1, 1), (1, 2), (1, 3), (1, 3), (1, 4), (2, 4), (3, 4), (3, 5), (3, 6), (3, 7)], 
'agent1': [(1, 7), (1, 6), (1, 5), (1, 4), (2, 4), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8)]}

resolved_paths = resolve_conflicts(solution_paths, level_data2)
print(resolved_paths)























