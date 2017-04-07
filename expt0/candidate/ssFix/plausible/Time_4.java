/**********************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: PatternDrawStrategy.java,v 1.19 2006/08/28 19:19:38 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.hyades.trace.views.internal;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.hyades.models.trace.TRCClass;
import org.eclipse.hyades.models.trace.TRCFullMethodInvocation;
import org.eclipse.hyades.models.trace.TRCFullTraceObject;
import org.eclipse.hyades.models.trace.TRCMethodInvocation;
import org.eclipse.hyades.models.trace.TRCMethodProperties;
import org.eclipse.hyades.models.trace.TRCThread;
import org.eclipse.hyades.models.trace.TRCTraceObject;
import org.eclipse.hyades.trace.ui.ITraceSelection;
import org.eclipse.hyades.trace.ui.UIPlugin;
import org.eclipse.hyades.trace.ui.ViewSelectionChangedEvent;
import org.eclipse.hyades.trace.ui.internal.util.PerftraceUtil;
import org.eclipse.hyades.trace.ui.internal.util.TString;
import org.eclipse.hyades.trace.views.adapter.internal.TraceConstants;
import org.eclipse.hyades.trace.views.internal.view.columnlabels.ColumnDisplayInfo;
import org.eclipse.hyades.trace.views.internal.view.columnlabels.ContextUpdaterHelper;
import org.eclipse.hyades.trace.views.internal.view.columnlabels.CumulativeTimeColumnLabel;
import org.eclipse.hyades.trace.views.internal.view.columnlabels.FullyQualMethodInvLabel;
import org.eclipse.hyades.trace.views.internal.view.columnlabels.InstanceNameColumnLabel;
import org.eclipse.hyades.trace.views.internal.view.columnlabels.MethodInvocationEntryTimeColumnLabel;
import org.eclipse.hyades.trace.views.internal.view.columnlabels.ThreadNameColumnLabel;
import org.eclipse.hyades.trace.views.util.internal.LinearPattern;
import org.eclipse.hyades.trace.views.util.internal.SpectrumColorMap;
import org.eclipse.hyades.ui.provisional.context.ContextManager;
import org.eclipse.hyades.ui.provisional.context.IContextAttributes;
import org.eclipse.hyades.ui.provisional.context.IContextLabelFormatProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;


/*
* CONTEXT_ID exef0004 for Detect all repetitions in execution flow view
*/
/*
* CONTEXT_ID exef0005 for hide all repetitions in execution flow view
*/
/*
* CONTEXT_ID exef0006 for show all threads in execution flow view
*/
/*
* CONTEXT_ID exef0007 for hide all threads in execution flow view
*/

public class PatternDrawStrategy extends GraphDrawStrategy
{
	protected ArrayList _visibleThreads = new ArrayList();
	protected ArrayList _popupThreads = new ArrayList();
	protected Object[]  _tmpArray;
	private final int NRLABELS = 10;
	private final float MAG_STEP = 2;

	private Pattern _parent;
	protected int _maxdepth = 0;
	protected long _maxtime = 0;
	private float timescale = .1f;
	private boolean _hidedetails = true;
	protected boolean _collectedempty = false;
	private boolean _drawgc = false;
	private int _drawMode = TraceUIPlugin.getDefault().getPreferenceStore().getInt(TraceConstants.TIME_OPTION);

	private Hashtable _foldedInvocations;
	protected float _canvasVisibleBottom,
		_canvasVisibleTop,
		_canvasVisibleLeft,
		_canvasVisibleRight;

	protected final static int MAXINV = 256;
	protected TRCMethodInvocation _drawArray[] = new TRCMethodInvocation[MAXINV];
	protected float _x[] = new float[MAXINV],
		_y[] = new float[MAXINV],
		_w[] = new float[MAXINV],
		_h[] = new float[MAXINV];

	private DetectAllRepetitionsdAction rep1;
	private HideAllRepetitionsAction rep2;
	private ShowAllThreadsAction _showAll;
	private HideAllThreadsAction _hideAll;
	private ThreadAction _gcThread;
	
	final int WIDTH_OF_LINE_ABOVE_LABEL = 4;
	
	class DetectAllRepetitionsdAction extends Action {
		public DetectAllRepetitionsdAction(String name) {
			super(name);

			PlatformUI.getWorkbench().getHelpSystem().setHelp(
				DetectAllRepetitionsdAction.this,
				TraceUIPlugin.getPluginId() + ".exef0004");
		}

		/**
		 * Invoked when an action occurs. 
			* Argument context is the Window which contains the UI from which this action was fired.
		 * This default implementation prints the name of this class and its label.
		 */
		public void run() {
			if (jcanvas() == null)
				return;

			detectLinearPatterns();
			jcanvas().redraw();
		}
	}

	//-------------------------------
	// HideAllRepetitionsdAction popup action
	//-------------------------------	
	class HideAllRepetitionsAction extends Action {
		public HideAllRepetitionsAction(String name) {
			super(name);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(
				HideAllRepetitionsAction.this,
				TraceUIPlugin.getPluginId() + ".exef0005");
		}

		/**
		 * Invoked when an action occurs. 
			* Argument context is the Window which contains the UI from which this action was fired.
		 * This default implementation prints the name of this class and its label.
		 */
		public void run() {
			if (jcanvas() == null)
				return;

			removeLinearPatterns();
			jcanvas().redraw();
		}
	}
	//-------------------------------
	// HideThreadAction popup action
	//-------------------------------	
	class HideAllThreadsAction extends Action {
		public HideAllThreadsAction(String name) {
			super(name);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(
				HideAllThreadsAction.this,
				TraceUIPlugin.getPluginId() + ".exef0007");
		}

		/**
		 * Invoked when an action occurs. 
			* Argument context is the Window which contains the UI from which this action was fired.
		 * This default implementation prints the name of this class and its label.
		 */
		public void run() {
			if (jcanvas() == null)
				return;

			_visibleThreads.clear();

			_drawgc = false;

			//resetArea();
			jcanvas().zoomToFill(1f, 1000f);
			jcanvas().redraw();
		}
	}

	//-------------------------------
	// ShowAllThreadsAction popup action
	//-------------------------------	
	class ShowAllThreadsAction extends Action {
		public ShowAllThreadsAction(String name) {
			super(name);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(
				ShowAllThreadsAction.this,
				TraceUIPlugin.getPluginId() + ".exef0006");
		}

		/**
		 * Invoked when an action occurs. 
			* Argument context is the Window which contains the UI from which this action was fired.
		 * This default implementation prints the name of this class and its label.
		 */
		public void run() {
			if (jcanvas() == null)
				return;

            Object[] threads = PerftraceUtil.getAllThreads(_parent.getPage().getMOFObject());
			for (int idx = 0; idx < threads.length; idx++) {
				
				int i=0;
				for(i=0; i<_visibleThreads.size(); i++)
				{
				  if(_visibleThreads.get(i) == threads[idx])
				    break;				  
				}
				
				if(i == _visibleThreads.size())
				   _visibleThreads.add(threads[idx]);
			}

            _maxdepth = 0;
			_drawgc = true;

            
			resetArea();
			jcanvas().zoomToFill(1f, 1000f);
			jcanvas().redraw();

		}
	}

