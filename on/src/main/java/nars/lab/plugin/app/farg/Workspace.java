/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.lab.plugin.app.farg;

import nars.NAR;
import nars.entity.Concept;
import nars.storage.LevelBag;
import nars.util.EventEmitter.EventObserver;
import nars.util.Events.CycleEnd;

/**
 *
 * @author patrick.hammer
 */
public class Workspace {

    public double temperature;
    public NAR nar;
    public int n_concepts;
    
    public Workspace(FluidAnalogiesAgents farg, NAR nar) {
        this.nar=nar;
        Workspace ws=this;
        farg.coderack=new LevelBag(farg.codelet_level,farg.max_codelets);
        nar.on(CycleEnd.class, (event, args) -> {
            for(int i=0;i<10;i++) { //process 10 codelets in each step
                Codelet cod=farg.coderack.takeNext();
                if(cod!=null) {
                    if(Codelet.run(ws)) {
                        farg.coderack.putIn(cod);
                    }
                }
                temperature=calc_temperature();
            }
            controller();
        });
    }
    
    public void controller() { 
        //when to put in Codelets of different type, and when to remove them
        //different controller for different domains would inherit from FARG
    }
    
    public double calc_temperature() {
        double s=0.0f;
        n_concepts=0;
        for(Concept node : nar.memory) {
            if(!node.desires.isEmpty()) {
                s+=node.getPriority()*node.desires.get(0).sentence.truth.getExpectation();
            }
            n_concepts++;
        }
        return s/((double) n_concepts);
    }
}