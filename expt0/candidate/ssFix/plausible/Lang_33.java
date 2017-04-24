/**
 * Mason : May 17, 2004 : 5:58:26 PM
 */

package net.java.dev.edgecase.util;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.jar.*;

public class ClassTools {

    public static final String VERSION = "$Id: ClassTools.java,v 1.1 2004/10/03 06:53:05 mason Exp $";
	public static final String CLASSPATH = System.getProperty("java.class.path");
	private static final List<String> CLASSPATH_CLASSNAMES = new ArrayList<String>();
	private static final char SPACE = ' ';

	static {
		try {
			CLASSPATH_CLASSNAMES.addAll(findClassesInPath(CLASSPATH));
		} catch (Exception e) {
            Logger.error(e);
		}
	}

	public static final byte[] freeze(Object obj) throws Exception {
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
		ObjectOutputStream objOut = new ObjectOutputStream(bytesOut);
		objOut.writeObject(obj);
		objOut.flush();
		return bytesOut.toByteArray();
	}

    public static final Object thaw(byte[] frozen) throws Exception {
        return new ObjectInputStream(new ByteArrayInputStream(frozen)).readObject();
    }

    public static final Object xerox(Serializable oldObj) throws Exception {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(bytesOut);
        objOut.writeObject(oldObj);
        objOut.flush();
        return new ObjectInputStream(new ByteArrayInputStream(bytesOut.toByteArray())).readObject();
    }

	public static final Object invoke(String classname, String method) throws Exception {
		return invoke(classname, method, null, null);
	}

	public static final Object invoke(String classname, String method, Class[] methparams, Object[] args) throws Exception {
		return invoke(Class.forName(findClassName(classname)), method, methparams, args);
	}

	public static final Object invoke(Object obj, String method, Class[] methparams, Object[] args) throws Exception {
		Class cobj = obj.getClass();
		if (obj instanceof Class) {
			cobj = (Class) obj;
		}
		Method meth = null;
		try {
			meth = cobj.getDeclaredMethod(method, methparams);
		} catch (Exception ex) {}
		if (meth == null) {
			try {
				meth = cobj.getMethod(method, methparams);
			} catch (Exception ex) {}
		}
		if (meth == null) {
			Method[] meths = cobj.getDeclaredMethods();
			for (int i = 0; i < meths.length; i++) {
				if (meths[i].getName().equals(method)) {
					meth = meths[i];
					break;
				}
			}
		}
		if (meth == null) {
			Method[] meths = cobj.getMethods();
			for (int i = 0; i < meths.length; i++) {
				if (meths[i].getName().equals(method)) {
					meth = meths[i];
					break;
				}
			}
		}
		if (meth == null) {
			throw new NoSuchMethodException(method);
		}
		Object robj = obj;
		if (!Modifier.isStatic(meth.getModifiers()) && (obj instanceof Class)) {
			robj = cobj.newInstance();
		}
		meth.setAccessible(true);
		return meth.invoke(robj, args);
	}

	public static final Object load(String classname) throws Exception {
		return Class.forName(findClassName(FileTools.convertFilePath(classname))).newInstance();
	}

	public static final Object load(String classname, Object[] params) throws Exception {
		return Class.forName(findClassName(FileTools.convertFilePath(classname))).getConstructor(getClassesForObjects(params)).newInstance(params);
	}

	public static final Object loadArray(String classname, int count) throws Exception {
		return Array.newInstance(Class.forName(findClassName(FileTools.convertFilePath(classname))), count);
	}

    public static final Object loadArray(String classname, int[] dim) throws Exception {
        return Array.newInstance(Class.forName(findClassName(FileTools.convertFilePath(classname))), dim);
    }

	public static final String findClassName(String shortname) {
		boolean array = false;
		if (shortname == null) {
			return null;
		}
		try {
			Class.forName(shortname);
		} catch (ClassNotFoundException ex) {
			if (shortname.endsWith("[]")) {
				array = true;
				shortname = shortname.substring(0, shortname.length() - 2);
			}
            if (CLASSPATH_CLASSNAMES.contains("java.lang." + shortname)) {
                return "java.lang." + shortname;
            }
			for (Iterator<String> i = CLASSPATH_CLASSNAMES.iterator(); i.hasNext();) {
				String classname = i.next();
				if (classname.endsWith("." + shortname)) {
					return classname;
				}
			}
		}
		if (array) {
			shortname += "[]";
		}
		return shortname;
	}

    public static final String traceCurrentLine() {
        return traceCurrentLine(null, true);
    }

