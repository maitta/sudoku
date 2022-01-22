package maitta.git.sudoku.ViewModels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static maitta.git.sudoku.ViewModels.PuzzleViewViewModel.numberOfClusters;
import static maitta.git.sudoku.ViewModels.PuzzleViewViewModel.numberOfTilesInCluster;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.core.content.ContextCompat;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import maitta.git.sudoku.Game;
import maitta.git.sudoku.R;


@RunWith(Enclosed.class)
public class PuzzleViewViewModelTest {
    @Config(sdk = 29)
    @RunWith(RobolectricTestRunner.class)
    public static class StylingAndDrawingSetupTest {
        @InjectMocks
        PuzzleViewViewModel puzzleVM;
        @Mock
        private Canvas canvas;
        @Mock
        private Rect rectangle;
        @Mock
        private Paint paint;
        @Mock
        private PuzzleViewViewModel.Board board;
        Context context;
        Random rd;

        @Before
        public void setup() {
            if (context == null) {
                /*
                context =  RuntimeEnvironment.systemContext works fine but it fails to load resources
                such as colours and some tests end up failing due to null pointers.
                context = RuntimeEnvironment.application works totally fine but it is deprecated.
                 */
                context = ApplicationProvider.getApplicationContext();
                MockitoAnnotations.initMocks(this);
                puzzleVM = new PuzzleViewViewModel(context);
                rd = new Random();
                puzzleVM.setWidth(rd.nextInt());
                puzzleVM.setHeight(rd.nextInt());
            }
        }

        @Test
        public void drawSelectionRectangle() {
            puzzleVM.drawSelection(canvas);
            verify(canvas, times(1)).drawRect(any(Rect.class), any(Paint.class));
        }

        @Test(expected = IllegalArgumentException.class)
        public void drawSelectionRectangleNullCanvas() {
            Canvas canvas = null;
            puzzleVM.drawSelection(canvas);
        }

        @Test
        public void styleNumbersSetsRightColor() {
            int color = ContextCompat.getColor(context, R.color.puzzle_foreground);
            puzzleVM.styleNumbers(paint);
            verify(paint).setColor(color);
        }

        @Test
        public void styleNumbersSetsRightStyle() {
            ArgumentCaptor argCaptor = ArgumentCaptor.forClass(Paint.Style.class);
            puzzleVM.styleNumbers(paint);
            verify(paint).setStyle((Paint.Style) argCaptor.capture());
            assertEquals(Paint.Style.FILL, argCaptor.getValue());
        }

        @Test
        public void styleNumbersSetsRightScale() {
            ArgumentCaptor argCaptor = ArgumentCaptor.forClass(Float.class);
            Float height = rd.nextFloat();
            Float width = rd.nextFloat();
            puzzleVM.setHeight(height);
            puzzleVM.setWidth(width);
            puzzleVM.styleNumbers(paint);
            verify(paint).setTextScaleX((Float) argCaptor.capture());
            assertEquals(width / height, argCaptor.getValue());
        }

        @Test
        public void drawMajorGridLines() {
            int numberOfMajorLines = 3 * 4;
            puzzleVM.drawMajorGridLines(canvas, rd.nextInt(), rd.nextInt());
            verify(canvas, times(numberOfMajorLines)).drawLine(anyInt(), anyFloat(), anyInt(),
                    anyFloat(), any(Paint.class));
        }

        @Test
        public void drawMinorGridLines() {
            int numberOfMinorLines = 9 * 4;
            puzzleVM.drawMinorGridLines(canvas, rd.nextInt(), rd.nextInt());
            verify(canvas, times(numberOfMinorLines)).drawLine(anyInt(), anyFloat(), anyInt(),
                    anyFloat(), any(Paint.class));
        }

        @Test
        public void drawBackground() {
            Paint background = mock(Paint.class);
            int width = rd.nextInt();
            int height = rd.nextInt();
            puzzleVM.drawBackground(canvas, background, width, height);
            verify(background, times(1)).setColor(anyInt());
            verify(canvas, times(1)).drawRect(0, 0, width, height,
                    background);
        }

        @Test
        public void drawHintsOnMovesEdgeCase() {
            Game game = mock(Game.class);
            Rect rect = mock(Rect.class);
            Paint paint = mock(Paint.class);
            int maximumSudokuMoves = 9;
            when(game.getGameVM()).thenReturn(mock(GameViewModel.class));
            when(game.getGameVM().getUsedTiles(anyInt(), anyInt())).
                    thenReturn(new int[maximumSudokuMoves + 1]);
            Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> {
                puzzleVM.drawHints(canvas, game.getGameVM() , rect, paint);
            });
            String expectedMessage = "out of bounds";
            String actualMessage = exception.getMessage();

