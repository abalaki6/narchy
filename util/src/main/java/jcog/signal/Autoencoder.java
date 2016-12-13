package jcog.signal;

import jcog.Util;
import org.eclipse.collections.impl.list.mutable.primitive.ShortArrayList;

import java.util.Random;

/**
 * Denoising Autoencoder (from DeepLearning.net)
 * 
 * TODO parameter for activation function (linear, sigmoid, etc..)
 */
public class Autoencoder {

	final static float NORMALIZATION_EPSILON = 0.0001f;


	/** input vector after preprocessing (noise, corruption, etc..) */
	protected final float[] xx;

	/** output vector */
	final public float[] y;

	public final float[][] W;
	private final float[] hbias;
	private final float[] vbias;
	private final Random rng;

	public final float[] z;
	final private float[] L_vbias;
	final private float[] L_hbias;

	private float uniform(float min, float max) {
		return rng.nextFloat() * (max - min) + min;
	}

	/*
	 * public float binomial(final int n, final float p) { if (p < 0 || p > 1)
	 * { return 0; }
	 * 
	 * int c = 0; float r;
	 * 
	 * for (int i = 0; i < n; i++) { r = rng.nextfloat(); if (r < p) { c++; } }
	 * 
	 * return c; }
	 */


	public Autoencoder(int ins, int outs, Random rng) {

		xx = new float[ins];
		z = new float[ins];
		L_vbias = new float[ins];
		y = new float[outs];
		L_hbias = new float[outs];

		this.rng = rng;

		this.W = new float[outs][ins];
		float a = 1.0f / ins;

		for (int i = 0; i < outs; i++) {
			for (int j = 0; j < ins; j++) {
				this.W[i][j] = uniform(-a, a);
			}
		}



		this.hbias = new float[outs];
		for (int i = 0; i < outs; i++) {
			this.hbias[i] = 0;
		}


		this.vbias = new float[ins];
		for (int i = 0; i < ins; i++) {
			this.vbias[i] = 0;
		}
	}

	private float[] preprocess(float[] x, float noiseLevel, float corruptionRate) {

        Random r = this.rng;
		int ins = x.length;

		float[] xx = this.xx;
		for (int i = 0; i < ins; i++) {
			float v = x[i];
            if ((corruptionRate > 0) && (r.nextFloat() < corruptionRate)) {
				v = 0;
			}
			if (noiseLevel > 0) {
				v += x[i] + r.nextGaussian() * noiseLevel; // (r.nextFloat() - 0.5f) * maxNoiseAmount;
//				if (nx < 0)
//					nx = 0;
//				if (nx > 1)
//					nx = 1;
			}
			xx[i] = v;
		}

		return xx;
	}

	// Encode
	public float[] encode(float[] x, float[] y, boolean sigmoid, boolean normalize) {

		float[][] W = this.W;

		int ins = x.length;
		int outs = y.length;

//		if (y == null)
//			y = new float[outs];

		float[] hbias = this.hbias;

		float max = Float.NEGATIVE_INFINITY, min = Float.POSITIVE_INFINITY;
		for (int i = 0; i < outs; i++) {
			float yi = hbias[i];
			float[] wi = W[i];

			for (int j = 0; j < ins; j++) {
				yi += wi[j] * x[j];
			}

			if (sigmoid)
				yi = Util.sigmoid(yi);

			if (yi > max)
				max = yi;
			if (yi < min)
				min = yi;

			y[i] = yi;

		}



		if ((normalize)) {
			float maxMin = max - min;
			if (!Util.equals(maxMin, 0, NORMALIZATION_EPSILON))

				for (int i = 0; i < outs; i++) {
					y[i] = (y[i] - min) / maxMin;
				}

//to check unit result:
//			float len = cartesianLength(y);
//			if (len > 0) {
//				for (int i = 0; i < nh; i++) {
//					y[i] = y[i] / len;
//				}
//				System.out.println(Arrays.toString(y) + " " + len + " " + cartesianLength(y));
//			}

//			for (int i = 0; i < nh; i++) {
//				y[i] = (y[i] - min) / (max-min);
//			}
			}



		return y;
	}

	private float cartesianLength(float[] y) {
		float d = 0;
		for (float z : y) {
			d += z*z;
		}
		return (float)Math.sqrt(d);
	}

