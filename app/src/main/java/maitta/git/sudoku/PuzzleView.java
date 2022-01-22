package maitta.git.sudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import maitta.git.sudoku.ViewModels.PuzzleViewViewModel;


public class PuzzleView extends View {
	private static final String TAG = "sudoku";
	private final Game game;
	private final PuzzleViewViewModel puzzleVM;

	private int selX; // X index of selection
	private int selY; // Y index of selection
	protected int getSelX(){return selX;}
	protected int getSelY(){return selY;}
	private final Rect selRect = new Rect();
	
	protected static final String SELX = "selX";
	protected static final String SELY = "selY";
	protected static final String VIEW_STATE = "viewState";

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable p = super.onSaveInstanceState();
		Log.d(TAG, "onSaveInstanceState");
		Bundle bundle = new Bundle();
		bundle.putInt(SELX, selX);
		bundle.putInt(SELY, selY);
		bundle.putParcelable(VIEW_STATE, p);
		return bundle;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Log.d(TAG, "onRestoreInstanceState");
		Bundle bundle = (Bundle) state;
		select(bundle.getInt(SELX), bundle.getInt(SELY));
		super.onRestoreInstanceState(bundle.getParcelable(VIEW_STATE));
	}

	public PuzzleView(Context context, Game game, PuzzleViewViewModel vm) {
		super(context);
		this.game = game;
		puzzleVM = vm;
		setFocusable(true);
		setFocusableInTouchMode(true);

		this.setId(R.id.puzzleId);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		puzzleVM.setWidth(w / 9f);
		puzzleVM.setHeight(h / 9f);
		puzzleVM.setRect(selX, selY, selRect);
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		Paint background = new Paint();
		puzzleVM.drawBackground(canvas, background, getWidth(), getHeight());
		puzzleVM.drawMinorGridLines(canvas, getWidth(), getHeight());
		puzzleVM.drawMajorGridLines(canvas, getWidth(), getHeight());
		Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
		puzzleVM.styleNumbers(foreground);
		puzzleVM.drawNumbers(canvas, foreground, game.getGameVM());
		puzzleVM.drawHints(canvas, game.getGameVM(),  new Rect(), new Paint());
		puzzleVM.drawSelection(canvas);
		// Background numbers need to be redrawn at this point for the selection to appear on screen.
		puzzleVM.drawNumbers(canvas, foreground, game.getGameVM());
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, "onKeyDown: keycode=" + keyCode + ", event=" + event);
		switch (keyCode) {
		// Handles D-pad input (directional pad)
		case KeyEvent.KEYCODE_DPAD_UP:
			select(selX, selY - 1);
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			select(selX, selY + 1);
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			select(selX - 1, selY);
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			select(selX + 1, selY);
			break;

		// Handles keyboard input
		case KeyEvent.KEYCODE_0:
		case KeyEvent.KEYCODE_SPACE:
			setSelectedTile(0);
			break;
		case KeyEvent.KEYCODE_1:
			setSelectedTile(1);
			break;
		case KeyEvent.KEYCODE_2:
			setSelectedTile(2);
			break;
		case KeyEvent.KEYCODE_3:
			setSelectedTile(3);
			break;
		case KeyEvent.KEYCODE_4:
			setSelectedTile(4);
			break;
		case KeyEvent.KEYCODE_5:
			setSelectedTile(5);
			break;
		case KeyEvent.KEYCODE_6:
			setSelectedTile(6);
			break;
		case KeyEvent.KEYCODE_7:
			setSelectedTile(7);
			break;
		case KeyEvent.KEYCODE_8:
			setSelectedTile(8);
			break;
		case KeyEvent.KEYCODE_9:
			setSelectedTile(9);
			break;
		// Handles center button on D-Pad (Ok button)
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			game.showKeypadOrError(selX, selY);
			break;		

		default:
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() != MotionEvent.ACTION_DOWN)
			return super.onTouchEvent(event);

		select((int) (event.getX() / puzzleVM.getWidth()),
				(int) (event.getY() / puzzleVM.getHeight()));
		game.showKeypadOrError(selX, selY);
		Log.d(TAG, "onTouchEvent: x " + selX + ", y " + selY);
		return true;
	}

	/**
	 * Convenience method with simpler signature.
	 * @param tile
	 */
	private void setSelectedTile(int tile){
		setSelectedTile(tile, new AnimationUtils());
	}

	public void setSelectedTile(int tile, AnimationUtils utils) {
		if (game.getGameVM().setTileIfValid(selX, selY, tile)) {
			invalidate();
		} else {
			// Number is not valid for this tile
			Log.d(TAG, "setSelectedTile: invalid: " + tile);
			startAnimation(loadAnimation(utils));
		}
	}

	/**
	 * loadAnimation is a framework's static method that cannot be mocked without powermockito
	 * and needs to be wrapped, spied upon and doreturn().when() for unit testing.
 	 */
	protected Animation loadAnimation(AnimationUtils utils){
		return utils.loadAnimation(game, R.anim.shake);
	}
	
	protected void select(int x, int y) {
		invalidate();
		selX = Math.min(Math.max(x, 0), 8);
		selY = Math.min(Math.max(y, 0), 8);
		puzzleVM.setRect(selX, selY, selRect);
		invalidate();
	}
}
