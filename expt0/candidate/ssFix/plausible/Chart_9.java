/***************************************
 *            ViPER                    *
 *  The Video Processing               *
 *         Evaluation Resource         *
 *                                     *
 *  Distributed under the GPL license  *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/

package edu.umd.cfar.lamp.chronicle;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.beans.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import edu.umd.cfar.lamp.chronicle.markers.*;
import edu.umd.cfar.lamp.viper.media.time.*;
import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.util.*;

/**
 * A basic timeline control. Presents three segments: the row headers, the
 * ruler, and the lines themselves. If you wish to put this in a scroll pane,
 * set labelsOutside to 'true' and use the get_Header methods to set up the
 * scroll pane.
 * 
 * @author davidm
 */
public class ChronicleViewer extends JPanel {
	private transient PCanvas proxy;

	// / Generates the piccolo views of the timeline nodes
	private RendererCatalogue factory;

	// / Maintains the list of timeline input event handlers
	private EventListenerList listenerList = new EventListenerList();

	private double footerHeight = 0;

	// / the y-zoom
	private double lineZoom = 1.0;

	// / The total height of the lines
	private double linesHeight = 0;

	// / The number of units of length to devote to the chronicle's lines
	private double viewLength = 600;

	// / The length of the headers before each line (where the label is)
	private double labelLength = 128;

	// / The data model used.
	protected ChronicleViewModel model;

	// / The selection model; useful for selecting lines and ranges of time
	protected ChronicleSelectionModel selectionModel;

	// / Listens for changes in the data and redraws the display as appropriate
	private ChronicleListener cl = new ChronicleListener();

	/**
	 * This holds all of the user specfied markers and the current frame arrow.
	 */
	public AllChronicleMarkersNode getMarkersNode() {
		return (AllChronicleMarkersNode) getProxy().getLayer().getChild(
				MARKERS_NODE_OFFSET);
	}

	// / This color is the background for the headers
	private Paint headColor = Color.lightGray;

	transient private ScrollViews scrollViews = null;

	private final class TimeSelectionEventListener implements ChangeListener,
			Serializable {
		public void stateChanged(ChangeEvent e) {
			getTimeSelectionOverlay().resetRanges();
			resetView();
		}
	}

	private final class MarkerToMajorMomentListener implements
			ChronicleMarkerListener, Serializable {
		public void markersChanged(ChronicleMarkerEvent e) {
			if (e.getType() == ChronicleMarkerEvent.MOVED) {
				if (e.getChangedMarker().equals(getMajorMomentMarker())) {
					getModel().setMajorMoment(e.getChangedMarker().getWhen());
				}
			}
		}
	}

	/**
	 * The scroll views are windows for the corresponding segements of a java
	 * swing scroll pane.
	 */
	public class ScrollViews {
		/**
		 * The west widget scrolls vertically. This includes timeline names and
		 * emblems.
		 */
		public PCanvas rowHeader = null;

		/**
		 * The northwest widget doesn't scroll at all.
		 */
		public PCanvas cornerHeader = null;

		/**
		 * The north scroll view, this scrolls only horizontally. This includes
		 * the ruler, frame numbers, and any marker headers.
		 */
		public PCanvas columnHeader = null;

		/**
		 * The content widget scrolls both vertically and horizontally.
		 */
		public PCanvas content = null;

		private PCamera cornerCam;

		private PCamera labelCam;

		private PCamera rulerCam;

		private PCamera contentCam;

		ScrollViews() {
			labelCam = new PCamera();
			labelCam.addLayer(getProxy().getLayer());
			this.rowHeader = new SimpleCanvas(labelCam);
			this.rowHeader
					.addInputEventListener(new ChroniclePanEventHandler());
			this.rowHeader.addInputEventListener(new TimeLineInputProxy());

			cornerCam = new PCamera();
			cornerCam.addLayer(getProxy().getLayer());
			this.cornerHeader = new SimpleCanvas(cornerCam);
			this.cornerHeader
					.addInputEventListener(new ChroniclePanEventHandler());

			rulerCam = new PCamera();
			rulerCam.addLayer(getProxy().getLayer());
			this.columnHeader = new SimpleCanvas(rulerCam);
			columnHeader.addInputEventListener(new ChronicleWheelZoomHandler(
					ChronicleViewer.this));
			columnHeader.addInputEventListener(new ChroniclePanEventHandler());
			columnHeader.addInputEventListener(new TimeLineInputProxy());

			contentCam = new PCamera();
			contentCam.addLayer(getProxy().getLayer());
			content = new SimpleCanvas(contentCam);
			// content.getLayer().addChild(contentCam);

			resetCameras();
			content.getCamera().addPropertyChangeListener(
					PCamera.PROPERTY_VIEW_TRANSFORM, recenterOtherCameras);
			content.addComponentListener(resizeEventListener);
			content.addInputEventListener(new ChronicleWheelZoomHandler(
					ChronicleViewer.this));
			content.addInputEventListener(new ChroniclePanEventHandler());
			content.addInputEventListener(new TimeLineInputProxy());
		}

		/**
		 * Sets the pan handler on all four views.
		 * 
		 * @param handler
		 *            the new handler
		 */
		public void setPanEventHandler(PPanEventHandler handler) {
			this.rowHeader.setPanEventHandler(handler);
			this.cornerHeader.setPanEventHandler(handler);
			this.columnHeader.setPanEventHandler(handler);
			this.content.setPanEventHandler(handler);
		}

		/**
		 * Sets the zoom handler on all four views.
		 * 
		 * @param handler
		 *            the new handler
		 */
		public void setZoomEventHandler(PZoomEventHandler handler) {
			this.rowHeader.setZoomEventHandler(handler);
			this.cornerHeader.setZoomEventHandler(handler);
			this.columnHeader.setZoomEventHandler(handler);
			this.content.setZoomEventHandler(handler);
		}

		void resetCameras() {
			double vl = Math.max(32, getViewLength());
			double vh = Math.max(32, linesHeight + footerHeight);

			cornerCam.setViewOffset(0, 0);
			cornerCam.setBounds(0, 0, labelLength, getRulerHeight());
			cornerHeader.setPreferredSize(new Dimension((int) getLabelLength(),
					getRulerHeight()));

			rulerCam.setViewOffset(-labelLength, 0);
			rulerCam.setBounds(0, 0, vl, getRulerHeight());
			columnHeader.setPreferredSize(new Dimension((int) vl,
					getRulerHeight()));

			labelCam.setViewOffset(0, -getRulerHeight());
			labelCam.setBounds(0, 0, labelLength, vh);
			rowHeader.setPreferredSize(new Dimension((int) labelLength,
					(int) vh));

			contentCam.setViewOffset(-labelLength, -getRulerHeight());
			contentCam.setBounds(0, 0, vl, vh);
			content.setPreferredSize(new Dimension((int) vl, (int) vh));
		}

