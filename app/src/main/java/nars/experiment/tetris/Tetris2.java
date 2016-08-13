package nars.experiment.tetris;

import com.google.common.collect.Lists;
import com.gs.collections.api.block.function.primitive.FloatToObjectFunction;
import nars.$;
import nars.NAR;
import nars.NARLoop;
import nars.Param;
import nars.data.AutoClassifier;
import nars.experiment.NAREnvironment;
import nars.experiment.arkanoid.Arkancide;
import nars.experiment.tetris.visualizer.TetrisVisualizer;
import nars.index.CaffeineIndex;
import nars.nal.Tense;
import nars.nar.Default;
import nars.nar.util.DefaultConceptBuilder;
import nars.op.VariableCompressor;
import nars.op.time.MySTMClustered;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;
import nars.term.obj.Termject;
import nars.time.FrameClock;
import nars.truth.Truth;
import nars.util.data.random.XORShiftRandom;
import nars.util.data.random.XorShift128PlusRandom;
import nars.util.math.RangeNormalizedFloat;
import nars.util.signal.MotorConcept;
import nars.util.signal.SensorConcept;
import spacegraph.Surface;
import spacegraph.math.Vector2f;
import spacegraph.obj.ConsoleSurface;
import spacegraph.obj.GridSurface;
import spacegraph.obj.MatrixView;
import spacegraph.obj.Plot2D;

import java.io.IOException;
import java.util.*;

import static nars.$.t;
import static nars.experiment.tetris.TetrisState.*;
import static spacegraph.obj.ControlSurface.newControlWindow;
import static spacegraph.obj.GridSurface.VERTICAL;

/**
 * Created by me on 7/28/16.
 */
public class Tetris2 extends NAREnvironment {

    public static final int TIME_DILATION = 0; //resolution in between frames for interpolation space
    public static final int DEFAULT_INDEX_WEIGHT = 12 * 1000000;

    static {
        Param.DEBUG = false;
        Param.CONCURRENCY_DEFAULT = 1;
    }

    public static final int runFrames = 100;
    public static final int cyclesPerFrame = 24;
    public static final int tetris_width = 6;
    public static final int tetris_height = 12;
    public static final int TIME_PER_FALL = 3;
    static boolean easy = false;

    static int frameDelay;



    private final TetrisState state;
    private final int visionSyncPeriod = 4; //16 * TIME_DILATION;

    public class View {

        public TetrisVisualizer vis;
        public Surface plot1;
        public ConsoleSurface term = new ConsoleSurface(40, 8);

        public Surface camView;
        //public Plot2D lstm;
    }

    final View view = new View();


    private MotorConcept motorRotate;
    //private MotorConcept motorDown;
    private MotorConcept motorLeftRight;
    private final boolean rotate = !easy;

    /**
     * @param width
     * @param height
     * @param timePerFall larger is slower gravity
     */
    public Tetris2(NAR nar, int width, int height, int timePerFall) {
        super(nar);

        state = new TetrisState(width, height, timePerFall) {
            @Override
            protected int nextBlock() {


                if (easy) {
                    //EASY MODE
                    return 1; //square blocks
                    //return 0; //long blocks
                } else {
                    return super.nextBlock(); //all blocks
                }
            }
        };
        view.vis = new TetrisVisualizer(state, 64, false) {
            @Override
            public boolean onKey(Vector2f hitPoint, char charCode, boolean pressed) {

                switch (charCode) {
                    case 'a': nar.goal(motorRotate, Tense.Present, pressed ? 0f: 0.5f, gamma); break;
                    case 's': nar.goal(motorRotate, Tense.Present, pressed ? 1f: 0.5f, gamma); break;
                    case 'z': nar.goal(motorLeftRight, Tense.Present, pressed ? 0f: 0.5f, gamma); break;
                    case 'x': nar.goal(motorLeftRight, Tense.Present, pressed ? 1f: 0.5f, gamma); break;
                }

                return true;
            }
        };

        //restart();
    }

    //TODO
    public static class NARCam {
        public int width;/*how wide our board is*/
        public int height;/*how tall our board is*/



    }

