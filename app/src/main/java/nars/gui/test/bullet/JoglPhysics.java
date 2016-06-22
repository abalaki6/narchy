/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * Bullet Continuous Collision Detection and Physics Library
 * Copyright (c) 2003-2007 Erwin Coumans  http://continuousphysics.com/Bullet/
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose, 
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package nars.gui.test.bullet;

import com.jogamp.newt.event.*;
import com.jogamp.opengl.*;
import nars.util.JoglSpace;

/**
 *
 * @author jezek2
 */

public class JoglPhysics extends JoglSpace implements MouseListener {
	

    private DemoApplication app;

    @Override
    public void mouseClicked(MouseEvent e) {
        //if(e.getClickCount()>1) {
            //quit=true;
        //}
    }
    @Override
    public void mouseEntered(MouseEvent e) {
    }
    @Override
    public void mouseExited(MouseEvent e) {
    }
    @Override
    public void mousePressed(MouseEvent e) {
    }
    @Override
    public void mouseReleased(MouseEvent e) {
    }
    @Override
    public void mouseMoved(MouseEvent e) {
    }
    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {

    }



    public void run(DemoApplication demoApp) {
        this.app = demoApp;
    }

    @Override
    protected void init(GL2 gl2) {
        super.init(gl2);

        //GLProfile.setProfileGLAny();
//        try {
//            NWCapabilities caps = new NWCapabilities();
//            // For emulation library, use 16 bpp
//            caps.setRedBits(5);
//            caps.setGreenBits(6);
//            caps.setBlueBits(5);
//            caps.setDepthBits(16);

//            Window nWindow = null;
//            if(0!=(type&USE_AWT)) {
//                Display nDisplay = NewtFactory.createDisplay(NativeWindowFactory.TYPE_AWT, null); // local display
//                Screen nScreen  = NewtFactory.createScreen(NativeWindowFactory.TYPE_AWT, nDisplay, 0); // screen 0
//                nWindow = NewtFactory.createWindow(NativeWindowFactory.TYPE_AWT, nScreen, caps, false);
//                window = GLWindow.create(nWindow);
//            } else {
//                window = GLWindow.create(caps);
//            }


            window.addWindowListener(this);
            window.addMouseListener(this);
            window.addMouseListener(app);
            window.addKeyListener(app);

            window.removeGLEventListener(this);
            window.addGLEventListener(app);

            // window.setEventHandlerMode(GLWindow.EVENT_HANDLER_GL_CURRENT); // default
            // window.setEventHandlerMode(GLWindow.EVENT_HANDLER_GL_NONE); // no current ..

//            window.setUpdateFPSFrames(FPSCounter.DEFAULT_FRAMES_PER_INTERVAL, System.err);
//            // Size OpenGL to Video Surface
//            window.setSize(width, height);
//            window.setFullscreen(true);
//            window.setVisible(true);
//            width = window.getWidth();
//            height = window.getHeight();


            // Shut things down cooperatively
//            window.destroy();
//        } catch (Throwable t) {
//            t.printStackTrace();
//        }

    }


    @Override
    public void display(GLAutoDrawable drawable) {
        //System.out.println("wrong display");
    }
}
