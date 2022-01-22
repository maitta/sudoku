package maitta.git.sudoku;

import static maitta.git.sudoku.ViewModels.GameViewModel.DIFFICULTY_CONTINUE;
import static maitta.git.sudoku.ViewModels.GameViewModel.KEY_DIFFICULTY;
import static maitta.git.sudoku.ViewModels.GameViewModel.PREF_PUZZLE;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import maitta.git.sudoku.ViewModels.GameViewModel;
import maitta.git.sudoku.ViewModels.PuzzleViewViewModel;

/**
 * Handles creation of puzzles in 3 difficulties. Saves current progress to the preferences (xml file)
 * see https://developer.android.com/training/basics/data-storage/shared-preferences.html#ReadSharedPreference
 */
public class Game extends AppCompatActivity {
	private static final String TAG = "sudoku";
	private PuzzleView puzzleView;
	private final GameViewModel gameVM = new GameViewModel();
	public GameViewModel getGameVM(){ return gameVM; }

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
		// Save the current puzzle
		getPreferences(MODE_PRIVATE).edit()
				.putString(PREF_PUZZLE, gameVM.toPuzzleString(gameVM.getPuzzle())).apply();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "game onCreate");

		setContentView(R.layout.game_activity);
		super.onCreate(savedInstanceState);
		setupActionBar();
		this.start(DIFFICULTY_CONTINUE);
		// If the activity is restarted, do a continue next time
		getIntent().putExtra(KEY_DIFFICULTY, DIFFICULTY_CONTINUE);
	}

	/**
	 * Sets icon and removes title from the action bar.
	 */
	private void setupActionBar(){
		ActionBar actionBar = getSupportActionBar();
		actionBar.setLogo(R.drawable.as_bar);
		actionBar.setDisplayUseLogoEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
	}

	/**
	 *  Open the keypad if there are any valid moves
	 *  */
	protected void showKeypadOrError(int x, int y) {
		int tiles[] = gameVM.getUsedTiles(x, y);
		if (tiles.length == 9) {
			Toast toast = Toast.makeText(this, R.string.no_moves_label, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		} else {
			Log.d(TAG, "showKeypad: used=" + gameVM.toPuzzleString(tiles));
			Dialog keyDialog = new Keypad(this, tiles, puzzleView);
			keyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			Window window = keyDialog.getWindow();
			window.setGravity(Gravity.BOTTOM);
			keyDialog.getWindow().setBackgroundDrawableResource(R.color.keypad_background);
			keyDialog.show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		boolean isDialog = false;
		switch (item.getItemId()) {
			case R.id.newGame:
				openNewGameDialog();
				isDialog = true;
				break;
		}
		return isDialog;
	}

	/**
	 * Instantiates a new puzzle with given difficulty.
	 * */
	private void start(int diff){
		Log.d(TAG, "starting new game " + diff);
		String previous = getPreferences(MODE_PRIVATE).getString(PREF_PUZZLE, gameVM.getEasyPuzzle());
		gameVM.setPuzzle(diff, previous);
		gameVM.calculateUsedTiles();
		Context context = getApplicationContext();
		puzzleView = new PuzzleView(context, this, new PuzzleViewViewModel(context));
		setContentView(puzzleView);
		puzzleView.requestFocus();
	}

	/**
	 * Pops up difficulty selection toast and starts a new game.
	 */
	private void openNewGameDialog() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.new_game_title)
				.setItems(R.array.difficulty,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int choice) {
								start(choice);
							}
						}).show();
	}
}