    /** RLE/scanline input method: groups similar pixels (monochrome) into a runline using a integer range */
    protected void input() {

//        float thresh = 0.5f;
//
//        inputAxis(thresh, true);
//        inputAxis(thresh, false);
    }

    private void inputAxis(float thresh, boolean horizontal) {
        int hh = horizontal ? state.height : state.width;
        for (int y = 0; y < hh; ) {

            int start = 0, end = 0;
            int sign = 0;

            int ww = horizontal ? state.width : state.height;
            for (int x = 0; x < ww; ) {

                int i;
                if (horizontal)
                    i = y * ww + x;
                else
                    i = x * hh + y;

                float s = state.seen[i];

                if (x == 0) {
                    //beginning of span
                    sign = (int) Math.signum(s);
                } else {

                    if (sign > 0) {
                        if (s < (thresh)) {
                            sign = -1;
                        } else {
                            end = x;  //continue span
                        }
                    }
                    if (sign < 0) {
                        if (s > (1f - thresh)) {
                            sign = +1;
                        } else {
                            end = x; //continue span
                        }
                    }
                }

                //if it switched or reach the end of the line
                if (end!=x || (x >= ww-1)) {
                    //end of span
                    if (end-start == 1) {
                        inputBlock(start, start+1, sign, horizontal);
                    } else {
                        inputSpan(start, end, y, sign, horizontal);
                    }
                }

                x++;
            }

            y++;
        }
    }

    private void inputSpan(int start, int end, int axis, int sign, boolean horizontal) {

        Truth t = $.t(sign>0 ? 1f : 0f,
            //(float)Math.pow(alpha, end-start)
            alpha
        );
        if (t == null)
            return; //too low confidence

        Term range = new Termject.IntInterval(start, end);
        Term fixed = new Termject.IntTerm(axis);

        //TODO collect evidence stamp
        nar.believe(
                horizontal ? $.p(range, fixed) : $.p(fixed, range),
                Tense.Present,
                t.freq(), t.conf() //HACK this parameters sux
        );
    }
    private void inputBlock(int x, int y, float v, boolean horizontal) {

        Truth t = $.t(v>0 ? 1f : 0f,
                //(float)Math.pow(alpha, end-start)
                alpha
        );
        if (t == null)
            return; //too low confidence

        //TODO collect evidence stamp
        nar.believe(
                horizontal ? $.p(x, y) : $.p(y, x),
                Tense.Present,
                t.freq(), t.conf() //HACK this parameters sux
        );
    }

    @Override
    protected void init(NAR nar) {

        state.seen = new float[state.width * state.height];

        for (int y = 0; y < state.height; y++) {
            int yy = y;
            for (int x = 0; x < state.width; x++) {
                int xx = x;
                Compound squareTerm = $.p(new Termject.IntTerm(x), new Termject.IntTerm(y));
                sensors.add(new SensorConcept(squareTerm, nar,
                        () -> state.seen[yy * state.width + xx] > 0 ? 1f : 0f,

                        //null //disable input

                        (v) -> $.t(v,alpha)

                ).timing(0, visionSyncPeriod));

            }
        }


        if (rotate) {
            actions.add(motorRotate = new MotorConcept("(rotate)", nar));
        }
        //actions.add(motorDown = new MotorConcept("(down)", nar));
        actions.add(motorLeftRight = new MotorConcept("(leftright)", nar));

        reset();
    }

    @Override
    public float act() {


        float rotateMotivation = (motorRotate != null && motorRotate.hasGoals()) ? motorRotate.goals().expectation(now) : 0.5f;
        //float downMotivation = motorDown.hasGoals() ? motorDown.goals().expectation(now) : 0.5f;
        float leftRightMotivation = motorLeftRight.hasGoals() ? motorLeftRight.goals().expectation(now) : 0.5f;

        float actionMargin = 0.3f;
        float actionThresholdHigh = 1f - actionMargin;
        float actionThresholdLow = actionMargin;

        if (rotateMotivation > actionThresholdHigh) {
            state.take_action(CW);
        } else if (rotateMotivation < actionThresholdLow) {
            state.take_action(CCW);
        }
//        if (downMotivation > actionThresholdHigh) {
//            state.take_action(FALL);
//        }

        if (leftRightMotivation > actionThresholdHigh) {
            state.take_action(RIGHT);
        } else if (leftRightMotivation < actionThresholdLow) {
            state.take_action(LEFT);
        }

        if (state.running) {
            state.take_action(-1); //actions already taken above
            state.update();
        } else {
            state.spawn_block();
        }

        state.checkIfRowAndScore();

        state.toVector(false, state.seen);


        if (state.gameOver()) {
            reset();
        }

        input();

        return state.score;
    }

