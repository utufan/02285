import os
import subprocess

SCRIPT_DIR = ""
LVL_DIR = ""
SEARCHCLIENT_DIR = ""


def set_path_variables():
    global SCRIPT_DIR, LVL_DIR, SEARCHCLIENT_DIR

    SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
    LVL_DIR = os.path.join(SCRIPT_DIR, "levels")
    SEARCHCLIENT_DIR = os.path.join(SCRIPT_DIR, "searchclient")

    print("Path variables set.")


def compile_searchclient():
    """Compile the searchclient."""
    subprocess.run(["javac", f"{SEARCHCLIENT_DIR}/*.java"])


def get_lvl_names():
    """Get all lvl names."""
    lvl_names = []
    for filename in os.listdir(LVL_DIR):
        if filename.endswith(".lvl"):
            lvl_names.append(filename)
    lvl_names.sort()
    return lvl_names


def select_lvl():
    """Provide a selection of all lvls that are in the folder path of lvl_file_path."""

    lvl_names = get_lvl_names()
    for i, lvl in enumerate(lvl_names):
        print(f"{i+1}: {lvl}")

    while True:
        selection = input(
            f"Enter the number of the lvl you want to solve (1-{len(lvl_names)}): ")
        try:
            selection = int(selection)
            if 1 <= selection <= len(lvl_names):
                return lvl_names[selection-1]
        except ValueError:
            pass
        print("Invalid selection.")


def solve_lvl(lvl_file, ram, heuristic):
    #
    # def solve_lvl(marker, ram, heuristic):
    # """Solve a lvl."""
    # mavis_jar_path = os.path.join(SCRIPT_DIR, "mavis.jar")\

    # if marker == True:
    #     lvl_path = os.path.join(LVL_DIR)
    #     searchclient_command = f"java -cp {SCRIPT_DIR}:{SEARCHCLIENT_DIR} {ram} searchclient.SearchClient {heuristic}"
    #     server_command = f"java -jar {mavis_jar_path} -l {lvl_path} -c \"{searchclient_command}\" -s 150 -t 180 -o 'logs.zip'"

    #     # lvl_names = get_lvl_names()
    #     # for lvl in lvl_names:

    # else:
    #     lvl = select_lvl()
    #     lvl_path = os.path.join(LVL_DIR, lvl)
    #     lvl_name = lvl.split(".")[0]
    #     searchclient_command = f"java -cp {SCRIPT_DIR}:{SEARCHCLIENT_DIR} {ram} searchclient.SearchClient {heuristic}"
    #     server_command = f"java -jar {mavis_jar_path} -l {lvl_path} -c \"{searchclient_command}\" -s 150 -t 180 -o 'logs/{lvl_name}.log'"
    """Solve a lvl."""
    mavis_jar_path = os.path.join(SCRIPT_DIR, "mavis.jar")
    lvl_path = os.path.join(LVL_DIR, lvl_file)
    lvl_name = lvl_file.split(".")[0]

    searchclient_command = f"java -cp {SCRIPT_DIR}:{SEARCHCLIENT_DIR} {ram} searchclient.SearchClient {heuristic}"
    server_command = f"java -jar {mavis_jar_path} -l {lvl_path} -c \"{searchclient_command}\" -s 150 -t 180 -o 'logs/{lvl_name}.log'"

    try:
        print(f"Solving level {lvl_path}...")
        result = subprocess.run(server_command, stdout=subprocess.PIPE,
                                stderr=subprocess.PIPE, shell=True, check=True, text=True)
        print(result.stdout)
    except subprocess.CalledProcessError as e:
        print(f"Error: {e.stderr}")


def choose_solver():
    options = {
        "dfs": "Depth-First Search",
        "bfs": "Breadth-First Search",
        "astar": "A* Search",
        "wastar": "Weighted A* Search"
    }

    print("\n\nPlease select a heuristic solver:\n")
    for key, value in options.items():
        print(f"- {key}: {value}")

    while True:
        solver = input("\nEnter the solver option (dfs/bfs/astar/wastar): \n")
        if solver in options.keys():
            return "-" + solver
        else:
            print("\nInvalid option. Please try again.\n")


def choose_ram():
    while True:
        ram = input("\nEnter the RAM option (1-16): \n")
        try:
            ram = int(ram)
            if ram >= 1 and ram <= 16:
                return "-Xmx{}G".format(ram)
            else:
                print("\nInvalid option. Please try again.\n")
        except ValueError:
            print("\nInvalid option. Please try again.\n")


def choose_multiple_levels():
    print("\n\nDo you want to solve multiple levels? ")
    print("y = multiple levels.")
    print("n = single level.")

    while True:
        choice = input("\nEnter your choice (y/n): \n")
        if choice == "y":
            return True
        elif choice == "n":
            return False
        else:
            print("\nInvalid option. Please try again.\n")


def main():
    """Start the server with a selected lvl."""
    set_path_variables()
    compile_searchclient()

    heuristic = choose_solver()
    ram = choose_ram()
    multiple_levels = choose_multiple_levels()

    if multiple_levels == True:
        lvl_names = get_lvl_names()
        for lvl in lvl_names:
            solve_lvl(lvl, ram, heuristic)

    else:
        lvl = select_lvl()
        solve_lvl(lvl, ram, heuristic)


if __name__ == "__main__":
    main()
