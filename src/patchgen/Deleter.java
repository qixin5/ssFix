package edu.brown.cs.ssfix.patchgen;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class Deleter
{
    public Modification delete(ASTNode node, AST ast0) {
	ASTRewrite rw0 = ASTRewrite.create(ast0);
	rw0.remove(node, null);
	Modification mod0 = new Modification(rw0, "DELETE", ModificationSizeCalculator.calculate0(node, null, 0), ModificationSizeCalculator.calculate0(node, null, 1));
	return mod0;
    }
}
