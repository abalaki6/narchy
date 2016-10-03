package nars;

import nars.concept.FuzzyScalarConcepts;
import nars.concept.SensorConcept;
import nars.term.Compound;
import nars.truth.Truth;
import nars.util.Util;
import nars.util.math.FloatNormalized;
import nars.util.math.FloatSupplier;
import ognl.*;
import org.eclipse.collections.api.block.function.primitive.FloatToObjectFunction;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Created by me on 9/30/16.
 */
public interface NSense {

    Collection<SensorConcept> sensors();

    NAR nar();


    default SensorConcept sense(String term, BooleanSupplier value) {
        return sense(term, () -> value.getAsBoolean() ? 1f : 0f);
    }

    default SensorConcept sense(String term, FloatSupplier value) {
        return sense(term, value, nar().truthResolution.floatValue(), (v) -> $.t(v, alpha()));
    }

    default SensorConcept sense(String term, FloatSupplier value, float resolution, FloatToObjectFunction<Truth> truthFunc) {
        return sense($.$(term), value, resolution, truthFunc);
    }

    default SensorConcept sense(Compound term, FloatSupplier value, float resolution, FloatToObjectFunction<Truth> truthFunc) {
        SensorConcept s = new SensorConcept(term, nar(), value, truthFunc);
        s.resolution(resolution);

        sensors().add(s);
        return s;
    }

    /**
     * learning rate
     */
    default float alpha() {
        return nar().confidenceDefault(Symbols.BELIEF);
    }

    /**
     * interpret an int as a selector between enumerated values
     */
    default <E extends Enum> void senseSwitch(String term, Supplier<E> value) {
        E[] values = ((Class<? extends E>) value.get().getClass()).getEnumConstants();
        for (E e : values) {
            String t = switchTerm(term, e.toString());
            sense(t, () -> value.get() == e);
        }
    }

    static String switchTerm(String term, String e) {
        //return "(" + e + " --> " + term + ")";
        return "(" + term + " , " + e + ")";
    }

    default void senseSwitch(String term, IntSupplier value, int min, int max) {
        senseSwitch(term, value, Util.intSequence(min, max));
    }

    /**
     * interpret an int as a selector between (enumerated) integer values
     */
    default void senseSwitch(String term, IntSupplier value, int[] values) {
        for (int e : values) {
            String t = switchTerm(term, String.valueOf(e));
            sense(t, () -> value.getAsInt() == e);
        }
    }

    /**
     * interpret an int as a selector between (enumerated) object values
     */
    default <O> void senseSwitch(String term, Supplier<O> value, O... values) {
        for (O e : values) {
            String t = switchTerm(term, "\"" + e.toString() + "\"");
            sense(t, () -> value.get().equals(e));
        }
    }


    default void senseFields(String id, Object o) {
        Field[] ff = o.getClass().getDeclaredFields();
        for (Field f : ff) {
            if (Modifier.isPublic(f.getModifiers())) {
                sense(id, o, f.getName());
            }
        }
    }

//    public NObj read(String... expr) {
//        for (String e : expr)
//            read(e);
//        return this;
//    }

    default void sense(String id, Object o, String exp) {

        try {
            //Object x = Ognl.parseExpression(exp);
            Object initialValue = Ognl.getValue(exp, o);


            String classString = initialValue.getClass().toString().substring(6);
            switch (classString) {
                case "java.lang.Double":
                case "java.lang.Float":
                case "java.lang.Long":
                case "java.lang.Integer":
                case "java.lang.Short":
                case "java.lang.Byte":
                case "java.lang.Boolean":
                    senseNumber(id, o, exp);
                    break;

                //TODO String

                default:
                    throw new RuntimeException("not handled: " + classString);
            }
                /*if (y != null) {
                    System.out.println("read: \t" + o + " " + exp + " " + x + " " + x.getClass() + " " + initialValue + " " + initialValue.getClass());
                }*/
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * generic lowest common denominator numeric input
     */
    default Object senseNumber(String id, Object o, String expr) {
        FuzzyScalarConcepts fs = new FuzzyScalarConcepts(

                new FloatNormalized(() -> {
                    try {
                        Object v = Ognl.getValue(expr, o, Object.class);
                        if (v instanceof Boolean) {
                            return ((Boolean) v).booleanValue() ? 1f : 0f;
                        } else if (v instanceof Number) {
                            return ((Number) v).floatValue();
                        } else {
                            return Float.NaN; //unknown
                        }
                    } catch (OgnlException e) {
                        e.printStackTrace();
                        return Float.NaN;
                    }
                }), nar(), id + ":(" + term(expr) + ')'
        );//.resolution(0.05f);
        sensors().addAll(fs.sensors);
        return fs;
    }

    private static String term(Object expr) {

        if (expr instanceof ASTConst) {

            String ae = expr.toString();
            return ae
                    .substring(1, ae.length() - 1); //it's raw field name, wont need quoted

        } else if ((expr instanceof ASTStaticMethod) || (expr instanceof ASTMethod)) {
            String ae = expr.toString();
            String key = //"\"" +
                    ae.substring(0, ae.indexOf('('));
            //+ "\"";
            key = key.replace("@", "X");
            //HACK remove the '@' from the key so it doesnt need quoted:

            return key + '(' +
                    term((SimpleNode) expr)
                    + ')';
        } else if (expr instanceof SimpleNode) {
            return term((SimpleNode) expr);
        } else {
            //safest for unknown type but semantics are lost
            return "\"" + expr + '"';
        }
    }


    private static String term(SimpleNode a) {
        int c = a.jjtGetNumChildren();

        StringBuilder sb = new StringBuilder(16);//.append('(');
        for (int i = 0; i < c; i++) {
            sb.append(term(a.jjtGetChild(i)));
            if (i != c - 1)
                sb.append(',');
        }
        return sb./*.append(')').*/toString();
    }


}