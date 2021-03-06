package spacegraph.audio;

import jcog.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spacegraph.audio.sample.SamplePlayer;
import spacegraph.audio.sample.SoundSample;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class Audio implements Runnable {
    final static Logger logger = LoggerFactory.getLogger(Audio.class);

    private static Audio defaultAudio;

    /**
     * the default audio system
     */
    public synchronized static Audio the() {

            if (defaultAudio == null) {
                defaultAudio = new Audio(4);
            }

        return defaultAudio;
    }



    private final int bufferBytes;
    public final int maxChannels;
    private final SoundSample silentSample;
    private final SourceDataLine sdl;

    private final int rate = 44100;
    private final int bufferSize = rate / 32;

    private final ListenerMixer listenerMixer;


    private final ByteBuffer soundBuffer = ByteBuffer.allocate(bufferSize * 4);
    private final float[] leftBuf, rightBuf;
    //private float amplitude = 1;
    //private float targetAmplitude = 1;
    private boolean alive = true;
    private float now;


    private FileOutputStream rec;
    public Thread thread;


    public Audio(int maxChannels) {

        this.maxChannels = maxChannels;
        silentSample = new SoundSample(new float[]{0}, 44100);
        Mixer mixer = AudioSystem.getMixer(null);

        bufferBytes = bufferSize * 2 * 2;


        SourceDataLine sdl;
        try {
            sdl = (SourceDataLine) mixer.getLine(new Line.Info(SourceDataLine.class));
            sdl.open(new AudioFormat(rate, 16, 2, true, false), bufferBytes);
            soundBuffer.order(ByteOrder.LITTLE_ENDIAN);
            sdl.start();


        } catch (LineUnavailableException e) {
            logger.error("initialize {} {}", this, e);
            thread = null;
            sdl = null;

        }

        this.sdl = sdl;

        try {
            FloatControl volumeControl = (FloatControl) sdl.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(volumeControl.getMaximum());
        } catch (IllegalArgumentException ignored) {
            System.out.println("Failed to set the sound volume");
        }

        listenerMixer = new ListenerMixer(maxChannels);
        setListener(SoundSource.center);

        leftBuf = new float[bufferSize];
        rightBuf = new float[bufferSize];

        thread = new Thread(this);
        thread.setDaemon(true);
        //thread.setPriority(10);
        thread.start();
    }


	/**
	 * Prints information about the current Mixer to System.out.
	 */
	public static void printMixerInfo() {
		Mixer.Info[] mixerinfo = AudioSystem.getMixerInfo();
		for (int i = 0; i < mixerinfo.length; i++) {
			String name = mixerinfo[i].getName();
			if (name.isEmpty())
				name = "No name";
			System.out.println((i+1) + ") " + name + " --- " + mixerinfo[i].getDescription());
			Mixer m = AudioSystem.getMixer(mixerinfo[i]);
			Line.Info[] lineinfo = m.getSourceLineInfo();
			for (int j = 0; j < lineinfo.length; j++) {
				System.out.println("  - " + lineinfo[j]);
			}
		}
	}

    public void record(String path) throws java.io.FileNotFoundException {

        //if (rec != null) //...

        logger.info("recording to: {}", path);
        rec = new FileOutputStream(new File(path), false);

//        PipedInputStream pi = new PipedInputStream();
//        pi.connect(rec = new PipedOutputStream());
//
//        AudioInputStream ais = new AudioInputStream(
//                pi,
//                audioFormat, bufferBytes);
//
//
//        // start recording
//        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(path));

    }

    public void setListener(SoundSource soundSource) {
        listenerMixer.setSoundListener(soundSource);
    }

    public void shutDown() {
        alive = false;
    }

    public int bufferSizeInFrames() {
        return bufferSize;
    }

    static class DefaultSource implements SoundSource {

        private final SoundProducer producer;
        static final float distanceFactor = 1.0f;
        private final float balance;

        DefaultSource(SoundProducer p, float balance) {
            this.producer = p;
            this.balance = balance;
        }

        @Override
        public float getY(float alpha) {
            return 0 + (1.0f - producer.getAmplitude()) * distanceFactor;
        }

        @Override
        public float getX(float alpha) {
            return balance;
        }
    }


    public void play(SoundSample sample, SoundSource soundSource, float volume, float priority) {
        play(new SamplePlayer(sample, rate), soundSource, volume, priority);
    }

    public <S extends SoundProducer> Sound<S> play(S p, float volume, float priority, float balance) {
        return play(p, new DefaultSource(p, balance), volume, priority);
    }

    public <S extends SoundProducer> Sound<S> play(S p, SoundSource soundSource, float volume, float priority) {
//        if (!alive)
//            return;
        return listenerMixer.addSoundProducer(p, soundSource, volume, priority);
    }


    public void clientTick(float alpha) {
        listenerMixer.update(alpha);
    }

    static final int max16 = 32767;

    void tick() {
        //soundBuffer.clear();

        //        targetAmplitude = (targetAmplitude - 1) * 0.9f + 1;
        //        targetAmplitude = (targetAmplitude - 1) * 0.9f + 1;
        listenerMixer.read(leftBuf, rightBuf, rate);
        ////            if (maxAmplitude > targetAmplitude) targetAmplitude = maxAmplitude;


        soundBuffer.clear();

        float gain = max16;

        byte[] ba = soundBuffer.array();

        int b = 0;
        for (int i = 0; i < bufferSize; i++) {
            short l = ((short)Util.clamp(leftBuf[i] * gain, -max16, max16));
            ba[b++] = (byte) (l & 0x00ff);
            ba[b++] = (byte) (l >> 8);
            short r = ((short)Util.clamp(rightBuf[i] * gain, -max16, max16));
            ba[b++] = (byte) (r & 0x00ff);
            ba[b++] = (byte) (r >> 8);
        }

        sdl.write(ba, 0, bufferSize * 2 * 2);
        if (rec != null) {
            try {
                rec.write(ba, 0, bufferSize * 2 * 2);
                rec.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        now = System.currentTimeMillis();
        int idle = 0;
        while (alive) {

            if (listenerMixer.isEmpty()) {
                Util.pauseNext(idle++);
            } else {
                idle = 0;
            }

            now = System.currentTimeMillis() - now;

            clientTick(now);

            tick();

        }
    }


    public void setNow(float now) {
        this.now = now;
    }
}