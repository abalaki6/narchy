package nars;

import static nars.$.*;

/**
 * meta-controller of an NAgent
 *      essentially tunes runtime parameters in response to feedback signals
 *      can be instantiated in a NAR 'around' any agent
 */
public class MetaAgent extends NAgent {

    /** agent controlled by this */
    private final NAgent agent;

    /** colocated with the agent's NAR */
    public MetaAgent(NAgent agent) {
        this(agent, agent.nar);
    }

    public MetaAgent(NAgent agent, NAR metaNAR) {
        super(func("meta", agent.id), metaNAR);
        this.agent = agent;
        NAR agentNAR = agent.nar;

        senseNumberNormalized(p("happy"), ()->agentNAR.emotion.happy());
        senseNumberNormalized(p("sad"), ()->agentNAR.emotion.sad());
        senseNumberNormalized(p("busyPri") /*$.func($.the("busy"),$.the("pri"))*/, ()->(float)agentNAR.emotion.busyPri.getSum());
        senseNumberNormalized(p("busyVol") /*$.func($.the("busy"),$.the("vol"))*/, ()->(float)agentNAR.emotion.busyVol.getSum());
        senseNumber(p("lernPri") /*$.func($.the("lern"),$.the("pri"))*/, ()-> agentNAR.emotion.learningPri());
        senseNumber(p("lernVol") /*$.func($.the("lern"),$.the("vol"))*/, ()-> agentNAR.emotion.learningVol());
        senseNumber(p("dext"), ()-> agent.dexterity());

        actionLerp(p("curi"), (q) -> agent.curiosity.setValue(q), 0f, 0.05f);
        actionLerp(p("quaMin"), (q) -> agentNAR.quaMin.setValue(q), 0f, 0.2f);
//        actionLerp($.p("dur"), (d) -> agentNAR.time.dur(d),
//                0.1f /* 0 might cause problems with temporal truthpolation, examine */,
//                nar.time.dur()*2f /* multiple of the input NAR */);
    }

    @Override
    protected float act() {
        //TODO other qualities to maximize: runtime speed, memory usage, etc..
        //float agentHappiness = agent.happy.asFloat();
        float narHappiness = agent.nar.emotion.happy();
        float narSadness = agent.nar.emotion.sad();

        return /*agentHappiness + */narHappiness - narSadness;
    }

}