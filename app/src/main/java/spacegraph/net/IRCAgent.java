package spacegraph.net;

import com.github.fge.grappa.exceptions.GrappaException;
import nars.$;
import nars.NAR;
import nars.Narsese;
import nars.Task;
import nars.index.term.TermIndex;
import nars.index.term.tree.TreeTermIndex;
import nars.nal.nal8.operator.TermFunction;
import nars.nar.Default;
import nars.nar.exe.MultiThreadExecutioner;
import nars.nar.util.DefaultConceptBuilder;
import nars.op.mental.Inperience;
import nars.op.time.MySTMClustered;
import nars.term.Compound;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.atom.Atomic;
import nars.term.var.Variable;
import nars.time.RealTime;
import nars.time.Tense;
import nars.util.Loop;
import nars.util.Wiki;
import nars.util.data.random.XorShift128PlusRandom;
import nars.util.event.On;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import static nars.Op.INH;
import static nars.Op.PROD;
import static nars.nlp.Twenglish.tokenize;

/**

 $0.9;0.9;0.99$

 $0.9;0.9;0.99$ (hear(?someone, $something) ==>+1 hear(I,$something)).
 $0.9;0.9;0.99$ (((hear(#someone,#someThing) &&+1 hear(#someone,$nextThing)) && hear(I, #someThing)) ==>+1 hear(I, $nextThing)).
 $0.9;0.9;0.99$ (((hear($someone,$someThing) &&+1 hear($someone,$nextThing)) <=> hear($someone, ($someThing,$nextThing)))).
 $0.9;0.9;0.99$ (((I<->#someone) && hear(#someone, $something)) ==>+1 hear(I, $something)).
 $0.9;0.9;0.99$ hear(I, #something)!
 hear(I,?x)?

 $0.9$ (($x,"the") <-> ($x,"a")).

 */
public class IRCAgent extends IRC {
    private static final Logger logger = LoggerFactory.getLogger(IRCAgent.class);

    //private final Talk talk;
    private final NAR nar;
    //private float ircMessagePri = 0.9f;

    final int wordDelayMS = 25; //for serializing tokens to events: the time in millisecond between each perceived (subvocalized) word, when the input is received simultaneously

    public IRCAgent(NAR nar, String nick, String server, String... channels) throws Exception {
        super(nick, server, channels);

        this.nar = nar;

        //talk = new Talk(nar);

        //nar.log();


        //SPEAK
        nar.onTask(t -> {
            float p = t.pri();
            if (t.op()==INH && t.term(1).equals(nar.self)) {
            //if (p >= 0.7f || (t.term().containsTermRecursively(nar.self) && (p > 0.5f))) {
                send(channels, t.toString());
            }
        });

        nar.on(new IRCBotOperator("readWiki") {


            @Override
            protected Object function(Compound arguments) {

                String base = "simple.wikipedia.org";
                //"en.wikipedia.org";
                Wiki enWiki = new Wiki(base);

                String lookup = arguments.term(0).toString();
                try {
                    //remove quotes
                    String page = enWiki.normalize(lookup.replace("\"", ""));
                    //System.out.println(page);

                    enWiki.setMaxLag(-1);

                    String html = enWiki.getRenderedText(page);
                    html = StringEscapeUtils.unescapeHtml4(html);
                    String strippedText = html.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", " ").toLowerCase();

                    //System.out.println(strippedText);

                    hear(strippedText, page);

                    return "Reading " + base + ":" + page + ": " + strippedText.length() + " characters";

                } catch (IOException e) {
                    e.printStackTrace();
                    return e.toString();
                }
            }
        });

//        nar.onExec(new TermProcedure("say") {
//            @Override public @Nullable Object function(Compound arguments, TermIndex i) {
//                Term content = arguments.term(0);
//                String context = arguments.size() > 1 ? arguments.term(1).toString() : "";
//                if (context.equals("I"))
//                    send(channel, content.toString());
//
//                return null;
//            }
//        });

//        nar.onExec(new IRCBotOperator("memstat") {
//            @Nullable @Override public Object function(Compound arguments) {
//
//                @NotNull Bag<Concept> cbag = ((Default) nar).core.concepts;
//                return nar.concepts.summary();
//                        //" | core pri: " + cbag.priMin() + "<" + Texts.n4(cbag.priHistogram(5)) + ">" + cbag.priMax();
//
//            }
//        });
//        nar.onExec(new IRCBotOperator("top") {
//
//            @Override
//            protected Object function(Compound arguments) {
//                return null;
//            }
//        });

        /*
        $0.9;0.9;0.99$ (hear(?someone, $something) ==>+1 hear(I,$something)).
 $0.9;0.9;0.99$ (((hear(#someone,#someThing) &&+1 hear(#someone,$nextThing)) && hear(I, #someThing)) ==>+1 hear(I, $nextThing)).
 $0.9;0.9;0.99$ (((hear($someone,$someThing) &&+1 hear($someone,$nextThing)) <=> hear($someone, ($someThing,$nextThing)))).
 $0.9;0.9;0.99$ (((I<->#someone) && hear(#someone, $something)) ==>+1 hear(I, $something)).
 $0.9;0.9;0.99$ hear(I, #something)!
 hear(I,?x)?

 $0.9$ (($x,"the") <-> ($x,"a")).
         */
        nar.input(
                "$0.9;0.9;0.99$ (hear(?someone, $something) ==>+0 hear(I,$something)).",
                "$0.9;0.9;0.99$ (((hear(#someone,#someThing) &&+1 hear(#someone,$nextThing)) &&+0 hear(I, #someThing)) ==>+1 hear(I, $nextThing)).\n",
                //"$0.9;0.9;0.99$ (((hear(#someone,$someThing) &&+1 hear(#someone,$nextThing)) <=> hear($someone, ($someThing,$nextThing)))).",
                "$0.9;0.9;0.99$ hear(I, #something)!",
                //"(((I<->#someone) && hear(#someone, $something)) ==>+1 hear(I, $something)).",
                "$0.9;0.9;0.99$ hear(I,?x)?"
        );
        final Atomic HEAR = $.the("hear");
        final Atomic I = $.the("I");
        nar.onTask(tt->{
            //HACK
            if (tt.isBelief()) {
                if (Math.abs(tt.occurrence()-nar.time()) < 100) {
                    if (tt.op() == INH && tt.term(0).op() == PROD && tt.term(1).equals(HEAR)) {
                        Compound arg = (Compound) tt.term(0);
                        if (arg.term(0).equals(I)) {
                            Term w = arg.term(1);
                            if (!(w instanceof Variable)) {
                                say(channels, w);
                            }
                        }
                    }
                }
            }
        });

    }