    public void reset() {
        state.reset();
        state.spawn_block();
        state.running = true;
    }


    public static void main(String[] args) {
        Random rng = new XorShift128PlusRandom(1);

        //Multi nar = new Multi(3,512,
        Default nar = new Default(2048,
                16, 2, 2, rng,
                new CaffeineIndex(new DefaultConceptBuilder(rng), DEFAULT_INDEX_WEIGHT, false)

                , new FrameClock()) {

            VariableCompressor.Precompressor p = new VariableCompressor.Precompressor(this);

            @Override
            protected Task preprocess(Task input) {
                return p.pre(input);
            }

        };

        nar.inputActivation.setValue(0.03f);
        nar.derivedActivation.setValue(0.03f);


        nar.beliefConfidence(0.8f);
        nar.goalConfidence(0.7f);
        nar.DEFAULT_BELIEF_PRIORITY = 0.25f;
        nar.DEFAULT_GOAL_PRIORITY = 0.75f;
        nar.DEFAULT_QUESTION_PRIORITY = 0.25f;
        nar.DEFAULT_QUEST_PRIORITY = 0.4f;
        nar.cyclesPerFrame.set(cyclesPerFrame);
        nar.confMin.setValue(0.03f);
        nar.truthResolution.setValue(0.02f);

//        nar.on(new TransformConcept("seq", (c) -> {
//            if (c.size() != 3)
//                return null;
//            Term X = c.term(0);
//            Term Y = c.term(1);
//
//            Integer x = intOrNull(X);
//            Integer y = intOrNull(Y);
//            Term Z = (x!=null && y!=null)? ((Math.abs(x-y) <= 1) ? $.the("TRUE") : $.the("FALSE")) : c.term(2);
//
//
//            return $.inh($.p(X, Y, Z), $.oper("seq"));
//        }));
//        nar.believe("seq(#1,#2,TRUE)");
//        nar.believe("seq(#1,#2,FALSE)");

        //nar.log();
        //nar.logSummaryGT(System.out, 0.1f);

//		nar.log(System.err, v -> {
//			if (v instanceof Task) {
//				Task t = (Task)v;
//				if (t instanceof DerivedTask && t.punc() == '!')
//					return true;
//			}
//			return false;
//		});

        //Global.DEBUG = true;

        //new Abbreviation2(nar, "_");

        MySTMClustered stm = new MySTMClustered(nar, 256, '.', 2);
        MySTMClustered stmGoal = new MySTMClustered(nar, 64, '!', 2);

        //new ArithmeticInduction(nar);
        //new VariableCompressor(nar);



        Tetris2 t = new Tetris2(nar, tetris_width, tetris_height, TIME_PER_FALL) {

            @Override
            public void init(NAR nar) {
                super.init(nar);

                AutoClassifier ac = new AutoClassifier($.the("row"), nar, sensors, tetris_width, 3, 0.05f);

//                newControlWindow(
//                        new GridSurface(VERTICAL,
//                                charted.stream().map(c -> new BeliefTableChart(nar, c)).collect(toList())
//                        )
//                );


                //BagChart.show((Default) nar, 1024);

                //STMView.show(stm, 800, 600);

                int plotHistory = 256;
                Plot2D plot = new Plot2D(plotHistory, Plot2D.Line);
                plot.add("Rwrd", () -> rewardValue);

                Plot2D plot2 = new Plot2D(plotHistory, Plot2D.Line);
                plot2.add("Busy", () -> nar.emotion.busy.getSum());
                plot2.add("Lern", () -> nar.emotion.busy.getSum() - nar.emotion.frustration.getSum());
                //plot2.add("Strs", ()->nar.emotion.stress.getSum());

                Plot2D plot3 = new Plot2D(plotHistory, Plot2D.Line);
                plot3.add("Hapy", () -> nar.emotion.happy.getSum());
                plot3.add("Sad", () -> nar.emotion.sad.getSum());

//                Plot2D plot4 = new Plot2D(plotHistory, Plot2D.Line);
//                plot4.add("Errr", ()->nar.emotion.errr.getSum());


                view.plot1 = new GridSurface(VERTICAL, plot, plot2, plot3);



//                {
//                    List<FloatSupplier> li = new ArrayList();
//                    for (int i = 0; i < sensors.size(); i++) {
//                        li.add(sensors.get(i).getInput());
//                    }
//
//                    List<FloatSupplier> lo = new ArrayList();
//                    RangeNormalizedFloat normReward = new RangeNormalizedFloat(() -> rewardValue);
//                    lo.add(normReward);
////
////
//                    LSTMPredictor lp = new LSTMPredictor(li, lo, 1);
////
//
//                    double[] lpp = new double[2];
//                    nar.onFrame(nn -> {
//                        double[] p = lp.next();
//                        System.arraycopy(p, 0, lpp, 0, p.length);
//                        //System.out.println("LSTM: " + Texts.n4(p) + " , " + normReward.asFloat());
//                    });
//
//                    view.lstm = new Plot2D(plotHistory, Plot2D.Line)
//                            .add("Reward (actual)", () -> normReward.asFloat())
//                            .add("Predicted", () -> lpp[0]);
//
//
//                }

                nar.onFrame(f -> {
                    plot.update();
                    plot2.update();
                    plot3.update();
                    //view.lstm.update();
                    try {
                        view.term.term.putLinePre(summary());
                    } catch (IOException e) {
                    }
                });


                int window = 32;
                view.camView =
                        new GridSurface(
                            new MatrixView(tetris_width, tetris_height, sensorMatrixView(nar, -window)),
                            new MatrixView(tetris_width, tetris_height, sensorMatrixView(nar, 0)),
                            new MatrixView(tetris_width, tetris_height, sensorMatrixView(nar, +window))
                        );

                newControlWindow(view);

                Arkancide.newBeliefChart(this, 200);
//                BeliefTableChart.newBeliefChart(nar, Lists.newArrayList(
//                        sensors.get(0),
//                        sensors.get(1),
//                        sensors.get(2),
//                        sensors.get(3),
//                        sensors.get(4),
//                        sensors.get(5)
//                ), 200);

                //NARSpace.newConceptWindow((Default) nar, 128, 8);
            }

            public MatrixView.ViewFunc sensorMatrixView(NAR nar, long whenRelative) {
                return (x, y, g) -> {
//            int rgb = cam.out.getRGB(x,y);
//            float r = decodeRed(rgb);
//            if (r > 0)
//                System.out.println(x + " "+ y + " " + r);
//            g.glColor3f(r,0,0);

                    SensorConcept s = sensors.get(y * tetris_width + x);
                    float b = s.hasBeliefs() ?
                            s.beliefs().freq(now + whenRelative) : 0;
                            //s.beliefs().expectation(now) : 0;
                    Truth dt = s.hasGoals() ? s.goals().truth(now) : null;
                    float dr, dg;
                    if (dt == null) {
                        dr = dg = 0;
                    } else {
                        float f = dt.freq();
                        float c = dt.conf();
                        if (f > 0.5f) {
                            dr = 0;
                            dg = (f - 0.5f) * 2f * c;
                        } else {
                            dg = 0;
                            dr = (0.5f - f) * 2f * c;
                        }
                    }

                    float p = nar.conceptPriority(s);
                    g.glColor4f(dr, dg, b, 0.5f + 0.5f * p);

                };
            }
        };


//        Iterable<Termed> cheats = Iterables.concat(
//                numericSensor(() -> t.currentX, nar, 0.3f,
//                        "(cursor_x)")
//                        //"(active,a)","(active,b)","(active,c)","(active,d)","(active,e)","(active,f)","(active,g)","(active,h)")
//                        //"I(a)","I(b)","I(c)","I(d)","I(e)","I(f)","I(g)","I(h)")
//                        //"(active,x)")
//                        .resolution(0.5f / t.width),
//                numericSensor(() -> t.currentY, nar, 0.3f,
//                        "(cursor_y)")
//                        //"active:(y,t)", "active:(y,b)")
//                        //"(active,y)")
//                        .resolution(0.5f / t.height)
//        );

//        NAgent n = new NAgent(nar) {
//            @Override
//            public void start(int inputs, int actions) {
//                super.start(inputs, actions);
//
//                List<Termed> charted = new ArrayList(super.actions);
//
//                charted.add(sad);
//                charted.add(happy);
//                Iterables.addAll(charted, cheats);
//
//                if (nar instanceof Default) {
//
//                    new BeliefTableChart(nar, charted).show(600, 900);
//
//                    //BagChart.show((Default) nar, 128);
//
//                    //STMView.show(stm, 800, 600);
//
//
//                    NARSpace.newConceptWindow((Default) nar, 128, 8);
//                }
//
//
//            }
//
//            @Override
//            protected Collection<Task> perceive(Set<Task> inputs) {
//                return super.perceive(inputs);
//            }
//        };


        //addCamera(t, nar, 8, 8);


        NARLoop loop = t.run(runFrames, frameDelay, TIME_DILATION);

        NARController meta = new NARController(nar, loop, t);
        Arkancide.newBeliefChart(meta, 500);

        loop.join();



        //nar.stop();

        //nar.index.print(System.out);
        NAR.printTasks(nar, true);
        NAR.printTasks(nar, false);

        //NAR.printTasks(meta.nar, true);
        //NAR.printTasks(meta.nar, false);
//        nar.forEachActiveConcept(c -> {
//            if (c.volume() < 12)
//                c.print();
//        });
    }

