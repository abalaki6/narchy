package nars.experiment.fzero;

import java4k.gradius4k.Gradius4K;
import jcog.Util;
import jcog.math.FloatNormalized;
import nars.*;
import nars.concept.SensorConcept;
import nars.nar.Default;
import nars.nar.NARBuilder;
import nars.term.atom.Atomic;
import nars.time.RealTime;
import nars.video.BufferedImageBitmap2D;
import nars.video.Scale;
import org.apache.commons.math3.util.MathUtils;
import org.jetbrains.annotations.NotNull;

import static nars.$.t;

/**
 * Created by me on 4/30/17.
 */
public class Gradius extends NAgentX {

    private final Gradius4K g;

    public Gradius(NAR nar) throws Narsese.NarseseException {
        super("G", nar);

        this.g =  new Gradius4K();

        Scale camScale = new Scale(() -> g.image, 24, 24);
        for (BufferedImageBitmap2D.ColorMode cm : new BufferedImageBitmap2D.ColorMode[] {
                BufferedImageBitmap2D.ColorMode.R,
                BufferedImageBitmap2D.ColorMode.B,
                BufferedImageBitmap2D.ColorMode.G
        }) {
            senseCamera("(G,c" + cm.name() + ")",
                    camScale.filter(cm),
                    (v) -> t(v, alpha()))
                    .setResolution(0.1f);
        }

        actionToggle($.inh(Atomic.the("fire"),id),
                (b)-> g.keys[Gradius4K.VK_SHOOT] = b );

        actionTriState($.inh(Atomic.the("x"), id ), (dh) -> {
            g.keys[Gradius4K.VK_LEFT] = false;
            g.keys[Gradius4K.VK_RIGHT] = false;
            switch (dh) {
                case +1: g.keys[Gradius4K.VK_RIGHT] = true; break;
                case -1: g.keys[Gradius4K.VK_LEFT] = true; break;
            }
        });

        actionTriState($.inh(Atomic.the("y"), id ), (dh) -> {
            g.keys[Gradius4K.VK_UP] = false;
            g.keys[Gradius4K.VK_DOWN] = false;
            switch (dh) {
                case +1: g.keys[Gradius4K.VK_DOWN] = true; break;
                case -1: g.keys[Gradius4K.VK_UP] = true; break;
            }
        });


    }


    int lastScore = 0;

    @Override
    protected float act() {


        int nextScore = g.score;

        float r = (nextScore - lastScore);
//        if (r > 0)
//            System.out.println(r);
        lastScore = nextScore;

        if (g.playerDead > 1)
            return -1f;
        else
            return r;
    }

    public static void main(String[] args) throws Narsese.NarseseException {
        Default n = NARBuilder.newMultiThreadNAR(
                3,
                new RealTime.CS(true)
                        .durFPS(10f), true);

        Gradius a = new Gradius(n);
        a.runRT(10f);


        NAgentX.chart(a);

    }

}