	//-------------------------------
	// ThreadAction popup action
	//-------------------------------	
	class ThreadAction extends Action {
		protected TRCThread _thread;
		protected String _name="";

		public ThreadAction() {
			super("");
		}

		public void setThread(TRCThread thread)
		{
			_thread = thread;
			if(_thread != null)
			  _name = PerftraceUtil.getThreadName(thread);
		}
		
		public void setName(String name)
		{
			_name = name;
		}

		public TRCThread getThread() {
			return _thread;
		}
				
		public String getName()
		{
			return _name;
		}

		/**
		 * Invoked when an action occurs. 
			* Argument context is the Window which contains the UI from which this action was fired.
		 * This default implementation prints the name of this class and its label.
		 */
		public void run() {
			if (jcanvas() == null)
				return;

			if (getName().equals(TraceUIMessages._75))
			{				
				_drawgc = !_drawgc;
				
			}
			else
			{			
				if(_visibleThreads.contains(_thread))
				{
					_visibleThreads.remove(_thread);
				}
				else
				{
					_visibleThreads.add(_thread);
				}
			}
			
			_maxdepth = 0;			
			_maxtime = 0;
				
			resetArea();			
			jcanvas().zoomToFill(1f, 1000f);
			jcanvas().redraw();
		}
	}

	public PatternDrawStrategy(Pattern parent) {
		super();

        _parent = parent;
		setNonPropZoom(true);
		_foldedInvocations = new Hashtable();

       Object[] threads = PerftraceUtil.getAllThreads(_parent.getPage().getMOFObject());
		for (int idx = 0; idx < threads.length; idx++) {
			_visibleThreads.add(threads[idx]);
		}
		
		rep1 = new DetectAllRepetitionsdAction(TraceUIMessages._14);
		rep2 = new HideAllRepetitionsAction(TraceUIMessages._15);

		_showAll = new ShowAllThreadsAction(TraceUIMessages._16);
		_hideAll = new HideAllThreadsAction(TraceUIMessages._17);

		_gcThread = new ThreadAction();
		_gcThread.setName(TraceUIMessages._75);		
	}
	
	/**
	 * 
	 */
	public void bgRedraw(GC gc) {
		
		JCanvas c = jcanvas();

		if (c == null)
			return;
			      
		for(int idx=0; idx<_visibleThreads.size(); idx++) {
						
			TRCThread thread = (TRCThread) _visibleThreads.get(idx);
			
			float offx = idx * treeWidth() + gcWidth();
			if (offx + treeWidth() > c.visibleLeft()
				&& offx < c.visibleRight()) {
				drawThread(gc, thread, offx, 0);
			}
		}
		
		_hidedetails = false;
		if (_drawgc)
			drawGC(gc, 0, 0);

		for(int idx=0; idx<_visibleThreads.size(); idx++) {
						
			TRCThread thread = (TRCThread) _visibleThreads.get(idx);
			
			float offx = idx * treeWidth() + gcWidth();
			if (offx + treeWidth() > c.visibleLeft()
				&& offx < c.visibleRight()) {
										
				drawThread(gc, thread, offx, 0);
			}
		}

		drawTimeMarks(gc, 0, 0);
	}
	protected int cellHeight() {
		return 20;
	}
	protected int cellWidth() {
		return 20;
	}
	
	/**
	 * Used to detect all repetition in an execution flow view
	 */
	protected void detectLinearPatterns() 
	{
		/* For every visible thread in the execution flow diagram */
		for (int i = 0; i < _visibleThreads.size(); ++i) {
			TRCThread thread = (TRCThread) _visibleThreads.get(i);
			
			Object[] invocations = thread.getInitialInvocations().toArray();
			
			/* For every invocation in a thread */
			for(int idx=0; idx<invocations.length; idx++)
			{
				detectLinearPatterns(((TRCMethodInvocation) invocations[idx]));
			}
		}
		
	}
	
	/**
	 * 
	 */
	protected void detectLinearPatterns(TRCMethodInvocation root) {
		
		Object[] segments = root.getInvokes().toArray();
		int fanout = segments.length;
		LinearPattern lp = null;
		
		
		if (fanout > 2) {
			lp = new LinearPattern();
			
			/* Add the methods that 'root' invokes */
			for (int j = 0; j < fanout; ++j) 
			{
				lp.add(((TRCMethodInvocation)segments[j]).getMethod());
			}
			lp.compute();

		} else {
			for (int j = 0; j < fanout; ++j)
				detectLinearPatterns(((TRCMethodInvocation)segments[j]));
			return;
		}

		if (lp.hasRepetition()) {
			_foldedInvocations.put(root, lp);
		}

		// Recurse into non-repetitive callees. The repetitive callees
		// are drawn with DrawRepetition, which is only one level deep, so
		// there is no need to recurse into these.

		for (int i = 0; i < fanout; ++i) {
			TRCMethodInvocation kid = ((TRCMethodInvocation)segments[i]);
			int repet = lp.repetition(i);
			if (repet == 1) {
				detectLinearPatterns(kid);
			} else {
				int len = lp.length(i);
				
				/* Ali M.: Defect 109662 -- We still need to step through the child's invocations */
				for (int k = 0; k < len; k++)
					detectLinearPatterns((TRCMethodInvocation)segments[i + k]);
				
				if (repet == 0 || len == 0) {
					System.err.println(
						"linpattern impossible (fanout :" + fanout + ")");
				} else {
					i += len * repet - 1;
				}
			}
		}
		
	}
	
	/**
	 * 
	 */
	void drawExtension(
		GC gc,
		TRCMethodInvocation inv,
		float x,
		float y,
		float w,
		float h) {
		JCanvas c = jcanvas();
		if (c == null)
			return;
		String clss = inv.getMethod().getDefiningClass().getName();
		Color col = SpectrumColorMap.color(clss);
		if (c.xscale() * w > 20.)
			c.fillRect(gc, x + 3 * w / 4, y, w / 4, h, col);
		else
			c.fillRect(gc, x + w * 7 / 8, y, w / 8, h, col);
	}
	
	/**
	 * 
	 */
	protected void drawGC(GC gc, int offx, int offy) {

		JCanvas c = jcanvas();
		if (isDirty() || c == null || offx + gcWidth() < c.visibleLeft())
			return;

		Display display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();

		float yscale = c.yscale();
		float lasty = 0f;
		_canvasVisibleBottom = c.visibleBottom();
		_canvasVisibleTop = c.visibleTop();

		ITraceSelection selModel = UIPlugin.getDefault().getSelectionModel(_parent.getPage().getMOFObject());			
		Object[] classes = PerftraceUtil.getAllClasses(_parent.getPage().getMOFObject());
		for(int idx=0; idx<classes.length; idx++)
		{	
			TRCClass clas = (TRCClass) classes[idx];
			
			ArrayList objects = new ArrayList();
			objects.addAll(clas.getObjects());
			objects.addAll(clas.getClassObjects());
			for(int i = 0; i<objects.size(); i++)
			{
				if(!(objects.get(i) instanceof TRCFullTraceObject))
				  continue;
				  				  				 
				TRCFullTraceObject o = (TRCFullTraceObject)objects.get(i);
				float y = TString.getTime(o.getCollectTime()) * timescale + titleMargin();
				
				
				if(y == 0)
				  continue;
				  
				if (isDirty())
					return;
				if (y > _canvasVisibleBottom)
					break;
				if (y < _canvasVisibleTop)
					continue;
				if (selModel.contains(o))
					c.fill3DRect(
						gc,
						offx,
						offy + y,
						gcWidth(),
						cellHeight() * 2,
						SpectrumColorMap.getSelectionColor());
				if ((y - lasty) * yscale < 1f)
					continue;

				TRCClass cls = PerftraceUtil.getClass(o);				
				
				lasty = y;
				Color ocolor = SpectrumColorMap.color(cls.toString());
				c.fillRect(
					gc,
					offx,
					offy + y,
					cellWidth() * 5,
					cellHeight() * 2,
					ocolor);
			}
		}


		float fontheight = gc.getFontMetrics().getHeight() / c.yscale();
		c.drawString(
			gc,
			TraceUIMessages._75,
			offx,
			c.visibleTop() + fontheight,
			display.getSystemColor(SWT.COLOR_BLUE));
			
	}
	
