package nars.experiment.minicraft;

import nars.$;
import nars.NAR;
import nars.Param;
import nars.experiment.minicraft.top.InputHandler;
import nars.experiment.minicraft.top.TopDownMinicraft;
import nars.op.DepIndepVarIntroduction;
import nars.op.VarIntroduction;
import nars.op.mental.Abbreviation;
import nars.remote.SwingAgent;
import nars.video.PixelAutoClassifier;
import nars.video.PixelBag;
import nars.video.Sensor2D;

import static spacegraph.SpaceGraph.window;

/**
 * Created by me on 9/19/16.
 */
public class TopCraft extends SwingAgent {

    private final TopDownMinicraft craft;
    private Sensor2D<PixelBag> pixels;
    private PixelAutoClassifier camAE = null;

    public static void main(String[] args) {
        run(TopCraft::new, 555500);
    }

    public TopCraft(NAR nar) {
        super(nar, 16);

        this.craft = new TopDownMinicraft();

        pixels = addCamera("see", ()->craft.image, 64,64, (v) -> $.t( v, alpha));
        //pixels = addFreqCamera("see", ()->craft.image, 64,64, (v) -> $.t( v, alpha));

        int nx = 8;
        camAE = new PixelAutoClassifier("seeAE", pixels.src.pixels, nx, nx,   (subX, subY) -> {
            //context metadata: camera zoom, to give a sense of scale
            //return new float[]{subX / ((float) (nx - 1)), subY / ((float) (nx - 1)), pixels.src.Z};
            return new float[]{ pixels.src.Z};
        }, 24, this);
        window(camAE.newChart(), 500, 500);


        senseSwitch("dir", ()->craft.player.dir, 0, 4);
        sense("(stamina)", ()->(craft.player.stamina)/((float)craft.player.maxStamina));
        sense("(health)", ()->(craft.player.health)/((float)craft.player.maxHealth));

        int tileMax = 13;
        senseSwitch("(tile,(0,0))", ()->craft.player.tile().id, 0, tileMax);
        senseSwitch("(tile,(0,1))", ()->craft.player.tile(0,1).id, 0, tileMax);
        senseSwitch("(tile,(0,-1))", ()->craft.player.tile(0,-1).id, 0, tileMax);
        senseSwitch("(tile,(1,0))", ()->craft.player.tile(1,0).id, 0, tileMax);
        senseSwitch("(tile,(-1,0))", ()->craft.player.tile(-1,0).id, 0, tileMax);

        InputHandler input = craft.input;
        actionToggleRapid("(fire)", (b) -> input.attack.toggle(b), 16 );
//        actionToggleRapid("(move,(0,1))", (b) -> input.up.toggle(b), 32 );
//        actionToggleRapid("(move,(0,-1))", (b) -> input.down.toggle(b),32 );
//        actionToggleRapid("(move,(-1,0))", (b) -> input.left.toggle(b),32 );
//        actionToggleRapid("(move,(1,0))", (b) -> input.right.toggle(b),32 );
        actionTriState("(move,x)", (i)->{
           boolean l = false, r = false;
           switch (i) {
               case -1: l = true;  break;
               case +1: r = true;  break;
           }
           input.left.toggle(l);
           input.right.toggle(r);
        });
        actionTriState("(move,y)", (i)->{
            boolean u = false, d = false;
            switch (i) {
                case -1:  u = true;  break;
                case +1:  d = true;  break;
            }
            input.up.toggle(u);
            input.down.toggle(d);
        });
        actionToggle("(menu)", (b) -> input.menu.toggle(b) );

//        Param.DEBUG = true;
//        nar.onTask(t ->{
//            if (t.isEternal() && (!(t instanceof VarIntroduction.VarIntroducedTask)) && t.concept(nar).get(Abbreviation.class)==null) {
//                System.err.println(t.proof());
//                System.err.println();
//            }
//        });

        TopDownMinicraft.start(craft, false);
    }



    float prevScore = 0;
    @Override protected float act() {



        float nextScore = craft.frameImmediate();
        float ds = nextScore - prevScore;
        this.prevScore = nextScore;
        float r = 0.5f * ds + 2f * (craft.player.health/((float)craft.player.maxHealth)-1f);// + 0.25f * (craft.player.stamina*((float)craft.player.maxStamina))-0.5f);
        return r;
    }

}
