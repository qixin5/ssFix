package edu.brown.cs.ssfix.patchgen;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;


public class PatchGenerator
{
    private String bug_id;
    private String ssfix_dpath;
    private String proj_dpath;
    private String proj_testbuild_dpath;
    private String dependjpath;
    private String tsuite_fpath;
    private String failed_testcases; //Separated by ":"
    private boolean delete_failed_patch;
    private PatchGenerator0 pgen0;
    private PatchValidator pval;
    private Set<String> tested_pset;
    private int parallel_granularity;

    public static Map<String, String> failed_class_map;

    private final int MAX_PATCHES_TO_BE_VALIDATED = 50;

    static {
	failed_class_map = new HashMap<String, String>();
	//=== UPDATE THIS!!! ===
	failed_class_map.put("Chart_1", "org.jfree.chart.renderer.category.junit.AbstractCategoryItemRendererTests");
	failed_class_map.put("Chart_4", "org.jfree.chart.axis.junit.LogAxisTests");
	failed_class_map.put("Chart_8", "org.jfree.data.time.junit.WeekTests");
	failed_class_map.put("Chart_12", "org.jfree.chart.plot.junit.MultiplePiePlotTests");
	failed_class_map.put("Chart_13", "org.jfree.chart.block.junit.BorderArrangementTests");
	failed_class_map.put("Chart_17", "org.jfree.data.time.junit.TimeSeriesTests");
	failed_class_map.put("Chart_20", "org.jfree.chart.plot.junit.ValueMarkerTests");
	failed_class_map.put("Chart_24", "org.jfree.chart.renderer.junit.GrayPaintScaleTests");
	failed_class_map.put("Chart_26", "org.jfree.chart.junit.AreaChartTests");
	failed_class_map.put("Closure_1", "com.google.javascript.jscomp.IntegrationTest");
	failed_class_map.put("Closure_5", "com.google.javascript.jscomp.InlineObjectLiteralsTest");
	failed_class_map.put("Closure_7", "com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest");
	failed_class_map.put("Closure_10", "com.google.javascript.jscomp.PeepholeFoldConstantsTest");
	failed_class_map.put("Closure_11", "com.google.javascript.jscomp.TypeCheckTest");	
	failed_class_map.put("Closure_14", "com.google.javascript.jscomp.ControlFlowAnalysisTest");
	failed_class_map.put("Closure_15", "com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest");
	failed_class_map.put("Closure_18", "com.google.javascript.jscomp.IntegrationTest");
	failed_class_map.put("Closure_19", "com.google.javascript.jscomp.TypeInferenceTest");
	failed_class_map.put("Closure_20", "com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest");
	failed_class_map.put("Closure_31", "com.google.javascript.jscomp.CommandLineRunnerTest");
	failed_class_map.put("Closure_33", "com.google.javascript.jscomp.TypeCheckTest");	
	failed_class_map.put("Closure_40", "com.google.javascript.jscomp.NameAnalyzerTest");
	failed_class_map.put("Closure_42", "com.google.javascript.jscomp.parsing.ParserTest");
	failed_class_map.put("Closure_51", "com.google.javascript.jscomp.CodePrinterTest");
	failed_class_map.put("Closure_52", "com.google.javascript.jscomp.CodePrinterTest");
	failed_class_map.put("Closure_59", "com.google.javascript.jscomp.CommandLineRunnerTest");
	failed_class_map.put("Closure_62", "com.google.javascript.jscomp.LightweightMessageFormatterTest");	
	failed_class_map.put("Closure_65", "com.google.javascript.jscomp.CodePrinterTest");
	failed_class_map.put("Closure_66", "com.google.javascript.jscomp.TypeCheckTest");
	failed_class_map.put("Closure_67", "com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest");	
	failed_class_map.put("Closure_70", "com.google.javascript.jscomp.LooseTypeCheckTest");
	failed_class_map.put("Closure_71", "com.google.javascript.jscomp.CheckAccessControlsTest");	
	failed_class_map.put("Closure_73", "com.google.javascript.jscomp.CodePrinterTest");
	failed_class_map.put("Closure_77", "com.google.javascript.jscomp.CodePrinterTest");
	failed_class_map.put("Closure_82", "com.google.javascript.jscomp.TypeCheckTest");
	failed_class_map.put("Closure_86", "com.google.javascript.jscomp.NodeUtilTest");
	failed_class_map.put("Closure_92", "com.google.javascript.jscomp.ProcessClosurePrimitivesTest");
	failed_class_map.put("Closure_97", "com.google.javascript.jscomp.PeepholeFoldConstantsTest");
	failed_class_map.put("Closure_104", "com.google.javascript.rhino.jstype.UnionTypeTest");
	failed_class_map.put("Closure_111", "com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest");
	failed_class_map.put("Closure_113", "com.google.javascript.jscomp.VarCheckTest");
	failed_class_map.put("Closure_119", "com.google.javascript.jscomp.CheckGlobalNamesTest");
	failed_class_map.put("Closure_120", "com.google.javascript.jscomp.InlineVariablesTest");
	failed_class_map.put("Closure_126", "com.google.javascript.jscomp.MinimizeExitPointsTest");
	failed_class_map.put("Closure_129", "com.google.javascript.jscomp.IntegrationTest");
	failed_class_map.put("Closure_130", "com.google.javascript.jscomp.CollapsePropertiesTest");
	failed_class_map.put("Closure_132", "com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest");
	
	failed_class_map.put("Lang_2", "org.apache.commons.lang3.LocaleUtilsTest");
	failed_class_map.put("Lang_6", "org.apache.commons.lang3.StringUtilsTest");
	failed_class_map.put("Lang_9", "org.apache.commons.lang3.time.FastDateFormat_ParserTest");
	failed_class_map.put("Lang_11", "org.apache.commons.lang3.RandomStringUtilsTest");
	failed_class_map.put("Lang_16", "org.apache.commons.lang3.math.NumberUtilsTest");
	failed_class_map.put("Lang_21", "org.apache.commons.lang3.time.DateUtilsTest");
	failed_class_map.put("Lang_24", "org.apache.commons.lang3.math.NumberUtilsTest");
	failed_class_map.put("Lang_26", "org.apache.commons.lang3.time.FastDateFormatTest");
	failed_class_map.put("Lang_33", "org.apache.commons.lang3.ClassUtilsTest");
	failed_class_map.put("Lang_38", "org.apache.commons.lang3.time.FastDateFormatTest");
	failed_class_map.put("Lang_39", "org.apache.commons.lang3.StringUtilsTest");
	failed_class_map.put("Lang_43", "org.apache.commons.lang.text.TextTestSuite");
	failed_class_map.put("Lang_44", "org.apache.commons.lang.NumberUtilsTest");
	failed_class_map.put("Lang_45", "org.apache.commons.lang.WordUtilsTest");
	failed_class_map.put("Lang_49", "org.apache.commons.lang.math.MathTestSuite");
	failed_class_map.put("Lang_51", "org.apache.commons.lang.BooleanUtilsTest");
	failed_class_map.put("Lang_54", "org.apache.commons.lang.LocaleUtilsTest");
	failed_class_map.put("Lang_55", "org.apache.commons.lang.time.StopWatchTest");
	failed_class_map.put("Lang_57", "org.apache.commons.lang.LocaleUtilsTest");
	failed_class_map.put("Lang_58", "org.apache.commons.lang.math.NumberUtilsTest");
	failed_class_map.put("Lang_59", "org.apache.commons.lang.text.StrBuilderAppendInsertTest");
	failed_class_map.put("Math_2", "org.apache.commons.math3.distribution.HypergeometricDistributionTest");
	failed_class_map.put("Math_3", "org.apache.commons.math3.util.MathArraysTest");	
	failed_class_map.put("Math_5", "org.apache.commons.math3.complex.ComplexTest");
	failed_class_map.put("Math_9", "org.apache.commons.math3.geometry.euclidean.threed.LineTest");
	failed_class_map.put("Math_10", "org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest");
	failed_class_map.put("Math_11", "org.apache.commons.math3.distribution.MultivariateNormalDistributionTest");
	failed_class_map.put("Math_27", "org.apache.commons.math3.fraction.FractionTest");
	failed_class_map.put("Math_28", "org.apache.commons.math3.optimization.linear.SimplexSolverTest");
	failed_class_map.put("Math_32", "org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSetTest");
	failed_class_map.put("Math_33", "org.apache.commons.math3.optimization.linear.SimplexSolverTest");
	failed_class_map.put("Math_34", "org.apache.commons.math3.genetics.ListPopulationTest");
	failed_class_map.put("Math_39", "org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest");	
	failed_class_map.put("Math_41", "org.apache.commons.math.stat.descriptive.moment.VarianceTest");
	failed_class_map.put("Math_48", "org.apache.commons.math.analysis.solvers.RegulaFalsiSolverTest");
	failed_class_map.put("Math_49", "org.apache.commons.math.linear.SparseRealVectorTest");
	failed_class_map.put("Math_50", "org.apache.commons.math.analysis.solvers.RegulaFalsiSolverTest");
	failed_class_map.put("Math_53", "org.apache.commons.math.complex.ComplexTest");
	failed_class_map.put("Math_57", "org.apache.commons.math.stat.clustering.KMeansPlusPlusClustererTest");	
	failed_class_map.put("Math_58", "org.apache.commons.math.optimization.fitting.GaussianFitterTest");
	failed_class_map.put("Math_59", "org.apache.commons.math.util.FastMathTest");
	failed_class_map.put("Math_69", "org.apache.commons.math.stat.correlation.PearsonsCorrelationTest");
	failed_class_map.put("Math_70", "org.apache.commons.math.analysis.solvers.BisectionSolverTest");
	failed_class_map.put("Math_71", "org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaIntegratorTest");
	failed_class_map.put("Math_75", "org.apache.commons.math.stat.FrequencyTest");
	failed_class_map.put("Math_79", "org.apache.commons.math.stat.clustering.KMeansPlusPlusClustererTest");
	failed_class_map.put("Math_80", "org.apache.commons.math.linear.EigenDecompositionImplTest");
	failed_class_map.put("Math_82", "org.apache.commons.math.optimization.linear.SimplexSolverTest");
	failed_class_map.put("Math_85", "org.apache.commons.math.distribution.NormalDistributionTest");	
	failed_class_map.put("Math_94", "org.apache.commons.math.util.MathUtilsTest");
	failed_class_map.put("Math_101", "org.apache.commons.math.complex.ComplexFormatTest");
	failed_class_map.put("Math_105", "org.apache.commons.math.stat.regression.SimpleRegressionTest");

	failed_class_map.put("Time_4", "org.joda.time.TestPartial_Basics");
	failed_class_map.put("Time_14", "org.joda.time.TestMonthDay_Basics");
	failed_class_map.put("Time_16", "org.joda.time.format.TestDateTimeFormatter");
	failed_class_map.put("Time_19", "org.joda.time.TestDateTimeZoneCutover");
	failed_class_map.put("Time_24", "org.joda.time.format.TestDateTimeFormatter");
	failed_class_map.put("Time_27", "org.joda.time.format.TestPeriodFormatterBuilder");

	//======================
    }

