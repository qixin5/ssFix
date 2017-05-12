package search;

import java.util.List;
import java.util.ArrayList;
import org.eclipse.jdt.core.dom.ASTNode;


public class SearchChunk
{
    List<SearchNode> node_list;

    public SearchChunk() {
	node_list = new ArrayList<SearchNode>();
    }

    public void addNode(ASTNode node, int prop) {
	node_list.add(new SearchNode(node, prop));
    }
    
    public void addNode(ASTNode node) {
	addNode(node, -1);
    }

    public List<SearchNode> getNodeList() {
	return node_list;
    }
}