	/**
	 * 
	 */
	void drawInvocation(GC gc,	TRCMethodInvocation inv, float x, float y,
						float w, float h, boolean showString,	boolean drawFaded)
   {
		JCanvas c = jcanvas();
		if (c == null)
			return;
			
		String msg = inv.getMethod().getName();
		Display display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();

		Color colmsg = display.getSystemColor(SWT.COLOR_BLACK);
		
		if ((inv.getMethod().getModifier() & TRCMethodProperties.JAVA_CONSTRUCTOR) != 0)
			colmsg = display.getSystemColor(SWT.COLOR_RED);
		
		double collectTime = 0;
		TRCTraceObject obj = inv.getOwningObject();
		if(obj instanceof TRCFullTraceObject)
		{
			collectTime = ((TRCFullTraceObject)obj).getCollectTime();
		}
		
		TRCClass cls = PerftraceUtil.getClass(inv); 			
		String clss = cls.getName();
		Color col = SpectrumColorMap.color(clss);

		float xscale = c.xscale();
		if (showString && xscale > .5 && !_hidedetails) {
			int maxchars = (int) (w * xscale / 5);
			// font width about 5 pix (hack!)
			if (msg.length() > maxchars) {
				msg = msg.substring(0, maxchars);
			}
			float fontheight = gc.getFontMetrics().getHeight() / c.yscale();
			c.drawString(gc, msg, x, y + fontheight, colmsg);
		}

		if (xscale * w > 20.) {
			c.drawLine(gc, x, y, x + 3 * w / 4, y, colmsg);
			if (!_collectedempty) {
				c.fillRect(gc, x + 3 * w / 4, y, w / 4, h, col);
			} else if (collectTime > 0) {
				c.drawRect(gc, x + 3 * w / 4, y, w / 4, h, col);
			} else {
				c.fillRect(gc, x + 3 * w / 4, y, w / 4, h, col);
			}

		} else
			c.fillRect(gc, x + w / 4, y, w * 3 / 4, h, col);
	}
	