    public static class NARController extends NAREnvironment {

        private final NARLoop loop;
        private final NAR worker;
        private final NAREnvironment env;


        @Override
        protected float act() {
            //float avgFramePeriodMS = (float) loop.frameTime.getMean();

            float mUsage = memory();
            float targetMemUsage = 0.75f;



            return
                    env.rewardNormalized.asFloat() +
                    happysad.asFloat() +  //boost for happiness
                    (mUsage < targetMemUsage ? 1f : (1f/(1f + mUsage - targetMemUsage))); //maintain % memory utilization TODO cache 'memory()' result
        }


        public NARController( NAR worker, NARLoop loop, NAREnvironment env) {

            super( new Default(384, 4, 2, 2, new XORShiftRandom(2),
                    new CaffeineIndex(new DefaultConceptBuilder(new XORShiftRandom(3)),5*100000),
                    //new CaffeineIndex(new DefaultConceptBuilder(random)),
                    new FrameClock()) {
                       @Override
                       protected void initHigherNAL() {
                           super.initHigherNAL();
                           cyclesPerFrame.setValue(16);
                           inputActivation.setValue(0.25f);
                           derivedActivation.setValue(0.25f);
                           //ctl.confMin.setValue(0.01f);
                           //ctl.truthResolution.setValue(0.01f);
                           beliefConfidence(0.9f);
                           goalConfidence(0.4f);
                       }
                   });

            this.worker = worker;
            this.loop = loop;
            this.env = env;

            happysad = new RangeNormalizedFloat(()->(float)worker.emotion.happysad());

            //nar.log();
            worker.onFrame(nn -> next());

            init(nar);
            mission();
        }

