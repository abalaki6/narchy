package spacegraph.obj.widget.console;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.SeparateTextGUIThread;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.virtual.DefaultVirtualTerminal;
import com.jogamp.opengl.GL2;
import spacegraph.Surface;
import spacegraph.render.Draw;

import java.io.IOException;

/**
 * Created by me on 4/1/16.
 */
public abstract class ConsoleSurface extends Surface implements Appendable {


    /**
     * height/width of each cell
     */
    final float charAspect = 1f;
    private int cols, rows;


    /**
     * percent of each grid cell width filled with the character
     */
    float charScaleX = 0.85f;

    /**
     * percent of each grid cell height filled with the character
     */
    float charScaleY = 0.85f;


    float bgAlpha = 0.35f;
    float fgAlpha = 0.9f;


    public ConsoleSurface(int cols, int rows) {
        resize(cols, rows);
    }

    public void resize(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        align(Align.Center, cols/1.5f, rows);
    }


    @Override
    public void paint(GL2 gl) {


        float charScaleX = this.charScaleX / charAspect;
        float charScaleY = this.charScaleY;




        long t = System.currentTimeMillis(); //HACK
        float dz = 0f;


        gl.glPushMatrix();


        gl.glScalef(1f / (cols), 1f / (rows), 1f);



        //background extents
        //gl.glColor3f(0.25f, 0.25f, 0.25f);
        //Draw.rect(gl, 0, 0, cols, rows);


        gl.glLineWidth(2f);


        for (int row = 0; row < rows; row++) {

            gl.glPushMatrix();

            Draw.textStart(gl,
                    charScaleX, charScaleY,
                    //0, (rows - 1 - jj) * charAspect,
                    0.5f,  (rows - 1 - row),
                    dz);

            for (int col = 0; col < cols; col++) {


                TextCharacter c = charAt(col, row);
                if (c == null)
                    continue;

                //TODO: Background color

//                    TextColor backColor = c.getBackgroundColor();
//                    if (backColor!=null) {
//
//                        gl.glColor4f(
//                                backColor.get(),
//                                backColor.green(), backColor.blue(), bgAlpha);
//                        Draw.rect(gl,
//                                (float) i, 0,
//                                (float) 1*16, charAspect*20
//                                ,-dz
//                        );
//                    }


                char cc = visible(c.getCharacter());
                if ((cc != 0) && (cc != ' ')) {
                    //TextColor fg = c.fore;

                    //TODO: if (!fg.equals(previousFG))
                    //gl.glColor4f(fg.red(), fg.green(), fg.blue(), fgAlpha);
                    gl.glColor3f(0.75f, 0.75f, 0.75f);

                    Draw.textNext(gl, cc, col/charScaleX);
                }
            }


            gl.glPopMatrix(); //next line


        }

        int[] cursor = getCursorPos();
        int curx = cursor[0];
        int cury = cursor[1];

        //DRAW CURSOR
        float p = (1f + (float) Math.sin(t / 100.0)) * 0.5f;
        float m = ( p);
        gl.glColor4f(1f, 0.7f, 0f, 0.4f + p * 0.4f);

        Draw.rect(gl,
                (float) (curx) + m/2f,
                (rows - 1 - cury),
                (float) (1 - m), (1 - m)
                , -dz
        );

        gl.glPopMatrix();

    }

    /** x,y aka col,row */
    public abstract int[] getCursorPos();

    abstract public TextCharacter charAt(int col, int row);


    public char visible(char cc) {
        //HACK: un-ANSIfy

        switch (cc) {
            case 9474:
                cc = '|';
                break;
            case 9472:
                cc = '-';
                break;
            case 9492:
                //..
            case 9496:
                cc = '*';
                break;
        }
        return cc;
    }




    public static class EditTerminal extends DefaultVirtualTerminal {
        public MultiWindowTextGUI gui;

