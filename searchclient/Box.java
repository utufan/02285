package searchclient;

// TODO: This is stupid because the box is pretty much the same code as the goal
public class Box {
    public int row;
    public int col;
    public String id;
    public Color color;

    // TODO: I don't know how we want to handle goals that might either be agent locations or box locations
    // For now, I am just assuming box locations, which might also stand to reason that the goal should know about
    // the box it is associated with
    public Box(int row, int col, String id, Color color) {
        this.row = row;
        this.col = col;
        this.id = id;
        this.color = color;
    }

    public Box(int row, int col, String id) {
        this.row = row;
        this.col = col;
        this.id = id;

    }

    public Box(String id, Color color) {
        this.id = id;
        this.color = color;
    }

    public String toString() {
        return "Box " + this.id + " with color " + this.color + " at " + this.row + ", " + this.col;
    }

    // TODO: This function is a filthy hack, but I can't think of another way right now
    public static String toAlphabetic(int i) {
        if (i < 0) {
            return "-" + toAlphabetic(-i - 1);
        }

        int quot = i / 26;
        int rem = i % 26;
        char letter = (char) ((int) 'A' + rem);
        if (quot == 0) {
            return String.valueOf(letter);
        } else {
            return toAlphabetic(quot - 1) + letter;
        }
    }

    public static int toNumeric(String s) {
        int result = 0;
        for (int i = 0; i < s.length(); i++) {
            result *= 26;
            result += s.charAt(i) - 'A' + 1;
        }
        return result - 1;
    }
}
