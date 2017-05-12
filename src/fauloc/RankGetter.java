package fauloc;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import org.apache.commons.io.FileUtils;


public class RankGetter
{
    public static Map<String, Set<String>> fault_map;

    static {
	fault_map = new HashMap<String, Set<String>>();

	Set<String> chart_1_set = new HashSet<String>();
	chart_1_set.add("org.jfree.chart.renderer.category.AbstractCategoryItemRenderer,1797");
	fault_map.put("Chart_1", chart_1_set);

	Set<String> chart_4_set = new HashSet<String>();
	chart_4_set.add("org.jfree.chart.plot.XYPlot,4493");
	fault_map.put("Chart_4", chart_4_set);

	Set<String> chart_8_set = new HashSet<String>();
	chart_8_set.add("org.jfree.data.time.Week,175");
	fault_map.put("Chart_8", chart_8_set);

	Set<String> chart_12_set = new HashSet<String>();
	chart_12_set.add("org.jfree.chart.plot.MultiplePiePlot,145");
	fault_map.put("Chart_12", chart_12_set);

	//The defective statement span across 5 lines, so there are 5 items.
	Set<String> chart_13_set = new HashSet<String>();
	chart_13_set.add("org.jfree.chart.block.BorderArrangement,493");
	chart_13_set.add("org.jfree.chart.block.BorderArrangement,494");
	chart_13_set.add("org.jfree.chart.block.BorderArrangement,495");
	chart_13_set.add("org.jfree.chart.block.BorderArrangement,496");
	chart_13_set.add("org.jfree.chart.block.BorderArrangement,497");
	fault_map.put("Chart_13", chart_13_set);

	Set<String> chart_17_set = new HashSet<String>();
	chart_17_set.add("org.jfree.data.time.TimeSeries,857");
	fault_map.put("Chart_17", chart_17_set);
	
	Set<String> chart_20_set = new HashSet<String>();
	chart_20_set.add("org.jfree.chart.plot.ValueMarker,95");
	fault_map.put("Chart_20", chart_20_set);

	Set<String> chart_24_set = new HashSet<String>();
	chart_24_set.add("org.jfree.chart.renderer.GrayPaintScale,126");
	chart_24_set.add("org.jfree.chart.renderer.GrayPaintScale,127");
	fault_map.put("Chart_24", chart_24_set);

	Set<String> chart_26_set = new HashSet<String>();
	chart_26_set.add("org.jfree.chart.axis.Axis,1191");
	chart_26_set.add("org.jfree.chart.axis.Axis,1192");
	fault_map.put("Chart_26", chart_26_set);

	Set<String> closure_1_set = new HashSet<String>();
	closure_1_set.add("com.google.javascript.jscomp.RemoveUnusedVars,372");
	fault_map.put("Closure_1", closure_1_set);

	Set<String> closure_5_set = new HashSet<String>();
	closure_5_set.add("com.google.javascript.jscomp.InlineObjectLiterals,169");
	closure_5_set.add("com.google.javascript.jscomp.InlineObjectLiterals,182");
	fault_map.put("Closure_5", closure_5_set);

	Set<String> closure_7_set = new HashSet<String>();
	closure_7_set.add("com.google.javascript.jscomp.type.ChainableReverseAbstractInterpreter,613");
	fault_map.put("Closure_7", closure_7_set);

	Set<String> closure_10_set = new HashSet<String>();
	closure_10_set.add("com.google.javascript.jscomp.NodeUtil,1417");
	fault_map.put("Closure_10", closure_10_set);

	Set<String> closure_11_set = new HashSet<String>();
	closure_11_set.add("com.google.javascript.jscomp.TypeCheck,1319");
	fault_map.put("Closure_11", closure_11_set);
	
	Set<String> closure_14_set = new HashSet<String>();
	closure_14_set.add("com.google.javascript.jscomp.ControlFlowAnalysis,767");
	closure_14_set.add("com.google.javascript.jscomp.ControlFlowAnalysis,766");
	fault_map.put("Closure_14", closure_14_set);

	Set<String> closure_15_set = new HashSet<String>();
	closure_15_set.add("com.google.javascript.jscomp.FlowSensitiveInlineVariables,98");
	closure_15_set.add("com.google.javascript.jscomp.FlowSensitiveInlineVariables,102");
	fault_map.put("Closure_15", closure_15_set);

	Set<String> closure_18_set = new HashSet<String>();
	closure_18_set.add("com.google.javascript.jscomp.Compiler,1288");
	fault_map.put("Closure_18", closure_18_set);

	Set<String> closure_19_set = new HashSet<String>();
	closure_19_set.add("com.google.javascript.jscomp.type.ChainableReverseAbstractInterpreter,170");
	closure_19_set.add("com.google.javascript.jscomp.type.ChainableReverseAbstractInterpreter,172");
	fault_map.put("Closure_19", closure_19_set);

	Set<String> closure_20_set = new HashSet<String>();
	closure_20_set.add("com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntax,215");
	fault_map.put("Closure_20", closure_20_set);

	Set<String> closure_31_set = new HashSet<String>();
	closure_31_set.add("com.google.javascript.jscomp.Compiler,1284");
	fault_map.put("Closure_31", closure_31_set);

	Set<String> closure_33_set = new HashSet<String>();
	closure_33_set.add("com.google.javascript.rhino.jstype.PrototypeObjectType,565");
	fault_map.put("Closure_33", closure_33_set);
	
	Set<String> closure_40_set = new HashSet<String>();
	closure_40_set.add("com.google.javascript.jscomp.NameAnalyzer,635");
	closure_40_set.add("com.google.javascript.jscomp.NameAnalyzer,634");
	closure_40_set.add("com.google.javascript.jscomp.NameAnalyzer,636");	
	fault_map.put("Closure_40", closure_40_set);

	Set<String> closure_42_set = new HashSet<String>();
	closure_42_set.add("com.google.javascript.jscomp.parsing.IRFactory,568");
	fault_map.put("Closure_42", closure_42_set);

	Set<String> closure_51_set = new HashSet<String>();
	closure_51_set.add("com.google.javascript.jscomp.CodeConsumer,241");
	fault_map.put("Closure_51", closure_51_set);

	Set<String> closure_52_set = new HashSet<String>();
	closure_52_set.add("com.google.javascript.jscomp.CodeGenerator,745");
	fault_map.put("Closure_52", closure_52_set);

	Set<String> closure_59_set = new HashSet<String>();
	closure_59_set.add("com.google.javascript.jscomp.Compiler,255");
	fault_map.put("Closure_59", closure_59_set);

	Set<String> closure_62_set = new HashSet<String>();
	closure_62_set.add(".com.google.javascript.jscompLightweightMessageFormatter,95");
	closure_62_set.add(".com.google.javascript.jscompLightweightMessageFormatter,96");
	fault_map.put("Closure_62", closure_62_set);
	
	Set<String> closure_65_set = new HashSet<String>();
	closure_65_set.add("com.google.javascript.jscomp.CodeGenerator,1015");
	fault_map.put("Closure_65", closure_65_set);

	Set<String> closure_66_set = new HashSet<String>();
	closure_66_set.add("com.google.javascript.jscomp.TypeCheck,514");
	closure_66_set.add("com.google.javascript.jscomp.TypeCheck,515");
	fault_map.put("Closure_66", closure_66_set);

	Set<String> closure_70_set = new HashSet<String>();
	closure_70_set.add("com.google.javascript.jscomp.TypedScopeCreator,1745");
	closure_70_set.add("com.google.javascript.jscomp.TypedScopeCreator,1744");
	closure_70_set.add("com.google.javascript.jscomp.TypedScopeCreator,1743");
	fault_map.put("Closure_70", closure_70_set);

	Set<String> closure_71_set = new HashSet<String>();
	closure_71_set.add("com.google.javascript.jscomp.CheckAccessControls,416");
	fault_map.put("Closure_71", closure_71_set);
	
	Set<String> closure_73_set = new HashSet<String>();
	closure_73_set.add("com.google.javascript.jscomp.CodeGenerator,1045");
	closure_73_set.add("com.google.javascript.jscomp.CodeGenerator,1046");
	fault_map.put("Closure_73", closure_73_set);

	Set<String> closure_77_set = new HashSet<String>();
	closure_77_set.add("com.google.javascript.jscomp.CodeGenerator,966");
	closure_77_set.add("com.google.javascript.jscomp.CodeGenerator,967");
	closure_77_set.add("com.google.javascript.jscomp.CodeGenerator,968");
	fault_map.put("Closure_77", closure_77_set);

	Set<String> closure_82_set = new HashSet<String>();
	closure_82_set.add("com.google.javascript.rhino.jstype.JSType,163");
	fault_map.put("Closure_82", closure_82_set);
	
	Set<String> closure_86_set = new HashSet<String>();
	closure_86_set.add("com.google.javascript.jscomp.NodeUtil,2462");
	closure_86_set.add("com.google.javascript.jscomp.NodeUtil,2461");
	closure_86_set.add("com.google.javascript.jscomp.NodeUtil,2463");
	fault_map.put("Closure_86", closure_86_set);

	Set<String> closure_92_set = new HashSet<String>();
	closure_92_set.add("com.google.javascript.jscomp.ProcessClosurePrimitives.java,789");
	fault_map.put("Closure_92", closure_92_set);

	Set<String> closure_97_set = new HashSet<String>();
	closure_97_set.add("com.google.javascript.jscomp.PeepholeFoldConstants,695");
	fault_map.put("Closure_97", closure_97_set);

	Set<String> closure_104_set = new HashSet<String>();
	closure_104_set.add("com.google.javascript.rhino.jstype.UnionType,292");
	fault_map.put("Closure_104", closure_104_set);

	Set<String> closure_113_set = new HashSet<String>();
	closure_113_set.add("com.google.javascript.jscomp.ProcessClosurePrimitives,329");
	fault_map.put("Closure_113", closure_113_set);

	Set<String> closure_119_set = new HashSet<String>();
	closure_119_set.add("com.google.javascript.jscomp.GlobalNamespace,365");
	closure_119_set.add("com.google.javascript.jscomp.GlobalNamespace,366");
	fault_map.put("Closure_119", closure_119_set);

	Set<String> closure_120_set = new HashSet<String>();
	closure_120_set.add("com.google.javascript.jscomp.ReferenceCollectingCallback,431");
	fault_map.put("Closure_120", closure_120_set);

	Set<String> closure_126_set = new HashSet<String>();
	closure_126_set.add("com.google.javascript.jscomp.MinimizeExitPoints,137");
	fault_map.put("Closure_126", closure_126_set);

	Set<String> closure_129_set = new HashSet<String>();
	closure_129_set.add("com.google.javascript.jscomp.PrepareAst,163");
	closure_129_set.add("com.google.javascript.jscomp.PrepareAst,164");
	fault_map.put("Closure_129", closure_129_set);

	Set<String> closure_130_set = new HashSet<String>();
	closure_130_set.add("com.google.javascript.jscomp.CollapseProperties,172");
	fault_map.put("Closure_130", closure_130_set);

	Set<String> closure_132_set = new HashSet<String>();
	closure_132_set.add("com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntax,777");
	closure_132_set.add("com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntax,782");
	fault_map.put("Closure_132", closure_132_set);
	
	
	Set<String> lang_2_set = new HashSet<String>();
	lang_2_set.add("org.apache.lang3.LocaleUtils,89");
	lang_2_set.add("org.apache.lang3.LocaleUtils,92");
	fault_map.put("Lang_2", lang_2_set);
	
	Set<String> lang_6_set = new HashSet<String>();
	lang_6_set.add("org.apache.commons.lang3.text.translate.CharSequenceTranslator,95");
	lang_6_set.add("org.apache.commons.lang3.text.translate.CharSequenceTranslator,94");
	fault_map.put("Lang_6", lang_6_set);

	Set<String> lang_9_set = new HashSet<String>();
	lang_9_set.add("org.apache.commons.lang3.time.FastDateParser,130");
	lang_9_set.add("org.apache.commons.lang3.time.FastDateParser,144");
	fault_map.put("Lang_9", lang_9_set);

	Set<String> lang_11_set = new HashSet<String>();
	lang_11_set.add("org.apache.commons.lang3.RandomStringUtils,234");
	fault_map.put("Lang_11", lang_11_set);
	
	Set<String> lang_16_set = new HashSet<String>();
	lang_16_set.add("org.apache.commons.lang3.math.NumberUtils,458");
	lang_16_set.add("org.apache.commons.lang3.math.NumberUtils,459");
	fault_map.put("Lang_16", lang_16_set);

	Set<String> lang_21_set = new HashSet<String>();
	lang_21_set.add("org.apache.commons.lang3.time.DateUtils,262");
	lang_21_set.add("org.apache.commons.lang3.time.DateUtils,263");
	lang_21_set.add("org.apache.commons.lang3.time.DateUtils,264");
	lang_21_set.add("org.apache.commons.lang3.time.DateUtils,265");
	lang_21_set.add("org.apache.commons.lang3.time.DateUtils,266");
	lang_21_set.add("org.apache.commons.lang3.time.DateUtils,267");
	lang_21_set.add("org.apache.commons.lang3.time.DateUtils,268");
	lang_21_set.add("org.apache.commons.lang3.time.DateUtils,269");
	fault_map.put("Lang_21", lang_21_set);

	Set<String> lang_24_set = new HashSet<String>();
	lang_24_set.add("org.apache.commons.lang3.math.NumberUtils,1413");
	fault_map.put("Lang_24", lang_24_set);

	Set<String> lang_26_set = new HashSet<String>();
	lang_26_set.add("org.apache.commons.lang3.time.FastDateFormat,820");
	fault_map.put("Lang_26", lang_26_set);
	
	Set<String> lang_33_set = new HashSet<String>();
	lang_33_set.add("org.apache.commons.lang3.ClassUtils,909");
	lang_33_set.add("org.apache.commons.lang3.ClassUtils,910");
	fault_map.put("Lang_33", lang_33_set);

	Set<String> lang_38_set = new HashSet<String>();
	lang_38_set.add("org.apache.commons.lang3.time.FastDateFormat,872");
	fault_map.put("Lang_38", lang_38_set);

	Set<String> lang_39_set = new HashSet<String>();
	lang_39_set.add("org.apache.commons.lang3.StringUtils,3676");
	fault_map.put("Lang_39", lang_39_set);
	
	Set<String> lang_43_set = new HashSet<String>();
	lang_43_set.add("org.apache.commons.lang.text.ExtendedMessageFormat,421");
	lang_43_set.add("org.apache.commons.lang.text.ExtendedMessageFormat,422");
	fault_map.put("Lang_43", lang_43_set);

	Set<String> lang_44_set = new HashSet<String>();
	lang_44_set.add("org.apache.commons.lang.NumberUtils,142");
	lang_44_set.add("org.apache.commons.lang.NumberUtils,145");
	fault_map.put("Lang_44", lang_44_set);

	Set<String> lang_45_set = new HashSet<String>();
	lang_45_set.add("org.apache.commons.lang.WordUtils,616");
	lang_45_set.add("org.apache.commons.lang.WordUtils,610");
	fault_map.put("Lang_45", lang_45_set);
	
	Set<String> lang_51_set = new HashSet<String>();
	lang_51_set.add("org.apache.commons.lang.BooleanUtils,677");
	lang_51_set.add("org.apache.commons.lang.BooleanUtils,678");
	lang_51_set.add("org.apache.commons.lang.BooleanUtils,679");
	lang_51_set.add("org.apache.commons.lang.BooleanUtils,680");
	fault_map.put("Lang_51", lang_51_set);

	Set<String> lang_54_set = new HashSet<String>();
	lang_54_set.add("org.apache.commons.lang.LocaleUtils,113");
	lang_54_set.add("org.apache.commons.lang.LocaleUtils,114");
	fault_map.put("Lang_54", lang_54_set);

	Set<String> lang_55_set = new HashSet<String>();
	lang_55_set.add("org.apache.commons.lang.time.StopWatch,118");
	fault_map.put("Lang_55", lang_55_set);

	Set<String> lang_57_set = new HashSet<String>();
	lang_57_set.add("org.apache.commons.lang.LocaleUtils,223");
	fault_map.put("Lang_57", lang_57_set);

	Set<String> lang_58_set = new HashSet<String>();
	lang_58_set.add("org.apache.commons.lang.math.NumberUtils,452");
	fault_map.put("Lang_58", lang_58_set);
	
	Set<String> lang_59_set = new HashSet<String>();
	lang_59_set.add("org.apache.commons.lang.text.StrBuilder,884");
	lang_59_set.add("org.apache.commons.lang.text.StrBuilder,883");
	fault_map.put("Lang_59", lang_59_set);



	Set<String> math_2_set = new HashSet<String>();
	math_2_set.add("org.apache.commons.math3.distribution.HypergeometricDistribution,268");
	fault_map.put("Math_2", math_2_set);

	Set<String> math_3_set = new HashSet<String>();
	math_3_set.add("org.apache.commons.math3.util.MathArrays,817");
	math_3_set.add("org.apache.commons.math3.util.MathArrays,821");
	fault_map.put("Math_3", math_3_set);
	
	Set<String> math_5_set = new HashSet<String>();
	math_5_set.add("org.apache.commons.math3.complex.Complex,305");
	fault_map.put("Math_5", math_5_set);

	Set<String> math_9_set = new HashSet<String>();
	math_9_set.add("org.apache.commons.math3.geometry.euclidean.threed.Line,87");
	fault_map.put("Math_9", math_9_set);
	
	Set<String> math_10_set = new HashSet<String>();
	math_10_set.add("org.apache.commons.math3.analysis.differentiation.DSCompiler,1394");
	fault_map.put("Math_10", math_10_set);
	
	Set<String> math_11_set = new HashSet<String>();
	math_11_set.add("org.apache.commons.math3.distribution.MultivariateNormalDistribution,183");
	math_11_set.add("org.apache.commons.math3.distribution.MultivariateNormalDistribution,184");
	math_11_set.add("org.apache.commons.math3.distribution.MultivariateNormalDistribution,185");
	fault_map.put("Math_11", math_11_set);

	Set<String> math_27_set = new HashSet<String>();
	math_27_set.add("org.apache.commons.math3.fraction.Fraction,597");
	fault_map.put("Math_27", math_27_set);

	Set<String> math_32_set = new HashSet<String>();
	math_32_set.add("org.apache.commons.math3.geometry.euclidean.twod.PolygonsSet,135");
	fault_map.put("Math_32", math_32_set);

	Set<String> math_33_set = new HashSet<String>();
	math_33_set.add("org.apache.commons.math3.optimization.linear.SimplexTableau,338");
	fault_map.put("Math_33", math_33_set);

	Set<String> math_34_set = new HashSet<String>();
	math_34_set.add("org.apache.commons.math3.genetics.ListPopulation,208");
	fault_map.put("Math_34", math_34_set);

	Set<String> math_39_set = new HashSet<String>();
	math_39_set.add("org.apache.commons.math.ode.nonstiff.EmbeddedRungeKuttaIntegrator,249");
	math_39_set.add("org.apache.commons.math.ode.nonstiff.EmbeddedRungeKuttaIntegrator,252");
	fault_map.put("Math_39", math_39_set);
	
	Set<String> math_41_set = new HashSet<String>();
	math_41_set.add("org.apache.commons.math.stat.descriptive.moment.Variance,520");
	fault_map.put("Math_41", math_41_set);

	Set<String> math_48_set = new HashSet<String>();
	math_48_set.add("org.apache.commons.math.analysis.solvers.BaseSecantSolver,186");
	math_48_set.add("org.apache.commons.math.analysis.solvers.BaseSecantSolver,188");
	fault_map.put("Math_48", math_48_set);

	//This is not a simple bug
	/*
	Set<String> math_49_set = new HashSet<String>();
	math_49_set.add("org.apache.commons.math.linear.OpenMapRealVector,345");
	math_49_set.add("org.apache.commons.math.linear.OpenMapRealVector,358");
	math_49_set.add("org.apache.commons.math.linear.OpenMapRealVector,370");
	math_49_set.add("org.apache.commons.math.linear.OpenMapRealVector,383");
	fault_map.put("Math_49", math_49_set);
	*/
	
	Set<String> math_50_set = new HashSet<String>();
	math_50_set.add("org.apache.commons.math.analysis.solvers.BaseSecantSolver,186");
	fault_map.put("Math_50", math_50_set);
	
	Set<String> math_53_set = new HashSet<String>();
	math_53_set.add("org.apache.commons.math.complex.Complex,152");
	math_53_set.add("org.apache.commons.math.complex.Complex,153");
	fault_map.put("Math_53", math_53_set);

	Set<String> math_57_set = new HashSet<String>();
	math_57_set.add("org.apache.commons.math.stat.clustering.KMeansPlusPlusClusterer,175");
	fault_map.put("Math_57", math_57_set);
	
	Set<String> math_58_set = new HashSet<String>();
	math_58_set.add("org.apache.commons.math.optimization.fitting.GaussianFitter,120");
	math_58_set.add("org.apache.commons.math.optimization.fitting.GaussianFitter,121");
	fault_map.put("Math_58", math_58_set);

	Set<String> math_59_set = new HashSet<String>();
	math_59_set.add("org.apache.commons.math.util.FastMath,3482");
	fault_map.put("Math_59", math_59_set);

	Set<String> math_69_set = new HashSet<String>();
	math_69_set.add("org.apache.commons.math.stat.correlation.PearsonsCorrelation,171");
	fault_map.put("Math_69", math_69_set);
	
	Set<String> math_70_set = new HashSet<String>();
	math_70_set.add("org.apache.commons.math.analysis.solvers.BisectionSolver,72");
	fault_map.put("Math_70", math_70_set);

	Set<String> math_75_set = new HashSet<String>();
	math_75_set.add("org.apache.commons.math.stat.Frequency,303");
	fault_map.put("Math_75", math_75_set);
	
	Set<String> math_79_set = new HashSet<String>();
	math_79_set.add("org.apache.commons.math.util.MathUtils,1624");
	math_79_set.add("org.apache.commons.math.util.MathUtils,1626");
	fault_map.put("Math_79", math_79_set);

	Set<String> math_80_set = new HashSet<String>();
	math_80_set.add("org.apache.commons.math.linear.EigenDecompositionImpl,1135");
	fault_map.put("Math_80", math_80_set);

	Set<String> math_82_set = new HashSet<String>();
	math_82_set.add("org.apache.commons.math.optimization.linear.SimplexSolver,81");
	fault_map.put("Math_82", math_82_set);

	Set<String> math_85_set = new HashSet<String>();
	math_85_set.add("org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils,198");
	fault_map.put("Math_85", math_85_set);
	
	Set<String> math_94_set = new HashSet<String>();
	math_94_set.add("org.apache.commons.math.util.MathUtils,412");
	math_94_set.add("org.apache.commons.math.util.MathUtils,413");
	fault_map.put("Math_94", math_94_set);

	Set<String> math_96_set = new HashSet<String>();
	math_96_set.add("org.apache.commons.math.complex.Complex,258");
	math_96_set.add("org.apache.commons.math.complex.Complex,259");
	math_96_set.add("org.apache.commons.math.complex.Complex,260");
	math_96_set.add("org.apache.commons.math.complex.Complex,261");	
	fault_map.put("Math_96", math_96_set);
	
	Set<String> math_101_set = new HashSet<String>();
	math_101_set.add("org.apache.commons.math.complex.ComplexFormat,377");
	fault_map.put("Math_101", math_101_set);

	Set<String> math_105_set = new HashSet<String>();
	math_105_set.add("org.apache.commons.math.stat.regression.SimpleRegression,264");
	fault_map.put("Math_105", math_105_set);

	Set<String> time_4_set = new HashSet<String>();
	time_4_set.add("org.joda.time.Partial,463");
	fault_map.put("Time_4", time_4_set);

	Set<String> time_14_set = new HashSet<String>();
	time_14_set.add("org.joda.time.chrono.BasicMonthOfYearDateTimeField,206");
	time_14_set.add("org.joda.time.chrono.BasicMonthOfYearDateTimeField,209");
	fault_map.put("Time_14", time_14_set);

	Set<String> time_16_set = new HashSet<String>();
	time_16_set.add("org.joda.time.format.DateTimeFormatter,708");
	fault_map.put("Time_16", time_16_set);

	Set<String> time_19_set = new HashSet<String>();
	time_19_set.add("org.joda.time.DateTimeZone,900");
	fault_map.put("Time_19", time_19_set);

	Set<String> time_24_set = new HashSet<String>();
	time_24_set.add("org.joda.time.format.DateTimeParserBucket,352");
	fault_map.put("Time_24", time_24_set);

	Set<String> time_27_set = new HashSet<String>();
	time_27_set.add("org.joda.time.format.PeriodFormatterBuilder,801");
	fault_map.put("Time_27", time_27_set);
    }