        public EditTerminal(int c, int r) {
            super(new TerminalSize(c, r));


            //term.clearScreen();
            new Thread(() -> {

                try {
                    TerminalScreen screen = new TerminalScreen(this);
                    screen.startScreen();
                    gui = new MultiWindowTextGUI(
                            new SeparateTextGUIThread.Factory(),
                            screen);


                    setCursorVisible(true);

                    gui.setBlockingIO(false);
                    gui.setEOFWhenNoWindows(false);


                    final BasicWindow window = new BasicWindow();
                    window.setPosition(new TerminalPosition(0,0));
                    window.setSize(new TerminalSize(c-2,r-2));


                    TextBox t = new TextBox("sdf", TextBox.Style.MULTI_LINE);
                    t.setPreferredSize(new TerminalSize(c-3,r-3));

                    t.takeFocus();
                    window.setComponent(t);


                    gui.addWindow(window);
                    gui.setActiveWindow(window);

                    commit();
                    gui.waitForWindowToClose(window);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        }

        public void commit() {
            try {
                gui.updateScreen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    public static class Demo extends DefaultVirtualTerminal {
//        public Demo(int c, int r) {
//            super(c, r);
//
//            new Thread(() -> {
//
//                Screen screen = null;
//                try {
//
//                    //term.clearScreen();
//
//                    screen = new TerminalScreen(this);
//                    screen.startScreen();
//                    MultiWindowTextGUI gui = new MultiWindowTextGUI(
//                            new SeparateTextGUIThread.Factory(),
//                            screen);
//
//                    gui.setBlockingIO(false);
//                    gui.setEOFWhenNoWindows(false);
//
//
//                    final BasicWindow window = new BasicWindow("Grid layout test");
//                    addSampleWindow(gui, window);
//                    gui.updateScreen();
//
////                TextGraphics tGraphics = screen.newTextGraphics();
//////
////                tGraphics.setForegroundColor(new TextColor.RGB(0, 125, 255));
////                tGraphics.setBackgroundColor(new TextColor.RGB(255, 125, 5));
////                tGraphics.drawRectangle(
////                        new TerminalPosition(3,3), new TerminalPosition(10,10), '*');
//
//
////                PrintStream ps = new PrintStream(new ByteArrayOutputStream());
////                WriteInput w = new WriteInput(ps, term);
////                w.start();
//
//                    setCursorVisible(true);
//                    fore(TextColor.ANSI.YELLOW);
//                    back(TextColor.ANSI.BLUE);
//                    moveCursorTo(0, 0);
//                    put("XYZ\nABC\nCDDFS");
//
//
//                    new Thread(() -> {
//
//                        while(true) {
//                            try {
//                                put(Integer.toString((int)(10000 * Math.random()), 36));
//                                if (Math.random() < 0.5f)
//                                    put(' ');
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            Util.pause(100);
//                        }
//                    }).start();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }).start();
//
//
//        }
//
//        public static void addSampleWindow(MultiWindowTextGUI gui, final BasicWindow window) {
//            Panel leftGridPanel = new Panel();
//            leftGridPanel.setLayoutManager(new com.googlecode.lanterna.gui2.GridLayout(4));
//            add(leftGridPanel);
//            add(leftGridPanel, TextColor.ANSI.BLUE, 4, 2);
//            add(leftGridPanel, TextColor.ANSI.CYAN, 4, 2);
//            add(leftGridPanel, TextColor.ANSI.GREEN, 4, 2);
//
//            leftGridPanel.addComponent(new EmptySpace(TextColor.ANSI.MAGENTA, new TerminalPosition(4, 2))
//                    .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.CENTER, true, false, 4, 1)));
//            leftGridPanel.addComponent(new EmptySpace(TextColor.ANSI.RED, new TerminalPosition(4, 2))
//                    .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER, true, false, 4, 1)));
//            leftGridPanel.addComponent(new EmptySpace(TextColor.ANSI.YELLOW, new TerminalPosition(4, 2))
//                    .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER, true, false, 4, 1)));
//            leftGridPanel.addComponent(new EmptySpace(TextColor.ANSI.BLACK, new TerminalPosition(4, 2))
//                    .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.CENTER, true, false, 4, 1)));
//
//            Panel rightGridPanel = new Panel();
//            rightGridPanel.setLayoutManager(new GridLayout(5));
//            TextColor.ANSI c = TextColor.ANSI.BLACK;
//            int columns = 4;
//            int rows = 2;
//            add(rightGridPanel, c, columns, rows);
//            rightGridPanel.addComponent(new EmptySpace(TextColor.ANSI.MAGENTA, new TerminalPosition(4, 2))
//                    .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.BEGINNING, false, true, 1, 4)));
//            rightGridPanel.addComponent(new EmptySpace(TextColor.ANSI.RED, new TerminalPosition(4, 2))
//                    .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER, false, true, 1, 4)));
//            rightGridPanel.addComponent(new EmptySpace(TextColor.ANSI.YELLOW, new TerminalPosition(4, 2))
//                    .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.END, false, true, 1, 4)));
//            rightGridPanel.addComponent(new EmptySpace(TextColor.ANSI.BLACK, new TerminalPosition(4, 2))
//                    .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.FILL, false, true, 1, 4)));
//            add(rightGridPanel, TextColor.ANSI.BLUE, 4, 2);
//            add(rightGridPanel, TextColor.ANSI.CYAN, 4, 2);
//            add(rightGridPanel, TextColor.ANSI.GREEN, 4, 2);
//
//            Panel contentPanel = new Panel();
//            contentPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
//            contentPanel.addComponent(Panels.horizontal(leftGridPanel, new EmptySpace(TerminalPosition.ONE), rightGridPanel));
//            contentPanel.addComponent(new EmptySpace(TerminalPosition.ONE));
//            contentPanel.addComponent(new Button("Close", new Runnable() {
//                @Override
//                public void run() {
//                    window.close();
//                }
//            }));
//            window.setComponent(contentPanel);
//            gui.addWindow(window);
//        }
//
//        public static void add(Panel rightGridPanel, TextColor.ANSI c, int columns, int rows) {
//            rightGridPanel.addComponent(new EmptySpace(c, new TerminalPosition(columns, rows)));
//        }
//
//        public static void add(Panel leftGridPanel) {
//
//            add(leftGridPanel, TextColor.ANSI.BLACK, 4, 2);
//        }
//
//    }

}
