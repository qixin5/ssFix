package edu.brown.cs.ssfix.repair;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import edu.brown.cs.cocker.application.ApplicationChunkQuery;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.ASTNode;
import edu.brown.cs.ssfix.search.*;
import edu.brown.cs.ssfix.util.*;


public class SearchInvoker
{
    private final boolean AVOID_CHEAT_FIX = true;
    
    public List<String> invoke(String bfpath, String search_loc, String ssfix_dpath, String anal_method, String cocker_rslt_fpath, boolean filter) {
	List<String> cchunk_lines = new ArrayList<String>();
	//Invoke cocker
	List<String> cocker_arg_list = new ArrayList<String>();
	cocker_arg_list.add("-a");
	cocker_arg_list.add(anal_method);
	cocker_arg_list.add("-n");
	cocker_arg_list.add("fred4");
	cocker_arg_list.add("-data");
	cocker_arg_list.add(search_loc);
	cocker_arg_list.add("-s");
	cocker_arg_list.add("even_weighted");
	cocker_arg_list.add("-host");
	cocker_arg_list.add("bdognom.cs.brown.edu");
	cocker_arg_list.add(bfpath);
	String[] cocker_args = cocker_arg_list.toArray(new String[0]);
	
	System.out.println("*** Cocker Search Arguments ***");
	for (String cocker_arg : cocker_args) {
	    System.out.print(cocker_arg+" ");
	}
	System.out.println();
	System.out.println();
	
	boolean cocker_ok = true;
	CockerRunner cocker_runner = new CockerRunner(cocker_args);
	ExecutorService executor = Executors.newSingleThreadExecutor();
	Future<String> future = executor.submit(cocker_runner);
	executor.shutdown();
	String search_rslt0 = null;
	try { search_rslt0 = future.get(3l, TimeUnit.MINUTES); }
	catch (Exception e) { System.err.println(e); e.printStackTrace(); }
	if (!executor.isTerminated()) { executor.shutdownNow(); }
	if (search_rslt0 == null) {
	    System.out.println("Cocker Failed.");
	    return cchunk_lines;
	}
	else {
	    System.out.println("Cocker Finished");
	}
	search_rslt0 = search_rslt0.trim();
	if (search_rslt0.equals("")) {
	    System.err.println("No cocker result available.");
	    return cchunk_lines;
	}

	//Return if no filtering
	if (!filter) { return Arrays.asList(search_rslt0.split("\n")); }
	
	//Filter the results
	System.out.println("Cocker Result Filtering ...");
	int max_lines_to_filter = 1000;
	int curr_line_count = 0;
	String[] binfo = getClassNameAndMethodSignature(bfpath, search_loc, 0, anal_method);
	if (binfo[0].startsWith("org.apache.commons.lang3")) {
	    binfo[0] = binfo[0].replace("org.apache.commons.lang3","org.apache.commons.lang");
	}
	else if (binfo[0].startsWith("org.apache.commons.math3")) {
	    binfo[0] = binfo[0].replace("org.apache.commons.math3","org.apache.commons.math");
	}
	String[] search_rslt0_lines_tmp = search_rslt0.split("\n");
	List<String> search_rslt0_lines = new ArrayList<String>();
	for (String search_rslt0_line : search_rslt0_lines_tmp) {
	    if (search_rslt0_line.startsWith("file://")) {
		int i0 = search_rslt0_line.indexOf(",");
		int i1 = search_rslt0_line.lastIndexOf(",");
		if ((i0==-1) || (i1==-1) || (i0+1==i1)) { continue; }
		String cfpath = search_rslt0_line.substring(7, i0);
		File cf = new File(cfpath);
		//if (!cf.exists()) { continue; }
		if (AVOID_CHEAT_FIX) {
		    String cloc = search_rslt0_line.substring(i0+1, i1);
		    String[] cinfo = getClassNameAndMethodSignature(cfpath, cloc, 1, anal_method);
		    String cinfo0 = cinfo[0];
		    if (cinfo0.startsWith("org.apache.commons.lang3")) {
			cinfo0 = cinfo0.replace("org.apache.commons.lang3","org.apache.commons.lang");
		    }
		    else if (cinfo0.startsWith("org.apache.commons.math3")) {
			cinfo0 = cinfo0.replace("org.apache.commons.math3","org.apache.commons.math");
		    }
		    if (cinfo0.equals(binfo[0]) && cinfo[1].equals(binfo[1])) {
			//Likely to contain the cheat fix
			continue;
		    }
		}
		search_rslt0_lines.add(search_rslt0_line);
		curr_line_count += 1;
		if (curr_line_count >= 1000) { break; }
	    }
	}
	String search_rslt0_filtered = SearchFilter.filter(search_rslt0_lines, null, max_lines_to_filter, anal_method);
	if (search_rslt0_filtered == null) {
	    System.err.println("No cocker result available after filtering.");
	    return cchunk_lines;
	}
	search_rslt0_filtered = search_rslt0_filtered.trim();
	if (search_rslt0_filtered.equals("")) {
	    System.err.println("No cocker result available after filtering.");
	    return cchunk_lines;
	}
	System.out.println("Done result filtering.");
	if (cocker_rslt_fpath != null) {
	    System.err.println("Writing Cocker Result ...");
	    String search_rslt_str = bfpath+","+search_loc+"\n"+search_rslt0_filtered;
	    File cocker_rslt_f = new File(cocker_rslt_fpath);
	    try { FileUtils.writeStringToFile(cocker_rslt_f, search_rslt_str); }
	    catch (Throwable t) { System.err.println(t); t.printStackTrace(); }
	    System.err.println("Done Writing Cocker Result.");
	}
	
	String[] cchunk_lines_tmp = search_rslt0_filtered.split("\n");
	return Arrays.asList(cchunk_lines_tmp);
    }