            assertTrue(actualMessage.contains(expectedMessage));
        }

        @Test
        public void drawNumbersRightParametersAndInvocations() {
            Game game = mock(Game.class);
            Paint.FontMetrics fm = mock(Paint.FontMetrics.class);
            fm.ascent = rd.nextFloat();
            fm.descent = rd.nextFloat();
            when(paint.getFontMetrics()).thenReturn(fm);
            String dummy = "some mocked value";
            when(game.getGameVM()).thenReturn(mock(GameViewModel.class));
            when(game.getGameVM().getTileString(anyInt(), anyInt())).thenReturn(dummy);
            puzzleVM.drawNumbers(canvas, paint, game.getGameVM());

            ArgumentCaptor argCaptor1 = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor argCaptor2 = ArgumentCaptor.forClass(Float.class);
            ArgumentCaptor argCaptor3 = ArgumentCaptor.forClass(Float.class);
            ArgumentCaptor argCaptor4 = ArgumentCaptor.forClass(Paint.class);
            int times = numberOfClusters * numberOfTilesInCluster;
            verify(canvas, times(times)).drawText((String) argCaptor1.capture(), (Float) argCaptor2.capture(),
                    (Float) argCaptor3.capture(), (Paint) argCaptor4.capture());

            List<String> arg1List = argCaptor1.getAllValues();
            List<Float> arg2List = argCaptor2.getAllValues();
            List<Float> arg3List = argCaptor3.getAllValues();
            List<Paint> arg4List = argCaptor4.getAllValues();
            final float x = puzzleVM.getWidth() / 2;
            final float y = puzzleVM.getHeight() / 2 - (fm.ascent + fm.descent) / 2;
            int index = 0;
            for (int i = 0; i < numberOfClusters; i++) {
                for (int j = 0; j < numberOfTilesInCluster; j++) {
                    assertEquals(dummy, arg1List.get(index));
                    assertEquals(i * puzzleVM.getWidth() + x, arg2List.get(index), 0.1);
                    assertEquals(j * puzzleVM.getHeight() + y, arg3List.get(index), 0.1);
                    assertEquals(paint, arg4List.get(index));
                    index++;
                }
            }
        }


        @Test
        public void getRect() {
            Rect rect = spy(Rect.class);
            final int x = rd.nextInt(Integer.MAX_VALUE);
            final int y = rd.nextInt(Integer.MAX_VALUE);
            float width = puzzleVM.getWidth();
            float height = puzzleVM.getHeight();
            puzzleVM.setRect(x, y, rect);
            assertEquals(rect.left, (int)(x * width));
            assertEquals(rect.top, (int)(y * height));
            assertEquals(rect.right, (int)(x * width + width));
            assertEquals(rect.bottom, (int)(y * height + height));
        }
    }

    @Config(sdk = 29)
    // Forces the class to be static
    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class DrawHintsAndNumbersTest {
        // Spy instead of real object so that it can be used to call 'verify'.
        @Spy
        PuzzleViewViewModel puzzleVM = new PuzzleViewViewModel(ApplicationProvider.
                getApplicationContext());
        @Mock
        private Canvas canvas;
        private int[] usedTilesInput;
        private int times;

        @Before
        public void setup() {
            MockitoAnnotations.initMocks(this);
        }

        public DrawHintsAndNumbersTest(int[] input, int times) {
            this.usedTilesInput = input;
            this.times = times;
        }

        @ParameterizedRobolectricTestRunner.Parameters(name = "usedTilesInput = {0}")
        public static Collection params(){
            return Arrays.asList(new Object[][] {
                    { new int[0], 0 },
                    { new int[1], 0 },
                    { new int[2], 0 },
                    { new int[3], 0 },
                    { new int[4], 0 },
                    { new int[5], 0 },
                    { new int[6], 0 },
                    { new int[7], numberOfClusters * numberOfTilesInCluster },
                    { new int[8], numberOfClusters * numberOfTilesInCluster },
                    { new int[9], numberOfClusters * numberOfTilesInCluster }
            });
        }

        @Test
        public void drawHintsOnMoves() {
            Game game = mock(Game.class);
            Rect rect = mock(Rect.class);
            Paint paint = mock(Paint.class);
            when(game.getGameVM()).thenReturn(mock(GameViewModel.class));
            when(game.getGameVM().getUsedTiles(anyInt(), anyInt())).thenReturn((usedTilesInput));
            puzzleVM.drawHints(canvas, game.getGameVM(), rect, paint);
            verify(puzzleVM, times(times)).setRect(anyInt(), anyInt(), any(Rect.class));
            verify(paint, times(times)).setColor(anyInt());
            verify(canvas, times(times)).drawRect(rect, paint);
        }
    }
}