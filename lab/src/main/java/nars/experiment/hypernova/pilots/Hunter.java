package nars.experiment.hypernova.pilots;

import nars.experiment.hypernova.Mass;
import nars.experiment.hypernova.Ship;

public class Hunter extends Pilot {
    public static final double TARGET_DIST = 60.0;

    protected Mass target;

    public Hunter(Ship ship, Mass target) {
        super(ship);
        setTarget(target);
    }

    public void setTarget(Mass target) {
        this.target = target;
    }

    public void fireShots(Ship ship)
    {
      ship.fire(0);
    }

    public void noFire() {}

    @Override
    public void drive(double dt) {
        if (target == null) return;

        Ship ship = getShip();
        double x = ship.getX(0);
        double y = ship.getY(0);
        double px = target.getX(0);
        double py = target.getY(0);
        double dir = Math.atan2(py - y, px - x);
        face(dt, dir);

        double diff = dir - ship.getA(0);
        if (Math.abs(diff) < 0.05 && target.isActive()) fireShots(ship);
        else noFire();

        double dist = Math.sqrt((px - x) * (px - x) + (py - y) * (py - y));
        ship.setEngines((dist - TARGET_DIST) / 250);
    }
}