	/**
	 * 
	 */
	void drawRepetition(
		GC gc,
		TRCMethodInvocation invocation,
		int depth,
		float offx,
		float offy,
		int starti,
		LinearPattern lp) {
			
		JCanvas canvas = jcanvas();
		if (canvas == null || !(invocation instanceof TRCFullMethodInvocation))
			return;
			
        TRCFullMethodInvocation inv = (TRCFullMethodInvocation) invocation;
        
		
        /* Ali M.: Defect #: 109662 -- The old sequence of method invocations: inv.getOwningObject().getProcess().getLastEventTime();
         * was causing a NPE because inv.getOwningObject() was null. */
        double potentialTime = ((TRCFullMethodInvocation)((TRCMethodInvocation)inv.getInvokes().get(starti))).getEntryTime();
        double time;
        double maxtime = 0;
        if (inv.getProcess() != null)
        	maxtime = inv.getProcess().getLastEventTime();
        time = (potentialTime == 0 ? maxtime : potentialTime);
        //double maxtime = inv.getOwningObject().getProcess().getLastEventTime();				/* Old code causing the NPE. */
        
		Display display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();

		float yscale = canvas.yscale();

		// big rectangle
		int repet = lp.repetition(starti);
		int len = lp.length(starti);
		
		float startrec = PerftraceUtil.getTime(time) * timescale + offy;
				
		
		potentialTime = ((TRCFullMethodInvocation)((TRCMethodInvocation)inv.getInvokes().get(starti + repet * len - 1))).getExitTime();
		time = (potentialTime == 0 ? maxtime : potentialTime);
		
		float endrec = PerftraceUtil.getTime(time) * timescale + offy;
		
		if (endrec < startrec)
			endrec =
			PerftraceUtil.getTime(((TRCFullMethodInvocation)((TRCMethodInvocation)inv.getInvokes().get(starti + repet * len - 1))).getEntryTime()) * timescale + offy;
		float heightrec = endrec - startrec;			
		
		
		canvas.fill3DRect(
			gc,
			offx + (WIDTH_OF_LINE_ABOVE_LABEL * depth + WIDTH_OF_LINE_ABOVE_LABEL) * cellWidth(),
			startrec,
			5 * cellWidth(),
			heightrec,
			display.getSystemColor(SWT.COLOR_GRAY));
		
		
		//light gray

		/* Ali M.: There is no need to draw this box */
//		// inner rectangle with details magnified
//		canvas.fill3DRect(
//			gc,
//			offx + (WIDTH_OF_LINE_ABOVE_LABEL * depth + 6) * cellWidth(),
//			startrec + heightrec / 40f,
//			5.5f * cellWidth(),
//			heightrec * .95f,
//			display.getSystemColor(SWT.COLOR_GRAY));
		//light gray

		if (heightrec * yscale < 5)
			return;

		/* Ali M.: No point in drawing this -- it gets blocked by the box drawn ealier */
//		canvas.drawLine(
//			gc,
//			offx + (WIDTH_OF_LINE_ABOVE_LABEL * depth + 5) * cellWidth() + 5,
//			startrec,
//			offx + (WIDTH_OF_LINE_ABOVE_LABEL * depth + 6) * cellWidth(),
//			startrec + heightrec / 40f,
//			display.getSystemColor(SWT.COLOR_GRAY));
		//light gray
        
		potentialTime = ((TRCFullMethodInvocation)((TRCMethodInvocation)inv.getInvokes().get(starti + len - 1))).getExitTime();
		time = (potentialTime == 0 ? maxtime : potentialTime);
		
		float firstend = PerftraceUtil.getTime(time) * timescale + offy;
		if (firstend < startrec)
			firstend = startrec;

		
		/* Ali M.: I don't know what the original intention of this line and box was but it is currently not even
		 * visible to the user because it is apart of a bigger box drawn earlier and they all use the same colour (i.e. SWT.COLOR_GRAY)
		 */
//		canvas.drawLine(
//			gc,
//			offx + (WIDTH_OF_LINE_ABOVE_LABEL * depth + 5) * cellWidth() + 5,
//			firstend,
//			offx + (WIDTH_OF_LINE_ABOVE_LABEL * depth + 6) * cellWidth(),
//			startrec + heightrec * .975f,
//			display.getSystemColor(SWT.COLOR_GRAY));
		//light gray 

//		// draw 'words' as gray small boxes
//		if (repet < 1000) {
//			for (int k = 0; k < repet; ++k) {
//				float startsmall =
//				PerftraceUtil.getTime(((TRCFullMethodInvocation)((TRCMethodInvocation)inv.getInvokes().get(starti + k * len))).getEntryTime()) * timescale + offy;
//				
//				potentialTime = ((TRCFullMethodInvocation)((TRCMethodInvocation)inv.getInvokes().get(starti + k * len + len - 1))).getExitTime();
//				time = (potentialTime == 0 ? maxtime : potentialTime);
//				
//				float heightsmall = PerftraceUtil.getTime(time) * timescale + offy - startsmall;
//				if (heightsmall < 0f)
//					heightsmall = 0f;
//								
//				canvas.fill3DRect(
//					gc,
//					offx + (WIDTH_OF_LINE_ABOVE_LABEL * depth + WIDTH_OF_LINE_ABOVE_LABEL) * cellWidth() + 5,
//					startsmall,
//					cellWidth(),
//					heightsmall,
//					display.getSystemColor(SWT.COLOR_GRAY));
//				//light gray
//			}
//		}

		/* Ali M.: Defect 109662 -- Give the gray box a black outline */
		canvas.drawRect(gc, offx + (WIDTH_OF_LINE_ABOVE_LABEL * depth + WIDTH_OF_LINE_ABOVE_LABEL) * cellWidth(), startrec,	5 * cellWidth(), heightrec,	display.getSystemColor(SWT.COLOR_BLACK));

		potentialTime = ((TRCFullMethodInvocation)((TRCMethodInvocation)inv.getInvokes().get(starti + len - 1))).getExitTime();
		time = (potentialTime == 0 ? maxtime : potentialTime);
		
		float endword = PerftraceUtil.getTime(time) * timescale + offy;
		float heightword = endword - startrec;
		if (heightword < 0f)
			heightword = 0f;
		float ratio = 1f;
		if (heightword > 0)
			ratio = (endrec - startrec) / heightword * .9f; // occupy 90%

		// draw first word in this repetition magnified
		for (int j = 0; j < len; ++j) {
			TRCFullMethodInvocation kid1 = (TRCFullMethodInvocation)((TRCMethodInvocation)inv.getInvokes().get(starti + j));
			float start1 = PerftraceUtil.getTime(kid1.getEntryTime()) * timescale + offy;
			
			potentialTime = kid1.getExitTime();
			time = (potentialTime == 0 ? maxtime : potentialTime);
			
			float end1 = PerftraceUtil.getTime(time) * timescale + offy;
			float h1 = end1 - start1;

			final float parentYCoordOffset = startrec + (start1 - startrec) * ratio + heightrec / 20f;	
			final float parentHeight = h1 * ratio;
			final float parentXCoordOffset = offx + (WIDTH_OF_LINE_ABOVE_LABEL * depth + 4) * cellWidth();
			final float parentWidth = cellWidth() * WIDTH_OF_LINE_ABOVE_LABEL;
 
			/* Draw the invocation that is part of the repetition */
			drawInvocation(gc, kid1, parentXCoordOffset, parentYCoordOffset, parentWidth, parentHeight, true, false);
			
			/* Ali M.: Defect 109662 -- We still need to step through the child invocations of this method and draw them */
			Object[] repeatedItemChildren = kid1.getInvokes().toArray();
			LinearPattern childLinePattern = (LinearPattern)_foldedInvocations.get(kid1);
			
			/* Walk through the children and draw an invocation if it's NOT a repeat or invoke drawRepetition if the child is a repeat */			
			float childXCoordOffset = parentXCoordOffset;
			float childYCoordOffset = parentYCoordOffset;
			
			for (int k = 0; k < repeatedItemChildren.length; k++)
			{				
				/* We've got a repeatable child here */
				if (childLinePattern != null && childLinePattern.repetition(k) > 1)
				{	
					TRCMethodInvocation firstRepeatableChildMethod = (TRCMethodInvocation)kid1.getInvokes().get(k);
					TRCMethodInvocation lastRepeatableChildMethod = (TRCMethodInvocation)kid1.getInvokes().get(k + childLinePattern.repetition(k) * childLinePattern.length(k) - 1);
					
					if (firstRepeatableChildMethod instanceof TRCFullMethodInvocation && lastRepeatableChildMethod instanceof TRCFullMethodInvocation)
					{
						double methodEntryTimeForFirstChild = ((TRCFullMethodInvocation)firstRepeatableChildMethod).getEntryTime();
						double methodExitTimeForLastChild = ((TRCFullMethodInvocation)lastRepeatableChildMethod).getExitTime();
						
						childXCoordOffset = parentXCoordOffset - ((WIDTH_OF_LINE_ABOVE_LABEL * depth + WIDTH_OF_LINE_ABOVE_LABEL) * cellWidth());
						childYCoordOffset = childYCoordOffset - (PerftraceUtil.getTime(methodEntryTimeForFirstChild) * timescale);
						
						
						drawRepetition(gc, kid1, depth + 1, childXCoordOffset , childYCoordOffset, k, childLinePattern);						
						
						/* Increment 'k' by the length of the repetition and the times that it repeats */
						k = (k + childLinePattern.length(k)) * childLinePattern.repetition(k);	
						
						childYCoordOffset = childYCoordOffset + (PerftraceUtil.getTime(methodExitTimeForLastChild) * timescale);
					}
				}
				/* Otherwise, we just draw the tree represeting the child invocation */
				else
				{	
					TRCMethodInvocation nonRepeatableChildMethod = (TRCMethodInvocation)repeatedItemChildren[k];
					if (nonRepeatableChildMethod instanceof TRCFullMethodInvocation)
					{
						TRCFullMethodInvocation nonRepeatableChildMethodInv = (TRCFullMethodInvocation)nonRepeatableChildMethod;
						double methodEntryTime =  nonRepeatableChildMethodInv.getEntryTime();		
						double methodExitTime =  nonRepeatableChildMethodInv.getExitTime();		
						
						/* The drawTree method will scale and add the method entry time to the offset y value.  We subtract the value from the parent offset y
						 * value in order to reverse this affect. */
						childYCoordOffset = childYCoordOffset - (PerftraceUtil.getTime(methodEntryTime) * timescale);
						
						/* Similar to y-offset, we need to do a similar reverse for the x offset we'll use for the child invocation */
						childXCoordOffset = parentXCoordOffset - ((depth ) * cellWidth() * WIDTH_OF_LINE_ABOVE_LABEL);
						
						//offx + depth * cellWidth() * WIDTH_OF_LINE_ABOVE_LABEL,
						drawTree(gc, (TRCMethodInvocation)repeatedItemChildren[k], depth + 1, childXCoordOffset, childYCoordOffset);
						childYCoordOffset = childYCoordOffset + (PerftraceUtil.getTime(methodExitTime) * timescale);
					}
					
				}
			}

		}
		// string with number of repetition
		canvas.drawString(
			gc,
			" X " + Integer.toString(repet),
			offx + (WIDTH_OF_LINE_ABOVE_LABEL * depth + 6) * cellWidth(),
			startrec + heightrec * .97f,
			display.getSystemColor(SWT.COLOR_BLACK));

	}
	
