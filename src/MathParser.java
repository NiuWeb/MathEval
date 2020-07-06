import java.util.regex.*;
import java.util.HashMap;
import java.lang.reflect.*;

public class MathParser {
	//Lista de funciones
	Object Functions;
	
	//REGEXES
	//Regex para whitespace
	private final Pattern whitespace  = Pattern.compile("(\\s+)");
	//private Matcher whitespacem;
	
	//Regex para cadena vacía
	private final Pattern emptystr = Pattern.compile("^\\s*$");
	//private Matcher emptystrm;
	
	//regex para número
	private final String numberStr = "((\\+|-)?[0-9]+(\\.[0-9]+)?(E(\\+|-)?[0-9]+(\\.[0-9]+)?)?)";
	private final Pattern number   = Pattern.compile(numberStr);  
	//private Matcher numberm;  
	private final int nstart = 7;
	
	//Regex para caracteres de operaciones aritméticas ordenados jerárquicamente
	private final String[] arithmetic = new String[] {
		"(\\^)",
		"(\\*|\\/|%)",
		"(\\+|-)"
	};
	private final Pattern[] arithmeticP = new Pattern[arithmetic.length];
	//private Matcher[] arithmatcher = new Matcher[arithmetic.length];
	
	private final int sumpos = 2; //posición de las sumas/restas en el arreglo
	private final Pattern signstr = Pattern.compile("(" + arithmetic[sumpos] + "{2,})");
	//private Matcher signstrm;
	