    public PatchGenerator(String bug_id, String ssfix_dpath, String proj_dpath, String proj_testbuild_dpath, String dependjpath, String tsuitefpath, String failed_testcases, boolean delete_failed_patch, int parallelgranularity) {
	this.bug_id = bug_id;
	this.ssfix_dpath = ssfix_dpath;
	this.proj_dpath = proj_dpath;
	this.proj_testbuild_dpath = proj_testbuild_dpath;
	this.dependjpath = dependjpath;
	this.tsuite_fpath = tsuitefpath;
	this.failed_testcases = failed_testcases;
	this.delete_failed_patch = delete_failed_patch;
	this.parallel_granularity = parallelgranularity;
	if (parallel_granularity <= 0) { parallel_granularity = 1; }
	else if (parallel_granularity >= 1024) { parallel_granularity = 1024; }
	pgen0 = new PatchGenerator0();
	pval = new PatchValidator(bug_id, proj_testbuild_dpath, dependjpath, tsuite_fpath, ssfix_dpath);
	tested_pset = new HashSet<String>();
    }

    public Patch generatePatch(BuggyChunk bchunk, CandidateChunk cchunk, String fix_dpath) {
	return generatePatch(bchunk, cchunk, fix_dpath, false, false, false);
    }
    
    public Patch generatePatch(BuggyChunk bchunk, CandidateChunk cchunk, String fix_dpath, boolean print_renaming, boolean print_ccmatching, boolean print_patches) {
    
	//Generate all candidate patches
	System.err.println("Generating Initial Patches ...");
	List<Modification> mod_list = pgen0.generatePatches(bchunk, cchunk, print_renaming, print_ccmatching, print_patches);
	if (mod_list.isEmpty()) { return new Patch(null, false); }
	int mod_list_size = mod_list.size();
	System.err.println("Done Generating "+mod_list_size+" Initial Patches.");

	//Validate the patches
	String bfpath = bchunk.getFilePath();
	File bf = new File(bfpath);
	String bfname = bf.getName();
	String bfctnt = bchunk.getFileContent();
	if (bfctnt == null) { return new Patch(null, false); }

	int tested_num = 0;
	if (failed_testcases == null) { //HOW TO IMPROVE?
	    System.err.println("Failed Class Not Available for " + bug_id);
	    return new Patch(null, false);
	}
	List<String> patch_text_list0 = new ArrayList<String>();

	//First, sort the generated patches.
	System.err.println("Sorting the Initial Patches based on their Modification Sizes");
	Collections.sort(mod_list);
	System.err.println("Done sorting the Initial Patches.");
	    

	//Second, generate a list of patch texts to be validated.
	Set<String> tested_pset_local = new HashSet<String>();
	for (int i=0; i<mod_list_size; i++) {
	    String patch_text = null;
	    ASTRewrite rw = mod_list.get(i).getASTRewrite();
	    Document bdoc = new Document(bfctnt);
	    try {
		rw.rewriteAST(bdoc, null).apply(bdoc);
		patch_text = bdoc.get();
	    } catch (Throwable t) {
		System.err.println("Failed in generating patch for the ASTRewrite:");
		System.err.println(rw.toString());
	    }
	    if (patch_text == null) { continue; }
	    String patch_text_nowhitespace = patch_text.replaceAll("\\s+","");
	    if (tested_pset.contains(patch_text_nowhitespace)) {
		continue; //Tested Before
	    }
	    boolean new_patch = tested_pset_local.add(patch_text_nowhitespace);
	    if (new_patch) {
		patch_text_list0.add(patch_text);
	    }
	}
	List<String> patch_text_list = null;
	int patch_text_list0_size = patch_text_list0.size();
	int validated_num = -1;
	//Only consider the TOP 50 (MAX_PATCHES_TO_BE_VALIDATED)
	if (patch_text_list0_size > MAX_PATCHES_TO_BE_VALIDATED) {
	    patch_text_list = patch_text_list0.subList(0, MAX_PATCHES_TO_BE_VALIDATED);
	    validated_num = MAX_PATCHES_TO_BE_VALIDATED;
	}
	else {
	    patch_text_list = patch_text_list0;
	    validated_num = patch_text_list0_size;
	}
	for (String patch_text : patch_text_list) {
	    //Add to the global patch set
	    tested_pset.add(patch_text.replaceAll("\\s+",""));
	}
	
	System.err.println("Generated "+validated_num+" Patches to be Validated.");
	
	//Third, validate each patch.
	Patch rslt_patch0 = null;
	if (parallel_granularity == 1) {
	    rslt_patch0 = validatePatches0(patch_text_list, fix_dpath, bfpath, bfname);
	}
	else { 
	    rslt_patch0 = validatePatches1(patch_text_list, fix_dpath, bfpath, bfname); 
	}
	if (rslt_patch0.isCorrect()) {
	    return rslt_patch0;
	}

	Patch rslt_patch = new Patch(null, false);
	if (patch_text_list0_size > MAX_PATCHES_TO_BE_VALIDATED) {
	    rslt_patch.setTestedNum(MAX_PATCHES_TO_BE_VALIDATED);
	} else {
	    rslt_patch.setTestedNum(patch_text_list0_size);
	}
	return rslt_patch;
    }