    public static final String traceCurrentLine(boolean printmeth) {
        return traceCurrentLine(null, printmeth);
    }

    public static final String traceCurrentLine(String exclude) {
        return traceCurrentLine(exclude, true);
    }

    public static final String traceCurrentLine(String exclude, boolean printmeth) {
        StackTraceElement[] el = new Throwable().getStackTrace();
        for (int i = 0; i < el.length; i++) {
            String name = el[i].getMethodName();
            if (name.equalsIgnoreCase("traceCurrentLine") || (exclude != null && name.indexOf(exclude) >= 0)) {
                for (int j = i + 1;j < el.length; j++) {
                    String ret = el[j].getFileName() + ":" + el[j].getLineNumber();
                    String namej = el[j].getMethodName();
                    if (printmeth) {
                        ret += ":" + namej + "()";
                    }
                    if ((namej.indexOf("traceCurrentLine") < 0) && (exclude == null || namej.indexOf(exclude) < 0)) {
                        return ret;
                    }
                }
                break;
            }
        }
        throw new RuntimeException("Cannot trace line!");
    }


	public static final String toString(Object o) throws Exception {
		return toString(o, 0, SPACE);
	}

	private static final String toString(Object o, int depth, char spacer) throws Exception {
		XList list = new XList(XFlags.VAL.QUEUE, XFlags.REF.SOFT);
		StringBuffer buff = new StringBuffer(toString(list, o, depth, spacer));
		buff.append(System.getProperty("line.separator", "\n"));
		for (int i = 0; i < list.size(); i++) {
			buff.append("<" + i + "> " + list.get(i));
			buff.append(System.getProperty("line.separator", "\n"));
		}
		return buff.toString();
	}

	private static final String toString(ArrayList printedlist, Object o, int depth, char spacer) throws Exception {
		if (o != null && printedlist.contains(o)) {
			return "[recurse] <" + printedlist.indexOf(o) + ">";
		}
		if (o != null) {
			printedlist.add(o);
		}
		StringBuffer buffer = new StringBuffer();
		if (depth > 20) {
			return "[TOO MUCH!]";
		}
		if (o instanceof Object[]) {
			buffer.append(shortName(o.getClass().getComponentType()) + "[" + ((Object[]) o).length + "]");
			buffer.append(System.getProperty("line.separator", "\n"));
			depth++;
			for (int i = 0; i < ((Object[]) o).length; i++) {
				buffer.append(printSpaces(spacer, depth));
				buffer.append("[" + i + "] ");
				buffer.append(toString(printedlist, ((Object[]) o)[i], depth + 1, spacer));
				buffer.append(System.getProperty("line.separator", "\n"));
			}
		} else if (o != null && (o.toString().indexOf(o.getClass().getName() + "@") >= 0 || o instanceof List)) {
			buffer.append(shortName(o.getClass()));
			buffer.append(System.getProperty("line.separator", "\n"));
			Field[] fields = getAllFields(o.getClass());
			for (int i = 0; i < fields.length; i++) {
				int mod = fields[i].getModifiers();
				if (Modifier.isFinal(mod) && Modifier.isStatic(mod) && fields[i].getType().equals(String.class)) {
					continue;
				}
				fields[i].setAccessible(true);
				String fname = fields[i].getName();
				if (fname.startsWith("this")) {
					continue;
				}
				String fclass = shortName(fields[i].getType());
				Object value = fields[i].get(o);
				buffer.append(printSpaces(spacer, depth + 1));
                appendModifier(mod, buffer);
                buffer.append(fclass + " " + fname + " = ");
				buffer.append(toString(printedlist, value, depth + 1, spacer));
				buffer.append(System.getProperty("line.separator", "\n"));
				fields[i].setAccessible(false);
			}
		} else {
			if (o != null) {
				buffer.append(shortName(o.getClass()) + "(" + o + ")");
			} else {
				buffer.append("null");
			}
		}
		return buffer.toString();
	}

    private static void appendModifier(int mod, StringBuffer buffer) {
        if (Modifier.isPublic(mod)) {
            buffer.append("public ");
        }
        if (Modifier.isPrivate(mod)) {
            buffer.append("private ");
        }
        if (Modifier.isProtected(mod)) {
            buffer.append("protected ");
        }
        if (Modifier.isStatic(mod)) {
            buffer.append("static ");
        }
        if (Modifier.isAbstract(mod)) {
            buffer.append("abstract ");
        }
        if (Modifier.isFinal(mod)) {
            buffer.append("final ");
        }
        if (Modifier.isInterface(mod)) {
            buffer.append("interface ");
        }
        if (Modifier.isNative(mod)) {
            buffer.append("native ");
        }
        if (Modifier.isStrict(mod)) {
            buffer.append("strict ");
        }
        if (Modifier.isSynchronized(mod)) {
            buffer.append("synch ");
        }
        if (Modifier.isTransient(mod)) {
            buffer.append("trans ");
        }
        if (Modifier.isVolatile(mod)) {
            buffer.append("volatile ");
        }
    }