	/**
	 * 
	 */
	protected void drawThread(GC gc, TRCThread thread, float offx, float offy) {
		JCanvas canvas = jcanvas();
		
		/* Ali M.: Defect 109662 -- Give the canva focus if it doesn't already have focus (this is so that the
		 * right context menu is displayed when the user righ clicks). */
		canvas.getControl().setFocus();
		
		if (canvas == null)
			return;

		Display display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();

		float fontheight = gc.getFontMetrics().getHeight() / canvas.yscale();
		
		canvas.drawLine(
			gc,
			offx,
			canvas.visibleTop(),
			offx,
			canvas.visibleBottom(),
			display.getSystemColor(SWT.COLOR_BLACK));

		// Truncate the thread name depending on length, so that if we have many threads they do not overlap.
		int numCharsToShow = (int)((canvas.xscale() * treeWidth()) / gc.getFontMetrics().getAverageCharWidth());

		String threadName = PerftraceUtil.getThreadName(thread);
		
		if (threadName.length() > numCharsToShow)
		{
			if (numCharsToShow > 4)
				threadName = threadName.substring(0, numCharsToShow - 3) + "...";
			else
				threadName = "";
		}		
		
		canvas.drawString(
			gc,
			threadName,
			offx,
			canvas.visibleTop() + fontheight,
			display.getSystemColor(SWT.COLOR_BLUE));

		// cache these values to use inside drawTree()
		_canvasVisibleBottom = canvas.visibleBottom();
		_canvasVisibleTop = canvas.visibleTop();
		_canvasVisibleLeft = canvas.visibleLeft();
		_canvasVisibleRight = canvas.visibleRight();

		initDrawArray();

        Object[] invocations = thread.getInitialInvocations().toArray();
        
        for(int idx=0; idx< invocations.length; idx++)
        {
			drawTree(
				gc,
				(TRCMethodInvocation)thread.getInitialInvocations().get(idx),
				0,
				offx,
				offy + titleMargin());
        }

		flushDrawArray(gc);
			
			
	}
	protected void drawTimeMarks(GC gc, int offx, int offy) {
		JCanvas c = jcanvas();
		if (isDirty() || c == null)
			return;
		float bufferForTranslations = 40;
		// added for bugzilla_143336 to ensure that regardless of how the
		// platforms display the data, it will not be truncated
		float margin = 50 / c.xscale();
		float rightvis = c.visibleRight();
		long toptime = (long) ((c.visibleTop() - titleMargin()) / timescale);
		long heighttime = (long) (c.visibleHeight() / timescale);
		long interval = heighttime / NRLABELS;
		float x = rightvis - margin;

		float fontheight = gc.getFontMetrics().getHeight() / c.yscale();
		Display display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();

		if(_visibleThreads.size() == 0)
		{
			//look for invocation data
			//if(PerftraceUtil.collectStatisticInfo(_parent.getPage().getMOFObject()))
			if (_parent.getPage().isEmpty()) {
				return;
			}
		}	   

		if (interval > 1) {
			long niceinterval =
				(long) Math.pow(
					10.,
					((int) (Math.log(interval) / Math.log(10)) + 1))
					/ 2;
			long newtoptime =
				niceinterval * ((long) (toptime / niceinterval + 1));
			for (int i = 0; i < NRLABELS * 2; ++i) {
				long timemark = newtoptime + i * niceinterval;
				float y = timemark * timescale + titleMargin();
				
				c.drawString(
					gc,
				    PerftraceUtil.formatTimeValue((double)timemark/1000000),
					offx + x - bufferForTranslations , //added for bugzilla_143336 to have the seconds column aligned
					offy + y,
					display.getSystemColor(SWT.COLOR_BLACK),
					display.getSystemColor(SWT.COLOR_WHITE));

			}
		}
		// bufferForTranslations and division of the y offset
		// added for bugzilla_143336
		
		c.drawString(
			gc,
			TraceUIMessages._110,
			offx + x - bufferForTranslations, // This value is needed for translations of "[seconds]" (i.e. "[segundos]").
			c.visibleTop() + fontheight/(float)1.5,// to move up the label for platform translation so it does not cover the times
			display.getSystemColor(SWT.COLOR_BLACK),
			display.getSystemColor(SWT.COLOR_WHITE));
	}
	
	
	/**
	 * Invoked to draw the tree structure displayed in the execution flow diagram.
	 */
	protected void drawTree(GC gc, TRCMethodInvocation invocation, int depth, float offx, float offy)
	 {
		JCanvas canvas = jcanvas();
		if (invocation == null || !(invocation instanceof TRCFullMethodInvocation)
		    || isDirty() || canvas == null)
			return;

        TRCFullMethodInvocation inv = (TRCFullMethodInvocation) invocation;
		Display display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();

		long start = PerftraceUtil.getTime(inv.getEntryTime());
		long end = PerftraceUtil.getTime(inv.getExitTime());
		
		if (end <= 0)
			end = _maxtime;

		if (offy + start * timescale > _canvasVisibleBottom
			|| offy + end * timescale < _canvasVisibleTop) {
			return;
		}

		if (depth > _maxdepth)
			_maxdepth = depth;

		float height = (end - start) * timescale;

		if (_hidedetails && canvas.yscale() * height < 10f) {
			return;
		}

		boolean selected = false;

        ITraceSelection selmodel = UIPlugin.getDefault().getSelectionModel(_parent.getPage().getMOFObject());
		if (selmodel.contains(inv) || selmodel.contains(inv.getMethod())) {
			canvas.fill3DRect(
				gc,
				offx + (WIDTH_OF_LINE_ABOVE_LABEL * depth) * cellWidth(),
				offy + start * timescale,
				treeWidth() - (WIDTH_OF_LINE_ABOVE_LABEL * depth) * cellWidth(),
				height,
				SpectrumColorMap.getSelectionColor());
			selected = true;
		} else if (selmodel.contains(inv.getOwningObject())) {
			canvas.fill3DRect(
				gc,
				offx + (WIDTH_OF_LINE_ABOVE_LABEL * depth + 3) * cellWidth() - 5f,
				offy + start * timescale,
				treeWidth() - (WIDTH_OF_LINE_ABOVE_LABEL * depth + 3) * cellWidth() + 5f,
				height,
				SpectrumColorMap.getSelectionColor());
			selected = true;
		}

		if (offx + (depth + 1) * cellWidth() * WIDTH_OF_LINE_ABOVE_LABEL < _canvasVisibleRight) {
			
			Object[] segments = inv.getInvokes().toArray();

			LinearPattern lp = linearPattern(inv);

			for (int i = 0; i < segments.length; ++i) {
				
				TRCMethodInvocation vkid = ((TRCMethodInvocation)segments[i]);
				if(!(vkid instanceof TRCFullMethodInvocation))
				  continue;
				 
				TRCFullMethodInvocation kid = (TRCFullMethodInvocation) vkid;  
				int repet;
				if (lp == null || (repet = lp.repetition(i)) == 1) {
					if (PerftraceUtil.getTime(kid.getEntryTime()) * timescale + offy
						> _canvasVisibleBottom) {
						break;
					} else {
						long kidend = PerftraceUtil.getTime(kid.getExitTime());
						if (kidend <= 0)
							kidend = _maxtime;
						if (kidend * timescale + offy < _canvasVisibleTop) {
							continue;
						} else {
							drawTree(gc, kid, depth + 1, offx, offy);
						}
					}
				} else {
					if (_hidedetails)
						continue;
					// draw condensed
					int len = lp.length(i);
					if (repet == 0 || len == 0) {
						System.err.println(
							"linpattern impossible (fanout :" + segments.length + ")");
					} else 
					{						
						drawRepetition(gc, inv, depth, offx, offy, i, lp);
						i += len * repet - 1;
					}
				}
			}
		}

		if (depth < MAXINV)
			push(
				gc,
				depth,
				inv,
				offx + depth * cellWidth() * WIDTH_OF_LINE_ABOVE_LABEL,
				offy + start * timescale,
				cellWidth() * WIDTH_OF_LINE_ABOVE_LABEL,
				height);
		else // buffer for optimization too small
			drawInvocation(
				gc,
				inv,
				offx + depth * cellWidth() * WIDTH_OF_LINE_ABOVE_LABEL,
				offy + start * timescale,
				cellWidth() * WIDTH_OF_LINE_ABOVE_LABEL,
				height,
				true,
				!selected && UIPlugin.getDefault().getSelectionModel(_parent.getPage().getMOFObject()).size() > 1);

		extendToCanvas(inv);
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (6/18/2001 3:19:01 PM)
	 * @param thread org.eclipse.hyades.models.trace.TRCMethodInvocation
	 */
	private void extendToCanvas(TRCMethodInvocation inv) {

		TRCThread thread = inv.getMethod().getDefiningClass().getLoadedBy();
		if (thread == null || !(inv instanceof TRCFullMethodInvocation))
			return;

        TRCFullMethodInvocation curinv = (TRCFullMethodInvocation) inv;
		int column = _visibleThreads.indexOf(thread);
		long start = PerftraceUtil.getTime(curinv.getEntryTime());
		float y = start * timescale + titleMargin();

		float maxX = treeWidth() * column + gcWidth() + 1f;
		float maxY = y;

		TRCMethodInvocation tmp = curinv;
		int depth = -1;
		while (tmp != null) {
			depth++;
			
			if(tmp.getInvokedBy() != null)
				tmp = tmp.getInvokedBy();
			else
			  tmp = null;	
		}
		
		maxX =
			Math.max(
				maxX,
				treeWidth() * column
					+ gcWidth()
					+ (depth + 1) * cellWidth() * WIDTH_OF_LINE_ABOVE_LABEL);

		jcanvas().extendedTo(maxX, maxY);

	}
	/**
	 * This method should fill (or update) the given menu. It is called just before
	 * the menu opens. 
	 */
	public void fillContextMenu(IMenuManager menu) {
		menu.add(fSeparator);
		menu.add(rep1);
		menu.add(rep2);

		menu.add(fSeparator);
		MenuManager popup1 = new MenuManager(TraceUIMessages._18);
		menu.add(popup1);

		popup1.add(_showAll);
		popup1.add(_hideAll);
		popup1.add(fSeparator);

		_gcThread.setText(_gcThread.getName());
		_gcThread.setChecked(_drawgc);

		popup1.add(_gcThread);

		Object[] threads = PerftraceUtil.getAllThreads(_parent.getPage().getMOFObject());
		for (int i = 0; i < threads.length; ++i) {
			TRCThread thread = (TRCThread) threads[i];

			if (!hasThread(thread)) {
				ThreadAction action = new ThreadAction();
				action.setThread(thread);
				_popupThreads.add(action);
			}
		}

		for (int idx = 0; idx < _popupThreads.size(); idx++) {

			ThreadAction action =
				(ThreadAction) _popupThreads.get(idx);
			action.setText(action.getName());
			action.setChecked(_visibleThreads.contains(action.getThread()));

			popup1.add(action);
		}

        menu.add(fSeparator);
        menu.add(fSeparator);
        
		super.fillContextMenu(menu);
	}
	protected void flushDrawArray(GC gc) {
		
		ITraceSelection selModel = UIPlugin.getDefault().getSelectionModel(_parent.getPage().getMOFObject());

		for (int i = 0; i < MAXINV; ++i) {
			if (_drawArray[i] != null) {
				drawInvocation(
					gc,
					_drawArray[i],
					_x[i],
					_y[i],
					_w[i],
					_h[i],
					true,
					selModel.size() > 1
						&& !selModel.contains(_drawArray[i].getOwningObject())
						&& !selModel.contains(_drawArray[i].getMethod()));
				
				_drawArray[i] = null;		
			}
		}
		
	}
	protected int gcWidth() {
		return _drawgc ? 300 : 100;
	}
	
	protected boolean hasThread(TRCThread thread) {
		
		for (int i = 0; i < _popupThreads.size(); ++i) {
			ThreadAction action =
				(ThreadAction) _popupThreads.get(i);
			if (action.getThread() == thread)
				return true;
		}

		return false;
	}
	
	public float height() {
		double mt = PerftraceUtil.getMaximumTime(_parent.getPage().getMOFObject());
		if (mt == 0) mt = 1;
 		_maxtime = PerftraceUtil.getTime(mt);
		return _maxtime * timescale + titleMargin() + 10 * cellHeight();
	}
	protected void initDrawArray() {
		for (int i = 0; i < MAXINV; ++i) {
			_drawArray[i] = null;
			_x[i] = _y[i] = _w[i] = _h[i] = 0f;
		}
	}
	public void keyPressed(org.eclipse.swt.events.KeyEvent e) {

		JCanvas canvas = jcanvas();
		if (canvas == null)
			return;

		if (e.keyCode == SWT.CONTROL) {
			_controlDown = true;

		} else if (e.keyCode == SWT.HOME) {
			canvas.zoomToFit();
		} else {
			float x = canvas.normX(canvas.getSize().width / 2);
			float y = canvas.normY(canvas.getSize().height / 2);
			if (e.character == ',' || e.character == '<') //||
				//				e.character == KeyEvent.VK_DIVIDE)
				{
				canvas.zoom(1f, 1 / MAG_STEP, x, y);
			} else if (e.character == '.' || e.character == '>') //||
				//					   e.character == KeyEvent.VK_MULTIPLY)
				{
				canvas.zoom(1f, MAG_STEP, x, y);
			}
		}
	}
	protected LinearPattern linearPattern(TRCMethodInvocation inv) {
		return (LinearPattern) _foldedInvocations.get(inv);
	}
	public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
		if (e.button == 3) //right click buton
			return;

		super.mouseDown(e);
	}
	public void mouseUp(org.eclipse.swt.events.MouseEvent e) {

		if (e.button == 3) //right click buton
			return;

		super.mouseUp(e);
	}
	
