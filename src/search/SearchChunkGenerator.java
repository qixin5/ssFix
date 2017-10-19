package edu.brown.cs.ssfix.search;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import edu.brown.cs.ssfix.util.*;



public class SearchChunkGenerator
{
    private static final int DEFAULT_LOCAL_LINES = 6;
    private static final int DEFAULT_REGIONAL_LINES = 12;
    
    public SearchChunk getBugOnlyChunk(ASTNode buggy_node) {
	CompilationUnit cu = (CompilationUnit) buggy_node.getRoot();
	SearchChunk bug_only_chunk = new SearchChunk();
	bug_only_chunk.addNode(buggy_node, 0);
	return bug_only_chunk;
    }

    public SearchChunk getLocalContextChunk(ASTNode buggy_node) {
	return getLocalContextChunk(buggy_node, DEFAULT_LOCAL_LINES);
    }
    
    public SearchChunk getLocalContextChunk(ASTNode buggy_node, int max_lines) {
	CompilationUnit cu = (CompilationUnit) buggy_node.getRoot();
	SearchChunk ctxt_chunk = new SearchChunk();
	List<ASTNode> ctxt_nodes = getLocalContextNodes(cu, buggy_node, max_lines);
	for (ASTNode ctxt_node : ctxt_nodes) {
	    if (ctxt_node == buggy_node) {
		ctxt_chunk.addNode(ctxt_node, 0);//0: Bug-only search property
	    }
	    else {
		ctxt_chunk.addNode(ctxt_node, 1);//1: Local search property
	    }
	}
	return ctxt_chunk;
    }
    
    private List<ASTNode> getLocalContextNodes(CompilationUnit cu, ASTNode node, int max_lines) {
	List<ASTNode> node_list = new ArrayList<ASTNode>();

	int curr_lines = getLengthInLines(cu, node);
	if (curr_lines >= max_lines) {
	    node_list.add(node); //Only the buggy statement
	    return node_list;
	}
	else {
	    ASTNode par = node.getParent();
	    while (par != null) {
		if (par instanceof Block) { par = par.getParent(); }
		else { break; }
	    }
	    if ((par != null) && (par instanceof Statement)) {
		if (getLengthInLines(cu, par) <= max_lines) {
		    node_list.add(par); //Only the parent
		    return node_list;
		}
	    }

	    //We reach here because either 1) the parent is not stmt any more,
	    //e.g., method declaration or 2) the parent exceeds the maximum LOC.
	    //So, we try to include the node's adjacent stmts.
	    node_list.add(node);
	    StructuralPropertyDescriptor spd = node.getLocationInParent();
	    if (spd == null) { return node_list; }
	    if (spd.isChildListProperty()) { //we have siblings
		List siblings = (List) node.getParent().getStructuralProperty(spd);
		int siblings_size = siblings.size();
		int node_index = -1;
		for (int i=0; i<siblings_size; i++) {
		    if (((ASTNode) siblings.get(i)) == node) {
			node_index = i;
			break;
		    }
		}
		if (node_index != -1) {
		    ASTNode stmt_before = (node_index-1>=0) ? ((ASTNode) siblings.get(node_index-1)) : null;
		    ASTNode stmt_after = (node_index+1<siblings_size) ? ((ASTNode) siblings.get(node_index+1)) : null;
		    if (stmt_before != null) {
			int stmt_before_lines = getLengthInLines(cu, stmt_before);
			if (curr_lines + stmt_before_lines <= max_lines) {
			    curr_lines += stmt_before_lines;
			    node_list.add(0, stmt_before);
			}
		    }
		    if (stmt_after != null) {
			int stmt_after_lines = getLengthInLines(cu, stmt_after);
			if (curr_lines + stmt_after_lines <= max_lines) {
			    curr_lines += stmt_after_lines;
			    node_list.add(stmt_after);
			}
		    }
		    return node_list;
		}
		else {
		    //node_index is -1, this shouldn't happen
		    return node_list;
		}
	    }
	    else {
		return node_list;
	    }
	}
    }

    private int getLengthInLines(CompilationUnit cu, ASTNode node) {
	int start_pos = node.getStartPosition();
	int end_pos = start_pos + node.getLength();
	return cu.getLineNumber(end_pos) - cu.getLineNumber(start_pos) + 1;
    }

    private ASTNode getParentMethod(ASTNode node) {
	ASTNode curr = node;
	while (curr != null) {
	    if (curr instanceof MethodDeclaration) { break; }
	    curr = curr.getParent();
	}
	return curr;
    }
}
