package spacegraph.widget.slider;

import com.jogamp.opengl.GL2;
import jcog.Texts;
import jcog.Util;
import jcog.math.FloatParam;
import jcog.math.FloatSupplier;
import org.eclipse.collections.api.block.procedure.primitive.FloatObjectProcedure;
import org.eclipse.collections.api.block.procedure.primitive.ObjectFloatProcedure;
import org.jetbrains.annotations.Nullable;
import spacegraph.AspectAlign;
import spacegraph.Scale;
import spacegraph.Surface;
import spacegraph.widget.text.Label;
import spacegraph.widget.windo.Widget;

/**
 * Created by me on 11/18/16.
 */
public class FloatSlider extends Widget {

    final Label label = new Label();
    public FloatSupplier input;
    private String labelText = "";

    private final BaseSlider slider;

    public FloatSlider(float v, float min, float max) {



        children(
            new Scale((slider = new XSlider(v, min, max)), 0.95f),
            label.scale(0.8f).align(AspectAlign.Align.Center)
        );
        updateText();

    }

    public FloatSlider(String label, float v, float min, float max) {
        this(v, min, max);
        this.labelText = label;
        //this.label.set(label);
    }

    public FloatSlider(FloatParam f) {
        this(f.floatValue(), f.min, f.max);
        input = f;
        slider.on((s, v) -> f.set(v));
    }

    public FloatSlider text(String label) {
        this.labelText = label;
        updateText();
        return this;
    }

    @Override
    public synchronized void start(@Nullable Surface parent) {
        super.start(parent);
        updateText();
    }

    private void updateText() {
        this.label.text(text());
    }

    public String text() {
        return this.labelText + Texts.n2(slider.value());
    }

    public FloatSlider type(FloatObjectProcedure<GL2> t) {
        slider.type(t);
        return this;
    }

    public double value() {
        return slider.value();
    }
    public void value(float v) {
        slider.value(v);
    }
    public FloatSlider on(ObjectFloatProcedure<BaseSlider> c) {
        slider.on(c);
        return this;
    }

    private class XSlider extends BaseSlider {

        private final float min;
        private final float max;

        public XSlider(float v, float min, float max) {
            super((v - min) / (max - min));
            this.min = min;
            this.max = max;
        }

        @Override
        protected void paint(GL2 gl, int dtMS) {
//            super.paint(gl, dtMS);
//        }
//
//        @Override
//        protected void paintIt(GL2 gl) {
            if (input != null)
                super.value(input.asFloat());

            super.paint(gl, dtMS);
        }

        @Override
        protected void changed(float p) {
            super.changed(p);
            updateText();
        }

        @Override
        protected float p(float v) {
            return (Util.clamp(v, min, max) - min) / (max - min);
        }

        @Override
        protected float v(float p) {
            return Util.clamp(p, 0, 1f) * (max - min) + min;
        }

    }
}