	/**
	 * 
	 */
	public void moved(float x, float y) {
		
		if (x <= gcWidth()) {
			
			// search collected objects			
			Object[] classes = PerftraceUtil.getAllClasses(_parent.getPage().getMOFObject());
			for(int idx=0; idx<classes.length; idx++)
			{	
				TRCClass clas = (TRCClass) classes[idx];
			
				ArrayList objects = new ArrayList();
				objects.addAll(clas.getObjects());
				objects.addAll(clas.getClassObjects());
				for(int i = 0; i<objects.size(); i++)
				{
					if(!(objects.get(i) instanceof TRCFullTraceObject))
					  continue;
				  				  				 
					TRCFullTraceObject o = (TRCFullTraceObject)objects.get(i);
					if(o.getCollectTime() == 0)
					  continue;
					
					float yo = TString.getTime(o.getCollectTime()) * timescale + titleMargin();
					if (y >= yo) {
						
						if (y <= yo + cellHeight() * 2)
						 {
							
							IContextLabelFormatProvider cflp = ContextManager.getContextLabelFormatProvider(ContextUpdaterHelper.getContext(o), IContextAttributes.GARBAGE_COLLECTED_OBJECT, IContextLabelFormatProvider.MODE_COLUMN_HEADER);
	
							status(NLS.bind(TraceUIMessages._76,
									cflp.getDisplayStringFromElement(IContextAttributes.GARBAGE_COLLECTED_OBJECT, null, IContextLabelFormatProvider.MODE_COLUMN_HEADER),
									PerftraceUtil.getClass(o).getName() + "." + o.getId()));
							return;
						}
					} else {
						status(TraceUIMessages._75);
						return;
					}
				}
			}
			status(TraceUIMessages._75);
			return;
			
		}
		
		x -= gcWidth();
		int threadnumber = (int) (x / treeWidth());

		if (threadnumber < 0 || threadnumber >= _visibleThreads.size()) {
			long tm = (long) ((y - titleMargin()) / timescale);

			status(NLS.bind(TraceUIMessages._77, String.valueOf(tm)));
			return;
		}
		TRCThread thread = (TRCThread) _visibleThreads.get(threadnumber);
		x = x % treeWidth();
		
		Object[] segments = thread.getInitialInvocations().toArray();
		for(int idx=0; idx<segments.length; idx++)
		{
			TRCMethodInvocation inv = subtreeContains((TRCMethodInvocation)segments[idx], x, y, 0);			
			if (inv != null && inv instanceof TRCFullMethodInvocation) {
	 
				String fullyQualMethod = new FullyQualMethodInvLabel().getDisplayString(inv, null);
				String instName = new InstanceNameColumnLabel().getDisplayString(inv, null);
				String threadName = getContextLabelFormatProvider(ContextUpdaterHelper.getContext(inv), IContextAttributes.THREAD_NAME, IContextLabelFormatProvider.MODE_COLUMN_HEADER).getDisplayStringFromElement(null, thread, IContextLabelFormatProvider.MODE_COLUMN_HEADER);
				String threadHeader = new ThreadNameColumnLabel().getDisplayString(thread, null);
				String entryTime = new MethodInvocationEntryTimeColumnLabel().getDisplayString(inv, null);
				
				CumulativeTimeColumnLabel cumulTimeCol = new CumulativeTimeColumnLabel();
				ColumnDisplayInfo info = ContextUpdaterHelper.updateCumulTime(cumulTimeCol, false, false, _drawMode, 1);
				String cumulTime = cumulTimeCol.getDisplayString(inv, info);

				String cumulTimeType;
				if (_drawMode == TraceConstants.COMPENSATED_TIME)
					cumulTimeType = getContextLabelFormatProvider(ContextUpdaterHelper.getContext(inv), IContextAttributes.METHOD_CUMULATIVE_TIME, IContextLabelFormatProvider.MODE_COLUMN_HEADER).getDisplayStringFromElement(null, inv, IContextLabelFormatProvider.MODE_COLUMN_HEADER);
				else
					cumulTimeType = getContextLabelFormatProvider(ContextUpdaterHelper.getContext(inv), IContextAttributes.METHOD_RAW_CUMULATIVE_TIME, IContextLabelFormatProvider.MODE_COLUMN_HEADER).getDisplayStringFromElement(null, inv, IContextLabelFormatProvider.MODE_COLUMN_HEADER);
	
				String message = TraceUIMessages._78;

				message =
					NLS.bind(
						message,
						new Object[] {
						threadHeader	
						, new StringBuffer().append(threadName).append("   ").append(fullyQualMethod)
						, instName
						, entryTime
						, cumulTimeType
						, cumulTime						
						});
							
				status(message);
				
				return;
	
			}
			
		}			
		long tm = (long) ((y - titleMargin()) / timescale);

		String threadHeader = getContextLabelFormatProvider(ContextUpdaterHelper.getContext(thread), IContextAttributes.THREAD_NAME, IContextLabelFormatProvider.MODE_COLUMN_HEADER).getDisplayStringFromElement(null, thread, IContextLabelFormatProvider.MODE_COLUMN_HEADER);
		
		String message = NLS.bind(TraceUIMessages._79, new Object[] {
				threadHeader, thread.getName(), TString.formatTimeValue((double)tm/1000000)});
		
		status(message);
		
	}
	
