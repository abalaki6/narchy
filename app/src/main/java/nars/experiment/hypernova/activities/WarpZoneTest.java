package nars.experiment.hypernova.activities;

import nars.experiment.hypernova.ActivitySimple;
import nars.experiment.hypernova.NewUniverse;
import nars.experiment.hypernova.gui.Transition;
import nars.experiment.hypernova.universes.*;
//import nars.experiment.hypernova.gui.backgrounds.EqualizerBackground;

public class WarpZoneTest extends ActivitySimple {
    private NewUniverse toLoad = new Test();

    public WarpZoneTest(NewUniverse n) {
       super();
       toLoad = n;
    }
    
    public void eventHandler(int event, String eventArgs)
    {
       switch (event) 
       {
           case 0: 
               u.queueMessage("3");
               setTimeout(1,2000);
               break;
           case 1: 
               u.queueMessage("2");
               setTimeout(2,2000);
               break;
           case 2: 
               u.queueMessage("1");
               setTimeout(3,2000);
               break;
           case 3: 
               u.queueMessage("Arrival");
               break;

       }
    }


    public void begin(double px, double py) {
        if(toLoad instanceof Test) 
          Transition.startTransition(Transition.Types.FADE);
        else 
          Transition.startTransition(Transition.Types.DIAGONAL);

        u.loadUniverse(toLoad);
        u.queueMessage("WARP DEBUG TEST ENCOUNTERED");
        setTimeout(0,5000);
    }

}