	/** TODO some or all of the bias vectors may need modified too here */
	public void forget(float rate) {
		float f = 1f - rate;
		float[][] w = this.W;
		int O = w.length;
		int I = w[0].length;
		for (int o = 0; o < O; o++) {
			float[] ii = w[o];
			for (int i = 0; i < I; i++)
				ii[i] *= f;
		}
	}

	// Decode
    private float[] decode(float[] y, boolean sigmoid) {
		float[][] w = W;

		float[] vbias = this.vbias;
		int ins = vbias.length;
		int outs = y.length;
		float[] z = this.z;

		for (int i = 0; i < ins; ) {
			float zi = vbias[i];

			for (int j = 0; j < outs; ) {
				zi += w[j][i] * y[j++];
			}

			z[i++] = sigmoid ? Util.sigmoid(zi) : zi;
		}

		return z;
	}


	public int outputs() {
		return y.length;
	}

	public float[] output() {
		return y;
	}

	public float train(float[] x, float learningRate,
					   float noiseLevel, float corruptionRate,
					   boolean sigmoid) {
		return train(x, learningRate, noiseLevel, corruptionRate, sigmoid, true, sigmoid);
	}

	/** returns the total error (not sqr(error) and not avg_error = error sum divided by # items) */
	public float train(float[] x, float learningRate,
					   float noiseLevel, float corruptionRate,
					   boolean sigmoidIn, boolean normalize, boolean sigmoidOut) {

		recode(preprocess(x, noiseLevel, corruptionRate), sigmoidIn, normalize, sigmoidOut);
		return learn(x, y, learningRate);
	}

	/** returns the total error (not sqr(error) and not avg_error = error sum divided by # items) */
	public float learn(float[] x, float[] y, float learningRate) {
		float[][] W = this.W;
		float[] L_hbias = this.L_hbias;
		float[] L_vbias = this.L_vbias;
		float[] vbias = this.vbias;

		int ins = x.length;

		int outs = y.length;

		float errorSq = 0, error = 0;

		float[] zz = z;

		// vbias
		for (int i = 0; i < ins; i++) {

			float lv = x[i] - zz[i];

			error += Math.abs(lv);

			L_vbias[i] = lv;

			errorSq += lv * lv; // square of difference

			vbias[i] += learningRate * lv;
		}

		//error /= ins;


		float[] hbias = this.hbias;


		// hbias
		for (int i = 0; i < outs; i++) {
			L_hbias[i] = 0f;
			float[] wi = W[i];

			float lbi = 0f;
			for (int j = 0; j < ins; j++) {
				lbi += wi[j] * L_vbias[j];
			}
			L_hbias[i] += lbi;

			float yi = y[i];
			L_hbias[i] *= yi * (1f - yi);
			hbias[i] += learningRate * L_hbias[i];
		}

		// W
		float[] xx = this.xx;
		for (int i = 0; i < outs; i++) {
			float yi = y[i];
			float lhb = L_hbias[i];
			float[] wi = W[i];
			for (int j = 0; j < ins; j++) {
				wi[j] += learningRate * (lhb * xx[j] + L_vbias[j] * yi);
			}
		}

		return error;
	}

	public float[] recode(float[] x, boolean sigmoidIn, boolean normalize, boolean sigmoidOut) {
		return decode(encode(x, y, sigmoidIn, normalize), sigmoidOut);
	}

	public float[] reconstruct(float[] x, float[] z) {
		float[] y = new float[this.y.length];

		decode(encode(x, y, true, true), false);

		return z;
	}

	/**
	 * finds the index of the highest output value, or returns a random one if
	 * none are
	 */
	public int max() {

		float m = Float.NEGATIVE_INFINITY;
		int best = -1;
		float[] y = this.y;
		int outs = y.length;
		int start = rng.nextInt(outs); //random starting point to give a fair chance to all if the value is similar
		for (int i = 0; i < outs; i++) {
			int ii = (i + start) % outs;
			float Y = y[ii];
			if (Y > m) {
				m = Y;
				best = ii;
			}
		}
		return best;
	}

	public short[] max(float thresh) {
		float[] y = this.y;
		ShortArrayList s = null;
		int outs = y.length;
		for (int i = 0; i < outs; i++) {
			float Y = y[i];
			if (Y >= thresh) {
				if (s == null)
					s = new ShortArrayList(3 /* est */);
				s.add((short)i);
			}
		}
		if (s == null)
			return null;
		return s.toArray();
	}

}