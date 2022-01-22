package maitta.git.sudoku.ViewModels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static maitta.git.sudoku.ViewModels.GameViewModel.DIFFICULTY_CONTINUE;
import static maitta.git.sudoku.ViewModels.GameViewModel.DIFFICULTY_EASY;
import static maitta.git.sudoku.ViewModels.GameViewModel.DIFFICULTY_HARD;
import static maitta.git.sudoku.ViewModels.GameViewModel.DIFFICULTY_MEDIUM;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Config(sdk = 29)
@RunWith(RobolectricTestRunner.class)
public class GameViewModelTest {
    private GameViewModel gameVM;
    private GameViewModel gameVMspy;
    private Context context;
    private Random rd;

    private final String dummyPuzzle = "123456789";

    private final String easyPuzzle = "000260701680070090190004500"
            + "820100040004602900050003028" + "009300074040050036703018000";
    private final String mediumPuzzle = "020608000580009700000040000"
            + "370000500600000004008000013" + "000020000009800036000306090";
    private final String hardPuzzle = "000600400700003600000091080"
            + "000000000050180003000306045" + "040200060903000000020000100";

    @Before
    public void setup() {
        if (context == null) {
            context = ApplicationProvider.getApplicationContext();
            MockitoAnnotations.initMocks(this);
            gameVM = new GameViewModel();
            gameVMspy = spy(gameVM);
            rd = new Random();
        }
    }

    @Test
    public void setPuzzleContinue() {
        ArgumentCaptor stringCaptor = ArgumentCaptor.forClass(String.class);
        int[] dummyReturn = new int[]{1, 2, 3};
        doReturn(dummyReturn).when(gameVMspy).fromPuzzleString(anyString());
        gameVMspy.setPuzzle(DIFFICULTY_CONTINUE, dummyPuzzle);
        verify(gameVMspy).fromPuzzleString((String)stringCaptor.capture());
        assertEquals(stringCaptor.getValue(), dummyPuzzle);
        assertSame(dummyReturn, gameVMspy.getPuzzle());
    }

    @Test
    public void setPuzzleHardDifficulty() {
        ArgumentCaptor stringCaptor = ArgumentCaptor.forClass(String.class);
        doReturn(null).when(gameVMspy).fromPuzzleString(anyString());

        gameVMspy.setPuzzle(DIFFICULTY_HARD, dummyPuzzle);
        verify(gameVMspy).fromPuzzleString((String)stringCaptor.capture());
        assertEquals(stringCaptor.getValue(), hardPuzzle);
    }

    @Test
    public void setPuzzleNormalDifficulty() {
        ArgumentCaptor stringCaptor = ArgumentCaptor.forClass(String.class);
        doReturn(null).when(gameVMspy).fromPuzzleString(anyString());

        gameVMspy.setPuzzle(DIFFICULTY_MEDIUM, dummyPuzzle);
        verify(gameVMspy).fromPuzzleString((String)stringCaptor.capture());
        assertEquals(stringCaptor.getValue(), mediumPuzzle);
    }

    @Test
    public void setPuzzleEasyDifficulty() {
        ArgumentCaptor stringCaptor = ArgumentCaptor.forClass(String.class);
        doReturn(null).when(gameVMspy).fromPuzzleString(anyString());

        gameVMspy.setPuzzle(DIFFICULTY_EASY, dummyPuzzle);
        verify(gameVMspy).fromPuzzleString((String)stringCaptor.capture());
        assertEquals(stringCaptor.getValue(), easyPuzzle);
    }

    @Test
    public void fromPuzzleString() {
        int[] res = gameVM.fromPuzzleString(dummyPuzzle);
        assertEquals(dummyPuzzle, Arrays.toString(res).replaceAll("\\[|\\]|,|\\s", ""));
    }

    @Test
    public void calculateUsedTilesSetsValues() {
        List<int[]> usedTilesBefore, usedTilesAfter;
        gameVM.setPuzzle(DIFFICULTY_EASY, easyPuzzle);
        usedTilesBefore = getAllUsedTiles();
        gameVM.calculateUsedTiles();
        usedTilesAfter = getAllUsedTiles();
        assertEquals(usedTilesBefore.size(), usedTilesAfter.size());
        assertNotEquals(usedTilesBefore.hashCode(), usedTilesAfter.hashCode());
    }

