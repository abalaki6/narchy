package spacegraph.widget.tab;

import jcog.data.ArrayHashSet;
import org.eclipse.collections.api.block.procedure.primitive.ObjectBooleanProcedure;
import org.jetbrains.annotations.Nullable;
import spacegraph.layout.Grid;
import spacegraph.widget.button.ToggleButton;

import java.util.Collections;

/** set of buttons, which may be linked behaviorally in various ways */
public class ButtonSet<T extends ToggleButton> extends Grid {


    /** uses both set and list (for ordering) aspects of the ArrayHashSet */
    final ArrayHashSet<T> buttons = new ArrayHashSet<>();
    private final Mode mode;
    public ObjectBooleanProcedure<T> action = null;

    public enum Mode {
        /**  all disabled */
        Disabled,

        /** only one can be enabled at any time */
        One,

        /** multiple can be enabled */
        Multi
    }

    public ButtonSet(Mode mode, T... buttons) {
        super();

        this.mode = mode;

        for (T b : buttons) {
            this.buttons.add(b);
            @Nullable ObjectBooleanProcedure<ToggleButton> outerAction = b.action;
            b.on((bb,e) -> {
                if (e) {
                    if (mode == Mode.Multi) {
                        //allow freely to toggle
                    } else if (mode == Mode.One) {
                        this.buttons.forEach(cc -> {
                            if (cc != bb)
                                cc.set(false);
                        });
                    }
                } else {
//                    if (mode == Mode.One) {
//                        bb.set(true); //HACK dont allow to untoggle
//                        return;
//                    }
                }

                if (outerAction != null)
                    outerAction.value(bb, e);
                if (action!=null)
                    action.value((T)bb, e);
            });

        }

        Collections.addAll(children, buttons);

        if (mode == Mode.One) {
            //select the first by default
            this.buttons.first().set(true);
        }
    }

    public void on(ObjectBooleanProcedure<T> action) {
        this.action = action;
    }
}
