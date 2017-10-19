package edu.brown.cs.ssfix.patchgen;

import java.util.List;
import java.util.ArrayList;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.text.edits.*;
import org.eclipse.jface.text.*;

class Replacer
{
    /* NOTE: node1 & node2 should both be NORMALIZED (block inserted for a non-block body). NOTE: node2 MUST be a node copy (as an un-rooted node). */
    public List<Modification> replaceWith(ASTNode node1, ASTNode node2, AST ast1) {
	//node1 = getCoreASTNode(node1);
	//node2 = getCoreASTNode(node2);
	List<Modification> rw_list = new ArrayList<Modification>();
	if (isEquivalent(node1, node2)) { return rw_list; }

	ASTRewrite rw0 = ASTRewrite.create(ast1);
	rw0.replace(node1, node2, null);
	Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(node1, node2, 0), ModificationSizeCalculator.calculate0(node1, node2, 1));
	rw_list.add(mod0);

	int node_type1 = node1.getNodeType();
	int node_type2 = node2.getNodeType();
	if (node_type1 == node_type2) {
	    if (node_type1 == ASTNode.CATCH_CLAUSE) {
		List<Modification> rw_list_tmp = replaceWith((CatchClause) node1, (CatchClause) node2, ast1);
		for (Modification rw_tmp : rw_list_tmp) { rw_list.add(rw_tmp); }
	    }
	    else if (node_type1 == ASTNode.CONDITIONAL_EXPRESSION) {
		List<Modification> rw_list_tmp = replaceWith((ConditionalExpression) node1, (ConditionalExpression) node2, ast1);
		for (Modification rw_tmp : rw_list_tmp) { rw_list.add(rw_tmp); }
	    }
	    else if (node_type1 == ASTNode.CONSTRUCTOR_INVOCATION) {
		List<Modification> rw_list_tmp = replaceWith((ConstructorInvocation) node1, (ConstructorInvocation) node2, ast1);
		for (Modification rw_tmp : rw_list_tmp) { rw_list.add(rw_tmp); }
	    }
	    else if (node_type1 == ASTNode.ENHANCED_FOR_STATEMENT) {
		List<Modification> rw_list_tmp = replaceLoopWith(node1, node2, ast1);
		for (Modification rw_tmp : rw_list_tmp) { rw_list.add(rw_tmp); }
	    }
	    else if (node_type1 == ASTNode.EXPRESSION_STATEMENT) {
		//Shouldn't happen
	    }
	    else if (node_type1 == ASTNode.RETURN_STATEMENT) {
		List<Modification> rw_list_tmp = replaceWith((ReturnStatement) node1, (ReturnStatement) node2, ast1);
		for (Modification rw_tmp : rw_list_tmp) { rw_list.add(rw_tmp); }
	    }
	    else if (node_type1 == ASTNode.SUPER_CONSTRUCTOR_INVOCATION) {
		List<Modification> rw_list_tmp = replaceWith((SuperConstructorInvocation) node1, (SuperConstructorInvocation) node2, ast1);
		for (Modification rw_tmp : rw_list_tmp) { rw_list.add(rw_tmp); }
	    }
	    else if (node_type1 == ASTNode.SWITCH_CASE) {
		//Do nothing.
	    }
	    else if (node_type1 == ASTNode.THROW_STATEMENT) {
		//Do nothing.
	    }
	    else if (node_type1 == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
		//Do nothing.
	    }
	    else if (node_type1 == ASTNode.ASSIGNMENT) {
		List<Modification> rw_list_tmp = replaceWith((Assignment) node1, (Assignment) node2, ast1);
		for (Modification rw_tmp : rw_list_tmp) { rw_list.add(rw_tmp); }
	    }
	    else if (node_type1 == ASTNode.INFIX_EXPRESSION) {
		List<Modification> rw_list_tmp = replaceWith((InfixExpression) node1, (InfixExpression) node2, ast1);
		for (Modification rw_tmp : rw_list_tmp) { rw_list.add(rw_tmp); }
	    }
	    else if (node_type1 == ASTNode.METHOD_INVOCATION) {
		List<Modification> rw_list_tmp = replaceWith((MethodInvocation) node1, (MethodInvocation) node2, ast1);
		for (Modification rw_tmp : rw_list_tmp) { rw_list.add(rw_tmp); }
	    }
	    else if (node_type1 == ASTNode.SUPER_METHOD_INVOCATION) {
		List<Modification> rw_list_tmp = replaceWith((SuperMethodInvocation) node1, (SuperMethodInvocation) node2, ast1);
		for (Modification rw_tmp : rw_list_tmp) { rw_list.add(rw_tmp); }
	    }
	    else if (node_type1 == ASTNode.POSTFIX_EXPRESSION) {
		List<Modification> rw_list_tmp = replaceWith((PostfixExpression) node1, (PostfixExpression) node2, ast1);
		for (Modification rw_tmp : rw_list_tmp) { rw_list.add(rw_tmp); }
	    }
	    else if (node_type1 == ASTNode.PREFIX_EXPRESSION) {
		List<Modification> rw_list_tmp = replaceWith((PrefixExpression) node1, (PrefixExpression) node2, ast1);
		for (Modification rw_tmp : rw_list_tmp) { rw_list.add(rw_tmp); }
	    }
	    else if (node_type1 == ASTNode.DO_STATEMENT) {
		List<Modification> rw_list_tmp = replaceLoopWith(node1, node2, ast1);
		for (Modification rw_tmp : rw_list_tmp) { rw_list.add(rw_tmp); }
	    }
	    else if (node_type1 == ASTNode.FOR_STATEMENT) {
		List<Modification> rw_list_tmp = replaceWith((ForStatement) node1, (ForStatement) node2, ast1);
		for (Modification rw_tmp : rw_list_tmp) { rw_list.add(rw_tmp); }
	    }
	    else if (node_type1 == ASTNode.IF_STATEMENT) {
		List<Modification> rw_list_tmp = replaceWith((IfStatement) node1, (IfStatement) node2, ast1);
		for (Modification rw_tmp : rw_list_tmp) { rw_list.add(rw_tmp); }
	    }
	    else if (node_type1 == ASTNode.SWITCH_STATEMENT) {
		List<Modification> rw_list_tmp = replaceWith((SwitchStatement) node1, (SwitchStatement) node2, ast1);
		for (Modification rw_tmp : rw_list_tmp) { rw_list.add(rw_tmp); }
	    }
	    else if (node_type1 == ASTNode.SYNCHRONIZED_STATEMENT) {
		List<Modification> rw_list_tmp = replaceWith((SynchronizedStatement) node1, (SynchronizedStatement) node2, ast1);
		for (Modification rw_tmp : rw_list_tmp) { rw_list.add(rw_tmp); }
	    }
	    else if (node_type1 == ASTNode.TRY_STATEMENT) {
		List<Modification> rw_list_tmp = replaceWith((TryStatement) node1, (TryStatement) node2, ast1);
		for (Modification rw_tmp : rw_list_tmp) { rw_list.add(rw_tmp); }
	    }
	    else if (node_type1 == ASTNode.WHILE_STATEMENT) {
		List<Modification> rw_list_tmp = replaceLoopWith(node1, node2, ast1);
		for (Modification rw_tmp : rw_list_tmp) { rw_list.add(rw_tmp); }
	    }
	}
	else {
	    //node types differ.
	    if (isLoopNode(node1) && isLoopNode(node2)) {
		List<Modification> rw_list_tmp = replaceLoopWith(node1, node2, ast1);
		for (Modification rw_tmp : rw_list_tmp) { rw_list.add(rw_tmp); }
	    }
	}
	return rw_list;
    }

    private List<Modification> replaceWith(ConditionalExpression ce1, ConditionalExpression ce2, AST tast) {
	List<Modification> mod_list = new ArrayList<Modification>();
	Expression c1 = ce1.getExpression();
	Expression c2 = ce2.getExpression();
	if (!isEquivalent(c1, c2)) {
	    ASTRewrite rw0 = ASTRewrite.create(tast);
	    rw0.set(ce1, ConditionalExpression.EXPRESSION_PROPERTY, c2, null);
	    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(c1, c2, 0), ModificationSizeCalculator.calculate0(c1, c2, 1));
	    mod_list.add(mod0);
	}

	Expression t1 = ce1.getThenExpression();
	Expression t2 = ce2.getThenExpression();
	if (!isEquivalent(t1, t2)) {
	    ASTRewrite rw0 = ASTRewrite.create(tast);
	    rw0.set(ce1, ConditionalExpression.THEN_EXPRESSION_PROPERTY, t2, null);
	    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(t1, t2, 0), ModificationSizeCalculator.calculate0(t1, t2, 1));
	    mod_list.add(mod0);
	}

	Expression e1 = ce1.getElseExpression();
	Expression e2 = ce2.getElseExpression();
	if (!isEquivalent(e1, e2)) {
	    ASTRewrite rw0 = ASTRewrite.create(tast);
	    rw0.set(ce1, ConditionalExpression.ELSE_EXPRESSION_PROPERTY, e2, null);
	    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(e1, e2, 0), ModificationSizeCalculator.calculate0(e1, e2, 1));
	    mod_list.add(mod0);
	}

	return mod_list;
    }
    
    private List<Modification> replaceWith(ConstructorInvocation ci1, ConstructorInvocation ci2, AST tast) {
	return replaceArgsWith(ci1.arguments(), ci2.arguments(), tast);
    }

    private List<Modification> replaceWith(SuperConstructorInvocation sci1, SuperConstructorInvocation sci2, AST tast) {
	List<Modification> mod_list = replaceArgsWith(sci1.arguments(), sci2.arguments(), tast);
	Expression exp1 = sci1.getExpression();
	Expression exp2 = sci2.getExpression();
	if (!isEquivalent(exp1, exp2)) {
	    ASTRewrite rw0 = ASTRewrite.create(tast);
	    rw0.set(sci1, SuperConstructorInvocation.EXPRESSION_PROPERTY, exp2, null);
	    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(exp1, exp2, 0), ModificationSizeCalculator.calculate0(exp1, exp2, 1));
	    mod_list.add(mod0);
	}
	return mod_list;
    }

    private List<Modification> replaceWith(Assignment a1, Assignment a2, AST tast) {
	List<Modification> mod_list = new ArrayList<Modification>();
	Expression lhs1 = a1.getLeftHandSide();
	Expression lhs2 = a2.getLeftHandSide();
	if (!isEquivalent(lhs1, lhs2)) {
	    ASTRewrite rw0 = ASTRewrite.create(tast);
	    rw0.set(a1, Assignment.LEFT_HAND_SIDE_PROPERTY, lhs2, null);
	    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(lhs1, lhs2, 0), ModificationSizeCalculator.calculate0(lhs1, lhs2, 1));
	    mod_list.add(mod0);
	}
	Assignment.Operator op1 = a1.getOperator();
	Assignment.Operator op2 = a2.getOperator();
	if (!op1.toString().equals(op2.toString())) {
	    ASTRewrite rw0 = ASTRewrite.create(tast);
	    rw0.set(a1, Assignment.OPERATOR_PROPERTY, op2, null);
	    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate1(op1, op2, 0), ModificationSizeCalculator.calculate1(op1, op2, 1));
	    mod_list.add(mod0);
	}
	Expression rhs1 = a1.getRightHandSide();
	Expression rhs2 = a2.getRightHandSide();
	if (!isEquivalent(rhs1, rhs2)) {
	    ASTRewrite rw0 = ASTRewrite.create(tast);
	    rw0.set(a1, Assignment.RIGHT_HAND_SIDE_PROPERTY, rhs2, null);
	    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(rhs1, rhs2, 0), ModificationSizeCalculator.calculate0(rhs1, rhs2, 1));
	    mod_list.add(mod0);
	}
	return mod_list;
    }

    private List<Modification> replaceWith(InfixExpression ie1, InfixExpression ie2, AST tast) {
	List<Modification> mod_list = new ArrayList<Modification>();
	boolean hasExOp1 = ie1.hasExtendedOperands();
	boolean hasExOp2 = ie2.hasExtendedOperands();
	if (hasExOp1 == hasExOp2) {
	    List exOps1 = ie1.extendedOperands();
	    List exOps2 = ie2.extendedOperands();
	    int exOps_size1 = (exOps1==null) ? 0 : exOps1.size();
	    int exOps_size2 = (exOps2==null) ? 0 : exOps2.size();
	    if (exOps_size1 == exOps_size2) {
		Expression lhs1 = ie1.getLeftOperand();
		Expression lhs2 = ie2.getLeftOperand();
		if (!isEquivalent(lhs1, lhs2)) {
		    ASTRewrite rw0 = ASTRewrite.create(tast);
		    rw0.set(ie1, InfixExpression.LEFT_OPERAND_PROPERTY, lhs2, null);
		    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(lhs1, lhs2, 0), ModificationSizeCalculator.calculate0(lhs1, lhs2, 1));
		    mod_list.add(mod0);
		}
		InfixExpression.Operator opt1 = ie1.getOperator();
		InfixExpression.Operator opt2 = ie2.getOperator();
		if (!opt1.toString().equals(opt2.toString())) {
		    ASTRewrite rw0 = ASTRewrite.create(tast);
		    rw0.set(ie1, InfixExpression.OPERATOR_PROPERTY, opt2, null);
		    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate1(opt1, opt2, 0), ModificationSizeCalculator.calculate1(opt1, opt2, 1));
		    mod_list.add(mod0);
		}
		Expression rhs1 = ie1.getRightOperand();
		Expression rhs2 = ie2.getRightOperand();
		if (!isEquivalent(rhs1, rhs2)) {
		    ASTRewrite rw0 = ASTRewrite.create(tast);
		    rw0.set(ie1, InfixExpression.RIGHT_OPERAND_PROPERTY, rhs2, null);
		    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(rhs1, rhs2, 0), ModificationSizeCalculator.calculate0(rhs1, rhs2, 1));
		    mod_list.add(mod0);
		}
		
		if (exOps_size1 > 0) {
		    for (int i=0; i<exOps_size1; i++) {
			Expression op1 = (Expression) exOps1.get(i);
			Expression op2 = (Expression) exOps2.get(i);
			if (!isEquivalent(op1, op2)) {
			    ASTRewrite rw0 = ASTRewrite.create(tast);
			    rw0.replace(op1, op2, null);
			    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(op1, op2, 0), ModificationSizeCalculator.calculate0(op1, op2, 1));
			    mod_list.add(mod0);
			}
		    }
		}
	    }
	}
	return mod_list;
    }

    private List<Modification> replaceWith(MethodInvocation mi1, MethodInvocation mi2, AST tast) {
	List<Modification> mod_list = new ArrayList<Modification>();
	SimpleName sname1 = mi1.getName();
	SimpleName sname2 = mi2.getName();
	if (!isEquivalent(sname1, sname2)) {
	    ASTRewrite rw0 = ASTRewrite.create(tast);
	    rw0.set(mi1, MethodInvocation.NAME_PROPERTY, sname2, null);
	    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(sname1, sname2, 0), ModificationSizeCalculator.calculate0(sname1, sname2, 1));
	    mod_list.add(mod0);
	}
	List<Modification> arg_mod_list = replaceArgsWith(mi1.arguments(), mi2.arguments(), tast);
	for (Modification arg_mod : arg_mod_list) {
	    mod_list.add(arg_mod);
	}
	Expression exp1 = mi1.getExpression();
	Expression exp2 = mi2.getExpression();
	if (!isEquivalent(exp1, exp2)) {
	    ASTRewrite rw0 = ASTRewrite.create(tast);
	    rw0.set(mi1, MethodInvocation.EXPRESSION_PROPERTY, exp2, null);
	    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(exp1, exp2, 0), ModificationSizeCalculator.calculate0(exp1, exp2, 1));
	    mod_list.add(mod0);
	}
	return mod_list;
    }

    private List<Modification> replaceWith(SuperMethodInvocation smi1, SuperMethodInvocation smi2, AST tast) {
	List<Modification> mod_list = new ArrayList<Modification>();
	SimpleName sname1 = smi1.getName();
	SimpleName sname2 = smi2.getName();
	if (!isEquivalent(sname1, sname2)) {
	    ASTRewrite rw0 = ASTRewrite.create(tast);
	    rw0.set(smi1, SuperMethodInvocation.NAME_PROPERTY, sname2, null);
	    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(sname1, sname2, 0), ModificationSizeCalculator.calculate0(sname1, sname2, 1));
	    mod_list.add(mod0);
	}
	List<Modification> arg_mod_list = replaceArgsWith(smi1.arguments(), smi2.arguments(), tast);
	for (Modification arg_mod : arg_mod_list) { mod_list.add(arg_mod); }
	Name q1 = smi1.getQualifier();
	Name q2 = smi2.getQualifier();
	if (!isEquivalent(q1, q2)) {
	    ASTRewrite rw0 = ASTRewrite.create(tast);
	    rw0.set(smi1, SuperMethodInvocation.QUALIFIER_PROPERTY, q2, null);
	    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(q1, q2, 0), ModificationSizeCalculator.calculate0(q1, q2, 1));
	    mod_list.add(mod0);
	}
	return mod_list;
    }

    private List<Modification> replaceWith(PostfixExpression pe1, PostfixExpression pe2, AST tast) {
	List<Modification> mod_list = new ArrayList<Modification>();
	Expression op1 = pe1.getOperand();
	Expression op2 = pe2.getOperand();
	if (!isEquivalent(op1, op2)) {
	    ASTRewrite rw0 = ASTRewrite.create(tast);
	    rw0.set(pe1, PostfixExpression.OPERAND_PROPERTY, op2, null);
	    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(op1, op2, 0), ModificationSizeCalculator.calculate0(op1, op2, 1));
	    mod_list.add(mod0);
	}
	PostfixExpression.Operator opt1 = pe1.getOperator();
	PostfixExpression.Operator opt2 = pe2.getOperator();
	if (!opt1.toString().equals(opt2.toString())) {
	    ASTRewrite rw0 = ASTRewrite.create(tast);
	    rw0.set(pe1, PostfixExpression.OPERATOR_PROPERTY, opt2, null);
	    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate1(opt1, opt2, 0), ModificationSizeCalculator.calculate1(opt1, opt2, 1));
	    mod_list.add(mod0);
	}
	return mod_list;
    }

    private List<Modification> replaceWith(PrefixExpression pe1, PrefixExpression pe2, AST tast) {
	List<Modification> mod_list = new ArrayList<Modification>();
	Expression op1 = pe1.getOperand();
	Expression op2 = pe2.getOperand();
	if (!isEquivalent(op1, op2)) {
	    ASTRewrite rw0 = ASTRewrite.create(tast);
	    rw0.set(pe1, PrefixExpression.OPERAND_PROPERTY, op2, null);
	    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(op1, op2, 0), ModificationSizeCalculator.calculate0(op1, op2, 1));
	    mod_list.add(mod0);
	}
	PrefixExpression.Operator opt1 = pe1.getOperator();
	PrefixExpression.Operator opt2 = pe2.getOperator();
	if (!opt1.toString().equals(opt2.toString())) {
	    ASTRewrite rw0 = ASTRewrite.create(tast);
	    rw0.set(pe1, PrefixExpression.OPERATOR_PROPERTY, opt2, null);
	    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate1(opt1, opt2, 0), ModificationSizeCalculator.calculate1(opt1, opt2, 1));
	    mod_list.add(mod0);
	}
	return mod_list;
    }
    
    private List<Modification> replaceWith(CatchClause cc1, CatchClause cc2, AST tast) {

	List<Modification> mod_list = new ArrayList<Modification>();

	//Replace the exception expression
	SingleVariableDeclaration e1 = cc1.getException();
	SingleVariableDeclaration e2 = cc2.getException();
	if (!isEquivalent(e1, e2)) {
	    ASTRewrite rw0 = ASTRewrite.create(tast);
	    rw0.set(cc1, CatchClause.EXCEPTION_PROPERTY, e2, null);
	    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(e1, e2, 0), ModificationSizeCalculator.calculate0(e1, e2, 1));
	    mod_list.add(mod0);
	}

	//Replace the body block
	Block b1 = cc1.getBody();
	Block b2 = cc2.getBody();
	if (!isEquivalent(b1, b2)) {
	    ASTRewrite rw1 = ASTRewrite.create(tast);
	    rw1.set(cc1, CatchClause.BODY_PROPERTY, b2, null);
	    Modification mod1 = new Modification(rw1, "REPLACE", ModificationSizeCalculator.calculate0(b1, b2, 0), ModificationSizeCalculator.calculate0(b1, b2, 1));
	    mod_list.add(mod1);
	}
	return mod_list;
    }

    private List<Modification> replaceWith(ReturnStatement rs1, ReturnStatement rs2, AST tast) {
	List<Modification> mod_list = new ArrayList<Modification>();
	Expression e1 = rs1.getExpression();
	Expression e2 = rs2.getExpression();
	if (isEquivalent(e1, e2)) { return mod_list; }

	Type rtype1 = getMethodReturnType(rs1);
	Type rtype2 = getMethodReturnType(rs2);
	if (rtype1 != null && rtype2 != null) {
	    String rtype1_str = rtype1.toString().trim();
	    String rtype2_str = rtype2.toString().trim();
	    if (("boolean".equals(rtype1_str) || "Boolean".equals(rtype1_str)) &&
	        ("boolean".equals(rtype2_str) || "Boolean".equals(rtype2_str))) {
		//Combine the return expressions with && and || respectively
		boolean check_with_null1 = checkWithNull(e1);
		boolean check_with_null2 = checkWithNull(e2);
		InfixExpression comb0 = tast.newInfixExpression();
		comb0.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
		InfixExpression comb1 = tast.newInfixExpression();
		comb1.setOperator(InfixExpression.Operator.CONDITIONAL_OR);
		if (check_with_null2 && !check_with_null1) {
		    comb0.setLeftOperand((Expression) ASTNode.copySubtree(tast, e2));
		    comb0.setRightOperand((Expression) ASTNode.copySubtree(tast, e1));
		    comb1.setLeftOperand((Expression) ASTNode.copySubtree(tast, e2));
		    comb1.setRightOperand((Expression) ASTNode.copySubtree(tast, e1));
		}
		else {
		    comb0.setLeftOperand((Expression) ASTNode.copySubtree(tast, e1));
		    comb0.setRightOperand((Expression) ASTNode.copySubtree(tast, e2));
		    comb1.setLeftOperand((Expression) ASTNode.copySubtree(tast, e1));
		    comb1.setRightOperand((Expression) ASTNode.copySubtree(tast, e2));
		}
		ASTRewrite rw1 = ASTRewrite.create(tast);
		ASTRewrite rw2 = ASTRewrite.create(tast);
		rw1.set(rs1, ReturnStatement.EXPRESSION_PROPERTY, comb0, null);
		Modification mod1 = new Modification(rw1, "REPLACE", ModificationSizeCalculator.calculate0(e1, comb0, 0), ModificationSizeCalculator.calculate0(e1, comb0, 1));
		mod_list.add(mod1);
		rw2.set(rs2, ReturnStatement.EXPRESSION_PROPERTY, comb1, null);
		Modification mod2 = new Modification(rw2, "REPLACE", ModificationSizeCalculator.calculate0(e1, comb1, 0), ModificationSizeCalculator.calculate0(e1, comb1, 1));
		mod_list.add(mod2);
	    }
	}
	return mod_list;
    }

    private List<Modification> replaceWith(Block block1, Block block2, AST tast) {

	List<Modification> mod_list = new ArrayList<Modification>();
	ASTRewrite rw = ASTRewrite.create(tast);
	ListRewrite lrw = rw.getListRewrite(block1, Block.STATEMENTS_PROPERTY);
	List stmts1 = block1.statements();
	List stmts2 = block2.statements();
	for (Object stmt_obj1 : stmts1) {
	    lrw.remove((ASTNode) stmt_obj1, null);
	}
	for (Object stmt_obj2 : stmts2) {
	    lrw.insertLast((ASTNode) stmt_obj2, null);
	}
	Modification mod = new Modification(rw, "REPLACE", ModificationSizeCalculator.calculate0(block1, block2, 0), ModificationSizeCalculator.calculate0(block1, block2, 1));
	mod_list.add(mod);
	return mod_list;
    }

    /* (1) replace condition and (2) replace body. */
    private List<Modification> replaceLoopWith(ASTNode loop_node1, ASTNode loop_node2, AST tast) {
	List<Modification> mod_list = new ArrayList<Modification>();
	Expression c1 = getLoopCondition(loop_node1);
	Expression c2 = getLoopCondition(loop_node2);
	if (!isEquivalent(c1, c2) && (c2 != null)) {
	    ASTRewrite rw0 = ASTRewrite.create(tast);
	    if (loop_node1 instanceof DoStatement) {
		rw0.set((DoStatement) loop_node1, DoStatement.EXPRESSION_PROPERTY, c2, null);
		Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(c1, c2, 0), ModificationSizeCalculator.calculate0(c1, c2, 1));
		mod_list.add(mod0);
	    }
	    else if (loop_node1 instanceof ForStatement) {
		rw0.set((ForStatement) loop_node1, ForStatement.EXPRESSION_PROPERTY, c2, null);
		Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(c1, c2, 0), ModificationSizeCalculator.calculate0(c1, c2, 1));
		mod_list.add(mod0);
	    }
	    else if (loop_node1 instanceof WhileStatement) {
		rw0.set((WhileStatement) loop_node1, WhileStatement.EXPRESSION_PROPERTY, c2, null);
		Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(c1, c2, 0), ModificationSizeCalculator.calculate0(c1, c2, 1));
		mod_list.add(mod0);
	    }
	}
	Block b1 = (Block) getLoopBody(loop_node1);
	Block b2 = (Block) getLoopBody(loop_node2);
	if (!isEquivalent(b1, b2) && (b2 != null)) {
	    List<Modification> mod_list0 = replaceWith(b1, b2, tast);
	    for (Modification mod0 : mod_list0) {
		mod_list.add(mod0);
	    }
	}
	return mod_list;
    }
    
    /* Return Value could be NULL. */
    private Modification replaceWithForInitializers(ASTRewrite rw2, ForStatement forstmt1, ForStatement forstmt2, AST tast) {
	Modification mod2 = null;
	List init_list1 = forstmt1.initializers();
	List init_list2 = forstmt2.initializers();
	if (!isEquivalent(init_list1, init_list2)) {
	    ListRewrite lrw2 = rw2.getListRewrite(forstmt1, ForStatement.INITIALIZERS_PROPERTY);
	    for (Object init1 : init_list1) {
		lrw2.remove((ASTNode) init1, null);
	    }
	    for (Object init2 : init_list2) {
		lrw2.insertLast((ASTNode) init2, null);
	    }
	    mod2 = new Modification(rw2, "REPLACE", ModificationSizeCalculator.calculate2(init_list1, init_list2, 0), ModificationSizeCalculator.calculate2(init_list1, init_list2, 1));
	}
	return mod2;
    } 

    /* Return Value could be NULL. */
    private Modification replaceWithForUpdaters(ASTRewrite rw3, ForStatement forstmt1, ForStatement forstmt2, AST tast) {
	Modification mod3 = null;
	List upt_list1 = forstmt1.updaters();
	List upt_list2 = forstmt2.updaters();
	if (!isEquivalent(upt_list1, upt_list2)) {
	    ListRewrite lrw3 = rw3.getListRewrite(forstmt1, ForStatement.UPDATERS_PROPERTY);
	    for (Object upt1 : upt_list1) {
		lrw3.remove((ASTNode) upt1, null);
	    }
	    for (Object upt2 : upt_list2) {
		lrw3.insertLast((ASTNode) upt2, null);
	    }
	    mod3 = new Modification(rw3, "REPLACE", ModificationSizeCalculator.calculate2(upt_list1, upt_list2, 0), ModificationSizeCalculator.calculate2(upt_list1, upt_list2, 1));
	}
	return mod3;
    }

    private Modification replaceWithForHeader(ForStatement forstmt1, ForStatement forstmt2, AST tast) {
	Modification mod = null;
	Expression c1 = getLoopCondition(forstmt1);
	Expression c2 = getLoopCondition(forstmt2);
	ASTRewrite rw0 = ASTRewrite.create(tast);
	if (!isEquivalent(c1, c2) && (c2 != null)) {
	    rw0.set(forstmt1, ForStatement.EXPRESSION_PROPERTY, c2, null);
	    mod = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(c1, c2, 0), ModificationSizeCalculator.calculate0(c1, c2, 1));
	}

	Modification mod2 = replaceWithForInitializers(rw0, forstmt1, forstmt2, tast);
	if (mod == null) { mod = mod2; }
	else if (mod2 != null) {
	    mod.setHeightSize(mod.getHeightSize() + mod2.getHeightSize());
	    mod.setStringSimilarity(mod.getStringSimilarity() + mod2.getStringSimilarity());
	}

	Modification mod3 = replaceWithForUpdaters(rw0, forstmt1, forstmt2, tast);
	if (mod == null) { mod = mod3; }
	else if (mod3 != null) {
	    mod.setHeightSize(mod.getHeightSize() + mod3.getHeightSize());
	    mod.setStringSimilarity(mod.getStringSimilarity() + mod3.getStringSimilarity());
	}

	return mod;
    }
    
    private List<Modification> replaceWith(ForStatement forstmt1, ForStatement forstmt2, AST tast) {
	List<Modification> mod_list = replaceLoopWith(forstmt1, forstmt2, tast);

	//Replace the initializers, condition, & updaters
	Modification mod1 = replaceWithForHeader(forstmt1, forstmt2, tast);
	if (mod1 != null) { mod_list.add(mod1); }
	
	//Replace the initializers
	Modification mod2 = replaceWithForInitializers(ASTRewrite.create(tast), forstmt1, forstmt2, tast);
	if (mod2 != null) { mod_list.add(mod2); }

	//Replace the updaters
	Modification mod3 = replaceWithForUpdaters(ASTRewrite.create(tast), forstmt1, forstmt2, tast);
	if (mod3 != null) { mod_list.add(mod3); }

	return mod_list;
    }
    
    private List<Modification> replaceWith(IfStatement ifstmt1, IfStatement ifstmt2, AST tast) {
	List<Modification> mod_list = new ArrayList<Modification>();
	Statement else1 = ifstmt1.getElseStatement();
	Statement else2 = ifstmt2.getElseStatement();
	
	//Predicate
	Expression cond1 = ifstmt1.getExpression();
	Expression cond2 = ifstmt2.getExpression();
	if (!isEquivalent(cond1, cond2)) {
	    ASTRewrite rw0 = ASTRewrite.create(tast);
	    rw0.set(ifstmt1, IfStatement.EXPRESSION_PROPERTY, cond2, null);
	    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(cond1, cond2, 0), ModificationSizeCalculator.calculate0(cond1, cond2, 1));
	    mod_list.add(mod0);

	    //Create the combined condition with && and || respectively	    
	    boolean check_with_null1 = checkWithNull(cond1);
	    boolean check_with_null2 = checkWithNull(cond2);
	    InfixExpression comb0 = tast.newInfixExpression();
	    comb0.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
	    InfixExpression comb1 = tast.newInfixExpression();
	    comb1.setOperator(InfixExpression.Operator.CONDITIONAL_OR);
	    if (check_with_null2 && !check_with_null1) {
		comb0.setLeftOperand((Expression) ASTNode.copySubtree(tast, cond2));
		comb0.setRightOperand((Expression) ASTNode.copySubtree(tast, cond1));
		comb1.setLeftOperand((Expression) ASTNode.copySubtree(tast, cond2));
		comb1.setRightOperand((Expression) ASTNode.copySubtree(tast, cond1));
	    }
	    else {
		comb0.setLeftOperand((Expression) ASTNode.copySubtree(tast, cond1));
		comb0.setRightOperand((Expression) ASTNode.copySubtree(tast, cond2));
		comb1.setLeftOperand((Expression) ASTNode.copySubtree(tast, cond1));
		comb1.setRightOperand((Expression) ASTNode.copySubtree(tast, cond2));
	    }
	    ASTRewrite rw1 = ASTRewrite.create(tast);
	    ASTRewrite rw2 = ASTRewrite.create(tast);
	    rw1.set(ifstmt1, IfStatement.EXPRESSION_PROPERTY, comb0, null);
	    Modification mod1 = new Modification(rw1, "REPLACE", ModificationSizeCalculator.calculate0(cond1, comb0, 0), ModificationSizeCalculator.calculate0(cond1, comb0, 1));
	    mod_list.add(mod1);
	    rw2.set(ifstmt1, IfStatement.EXPRESSION_PROPERTY, comb1, null);
	    Modification mod2 = new Modification(rw2, "REPLACE", ModificationSizeCalculator.calculate0(cond1, comb1, 0), ModificationSizeCalculator.calculate0(cond1, comb1, 1));
	    mod_list.add(mod2);
	}

	//then-body
	Statement then1 = ifstmt1.getThenStatement();
	Statement then2 = ifstmt2.getThenStatement();
	if (!isEquivalent(then1, then2)) {
	    Block block1 = (Block) then1;
	    Block block2 = (Block) then2;
	    List<Modification> mod_list1 = replaceWith(block1, block2, tast);
	    for (Modification mod1 : mod_list1) {
		mod_list.add(mod1);
	    }
	}

	//else-body
	if (!isEquivalent(else1, else2)) {
	    if (else1 == null || else2 == null) {
		ASTRewrite rw2 = ASTRewrite.create(tast);
		rw2.set(ifstmt1, IfStatement.ELSE_STATEMENT_PROPERTY, else2, null);
		Modification mod2 = new Modification(rw2, "REPLACE", ModificationSizeCalculator.calculate0(else1, else2, 0), ModificationSizeCalculator.calculate0(else1, else2, 1));
		mod_list.add(mod2);
	    }
	    else {
		Block block1 = (Block) else1;
		Block block2 = (Block) else2;
		List<Modification> mod_list2 = replaceWith(block1, block2, tast);
		for (Modification mod2 : mod_list2) {
		    mod_list.add(mod2);
		}
	    }
	}
	
	return mod_list;
    }

    
    private List<Modification> replaceWith(SwitchStatement ss1, SwitchStatement ss2, AST tast) {
	List<Modification> mod_list = new ArrayList<Modification>();

	//Predicate
	Expression cond1 = ss1.getExpression();
	Expression cond2 = ss2.getExpression();
	if (!isEquivalent(cond1, cond2)) {
	    ASTRewrite rw0 = ASTRewrite.create(tast);
	    rw0.set(ss1, SwitchStatement.EXPRESSION_PROPERTY, cond2, null);
	    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(cond1, cond2, 0), ModificationSizeCalculator.calculate0(cond1, cond2, 1));
	    mod_list.add(mod0);
	}

	List body1 = ss1.statements();
	List body2 = ss2.statements();
	if (!isEquivalent(body1, body2)) {
	    
	    ASTRewrite rw1 = ASTRewrite.create(tast);
	    ListRewrite lrw = rw1.getListRewrite(ss1, SwitchStatement.STATEMENTS_PROPERTY);
	    for (Object stmt_obj1 : body1) {
		lrw.remove((ASTNode) stmt_obj1, null);
	    }
	    for (Object stmt_obj2 : body2) {
		lrw.insertLast((ASTNode) stmt_obj2, null);
	    }
	    Modification mod1 = new Modification(rw1, "REPLACE", ModificationSizeCalculator.calculate2(body1, body2, 0), ModificationSizeCalculator.calculate2(body1, body2, 1));
	    mod_list.add(mod1);
	}
	
	return mod_list;
    }
    
    private List<Modification> replaceWith(SynchronizedStatement ss1, SynchronizedStatement ss2, AST tast) {

	List<Modification> mod_list = new ArrayList<Modification>();
	
	//Predicate
	Expression cond1 = ss1.getExpression();
	Expression cond2 = ss2.getExpression();
	if (!isEquivalent(cond1, cond2)) {
	    ASTRewrite rw0 = ASTRewrite.create(tast);
	    rw0.set(ss1, SynchronizedStatement.EXPRESSION_PROPERTY, cond2, null);
	    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(cond1, cond2, 0), ModificationSizeCalculator.calculate0(cond1, cond2, 1));
	    mod_list.add(mod0);
	}

	//Body
	Statement body1 = ss1.getBody();
	Statement body2 = ss2.getBody();
	if (!isEquivalent(body1, body2)) {
	    Block block1 = (Block) body1;
	    Block block2 = (Block) body2;
	    List<Modification> mod_list1 = replaceWith(block1, block2, tast);
	    for (Modification mod1 : mod_list1) {
		mod_list.add(mod1);
	    }
	}
	
	return mod_list;
    }

    private List<Modification> replaceWith(TryStatement try_stmt1, TryStatement try_stmt2, AST tast) {

	List<Modification> mod_list = new ArrayList<Modification>();

	Statement try_body1 = try_stmt1.getBody();
	Statement try_body2 = try_stmt2.getBody();
	if (!isEquivalent(try_body1, try_body2)) {
	    Block block1 = (Block)try_body1;
	    Block block2 = (Block)try_body2;
	    List<Modification> mod_list1 = replaceWith(block1, block2, tast);
	    for (Modification mod1 : mod_list1) {
		mod_list.add(mod1);
	    }
	}

	/*
	  //Ignored temporarily.
	List catch_list1 = try_stmt1.catchClauses();
	List catch_list2 = try_stmt2.catchClauses();
	if (!isEquivalent(catch_list1, catch_list2)) {
	    ASTRewrite rw1 = ASTRewrite.create(tast);
	    rw1.set(try_stmt1, TryStatement.CATCH_CLAUSES_PROPERTY, catch_list2, null);
	    mod_list.add(rw1);
	}
	*/

	Statement finally_body1 = try_stmt1.getFinally();
	Statement finally_body2 = try_stmt2.getFinally();
	if (!isEquivalent(finally_body1, finally_body2)) {
	    Block block1 = (Block)finally_body1;
	    Block block2 = (Block)finally_body2;
	    List<Modification> mod_list1 = replaceWith(block1, block2, tast);
	    for (Modification mod1 : mod_list1) {
		mod_list.add(mod1);
	    }
	}

	return mod_list;
    }

    
    private List<Modification> replaceArgsWith(List args1, List args2, AST tast) {
	List<Modification> mod_list = new ArrayList<Modification>();
	int size1 = args1.size();
	int size2 = args2.size();
	if (size1 == size2) {
	    for (int i=0; i<size1; i++) {
		ASTNode arg1 = (ASTNode) args1.get(i);
		ASTNode arg2 = (ASTNode) args2.get(i);
		if (!isEquivalent(arg1, arg2)) {
		    ASTRewrite rw0 = ASTRewrite.create(tast);
		    rw0.replace(arg1, arg2, null);
		    Modification mod0 = new Modification(rw0, "REPLACE", ModificationSizeCalculator.calculate0(arg1, arg2, 0), ModificationSizeCalculator.calculate0(arg1, arg2, 1));
		    mod_list.add(mod0);
		}
	    }
	}
	return mod_list;
    }

    

    private ASTNode getCoreASTNode(ASTNode node) {
	if (node instanceof ExpressionStatement) {
	    ExpressionStatement es = (ExpressionStatement) node;
	    return getCoreASTNode(es.getExpression());
	}
	else if (node instanceof ParenthesizedExpression) {
	    ParenthesizedExpression pe = (ParenthesizedExpression) node;
	    return getCoreASTNode(pe.getExpression());
	}
	else {
	    return node;
	}
    }

    public boolean isEquivalent(ASTNode node1, ASTNode node2) {
	return isEquivalent(node1, node2, true);
    }
    
    public boolean isEquivalent(ASTNode node1, ASTNode node2, boolean strip) {
	if (node1 == null && node2 == null) { return true; }
	else if (node1 == null && node2 != null) { return false; }
	else if (node1 != null && node2 == null) { return false; }
	else {
	    String str1 = (strip) ? getCoreASTNode(node1).toString() : node1.toString();
	    String str2 = (strip) ? getCoreASTNode(node2).toString() : node1.toString();
	    if (str1.equals(str2)) { return true; }
	    else { return false; }
	}
    }

    private boolean isEquivalent(List list1, List list2) {
	return isEquivalent(list1, list2, true);
    }
    
    private boolean isEquivalent(List list1, List list2, boolean strip) {
	if (list1 == null && list2 == null) { return true; }
	else if (list1 == null && list2 != null) { return false; }
	else if (list1 != null && list2 == null) { return false; }
	else {
	    int size1 = list1.size();
	    int size2 = list2.size();
	    if (size1 == size2) {
		for (int i=0; i<size1; i++) {
		    Object obj1 = list1.get(i);
		    Object obj2 = list2.get(i);
		    if (obj1 == null && obj2 == null) {}
		    else if (obj1 == null && obj2 != null) { return false; }
		    else if (obj1 != null && obj2 == null) { return false; }
		    else {
			ASTNode node1 = (ASTNode) obj1;
			ASTNode node2 = (ASTNode) obj2;
			String str1 = (strip) ? getCoreASTNode(node1).toString() : node1.toString();
			String str2 = (strip) ? getCoreASTNode(node2).toString() : node1.toString();
			
			if (!str1.equals(str2)) {
			    return false;
			}
		    }
		}
		return true;
	    }
	    else {
		return false;
	    }
	}
    }

    private boolean isLoopNode(ASTNode node) {
	if (node instanceof DoStatement) { return true; }
	else if (node instanceof ForStatement) { return true; }
	else if (node instanceof EnhancedForStatement) { return true; }
	else if (node instanceof WhileStatement) { return true; }
	else { return false; }
    }

    private Expression getLoopCondition(ASTNode node) {
	if (node instanceof DoStatement) {
	    DoStatement do_stmt = (DoStatement) node;
	    return do_stmt.getExpression();
	}
	else if (node instanceof ForStatement) {
	    ForStatement fstmt = (ForStatement) node;
	    return fstmt.getExpression();
	}
	else if (node instanceof WhileStatement) {
	    WhileStatement wstmt = (WhileStatement) node;
	    return wstmt.getExpression();
	}
	else {
	    return null;
	}
    }

    private ASTNode getLoopBody(ASTNode node) {
	if (node instanceof DoStatement) {
	    DoStatement do_stmt = (DoStatement) node;
	    return do_stmt.getBody();
	}
	else if (node instanceof ForStatement) {
	    ForStatement fstmt = (ForStatement) node;
	    return fstmt.getBody();
	}
	else if (node instanceof EnhancedForStatement) {
	    EnhancedForStatement efstmt = (EnhancedForStatement) node;
	    return efstmt.getBody();
	}
	else if (node instanceof WhileStatement) {
	    WhileStatement wstmt = (WhileStatement) node;
	    return wstmt.getBody();
	}
	else {
	    return null;
	}
	
    }

    private boolean checkWithNull(ASTNode node) {
	String node_str = node.toString().trim();
	if (node_str.endsWith("== null") || node_str.endsWith("!= null")) {
	    return true;
	}
	else {
	    return false;
	}
    }

    private Type getMethodReturnType(ASTNode node) {
	ASTNode md_node = node;
	while (md_node != null) {
	    if (md_node instanceof MethodDeclaration) { break; }
	    md_node = md_node.getParent();
	}
	if (md_node == null) { return null; }
	else {
	    MethodDeclaration md = (MethodDeclaration) md_node;
	    return md.getReturnType2();
	}
    }
}
