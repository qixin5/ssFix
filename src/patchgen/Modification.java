package edu.brown.cs.ssfix.patchgen;

import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class Modification implements Comparable<Modification>
{
    ASTRewrite rw;
    String type;
    float height_size;
    float str_sim;


    public Modification(ASTRewrite rw, String type, float height_size, float str_sim) {
	this.rw = rw;
	this.type = type;
	this.height_size = height_size;	
	this.str_sim = str_sim;

    }

    public ASTRewrite getASTRewrite() {
	return rw;
    }

    public String getType() {
	return type;
    }

    public void setStringSimilarity(float str_sim) {
	this.str_sim = str_sim;
    }
    
    public float getStringSimilarity() {
	return str_sim;
    }

    public void setHeightSize(float height_size) {
	this.height_size = height_size;
    }
    
    public float getHeightSize() {
	return height_size;
    }

    public int compareTo(Modification m2) {
	if (m2 == null) { return -1; }
	String type2 = m2.getType();

	//One is DELETE, the other is not.
	if ("DELETE".equals(type2)) {
	    if (!("DELETE".equals(type))) {
		return -1;
	    }
	} else {
	    if ("DELETE".equals(type)) {
		return 1;
	    }
	}

	//Either both are DETELEs or not DELETEs.
	float height_size2 = m2.getHeightSize();
	if (height_size < height_size2) {
	    return -1;
	}
	else if (height_size > height_size2) {
	    return 1;
	}
	else {
	    //return char_size - m2.getCharacterSize();
	    float str_sim2 = m2.getStringSimilarity();
	    if (str_sim2 > str_sim) { return 1; }
	    else if (str_sim2 < str_sim) { return -1; }
	    else { return 0; }
	}
    }

    public String toString() {
	String s = "";
	s += "Modification Type: " + type + "\n";
	s += "Modification Height Size: " + height_size + "\n";
	s += "Modification Similarity: " + str_sim + "\n";
	s += "ASTRewrite: " + "\n";
	s += rw.toString();
	return s;
    }
}
