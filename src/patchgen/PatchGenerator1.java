package patchgen;

import java.util.List;
import java.util.ArrayList;
import ccmatcher.MatchCodeComponents;
import ccmatcher.CCMaps;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AST;


public class PatchGenerator1
{
    public List<Modification> generatePatches(BuggyChunk bchunk, CandidateChunk rcchunk) {
	return generatePatches(bchunk, rcchunk, false, false);
    }
    
    public List<Modification> generatePatches(BuggyChunk bchunk, CandidateChunk rcchunk, boolean print_ccmatching, boolean print_patches) {

	if (!bchunk.isNormalized()) { bchunk.normalize(); }
	if (!rcchunk.isNormalized()) { rcchunk.normalize(); }
	
	List<ASTNode> node_list0 = bchunk.getNodeList();
	List<ASTNode> node_list1 = rcchunk.getNodeList();

	if (node_list0.isEmpty()) { return new ArrayList<Modification>(); }
	AST ast0 = node_list0.get(0).getAST();
	MatchCodeComponents ccmatcher = new MatchCodeComponents();
	CCMaps ccmaps = ccmatcher.match(node_list0, node_list1);
	if (print_ccmatching) { System.out.println(ccmaps); }
	
	Modifier modifier = new Modifier();
	List<Modification> patch_rws = modifier.modify(node_list0, node_list1, ccmaps, ast0);
	if (print_patches) {
	    int patch_rws_size = patch_rws.size();
	    for (int i=0; i<patch_rws_size; i++) {
		System.out.println("------ Patch " + i + " ------");
		System.out.println(patch_rws.get(i));
	    }
	}

	return patch_rws; //to be validated
    }
}
