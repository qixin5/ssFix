package edu.brown.cs.ssfix.search;

import org.apache.commons.io.FileUtils;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import org.eclipse.jdt.core.dom.*;
import edu.brown.cs.ssfix.util.*;

public class SearchMerger
{
    public static String merge(File rslt_f1, File rslt_f2) {
	List<SearchResultItem> rslt_items1 = getSearchResultItems(rslt_f1);
	List<SearchResultItem> rslt_items2 = getSearchResultItems(rslt_f2);
	normalize(rslt_items1, 0.6f);
	normalize(rslt_items2, 0.4f);

	//Used for ctxt look-up
	Map<String,Float> map2 = new HashMap<String,Float>();
	for (SearchResultItem rslt_item2 : rslt_items2) {
	    String path_loc_str = rslt_item2.getFilePath()+","+rslt_item2.getLoc();
	    float score = rslt_item2.getScore();
	    map2.put(path_loc_str, score);
	}
	    
	List<SearchResultItem> rslt_items = new ArrayList<SearchResultItem>();
	for (SearchResultItem rslt_item1 : rslt_items1) {
	    String mpathloc = getMethodPathLocString(rslt_item1.getFilePath(),rslt_item1.getLoc());
	    if (mpathloc != null) {
		Float mscore = map2.get(mpathloc);
		if (mscore != null) {
		    //Take the sum of the self-score and the method-score
		    rslt_item1.setScore(rslt_item1.getScore() + mscore.floatValue());
		}
	    }
	    rslt_items.add(rslt_item1);
	}
	for (SearchResultItem rslt_item2 : rslt_items2) {
	    rslt_items.add(rslt_item2);
	}
	Collections.sort(rslt_items, new Comparator<SearchResultItem>() {
		@Override public int compare(SearchResultItem r1, SearchResultItem r2) {
		    Float f1 = (Float) r1.getScore();
		    Float f2 = (Float) r2.getScore();
		    return f2.compareTo(f1);
		}
	    });
	StringBuilder sb = new StringBuilder();
	for (SearchResultItem rslt_item : rslt_items) {
	    sb.append(rslt_item.toString());
	    sb.append("\n");
	}
	return sb.toString();
    }

    private static List<SearchResultItem> getSearchResultItems(File rslt_f) {
	List<SearchResultItem> rslt_items = new ArrayList<SearchResultItem>();
	List<String> rslt_lines = null;
	try { rslt_lines = FileUtils.readLines(rslt_f, (String) null); }
	catch (IOException e) {
	    System.err.println(e);
	    e.printStackTrace();
	}
	if (rslt_lines == null) { return rslt_items; }
	for (String rslt_line : rslt_lines) {
	    int i1 = rslt_line.indexOf(",");
	    int i2 = rslt_line.lastIndexOf(",");
	    if (i1==-1 || i2==-1) { continue; }
	    String rslt_fpath = rslt_line.substring(0, i1);
	    String rslt_loc = rslt_line.substring(i1+1, i2);
	    String rslt_score_str = rslt_line.substring(i2+1);
	    if (rslt_fpath.startsWith("file://")) {
		rslt_fpath = rslt_fpath.substring(7);
	    }
	    File rslt_f0 = new File(rslt_fpath);
	    if (!rslt_f0.exists()) { continue; }
	    SearchResultItem rslt_item = new SearchResultItem(rslt_fpath, rslt_loc, Float.parseFloat(rslt_score_str));
	    rslt_items.add(rslt_item);
	}
	return rslt_items;
    }

    private static void normalize(List<SearchResultItem> rslt_items, float weight) {
	if (rslt_items.isEmpty()) { return; }
	float norm_factor = rslt_items.get(0).getScore();
	for (SearchResultItem rslt_item : rslt_items) {
	    rslt_item.setScore(rslt_item.getScore() / norm_factor * weight);
	}
    }

    private static String getMethodPathLocString(String fpath, String loc) {
	ASTNode cu_node = ASTNodeLoader.getASTNode(new File(fpath));
	if (cu_node == null) { return null; }
	CompilationUnit cu = (CompilationUnit) cu_node;
	List<ASTNode> found_node_list = ASTNodeFinder.find(cu, loc);
	if (!found_node_list.isEmpty()) {
	    ASTNode found_node0 = found_node_list.get(0);
	    if (found_node0 != null) {
		if (found_node0 instanceof MethodDeclaration) {
		    return fpath+","+loc;
		}
		else {
		    ASTNode parent0 = found_node0.getParent();
		    while (parent0 != null) {
			if (parent0 instanceof MethodDeclaration) { break; }
			else { parent0 = parent0.getParent(); }
		    }
		    if (parent0 != null) {
			int parent0_start_pos = parent0.getStartPosition();
			int sln = cu.getLineNumber(parent0_start_pos);
			int scn = cu.getColumnNumber(parent0_start_pos);
			return fpath+",slc:"+sln+","+scn;
		    }
		}
	    }
	}
	return null;
    }
}
