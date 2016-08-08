package nars.experiment.arkanoid;


import nars.$;
import nars.NAR;
import nars.Param;
import nars.experiment.NAREnvironment;
import nars.gui.BagChart;
import nars.gui.BeliefTableChart;
import nars.index.CaffeineIndex;
import nars.nar.Default;
import nars.nar.util.DefaultConceptBuilder;
import nars.op.ArithmeticInduction;
import nars.op.VariableCompressor;
import nars.op.time.MySTMClustered;
import nars.task.Task;
import nars.term.Compound;
import nars.term.obj.Termject;
import nars.time.FrameClock;
import nars.truth.Truth;
import nars.util.Util;
import nars.util.data.random.XorShift128PlusRandom;
import nars.util.signal.MotorConcept;
import nars.util.signal.SensorConcept;
import nars.vision.SwingCamera;
import org.jetbrains.annotations.Nullable;
import spacegraph.Facial;
import spacegraph.SpaceGraph;
import spacegraph.Surface;
import spacegraph.obj.ControlSurface;
import spacegraph.obj.GridSurface;
import spacegraph.obj.MatrixView;

import java.util.List;
import java.util.Random;

import static java.util.stream.Collectors.toList;
import static nars.$.t;
import static nars.gui.NARSpace.newConceptWindow;
import static nars.vision.PixelCamera.decodeRed;
import static spacegraph.obj.GridSurface.VERTICAL;

public class Arkancide extends NAREnvironment {

    private static final int cyclesPerFrame = 127;
    public static final int runFrames = 100000;
    final Arkanoid noid;
    private final SwingCamera cam;



    private MotorConcept motorLeftRight;

    final int visW = 32;
    final int visH = 24;
    final SensorConcept[][] ss;

    private int visionSyncPeriod = 16;
    float noiseLevel = 0;

    float paddleSpeed = 70f;
    private float prevScore;

    public class View {
        //public Surface camView;
        public List attention = $.newArrayList();
    }
    private final View view = new View();

    public Arkancide(NAR nar) {
        super(nar);
        noid = new Arkanoid();

        ss = new SensorConcept[visW][visH];

        cam = new SwingCamera(noid, visW, visH);
        cam.update();



    }

    @Override
    protected void init(NAR n) {
        for (int x = 0; x < visW; x++) {
            int xx = x;
            for (int y = 0; y < visH; y++) {
                Compound squareTerm = $.p(new Termject.IntTerm(x), new Termject.IntTerm(y));
                int yy = y;
                SensorConcept sss;
                sensors.add(sss = new SensorConcept(squareTerm, nar,
                        () -> noise(decodeRed(cam.out.getRGB(xx, yy))) ,// > 0.5f ? 1 : 0,
                        (v) -> t(v, alpha)
                ));
                sss.timing(0,visionSyncPeriod);
                ss[x][y] = sss;
            }
        }


        MatrixView camView = new MatrixView(visW, visH, (x, y, g) -> {
//            int rgb = cam.out.getRGB(x,y);
//            float r = decodeRed(rgb);
//            if (r > 0)
//                System.out.println(x + " "+ y + " " + r);
//            g.glColor3f(r,0,0);

            SensorConcept s = ss[x][y];
            float b = s.hasBeliefs() ? s.beliefs().expectation(now) : 0;
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

        });


        actions.add(motorLeftRight = new MotorConcept("(leftright)", nar, (b,d)->{

            noid.paddle.move((motorLeftRight.goals().expectation(now) - 0.5f) * paddleSpeed);
            return d;
            //return $.t((float)(noid.paddle.x / noid.SCREEN_WIDTH), 0.9f);

            //@Nullable Truth tNow = motorLeftRight.goals().truth(now);
            //if (tNow!=null)
                //noid.paddle.set(tNow.freq());

            //return $.t(noid.paddle.moveTo(d.freq(), paddleSpeed), 0.9f);
        }));

        //view.attention.add(nar.inputActivation);
        //view.attention.add(nar.derivedActivation);

        newBeliefChart(this, 500);

        ControlSurface.newControlWindow(
                //new GridSurface(VERTICAL, actionTables),
                //BagChart.newBagChart((Default)nar, 512),
                camView
        );

        //newConceptWindow((Default) n, 64, 4);

    }

    public static void newBeliefChart(NAREnvironment narenv, long window) {
        NAR nar = narenv.nar;
        long[] btRange = new long[2];
        nar.onFrame(nn -> {
            long now = nn.time();
            btRange[0] = now - window;
            btRange[1] = now + window;
        });
        List<Surface> actionTables = narenv.actions.stream().map(c -> new BeliefTableChart(nar, c, btRange)).collect(toList());
        actionTables.add(new BeliefTableChart(nar, narenv.happy, btRange));
        actionTables.add(new BeliefTableChart(nar, narenv.joy, btRange));



        new SpaceGraph().add(new Facial(new GridSurface(VERTICAL, actionTables)).maximize()).show(800,600);
    }

    private float noise(float v) {
        if (noiseLevel > 0) {
            return Util.clamp(v + (nar.random.nextFloat() * noiseLevel));
        }
        return v;
    }

    @Override
    protected float act() {
        cam.update();


        float nextScore = noid.next();
        float reward = nextScore - prevScore;
        this.prevScore = nextScore;
        return reward;
    }

    public static void main(String[] args) {
        Random rng = new XorShift128PlusRandom(1);

        Param.CONCURRENCY_DEFAULT = 3;
        //Multi nar = new Multi(3,512,
        Default nar = new Default(1024,
                4, 2, 2, rng,
                new CaffeineIndex(new DefaultConceptBuilder(rng), 10 * 10000000, false)
                , new FrameClock()) {

            VariableCompressor.Precompressor p = new VariableCompressor.Precompressor(this);
            @Override protected Task preprocess(Task input) {
                return p.pre(input);
            }

        };
        nar.inputActivation.setValue(0.1f);
        nar.derivedActivation.setValue(0.05f);


        nar.beliefConfidence(0.95f);
        nar.goalConfidence(0.8f);
        nar.DEFAULT_BELIEF_PRIORITY = 0.15f;
        nar.DEFAULT_GOAL_PRIORITY = 0.6f;
        nar.DEFAULT_QUESTION_PRIORITY = 0.1f;
        nar.DEFAULT_QUEST_PRIORITY = 0.1f;
        nar.cyclesPerFrame.set(cyclesPerFrame);
        nar.confMin.setValue(0.05f);

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

        MySTMClustered stm = new MySTMClustered(nar, 256, '.', 3);
        MySTMClustered stmGoal = new MySTMClustered(nar, 256, '!', 2);

        //new ArithmeticInduction(nar);
        //new VariableCompressor(nar);



        Arkancide t = new Arkancide(nar);

        t.run(runFrames, 0);

        //nar.index.print(System.out);
        NAR.printTasks(nar, true);
        NAR.printTasks(nar, false);
    }


}