package edu.brown.cs.ssfix.patchgen;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;


public class Inserter
{
    /* stmt_node1 should be fully unmatched and should not be a copy node. */
    public List<Modification> insert(ASTNode stmt_node1, Map<ASTNode, ASTNode> cmap, AST ast0) {
	List<Modification> final_mod_list = new ArrayList<Modification>();
	Statement cstmt = (Statement) stmt_node1;

	//Find the bounding statements
	//(closest to ctmt and have matching stmts in the target chunk)
	Statement cstmt_lb = null, cstmt_hb = null;
	ASTNode cstmt_parent = cstmt.getParent();
	StructuralPropertyDescriptor cstmt_loc = cstmt.getLocationInParent();
	//=========
	//System.err.println(cstmt);
	//System.err.println(cstmt.getParent());
	//=========
	if (cstmt_loc.isChildListProperty()) {
	    List parent_list = (List) cstmt_parent.getStructuralProperty(cstmt_loc);
	    int parent_list_size = parent_list.size();
	    int cstmt_index = parent_list.indexOf(cstmt);
	    if (cstmt_index == -1) { return final_mod_list; }

	    //Find the low-bounding statement
	    for (int j=cstmt_index-1; j>=0; j--) {
		Object node_obj = parent_list.get(j);
		if (cmap.get(node_obj) != null) {
		    //This means having a match in the target chunk
		    cstmt_lb = (Statement) node_obj;
		    break;
		}
	    }

	    //Find the high-bounding statement
	    for (int j=cstmt_index+1; j<parent_list_size; j++) {
		Object node_obj = parent_list.get(j);
		if (cmap.get(node_obj) != null) {
		    //This means having a match in the target chunk
		    cstmt_hb = (Statement) node_obj;
		    break;
		}
	    }
	}

	boolean inserted = false;
	//If tstmt_lb & tstmt_hb are from the same list,
	//only insert cstmt into any places between them.
	if (cstmt_lb != null && cstmt_hb != null) {
	    ASTNode tstmt_lb = cmap.get(cstmt_lb);
	    ASTNode tstmt_hb = cmap.get(cstmt_hb);
	    StructuralPropertyDescriptor tstmt_lb_loc=tstmt_lb.getLocationInParent();
	    StructuralPropertyDescriptor tstmt_hb_loc=tstmt_hb.getLocationInParent();
	    
	    if (tstmt_lb_loc.isChildListProperty() &&
		tstmt_hb_loc.isChildListProperty()) {
		ASTNode tstmt_lb_parent = tstmt_lb.getParent();
		ASTNode tstmt_hb_parent = tstmt_hb.getParent();
		List plist1 = (List) tstmt_lb_parent.getStructuralProperty(tstmt_lb_loc);
		List plist2 = (List) tstmt_hb_parent.getStructuralProperty(tstmt_hb_loc);
		//Make sure they are from the same list
		if (plist1 == plist2) {
		    int tstmt_lb_index = plist1.indexOf(tstmt_lb);
		    int tstmt_hb_index = plist1.indexOf(tstmt_hb);
		    if (tstmt_lb_index!=-1 && tstmt_hb_index!=-1) {
			int low_index = (tstmt_lb_index<=tstmt_hb_index) ? tstmt_lb_index : tstmt_hb_index;
			int high_index = (tstmt_lb_index<=tstmt_hb_index) ? tstmt_hb_index : tstmt_lb_index;
			for (int j=low_index; j<high_index; j++) {
			    ASTNode tnode = (ASTNode) plist1.get(j);
			    try {
				ASTRewrite rw = ASTRewrite.create(ast0);
				ListRewrite lrw = rw.getListRewrite((ASTNode)tstmt_lb_parent,(ChildListPropertyDescriptor)tstmt_lb_loc);
				Statement tstmt_insert_copy = (Statement)(ASTNode.copySubtree(ast0, cstmt));
				lrw.insertAfter(tstmt_insert_copy, tnode, null);
				Modification mod = new Modification(rw, "INSERT", ModificationSizeCalculator.calculate0(null, tstmt_insert_copy, 0), ModificationSizeCalculator.calculate0(null, tstmt_insert_copy, 1));
				final_mod_list.add(mod);
			    } catch (Exception e) {
				System.err.println("Insert After Error For Node:");
				System.err.println(tnode);
				e.printStackTrace();
			    }
			}
			inserted = true;
		    }
		}
	    }
	}
	
	    
	if (!inserted) {
	    //Insert after the matched low-bound statement in the target
	    //but only within that target statement's list.
	    if (cstmt_lb != null) {
		ASTNode tstmt_lb = cmap.get(cstmt_lb);
		ASTNode tstmt_lb_parent = tstmt_lb.getParent();
		StructuralPropertyDescriptor tstmt_lb_loc = tstmt_lb.getLocationInParent();
		if (tstmt_lb_loc.isChildListProperty()) {
		    List plist = (List) tstmt_lb_parent.getStructuralProperty(tstmt_lb_loc);
		    int plist_size = plist.size();
		    int tstmt_lb_index = plist.indexOf(tstmt_lb);
		    if (tstmt_lb_index != -1) {
			for (int j=tstmt_lb_index; j<plist_size; j++) {
			    ASTNode node = (ASTNode) plist.get(j);
			    try {
			    //If the chunk is not compact, meaning two adjacent
			    //statements in the chunk are not real adjacent statements,
			    //then we may need to check if *node* actually comes from
			    //the chunk.
			    ASTRewrite rw = ASTRewrite.create(ast0);
			    ListRewrite lrw = rw.getListRewrite((ASTNode) tstmt_lb_parent, (ChildListPropertyDescriptor) tstmt_lb_loc);
			    Statement tstmt_insert_copy =
				(Statement) (ASTNode.copySubtree(ast0, cstmt));
			    lrw.insertAfter(tstmt_insert_copy, node, null);
			    Modification mod = new Modification(rw, "INSERT", ModificationSizeCalculator.calculate0(null, tstmt_insert_copy, 0), ModificationSizeCalculator.calculate0(null, tstmt_insert_copy, 1));
			    final_mod_list.add(mod);
			    } catch (Exception e) {
				System.err.println("Insert After Error For Node:");
				System.err.println(node);
				e.printStackTrace();
			    }
			}
		    }
		}
	    }


	    //Insert before the high-bounding statement in the target
	    //but only within that statement's list.
	    if (cstmt_hb != null) {
		ASTNode tstmt_hb = cmap.get(cstmt_hb);
		ASTNode tstmt_hb_parent = tstmt_hb.getParent();
		StructuralPropertyDescriptor tstmt_hb_loc = tstmt_hb.getLocationInParent();
		if (tstmt_hb_loc.isChildListProperty()) {
		    List plist = (List) tstmt_hb_parent.getStructuralProperty(tstmt_hb_loc);
		    int tstmt_hb_index = plist.indexOf(tstmt_hb);
		    if (tstmt_hb_index != -1) {
			for (int j=tstmt_hb_index; j>=0; j--) {
			    ASTNode node = (ASTNode) plist.get(j);
			    try {
			    //Do we check whether node_obj is from the chunk?
			    ASTRewrite rw = ASTRewrite.create(ast0);
			    ListRewrite lrw = rw.getListRewrite((ASTNode) tstmt_hb_parent, (ChildListPropertyDescriptor) tstmt_hb_loc);
			    Statement tstmt_insert_copy =
				(Statement) (ASTNode.copySubtree(ast0, cstmt));
			    lrw.insertBefore(tstmt_insert_copy, node, null);
			    Modification mod = new Modification(rw, "INSERT", ModificationSizeCalculator.calculate0(null, tstmt_insert_copy, 0), ModificationSizeCalculator.calculate0(null, tstmt_insert_copy, 1));
			    final_mod_list.add(mod);
			    } catch (Exception e) {
				System.err.println("Insert Before Error For Node:");
				System.err.println(node);
				e.printStackTrace();
			    }
			}
		    }
		}
	    }
	    
	}  //End of Candidate Chunk's Iteration
	
	return final_mod_list;	
    }
	

    private int indexOf(List list, Object target) {
	int index = -1;
	int size = list.size();
	for (int i=0; i<size; i++) {
	    if (target == list.get(i)) {
		index = i;
		break;
	    }
	}
	return index;
    } 
}
