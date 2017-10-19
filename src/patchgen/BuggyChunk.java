package edu.brown.cs.ssfix.patchgen;

import java.util.List;
import java.util.ArrayList;
import org.eclipse.jdt.core.dom.ASTNode;

public class BuggyChunk extends Chunk
{
    public BuggyChunk(String fpath, String loc, List<ASTNode> node_list, String fctnt) {
	super(fpath, loc, node_list, fctnt);
    }
}