    /* Validate patches in sequential order */
    private Patch validatePatches0(List<String> patch_text_list, String fix_dpath, String bfpath, String bfname) {

	int tested_num = 0;
	int patch_text_list_size = patch_text_list.size();
	for (int i=0; i<patch_text_list_size; i++) {
	    tested_num++;
	    String patch_dpath = fix_dpath+"/p"+i;
	    String patch_fpath = patch_dpath+"/"+bfname;
	    File patch_d = new File(patch_dpath);
	    if (!patch_d.exists()) { patch_d.mkdirs(); }
	    Patch patch = pval.validate(patch_text_list.get(i), patch_fpath, patch_dpath, failed_testcases);
	    if (patch != null && patch.isCorrect()) {
		patch.setTestedNum(tested_num);
		return patch;
	    }
	    else {
		if (delete_failed_patch) {
		    try { FileUtils.deleteDirectory(patch_d); }
		    catch (IOException e) {
			System.err.println(e);
			e.printStackTrace();
		    }
		}
	    }
	}
	return new Patch(null, false, tested_num);
    }

    /* Validate patches with parallelism */
    private Patch validatePatches1(List<String> patch_text_list, String fix_dpath, String bfpath, String bfname) {

	int para_gran = parallel_granularity;

	//Init validate runners
	int patch_text_list_size = patch_text_list.size();
	if (patch_text_list_size < para_gran) {
	    Patch patch = validatePatches1Helper(patch_text_list, 0, patch_text_list_size, fix_dpath, bfpath, bfname);
	    patch.setTestedNum(patch_text_list_size);
	    return patch;
	}
	else {
	    for (int i=0; i<patch_text_list_size; ) {
		Patch patch = null;
		if (i+para_gran <= patch_text_list_size) {
		    patch = validatePatches1Helper(patch_text_list, i, i+para_gran, fix_dpath, bfpath, bfname);
		    if (patch.isCorrect()) {
			patch.setTestedNum((i+1)*para_gran);
			return patch;
		    }
		}
		else {
		    patch = validatePatches1Helper(patch_text_list, i, patch_text_list_size, fix_dpath, bfpath, bfname);
		    if (patch.isCorrect()) {
			patch.setTestedNum(patch_text_list_size);
			return patch;
		    }
		}
		i += para_gran;
	    }

	    return new Patch(null, false, patch_text_list_size);
	}
    }

