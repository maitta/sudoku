package maitta.git.sudoku.ViewModels;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import androidx.core.content.ContextCompat;

import maitta.git.sudoku.R;

public class PuzzleViewViewModel {
    private static final String TAG = "sudoku";
    private final Rect selRect = new Rect();
    private Canvas canvas;
    private Context context;
    private Board board;
    private float width; // width of one tile
    private float height; // height of one tile
    public static final int numberOfClusters = 9;
    public static final int numberOfTilesInCluster = 9;
    public static final int numberOfMoves = 9;
    public static final int numberOfClustersInRow = 3;

    public float getWidth(){ return this.width; }
    public void setWidth(float width){
        this.width = width;
        Log.d(TAG, "PuzzleVM size changed: width " + width);
    }
    public float getHeight(){ return this.height; }
    public void setHeight(float height){
        this.height = height;
        Log.d(TAG, "PuzzleVM size changed: height " + height);
    }

    public class Board{
        private Paint dark, light, highlight;

        public Paint getDark() {
            return dark;
        }
        public Paint getLight() {
            return light;
        }
        public Paint getHighlight() {
            return highlight;
        }
        public Board(Paint dark, Paint light, Paint highlight){
            this.dark = dark;
            this.light = light;
            this.highlight = highlight;
        }
    }

    public PuzzleViewViewModel(Context context) {
        this.context = context;
    }

    public void drawBackground(Canvas canvas, Paint background, int screenWidth, int screenHeight) {
        background.setColor(ContextCompat.getColor(context, R.color.game_background));
        canvas.drawRect(0, 0, screenWidth, screenHeight, background);
    }

    public void drawMajorGridLines(Canvas canvas, int screenWidth, int screenHeight) {
        if(this.board == null){
            board = drawBoard();
        }
        for (int i = 0; i < numberOfClusters; i++) {
            if (i % numberOfClustersInRow != 0)
                continue;
            canvas.drawLine(0, i * height, screenWidth, i * height, board.getDark());
            canvas.drawLine(0, i * height + 1, screenWidth, i * height + 1,
                    board.getHighlight());
            canvas.drawLine(i * width, 0, i * width, screenHeight, board.getDark());
            canvas.drawLine(i * width + 1, 0, i * width + 1, screenHeight,
                    board.getHighlight());
        }
    }

    public void drawMinorGridLines(Canvas canvas, int screenWidth, int screenHeight) {
        if(this.board == null){
            board = drawBoard();
        }
        for (int i = 0; i < numberOfTilesInCluster; i++) {
            canvas.drawLine(0, i * height, screenWidth, i * height, board.getLight());
            canvas.drawLine(0, i * height + 1, screenWidth, i * height + 1,
                    board.getHighlight());
            canvas.drawLine(i * width, 0, i * width, screenHeight, board.getLight());
            canvas.drawLine(i * width + 1, 0, i * width + 1, screenHeight,
                    board.getHighlight());
        }
    }

    public void drawSelection(Canvas canvas) {
        if(canvas == null) throw new IllegalArgumentException("Canvas cannot be null");
        int selectedColor = ContextCompat.getColor(context, R.color.puzzle_selected_tile);
        Log.d(TAG, "selRect=" + selRect);
        Paint selected = new Paint();
        selected.setColor(selectedColor);
        canvas.drawRect(selRect, selected);
    }

    public void styleNumbers(Paint foreground) {
        int color = ContextCompat.getColor(context, R.color.puzzle_foreground);
        foreground.setColor(color);
        foreground.setStyle(Paint.Style.FILL);
        foreground.setTextSize(height * 0.75f);
        foreground.setTextScaleX(width / height);
        foreground.setTextAlign(Paint.Align.CENTER);
    }

    public Board drawBoard(){
        // Define colors for the grid lines
        final Paint dark = new Paint();
        dark.setColor(ContextCompat.getColor(this.context, R.color.puzzle_main_gridlines));
        final Paint highlight = new Paint();
        highlight.setColor(ContextCompat.getColor(this.context, R.color.puzzle_gridlines));
        final Paint light = new Paint();
        light.setColor(ContextCompat.getColor(this.context, R.color.puzzle_light));

        return new Board(dark, light, highlight);
    }

    /**
     * TODO decide whether or not to include the hints as an optional or fixed thing.
     * Picks a hint color based on #moves left
     * @param canvas
     */
    public void drawHints(Canvas canvas, GameViewModel gameVM, Rect r, Paint hint) {
        int c[] = { ContextCompat.getColor(context, R.color.puzzle_hint_no_move),
                ContextCompat.getColor(context, R.color.puzzle_hint_one_move),
                ContextCompat.getColor(context, R.color.puzzle_hint_two_moves) };
        for (int i = 0; i < numberOfClusters; i++) {
            for (int j = 0; j < numberOfTilesInCluster; j++) {
                int movesLeft = numberOfMoves - gameVM.getUsedTiles(i, j).length;
                if (movesLeft < c.length) {
                    setRect(i, j, r);
                    hint.setColor(c[movesLeft]);
                    canvas.drawRect(r, hint);
                }
            }
        }
    }

    public void drawNumbers(Canvas canvas, Paint foreground, GameViewModel gameVM) {
        // Draw the number in the center of the tile
        Paint.FontMetrics fm = foreground.getFontMetrics();
        // Centering in X: use alignment (and X at midpoint)
        final float x = getWidth() / 2;
        // Centering in Y: measure ascent/descent first
        final float y = getHeight() / 2 - (fm.ascent + fm.descent) / 2;

        for (int i = 0; i < numberOfClusters; i++) {
            for (int j = 0; j < numberOfTilesInCluster; j++) {
                canvas.drawText(gameVM.getTileString(i, j), i * getWidth() + x, j
                        * getHeight() + y, foreground);
            }
        }
    }

    public void setRect(int x, int y, Rect rect) {
        float width = getWidth();
        float height = getHeight();
        rect.set((int) (x * width), (int) (y * height),
                (int) (x * width + width), (int) (y * height + height));
    }
}
