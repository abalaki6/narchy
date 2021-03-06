package spacegraph.space;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.opengl.GL2;
import spacegraph.SpaceGraph;
import spacegraph.Surface;
import spacegraph.render.Draw;

/**
 * Created by me on 6/27/16.
 */
public class CrosshairSurface extends Surface implements MouseListener {

    private final SpaceGraph space;
    int mx, my;
    boolean mouseEnabled;
    private float smx, smy;
    private short[] pressed;


    public CrosshairSurface(SpaceGraph s) {
        this.space = s;
    }


    @Override
    protected void paint(GL2 gl, int dtMS) {
        gl.glPushMatrix();

        if (!mouseEnabled) {
            //HACK
            space.addMouseListener(this);
            mouseEnabled = true;
        }


        //gl.glTranslatef(mx, my, 0);
        float g, b;
        float r = g = b = 0.75f;
        if (pressed!=null && pressed.length > 0) {
            switch (pressed[0]) {
                case 1:
                    r = 1f; g = 0.5f; b = 0f;
                    break;
                case 2:
                    r = 0.5f; g = 1f; b = 0f;
                    break;
                case 3:
                    r = 0f; g = 0.5f; b = 1f;
                    break;
            }
        }
        gl.glColor4f(r, g, b, 0.6f);

        gl.glLineWidth(4f);
        float ch = 175f; //TODO proportional to ortho height (pixels)
        float cw = 175f; //TODO proportional to ortho width (pixels)
        Draw.rectStroke(gl, smx-cw/2f, smy-ch/2f, cw, ch);

        float hl = 1.25f; //cross hair length
        Draw.line(gl, smx, smy-ch*hl, smx, smy+ch*hl);
        Draw.line(gl, smx-cw*hl, smy, smx+cw*hl, smy);

        gl.glPopMatrix();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        update(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        update(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        update(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        update(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        update(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        update(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        update(e);
    }

    private void update(MouseEvent e) {
        mx = e.getX();
        smx = ((float)mx) ;
        my = e.getY();
        smy = (space.getHeight() - ((float)my)) ;

        pressed = e.getButtonsDown();
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {
        update(e);
    }


}