    private Patch validatePatches1Helper(List<String> patch_text_list, int start, int end, String fix_dpath, String bfpath, String bfname) {

	ExecutorService exe_service = Executors.newFixedThreadPool(parallel_granularity);
	List<Callable<Patch>> call_list = new ArrayList<Callable<Patch>>();
	for (int i=start; i<end; i++) {
	    String patch_dpath = fix_dpath+"/p"+i;
	    File patch_d = new File(patch_dpath);
	    if (!patch_d.exists()) { patch_d.mkdirs(); }
	    String patch_fpath = patch_dpath+"/"+bfname;
	    ValidatorRunner vrunner = new ValidatorRunner(patch_text_list.get(i), patch_fpath, patch_dpath, failed_testcases);
	    call_list.add(vrunner);
	}

	System.err.println("To validate patches from " + start + " to " + end);
	
	List<Future<Patch>> patch_future_list = null;
	try { patch_future_list = exe_service.invokeAll(call_list); }
	catch (Throwable t) {
	    System.err.println("Patch validating error.");
	    System.err.println(t);
	    t.printStackTrace();
	    exe_service.shutdownNow();
	}
	if (!exe_service.isShutdown()) {
	    exe_service.shutdown();
	}
	
	if (patch_future_list == null) { return new Patch(null, false); }
	for (Future<Patch> patch_future : patch_future_list) {
	    Patch patch = null;
	    try { patch = patch_future.get(); }
	    catch (Throwable t) {
		System.err.println("Error in getting patch from future.");
		System.err.println(t);
		t.printStackTrace();
	    }
	    if (patch != null && patch.isCorrect()) {
		return patch;
	    }
	}

	return new Patch(null, false);
    }
    
    private class ValidatorRunner implements Callable<Patch>
    {
	String patch_text, patch_fpath, patch_dpath, failed_testcases;

	public ValidatorRunner(String patch_text, String patch_fpath, String patch_dpath, String failed_testcases) {
	    this.patch_text = patch_text;
	    this.patch_fpath = patch_fpath;
	    this.patch_dpath = patch_dpath;
	    this.failed_testcases = failed_testcases;
	}

	@Override public Patch call() {
	    Patch patch = pval.validate(patch_text, patch_fpath, patch_dpath, failed_testcases);
	    if (!patch.isCorrect() && delete_failed_patch) {
		File patch_f = new File(patch_fpath);
		try { FileUtils.deleteDirectory(patch_f.getParentFile()); }
		catch (Throwable t) {
		    System.err.println(t);
		    t.printStackTrace();
		}
	    }
	    return patch;
	}
    }
}