    private class CockerRunner implements Callable<String>
    {
	private String[] cocker_args;

	public CockerRunner(String[] args) {
	    cocker_args = args;
	}

	@Override public String call() {
	    ApplicationChunkQuery chunker = new ApplicationChunkQuery(cocker_args);
	    return chunker.execute();
	}
    }

    /* All the located nodes should be from only ONE method.
       flag 0: bchunk
       flag 1: cchunk */
    private String[] getClassNameAndMethodSignature(String fpath, String loc, int flag, String analmethod) {
	String cname = "";
	String msig = "";
	List<ASTNode> found_nodes = null;
        CompilationUnit cu = null;
	if (flag == 0) {
	    cu = (CompilationUnit) ASTNodeLoader.getASTNode(new File(fpath));
	    found_nodes = ASTNodeFinder.find(cu, loc);
	}
	else {
	    String cfctnt = CandidateLoader.getFileContent(fpath, analmethod);
            cu = (CompilationUnit) ASTNodeLoader.getResolvedASTNode(fpath, cfctnt);
            found_nodes = ASTNodeFinder.find(cu, loc);
	}

	if (found_nodes!=null && !found_nodes.isEmpty()) {
	    ASTNode tnode = found_nodes.get(0);
	    AbstractTypeDeclaration atd = null;
	    MethodDeclaration md = null;
	    ASTNode currnode = tnode;
	    while (currnode != null) {
		if ((md==null) && (currnode instanceof MethodDeclaration)) {
		    md = (MethodDeclaration) currnode; //Find the first enclosing method
		}
		if (currnode instanceof AbstractTypeDeclaration) {
		    atd = (AbstractTypeDeclaration) currnode; //Find the last enclosing class
		}
		currnode = currnode.getParent();
	    }
	    if (md != null) {
		msig = getMethodSignatureString(md);
	    }
	    if (atd != null) {
		cname = atd.getName().getIdentifier();
		PackageDeclaration pd = cu.getPackage();
		if (pd != null) { cname = pd.getName().toString()+"."+cname; }
	    }
	}
	return new String[] {cname, msig};
    }

    private String getMethodSignatureString(MethodDeclaration md) {
	String mname = md.getName().getIdentifier();
	String marg = null;
	List param_list = md.parameters();
	for (Object param_obj : param_list) {
	    SingleVariableDeclaration param_svd = (SingleVariableDeclaration) param_obj;
	    if (marg == null) { marg = param_svd.getType().toString(); }
	    else { marg += "$" + param_svd.getType().toString(); }
	}
	if (marg == null) { marg = ""; }
	return mname + "(" + marg + ")";
    }
}
