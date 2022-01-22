package maitta.git.sudoku;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Dialog;
import android.view.Menu;

import androidx.appcompat.app.AlertDialog;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;

import java.util.List;

@Config(sdk = 29)
@RunWith(RobolectricTestRunner.class)
public class GameTest {
    private static Game game;
    private static ShadowActivity shadowGame;
    // Internal ID not shown to the user.
    private static final String menuTitle = "New Game";

    @Before
    public void setMainActivity() {
        if (game == null) {
            game = Robolectric.buildActivity(Game.class).create().visible().get();
            shadowGame = shadowOf(game);
        }
    }
    @Test
    public void onCreateOptionsMenu() {
        Menu menu = shadowGame.getOptionsMenu();
        assertTrue(menu.hasVisibleItems());
        assertEquals(menu.findItem(R.id.newGame).isVisible(), true);
        assertEquals(menu.getItem(0).getTitle(), menuTitle);
    }

    @Test
    public void onOptionsItemSelected() {
        Menu menu = shadowGame.getOptionsMenu();
        assertTrue(game.onOptionsItemSelected(menu.getItem(0)));
        List<Dialog> dialogs = ShadowAlertDialog.getShownDialogs();
        assertFalse(dialogs.isEmpty());
        assertEquals(dialogs.size(), 1);
        AlertDialog newGameDialog = (AlertDialog) dialogs.get(0);
        assertTrue(newGameDialog.isShowing());
    }
}