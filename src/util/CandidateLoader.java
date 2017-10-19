package edu.brown.cs.ssfix.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
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

public class CandidateLoader
{
    public static List<ASTNode> getNodes(String cfpath, String cloc, String anal_method) {
	List<ASTNode> cnodes = new ArrayList<ASTNode>();
	String cfctnt = getFileContent(cfpath, anal_method);
	if (cfctnt == null || "".equals(cfctnt)) { return cnodes; }
	CompilationUnit ccu = null;
        try { 
	    ccu = (CompilationUnit) ASTNodeLoader.getResolvedASTNode(cfpath, cfctnt); } 
	catch (Throwable t) {
            System.err.println(t);
            t.printStackTrace(); }
        if (ccu == null) { return cnodes; }
        else {
	    return ASTNodeFinder.find(ccu, cloc); 
	}
    }

    public static String getFileContent(String cfpath, String anal_method) {
	List<String> cocker_arg_list = new ArrayList<String>();
	cocker_arg_list.add("-a");
	cocker_arg_list.add(anal_method);
	cocker_arg_list.add("-n");
	cocker_arg_list.add("fred4");
	cocker_arg_list.add("-host");
	cocker_arg_list.add("bdognom.cs.brown.edu");
	cocker_arg_list.add("-cf");
	cocker_arg_list.add(cfpath);
	String[] cocker_args = cocker_arg_list.toArray(new String[0]);
	
	//====================
	/*System.out.println("*** Cocker Search Arguments ***");
        for (String cocker_arg : cocker_args) {
            System.out.print(cocker_arg+" ");
        }
        System.out.println();
        System.out.println();*/
	//====================

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
	    System.out.println("Candidate Loader Failed.");
	    return ""; }
	//else {
	//System.out.println("Cocker Finished"); 
	//}
	search_rslt0 = search_rslt0.trim();
	return search_rslt0;
    }

    private static class CockerRunner implements Callable<String>
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

}