    public static final Field[] getAllFields(Class o) {
		Field[] last = null;
		if (!o.getName().equals(java.lang.Object.class.getName()) && !o.getSuperclass().getName().equals(java.lang.Object.class.getName())) {
			last = getAllFields(o.getSuperclass());
		}
		Field[] fields = o.getFields();
		Field[] dfields = o.getDeclaredFields();
		HashMap<String,Field> map = new HashMap<String, Field>();
		for (int i = 0; i < fields.length; i++) {
			map.put(fields[i].getName(), fields[i]);
		}
		for (int i = 0; i < dfields.length; i++) {
			map.put(dfields[i].getName(), dfields[i]);
		}
		if (last != null) {
			for (int i = 0; i < last.length; i++) {
				map.put(last[i].getName(), last[i]);
			}
		}
		int count = 0;
		Field[] all = new Field[map.values().size()];
		for (Iterator<Field> i = map.values().iterator(); i.hasNext();) {
			Field f = i.next();
			all[count] = f;
			count++;
		}
		return all;
	}

	private static final String printSpaces(char ch, int depth) {
		StringBuffer buffer = new StringBuffer();
		for (int j = 0; j < depth; j++) {
			buffer.append(ch);
			buffer.append(ch);
		}
		return buffer.toString();
	}

	private static final String shortName(Class c) {
		String name;
		if (c.isArray()) {
			name = c.getComponentType().getName() + "[]";
		} else {
			name = c.getName();
		}
		return name.substring(name.lastIndexOf('.') + 1);
	}

	private static final Set<String> findClassesInPath(String path) throws Exception {
		Set<String> classes = new HashSet<String>();
		StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
		while (st.hasMoreTokens()) {
			ArrayList<String> ret = new ArrayList<String>();
			File dir = new File(st.nextToken());
			if (dir.isDirectory()) {
				ret.addAll(walkDir(dir));
			} else if (dir.isFile() && dir.getName().endsWith(".jar")) {
				ret.addAll(walkJar(dir));
			} else {
				if (dir.getName().endsWith(".class")) {
					ret.add(dir.getAbsolutePath().substring(0, dir.getAbsolutePath().length() - ".class".length()));
				}
			}
			if (!ret.isEmpty()) {
				boolean isJar = dir.getAbsolutePath().endsWith(".jar");
				for (Iterator<String> i = ret.iterator(); i.hasNext();) {
					String cfile = i.next();
					if (!isJar) {
						cfile = cfile.substring(dir.getAbsolutePath().length() + 1);
					}
					classes.add(cfile.replace('\\', '.').replace('/', '.'));
				}
			}
		}
		return classes;
	}

	private static final List<String> walkDir(File dir) {
		ArrayList<String> ret = new ArrayList<String>();
		File[] dirs = dir.listFiles();
		for (int i = 0; i < dirs.length; i++) {
			if (dirs[i].isDirectory()) {
				ret.addAll(walkDir(dirs[i]));
			} else if (dirs[i].getName().endsWith(".class")) {
                final String dirpath = dirs[i].getAbsolutePath();
                ret.add(dirpath.substring(0, dirpath.length() - ".class".length()));
			}
		}
		return ret;
	}

	private static final List<String> walkJar(File jar) throws IOException {
		ArrayList<String> ret = new ArrayList<String>();
		Enumeration en = new JarFile(jar).entries();
		while (en.hasMoreElements()) {
			String entryname = en.nextElement().toString();
			if (entryname.endsWith(".class")) {
				ret.add(entryname.substring(0, entryname.length() - ".class".length()));
			}
		}
		return ret;
	}

	private static final Class[] getClassesForObjects(Object[] objs) {
		Class[] classes = new Class[objs.length];
		for (int i = 0; i < objs.length; i++) {
			if (objs[i] != null) {
                classes[i] = objs[i].getClass();
			}
		}
		return classes;
	}

    public static byte[] copy(byte[] src) throws Exception {
        byte[] dest = new byte[src.length];
        System.arraycopy(src, 0, dest, 0, src.length);
        return dest;
    }

	private ClassTools() {}

}