        @Override public void next() {
            super.next();
            nar.next();
        }



        @Override
        protected void init(NAR n) {

            float sensorResolution = 0.05f;

            float sensorConf = alpha;

            FloatToObjectFunction<Truth> truther = (v) -> $.t(v, sensorConf);

            sensors.addAll(Lists.newArrayList(
                    new SensorConcept("(motive)", n,
                            happysad,
                            truther
                    ).resolution(sensorResolution),
                    new SensorConcept("(busy)", n,
                            new RangeNormalizedFloat(() -> (float) worker.emotion.busy.getSum()),
                            truther
                    ).resolution(sensorResolution),
                    new SensorConcept("(learn)", n,
                    /* new RangeNormalizedFloat(  */ () -> worker.emotion.learning(),
                            truther
                    ).resolution(sensorResolution),
                    new SensorConcept("(memory)", n,
                            () -> memory(),
                            truther
                    ).resolution(sensorResolution)
            ));

            //final int BASE_PERIOD_MS = 100;
            final int MAX_CONCEPTS_FIRE_PER_CYCLE = 32;
            final int MAX_LINKS_PER_CONCEPT = 24;

            actions.addAll(Lists.newArrayList(
                    //cpu throttle
                    /*new MotorConcept("(cpu)", nar, (b, d) -> {
                        int newPeriod = Math.round(((1f - (d.expectation())) * BASE_PERIOD_MS));
                        loop.setPeriodMS(newPeriod);
                        //System.err.println("  loop period ms: " + newPeriod);
                        return d;
                    }),*/

                    //memory throttle
                    new MotorConcept("(memoryWeight)", nar, (b, d) -> {
                        ((CaffeineIndex) worker.index).compounds.policy().eviction().ifPresent(e -> {
                            float sweep = 0.1f; //% sweep , 0<sweep
                            e.setMaximum((long) (DEFAULT_INDEX_WEIGHT * (1f + sweep * 2f * (d.freq() - 0.5f))));
                        });
                        //System.err.println("  loop period ms: " + newPeriod);
                        return d;
                    }),

                    new MotorConcept("(confMin)", nar, (b, d) -> {
                        float MAX_CONFMIN = 0.1f;
                        float newConfMin = Math.max(Param.TRUTH_EPSILON, MAX_CONFMIN * d.freq());
                        worker.confMin.setValue(newConfMin);
                        return d;
                    }),

                    new MotorConcept("(inputActivation)", nar, (b, d) -> {
                        worker.inputActivation.setValue(d.freq());
                        return d;
                    }),

                    new MotorConcept("(derivedActivation)", nar, (b, d) -> {
                        worker.derivedActivation.setValue(d.freq());
                        return d;
                    }),

                    new MotorConcept("(conceptsPerFrame)", nar, (b, d) -> {
                        ((Default) worker).core.conceptsFiredPerCycle.setValue((int) (d.freq() * MAX_CONCEPTS_FIRE_PER_CYCLE));
                        return d;
                    }),

                    new MotorConcept("(linksPerConcept)", nar, (b, d) -> {
                        float l = d.freq() * MAX_LINKS_PER_CONCEPT;
                        l = Math.max(l, 1f);
                        int vv = (int) Math.floor((float)Math.sqrt(l));

                        ((Default) worker).core.tasklinksFiredPerFiredConcept.setValue(vv);
                        ((Default) worker).core.termlinksFiredPerFiredConcept.setValue((int)Math.ceil(l / vv));
                        return d;
                    }),

                    new MotorConcept("(envCuriosity)", nar, (b, d) -> {
                        float exp = d.freq();
                        env.epsilon = exp;
                        env.gammaEpsilonFactor = exp*exp;
                        return d;
                    })
            ));
        }

