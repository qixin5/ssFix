package patchgen;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import util.*;


public class PatchValidator
{
    private final boolean OUTPUT_TESTING_RESULT = true;

    private String bug_id;
    //private String proj_dpath;
    private String proj_testbuild_dpath;
    private String dependjpath;
    private String ssfix_dpath;
    private String output_bugid_dpath;
    private String exec_dpath;

    public PatchValidator(String bug_id, String proj_testbuild_dpath, String dependjpath, String ssfix_dpath) {
	this.bug_id = bug_id;
	//this.proj_dpath = proj_dpath;
	this.proj_testbuild_dpath = proj_testbuild_dpath;
	this.dependjpath = dependjpath;
	this.ssfix_dpath = ssfix_dpath;
	String outputdpath = repair.Global.outputdpath;
	this.output_bugid_dpath = outputdpath+"/"+bug_id;	
	this.exec_dpath = outputdpath;
    }
    
    public Patch validate(String patch_text, String patch_fpath, String patch_dpath, String failed_testcases) {
	//Write patch to file
	File patch_f = new File(patch_fpath);
	boolean flag0 = true;
	try { FileUtils.writeStringToFile(patch_f, patch_text, (String) null); }
	catch (Throwable t) {
	    System.err.println("Failed in Writing the Patch File: " + patch_fpath);
	    flag0 = false;
	}
	if (!flag0) {
	    return new Patch(patch_fpath, false);
	}
	
	String script_fpath = ssfix_dpath+"/patchrunner.xml";
	//Compile the patch
	File patch_d = new File(patch_dpath);
	if (!patch_d.exists()) {
	    System.err.println("Patch Directory does not Exist: " + patch_dpath);
	    return new Patch(patch_fpath, false);
	}
	if (exec_dpath == null) { exec_dpath = patch_dpath; }
	File exec_d = new File(exec_dpath); //The reason for differentiating exec_d & patch_d is because sometimes the program (e.g., Time) need to access resource files. Then we could keep only one copy of the resource files under exec_d.
	String[] compile_cmds = new String[] {
	    "ant", "-f", script_fpath,
	    "-Ddependjpath="+dependjpath,
	    "-Dssfixdir="+ssfix_dpath,
	    "-Dpatchdir="+patch_dpath,
	    "-Dtestbuilddir="+proj_testbuild_dpath,
	    "compile-patch"
	};
	File output_f0 = null;
	if (OUTPUT_TESTING_RESULT) {
	    output_f0 = new File(patch_dpath + "/compile_rslt");
	}
	int exit_val0 = CommandExecutor.execute(compile_cmds, exec_d, output_f0);
	if (exit_val0 != 0) { //Compiling failure
	    //================
	    //System.err.println("Compiling Failure.");
	    //for (String compile_cmd : compile_cmds) {
	    //System.err.print(compile_cmd + " ");
	    //}
	    //System.err.println();
	    //================	    
	    return new Patch(patch_fpath, false);
	}
	
	//Test the patch against each failed test case
	String[] failed_testcase_arr = failed_testcases.split(":");
	int failed_testcase_arr_length = failed_testcase_arr.length;
	for (int i=0; i<failed_testcase_arr_length; i++) {
	    String failed_testcase = failed_testcase_arr[i];
	    String[] test_cmds0 = new String[] {
		"ant", "-f", script_fpath,
		"-Ddependjpath="+dependjpath,
		"-Dssfixdir="+ssfix_dpath,
		"-Dpatchdir="+patch_dpath,
		"-Dfailedclassname="+failed_testcase,
		"-Dtestbuilddir="+proj_testbuild_dpath,
		"test-patch0"
	    };
	    File output_f1 = null;
	    if (OUTPUT_TESTING_RESULT) {
		output_f1 = new File(patch_dpath+"/failed_testcase_testing_rslt"+i);
	    }
	    int exit_val1 = CommandExecutor.execute(test_cmds0, exec_d, output_f1);
	    if (exit_val1 != 0) { //Testing failure
		return new Patch(patch_fpath, false);
	    }
	}

	//File testsuite_f = new File(proj_dpath+"/testsuite_classes");
	File testsuite_f = new File(output_bugid_dpath+"/testsuite_classes");
	String test_case_paths = getTestCasePathsString(testsuite_f);
	if (test_case_paths == null) { return new Patch(patch_fpath, false); }

	//Test the patch against all the test cases
	String[] test_cmds1 = new String[] {
	    "ant", "-f", script_fpath,
	    "-Ddependjpath="+dependjpath,
	    "-Dssfixdir="+ssfix_dpath,
	    "-Dpatchdir="+patch_dpath,
	    "-Dtestbuilddir="+proj_testbuild_dpath,
	    "-Dtestcasepaths="+test_case_paths,
	    "test-patch"
	};
	File output_f2 = null;
	if (OUTPUT_TESTING_RESULT) {
	    output_f2 = new File(patch_dpath + "/all_testcases_testing_rslt");
	}
	int exit_val2 = CommandExecutor.execute(test_cmds1, exec_d, output_f2);
	if (exit_val2 != 0) {
	    return new Patch(patch_fpath, false);
	}
	
	return new Patch(patch_fpath, true);
    }

    private String getTestCasePathsString(File testsuite_f) {

	StringBuilder rslt_sb = new StringBuilder();
	String test_case_names = null;
	try { test_case_names = FileUtils.readFileToString(testsuite_f); }
	catch (Throwable t) { System.err.println(t); t.printStackTrace(); }
	if (test_case_names == null) { return null; }
	test_case_names = test_case_names.trim();
	
	String[] test_case_name_arr = test_case_names.split(";");
	int test_case_name_arr_length = test_case_name_arr.length;
	for (int i=0; i<test_case_name_arr_length; i++) {
	    String test_case_name = test_case_name_arr[i];
	    if (i != 0) { rslt_sb.append(","); }
	    int index0 = test_case_name.indexOf(".class");
	    if (index0 != -1) {
		rslt_sb.append(test_case_name.substring(0, index0).replace(".", "/"));
		rslt_sb.append(".class");
	    }
	    else {
		rslt_sb.append(test_case_name.replace(".", "/"));
	    }
	}

	return rslt_sb.toString();
    }
}
