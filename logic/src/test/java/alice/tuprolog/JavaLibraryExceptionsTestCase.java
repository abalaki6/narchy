package alice.tuprolog;


import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Matteo Iuliani
 * 
 *         Test del funzionamento delle eccezioni lanciate dai predicati della
 *         JavaLibrary
 */
public class JavaLibraryExceptionsTestCase {

	// verifico che java_object(ClassName, ArgList, ObjId) lancia una
	// ClassNotFoundException se ClassName non identifica una classe Java valida
	@Test
	public void test_java_object_3_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "java_catch(java_object('Counter', ['MyCounter'], c), [('java.lang.ClassNotFoundException'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());
	}

	// verifico che java_object(ClassName, ArgList, ObjId) lancia una
	// NoSuchMethodException se il costruttore non esiste
	@Test public void test_java_object_3_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "java_catch(java_object('java.util.ArrayList', [a], c), [('java.lang.NoSuchMethodException'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());
	}

	// verifico che java_object(ClassName, ArgList, ObjId) lancia una
	// InvocationTargetException se gli argomenti di ArgList non sono "ground"
	@Test public void test_java_object_3_3() throws Exception {
		Prolog engine = new Prolog();
		String goal = "java_catch(java_object('java.util.ArrayList', [X], c), [('java.lang.reflect.InvocationTargetException'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());
	}

	// verifico che java_object(ClassName, ArgList, ObjId) lancia una
	// Exception se ObjId gia' riferisce un altro oggetto nel sistema
	@Test public void test_java_object_3_4() throws Exception {
		Prolog engine = new Prolog();
		String goal = "java_object('java.util.ArrayList', [], c), java_catch(java_object('java.util.ArrayList', [], c), [('java.lang.Exception'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());
	}

	// verifico che java_object_bt(ClassName, ArgList, ObjId) lancia una
	// ClassNotFoundException se ClassName non identifica una classe Java valida
	@Test public void test_java_object_bt_3_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "java_catch(java_object_bt('Counter', ['MyCounter'], c), [('java.lang.ClassNotFoundException'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());
	}

	// verifico che java_object_bt(ClassName, ArgList, ObjId) lancia una
	// NoSuchMethodException se il costruttore non esiste
	@Test public void test_java_object_bt_3_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "java_catch(java_object_bt('java.util.ArrayList', [a], c), [('java.lang.NoSuchMethodException'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());
	}

	// verifico che java_object_bt(ClassName, ArgList, ObjId) lancia una
	// InvocationTargetException se gli argomenti di ArgList non sono "ground"
	@Test public void test_java_object_bt_3_3() throws Exception {
		Prolog engine = new Prolog();
		String goal = "java_catch(java_object_bt('java.util.ArrayList', [X], c), [('java.lang.reflect.InvocationTargetException'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());
	}

	// verifico che java_object_bt(ClassName, ArgList, ObjId) lancia una
	// Exception se ObjId gia' riferisce un altro oggetto nel sistema
	@Test public void test_java_object_bt_3_4() throws Exception {
		Prolog engine = new Prolog();
		String goal = "java_object_bt('java.util.ArrayList', [], c), java_catch(java_object('java.util.ArrayList', [], c), [('java.lang.Exception'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());
	}

	// verifico che java_class(ClassSourceText, FullClassName, ClassPathList,
	// ObjId) lancia una ClassNotFoundException se ClassSourceText contiene
	// errori
	@Test public void test_java_class_4_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "Source = 'public class Counter { , }', java_catch(java_class(Source, 'Counter', [], c), [('java.io.IOException'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());
		new File("Counter.java").delete();
	}

	// verifico che java_class(ClassSourceText, FullClassName, ClassPathList,
	// ObjId) lancia una ClassNotFoundException se la classe non puo' essere
	// localizzata nella gerarchia dei package cosi' come specificato
	@Test public void test_java_class_4_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "Source = 'public class Counter {  }', java_catch(java_class(Source, 'Counter', [], c), [('java.lang.ClassNotFoundException'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);

		new File("Counter.java").delete();
		new File("Counter.class").delete();
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());

	}

	// verifico che java_call(ObjId, MethodInfo, ObjIdResult) lancia una
	// MoSuchMethodException se si invoca un metodo non valido per l'oggetto o
	// la classe
	@Test public void test_java_call_3_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "java_object('java.util.ArrayList', [], l), java_catch(java_call(l, sizes, res), [('java.lang.NoSuchMethodException'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());
	}

	// verifico che java_call(ObjId, MethodInfo, ObjIdResult) lancia una
	// NoSuchMethodException se gli argomenti del metodo non sono validi
	@Test public void test_java_call_3_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "java_object('java.lang.String', ['call'], s), java_catch(java_call(s, charAt(a), res), [('java.lang.NoSuchMethodException'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());
	}

	// verifico che java_call(ObjId, MethodInfo, ObjIdResult) lancia una
	// NoSuchMethodException se gli argomenti del metodo non sono "ground"
	@Test public void test_java_call_3_3() throws Exception {
		Prolog engine = new Prolog();
		String goal = "java_object('java.lang.String', ['call'], s), java_catch(java_call(s, charAt(X), res), [('java.lang.NoSuchMethodException'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());
	}

	// verifico che returns('<-'(ObjId, MethodInfo), ObjIdResult) lancia una
	// NoSuchMethodException se si invoca un metodo non valido per l'oggetto o
	// la classe
	@Test public void test_java_returns_2_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "java_object('java.util.ArrayList', [], l), java_catch((l <- sizes returns res), [('java.lang.NoSuchMethodException'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());
	}

	// verifico che returns('<-'(ObjId, MethodInfo), ObjIdResult) lancia una
	// NoSuchMethodException se gli argomenti del metodo non sono validi
	@Test public void test_java_returns_2_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "java_object('java.lang.String', ['call'], s), java_catch((s <- charAt(a) returns res), [('java.lang.NoSuchMethodException'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());
	}

	// verifico che returns('<-'(ObjId, MethodInfo), ObjIdResult) lancia una
	// NoSuchMethodException se gli argomenti del metodo non sono "ground"
	@Test public void test_java_returns_2_3() throws Exception {
		Prolog engine = new Prolog();
		String goal = "java_object('java.lang.String', ['call'], s), java_catch((s <- charAt(X) returns res), [('java.lang.NoSuchMethodException'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());
	}

	// verifico che java_array_set(ObjArrayId, Index, ObjId) lancia una
	// IllegalArgumentException se Index non rappresenta un valore corretto
	@Test public void test_java_array_set_3_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "java_object('java.lang.String[]', [1], s), java_catch(java_array_set(s, -1, a), [('java.lang.IllegalArgumentException'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());
	}

	// verifico che java_array_set(ObjArrayId, Index, ObjId) lancia una
	// IllegalArgumentException se ObjId non e' un valore corretto
	@Test public void test_java_array_set_3_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "java_object('java.lang.String[]', [1], s), java_catch(java_array_set(s, 0, 1), [('java.lang.IllegalArgumentException'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());
	}

	// verifico che java_array_set(ObjArrayId, Index, ObjId) lancia una
	// IllegalArgumentException se ObjArrayId non riferisce alcun oggetto del
	// sistema
	@Test public void test_java_array_set_3_3() throws Exception {
		Prolog engine = new Prolog();
		String goal = "java_object('java.lang.String[]', [1], s), java_catch(java_array_set(x, 0, a), [('java.lang.IllegalArgumentException'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());
	}

	// verifico che java_array_get(ObjArrayId, Index, ObjIdResult) lancia una
	// IllegalArgumentException se Index non rappresenta un valore corretto
	@Test public void test_java_array_get_3_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "java_object('java.lang.String[]', [1], s), java_catch(java_array_get(s, -1, ObjIdResult), [('java.lang.IllegalArgumentException'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());
	}

	// verifico che java_array_get(ObjArrayId, Index, ObjIdResult) lancia una
	// IllegalArgumentException se ObjArrayId non riferisce alcun oggetto del
	// sistema
	@Test public void test_java_array_get_3_3() throws Exception {
		Prolog engine = new Prolog();
		String goal = "java_object('java.lang.String[]', [1], s), java_catch(java_array_set(x, 0, ObjIdResult), [('java.lang.IllegalArgumentException'(Cause, Message, StackTrace), true)], true).";
		Solution info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Term cause = info.getTerm("Cause");
		assertFalse(cause instanceof Var);
		Term message = info.getTerm("Message");
		assertFalse(message instanceof Var);
		Term stackTrace = info.getTerm("StackTrace");
		assertTrue(stackTrace.isList());
	}

}