        public final float memory() {
            Runtime runtime = Runtime.getRuntime();
            long total = runtime.totalMemory(); // current heap allocated to the VM process
            long free = runtime.freeMemory(); // out of the current heap, how much is free
            long max = runtime.maxMemory(); // Max heap VM can use e.g. Xmx setting
            long usedMemory = total - free; // how much of the current heap the VM is using
            long availableMemory = max - usedMemory; // available memory i.e. Maximum heap size minus the current amount used
            float ratio = 1f - ((float)availableMemory) / max;
            //logger.warn("max={}k, used={}k {}%, free={}k", max/1024, total/1024, Texts.n2(100f * ratio), free/1024);
            return ratio;
        }

        final RangeNormalizedFloat happysad;

    }


    //    static void addCamera(Tetris t, NAR n, int w, int h) {
//        //n.framesBeforeDecision = GAME_DIVISOR;
//        SwingCamera s = new SwingCamera(t.vis);
//
//        NARCamera nc = new NARCamera("t", n, s, (x, y) -> $.p($.the(x), $.the(y)));
//
//        NARCamera.newWindow(s);
//
//        s.input(0, 0, t.vis.getWidth(),t.vis.getHeight());
//        s.output(w, h);
//
//        n.onFrame(nn -> {
//            s.update();
//        });
//    }

}
