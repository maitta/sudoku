package maitta.git.sudoku.ViewModels;

import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class GameViewModel {
    // TODO multiple times defined
    private static final String TAG = "sudoku";

    private int puzzle[];
    public int[] getPuzzle(){
        return puzzle;
    }

    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_MEDIUM = 1;
    public static final int DIFFICULTY_HARD = 2;
    public static final String KEY_DIFFICULTY = "maitta.git.sudoku.difficulty";

    public static final String PREF_PUZZLE = "puzzle";
    public static final int DIFFICULTY_CONTINUE = -1;

    /**
     * Cache of used tiles
     * */
    private final int used[][][] = new int[9][9][];

    private final String easyPuzzle;
    private final String mediumPuzzle;
    private final String hardPuzzle;

    public String getEasyPuzzle(){
        return easyPuzzle;
    }
    public String getMediumPuzzle(){
        return mediumPuzzle;
    }
    public String getHardPuzzle(){
        return hardPuzzle;
    }

    /**
     * TODO For improving puzzle generation see these:
     * https://puzzling.stackexchange.com/questions/142/removing-numbers-from-a-full-sudoku-puzzle-to-create-one-with-a-unique-solution
     * https://gamedev.stackexchange.com/questions/56149/how-can-i-generate-sudoku-puzzles
     */
    public GameViewModel(){
        easyPuzzle = "000260701680070090190004500"
                + "820100040004602900050003028" + "009300074040050036703018000";
        mediumPuzzle = "020608000580009700000040000"
                + "370000500600000004008000013" + "000020000009800036000306090";
        hardPuzzle = "000600400700003600000091080"
                + "000000000050180003000306045" + "040200060903000000020000100";
    }

    /**
     * Given a difficulty level, come up with a new puzzle
     * @param diff easy, normal & hard
     * @param previousPuzzle will use previous puzzle to set current
     */
    public void setPuzzle(int diff, String previousPuzzle) {
        String puzzle;
        switch (diff) {
            case DIFFICULTY_CONTINUE:
                puzzle = previousPuzzle;
                break;
            case DIFFICULTY_HARD:
                puzzle = hardPuzzle;
                break;
            case DIFFICULTY_MEDIUM:
                puzzle = mediumPuzzle;
                break;
            case DIFFICULTY_EASY:
            default:
                puzzle = easyPuzzle;
                break;
        }
        this.puzzle = fromPuzzleString(puzzle);
    }

    /**
     * Convert a puzzle string into an array
     * */
    protected int[] fromPuzzleString(String string) {
        int[] puz = new int[string.length()];
        for (int i = 0; i < puz.length; i++) {
            puz[i] = string.charAt(i) - '0'; // integer parsing
        }
        return puz;
    }

    /**
     * Compute the two dimensional array of used tiles
     * */
    public void calculateUsedTiles() {
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                used[x][y] = calculateUsedTiles(x, y);
                Log.d(TAG, "used[" + x + "][" + y + "] = " + toPuzzleString(used[x][y]));
            }
        }
    }

    /**
     * Compute the used tiles visible from this position
     * */
    private int[] calculateUsedTiles(int x, int y) {
        int c[] = new int[9];
        // Horizontal
        for (int i = 0; i < 9; i++) {
            if (i == x)
                continue;
            int t = getTile(i, y);
            if (t != 0)
                c[t - 1] = t;
        }
        // Vertical
        for (int i = 0; i < 9; i++) {
            if (i == y)
                continue;
            int t = getTile(x, i);
            if (t != 0)
                c[t - 1] = t;
        }
        // Same cell block
        int startx = (x / 3) * 3;
        int starty = (y / 3) * 3;
        for (int i = startx; i < startx + 3; i++) {
            for (int j = starty; j < starty + 3; j++) {
                if (i == x && j == y)
                    continue;
                int t = getTile(i, j);
                if (t != 0)
                    c[t - 1] = t;
            }
        }
        // Compress
        int nused = 0;
        for (int t : c) {
            if (t != 0)
                nused++;
        }
        int c1[] = new int[nused];
        nused = 0;
        for (int t : c) {
            if (t != 0)
                c1[nused++] = t;
        }
        return c1;
    }

    /**
     * Convert an array into a puzzle string
     * */
    public String toPuzzleString(int[] puz) {
        StringBuilder buf = new StringBuilder();
        for (int element : puz) {
            buf.append(element);
        }
        return buf.toString();
    }

    /**
     * Return the tile at the given coordinates
     * */
    public int getTile(int x, int y) {
        return puzzle[y * 9 + x];
    }

    /**
     * Change the tile only if it's a valid move
     * */
    public boolean setTileIfValid(int x, int y, int value) {
        int tiles[] = getUsedTiles(x, y);
        if (value != 0) {
            for (int tile : tiles) {
                if (tile == value)
                    return false;
            }
        }
        setTile(x, y, value);
        calculateUsedTiles();
        return true;
    }

    /**
     * OBSOLETE Since Android 11, custom toasts/toast modifications are deprecated, according
     * to Google to "protect users". Default settings toasts can still be shown.
     * https://stackoverflow.com/questions/62884286/toast-getview-returns-null-on-android-11-api-30
     * @param toast
     */
    private void setToastTextSize(Toast toast){
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(18);
    }

    /**
     * Return cached used tiles visible from the given coords
     * */
    public int[] getUsedTiles(int x, int y) {
        return used[x][y];
    }

    /**
     * Change the tile at the given coordinates
     * */
    public void setTile(int x, int y, int value) {
        puzzle[y * 9 + x] = value;
    }

    /**
     * Return a string for the tile at the given coordinates
     * */
    public String getTileString(int x, int y) {
        int v = getTile(x, y);
        if (v == 0)
            return "";
        else
            return String.valueOf(v);
    }
}