    private List<int[]> getAllUsedTiles(){
        List<int[]> res = new ArrayList<>();
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                res.add(gameVM.getUsedTiles(x,y));
            }
        }
        return res;
    }

    @Test
    public void toPuzzleString() {
        int[] param = new int[]{1,2,3,4,5,6,7,8,9};
        String res = gameVM.toPuzzleString(param);
        assertEquals(dummyPuzzle, res);
    }

    @Test
    public void getTileEasyPuzzle() {
        gameVM.setPuzzle(DIFFICULTY_CONTINUE, easyPuzzle);
        for(int x = 0; x < 9; x++){
            for(int y = 0; y < 9; y++){
                int res = gameVM.getTile(x,y);
                int n = Character.getNumericValue(easyPuzzle.charAt(y * 9 + x));
                assertEquals(n, res);
            }
        }
    }

    @Test
    public void setTileIfNotValid() {
        int x = rd.nextInt(), y = rd.nextInt();
        int[] dummyRes = new int[]{1,2,3,4,5};
        doReturn(dummyRes).when(gameVMspy).getUsedTiles(x, y);
        for (int i : dummyRes) {
            boolean res = gameVMspy.setTileIfValid(x, y, i);
            assertFalse(res);
        }
    }

    @Test
    public void setTileIfValid() {
        int x = rd.nextInt(), y = rd.nextInt();
        int[] invalidValues = new int[]{6,7,8,9};
        int validValue = rd.nextInt(5)+1;
        ArgumentCaptor intCaptor1 = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor intCaptor2 = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor intCaptor3 = ArgumentCaptor.forClass(Integer.class);

        doReturn(invalidValues).when(gameVMspy).getUsedTiles(x, y);
        doNothing().when(gameVMspy).setTile(x, y, validValue);
        doNothing().when(gameVMspy).calculateUsedTiles();
        boolean res = gameVMspy.setTileIfValid(x, y, validValue);
        verify(gameVMspy).setTile((int)intCaptor1.capture(), (int)intCaptor2.capture(),
                (int)intCaptor3.capture());
        assertEquals(x, intCaptor1.getValue());
        assertEquals(y, intCaptor2.getValue());
        assertEquals(validValue, intCaptor3.getValue());
        verify(gameVMspy).calculateUsedTiles();
        assertTrue(res);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void getUsedTilesFails() {
        int maxIndex = 9;
        gameVM.getUsedTiles(maxIndex+1, maxIndex+1);
    }

    @Test
    public void setTile() {
        int maxBound = 9;
        int x = rd.nextInt(maxBound), y = rd.nextInt(maxBound), value = rd.nextInt(maxBound);
        gameVM.setPuzzle(DIFFICULTY_EASY, easyPuzzle);
        int prevValue = gameVM.getPuzzle()[y * 9 + x];
        gameVM.setTile(x, y, value);
        int curValue = gameVM.getPuzzle()[y * 9 + x];
        if(prevValue != value) assertNotEquals(prevValue, curValue);
        assertEquals(value, curValue);
    }

    @Test
    public void getTileString() {
        int maxBound = 9;
        int x = rd.nextInt(maxBound), y = rd.nextInt(maxBound);
        int randomRes = rd.nextInt(maxBound)+1;
        gameVMspy.setPuzzle(DIFFICULTY_EASY, easyPuzzle);
        doReturn(randomRes).when(gameVMspy).getTile(x, y);
        String res = gameVMspy.getTileString(x, y);
        assertEquals(String.valueOf(randomRes), res);
    }

    @Test
    public void getTileEmptyString() {
        int maxBound = 9;
        int x = rd.nextInt(maxBound), y = rd.nextInt(maxBound);
        gameVMspy.setPuzzle(DIFFICULTY_EASY, easyPuzzle);
        doReturn(0).when(gameVMspy).getTile(x, y);
        String res = gameVMspy.getTileString(x, y);
        assertEquals("", res);
    }
}