    //    abstract class IRCBotOperator extends TermProcedure {
//
//
//        public IRCBotOperator(String id) {
//            super(id);
//        }
//
//
//        @Nullable
//        @Override
//        public final Object function(Compound arguments, TermIndex i) {
//            Object y = function(arguments);
//
//            if (y!=null)
//                send( y.toString().replace("\n"," ") );
//
//            //loop back as hearing
//            //say($.quote(y.toString()), $.p(nar.self, $.the("controller")));
//
//            return y;
//        }
//
//        protected abstract Object function(Compound arguments);
//
//
//    }

    abstract class IRCBotOperator extends TermFunction {


        public IRCBotOperator(String id) {
            super(id);
        }


        @Nullable
        @Override
        public final synchronized Object function(Compound arguments, TermIndex i) {

            Object y = function(arguments);

            if (y != null)
                broadcast(y.toString().replace("\n", " "));

            //loop back as hearing
            //say($.quote(y.toString()), $.p(nar.self, $.the("controller")));

            return null;
        }

        protected abstract Object function(Compound arguments);

    }

    @Override
    public void onGenericMessage(GenericMessageEvent event) throws Exception {

        //}

        //@Override
        //protected void onMessage(String channel, String nick, String msg) {

//        nar.goal(
//                "say(\"" + msg + "\",(\"" + channel+ "\",\"" + nick + "\"))",
//                Tense.Present, 1f, 0.9f );
//
//    }
//
//    protected void onMessage(String channel, String nick, String msg) {

        if (event instanceof MessageEvent) {
            MessageEvent pevent = (MessageEvent) event;

            if (pevent.getUser().equals(irc.getUserBot())) {
                return; //ignore own messages (echo)
            }

            String msg = pevent.getMessage().trim();

            //        if (channel.equals("unknown")) return;
            if (msg.startsWith("//"))
                return; //comment or previous output

//            switch (msg) {
//                case "memstat":
//                    pevent.respondWith(nar.concepts.summary());
//                    return;
//                //@NotNull Bag<Concept> cbag = ((Default) nar).core.concepts;
//                //" | core pri: " + cbag.priMin() + "<" + Texts.n4(cbag.priHistogram(5)) + ">" + cbag.priMax();
////                case "top":
////                    pevent.respondWith(top(Terms.ZeroProduct));
////                    return;
//                case "clear":
//                    ((Default) nar).core.active.clear();
//                    pevent.respondWith("Ready.");
//                    return;
//                case "save":
//                    File tmp = Files.createTempFile("nar_save_", ".nal").toFile();
//                    PrintStream ps = new PrintStream(new FileOutputStream(tmp));
//                    nar.outputTasks((x) -> true, ps);
//                    pevent.respondWith("Memory saved to: " + tmp.getAbsolutePath());
//                    return;
//            }

            String nick = pevent.getUser().getNick(); //TODO use hostmask ?
            String channel = pevent.getChannel().getName();

            //        if (msg.equals("RESET")) {
            //            restart();
            //        }
            //        else if (msg.equals("WTF")) {
            //            flush();
            //        }
            //        else {

            try {
                try {
                    @NotNull List<Task> parsed = nar.tasks(msg.replace("http://", "") /* HACK */, o -> {
                        //logger.error("unparsed narsese {} in: {}", o, msg);
                    });


                    int narsese = parsed.size();
                    if (narsese > 0) {
                        for (Task t : parsed) {
                            logger.info("narsese({},{}): {}", channel, nick, t);
                        }
                        parsed.forEach(nar::input);
                        return;
                    }
                } catch (GrappaException | Narsese.NarseseException f) {
                    hear(msg, nick);
                }
            } catch (Exception e) {
                pevent.respond(e.toString());
            }

            //logger.info("hear({},{}): {}", channel, nick, msg);
            //talk.hear(msg, context(channel, nick), ircMessagePri);
        }


    }


