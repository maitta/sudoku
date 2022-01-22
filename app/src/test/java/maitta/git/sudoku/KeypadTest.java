package maitta.git.sudoku;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.view.KeyEvent;
import android.view.animation.AnimationUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Enclosed.class)
public class KeypadTest{
    @Config(sdk = 29)
    // Being enclosed forces the class to be static
    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class KeypressTest {
        @Mock
        PuzzleView puzzleView;
        @Mock
        KeyEvent keyEvent;
        // Calling super(context) will fail if mocked with mockito. Roboelectric is the way to go.
        Context context;
        private Integer inputKeyCode;
        private Integer times;

        @Before
        public void setup(){
            if (context == null) {
                context =  RuntimeEnvironment.systemContext;
                MockitoAnnotations.initMocks(this);
            }
        }

        Keypad getKeypad(int[] useds){
            return new Keypad(context, useds, puzzleView);
        }

        public KeypressTest(Integer input, Integer times) {
            this.inputKeyCode = input;
            this.times = times;
        }

        @ParameterizedRobolectricTestRunner.Parameters(name = "keycode = {0}")
        public static Collection params(){
            return Arrays.asList(new Object[][] {
                    { KeyEvent.KEYCODE_1, 1 },
                    { KeyEvent.KEYCODE_2, 1 },
                    { KeyEvent.KEYCODE_3, 1 },
                    { KeyEvent.KEYCODE_4, 1 },
                    { KeyEvent.KEYCODE_5, 1 },
                    { KeyEvent.KEYCODE_6, 1 },
                    { KeyEvent.KEYCODE_7, 1 },
                    { KeyEvent.KEYCODE_8, 1 },
                    { KeyEvent.KEYCODE_9, 1 },
                    { KeyEvent.KEYCODE_11, 0 },
                    { KeyEvent.KEYCODE_12, 0 }
            });
        }

        @Test
        public void onKeyDownIsValid(){
            Keypad keypad = Mockito.spy(getKeypad(new int[]{}));
            keypad.onKeyDown(inputKeyCode, keyEvent);
            verify(keypad, times(times)).isValid(anyInt());
        }

        @Test
        public void resultIsSet(){
            Keypad keypad = Mockito.spy(getKeypad(new int[]{}));
            when(keypad.isValid(inputKeyCode)).thenReturn(true);
            keypad.onKeyDown(inputKeyCode, keyEvent);
            verify(puzzleView, times(times)).setSelectedTile(anyInt(), any(AnimationUtils.class));
        }
    }

    @Config(sdk = 29)
    @RunWith(RobolectricTestRunner.class)
    public static class tilesValidTest {
        private Keypad keypad;
        @Mock
        private PuzzleView puzzleView;
        private int useds[];
        private Context context;

        @Before
        public void setup(){
            if (context == null) {
                context =  RuntimeEnvironment.systemContext;
                MockitoAnnotations.initMocks(this);
            }
        }

        Keypad getKeypad(int[] useds){
            return new Keypad(context, useds, puzzleView);
        }

        @Test
        public void isTilesLowerRangeValid(){
            useds = new int[]{ 1,2,3,4,5 };
            keypad = getKeypad(useds);
            isAllValid(new int[]{ 6,7,8,9 });
        }

        @Test
        public void isTilesUpperRangeValid(){
            useds = new int[]{ 6,7,8,9 };
            keypad = getKeypad(useds);
            isAllValid(new int[]{ 1,2,3,4,5 });
        }

        private void isAllValid(int[] testCase){
            for(int i: testCase){
                Assert.assertTrue(keypad.isValid(i));
            }
        }

        @Test
        public void isTilesRangeInvalid(){
            useds = new int[]{ 1,2,3,4,5,6,7,8,9 };
            keypad = getKeypad(useds);
            for(int i: useds){
                Assert.assertFalse(keypad.isValid(i));
            }
        }
    }
}

