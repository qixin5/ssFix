package edu.brown.cs.ssfix.patchgen;

import edu.brown.cs.ivy.jcomp.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.*;
import org.eclipse.jface.text.*;
import ccmatcher.CCMaps;



public class Modifier
{
    private Replacer replacer;
    private Inserter inserter;
    private Deleter  deleter;

    public Modifier() {
	replacer = new Replacer();
	inserter = new Inserter();
	deleter = new Deleter();
    }
    
    /* All Nodes Need to be NORMALIZED! */
    public List<Modification> modify(List<ASTNode> node_list0, List<ASTNode> node_list1, CCMaps ccmaps, AST ast0) {

	List<Modification> rslt_list = new ArrayList<Modification>();
	Map<ASTNode,ASTNode> map0 = ccmaps.getMap1();
	Map<ASTNode,ASTNode> map1 = ccmaps.getMap2();
	Set<ASTNode> node_set0 = map0.keySet();
	Set<ASTNode> node_set1 = map1.keySet();

	/*
	//All-match Replacement
	ASTRewrite rw = ASTRewrite.create(ast0);
	boolean chunk_replaced = false;
	for (ASTNode node0 : node_list0) {
	    ASTNode node0_match = map0.get(node0);
	    if (node0_match != null && !replacer.isEquivalent(node0, node0_match)) {
		ASTNode node1 = ASTNode.copySubtree(ast0, node0_match);
		rw.replace(node0, node1, null);
		chunk_replaced = true;
	    }
	}
	if (chunk_replaced) { rslt_list.add(rw); }
	*/
	

	//Component Replacement
	for (ASTNode node0 : node_set0) {
	    ASTNode node1 = ASTNode.copySubtree(ast0, map0.get(node0));
	    List<Modification> rw_list0 = replacer.replaceWith(node0, node1, ast0);
	    for (Modification rw0 : rw_list0) {
		rslt_list.add(rw0);
	    }
	}

	//Insertion
	List<ASTNode> stmt_node_list1 = getAllNestedStatementsWithoutBlocks(node_list1);
	for (ASTNode stmt_node1 : stmt_node_list1) {
	    if (!node_set1.contains(stmt_node1)) {
		List<ASTNode> stmt_node_list1a = getAllNestedStatementsWithoutBlocks(stmt_node1);
		boolean matched0 = false; //Whether stmt_node1 is fully unmatched
		for (ASTNode stmt_node1a : stmt_node_list1a) {
		    if (node_set1.contains(stmt_node1a)) {
			matched0 = true;
			break;
		    }
		}
		if (!matched0) {
		    //ASTNode stmt_node1cp = ASTNode.copySubtree(ast0, stmt_node1);
		    List<Modification> rw_list0 = inserter.insert(stmt_node1, map1, ast0);
		    for (Modification rw0 : rw_list0) {
			rslt_list.add(rw0);
		    }
		}
	    }
	}
	
	//Deletion
	List<ASTNode> stmt_node_list0 = getAllNestedStatementsWithoutBlocks(node_list0);
	for (ASTNode stmt_node0 : stmt_node_list0) {	    
	    if (!node_set0.contains(stmt_node0)) {
		List<ASTNode> stmt_node_list0a = getAllNestedStatementsWithoutBlocks(stmt_node0);
		boolean matched0 = false; //Whether stmt_node0 is fully unmatched
		for (ASTNode stmt_node0a : stmt_node_list0a) {
		    if (node_set0.contains(stmt_node0a)) {
			matched0 = true;
			break;
		    }
		}

		if (!matched0) {
		    Modification rw0 = deleter.delete(stmt_node0, ast0);
		    rslt_list.add(rw0);
		}
	    }
	}

	return rslt_list;
    }

    private List<ASTNode> getAllNestedStatementsWithoutBlocks(ASTNode node) {
	StmtNodeVisitor visitor = new StmtNodeVisitor();
	node.accept(visitor);
	return visitor.getStmtNodeList();
    }

    private List<ASTNode> getAllNestedStatementsWithoutBlocks(List<ASTNode> node_list) {
	List<ASTNode> rslt_list = new ArrayList<ASTNode>();
	for (ASTNode node : node_list) {
	    StmtNodeVisitor visitor = new StmtNodeVisitor();
	    node.accept(visitor);
	    List<ASTNode> node_list0 = visitor.getStmtNodeList();
	    for (ASTNode node0 : node_list0) {
		rslt_list.add(node0);
	    }
	}
	return rslt_list;
    }
    
    /*
      Wrap any body statement as a list of statement.

      Without normalization, the body statement of a compound statement could be
      a non-block statement. In that case, what is returned is a list containing
      only that statement. Otherwise, the return is the block's statement list.
     */
    /*    
    private List getStatementList(Statement stmt) {

	if (stmt instanceof Block) {
	    return ((Block) stmt).statements();
	} else {
	    List stmt_list = new ArrayList();
	    stmt_list.add(stmt);
	    return stmt_list;
	}
    }
    */  
    /*
    private boolean resolved() {

	//Create a new JrepSource object whose file content is "tcu"'s text
	JrepSource patch_jrs = 
	    new JrepSource(tjrs.getFilePath(), tjrs.getFileName(), tcu.toString());

	//Call Jcomp for Resolving
	JcompControl jcc = new JcompControl();
	Collection<JcompSource> jcs_set = new ArrayList<JcompSource>();
	jcs_set.add(patch_jrs);
        JcompProject jcp = jcc.getSemanticData(src_jar_path, jcs_set);
	jcp.resolve();
        List<JcompMessage> err_msg_list = jcp.getMessages();
        if (err_msg_list == null || err_msg_list.isEmpty()) { 
	    return true; 
	} else {
            if (print_resolve_errors) { 
		System.err.println("=======================");
		System.err.println("Resolve Error Messages:");
		for (JcompMessage jc_msg : err_msg_list) {
		    System.err.println("File: " + jc_msg.getSource().getFileName()
				       + ", Line: " + jc_msg.getLineNumber());
		    System.err.println(jc_msg.getText());
		}
		System.err.println("=======================");
	    }
            return false;
	}
    }
	*/

    private class StmtNodeVisitor extends ASTVisitor
    {
	List<ASTNode> rslt_list;
	public StmtNodeVisitor() {
	    rslt_list = new ArrayList<ASTNode>();
	}
	public List<ASTNode> getStmtNodeList() {
	    return rslt_list;
	}
	@Override public boolean preVisit2(ASTNode node) {
	    if (node instanceof Statement) {
		if (node instanceof Block) {}
		else { rslt_list.add(node); }
		return true;
	    }
	    else if (node instanceof Expression) {
		return false;
	    }
	    else {
		return true;
	    }
	}
    }

}
