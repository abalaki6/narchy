package nars;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Javascript NAR Runner
 * 
 * @author me
 */
public class NARjs {
	static final ScriptEngineManager factory = new ScriptEngineManager();

	final ScriptEngine js = factory.getEngineByName("JavaScript");

	private static final ThreadLocal<NARjs> the = ThreadLocal.withInitial(NARjs::new);

	public static NARjs the() {
		return the.get();
	}

	private NARjs() {
		try {
			js.eval("load('nashorn:mozilla_compat.js')");

			js.eval("importPackage('java.lang')");
			js.eval("importPackage('java.other')");
			js.eval("importPackage('java.io')");

			js.eval("importPackage('nars')");
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}

//		js.eval("importPackage('nars.nal')");
//		js.eval("importPackage('nars.nal.nal7')");
//		js.eval("importPackage('nars.builder')");
//		js.eval("importPackage('nars.io')");
//		js.eval("importPackage('nars.gui')");
//		js.eval("function newDefaultNAR() { var x = new DefaultNARBuilder().builder(); new TextOutput(x, System.out); return x; }");
	}

	public Object eval(String s) throws ScriptException {
		return js.eval(s);
	}

	public static void printHelp() {
		System.out.println("Help coming soon.");
	}

	public static void main(String[] args) throws java.io.IOException {
		NARjs j = NARjs.the();

		System.out.println(NAR.VERSION
				+ " Javascript Console - :h for help, :q to exit");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("> ");

		String s;
		while ((s = br.readLine()) != null) {

			try {
				if (":q".equals(s))
					break;
				if (s.startsWith(":h")) {
					printHelp();
					continue;
				}

				Object ret = j.eval(s);

				if (ret != null) {
					System.out.println(ret);
				}
			} catch (ScriptException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
				System.out.println(e.getClass().getName() + " in parsing: "
						+ e.getMessage());
			} finally {

				System.out.print("> ");

			}
		}

		br.close();
		System.exit(0);
	}
}
