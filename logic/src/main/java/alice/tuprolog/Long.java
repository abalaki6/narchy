/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package alice.tuprolog;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Long class represents the long prolog data type
 *
 *
 *
 */
public class Long extends Number {
   private static final long serialVersionUID = 1L;
   private final long value;
    
    public Long(long v) {
        value = v;
    }
    
    /**
     *  Returns the value of the Integer as int
     *
     */
    @Override
    final public int intValue() {
        return (int) value;
    }
    
    /**
     *  Returns the value of the Integer as float
     *
     */
    @Override
    final public float floatValue() {
        return value;
    }
    
    /**
     *  Returns the value of the Integer as double
     *
     */
    @Override
    final public double doubleValue() {
        return value;
    }
    
    /**
     *  Returns the value of the Integer as long
     *
     */
    @Override
    final public long longValue() {
        return value;
    }
    
    
    /** is this term a prolog integer term? */
    @Override
    final public boolean isInteger() {
        return true;
    }
    
    /** is this term a prolog real term? */
    @Override
    final public boolean isReal() {
        return false;
    }

    /**
     * Returns true if this integer term is grater that the term provided.
     * For number term argument, the int value is considered.
     */
    @Override
    public boolean isGreater(Term t) {
        t = t.term();
        if (t instanceof Number) {
            return value > ( (Number) t ).longValue();
        } else if (t instanceof Struct) {
            return false;
        } else return t instanceof Var;
    }
    @Override
    public boolean isGreaterRelink(Term t, ArrayList<String> vorder) {
        return isGreater(t);
//        t = t.getTerm();
//        if (t instanceof Number) {
//            return value > ( (Number) t ).longValue();
//        } else if (t instanceof Struct) {
//            return false;
//        } else return t instanceof Var;
    }
    
    /**
     * Returns true if this integer term is equal that the term provided.
     * For number term argument, the int value is considered.
     */
    @Override
    public boolean isEqual(Term t) {
        t = t.term();
        return t instanceof Number && value == ((Number) t).longValue();
    }
    
    /**
     * Tries to unify a term with the provided term argument.
     * This service is to be used in demonstration context.
     */
    @Override
    boolean unify(List<Var> vl1, List<Var> vl2, Term t) {
        t = t.term();
        if (t instanceof Var) {
            return t.unify(vl1, vl2, this);
        } else if (t instanceof Number && ((Number) t).isInteger()) {
            return value == ((Number) t).longValue();
        } else {
            return false;
        }
    }
    
    public String toString() {
        return java.lang.Long.toString(value);
    }

    /**
     * @author Paolo Contessi
     */
    @Override
    public int compareTo(Number o) {
        return java.lang.Long.compare(value, o.longValue());
    }
    
}