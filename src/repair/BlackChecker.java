package edu.brown.cs.ssfix.repair;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class BlackChecker
{
    public static Map<String, Map<String, List<LineRange>>> black_map;

    static {
	black_map = new HashMap<String, Map<String, List<LineRange>>>();

	Map<String, List<LineRange>> chart_1_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_c1 = new ArrayList<LineRange>();
	lr_list_c1.add(new LineRange(1562, 1590));
	chart_1_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Chart_8_buggy/source/org/jfree/chart/renderer/category/AbstractCategoryItemRenderer.java", lr_list_c1);
	black_map.put("Chart_1", chart_1_map);

	Map<String, List<LineRange>> chart_4_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_c4 = new ArrayList<LineRange>();
	lr_list_c4.add(new LineRange(3971, 4029));
	chart_4_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Chart_8_buggy/source/org/jfree/chart/plot/XYPlot.java", lr_list_c4);
	black_map.put("Chart_4", chart_4_map);

	Map<String, List<LineRange>> chart_8_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_c8 = new ArrayList<LineRange>();
	lr_list_c8.add(new LineRange(164, 176));
	chart_8_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Chart_8_buggy/source/org/jfree/data/time/Week.java", lr_list_c8);
	black_map.put("Chart_8", chart_8_map);

	Map<String, List<LineRange>> chart_17_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_c17 = new ArrayList<LineRange>();
	lr_list_c17.add(new LineRange(843, 862));
	chart_17_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Chart_8_buggy/source/org/jfree/data/time/TimeSeries.java", lr_list_c17);
	black_map.put("Chart_17", chart_17_map);
	
	
	Map<String, List<LineRange>> chart_20_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_c20 = new ArrayList<LineRange>();
	lr_list_c20.add(new LineRange(83, 97));
	chart_20_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Chart_8_buggy/source/org/jfree/chart/plot/ValueMarker.java", lr_list_c20);
	black_map.put("Chart_20", chart_20_map);


	Map<String, List<LineRange>> chart_24_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_c24 = new ArrayList<LineRange>();
	lr_list_c24.add(new LineRange(113, 127));
	chart_24_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Chart_8_buggy/source/org/jfree/chart/renderer/GrayPaintScale.java", lr_list_c24);
	black_map.put("Chart_24", chart_24_map);


	Map<String, List<LineRange>> closure_1_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl1 = new ArrayList<LineRange>();
	lr_list_cl1.add(new LineRange(354, 389));
	closure_1_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/RemoveUnusedVars.java", lr_list_cl1);
	black_map.put("Closure_1", closure_1_map);
	

	Map<String, List<LineRange>> closure_5_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl5 = new ArrayList<LineRange>();
	lr_list_cl5.add(new LineRange(147, 250));
	closure_5_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/InlineObjectLiterals.java", lr_list_cl5);
	black_map.put("Closure_5", closure_5_map);


	Map<String, List<LineRange>> closure_7_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl7 = new ArrayList<LineRange>();
	lr_list_cl7.add(new LineRange(606, 613));
	closure_7_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/type/ChainableReverseAbstractInterpreter.java", lr_list_cl7);
	black_map.put("Closure_7", closure_7_map);


	Map<String, List<LineRange>> closure_10_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl10 = new ArrayList<LineRange>();
	lr_list_cl10.add(new LineRange(1392, 1398));
	closure_10_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/NodeUtil.java", lr_list_cl10);
	black_map.put("Closure_10", closure_10_map);


	/*
	Map<String, List<LineRange>> closure_11_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl11 = new ArrayList<LineRange>();
	lr_list_cl11.add(new LineRange(, ));
	closure_11_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/", lr_list_cl11);
	black_map.put("Closure_11", closure_11_map);
	*/
	
	
	Map<String, List<LineRange>> closure_14_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl14 = new ArrayList<LineRange>();
	lr_list_cl14.add(new LineRange(682, 789));
	closure_14_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/ControlFlowAnalysis.java", lr_list_cl14);
	black_map.put("Closure_14", closure_14_map);


	Map<String, List<LineRange>> closure_15_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl15 = new ArrayList<LineRange>();
	lr_list_cl15.add(new LineRange(86, 118));
	closure_15_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/FlowSensitiveInlineVariables.java", lr_list_cl15);
	black_map.put("Closure_15", closure_15_map);


	Map<String, List<LineRange>> closure_18_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl18 = new ArrayList<LineRange>();
	lr_list_cl18.add(new LineRange(1234, 1362));
	closure_18_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/Compiler.java", lr_list_cl18);
	black_map.put("Closure_18", closure_18_map);


	Map<String, List<LineRange>> closure_19_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl19 = new ArrayList<LineRange>();
	lr_list_cl19.add(new LineRange(152, 180));
	closure_19_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/type/ChainableReverseAbstractInterpreter.java", lr_list_cl19);
	black_map.put("Closure_19", closure_19_map);


	Map<String, List<LineRange>> closure_20_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl20 = new ArrayList<LineRange>();
	lr_list_cl20.add(new LineRange(208, 231));
	closure_20_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/PeepholeSubstituteAlternateSyntax.java", lr_list_cl20);
	black_map.put("Closure_20", closure_20_map);


	Map<String, List<LineRange>> closure_31_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl31 = new ArrayList<LineRange>();
	lr_list_cl31.add(new LineRange(1287, 1295));
	closure_31_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/Compiler.java", lr_list_cl31);
	black_map.put("Closure_31", closure_31_map);


	Map<String, List<LineRange>> closure_33_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl33 = new ArrayList<LineRange>();
	lr_list_cl33.add(new LineRange(555, 584));
	closure_33_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/rhino/jstype/PrototypeObjectType.java", lr_list_cl33);
	black_map.put("Closure_33", closure_33_map);
	

	Map<String, List<LineRange>> closure_40_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl40 = new ArrayList<LineRange>();
	lr_list_cl40.add(new LineRange(619, 639));
	closure_40_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/NameAnalyzer.java", lr_list_cl40);
	black_map.put("Closure_40", closure_40_map);


	Map<String, List<LineRange>> closure_42_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl42 = new ArrayList<LineRange>();
	lr_list_cl42.add(new LineRange(583, 599));
	closure_42_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/parsing/IRFactory.java", lr_list_cl42);
	black_map.put("Closure_42", closure_42_map);


	Map<String, List<LineRange>> closure_51_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl51 = new ArrayList<LineRange>();
	lr_list_cl51.add(new LineRange(240, 276));
	closure_51_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/CodeConsumer.java", lr_list_cl51);
	black_map.put("Closure_51", closure_51_map);


	Map<String, List<LineRange>> closure_52_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl52 = new ArrayList<LineRange>();
	lr_list_cl52.add(new LineRange(759, 768));
	closure_52_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/CodeGenerator.java", lr_list_cl52);
	black_map.put("Closure_52", closure_52_map);


	Map<String, List<LineRange>> closure_59_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl59 = new ArrayList<LineRange>();
	lr_list_cl59.add(new LineRange(275, 280));
	closure_59_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/Compiler.java", lr_list_cl59);
	black_map.put("Closure_59", closure_59_map);


	Map<String, List<LineRange>> closure_62_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl62 = new ArrayList<LineRange>();
	lr_list_cl62.add(new LineRange(69, 114));
	closure_62_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/LightweightMessageFormatter.java", lr_list_cl62);
	black_map.put("Closure_62", closure_62_map);
	

	Map<String, List<LineRange>> closure_65_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl65 = new ArrayList<LineRange>();
	lr_list_cl65.add(new LineRange(1014, 1102));
	closure_65_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/CodeGenerator.java", lr_list_cl65);
	black_map.put("Closure_65", closure_65_map);


	Map<String, List<LineRange>> closure_66_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl66 = new ArrayList<LineRange>();
	lr_list_cl66.add(new LineRange(451, 810));
	closure_66_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/TypeCheck.java", lr_list_cl66);
	black_map.put("Closure_66", closure_66_map);


	/*
	Map<String, List<LineRange>> closure_67_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl67 = new ArrayList<LineRange>();
	lr_list_cl67.add(new LineRange(, ));
	closure_67_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/", lr_list_cl67);
	black_map.put("Closure_67", closure_67_map);
	*/
	

	Map<String, List<LineRange>> closure_70_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl70 = new ArrayList<LineRange>();
	lr_list_cl70.add(new LineRange(1917, 1940));
	closure_70_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/TypedScopeCreator.java", lr_list_cl70);
	black_map.put("Closure_70", closure_70_map);


	Map<String, List<LineRange>> closure_71_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl71 = new ArrayList<LineRange>();
	lr_list_cl71.add(new LineRange(430, 450));
	closure_71_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/CheckAccessControls.java ", lr_list_cl71);
	black_map.put("Closure_71", closure_71_map);
	

	Map<String, List<LineRange>> closure_73_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl73 = new ArrayList<LineRange>();
	lr_list_cl73.add(new LineRange(1014, 1102));
	closure_73_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/CodeGenerator.java", lr_list_cl73);
	black_map.put("Closure_73", closure_73_map);

	
	Map<String, List<LineRange>> closure_77_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl77 = new ArrayList<LineRange>();
	lr_list_cl77.add(new LineRange(1014, 1102));
	closure_77_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/CodeGenerator.java", lr_list_cl77);
	black_map.put("Closure_77", closure_77_map);


	Map<String, List<LineRange>> closure_82_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl82 = new ArrayList<LineRange>();
	lr_list_cl82.add(new LineRange(158, 162));
	closure_82_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/rhino/jstype/JSType.java", lr_list_cl82);
	black_map.put("Closure_82", closure_82_map);
	

	Map<String, List<LineRange>> closure_86_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl86 = new ArrayList<LineRange>();
	lr_list_cl86.add(new LineRange(2896, 2965));
	closure_86_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/NodeUtil.java", lr_list_cl86);
	black_map.put("Closure_86", closure_86_map);


	Map<String, List<LineRange>> closure_92_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl92 = new ArrayList<LineRange>();
	lr_list_cl92.add(new LineRange(848, 918));
	closure_92_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/ProcessClosurePrimitives.java", lr_list_cl92);
	black_map.put("Closure_92", closure_92_map);


	Map<String, List<LineRange>> closure_97_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl97 = new ArrayList<LineRange>();
	lr_list_cl97.add(new LineRange(894, 900));
	closure_97_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/PeepholeFoldConstants.java", lr_list_cl97);
	black_map.put("Closure_97", closure_97_map);


	Map<String, List<LineRange>> closure_104_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl104 = new ArrayList<LineRange>();
	lr_list_cl104.add(new LineRange(282, 307));
	closure_104_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/rhino/jstype/UnionType.java", lr_list_cl104);
	black_map.put("Closure_104", closure_104_map);


	Map<String, List<LineRange>> closure_111_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl111 = new ArrayList<LineRange>();
	lr_list_cl111.add(new LineRange(49, 54));
	closure_111_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/type/ClosureReverseAbstractInterpreter.java", lr_list_cl111);
	black_map.put("Closure_111", closure_111_map);


	Map<String, List<LineRange>> closure_113_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl113 = new ArrayList<LineRange>();
	lr_list_cl113.add(new LineRange(261, 304));
	closure_113_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/ProcessClosurePrimitives.java", lr_list_cl113);
	black_map.put("Closure_113", closure_113_map);


	Map<String, List<LineRange>> closure_119_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl119 = new ArrayList<LineRange>();
	lr_list_cl119.add(new LineRange(372, 374));
	closure_119_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/GlobalNamespace.java", lr_list_cl119);
	black_map.put("Closure_119", closure_119_map);


	Map<String, List<LineRange>> closure_120_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl120 = new ArrayList<LineRange>();
	lr_list_cl120.add(new LineRange(417, 438));
	closure_120_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/ReferenceCollectingCallback.java", lr_list_cl120);
	black_map.put("Closure_120", closure_120_map);


	Map<String, List<LineRange>> closure_126_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl126 = new ArrayList<LineRange>();
	lr_list_cl126.add(new LineRange(135, 149));
	closure_126_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/MinimizeExitPoints.java", lr_list_cl126);
	black_map.put("Closure_126", closure_126_map);


	Map<String, List<LineRange>> closure_129_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl129 = new ArrayList<LineRange>();
	lr_list_cl129.add(new LineRange(160, 180));
	closure_129_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/PrepareAst.java", lr_list_cl129);
	black_map.put("Closure_129", closure_129_map);


	Map<String, List<LineRange>> closure_130_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl130 = new ArrayList<LineRange>();
	lr_list_cl130.add(new LineRange(172, 185));
	closure_130_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/CollapseProperties.java", lr_list_cl130);
	black_map.put("Closure_130", closure_130_map);


	Map<String, List<LineRange>> closure_132_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_cl132 = new ArrayList<LineRange>();
	lr_list_cl132.add(new LineRange(770, 797));
	closure_132_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Closure_14_buggy/src/com/google/javascript/jscomp/PeepholeSubstituteAlternateSyntax.java ", lr_list_cl132);
	black_map.put("Closure_132", closure_132_map);
	

	Map<String, List<LineRange>> lang_2_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_l2 = new ArrayList<LineRange>();
	lr_list_l2.add(new LineRange(61, 124));
	lang_2_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Lang_6_buggy/src/main/java/org/apache/commons/lang3/LocaleUtils.java", lr_list_l2);
	black_map.put("Lang_2", lang_2_map);

	
	Map<String, List<LineRange>> lang_6_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_l6 = new ArrayList<LineRange>();
	lr_list_l6.add(new LineRange(67, 98));
	lang_6_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Lang_6_buggy/src/main/java/org/apache/commons/lang3/text/translate/CharSequenceTranslator.java", lr_list_l6);
	black_map.put("Lang_6", lang_6_map);


	Map<String, List<LineRange>> lang_9_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_l9 = new ArrayList<LineRange>();
	lr_list_l9.add(new LineRange(104, 146));
	lang_9_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Lang_6_buggy/src/main/java/org/apache/commons/lang3/time/FastDateParser.java", lr_list_l9);
	black_map.put("Lang_9", lang_9_map);
	
	Map<String, List<LineRange>> lang_11_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_l11 = new ArrayList<LineRange>();
	lr_list_l11.add(new LineRange(190, 293));
	lang_11_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Lang_6_buggy/src/main/java/org/apache/commons/lang3/RandomStringUtils.java", lr_list_l11);
	black_map.put("Lang_11", lang_11_map);
	
	
	Map<String, List<LineRange>> lang_16_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_l16 = new ArrayList<LineRange>();
	lr_list_l16.add(new LineRange(419, 605));
	lang_16_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Lang_6_buggy/src/main/java/org/apache/commons/lang3/math/NumberUtils.java", lr_list_l16);
	black_map.put("Lang_16", lang_16_map);


	Map<String, List<LineRange>> lang_21_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_l21 = new ArrayList<LineRange>();
	lr_list_l21.add(new LineRange(225, 249));
	lang_21_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Lang_6_buggy/src/main/java/org/apache/commons/lang3/time/DateUtils.java", lr_list_l21);
	black_map.put("Lang_21", lang_21_map);


	Map<String, List<LineRange>> lang_24_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_l24 = new ArrayList<LineRange>();
	lr_list_l24.add(new LineRange(1297, 1410));
	lang_24_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Lang_6_buggy/src/main/java/org/apache/commons/lang3/math/NumberUtils.java", lr_list_l24);
	black_map.put("Lang_24", lang_24_map);
	

	Map<String, List<LineRange>> lang_26_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_l26 = new ArrayList<LineRange>();
	lr_list_l26.add(new LineRange(401, 410));
	lang_26_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Lang_6_buggy/src/main/java/org/apache/commons/lang3/time/FastDateFormat.java", lr_list_l26);
	black_map.put("Lang_26", lang_26_map);


	Map<String, List<LineRange>> lang_33_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_l33 = new ArrayList<LineRange>();
	lr_list_l33.add(new LineRange(971, 992));
	lang_33_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Lang_6_buggy/src/main/java/org/apache/commons/lang3/ClassUtils.java", lr_list_l33);
	black_map.put("Lang_33", lang_33_map);


	Map<String, List<LineRange>> lang_38_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_l38 = new ArrayList<LineRange>();
	lr_list_l38.add(new LineRange(450, 461));
	lang_38_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Lang_6_buggy/src/main/java/org/apache/commons/lang3/time/FastDateFormat.java", lr_list_l38);
	black_map.put("Lang_38", lang_38_map);


	Map<String, List<LineRange>> lang_39_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_l39 = new ArrayList<LineRange>();
	lr_list_l39.add(new LineRange(4494, 4668));
	lang_39_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Lang_6_buggy/src/main/java/org/apache/commons/lang3/StringUtils.java", lr_list_l39);
	black_map.put("Lang_39", lang_39_map);
	

	Map<String, List<LineRange>> lang_43_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_l43 = new ArrayList<LineRange>();
	lr_list_l43.add(new LineRange(479, 485));
	lang_43_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Lang_6_buggy/src/main/java/org/apache/commons/lang3/text/ExtendedMessageFormat.java", lr_list_l43);
	black_map.put("Lang_43", lang_43_map);

	/*
	Map<String, List<LineRange>> lang_44_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_l44 = new ArrayList<LineRange>();
	lr_list_l44.add(new LineRange(, ));
	lang_44_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Lang_6_buggy/src/main/java/", lr_list_l44);
	black_map.put("Lang_44", lang_44_map);
	*/

	/*
	Map<String, List<LineRange>> lang_45_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_l45 = new ArrayList<LineRange>();
	lr_list_l45.add(new LineRange(, ));
	lang_45_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Lang_6_buggy/src/main/java/", lr_list_l45);
	black_map.put("Lang_45", lang_45_map);
	*/
	
	Map<String, List<LineRange>> lang_51_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_l51 = new ArrayList<LineRange>();
	lr_list_l51.add(new LineRange(513, 624));
	lang_51_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Lang_6_buggy/src/main/java/org/apache/commons/lang3/BooleanUtils.java", lr_list_l51);
	black_map.put("Lang_51", lang_51_map);


	Map<String, List<LineRange>> lang_54_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_l54 = new ArrayList<LineRange>();
	lr_list_l54.add(new LineRange(61, 124));
	lang_54_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Lang_6_buggy/src/main/java/org/apache/commons/lang3/LocaleUtils.java", lr_list_l54);
	black_map.put("Lang_54", lang_54_map);


	Map<String, List<LineRange>> lang_55_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_l55 = new ArrayList<LineRange>();
	lr_list_l55.add(new LineRange(136, 156));
	lang_55_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Lang_6_buggy/src/main/java/org/apache/commons/lang3/time/StopWatch.java", lr_list_l55);
	black_map.put("Lang_55", lang_55_map);


	Map<String, List<LineRange>> lang_57_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_l57 = new ArrayList<LineRange>();
	lr_list_l57.add(new LineRange(207, 215));
	lang_57_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Lang_6_buggy/src/main/java/org/apache/commons/lang3/LocaleUtils.java", lr_list_l57);
	List<LineRange> lr_list_l57b = new ArrayList<LineRange>();
	lr_list_l57b.add(new LineRange(121, 123));
	lang_57_map.put("/gpfs/data/people/qx5/search_fix/cocker_index2/chart_8_fix/mc-dev-1.10.2-master/org/apache/commons/lang3/LocaleUtils.java", lr_list_l57b);
	black_map.put("Lang_57", lang_57_map);


	Map<String, List<LineRange>> lang_58_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_l58 = new ArrayList<LineRange>();
	lr_list_l58.add(new LineRange(419, 605));
	lang_58_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Lang_6_buggy/src/main/java/org/apache/commons/lang3/math/NumberUtils.java", lr_list_l58);
	black_map.put("Lang_58", lang_58_map);
	
	
	Map<String, List<LineRange>> lang_59_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_l59 = new ArrayList<LineRange>();
	lr_list_l59.add(new LineRange(1337, 1368));
	lang_59_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Lang_6_buggy/src/main/java/org/apache/commons/lang3/text/StrBuilder.java", lr_list_l59);
	black_map.put("Lang_59", lang_59_map);


	Map<String, List<LineRange>> math_2_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m2 = new ArrayList<LineRange>();
	lr_list_m2.add(new LineRange(264, 272));
	math_2_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/distribution/HypergeometricDistribution.java", lr_list_m2);
	black_map.put("Math_2", math_2_map);


	Map<String, List<LineRange>> math_3_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m3 = new ArrayList<LineRange>();
	lr_list_m3.add(new LineRange(565, 636));
	math_3_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/util/MathArrays.java", lr_list_m3);
	black_map.put("Math_3", math_3_map);
	
	
	Map<String, List<LineRange>> math_5_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m5 = new ArrayList<LineRange>();
	lr_list_m5.add(new LineRange(298, 321));
	math_5_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/complex/Complex.java", lr_list_m5);
	black_map.put("Math_5", math_5_map);


	Map<String, List<LineRange>> math_9_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m9 = new ArrayList<LineRange>();
	lr_list_m9.add(new LineRange(83, 88));
	math_9_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/geometry/euclidean/threed/Line.java", lr_list_m9);
	black_map.put("Math_9", math_9_map);


	/*
	Map<String, List<LineRange>> math_10_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m10 = new ArrayList<LineRange>();
	lr_list_m10.add(new LineRange(, ));
	math_10_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/", lr_list_m10);
	black_map.put("Math_10", math_10_map);
	*/
	
	
	//Map<String, List<LineRange>> math_11_map = new HashMap<String, List<LineRange>>();
	//List<LineRange> lr_list_m11 = new ArrayList<LineRange>();
	//lr_list_m11.add(new LineRange(, ));
	//math_11_map.put("", lr_list_m11);
	//black_map.put("Math_11", math_11_map);


	Map<String, List<LineRange>> math_27_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m27 = new ArrayList<LineRange>();
	lr_list_m27.add(new LineRange(588, 598));
	math_27_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/fraction/Fraction.java", lr_list_m27);
	black_map.put("Math_27", math_27_map);


	Map<String, List<LineRange>> math_32_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m32 = new ArrayList<LineRange>();
	lr_list_m32.add(new LineRange(128, 179));
	math_32_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/geometry/euclidean/twod/PolygonsSet.java", lr_list_m32);
	black_map.put("Math_32", math_32_map);


	Map<String, List<LineRange>> math_33_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m33 = new ArrayList<LineRange>();
	lr_list_m33.add(new LineRange(323, 367));
	math_33_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/optimization/linear/SimplexTableau.java", lr_list_m33);
	black_map.put("Math_33", math_33_map);
	

	Map<String, List<LineRange>> math_34_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m34 = new ArrayList<LineRange>();
	lr_list_m34.add(new LineRange(203, 211));
	math_34_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/genetics/ListPopulation.java", lr_list_m34);
	black_map.put("Math_34", math_34_map);


	Map<String, List<LineRange>> math_39_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m39 = new ArrayList<LineRange>();
	lr_list_m39.add(new LineRange(189, 337));
	math_39_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/ode/nonstiff/EmbeddedRungeKuttaIntegrator.java", lr_list_m39);
	black_map.put("Math_39", math_39_map);

	
	Map<String, List<LineRange>> math_41_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m41 = new ArrayList<LineRange>();
	lr_list_m41.add(new LineRange(456, 532));
	math_41_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/stat/descriptive/moment/Variance.java", lr_list_m41);
	black_map.put("Math_41", math_41_map);


	Map<String, List<LineRange>> math_48_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m48 = new ArrayList<LineRange>();
	lr_list_m48.add(new LineRange(128, 255));
	math_48_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/analysis/solvers/BaseSecantSolver.java", lr_list_m48);
	black_map.put("Math_48", math_48_map);

	
	Map<String, List<LineRange>> math_49_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m49 = new ArrayList<LineRange>();
	lr_list_m49.add(new LineRange(338, 362));
	math_49_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/linear/OpenMapRealVector.java", lr_list_m49);
	black_map.put("Math_49", math_49_map);


	Map<String, List<LineRange>> math_50_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m50 = new ArrayList<LineRange>();
	lr_list_m50.add(new LineRange(128, 255));
	math_50_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/analysis/solvers/BaseSecantSolver.java", lr_list_m50);
	black_map.put("Math_50", math_50_map);
	

	Map<String, List<LineRange>> math_53_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m53 = new ArrayList<LineRange>();
	lr_list_m53.add(new LineRange(137, 164));
	math_53_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/complex/Complex.java", lr_list_m53);
	black_map.put("Math_53", math_53_map);


	/*
	Map<String, List<LineRange>> math_57_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m57 = new ArrayList<LineRange>();
	lr_list_m57.add(new LineRange(, ));
	math_57_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/stat/clustering/KMeansPlusPlusClusterer.java", lr_list_m57);
	black_map.put("Math_57", math_57_map);
	*/
	
	Map<String, List<LineRange>> math_58_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m58 = new ArrayList<LineRange>();
	lr_list_m58.add(new LineRange(114, 123));
	math_58_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/optimization/fitting/GaussianFitter.java", lr_list_m58);
	black_map.put("Math_58", math_58_map);


	/*
	Map<String, List<LineRange>> math_59_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m59 = new ArrayList<LineRange>();
	lr_list_m59.add(new LineRange(, ));
	math_59_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/", lr_list_m59);
	black_map.put("Math_59", math_59_map);
	*/
	

	Map<String, List<LineRange>> math_69_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m69 = new ArrayList<LineRange>();
	lr_list_m69.add(new LineRange(149, 179));
	math_69_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/stat/correlation/PearsonsCorrelation.java", lr_list_m69);
	black_map.put("Math_69", math_69_map);

	
	//Map<String, List<LineRange>> math_70_map = new HashMap<String, List<LineRange>>();
	//List<LineRange> lr_list_m70 = new ArrayList<LineRange>();
	//lr_list_m70.add(new LineRange(, ));
	//math_70_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/analysis/solvers/BisectionSolver.java", lr_list_m70);
	//black_map.put("Math_70", math_70_map);


	/*
	Map<String, List<LineRange>> math_75_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m75 = new ArrayList<LineRange>();
	lr_list_m75.add(new LineRange(, ));
	math_75_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/", lr_list_m75);
	black_map.put("Math_75", math_75_map);
	*/
	
	
	Map<String, List<LineRange>> math_79_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m79 = new ArrayList<LineRange>();
	lr_list_m79.add(new LineRange(94, 108));
	math_79_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/util/MathArrays.java", lr_list_m79);
	black_map.put("Math_79", math_79_map);

	/*
	Map<String, List<LineRange>> math_80_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m80 = new ArrayList<LineRange>();
	lr_list_m80.add(new LineRange(, ));
	math_80_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/", lr_list_m80);
	black_map.put("Math_80", math_80_map);
	*/


	Map<String, List<LineRange>> math_82_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m82 = new ArrayList<LineRange>();
	lr_list_m82.add(new LineRange(82, 126));
	math_82_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/optimization/linear/SimplexSolver.java", lr_list_m82);
	black_map.put("Math_82", math_82_map);


	Map<String, List<LineRange>> math_85_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m85 = new ArrayList<LineRange>();
	lr_list_m85.add(new LineRange(270, 275));
	math_85_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/analysis/solvers/UnivariateSolverUtils.java", lr_list_m85);
	black_map.put("Math_85", math_85_map);
	
	
	//Map<String, List<LineRange>> math_94_map = new HashMap<String, List<LineRange>>();
	//List<LineRange> lr_list_m94 = new ArrayList<LineRange>();
	//lr_list_m94.add(new LineRange(, ));
	//math_94_map.put("", lr_list_m94);
	//black_map.put("Math_94", math_94_map);


	Map<String, List<LineRange>> math_105_map = new HashMap<String, List<LineRange>>();
	List<LineRange> lr_list_m105 = new ArrayList<LineRange>();
	lr_list_m105.add(new LineRange(404, 435));
	math_105_map.put("/gpfs/data/people/qx5/defects4j-bugs/cocker_local_index/Math_33_buggy/src/main/java/org/apache/commons/math3/stat/regression/SimpleRegression.java", lr_list_m105);
	black_map.put("Math_105", math_105_map);
	
    }

    public static boolean isBlack(String bug_id, String fpath, String loc) {

	Map<String, List<LineRange>> black_map0 = black_map.get(bug_id);
	if (black_map0 == null) { return false; }
	if (fpath == null) { return false; }
	if (fpath.startsWith("file://")) { fpath.substring(7); }
	List<LineRange> lr_list = black_map0.get(fpath);
	if (lr_list == null) { return false; }
	List<Integer> loc_lints = getLineNumbers(loc);
	for (Integer loc_lint : loc_lints) {
	    int loc_ln = loc_lint.intValue();
	    for (LineRange lr : lr_list) {
		if (lr.include(loc_ln)) { return true; }
	    }
	}
	return false;
    }

    private static List<Integer> getLineNumbers(String loc) {

	List<Integer> lns = new ArrayList<Integer>();
	String[] sublocs = loc.split(";");
	for (String subloc : sublocs) {
	    String[] subloc_items = subloc.split(":");
	    if (subloc_items[0].endsWith("-nested")) { continue; }
	    else {
		int dot_index = subloc_items[1].indexOf(",");
		Integer lint = null;
		try { lint = Integer.valueOf(subloc_items[1].substring(0, dot_index)); }
		catch (Throwable t) {
		    System.err.println(t);
		    t.printStackTrace();
		}
		if (lint != null) {
		    lns.add(lint);
		}
	    }
	}
	return lns;
    }

    private static class LineRange
    {
	int l0, l1;

	public LineRange(int l0, int l1) {
	    this.l0 = l0;
	    this.l1 = l1;
	}

	public boolean include(int l) {
	    return (l0<=l && l<=l1);
	}
    }
}
