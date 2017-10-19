package edu.brown.cs.ssfix.patchgen;

import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;
import org.apache.lucene.search.spell.LevensteinDistance;
import edu.brown.cs.ssfix.util.*;


public class ModificationSizeCalculator
{
    public static final int HEIGHT_SIZE = 0;
    public static final int STRING_SIMILARITY = 1;
    public static LevensteinDistance ld;
    static { ld = new LevensteinDistance(); }

    public static float calculate0(ASTNode node1, ASTNode node2, int size_type) {
	if (node1 == null && node2 == null) {
	    return (size_type == HEIGHT_SIZE) ? 0 : 1;
	}
	else if (node1 == null && node2 != null) {
	    if (size_type == HEIGHT_SIZE) {
		return ASTNodeHeightCalculator.calculate(node2);
	    }
	    else {
		return calculateStringSimilarity("", node2.toString());
	    }
	}
	else if (node1 != null && node2 == null) {
	    if (size_type == HEIGHT_SIZE) {
		return ASTNodeHeightCalculator.calculate(node1);
	    }
	    else {
		return calculateStringSimilarity(node1.toString(), "");
	    }
	}
	else {
	    if (size_type == HEIGHT_SIZE) {
		float s1 = ASTNodeHeightCalculator.calculate(node1);
		float s2 = ASTNodeHeightCalculator.calculate(node2);
		return (s1 <= s2) ? s2 : s1;
	    }
	    else {
		return calculateStringSimilarity(node1.toString(), node2.toString());
	    }
	}
    }

    public static float calculate1(Object obj1, Object obj2, int size_type) {
	if (obj1 == null && obj2 == null) {
	    return (size_type == HEIGHT_SIZE) ? 0 : 1;
	}
	else if (obj1 == null && obj2 != null) {
	    return (size_type == HEIGHT_SIZE) ? calculateHeightSize(obj2) : calculateStringSimilarity("", obj2.toString());
	}
	else if (obj1 != null && obj2 == null) {
	    return (size_type == HEIGHT_SIZE) ? calculateHeightSize(obj1) : calculateStringSimilarity(obj1.toString(), "");
	}
	else {
	    if (size_type == HEIGHT_SIZE) {
		float s1 = calculateHeightSize(obj1);
		float s2 = calculateHeightSize(obj2);
		return (s1 <= s2) ? s2 : s1;
	    }
	    else {
		return calculateStringSimilarity(obj1.toString(), obj2.toString());
	    }
	}
    }

    public static float calculate2(List list1, List list2, int size_type) {
	if (list1 == null && list2 == null) {
	    return 0;
	}
	else if (list1 == null && list2 != null) {
	    if (size_type == HEIGHT_SIZE) {
		return calculateHeightSize(list2);
	    }
	    else {
		return calculateStringSimilarity(list1, list2);
	    }
	}
	else if (list1 != null && list2 == null) {
	    if (size_type == HEIGHT_SIZE) {
		return calculateHeightSize(list1);
	    }
	    else {
		return calculateStringSimilarity(list1, list2);
	    }
	}
	else {
	    if (size_type == HEIGHT_SIZE) {
		float s1 = calculateHeightSize(list1);
		float s2 = calculateHeightSize(list2);
		return (s1 <= s2) ? s2 : s1;
	    }
	    else {
		return calculateStringSimilarity(list1, list2);
	    }

	}
    }

    private static float calculateStringSimilarity(String s0, String s1) {
	String x = (s0 == null) ? "" : s0;
	String y = (s1 == null) ? "" : s1;
	x = x.replaceAll("\\s+","");
	y = y.replaceAll("\\s+","");
	return ld.getDistance(x, y);
    }

    private static float calculateHeightSize(Object obj) {
	if (obj instanceof ASTNode) {
	    return ASTNodeHeightCalculator.calculate((ASTNode) obj);
	}
	else {
	    return 1;
	}
    }
    
    private static float calculateHeightSize(List list) {
	if (list == null) { return 0; }
	int height_size = 0;
	for (Object obj : list) {
	    height_size += calculateHeightSize(obj);
	}
	return height_size;
    }

    private static float calculateStringSimilarity(List list1, List list2) {
	String s1 = "", s2 = "";
	if (list1 != null) {
	    for (Object obj1 : list1) {
		if (obj1 != null) {
		    if (obj1 instanceof ASTNode) {
			s1 += ((ASTNode) obj1).toString();
		    }
		    else {
			s1 += obj1.toString();
		    }
		}
	    }
	}
	if (list2 != null) {
	    for (Object obj2 : list2) {
		if (obj2 != null) {
		    if (obj2 instanceof ASTNode) {
			s2 += ((ASTNode) obj2).toString();
		    }
		    else {
			s2 += obj2.toString();
		    }
		}
	    }
	}
	return calculateStringSimilarity(s1, s2);
    }
}