		// / This listens for resize events and appropriately changes the
		// / viewLength, making sure that it is at least as large as the current
		// view
		private ComponentListener resizeEventListener = new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (getViewLength() < getMinimumViewLength()) {
					setViewLength(getMinimumViewLength());
				} else {
					resetCameras();
				}
			}
		};

		private PropertyChangeListener recenterOtherCameras = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(
						PCamera.PROPERTY_VIEW_TRANSFORM)) {
					// resetCameras();
				}
			}
		};
	}

	private ChronicleMarkerListener cml = new MarkerToMajorMomentListener();

	// / The popup that appears when a use context-clicks on a marker.
	private transient ChronicleMarkerPopUp markerPop;

	public void setTransferHandler(TransferHandler newHandler) {
		super.setTransferHandler(newHandler);
		if (scrollViews != null || true) {
			resetScrollViewsTransferHandler();
		}
	}

	/**
	 * 
	 */
	private void resetScrollViewsTransferHandler() {
		getScrollViews().columnHeader.setTransferHandler(getTransferHandler());
		getScrollViews().content.setTransferHandler(getTransferHandler());
		getScrollViews().cornerHeader.setTransferHandler(getTransferHandler());
		getScrollViews().rowHeader.setTransferHandler(getTransferHandler());
	}

	private ChronicleMarker getMajorMomentMarker() {
		return getMarkersNode().getModel().getMarker(0);
	}

	/**
	 * Gets the marker model associated with this viewer.
	 * 
	 * @return the marker model
	 */
	public ChronicleMarkerModel getMarkerModel() {
		return getMarkersNode().getModel();
	}

	private class ChronicleListener implements ChronicleViewListener {
		/**
		 * Calls resetView {@inheritDoc}
		 */
		public void focusChanged(EventObject e) {
			// XXX Should only redo what's changed, instead of everything
			// focus change indicates need to redo markers, but only whole
			// view if focusInterval changed.
			resetView();
			getOverlay().redoLength();
			getOverlay().redoHeight();
		}

		/**
		 * Calls resetMarkers {@inheritDoc}
		 */
		public void momentChanged(EventObject e) {
			resetMarkers();
		}

		/**
		 * Calls resetView {@inheritDoc}
		 */
		public void dataChanged(EventObject e) {
			resetLineLayer();
			resetCameras();
			getOverlay().redoHeight();
		}
	};

	private class OverlayLayoutManager extends PLayer {
		private boolean dirtyLength = false;

		private boolean dirtyHeight = false;

		public void redoLength() {
			dirtyLength = true;
			invalidateLayout();
		}

		public void redoHeight() {
			dirtyHeight = true;
			invalidateLayout();
		}

		public void layoutChildren() {
			if (dirtyLength || dirtyHeight) {
				Iterator i = getChildrenIterator();
				while (i.hasNext()) {
					PNode each = (PNode) i.next();
					if (each instanceof NodeWithViewLength) {
						NodeWithViewLength n = (NodeWithViewLength) each;
						if (dirtyHeight && dirtyLength) {
							n.viewAndDataChanged();
						} else if (dirtyHeight) {
							n.dataHeightChanged();
						} else {
							n.viewLengthChanged();
						}
					}
				}
			}
			dirtyLength = false;
			dirtyHeight = false;
		}
	}

	private class LineLayoutManager extends PNode {
		double[] lineOffsets = new double[0];

		double instant2local(Instant i) {
			double offset = i.doubleValue();
			double focalWidth = model.getFocus().width();
			offset = offset * getViewLength() / focalWidth;
			return offset;
		}

		Instant local2instant(double d) {
			long focalWidth = model.getFocus().width();
			long offset = (long) (d * focalWidth / getViewLength());
			return model.getFocus().getStart().go(offset);
		}

		double[] timeline2local(TimeLine tqe) {
			int i = model.indexOf(tqe);
			if (i < 0) {
				return null;
			}
			return new double[] { lineOffsets[i + 1], lineOffsets[i + 2] };
		}

		TimeLine local2timeline(double d) {
			int i = Arrays.binarySearch(lineOffsets, d);
			if (i < 0) {
				// i = -insertionPoint - 1 -> insertionPoint = -i - 1 -> line =
				// -i - 2
				i = -i - 2;
			} else {
				i--;
			}
			if (i < 1 || i >= lineOffsets.length - 1) {
				return null;
			}
			if (model.getSize() != lineOffsets.length - 2) {
				// XXX: local2timeline only works if the timeline has been drawn since the model has changed.
				return null;
			}
			return model.getElementAt(i - 1); // take into account ruler
		}

		void layoutHorizontalChildren() {
			double xOffset = this.getX();
			double yOffset = this.getY();

			Iterator i = getChildrenIterator();
			int offsetIndex = 0;
			while (i.hasNext()) {
				PNode each = (PNode) i.next();
				each.setOffset(xOffset, yOffset - each.getY());
				lineOffsets[offsetIndex++] = yOffset;
				yOffset += each.getHeight();
			}
			lineOffsets[offsetIndex] = yOffset;
		}

		void layoutVerticalChildren() {
			double xOffset = this.getWidth();
			double yOffset = labelLength;

			Iterator i = getChildrenIterator();
			int offsetIndex = 0;
			while (i.hasNext()) {
				PNode each = (PNode) i.next();
				each.setOffset(xOffset - each.getX(), yOffset);
				lineOffsets[offsetIndex++] = xOffset;
				xOffset += each.getWidth();
			}
			lineOffsets[offsetIndex] = xOffset;
		}

		/**
		 * {@inheritDoc}
		 */
		public void layoutChildren() {
			if (lineOffsets.length != getChildrenCount() + 1) {
				lineOffsets = new double[getChildrenCount() + 1];
			}
			if (getOrientation() == Adjustable.VERTICAL) {
				layoutVerticalChildren();
			} else {
				layoutHorizontalChildren();
			}
		}
	}

	transient private PNode headerSlug;

	transient private PNode cornerNode;

	/**
	 * String indicating
	 */
	public static final String CURR_FRAME_LABEL = "Current Frame";

	private class TimeLineInputProxy implements PInputEventListener,
			Serializable {
		public void processEvent(PInputEvent e, int type) {
			fireTimeLineInputEvent(e, type);
		}
	}

	/**
	 * Refreshes the chronicle display.
	 */
	public void resetView() {
		recenter();
		resetLineLayer();
		resetMarkers();
		resetCameras();
	}
	
	protected boolean dirtyCenter = false;
	protected boolean dirtyLineLayer = false;
	protected boolean dirtyMarkers = false;
	protected boolean dirtyCameras = false;
	
	protected void resetCameras() {
		if (scrollViews == null) {
			return;
		}
		dirtyCameras = true;
		sendRepaint();
	}
	
	protected void finishReset() {
		finishRecenter();
		finishResetLineLayer();
		finishResetMarkers();
		finishResetCameras();
	}
	
	protected void finishResetCameras() {
		if (!dirtyCameras) {
			return;
		}
		if (scrollViews != null) {
			scrollViews.resetCameras();
		}
		dirtyCameras = false;
	}

	/**
	 * Makes sure the current view contains the extents of the focus.
	 */
	protected void recenter() {
		dirtyCenter = true;
		sendRepaint();
	}

	/**
	 * 
	 */
	private void sendRepaint() {
		if (proxy != null) {
			proxy.repaint();
		}
		if (scrollViews != null) {
			scrollViews.cornerHeader.repaint();
			scrollViews.rowHeader.repaint();
			scrollViews.columnHeader.repaint();
			scrollViews.content.repaint();
		}
	}
	protected void finishRecenter() {
		if (!dirtyCenter) {
			return;
		}
		dirtyCenter = false;
		PCamera c = getProxy().getCamera();
		// Find what the first and last pixel should be
		// Ignore this if focus is null or empty
		if (model == null || model.getFocus() == null
				|| model.getFocus().isEmpty()) {
			return;
		}
		PAffineTransform camTransform = c.getViewTransformReference();
		double currXOffset = -camTransform.getTranslateX();
		double totalXWidth = getViewLength() + getViewOffsetX();
		double maxXOffset = totalXWidth - c.getBoundsReference().getWidth();
		if (maxXOffset < currXOffset) {
			c.setViewOffset(-maxXOffset, camTransform.getTranslateY());
		}
	}

	/**
	 * Changes the focus to the current focus of the mfl, and changes the cursor
	 * to lay over the current major moment.
	 */
	public void resetMarkers() {
		dirtyMarkers = true;
		sendRepaint();
	}
	
	protected void finishResetMarkers() {
		if (!dirtyMarkers) {
			return;
		}
		dirtyMarkers = false;
		getMarkersNode()
				.setBounds(
						getViewOffsetX(),
						0,
						getViewLength(),
						getPreferredViewHeight() + getViewOffsetY()
								+ getFooterHeight());
		if (model != null) {
			InstantInterval all = model.getFocus();
			if (all == null) {
				return;
			}
			getMarkersNode().setHeaderHeight(getRulerHeight());
			getMarkersNode().setFooterHeight(footerHeight);
			getMarkersNode().getModel().setInterval(all);
			ChronicleMarker nowMarker = getMajorMomentMarker();
			nowMarker.setWhen(model.getMajorMoment());
		}
	}

	/**
	 * Refreshes all the timelines and their boundary.
	 * Replaces the stuff attached to the LineLayer, the LabelLayer, and resizes the Underlay
	 */
	public void resetLineLayer() {
		dirtyLineLayer = true;
		sendRepaint();
	}
	
	protected void finishResetLineLayer() {
		if (!dirtyLineLayer) {
			return;
		}
		dirtyLineLayer = false;
		// clean line layer
		getLineLayer().removeAllChildren();
		headerSlug = null;
		getLineLayer().addChild(getHeaderSlug());
		linesHeight = 0;

		// clean label layer
		getLabelLayer().removeAllChildren();
		cornerNode = null;
		getLabelLayer().addChild(getCornerNode());

		if (model != null) {
			InstantInterval focus = model.getFocus();
			for (int i = 0; i < model.getSize(); i++) {
				resetTimeLine(i);
				PNode view = getLineLayer().getChild(i+1);
				if (this.getOrientation() == Adjustable.HORIZONTAL) {
					linesHeight += view.getBoundsReference().height;
				} else {
					linesHeight += view.getBoundsReference().width;
				}
			}
		}
		getUnderlay()
				.setBounds(
						0,
						0,
						viewLength,
						getPreferredViewHeight() + getViewOffsetY()
								+ getFooterHeight());
		getUnderlay().setOffset(getViewOffsetX(), 0);
	}

	/**
	 * Redraws the requested timeline.
	 * @param i the line to redraw, from model.getElementAt(i).
	 */
	public void resetTimeLine(int i) {
		if (dirtyLineLayer) {
			return;
		}
		if (i+1 < getLineLayer().getChildrenCount()) {
			PNode old = getLineLayer().getChild(i+1);
			if (old.getAttribute("timeline") != null) {
				getLineLayer().removeChild(i+1);
				if (i + 1 <= getLabelLayer().getChildrenCount()) {
					getLabelLayer().removeChild(i+1);
				}
			}
		}
		
		TimeLine tqe = model.getElementAt(i);
		TimeLineRenderer renderer = factory.getTimeLineRenderer(tqe);
		boolean currSelected = getSelectionModel().isSelected(tqe);
		boolean currFocus = false; // XXX implement focus
		double neoSize = renderer.getPreferedTimeLineInfoLength(this,
				tqe, currSelected, currFocus, (int) viewLength,
				getOrientation());
		PNode view = renderer.getTimeLineRendererNode(this, tqe,
				currSelected, currFocus, (int) viewLength,
				(int) neoSize, getOrientation());
		view.addAttribute("timeline", tqe);
		getLineLayer().addChild(i+1, view);

		PNode newLabel = renderer.generateLabel(this, tqe,
				currSelected, currFocus, (int) neoSize,
				getOrientation());
		PBounds viewBounds = view.getBoundsReference();
		double prefHeight = newLabel.getFullBounds().getHeight();
		if (prefHeight > viewBounds.getHeight()) {
			view.setBounds(viewBounds.x, viewBounds.y,
					viewBounds.width, prefHeight);
			newLabel.setBounds(0, 0, getViewOffsetX(), prefHeight);
		} else {
			newLabel.setBounds(0, 0, getViewOffsetX(), viewBounds
					.getHeight());
		}
		getLabelLayer().addChild(i+1, newLabel);
	}

	private PNode getHeaderSlug() {
		if (headerSlug == null) {
			this.headerSlug = new PNode();
			headerSlug.setBounds(0, 0, getViewLength(), getRulerHeight());
		}
		return headerSlug;
	}

	private PNode getCornerNode() {
		if (cornerNode == null) {
			boolean horizontal = this.getOrientation() == SwingConstants.HORIZONTAL;
			cornerNode = new PNode();
			cornerNode.setBounds(0, 0, horizontal ? labelLength
					: getRulerHeight(), horizontal ? getRulerHeight()
					: labelLength);
		}
		return cornerNode;
	}

	/**
	 * Zoom out to fit the timelines in the window.
	 */
	public void fitInWindow() {
		Instant i = model.getMajorMoment();
		if (i != null) {
			this.setZoom(1, model.getMajorMoment());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Dimension getPreferredSize() {
		if (super.isPreferredSizeSet()) {
			return super.getPreferredSize();
		}
		double height = getRulerHeight() + linesHeight;
		double width = labelLength + getViewLength();
		Dimension rval = new Dimension((int) (width + .9), (int) (height + .9));
		return rval;
	}
	
	

	/**
	 * {@inheritDoc}
	 */
	public Dimension getMinimumSize() {
		if (super.isMinimumSizeSet()) {
			return super.getMinimumSize();
		}
		int height = getRulerHeight();
		int width = (int) Math.ceil(getLabelLength());
		return new Dimension(1 + width, 1 + height);
	}

	/**
	 * {@inheritDoc}
	 */
	public Dimension getMaximumSize() {
		if (super.isMaximumSizeSet()) {
			return super.getMaximumSize();
		}
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	private int orientation = Adjustable.HORIZONTAL;

	/**
	 * Gets the orientation of the lines, either horizontal or vertical.
	 * 
	 * @return
	 */
	public int getOrientation() {
		return orientation;
	}

	/**
	 * Sets the orientation of the widget.
	 * 
	 * @param newOrientation
	 *            {@link Adjustable#HORIZONTAL}or {@link Adjustable#VERTICAL}
	 */
	public void setOrientation(int newOrientation) {
		if (newOrientation != this.orientation) {
			switch (newOrientation) {
			case Adjustable.HORIZONTAL:
			case Adjustable.VERTICAL:
				invalidate();
				break;
			default:
				String err = "Not a valid orientation";
				throw new IllegalArgumentException(err);
			}
			int oldOrientation = this.orientation;
			this.orientation = newOrientation;
			firePropertyChange("orientation", oldOrientation, newOrientation);
		}
	}

	/**
	 * Gets the y-direction zoom ratio.
	 * 
	 * @return the y zoom
	 */
	public double getYZoom() {
		return lineZoom;
	}

	/**
	 * Sets the y-direction zoom ratio.
	 * 
	 * @param yZoom
	 *            the y zoom
	 */
	public void setYZoom(double yZoom) {
		if (this.lineZoom != yZoom) {
			double oldZoom = this.lineZoom;
			this.lineZoom = yZoom;
			resetLineLayer();
			getOverlay().redoHeight();
			firePropertyChange("yZoom", oldZoom, yZoom);
		}
	}

	/**
	 * Gets where the timelines actually start in the view.
	 * 
	 * @return the label length
	 */
	public double getViewOffsetX() {
		return labelLength;
	}

	/**
	 * Gets where the timelines actually start in the view.
	 * 
	 * @return the ruler height
	 */
	public double getViewOffsetY() {
		return getRulerHeight();
	}

	/**
	 * Gets the actual length of the view, in pixels.
	 * 
	 * @return the length of the view
	 */
	public double getViewLength() {
		return viewLength;
	}

	/**
	 * Gets the preferred height of the canvas - this is the sum of the timeline
	 * heights.
	 * 
	 * @return
	 */
	public double getPreferredViewHeight() {
		return linesHeight;
	}

	/**
	 * Gets the height of the scroll window aperture.
	 * 
	 * @return the height of the scroll window
	 */
	public double getVisibleViewHeight() {
		return getBounds().getHeight() - getViewOffsetY();
	}

	/**
	 * Gets the overall view height. This is necessary, as the actual height can
	 * be greater than the preferred height, and some things need to be
	 * bottom-aligned.
	 * 
	 * @return the maxium of the preferred and visible heights.
	 */
	public double getViewHeight() {
		return Math.max(getPreferredViewHeight(), getVisibleViewHeight());
	}

	/**
	 * Sets the actual view length. I've been unable to determine this
	 * programatically, at least reliably, so it is best to just keep this up to
	 * date. You shouldn't have to worry about it, unless you put the canvas in
	 * some strange, non-swing holder.
	 * 
	 * @param viewWidth
	 *            the new length
	 */
	public void setViewLength(double viewWidth) {
		if (this.viewLength != viewWidth) {
			double oldViewWidth = this.viewLength;
			this.viewLength = viewWidth;
			resetView();
			getOverlay().redoLength();
			firePropertyChange("viewLength", oldViewWidth, viewWidth);
		}
	}

	/**
	 * Small enough to fit all the lines into the current view, but still take
	 * up available space.
	 * 
	 * @return
	 */
	private double getMinimumViewLength() {
		if (scrollViews == null) {
			return Math.max(0, super.getBounds().getWidth() - labelLength);
		} else {
			return Math.max(0, scrollViews.content.getWidth());
		}
	}

	/**
	 * Gets a view length that fits one instant in one width of the view.
	 * 
	 * @return
	 */
	private double getMaximumViewLength() {
		if (this.model == null || this.model.getFocus() == null) {
			return getMinimumViewLength();
		}
		double minLengh = getMinimumViewLength();
		long focalWidth = model.getFocus().width();
		double maxLength = focalWidth * labelLength;
		assert focalWidth >= 0;
		maxLength += labelLength;
		return Math.max(minLengh, maxLength);
	}

	/**
	 * Gets the data model behind this chronicle view.
	 * 
	 * @return ChronicleModel the supporting model
	 */
	public ChronicleViewModel getModel() {
		return model;
	}

	/**
	 * Sets the model to display.
	 * 
	 * @param cm
	 *            the new model
	 */
	public void setModel(ChronicleViewModel cm) {
		if (this.model != cm) {
			ChronicleViewModel oldModel = this.model;
			if (this.model != null) {
				this.model.removeChronicleViewListener(cl);
			}
			this.model = cm;
			if (cm != null) {
				this.model.addChronicleViewListener(cl);
			}
			resetView();
			getOverlay().redoLength();
			getOverlay().redoHeight();
			firePropertyChange("model", oldModel, cm);
		}
	}

	/**
	 * Sets the factory used to generate timeline factories.
	 * 
	 * @param metafact
	 *            the factory object that will generate templates for each
	 *            timeline
	 */
	public void setRendererCatalogue(RendererCatalogue cat) {
		if (this.factory != cat) {
			RendererCatalogue old = this.factory;
			this.factory = cat;
			firePropertyChange("rendererCatalogue", old, cat);
		}
	}

	/**
	 * Gets the timeline view metafactory.
	 * 
	 * @return the factory object that is used to generate templates for each
	 *         timeline
	 */
	public RendererCatalogue getRendererCatalogue() {
		return this.factory;
	}

	class PopupActionListener extends MouseAdapter implements Serializable {
		private transient PCanvas canvas;

		PopupActionListener() {
			canvas = getProxy();
		}

		PopupActionListener(PCanvas c) {
			this.canvas = c;
		}

		/**
		 * Shows popup if a popup exists for the location and the mouse event
		 * indicates a popup request. {@inheritDoc}
		 */
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		/**
		 * Shows popup if a popup exists for the location and the mouse event
		 * indicates a popup request. {@inheritDoc}
		 */
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				PCamera pc = canvas.getCamera();
				PPickPath ppp = pc.pick(e.getX(), e.getY(), 4);
				PNode bottom = ppp.getPickedNode();
				while (bottom != pc && bottom != null) {
					if (bottom instanceof ChronicleMarkerNode) {
						getMarkerPop().setCanvas(canvas);
						ChronicleMarkerNode cmn = (ChronicleMarkerNode) bottom;
						getMarkerPop().show(e.getX(), e.getY(),
								e.getComponent(), cmn);
						return;
					}
					bottom = bottom.getParent();
				}
			}
		}
	}

	/**
	 * Modified version of Piccolo's PPanEventHandler to support middle-mouse
	 * button panning on the chronicle.
	 */
	public class ChroniclePanEventHandler extends PDragSequenceEventHandler {

		private boolean autopan;

		private double minAutopanSpeed = 5;

		private double maxAutopanSpeed = 15;

		/**
		 * Creates a new pan handler for the outer class.
		 */
		public ChroniclePanEventHandler() {
			super();
			PInputEventFilter filter = new PInputEventFilter(
					InputEvent.BUTTON2_MASK, InputEvent.ALT_DOWN_MASK
							| InputEvent.CTRL_DOWN_MASK);
			setEventFilter(filter);
			setAutopan(true);
		}

		protected void drag(PInputEvent e) {
			super.drag(e);
			pan(e);
		}

		private double squelchPanSpeed(double speed) {
			if (Math.abs(speed) < minAutopanSpeed) {
				speed = 0;
			} else if (speed < -minAutopanSpeed) {
				speed += minAutopanSpeed;
				speed = Math.max(speed, -maxAutopanSpeed);
			} else if (speed > minAutopanSpeed) {
				speed -= minAutopanSpeed;
				speed = Math.min(speed, maxAutopanSpeed);
			}
			return speed;
		}

		private void autoPan(PInputEvent e) {
			// Calculate amount to pan
			Point2D last = super.getMousePressedCanvasPoint();
			Point2D current = e.getCanvasPosition();
			PDimension r = new PDimension(current.getX() - last.getX(), current
					.getY()
					- last.getY());
			e.getPath().canvasToLocal(r, e.getCamera());
			PDimension d = (PDimension) e.getCamera().localToView(r);
			d.height = -squelchPanSpeed(d.height);
			d.width = -squelchPanSpeed(d.width);

			// Make sure panning doesn't go out of range
			PCamera c;
			if (scrollViews == null) {
				c = getProxy().getCamera();
			} else {
				c = scrollViews.content.getCamera();
			}
			PAffineTransform T = c.getViewTransform();
			double ty = T.getTranslateY() + d.height;
			double viewHeight = getViewHeight() + getFooterHeight();
			double cameraHeight = c.getHeight();
			ty = Math.max(ty, cameraHeight - viewHeight); // make sure it
			// doesn't go past the
			// bottom
			ty = Math.min(0, ty); // make sure it doesn't pan up

			double tx = T.getTranslateX() + d.width;
			tx = Math.min(0, tx); // make sure it doesn't pan to the left of
			// the
			// start
			tx = Math.max(tx, c.getWidth() - getViewLength()); // or right of
			// end

			T.setOffset(tx, ty);
			c.setViewTransform(T);
		}

		protected void pan(PInputEvent e) {
			PCamera c = e.getCamera();
			Point2D l = e.getPosition();

			if (c.getViewBounds().contains(l)) {
				if (autopan) {
					autoPan(e);
				} else {
					PDimension d = e.getDelta();
					c.translateView(d.getWidth(), d.getHeight());
				}
			}
		}

		// ****************************************************************
		// Auto Pan
		// ****************************************************************

		/**
		 * Turns on or off autopan.
		 * 
		 * @param autopan
		 *            whether to use panning like in internet explorer, where
		 *            dragging the mouse is like a joystick
		 */
		public void setAutopan(boolean autopan) {
			this.autopan = autopan;
		}

		/**
		 * Gets whether autopan is enabled.
		 * 
		 * @return if panning is like in internet explorer, where dragging the
		 *         mouse is like a joystick
		 */
		public boolean getAutopan() {
			return autopan;
		}

		/**
		 * Set the minimum speed for autopan (outside of the dead region near
		 * where the mouse was clicked)
		 * 
		 * @param minAutopanSpeed
		 *            the minimum speed
		 */
		public void setMinAutopanSpeed(double minAutopanSpeed) {
			this.minAutopanSpeed = minAutopanSpeed;
		}

		/**
		 * This is the maximum autopan speed, which happens when the mouse is
		 * dragged outside the region of accelleration.
		 * 
		 * @param maxAutopanSpeed
		 *            the maximum speed
		 */
		public void setMaxAutopanSpeed(double maxAutopanSpeed) {
			this.maxAutopanSpeed = maxAutopanSpeed;
		}

		/**
		 * Do auto panning even when the mouse is not moving. {@inheritDoc}
		 */
		protected void dragActivityStep(PInputEvent aEvent) {
			if (!autopan)
				return;
			autoPan(aEvent);
		}
	}

	/**
	 * The right-click zoom handler for the chronicle has been deprecated in
	 * favor of the scroll wheel zoom handler. I'm leaving it in the code in
	 * case someone insists on using it.
	 */
	public class ChronicleRightClickZoomHandler extends PZoomEventHandler {
		// Zoom in, while keeping 'zoomIntoInstant' at point 'zoomPoint'
		// the instant i is found at point zoomPoint in global coords
		private double zoomPoint;

		private Instant zoomIntoInstant;

		private ChronicleViewer canvas;

		/**
		 * Creates a new zoom handler.
		 * 
		 * @param canvas
		 *            the canvas to zoom into when drag events occur
		 */
		public ChronicleRightClickZoomHandler(ChronicleViewer canvas) {
			super();
			this.canvas = canvas;
			setEventFilter(new PInputEventFilter(InputEvent.BUTTON3_MASK));
		}

		protected void dragActivityFirstStep(PInputEvent aEvent) {
			Point2D p = aEvent.getPosition();
			zoomPoint = p.getX()
					+ canvas.getProxy().getCamera().getViewTransform()
							.getTranslateX();
			if (zoomPoint > getViewOffsetX() && model != null
					&& model.getFocus() != null) {
				// If on the chronicle and not on the row header area
				double localX = getUnderlay().globalToLocal(p).getX();
				zoomIntoInstant = getLineLayer().local2instant(localX);
				super.dragActivityFirstStep(aEvent);
			} // else on the row header. don't do zooming there (yet?)
		}

		protected void dragActivityStep(PInputEvent aEvent) {
			if (zoomIntoInstant == null) {
				return;
			}
			double dx = aEvent.getCanvasPosition().getX()
					- getMousePressedCanvasPoint().getX();
			double scaleDelta = (1.0 + (0.001 * dx));
			setZoom(getZoomAmount() * scaleDelta, zoomIntoInstant, zoomPoint);
		}
	}

	/**
	 * The wheel zoom handler zooms in when the user scrolls up, and out when
	 * the user scrolls down.
	 */
	public class ChronicleWheelZoomHandler extends PBasicInputEventHandler {
		private ChronicleViewer canvas;

		/**
		 * Creates a new zoom handler.
		 * 
		 * @param canvas
		 *            the canvas to zoom in when scroll wheel events occur
		 */
		public ChronicleWheelZoomHandler(ChronicleViewer canvas) {
			super();
			this.canvas = canvas;
			PInputEventFilter scrollFilter = new PInputEventFilter();
			scrollFilter.rejectAllEventTypes();
			scrollFilter.setAcceptsMouseWheelRotated(true);
			setEventFilter(scrollFilter);
		}

		/**
		 * {@inheritDoc}
		 */
		public void mouseWheelRotated(PInputEvent event) {
			if (model == null || model.getFocus() == null) {
				resetView();
			} else {
				Point2D p = event.getPosition();
				double clickPoint = p.getX();
				double offset;
				if (scrollViews == null) {
					offset = canvas.getProxy().getCamera().getViewTransform()
							.getTranslateX();
				} else {
					offset = -labelLength;
				}
				double zoomPoint = clickPoint + offset;

				// If on the chronicle and not on the row header area
				double localX = getUnderlay().globalToLocal(p).getX();
				Instant zoomIntoInstant = getLineLayer().local2instant(localX);

				// Zoom
				int r = event.getWheelRotation();
				double scaleDelta = (1.0 - (0.125 * r));

				setZoom(getZoomAmount() * scaleDelta, zoomIntoInstant,
						zoomPoint);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void mouseWheelRotatedByBlock(PInputEvent event) {
			super.mouseWheelRotatedByBlock(event);
		}
	}

	/**
	 * Resets the zoom of the chronicle, keeping the given instant at the same
	 * position (if possible).
	 * 
	 * @param zoomAmount
	 * @param where
	 */
	public void setZoom(double zoomAmount, Instant where) {
		setZoom(zoomAmount, where, instant2pixelDistance(where));
	}

	/**
	 * Zooms in on the specified moment in time. Keeps the instant
	 * <code>where</code> located at the point <code>zoomPoint</code>.
	 * 
	 * @param zoomAmount
	 * @param where
	 *            the instant the user zooms in toward
	 * @param zoomPoint
	 *            the place the user put her mouse
	 */
	private void setZoom(double zoomAmount, Instant where, double zoomPoint) {
		// Zoom
		if (zoomAmount < 1) {
			zoomAmount = 1;
		}
		double newViewLength = getMinimumViewLength() * zoomAmount;
		newViewLength = Math.min(getMaximumViewLength(), newViewLength);
		newViewLength = Math.max(getMinimumViewLength(), newViewLength);
		if (getViewLength() == newViewLength) {
			return;
		}
		zoomAmount = newViewLength / getViewLength();

		// Recenter
		PCamera c;
		if (scrollViews == null) {
			c = getProxy().getCamera();
		} else {
			c = scrollViews.content.getCamera();
		}
		PAffineTransform T = c.getViewTransform();
		double newZoomPoint = zoomPoint * zoomAmount;
		double tx = zoomPoint - newZoomPoint + T.getTranslateX();
		double ty = T.getTranslateY();
		tx = Math.min(0, tx); // make sure it doesn't zoom to the left of the
		// start
		tx = Math.max(tx, c.getWidth() - newViewLength); // or right of end

		T.setOffset(tx, ty);
		c.setViewTransform(T);
		setViewLength(newViewLength);
	}

	/**
	 * Gets the current zoom ratio.
	 * 
	 * @return the current zoom ratio
	 */
	public double getZoomAmount() {
		if (getViewLength() == 0 || getMinimumViewLength() == 0) {
			return 0;
		}
		return getViewLength() / getMinimumViewLength();
	}

	/**
	 * Gets the hight of the ruler, in pixels.
	 * 
	 * @return the ruler height
	 */
	public int getRulerHeight() {
		return getRuler().getRulerHeight();
	}

	public RulerChronicleBackground getRuler() {
		return (RulerChronicleBackground) getUnderlay();
	}

	public void setRuler(RulerChronicleBackground ruler) {
		setUnderlay(ruler);
	}

	/**
	 * Gets the height of the footer line.
	 * 
	 * @return the height of the footer
	 */
	public double getFooterHeight() {
		return footerHeight;
	}

	/**
	 * Gets the color to be used in the header.
	 * 
	 * @return the head color
	 */
	public Paint getHeadColor() {
		return headColor;
	}

	/**
	 * Gets the length of the timeline labels.
	 * 
	 * @return the number of pixels to reserve for the timeline names and
	 *         emblems
	 */
	public double getLabelLength() {
		return labelLength;
	}

	/**
	 * Sets the height to reserve for the footer.
	 * 
	 * @param newFooterHeight
	 *            the height for the footer row
	 */
	public void setFooterHeight(double newFooterHeight) {
		if (footerHeight != newFooterHeight) {
			double oldFooterHeight = footerHeight;
			footerHeight = newFooterHeight;
			firePropertyChange("footerHeight", oldFooterHeight, newFooterHeight);
			invalidate();
		}
	}

	/**
	 * Sets the color for the header.
	 * 
	 * @param paint
	 *            the head color
	 */
	public void setHeadColor(Paint paint) {
		if (headColor != paint) {
			Paint oldPaint = headColor;
			headColor = paint;
			invalidate();
			firePropertyChange("headColor", oldPaint, paint);
		}
	}

	/**
	 * Sets the length to reserve for line labels.
	 * 
	 * @param d
	 *            the size of the space to use for line names and emblems
	 */
	public void setLabelLength(double d) {
		if (labelLength != d) {
			double oldLabelLength = this.labelLength;
			this.labelLength = d;
			invalidate();
			firePropertyChange("labelLength", oldLabelLength, d);
		}
	}

	/**
	 * Sets the height to allocate for the header, including the ruler and
	 * marker labels.
	 * 
	 * @param d
	 *            the ruler height
	 */
	public void setRulerHeight(int d) {
		if (this.getRulerHeight() != d) {
			int oldRulerHeight = getRulerHeight();
			getRuler().setRulerHeight(d);
			invalidate();
			firePropertyChange("rulerHeight", oldRulerHeight, d);
		}
	}

	/**
	 * Gets the instant beneath the point.
	 * 
	 * @param point2D
	 *            the point
	 * @return the instant beneath the point
	 */
	public Instant getInstantFor(Point2D localPoint) {
		return getLineLayer().local2instant(
				getOrientation() == SwingConstants.HORIZONTAL ? localPoint
						.getX() : localPoint.getY());
	}

	/**
	 * Gets the instant beneath the event's point.
	 * 
	 * @param aEvent
	 *            the event
	 * @return the instant beneath the event
	 */
	public Instant getInstantFor(PInputEvent aEvent) {
		return getInstantFor(getLocalPosition(aEvent));
	}

	/**
	 * Gets the timeline beneath the point.
	 * 
	 * @param point2D
	 *            the point, relative to the top of the timeline
	 * @return the timeline beneath the point
	 */
	public TimeLine getTimeLineFor(Point2D global) {
		double offset = getViewOffsetY();
		if (getOrientation() == SwingConstants.HORIZONTAL) {
			offset += global.getY();
		} else {
			offset += global.getX();
		}
		return getLineLayer().local2timeline(offset);
	}

	/**
	 * Gets the line beneath the event's point.
	 * 
	 * @param aEvent
	 *            the event
	 * @return the line beneath the event
	 */
	public TimeLine getTimeLineFor(PInputEvent aEvent) {
		Point2D localPosition = getLocalPosition(aEvent);
		return getTimeLineFor(localPosition);
	}

	/**
	 * @param aEvent
	 * @return
	 */
	public Point2D getLocalPosition(PInputEvent aEvent) {
		// first check the event's
		PPickPath path = aEvent.getPath();
		PCamera top = path.getTopCamera();
		PStack pathStack = path.getNodeStackReference();
		if (scrollViews != null && pathStack.size() > 2
				&& (pathStack.get(2) instanceof PCamera)) {
			top = (PCamera) pathStack.get(2);
		}
		Point2D canvasPosition = aEvent.getCanvasPosition();
		Point2D localPosition = path.canvasToLocal(canvasPosition, top);
		if (scrollViews != null) {
			if (pathStack.contains(scrollViews.contentCam)) {
			} else if (pathStack.contains(scrollViews.cornerCam)) {
				localPosition.setLocation(
						localPosition.getX() - top.getWidth(), localPosition
								.getY()
								- top.getHeight());
			} else if (pathStack.contains(scrollViews.rulerCam)) {
				localPosition.setLocation(localPosition.getX(), localPosition
						.getY()
						- top.getHeight());
			} else if (pathStack.contains(scrollViews.labelCam)) {
				localPosition.setLocation(
						localPosition.getX() - top.getWidth(), localPosition
								.getY());
			}
		}
		return localPosition;
	}

	/**
	 * Gets the number of instants that the given number of pixels is equivalent
	 * to.
	 * 
	 * @param pixels
	 *            the number of pixels
	 * @return the number of instants the number of pixels represents, rounded
	 *         down
	 */
	public long pixel2instantDistance(double pixels) {
		return getLineLayer().local2instant(pixels).longValue();
	}

	/**
	 * Gets the number of instants that the given number of pixels is equivalent
	 * to.
	 * 
	 * @param i
	 *            the count of instants
	 * @return the number of pixels the number of instants represents
	 */
	public double instant2pixelDistance(Instant i) {
		return getLineLayer().instant2local(i);
	}

	/**
	 * Gets the number of instants that the given number of pixels is equivalent
	 * to.
	 * 
	 * @param i
	 *            the count of instants
	 * @return the number of pixels the number of instants represents
	 */
	public double instant2pixelLocation(Instant i) {
		return getLineLayer().instant2local(
				i.go(-model.getFocus().getStart().longValue()));
	}

	/**
	 * Gets the number of instants that the given number of pixels is equivalent
	 * to.
	 * 
	 * @param pixels
	 *            the number of pixels
	 * @return the number of instants the number of pixels represents, rounded
	 *         down
	 */
	public TimeLine pixel2timeLine(double pixel) {
		return getLineLayer().local2timeline(pixel);
	}

	/**
	 * Gets the number of instants that the given number of pixels is equivalent
	 * to.
	 * 
	 * @param i
	 *            the count of instants
	 * @return the number of pixels the number of instants represents
	 */
	public double[] timeLine2pixels(TimeLine tqe) {
		return getLineLayer().timeline2local(tqe);
	}

	private class SimpleCanvas extends PCanvas {
		SimpleCanvas(PCamera cam) {
			super();
			super.setPanEventHandler(null);
			super.setZoomEventHandler(null);
			super.getLayer().addChild(cam);
			super.setToolTipText("text");
			this.addMouseListener(new PopupActionListener(this));
		}

		/**
		 * {@inheritDoc}
		 */
		public Point getToolTipLocation(MouseEvent event) {
			// getToolTipText(event);
			return super.getToolTipLocation(event);
		}

		/**
		 * {@inheritDoc}
		 */
		public String getToolTipText(MouseEvent event) {
			PCamera cam = this.getCamera();
			Point2D point = cam.globalToLocal(event.getPoint());
			PPickPath path = cam.pick(point.getX(), point.getY(), 3);
			PNode n = path.getPickedNode();
			return (String) n.getAttribute(PToolTip.TOOLTIP_PROPERTY);
		}

		@Override
		public void paintComponent(Graphics g) {
			finishReset();
			super.paintComponent(g);
		}
		
		
	}

	/**
	 * Gets the canvases associated with this canvas.
	 * 
	 * @return a set of four views for use in a scroll pane
	 */
	public ScrollViews getScrollViews() {
		if (scrollViews == null) {
			scrollViews = new ScrollViews();
			resetScrollViewsTransferHandler();
		}
		return scrollViews;
	}

	private PPanEventHandler panHandler = null;

	/**
	 * {@inheritDoc}
	 */
	public void setPanEventHandler(PPanEventHandler handler) {
		if (getProxy().getPanEventHandler() != handler) {
			assert getProxy().getPanEventHandler() == this.panHandler;
			PPanEventHandler oldHandler = getProxy().getPanEventHandler();
			panHandler = handler;
			getProxy().setPanEventHandler(handler);
			if (scrollViews != null) {
				scrollViews.setPanEventHandler(handler);
			}
			firePropertyChange("panEventHandler", oldHandler, handler);
		}
	}

	private PZoomEventHandler zoomHandler;

	/**
	 * {@inheritDoc}
	 */
	public void setZoomEventHandler(PZoomEventHandler handler) {
		if (handler != getProxy().getZoomEventHandler()) {
			assert getProxy().getZoomEventHandler() == zoomHandler;
			PZoomEventHandler oldHandler = getProxy().getZoomEventHandler();
			zoomHandler = handler;
			getProxy().setZoomEventHandler(handler);
			if (scrollViews != null) {
				scrollViews.setZoomEventHandler(handler);
			}
			firePropertyChange("zoomEventHandler", oldHandler, handler);
		}
	}

	/**
	 * Gets the popup menu for the markers. This is necessary if you, instead of
	 * using the canvas, insert the view into your own scroll pane, or similar
	 * container widget. In this case, make sure to set the pop-up's viewer
	 * appropriately.
	 * 
	 * @return Returns the markerPop.
	 */
	public ChronicleMarkerPopUp getMarkerPop() {
		if (markerPop == null) {
			this.markerPop = new ChronicleMarkerPopUp();
			this.markerPop.setViewer(this);
			this.markerPop.setCanvas(getProxy());
		}
		return markerPop;
	}

	/**
	 * Gets the model to be used for selection and viewing.
	 * 
	 * @return Returns the selectionModel.
	 */
	public ChronicleSelectionModel getSelectionModel() {
		return selectionModel;
	}

	/**
	 * Changes the chronicle selection model
	 * 
	 * @param selectionModel
	 *            The selectionModel to set.
	 */
	public void setSelectionModel(ChronicleSelectionModel selectionModel) {
		if (this.selectionModel != selectionModel) {
			if (this.selectionModel != null) {
				this.selectionModel
						.removeChangeListener(timeSelectionChangeListener);
			}
			ChronicleSelectionModel oldSelectionModel = this.selectionModel;
			this.selectionModel = selectionModel;
			if (this.selectionModel != null) {
				this.selectionModel
						.addChangeListener(timeSelectionChangeListener);
			}
			firePropertyChange("selectionModel", oldSelectionModel,
					selectionModel);
		}
	}

	/**
	 * Creates a new ChronicleViewer, using the mediator as the data model.
	 */
	public ChronicleViewer() {
		super();
		this.setSelectionModel(new DefaultChronicleSelectionModel());
		this.factory = new DefaultRendererCatalogue();

		this.zoomHandler = new ChronicleRightClickZoomHandler(this);
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.add(getProxy());
	}

	public PCanvas getProxy() {
		if (proxy == null) {
			proxy = new PCanvas() {@Override
			public void paintComponent(Graphics g) {
				finishReset();
				super.paintComponent(g);
			}} ;
			proxy.getLayer().addChild(new FrameRulerChronicleBackground(this));
			proxy.getLayer().addChild(new LineLayoutManager());
			proxy.getLayer().addChild(new LineLayoutManager());
			proxy.getLayer().addChild(new TimeSelectionOverlay());
			proxy.getLayer().addChild(new OverlayLayoutManager());
			proxy.getLayer().addChild(new AllChronicleMarkersNode());

			getMarkersNode().setViewer(this);
			getLineLayer().setOffset(getViewOffsetX(), 0);

			// proxy.setDefaultRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);

			proxy.setZoomEventHandler(zoomHandler);
			proxy.setPanEventHandler(panHandler);

			proxy.addInputEventListener(new TimeLineInputProxy());
			proxy.addMouseListener(new PopupActionListener(proxy));
			this.setViewLength(getMinimumViewLength());

			ChronicleMarkerModel markerModel = new DefaultChronicleMarkerModel();
			ChronicleMarker majorMomentMarker = markerModel.createMarker();
			markerModel.addChronicleMarkerListener(cml);
			majorMomentMarker.setLabel(CURR_FRAME_LABEL);
			getMarkersNode().setModel(markerModel);

			resetView();
		}
		return proxy;
	}

	private static final int UNDERLAY_OFFSET = 0;

	private static final int LINE_LAYER_OFFSET = 1;

	private static final int LABEL_LAYER_OFFSET = 2;

	private static final int TIME_SELECTION_OVERLAY_OFFSET = 3;

	private static final int OVERLAY_OFFSET = 4;

	private static final int MARKERS_NODE_OFFSET = 5;

	private PNode getUnderlay() {
		return getProxy().getLayer().getChild(
				UNDERLAY_OFFSET);
	}
	private void setUnderlay(PNode underlay) {
		getProxy().getLayer().removeChild(
				UNDERLAY_OFFSET);
		getProxy().getLayer().addChild(
				UNDERLAY_OFFSET, underlay);
	}

	private LineLayoutManager getLineLayer() {
		return (LineLayoutManager) getProxy().getLayer().getChild(
				LINE_LAYER_OFFSET);
	}

	private LineLayoutManager getLabelLayer() {
		return (LineLayoutManager) getProxy().getLayer().getChild(
				LABEL_LAYER_OFFSET);
	}

	private TimeSelectionOverlay getTimeSelectionOverlay() {
		return (TimeSelectionOverlay) getProxy().getLayer().getChild(
				TIME_SELECTION_OVERLAY_OFFSET);
	}

	private OverlayLayoutManager getOverlay() {
		return (OverlayLayoutManager) getProxy().getLayer().getChild(
				OVERLAY_OFFSET);
	}

	private ChangeListener timeSelectionChangeListener = new TimeSelectionEventListener();

	private class TimeSelectionOverlay extends PNode {
		double[] ranges;

		/**
		 * {@inheritDoc}
		 */
		public boolean intersects(Rectangle2D localBounds) {
			double x1 = localBounds.getMinX();
			double x2 = localBounds.getMaxX();
			int start = Arrays.binarySearch(ranges, x1);
			if (start < 0) {
				start = -(start + 1);
				if ((start & 1) == 1) {
					// inserts within a selected span
					return true;
				}
			} else {
				return true;
			}
			int end = Arrays.binarySearch(ranges, x2);
			if (end < 0) {
				end = -(end + 1);
				if ((end & 1) == 1) {
					// inserts within a selected span
					return true;
				}
			}
			return end != start + 1;
		}

		/**
		 * {@inheritDoc}
		 */
		protected void paint(PPaintContext paintContext) {
			Graphics2D g2 = paintContext.getGraphics();
			g2.setPaint(getPaint());
			Rectangle rect = g2.getClipBounds();
			double x1 = 0;
			double x2 = Double.MAX_VALUE;
			double y1 = 0;
			double y2 = Double.MAX_VALUE;
			int startIndex = 0;
			int afterLastIndex = ranges.length;
			if (rect != null) {
				x1 = rect.getMinX();
				x2 = rect.getMaxX();
				y1 = rect.getMinY();
				y2 = rect.getMaxY();
				startIndex = Arrays.binarySearch(ranges, x1);
				if (startIndex < 0) {
					startIndex = -(startIndex + 1);
				}
				startIndex = startIndex & 0xFFFFFFFE;
				afterLastIndex = Arrays.binarySearch(ranges, x2);
				if (afterLastIndex < 0) {
					afterLastIndex = -afterLastIndex;
				}
				afterLastIndex = afterLastIndex | 1;
			}
			Rectangle2D.Double r = new Rectangle2D.Double();
			r.y = y1;
			r.height = y2 - y1;
			for (int i = startIndex; i < afterLastIndex; i += 2) {
				r.x = ranges[i];
				r.width = ranges[i + 1] - r.x;
				g2.fill(r);
			}
		}

		private void resetRanges() {
			PBounds b = super.getBoundsReference();
			TemporalRange time = selectionModel.getSelectedTime();
			if (time == null || time.isEmpty()) {
				ranges = new double[0];
				return;
			}
			if (ranges.length != time.getContiguousIntervalCount() * 2) {
				ranges = new double[time.getContiguousIntervalCount() * 2];
			}
			Iterator iter = time.iterator();
			InstantInterval i = model.getFocus();
			long s = i.getStart().longValue();
			long e = i.getEnd().longValue();
			long width = e - s;
			double pixelsPerInstant = b.getWidth() / width;
			int counter = 0;
			while (iter.hasNext()) {
				InstantInterval ii = (InstantInterval) iter.next();
				long i1 = ii.getStart().longValue();
				long i2 = ii.getEnd().longValue();
				ranges[counter++] = (i1 - s) * pixelsPerInstant;
				ranges[counter++] = (i2 - s) * pixelsPerInstant;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean setBounds(double x, double y, double width, double height) {
			if (super.setBounds(x, y, width, height)) {
				resetRanges();
				return true;
			}
			return false;
		}
	}

	public void addTimeLineInputEventListener(TimeLineInputEventListener l) {
		listenerList.add(TimeLineInputEventListener.class, l);
	}

	public void removeTimeLineInputEventListener(TimeLineInputEventListener l) {
		listenerList.remove(TimeLineInputEventListener.class, l);
	}

	public TimeLineInputEventListener[] getTimeLineInputEventListeners() {
		return (TimeLineInputEventListener[]) listenerList
				.getListeners(TimeLineInputEventListener.class);
	}

	protected void fireTimeLineInputEvent(PInputEvent pe, int ptype) {
		Object[] listeners = listenerList.getListenerList();
		TimeLineInputEvent e = null;
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TimeLineInputEventListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new TimeLineInputEvent(this, pe);
				((TimeLineInputEventListener) listeners[i + 1]).processEvent(e,
						ptype);
			}
		}
	}

	public void setSize(Dimension arg0) {
		super.setSize(arg0);
		setViewLength(Math.max(getViewLength(), getMinimumViewLength()));
	}

	public void setSize(int arg0, int arg1) {
		super.setSize(arg0, arg1);
		setViewLength(Math.max(getViewLength(), getMinimumViewLength()));
	}

	/**
	 * @param event
	 * @param i
	 * @param toSnapTo
	 * @return
	 */
	public Instant snapInstantToNearest(double wellDistance, Point2D position,
			Instant i, Collection toSnapTo) {
		double d = Double.POSITIVE_INFINITY;
		double clickPoint = position.getX() - getLabelLength();
		Instant toSnap = null;
		for (Iterator iter = toSnapTo.iterator(); iter.hasNext();) {
			Instant snapInstant = (Instant) iter.next();
			double snapPoint = instant2pixelLocation(snapInstant);
			double pix = Math.abs(snapPoint - clickPoint);
			if (pix < d) {
				d = pix;
				toSnap = snapInstant;
			}
		}
		if (d < wellDistance) {
			i = toSnap;
		}
		return i;
	}

	/**
	 * @param i
	 * @param toSnapTo
	 */
	public static void addNearbySnapPoints(Instant i, Collection toSnapTo,
			TemporalRange refRange) {
		boolean inside = refRange.contains(i);
		Comparable c = refRange.firstBefore(i);
		if (c != null) {
			if (inside) {
				toSnapTo.add(c);
			}
			toSnapTo.add(((Incrementable) refRange.endOf(c)).previous());
		}
		c = refRange.firstAfterOrAt(i);
		if (c != null) {
			toSnapTo.add(c);
			if (inside) {
				toSnapTo.add(((Incrementable) refRange.endOf(c)).previous());
			}
		}
	}

	public PLayer getOverlayLayer() {
		return getOverlay();
	}

	private void writeObject(java.io.ObjectOutputStream stream)
			throws IOException {
		super.remove(getProxy());
		stream.defaultWriteObject();
		super.add(getProxy());
	}

}