    void hear(@NotNull String msg, @NotNull  String who) {
        hear(nar, msg, who, wordDelayMS);
    }

    static public Loop hear(NAR nar, @NotNull String msg, @NotNull  String src, int wordDelayMS) {
//        nar.believe(
//                $.inst($.p(tokenize(msg)), $.p($.quote(channel), $.quote(nick))),
//                Tense.Present
//        );

        final List<Term> tokens = tokenize(msg);
        if (!tokens.isEmpty()) {

            Atom chan_nick = $.quote( src );

            return new Loop(msg, wordDelayMS) {

                public On onReset;
                int token = 0;

                @Override
                public void next() {
                    if (token >= tokens.size()) {
                        stop();
                        return;
                    }

                    onReset = nar.eventReset.on((n)->{
                        if (onReset!=null) {
                            onReset.off();
                            onReset = null;
                            stop();
                        }
                    });



                    //hear(who,what)
                    Term pr = $.func("hear", chan_nick, tokens.get(token++));

                    // (what --> (|,who,"hear")
                    //Term pr = $.inh(tokens.get(token++), $.secti($.the("hear"), chan_nick));

                    nar.believe(pr, Tense.Present, 1f);

                }
            };
//            nar.runLater(() -> {
//
//                for (Term token :) {
//                    nar.believe(pr, time[0], 1f);
//                    time[0] += wordDelayMS;
//                }
//
//            });
        }

        return null;
    }

    @NotNull
    public static Default newRealtimeNAR(int activeConcepts, int framesPerSecond, int conceptsPerFrame) throws FileNotFoundException {

        Random random = new XorShift128PlusRandom(System.currentTimeMillis());

        MultiThreadExecutioner exe = new MultiThreadExecutioner(2, 1024*4);
        exe.sync(false);

        Default nar = new Default(activeConcepts, conceptsPerFrame, 2, 3, random,

                //new CaffeineIndex(new DefaultConceptBuilder(random), 10000000, false, exe),
                new TreeTermIndex.L1TreeIndex(new DefaultConceptBuilder(), 400000, 64 * 1024, 3),

                new RealTime.MS(true),
                exe
        );


        int volMax = 96;

//        //Multi nar = new Multi(3,512,
//        Default nar = new Default(2048,
//                conceptsPerCycle, 2, 2, rng,
//                //new CaffeineIndex(new DefaultConceptBuilder(rng), 1024*1024, volMax/2, false, exe)
//                new TreeIndex.L1TreeIndex(new DefaultConceptBuilder(new XORShiftRandom(3)), 400000, 64*1024, 3)
//
//                , new FrameClock(), exe);


        nar.beliefConfidence(0.9f);
        nar.goalConfidence(0.8f);

        float p = 0.05f;
        nar.DEFAULT_BELIEF_PRIORITY = 0.75f * p;
        nar.DEFAULT_GOAL_PRIORITY = 1f * p;
        nar.DEFAULT_QUESTION_PRIORITY = 0.25f * p;
        nar.DEFAULT_QUEST_PRIORITY = 0.5f * p;

        nar.confMin.setValue(0.02f);
        nar.compoundVolumeMax.setValue(volMax);


//        nar.inputLater(
//                NQuadsRDF.stream(nar, new File(
//                        "/home/me/Downloads/nquad"
//                )).
//                        peek(t -> {
//                            t.setBudget(0.01f, 0.5f, 0.9f);
//                        }).
//                        collect(Collectors.toList())
//                , 32
//        );
//        nar.run(1);

        MySTMClustered stm = new MySTMClustered(nar, 64, '.', 3, true, 1);

        new Inperience(nar, 0.05f);

        nar.loop(framesPerSecond);

        return nar;
    }

    public static void main(String[] args) throws Exception {

        @NotNull Default n = newRealtimeNAR(2048, 20, 60);


        IRCAgent bot = new IRCAgent(n,
                "experiment1", "irc.freenode.net",
                //"#123xyz"
                "#netention"
                //"#nars"
        );


        bot.start();


    }
    final StringBuilder b = new StringBuilder();

    public void say(String[] channels, Term w) {
        logger.info("say {}", w);
        b.append(w.toString()).append(' ');
        if (b.length() > 40) {
            send(channels, b.toString());
            b.setLength(0);
        }

    }


//    public static void main(String[] args) throws Exception {
//        //while (running) {
//            try {
//                new IRCAgent(
//                        "irc.freenode.net",
//                        //"localhost",
//                        "NARchy", "#netention");
//            } catch (Exception e) {
//                e.printStackTrace();
//
//            }
//        //}
//    }




}