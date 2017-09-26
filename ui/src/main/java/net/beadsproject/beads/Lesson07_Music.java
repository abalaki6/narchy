package net.beadsproject.beads;

import jcog.Util;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.ugens.*;

public class Lesson07_Music {

    public static class Music1 {
        public static void main(String[] args) {

            final AudioContext ac;

            ac = new AudioContext();
            /*
             * In this example a Clock is used to trigger events. We do this by
             * adding a listener to the Clock (which is of type Bead).
             *
             * The Bead is made on-the-fly. All we have to do is to give the Bead a
             * callback method to make notes.
             *
             * This example is more sophisticated than the previous ones. It uses
             * nested code.
             */
            Clock clock = new Clock(ac, 700);
            clock.on(
                    //this is the on-the-fly bead
                    new Bead() {
                        //this is the method that we override to make the Bead do something
                        int pitch;

                        @Override
                        public void messageReceived(Bead message) {
                            Clock c = (Clock) message;
                            if (c.isBeat()) {
                                //choose some nice frequencies
                                if (random(1) < 0.5) return;
                                pitch = Pitch.forceToScale((int) random(12), Pitch.dorian);
                                float freq = Pitch.mtof(pitch + (int) random(5) * 12 + 32);
                                WavePlayer wp = new WavePlayer(ac, freq, Buffer.SINE);
                                Gain g = new Gain(ac, 1, new Envelope(ac, 0));
                                g.in(wp);
                                ac.out.in(g);
                                ((Envelope) g.getGainUGen()).add(0.1f, random(200));
                                ((Envelope) g.getGainUGen()).add(0, random(7000), g.die());
                            }
                            if (c.getCount() % 4 == 0) {
                                //choose some nice frequencies
                                int pitchAlt = pitch;
                                if (random(1) < 0.2)
                                    pitchAlt = Pitch.forceToScale((int) random(12), Pitch.dorian) + (int) random(2) * 12;
                                float freq = Pitch.mtof(pitchAlt + 32);
                                WavePlayer wp = new WavePlayer(ac, freq, Buffer.SQUARE);
                                Gain g = new Gain(ac, 1, new Envelope(ac, 0));
                                g.in(wp);
                                Panner p = new Panner(ac, random(1));
                                p.in(g);
                                ac.out.in(p);
                                Envelope gain = (Envelope) g.getGainUGen();
                                gain.add(random(0.1), random(50));
                                ((Envelope) g.getGainUGen()).add(0, random(400), p.die());
                            }
                            if (c.getCount() % 4 == 0) {
                                Noise n = new Noise(ac);
                                Gain g = new Gain(ac, 1, new Envelope(ac, 0.05f));
                                g.in(n);
                                Panner p = new Panner(ac, random(0.5) + 0.5f);
                                p.in(g);
                                ac.out.in(p);
                                ((Envelope) g.getGainUGen()).add(0, random(100), p.die());
                            }
                        }
                    }
            );
            ac.out.dependsOn(clock);

            ac.start();
            Util.pause(1000000);
        }
    }
    public static class Music2 {
        public static void main(String[] args) {

            final AudioContext ac;

            ac = new AudioContext();
            /*
             * In this example a Clock is used to trigger events. We do this by
             * adding a listener to the Clock (which is of type Bead).
             *
             * The Bead is made on-the-fly. All we have to do is to give the Bead a
             * callback method to make notes.
             *
             * This example is more sophisticated than the previous ones. It uses
             * nested code.
             */
            Clock clock = new Clock(ac, 255);
            clock.on(
                    //this is the on-the-fly bead
                    new Bead() {
                        //this is the method that we override to make the Bead do something
                        int pitch;

                        @Override
                        public void messageReceived(Bead message) {
                            Clock c = (Clock) message;
                            if (c.isBeat()) {
                                //choose some nice frequencies
                                if (random(1) < 0.5) return;
                                pitch = Pitch.forceToScale((int) random(12), Pitch.minor);

                                Envelope e = new Envelope(ac, 0);

                                Gain g = new Gain(ac, 1, e)
                                        .in(new WavePlayer(ac,
                                                Pitch.mtof(pitch + (int) random(5) * 12 + 24),
                                                Buffer.SINE));

                                e.add(0.1f, random(200));
                                e.add(0, random(7000), g.die());

                                ac.out.in(g);

                            }
//                            if (c.getCount() % 4 == 0) {
//                                //choose some nice frequencies
//                                int pitchAlt = pitch;
//                                if (random(1) < 0.2)
//                                    pitchAlt = Pitch.forceToScale((int) random(12), Pitch.dorian) + (int) random(2) * 12;
//                                float freq = Pitch.mtof(pitchAlt + 32);
//                                WavePlayer wp = new WavePlayer(ac, freq, Buffer.SQUARE);
//                                Gain g = new Gain(ac, 1, new Envelope(ac, 0));
//                                g.addInput(wp);
//                                Panner p = new Panner(ac, random(1));
//                                p.addInput(g);
//                                ac.out.addInput(p);
//                                Envelope gain = (Envelope) g.getGainUGen();
//                                gain.addSegment(random(0.1), random(50));
//                                ((Envelope) g.getGainUGen()).addSegment(0, random(400), p.die());
//                            }
//                            if (c.getCount() % 4 == 0) {
//                                Noise n = new Noise(ac);
//                                Gain g = new Gain(ac, 1, new Envelope(ac, 0.05f));
//                                g.addInput(n);
//                                Panner p = new Panner(ac, random(0.5) + 0.5f);
//                                p.addInput(g);
//                                ac.out.addInput(p);
//                                ((Envelope) g.getGainUGen()).addSegment(0, random(100), p.die());
//                            }
                        }
                    }
            );
            ac.out.dependsOn(clock);

            ac.start();
            Util.pause(1000000);
        }

    }

    public static float random(double x) {
        return (float) (Math.random() * x);
    }
}
