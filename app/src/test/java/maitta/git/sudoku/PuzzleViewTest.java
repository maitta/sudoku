package maitta.git.sudoku;

import static android.view.KeyEvent.KEYCODE_0;
import static android.view.KeyEvent.KEYCODE_1;
import static android.view.KeyEvent.KEYCODE_2;
import static android.view.KeyEvent.KEYCODE_3;
import static android.view.KeyEvent.KEYCODE_4;
import static android.view.KeyEvent.KEYCODE_5;
import static android.view.KeyEvent.KEYCODE_6;
import static android.view.KeyEvent.KEYCODE_7;
import static android.view.KeyEvent.KEYCODE_8;
import static android.view.KeyEvent.KEYCODE_9;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.KeyEvent.KEYCODE_SPACE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static maitta.git.sudoku.PuzzleView.SELX;
import static maitta.git.sudoku.PuzzleView.SELY;
import static maitta.git.sudoku.PuzzleView.VIEW_STATE;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import maitta.git.sudoku.ViewModels.GameViewModel;
import maitta.git.sudoku.ViewModels.PuzzleViewViewModel;

@RunWith(Enclosed.class)
public class PuzzleViewTest {
    @Config(sdk = 29)
    @RunWith(RobolectricTestRunner.class)
    public static class PuzzleViewNoParameterizedTest {
        PuzzleView puzzle;
        PuzzleView puzzleSpy;
        @Mock
        Game game;
        @Mock
        PuzzleViewViewModel puzzlevmMock;
        PuzzleViewViewModel puzzlevmSpy;
        Context context;
        Random rd;

        @Before
        public void setup() {
            if (context == null) {
                context = ApplicationProvider.getApplicationContext();
                MockitoAnnotations.initMocks(this);
                puzzlevmSpy = spy(new PuzzleViewViewModel(context));
                puzzle = new PuzzleView(context, game, puzzlevmMock);
                puzzleSpy = spy(puzzle);
                rd = new Random();
                when(game.getGameVM()).thenReturn(mock(GameViewModel.class));
            }
        }

        @Test
        public void setSelectedTileValid() {
            when(game.getGameVM().setTileIfValid(anyInt(), anyInt(), anyInt())).thenReturn(true);
            puzzleSpy.setSelectedTile(rd.nextInt(), new AnimationUtils());
            verify(puzzleSpy, times(1)).invalidate();
        }

        @Test
        public void setSelectedTileInvalid() {
            AnimationUtils utils = spy(AnimationUtils.class);
            Animation animation = mock(Animation.class);
            when(game.getGameVM().setTileIfValid(anyInt(), anyInt(), anyInt())).thenReturn(false);
            // Bc AnimationUtils.loadAnimation is a framework static method and Powermockito is not used,
            // 1. puzzle must be a spy, not a mock or a real object.
            // 2. loadAnimation static method call must be wrapped in a method inside the spy.
            // 3. doReturn().when() instead of when().thenReturn() must be called for stubbing.
            // https://stackoverflow.com/questions/20353846/mockito-difference-between-doreturn-and-when
            doReturn(animation).when(puzzleSpy).loadAnimation(any(AnimationUtils.class));
            puzzleSpy.setSelectedTile(rd.nextInt(), utils);
            verify(puzzleSpy, times(1)).startAnimation(animation);
        }

        @Test
        public void onSaveInstanceStateValuesSet() {
            Bundle bundle = (Bundle) puzzle.onSaveInstanceState();
            assertEquals(puzzle.getSelX(), bundle.getInt(SELX));
            assertEquals(puzzle.getSelY(), bundle.getInt(SELY));
            assertNotNull(bundle.getParcelable(puzzle.VIEW_STATE));
        }

        @Test
        public void onRestoreInstanceState() {
            Bundle bundle = mock(Bundle.class);
            puzzleSpy.onRestoreInstanceState(bundle);
            verify(puzzleSpy, times(1)).
                    select(bundle.getInt(SELX), bundle.getInt(SELY));
            verify(bundle, times(1)).getParcelable(VIEW_STATE);
        }

        @Test
        public void onSizeChangedRightMeasures() {
            int width = rd.nextInt();
            int height = rd.nextInt();
            PuzzleView puzzleWithSpy = new PuzzleView(context, game, puzzlevmSpy);
            puzzleWithSpy.onSizeChanged(width, height, rd.nextInt(), rd.nextInt());
            assertEquals(puzzlevmSpy.getWidth(), width / 9f, 0.1);
            assertEquals(puzzlevmSpy.getHeight(), height / 9f, 0.1);
        }

        @Test
        public void onDrawRightSequence() {
            Canvas canvas = mock(Canvas.class);
            puzzle.onDraw(canvas);
            InOrder orderVerifier = inOrder(puzzlevmMock);

            orderVerifier.verify(puzzlevmMock).
                    drawBackground(any(Canvas.class), any(Paint.class), anyInt(), anyInt());
            orderVerifier.verify(puzzlevmMock).
                    drawMinorGridLines(any(Canvas.class), anyInt(), anyInt());
            orderVerifier.verify(puzzlevmMock).
                    drawMajorGridLines(any(Canvas.class), anyInt(), anyInt());
            orderVerifier.verify(puzzlevmMock).styleNumbers(any(Paint.class));
            orderVerifier.verify(puzzlevmMock).
                    drawNumbers(any(Canvas.class), any(Paint.class), any(GameViewModel.class));
            orderVerifier.verify(puzzlevmMock).
                    drawHints(any(Canvas.class), any(GameViewModel.class), any(Rect.class), any(Paint.class));
            orderVerifier.verify(puzzlevmMock).drawSelection(any(Canvas.class));
            orderVerifier.verify(puzzlevmMock).
                    drawNumbers(any(Canvas.class), any(Paint.class), any(GameViewModel.class));
        }

