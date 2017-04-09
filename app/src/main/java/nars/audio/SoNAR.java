package nars.audio;

import nars.NAR;
import nars.Narsese;
import nars.concept.Concept;
import nars.nar.Default;
import nars.term.Term;
import nars.truth.Truth;
import spacegraph.audio.Audio;
import spacegraph.audio.Sound;
import spacegraph.audio.granular.Granulize;
import spacegraph.audio.sample.SampleLoader;
import spacegraph.audio.sample.SonarSample;

import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static nars.$.$;

/**
 * NAR sonification
 */
public class SoNAR {

    private final NAR nar;
    public final Audio audio;

    final Map<String, SonarSample> samples = new ConcurrentHashMap<>();

    public SoNAR(NAR n) throws LineUnavailableException {
        this(n, new Audio(64));
    }

    public SonarSample sample(String file) {
        return samples.computeIfAbsent(file, SampleLoader::load);
    }

    public void samples(String dirPath) {
        for (File f : new File(dirPath).listFiles()) {
            String path = f.getAbsolutePath();
            samples.computeIfAbsent(path, SampleLoader::load);
        }
    }

    /**
     * gets a random sample from what is loaded
     */
    public SonarSample sampleRandom() {
        List<SonarSample> l = samples.values().stream().collect(Collectors.toList());         //HACK
        if (l != null && !l.isEmpty()) {
            SonarSample s;
            do {
                s = l.get(Math.abs(Math.abs(nar.random.nextInt()) % l.size()));
            } while (s == null);
            return s;
        } else
            return null;
    }

    public SoNAR(NAR n, Audio audio) {
        this.nar = n;
        this.audio = audio;

//        Granulize ts =
//                new Granulize(sample("/tmp/awake.wav"), 0.25f, 0.9f)
//                        .setStretchFactor(0.25f);

        //audio.play(ts, SoundListener.zero, 1, 1);

        //audio.play(new SamplePlayer(smp, 0.5f), SoundListener.zero, 1, 1);

        n.onCycle(this::update);

    }

    /**
     * updated each cycle
     */
    final Map<Term, Sound<Granulize>> termListeners = new ConcurrentHashMap();

    public void listen(Term k) {
        termListeners.computeIfAbsent(k, kk -> {
            Granulize g = new Granulize(sampleRandom(), 0.25f, 1.5f);

            return audio.play(g, 0.25f, 0.5f, (float) (Math.random() - 0.5f));
        });

    }

    protected synchronized void update() {
        termListeners.forEach(this::update);
    }

    private boolean update(Term k, Sound<Granulize> s) {
        Granulize v = s.producer;
        Concept c = nar.concept(k);
        if (c != null) {
            float p = nar.pri(k);
            if (p == p && p > 0) {

                //v.setAmplitude(0.1f * p);

                Truth b = c.belief(nar.time(), nar.dur());
                if (b != null) {
                    //System.out.println(c + " "+ b);
                    float stretchFactor = (b.freq() - 0.5f) * 2f;
                    if (stretchFactor > 0 && stretchFactor < 0.25f) stretchFactor = 0.25f;
                    else if (stretchFactor < 0 && stretchFactor > -0.25f) stretchFactor = -0.25f;
                    v.setStretchFactor(stretchFactor);
                    v.setAmplitude(b.conf());
                    v.play();
                } else {
                    v.stop();
                    v.setStretchFactor(1f);
                    v.setAmplitude(0f);
                }

                //
                //v.setStretchFactor();
                v.pitchFactor.setValue(1f/Math.log(c.volume()));
                //g.setStretchFactor(1f/(1f+kk.volume()/4f));
                return true;
            }
        }

        v.setAmplitude(0f);
        //v.stop();
        return false;
    }

    public void join() throws InterruptedException {
        audio.thread.join();
    }

    public static void main(String[] args) throws LineUnavailableException, InterruptedException, Narsese.NarseseException {
        Default n = new Default();
        n.deriver.conceptsFiredPerCycle.set(2);
        //n.log();
        n.input("a:b. :|: (--,b:c). c:d. d:e. (--,e:f). f:g. b:f. a:g?");
        n.loop(64);
        SoNAR s = new SoNAR(n);
        s.samples("/home/me/wav");
        s.listen($("a"));
        s.listen($("b"));
        s.listen($("c"));
        s.listen($("d"));
        s.listen($("e"));
        s.listen($("f"));
        s.listen($("g"));
        s.listen($("a:b"));
        s.listen($("b:c"));
        s.listen($("c:d"));
        s.listen($("d:e"));
        s.listen($("e:f"));
        s.listen($("f:g"));
        s.listen($("a:g"));
        try {
            s.audio.record("/tmp/test.raw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        s.join();
    }
}