    public static void main(String[] args) {
	String bug_id = args[0];
	String rslt_fpath = args[1];
	String flag = args[2];
	boolean count_stacktrace = false;
	if ("0".equals(flag)) { count_stacktrace = true; }

	File rslt_f = new File(rslt_fpath);
	int rank = getRank(bug_id, rslt_f, count_stacktrace);
	System.out.println(bug_id+","+rank);
    }
    
    public static int getRank(String bug_id, File rslt_f, boolean count_stacktrace) {
	List<String> rslt_lines = null;
	try { rslt_lines = FileUtils.readLines(rslt_f, (String) null); }
	catch (Throwable t) { System.err.println(t); t.printStackTrace(); }
	if (rslt_lines == null) { return -1; }

	Set<String> fault_item_set = fault_map.get(bug_id);
	if (fault_item_set == null) {
	    System.err.println("No Truly Faulty Lines Specified for " + bug_id);
	    return -1;
	}
	
	int curr_rank = 0;
	for (String rslt_line0 : rslt_lines) {
	    String rslt_line = rslt_line0.trim();
	    if (rslt_line.startsWith("Suspicious line:")) {
		int i0 = rslt_line.indexOf(":");		
		String[] susp_items = rslt_line.substring(i0+1).split(",");
		String susp_item0 = susp_items[0];
		int susp_item0_i0 = susp_item0.indexOf("$");
		if (susp_item0_i0 != -1) {
		    susp_item0 = susp_item0.substring(0, susp_item0_i0);
		}
		String susp_item = susp_item0+","+susp_items[1];
		if (fault_item_set.contains(susp_item)) {
		    return curr_rank;
		}
		curr_rank += 1;
	    }
	    else if (count_stacktrace && rslt_line.startsWith("at ")) {
		int i0 = rslt_line.indexOf("(");
		int i1 = rslt_line.indexOf(")");
		String full_class_name = rslt_line.substring(3,i0);
		int item0_i0 = full_class_name.indexOf("$");
		if (item0_i0 != -1) {
		    full_class_name = full_class_name.substring(0, item0_i0);
		}
		full_class_name = full_class_name.substring(0, full_class_name.lastIndexOf("."));

		String lnumber = rslt_line.substring(i0+1, i1);
		int i2 = lnumber.indexOf(":");
		if (i2 != -1) {
		    lnumber = lnumber.substring(i2+1);
		    String susp_item = full_class_name+","+lnumber;
		    if (fault_item_set.contains(susp_item)) {
			return curr_rank;
		    }
		    else {
			if ((susp_item.startsWith("org.jfree") ||
			     susp_item.startsWith("com.google.javascript") ||
			     susp_item.startsWith("org.apache.commons.lang") ||
			     susp_item.startsWith("org.apache.commons.math")) &&
			    (!full_class_name.endsWith("Test") &&
			     !full_class_name.endsWith("Tests"))) {
			    curr_rank += 1;
			}
		    }
		}
	    }
	}

	return -1;
    }
}
