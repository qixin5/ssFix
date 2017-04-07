package src;

/**
 * @author Adam Mazurek
 *
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Scanner {
	
	private ProjectGUI parent;
	
	public Scanner(ProjectGUI parent) {
		this.parent = parent;
	}
	public static final String[] keyWords = new String[] {
		"boolean","program","begin","end","integer","real","while","do","var","if","then","else","read","write","end."
	};
	
	public static final String[] operators = new String[] {
		"+","-","/","*","div","mod",":=","=",">",">=","<","<=","<>","(",")",".",";"
	};
	
	public static boolean isArithemiticalExpression(String s) {
		return (s.equals("+") || s.equals("-") || s.equals("/") || s.equals("*") || s.equals("div") || s.equals("mod"));
	}
	
	/**
	 * Czy s³owo kluczowe
	 * @param s
	 * @return
	 */
	public static boolean isKeyword(String s) {
		for (String str : keyWords)
			if (str.equalsIgnoreCase(s)) 
				return true;
		return false;
	}
	/**
	 * Czy identyfikator
	 * @param s
	 * @return
	 */
	public static boolean isIdentifier(String s) {
		
		if (isKeyword(s) || isOperator(s) || isInteger(s) || isReal(s))
			return false;
		for (String str : operators)
			if (s.indexOf(str)!=-1)
				return false;
		if (s.equals(";"))
			return false;
		if (s.length()==0) return false;
		char c = s.charAt(0);
		if (c=='0' || c=='1' || c=='2'|| c=='3' || c=='4' || c=='5' || c=='6' || c=='7' || c=='8' || c=='9')
			return false;
		if (s.indexOf("\'")!=-1 ||  s.indexOf('!')!=-1 || s.indexOf('@')!=-1 || s.indexOf('$')!=-1 || s.indexOf('^')!=-1 || s.indexOf('*')!=-1 || s.indexOf('(')!=-1 || s.indexOf(')')!=-1 || s.indexOf('~')!=-1 || s.indexOf('#')!=-1 || s.indexOf('%')!=-1)
			return false;
		if (s.indexOf("{")!=-1 || s.indexOf('}')!=-1 || s.indexOf('"')!=-1 || s.indexOf('.')!=-1 || s.indexOf(',')!=-1 || s.indexOf(":")!=-1)
			return false;
		if (s.length()>1 && s.indexOf(";")!=-1)
		   if (s.indexOf(";")!=(s.length()-1)) 
			   return false;

		return true;
	}
	/**
	 * Czy operator
	 * @param s
	 * @return
	 */
	public static boolean isOperator(String s) {
		
		for (String str : operators)
			if (str.equalsIgnoreCase(s))
				return true;
	//	System.out.println("false");
		return false;
	}
	/**
	 * Czy liczba ca³kowita
	 * @param s
	 * @return
	 */
	public static boolean isInteger(String s) {
		try{
			Integer.parseInt(s);
			return true;
		}catch(Exception e) {
			return false;
		}
	}
	/**
	 * Czy liczba rzeczywista
	 * @param s
	 * @return
	 */
	public static boolean isReal(String s) {
		try{
			Float.parseFloat(s);
			return true;
		}catch(Exception e) {
			return false;
		}
	}
	
	/**
	 * Czy znak
	 */
	public static boolean isChar(String s) {
		if (s.length()!=3) return false;
		return (s.charAt(0)=='\'' && s.charAt(1)!='\'' && s.charAt(2)=='\'');
		
	}
	
	/**
	 * Czy ³añcuch znaków
	 */
	public static boolean isString(String s) {
		if (s.length()<3) return false;
		for (int i=1;i<s.length()-1;i++) 
			if (s.charAt(i)=='"')
				return false;
		return (s.charAt(0)=='\"' && s.charAt(s.length()-1)=='\"') ;
		
	}
	
	/**
	 * czy komentarz
	 */
	public static boolean isComment(String s) {
		//to-do
		return true;
	}
	/**
	 * Sprawdza czy znak mo¿e nale¿eæ do identyfikatora
	 * @param c
	 * @return
	 */
	public boolean isNextCharIdentifier(char c){
		String temp = String.valueOf(c);
		if (isOperator(temp)) return false;
		if (c=='\'' ||  c=='!' || c=='@' || c=='$' || c=='^' || c=='*' || c=='(' || c==')' || c=='~' || c=='#' || c=='%')
			return false;
		if (c=='{' || c=='}' || c=='"' || c=='.' || c==',' || c==':')
			return false;
		return true;
	}
	
	/**
	 * Skanuje jedn¹ liniê tekstu w poszukiwaniu lexemów
	 * @param line Linia wejœciowa
	 * @param a Lista lexemów
	 */
	public void scan(String line,ArrayList<String> lexems) throws Exception {
		if (line==null || line.length()==0) return;
		//rozbicie linie wyra¿enia miêdzy spacjami
		String[] str = line.split(" ");
		for (String s:str) {
			
			if (isKeyword(s) || isOperator(s) || isIdentifier(s) || isString(s) || isInteger(s) || isReal(s) || isChar(s)) {
				lexems.add(s);
				
			}  else {
				analize(s,lexems);
			}
		}
	}
	/**
	 * Analizuje fragment tekstu
	 * @param s
	 */
	public void analize(String s,ArrayList<String> a) throws Exception {
		//czy np var aa:string; - zostaje tylko aa:string - sekcja deklaracji
		
		if (s.indexOf(":")!=-1 && s.indexOf(":=")==-1) { //sekcja deklaracji
		 if (ileSrednikow(s)==1) { //tylko jeden œrednik 
			rozdzielNaDwukropki(s,a);
		
		} else { //dalej w sekcji deklaracji 
			//for (int i=0;i<ileSrednikow(s);i++)
			//	rozdzielNaDwukropki(s,a);
			String[] t = s.split(";");
			for (String x:t)
				rozdzielNaDwukropki(x,a);
		}
		return;
	 } 
		//instrukcja przypisania
		if (s.indexOf(":=")!=-1 && s.length()>2) {
			
			String[] t = s.split(":=");
			//lewa czêœæ wyra¿enia do której przypisujemy to t[0];
			//prawa czêœæ, czyli jakaœ wartoœæ to t[1];
			
			if (isIdentifier(t[0]) && t[0].length()>0) {
				a.add(t[0]);
				a.add(":=");
				String temp = "";
				int i=0;
				if (t.length>1) {
					System.out.println("analiza = "+t[1]);
				if (t[1].length()>0) {	
					while (i<t[1].length()) {
						char c = t[1].charAt(i);
						temp+=c;
						//System.out.println("temp = "+temp);

						
						if (isOperator(temp)) {
					
							if ((i+1)<t[1].length()) {
								if (isOperator(String.valueOf(t[1].charAt(i+1))) || isInteger(String.valueOf(t[1].charAt(i+1))) || isReal(String.valueOf(t[1].charAt(i+1))) || isIdentifier(String.valueOf(t[1].charAt(i+1)))) {
							//	if (isInteger(String.valueOf(t[1].charAt(i+1))) || isReal(String.valueOf(t[1].charAt(i+1))) || isIdentifier(String.valueOf(t[1].charAt(i+1)))) {
									a.add(temp);
									temp = "";
									i++;
									continue;
								} 
								
							}
						}
						
						if (isIdentifier(temp)) {
							if ((i+1)<t[1].length()) {
								if (!isIdentifier(temp+t[1].charAt(i+1))) {
									a.add(temp);
									temp = "";
									i++;
									continue;
								}
								
							}
						}
						
						if (isInteger(temp)) {
							if ((i+1)<t[1].length()) {
								if (!isInteger(String.valueOf(t[1].charAt(i+1)))) {
									a.add(temp);
									temp = "";
									i++;
									continue;
								}
								
							}
						}
						if (isReal(temp)) {
							if ((i+1)<t[1].length()) {
								if (!isReal(temp+t[1].charAt(i+1))) {
									a.add(temp);
									temp = "";
									i++;
									continue;
								}
								
							}
						}
						
						if (isString(temp)) {
							if ((i+1)<t[1].length()) {
								if (!isString(temp+t[1].charAt(i+1))) {
									a.add(temp);
									temp = "";
									i++;
									continue;
								}
								
							}
						}
						
						i++;
					}
					//System.out.println("na koniec temp = "+temp);
					if (isOperator(temp) || isInteger(temp) || isReal(temp) || isString(temp) || isIdentifier(temp) || temp.equals(";")) {
						a.add(temp);
					}
					
					//	a.add(";");
						
					
						
					
				}	
			 }		
			} else {
				throw new Exception("Wyra¿enie "+t[0]+" nie jest poprawnym identyfikatorem");
			
			}
			return;
		}
		// inne przypadki poza instrukcj¹ przypisania
		
		if (s.length()>=2) {
		/*	if (s.equals(");")) {
				a.add(")");
				a.add(";");
				return;
			} */
		//	System.out.println("analiza2 "+s);
			int i = 0;
			String temp="";
			
				while (i<s.length()) {
					char c = s.charAt(i);
					temp+=c;
				//	System.out.println("temp: "+temp);
					
			
					if (isOperator(temp)) {
						if ((i+1)<s.length()) {
							if ((isOperator(String.valueOf(s.charAt(i+1))) && !isOperator(temp+s.charAt(i+1))) || isInteger(String.valueOf(s.charAt(i+1))) || isReal(String.valueOf(s.charAt(i+1))) || isIdentifier(String.valueOf(s.charAt(i+1)))) {
								a.add(temp);
								temp = "";
								i++;
								continue;
							}
						}
					}
				
					if (isIdentifier(temp)) {
						if ((i+1)<s.length()) {
							if (isInteger(String.valueOf(s.charAt(i+1))) || !isNextCharIdentifier(s.charAt(i+1))) {
								a.add(temp);
								temp="";
								i++;
								continue;
							}
							
						}
					}
					if (isInteger(temp)) {
						if ((i+1)<s.length()) {
							if (!isInteger(String.valueOf(s.charAt(i+1)))) {
								a.add(temp);
								temp = "";
							}
							
						}
					}
					if (isReal(temp)) {
						if ((i+1)<s.length()) {
							if (!isReal(temp+s.charAt(i+1))) {
								a.add(temp);
								temp = "";
							}
							
						}
					}
					i++;
				}
				
				if (isOperator(temp) || isInteger(temp) || isReal(temp) || isString(temp) || isIdentifier(temp) || temp.equals(")")) {
					a.add(temp);
				}	
				if (temp.equals(";")) {
					a.add(temp);
					temp = "";
				}
		}
		 
		 
	 }
	
	private void rozdzielNaDwukropki(String s,ArrayList<String> a) throws Exception{
		String[] t = s.split(":");
		//sprawdzenie poprawnoœci zadeklarowanych typów danych
		if (t[1]!=null && t[1].length()>0) {
			if (t[1].indexOf(";")!=-1)
				t[1] = t[1].substring(0,t[1].indexOf(";"));
			if (!t[1].equalsIgnoreCase("real") && !t[1].equalsIgnoreCase("integer") && !t[1].equalsIgnoreCase("boolean") && !t[1].equalsIgnoreCase("string") && !t[1].equalsIgnoreCase("char")) 
				throw new Exception("Niepoprawny typ danych "+t[1]);
		}
		//czy zadeklarowano kilka zmiennych
		//np a,b:integer;
		if (s.indexOf(",")!=-1) {
			String[] vbs = t[0].split(",");
				for (String x:vbs)  {
					a.add(x);
					Variable v = new Variable();
					v.setName(x);
					if (t[1].indexOf(";")==-1) 
						v.setType(t[1]);
					else 
						v.setType(t[1].substring(0,t[1].indexOf(";")));
					parent.getVariables().put(x,v);
				}
			
			a.add(t[1]); //dodanie typu zmiennej
			a.add(";");
		} else { // jeœli nie ma przecinków - tylko jedn¹ zmienn¹ deklarujemy
			if (t.length>0)
			a.add(t[0]); //nazwa zmiennej
			if (t.length>1)
			a.add(t[1]); //typ zmiennej razem z œrednikiem
			a.add(";");
		}
		parent.setPosOfLastVariable(a.size()-1);
	}
	
	
	private int ileSrednikow(String s) {
		int result=0;
		for (char c: s.toCharArray()) {
			if (c==';') result++;
		}
		return result;
	}



}