	private IContextLabelFormatProvider getContextLabelFormatProvider(String context, String attributeId, int mode)
	{
		return ContextManager.getContextLabelFormatProvider(context, attributeId, mode);
	}
	
	
	/*  This is an optimization to draw many small invocations on a big screen. For
		example to redraw the whole trace takes a long time, because every small invocation
		(usually hundreds of thousands) have to be redrawn on the screen. What happened in
		reality, is that hundreds of invocations are drawn on the same pixel(s). We can't
		just filter out small invocations, because this would not show "busy" program parts
		with thousands of small invocations.
		The optimization is twofold:
		1. if two invocations are very close to each other (in time, and therefore in space
		on the screen), the string of the first invocation was overwritten by the
		method line and/or string of the second one. We can avoid this by not drawing the
		string of the first invocation when the second invocation is very close.
		2. if two invocations are so close and so small,
		that they probably are drawn in the same
		horizontal line of pixels, we dont' draw the second invocation. The effect is the
		same, namely, that this line of pixels is filled. It doesn't matter which color
		it is, since both invocations are very small.
	
		In order to find the next lower invocation, we use a buffer (_drawArray[], + other
		arrays) to draw a thread. In this buffer, we keep track of the last "to be
		drawn" invocation for a given stackdepth. Rather than drawing invocations
		immediately as we traverse the tree, we draw it "one step behind".
		We store exactly one invocation (per depth), so that we can compare it with the
		next one.
		flushDrawArray is used to draw the last invocation (still in the buffer).
		*/

	protected void push(
		GC gc,
		int i,
		TRCMethodInvocation inv,
		float x,
		float y,
		float w,
		float h) {

		JCanvas canvas = jcanvas();
		if (i >= MAXINV || canvas == null)
			return;

		if (_drawArray[i] != null) {
			float yscale = canvas.yscale();
			float spaceforstring = (y - _y[i]) * yscale;
			float spaceforinvoc = (y + h - _y[i]) * yscale;
			if (spaceforinvoc < 1.)
				return;
			drawInvocation(
				gc,
				_drawArray[i],
				_x[i],
				_y[i],
				_w[i],
				_h[i],
				(spaceforstring > 8.),
				true);

		}
		_drawArray[i] = inv;
		_x[i] = x;
		_y[i] = y;
		_w[i] = w;
		_h[i] = h;
	}
	public void redraw() {
		
		_visibleThreads.clear();

       Object[] threads = PerftraceUtil.getAllThreads(_parent.getPage().getMOFObject());
		for (int idx = 0; idx < threads.length; idx++) {
			_visibleThreads.add(threads[idx]);
		}
		
		jcanvas().zoomToFill(1f, 1000f);
		jcanvas().redraw();
	}
	protected void removeLinearPatterns() {
		_foldedInvocations.clear();
	}
	