        @Test
        public void onTouchEventActionDown() {
            MotionEvent event = mock(MotionEvent.class);
            when(event.getAction()).thenReturn(MotionEvent.ACTION_DOWN);
            boolean res = puzzleSpy.onTouchEvent(event);
            verify(game).showKeypadOrError(anyInt(), anyInt());
            verify(puzzleSpy).select(anyInt(), anyInt());
            assertTrue(res);
        }

        @Test
        public void onTouchEventNotActionDown() {
            MotionEvent event = mock(MotionEvent.class);
            when(event.getAction()).thenReturn(MotionEvent.ACTION_BUTTON_PRESS);
            boolean res = puzzle.onTouchEvent(event);
            verify(game, times(0)).showKeypadOrError(anyInt(), anyInt());
            assertFalse(res);
        }

        @Test
        public void selectInvalidateRightSequence() {
            int x = rd.nextInt(),y = rd.nextInt();
            puzzleSpy.select(x, y);
            InOrder orderVerifier = inOrder(puzzleSpy, puzzlevmMock);
            orderVerifier.verify(puzzleSpy).invalidate();
            orderVerifier.verify(puzzlevmMock).
                    setRect(anyInt(), anyInt(), any(Rect.class));
            orderVerifier.verify(puzzleSpy).invalidate();
        }

        @Test
        public void selectInvalidateRightParameters() {
            int x = rd.nextInt(Integer.MAX_VALUE),y = rd.nextInt(Integer.MAX_VALUE);
            puzzleSpy.select(x, y);
            ArgumentCaptor intCaptor1 = ArgumentCaptor.forClass(Integer.class);
            ArgumentCaptor intCaptor2 = ArgumentCaptor.forClass(Integer.class);
            verify(puzzlevmMock).setRect((Integer)intCaptor1.capture(),
                    (Integer)intCaptor2.capture(), any(Rect.class));
            assertEquals(Math.min(Math.max(x, 0), 8), intCaptor1.getValue());
            assertEquals(Math.min(Math.max(y, 0), 8), intCaptor2.getValue());
        }
    }

    @Config(sdk = 29)
    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class PuzzleViewParameterizedTest{
        PuzzleView puzzle;
        @Mock
        Game game;
        @Mock
        PuzzleViewViewModel puzzleVM;
        @Mock
        KeyEvent event;
        Context context;

        private int keycodeInput;
        private int paramInput;

        public PuzzleViewParameterizedTest(int keycodeInput, int paramInput) {
            this.keycodeInput = keycodeInput;
            this.paramInput = paramInput;
        }

        @Before
        public void setup() {
            if (context == null) {
                context = ApplicationProvider.getApplicationContext();
                MockitoAnnotations.initMocks(this);
                puzzle = new PuzzleView(context, game, puzzleVM);
            }
        }

        @ParameterizedRobolectricTestRunner.Parameters(name = "keycodeInput = {0}")
        public static Collection params(){
            int dummy = -100;
            return Arrays.asList(new Object[][] {
                    { KEYCODE_DPAD_UP, -1 },
                    { KEYCODE_DPAD_DOWN, 1 },
                    { KEYCODE_DPAD_LEFT, -1 },
                    { KEYCODE_DPAD_RIGHT, 1 },
                    { KEYCODE_0, 0 },
                    { KEYCODE_SPACE, 0 },
                    { KEYCODE_1, 1 },
                    { KEYCODE_2, 2 },
                    { KEYCODE_3, 3 },
                    { KEYCODE_4, 4 },
                    { KEYCODE_5, 5 },
                    { KEYCODE_6, 6 },
                    { KEYCODE_7, 7 },
                    { KEYCODE_8, 8 },
                    { KEYCODE_9, 9},
                    { KEYCODE_ENTER, dummy},
                    { KEYCODE_DPAD_CENTER, dummy}
            });
        }

        @Test
        public void onKeyDown() {
            PuzzleView puzzleSpy = spy(puzzle);
            int selx = puzzleSpy.getSelX();
            int sely = puzzleSpy.getSelY();
            ArgumentCaptor utilsCaptor = ArgumentCaptor.forClass(AnimationUtils.class);
            ArgumentCaptor intCaptor = ArgumentCaptor.forClass(Integer.class);
            doNothing().when(puzzleSpy).setSelectedTile(anyInt(), any(AnimationUtils.class));

            boolean res = puzzleSpy.onKeyDown(keycodeInput, event);
            switch (keycodeInput) {
                case KEYCODE_DPAD_UP:
                case KEYCODE_DPAD_DOWN:
                    verify(puzzleSpy).select(selx, sely + paramInput);
                    break;
                case KEYCODE_DPAD_LEFT:
                case KEYCODE_DPAD_RIGHT:
                    verify(puzzleSpy).select(selx + paramInput, sely);
                    break;
                case KEYCODE_0: case KEYCODE_SPACE: case KEYCODE_1: case KEYCODE_2: case KEYCODE_3:
                case KEYCODE_4: case KEYCODE_5: case KEYCODE_6: case KEYCODE_7: case KEYCODE_8:
                case KEYCODE_9:
                    verify(puzzleSpy).setSelectedTile((Integer) intCaptor.capture(),
                            (AnimationUtils) utilsCaptor.capture());
                    assertEquals(paramInput, intCaptor.getValue());
                    break;
                case KEYCODE_ENTER: case KEYCODE_DPAD_CENTER:
                    verify(game).showKeypadOrError(anyInt(), anyInt());
                    break;
            }
            assertTrue(res);
        }
    }
}