	private final char rest  = '-'; //caracter de resta
	private final Pattern group = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)?\\(([^()]*)\\)"); //regex para grupos
	
	private double result = 0; //resultado del parse
	
	//Constructor/inicialización
	MathParser(Object f) throws IllegalArgumentException, IllegalAccessException {
		
		//Inicialización de funciones
		Functions = f;
		setFunctions();
		
		/*
		
		SOBRE LA INICIALIZACIÓN DE LAS FUNCIONES QUE SE PUEDEN EJECUTAR EN EL INTÉRPRETE:
		
		La lista de funciones se debe escribir en una clase, donde cada función es un método público
		de la misma, y a cada método le debe corresponder un atributo público de tipo entero y con el
		mismo nombre, que almacene la cantidad de parámetros que dicho método recibe.
		
		Los N parámetros que recibe la función se pasan como un arreglo de double[N], y siempre se debe
		devolver double
		
		Por ejemplo:
		
		class Functions {
			public int func1 = 3;
			public double func1(double[] x) {
				return x[0] + x[1] + x[2]
			}
		}
		
		
		*/
		
		//crear regex para operaciones aritméticas
		for(int i = 0; i < arithmetic.length; i++) {
			arithmeticP[i] = Pattern.compile(numberStr + arithmetic[i] + numberStr);
		}
	}
	
	
	//Lista de errores
	private static class Error {
		static double error = 0;
		
		static void show(String t) {
			System.out.println(t);
			//JOptionPane.showMessageDialog(null, t, "ERROR", JOptionPane.ERROR_MESSAGE);
		}
		static void sintax(int line, String ch) {
			show("sintaxis desconocida en la línea " + line + " (" + ch + ")");
			error = 1;
		}
		static void refference(int line, String ch) {
			show("función o variable desconocida en la línea " + line + " (" + ch + ")");
			error = 2;
		}
		static void arguments(int line, String f, int a, int b) {
			show("Cantidad incorrecta de parámetros en la línea " + line + " (" + f
			+ " requiere " + a + " parámetros, pero se le suministraron " + b + ")");
			error = 3;
		}
	}
	
	///GETTER/SETTER
	public double getResult() {
		return result;
	}
	public double getError() {
		return Error.error;
	}
	
	//Función principal
	public void parse(String str) 
	throws IllegalAccessException, 
	IllegalArgumentException, 
	InvocationTargetException {
		//Eliminar whitespace
		Matcher m  = whitespace.matcher(str);
		str = m.replaceAll("");
		
		result = parseGroup(str, 1);
	}
	///ARITMÉTICA
	//Ejecutar instrucciones aritméticas (a+b ,a-b, a*b, a/b, a%b)
	private double evalArithmetic(double A, double B, String o) {
		double r;
		
		//Ejecutar la operación aritmética correspondiente con el caracter
		switch(o) {
			default:
				r = 0;
				break;
				
			case "^":
				r = Math.pow(A, B);
				break;
				
			case "*":
				r = A*B;
				break;
			  
			case "/":
				r = A/B;
				break;
			
			case "%":
				r = A%B;
				break;
				
			case "+":
				r = A+B;
				break;
				
			case "-":
				r = A-B;
				break;
		}
		
		return r;
	}
	
	//Interpretar líneas como operaciones aritméticas
	private double parseArithmetic(String str, int line) {
		
		Matcher m;
		//Devolver 0 en caso de cadena vacía
		m = emptystr.matcher(str);
		if(m.find()) {
			return 0;
		}
		
		//Condensar cadenas de signos +-
		m = signstr.matcher(str);
		String c;
		int start, end;
		while(m.find()) {
			
			//obtener inicio y final de la coincidencia en la cadena
			start = m.start();
			end = m.end();
			
			//obtener coincidencia
			c = str.substring(start, end);
			//contar cantidad de caracteres de resta
			int n = 0;
			for(int i = 0; i < c.length(); i++) {
				char p = c.charAt(i);
				if(p == rest) {
					n++;
				}
			}
			c = ( n%2 == 0 ) ? "+": "-";
			
			str = str.substring(0, start) + c + str.substring(end, str.length());
			m = signstr.matcher(str);
		}
		//buscar las coincidencis con operaciones aritméticas
		for (Pattern ar : arithmeticP) {
			m = ar.matcher(str);
			double A, B, r;
			String o;
			while(m.find()) {
				//obtener partes de la operación (AoB)
				A = Double.parseDouble(str.substring(m.start(1), m.end(1)));
				B = Double.parseDouble(str.substring(m.start(nstart+1), m.end(nstart+1)));
				o = str.substring(m.start(nstart), m.end(nstart));
				
				r = evalArithmetic(A, B, o);
						
				start = m.start();
				end = m.end();

				//Reemplazar coincidencia con el resultado de la operación
				str =
					str.substring(0, start) + (r>0?"+":"") + r +
					str.substring(end, str.length());
				
				m = ar.matcher(str);
			}
		}
		
		//Revisar errores de sintaxis
		m = number.matcher(str);
		String after = m.replaceAll("");
		
		boolean error = !after.equals("");
		if(error) {
			Error.sintax(line, after);
		}
		
		return error ? 0: Double.parseDouble(str);
	}
	
	///LISTA DE FUNCIONES
	//Cantidad de parámetros de cada función
	private final HashMap<String, Integer> FunctionsParams = new HashMap<>();
	//Lista de funciones
	private final HashMap<String, Method> FunctionsMethods = new HashMap<>();
	
	//Recoger datos del objeto ingresado
	private void setFunctions() throws IllegalArgumentException, IllegalAccessException {       
		
		Class c = Functions.getClass(); //obtener clase
		Field[] field   = c.getFields(); //obtener atributos de la clase
		Method[] method = c.getMethods(); //obtener métodos de la clase
		
		for(Field f: field) { //conseguir el nombre y valor de cada atributo
			//cada atributo determina la cantidad de parámetros de cada método
			String name = f.getName();
			int value   = (int) f.get(Functions);
			FunctionsParams.put(name, value);
		}
		for(Method m: method) { 
			//añadir el método a la lista solo si tiene su correspondiente atributo
			//determinando la cantidad de parámetros necesarios
			String name = m.getName();
			if(FunctionsParams.containsKey(name)) {
				FunctionsMethods.put(name, m);
			}
		}
	}
	
	//evaluar una función
	private double evalGroup(String fun, double[] params, int line) 
	throws 
	IllegalAccessException, 
	IllegalArgumentException, 
	InvocationTargetException {
		double r = 0;
		
		if(FunctionsParams.containsKey(fun)) { //Si la función ingresada existe
			int nparam = FunctionsParams.get(fun); //número de parámetros de la función
			if(params.length != nparam) {
				//lanzar error si la cantidad de parametros no coincide
				Error.arguments(line, fun, nparam, params.length); 
				return r;
			}
			
			r = (double) FunctionsMethods.get(fun).invoke(Functions, params);
		}
		else if(fun.equals("")) { //Grupo sin función (solo agrupador)
			if(params.length >= 1) {
				r = params[0];
			}
			else {
				r = 0;
			}
		}
		else {
			//Error: función desconocida
			Error.refference(line, fun);
		}
		return r;
	}
	
	//Ejecutar agrupadores de la forma funcion(param1, param2, ..., paramN)
	private double parseGroup(String str, int line) 
	throws 
	IllegalAccessException, 
	IllegalArgumentException, 
	InvocationTargetException {
		
		//regex de grupo
		Matcher m = group.matcher(str);
		
		String fun, cont;
		String[] params;
		double[] dparams;
		double r;
		String p1, p2;
		int start, end;
		while(m.find()) {
			
			//obtener cadeja ajena a la coincidencia
			start = m.start();
			end = m.end();
			
			p1 = str.substring(0, start);
			p2 = str.substring(end, str.length());
			
			//obtener nombre de la función que agrupa (si lo hay)
			start = m.start(1);
			end = m.end(1);
			
			if(start >= 0 && end >= 0) {
				fun = str.substring(start, end);
			}
			else fun = "";
			
			//obtener contenido del grupo
			start = m.start(2);
			end = m.end(2);
			if(start == -1 || end == -1) {
				break;
			}
			cont = str.substring(start, end);
			
			//Dividir contenido en parámetros
			params = cont.split(",");
			int pcount = 0;
			for(int i = 0; i < params.length; i++) {
				if(!params[i].equals("")) {
					pcount++;
				}
			}
			dparams = new double[pcount];
			
			for(int i = 0; i < params.length; i++) {
				if(!params[i].equals("")) {
					dparams[i] = parseArithmetic(params[i], line);
				}
			}
			//evaluar el grupo y la función
			r = evalGroup(fun, dparams, line);
			
			//reemplazar resultado por coincidencia
			str = p1 + r + p2;
			m = group.matcher(str);
		}
		return parseArithmetic(str, line);
	}
}