	protected void removeLinearPatterns(TRCMethodInvocation root) {
		_foldedInvocations.remove(root);
		
		Object[] segments = root.getInvokes().toArray();		
		for (int i = 0; i < segments.length; ++i)
			removeLinearPatterns(((TRCMethodInvocation)segments[i]));
	}
	
	public void selected(
		float x,
		float y,
		boolean shiftdown,
		boolean controldown,
		boolean metadown) {
			
		ITraceSelection _selectionmodel = UIPlugin.getDefault().getSelectionModel(_parent.getPage().getMOFObject());
	
		if (x <= gcWidth()) {
			if (!metadown) {
			} else {
				return;
			}
		}
		x -= gcWidth();
		int threadnumber = (int) (x / treeWidth());
	
		if (threadnumber < 0 || threadnumber >= _visibleThreads.size())
			return;
		TRCThread thread = (TRCThread) _visibleThreads.get(threadnumber);
	
		x = x % treeWidth();
		
		Object[] invocations = thread.getInitialInvocations().toArray();
		
		for(int idx=0; idx<invocations.length; idx++)
		{
			TRCMethodInvocation selinv =
				subtreeContains((TRCMethodInvocation)invocations[idx], x, y, 0);
			
			_selectionmodel.add(selinv);
		};
		
		ViewSelectionChangedEvent event = UIPlugin.getDefault().getViewSelectionChangedEvent();
		event.setSource(_parent.getPage().getMOFObject());
		UIPlugin.getDefault().notifyViewSelectionChangedListener(event);
	}
	public void shutdown() {
		
		_parent = null;
        _foldedInvocations.clear();
        _hideAll = null;
        _showAll = null;
        rep1 = null;
        rep2 = null;
        _visibleThreads.clear();
        
        for(int idx=0; idx<_popupThreads.size(); idx++)
        {
        	((ThreadAction)_popupThreads.get(idx)).setThread(null);
        }        
        _popupThreads.clear();
        
        _gcThread = null;                		
	}
	protected void status(String s) {
		
		_parent.updateStatus(s);
	}
	protected TRCMethodInvocation subtreeContains(
		TRCMethodInvocation invocation,
		float xs,
		float ys,
		int d) {
		if (invocation == null || !(invocation instanceof TRCFullMethodInvocation))
			return null;

        TRCFullMethodInvocation inv = (TRCFullMethodInvocation) invocation;
		int depth = (int) (xs / cellWidth() / WIDTH_OF_LINE_ABOVE_LABEL);

		if (d == depth) {
			float startrec =
				PerftraceUtil.getTime(inv.getEntryTime())* timescale + titleMargin();
					
			double time = inv.getExitTime();
			if(time == 0){
				/* Ali M.: Defect 109662 -- inv.getOwningObject().getProcess().getLasEventTime() causes an NPE.  inv.getProcess().getLasEventTime() should be used */
				time = inv.getProcess().getLastEventTime();
			}
			float endrec =
				PerftraceUtil.getTime(time) * timescale + titleMargin();
			if (ys >= startrec && ys <= endrec)
				return inv;
			else
				return null;

		} else if (d > depth) {
			return null;

		} else { // take into account the repetitions

			LinearPattern lp = null;
			if (xs / cellWidth() > (d + 1) * WIDTH_OF_LINE_ABOVE_LABEL + 2)
				lp = linearPattern(inv);

			Object[] segments = inv.getInvokes().toArray();
			for (int i = 0; i < segments.length; ++i) {
				TRCFullMethodInvocation calleeinv = ((TRCFullMethodInvocation)segments[i]);
				if (ys < PerftraceUtil.getTime(calleeinv.getEntryTime()) * timescale + titleMargin())
					return null;

				int repet;
				if (lp == null
					|| (repet = lp.repetition(i)) == 1) { // no repetition
					if (calleeinv.getExitTime() == 0
						|| PerftraceUtil.getTime(calleeinv.getExitTime()) * timescale + titleMargin() >= ys) {
						return subtreeContains(calleeinv, xs, ys, d + 1);
					}

				} else { // repetition. find the components of the first word.
					int len = lp.length(i);
					if (i >= segments.length)
					{
						return null; //should never happen
					}

					float startrec =
					PerftraceUtil.getTime(calleeinv.getEntryTime())* timescale + titleMargin();
						
					TRCFullMethodInvocation inv2 = ((TRCFullMethodInvocation)segments[i + repet * len - 1]);
					float endrec =
					PerftraceUtil.getTime(inv2.getExitTime()) * timescale
							+ titleMargin();
					if (ys >= startrec && ys <= endrec) {
						if (lp != null
							&& repet > 1
							&& xs / cellWidth() > (d + 1) * WIDTH_OF_LINE_ABOVE_LABEL + 7)
							return null;
						float heightrec = endrec - startrec;
						
					   TRCFullMethodInvocation inv3 = ((TRCFullMethodInvocation)segments[i + len - 1]);
						
						float endword =
						PerftraceUtil.getTime(inv3.getExitTime()) * timescale
								+ titleMargin();
						float heightword = endword - startrec;
						if (heightword < 0f)
							heightword = 0f;
						float ratio = 1f;
						if (heightword > 0)
							ratio = (endrec - startrec) / heightword * .9f;
						// occupy 90%
						// first word in this repetition
						for (int j = 0; j < len; ++j) {
							
							TRCFullMethodInvocation kid1 = ((TRCFullMethodInvocation)segments[i + j]);
							float start1 =
							PerftraceUtil.getTime(kid1.getEntryTime()) * timescale + titleMargin();
							float end1 = PerftraceUtil.getTime(kid1.getExitTime()) * timescale + titleMargin();
							float h1 = end1 - start1;
							float startmag =
								startrec
									+ (start1 - startrec) * ratio
									+ heightrec / 20f;
							float hmag = h1 * ratio;
							if (ys < startmag)
								return null;
							if (ys < startmag + hmag)
								return kid1;
						}
					}
					i += len * repet - 1;
				}
			}
			return null;
		}
	}
	public String title() {
		return "";
	}
	protected int titleMargin() {
		return 0;
	}
	
	protected int treeWidth() {
		
		if (_maxdepth == 0) {						
	
			for (int idx=0; idx<_visibleThreads.size(); idx++) {
				TRCThread thread = (TRCThread) _visibleThreads.get(idx);
				
				if(_maxdepth < thread.getMaxStackDepth())
				   _maxdepth = thread.getMaxStackDepth();
			}
		}
		
		return cellWidth() * WIDTH_OF_LINE_ABOVE_LABEL * (10 + (_maxdepth / 10) * 10);
	}
	public float width() {
		
		
		float width =  gcWidth()
			+ _visibleThreads.size() * treeWidth()
			+ 1f;
			
		return width;	

	}

	public void setDrawMode(int drawMode) {
		_drawMode = drawMode;
	}
	
}
