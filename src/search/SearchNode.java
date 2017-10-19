package edu.brown.cs.ssfix.search;

import org.eclipse.jdt.core.dom.ASTNode;

public class SearchNode
{
    ASTNode node;
    int prop;

    public SearchNode(ASTNode node, int prop) {
	this.node = node;
	this.prop = prop;
    }

    public int getProp() { return prop; }

    public ASTNode getASTNode() { return node; }
}
