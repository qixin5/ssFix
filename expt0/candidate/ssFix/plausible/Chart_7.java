package com.adum.go;
/*  -*- Mode: java; indent-tabs-mode: nil; c-basic-indent: 2; c-basic-offset: 2; java-indent-level: 2 -*-
 
 problematic's goban applet
 Copyright 1999,2000 Adam Miller (adum@adum.com)
 this software licensed under the GPL. a copy of this license should be included with the distribution you received. otherwise, get a copy from www.gnu.org or a favorite Free Software Foundation vendor near you.
 
 requires java 1.1 or greater
 
 some functionality is particular to a web environment with a database backend and dynamic page creation. see example at http://www.goproblems.com/
 yes, this code is a total mess
 */

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Label;
import java.awt.MediaTracker;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.awt.image.ReplicateScaleFilter;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.Vector;

import javax.swing.ImageIcon;

import com.adum.go.net.NetUtil;
import com.adum.go.parse.LongCommentRecurser;
import com.adum.go.parse.Parser;
import com.adum.go.parse.PathRecurser;
import com.adum.go.widget.ClockControl;
import com.adum.go.widget.CommentLabel;
import com.adum.go.widget.MoveLabel;
import com.adum.go.widget.PictureButton;
import com.adum.go.widget.PictureCheckbox;
import com.adum.go.widget.Smile;
import com.adum.go.widget.StatusLabel;

public class GoApplet extends java.applet.Applet implements Runnable, ItemListener, ActionListener, KeyListener, MouseListener  {
    static final int VERSION = 8;
    
    public static final int RIGHT = 1;
    public static final int WRONG = 2;
    
    public static final int EMPTY = 0;
    public static final int BLACK = 1;
    public static final int WHITE = 2;
    public static final int MURKY = 3;
    
    public static final int MODEMOVE = 0;
    public static final int MODENAVIGATE = 1;
    public static final int MODEANIMATE = 2;
    
    /** the board */
    public Goban gb;
    
    private int result = 0, realresult = 0;
    private boolean sentHard = false;
    
    static final String ADD_PROBLEM_PAGE = "/add1.php3";
    
    boolean doResponse = true; // respond to the user's moves if there's a path continuation
    public boolean trialMode = false; // are they in time trial mode?
    public boolean alreadyDied = false; // they have failed once -- we count this against them
    public boolean searchMode = false; // trying to draw a search pattern
    public boolean editMode = false; // editing the tree
    boolean allowForbiddenNode = false; // can we move to a node that has the NOTTHIS param set?
    int trialid = 0; // for time trials
    boolean expandedMode = false; // in editing, show whole screen or just part?
    
    Thread runner = null;
    MediaTracker tracker; // how we keep track of which images we have received
    String sgf;
    URL docBase;
    boolean doneInit = false;
    int width = 200;
    int height = 200;
    int mode = MODEMOVE;
    int playerStone = BLACK;
    
    // the images
    Image imgBoard, imgWhite, imgBlack;
    Image srcBoard;
    
    Locale curLocale;
    public ResourceBundle resBundle;
    
    public Image srcWhite;
    
    public Image srcBlack;
    Image sBoard, sWhite, sBlack;
    Image srcghostWhite, srcghostBlack;
    Image ghostWhite, ghostBlack;
    Image sghostWhite, sghostBlack;
    public Image smileyImg;
    // edit button images
    Image ebiCut, ebiSetup, ebiBlack, ebiWhite, ebiLabel, ebiTriangle, ebiSquare, ebiRight, ebiNot, ebiForce, ebiExpand, ebiContract, ebiChoice;
    
    private String comment = ""; // taken from SGF -- what is commented in this node
    Node tree; // the parsed SGF plus user moves
    
    Panel controlPanel;
    Button buttRestart;
    Button buttBack;
    Button buttComment, buttPaths, buttDoSearch, buttClearSearch;
    Choice choiceShow = new Choice();
    Choice choiceSize = new Choice();
    Choice choiceKool = new Choice();
    Checkbox checkboxSide;
    StatusLabel statuslabel;
    CommentLabel commentlabel;
    MoveLabel movelabel;
    public ClockControl clockControl;
    public Smile smileControl;
    AnimateThread anim; // animate solution thread
    
    ///////// edit stuff
    ScrollPane atlasPane = new ScrollPane();
    public Atlas atlas; // show the move tree
    PictureButton ebCut, ebRight, ebNot, ebForce, ebExpand, ebChoice;
    PictureCheckbox ebWhite, ebLabel, ebSetup, ebBlack, ebTriangle, ebSquare;
    CheckboxGroup editCheckboxGroup = new CheckboxGroup();
    Label rolloverLabel = new Label("");
    Vector rolloverComps = new Vector(), rolloverTexts = new Vector();
    Button submitEdit;
    
    MoveDelayThread moveDelay = null; // pause before answering thread
    int moveDelayTime = 300;
    
    String solutionPaths; // how previous folks have tried to solve this problem
    
    public static boolean advancedJava = false;
    
    // the colors that shape our world
//  public Color colBack = new Color(229, 200, 151);
//  public Color colWood = new Color(238, 170, 106);
//  public Color colBack = new Color(0x00819A65);
    public Color colBack = new Color(0x00bbbbbb);
    public Color colWood = new Color(0x00E3B268);
    Color colShadow = new Color(130, 79, 50);
    
    protected GridBagLayout gridBag;
    protected GridBagConstraints gridConstraints;
    
    boolean loadError = false;
    
    private boolean validating = false, udahValidated = false;
    private boolean seenBefore = false; // set if the user has already seen this problem
    int dbid = 0;
    private int userID = 0;
    private int numcom = 0;
    private long startMillis = System.currentTimeMillis();
    
    Stack bstates = new Stack(); // to make going back simple, keep a stack of past board states
    
    class BoardState {
        Intersection board[][] = new Intersection[19][19];
        int toMove;
        Node curnode;
        Point lastMove;
        int result;
        
        BoardState() {}
    }
    
    public void init () {
        try {
            Class class1 = Class.forName("java.awt.Graphics2D");
            if (class1 != null)
                advancedJava = true;
        }
        catch (Exception e1) {
        }
        if (!advancedJava)
            System.out.println("using java 1.1");
        
        //    System.out.println("starting up...");
        // let's get the base document root
        // some browsers/jre's don't return us the correct path, so we do it ourselves
        String ss = getDocumentBase().toString();
        int ind = ss.lastIndexOf("/");
        if (ind > 0) {
            ss = ss.substring(0, ind+1);
        }
        try {
            docBase = new URL(ss);
            System.out.println(docBase);
        }
        catch (MalformedURLException e) {
            docBase = getDocumentBase();
            System.out.println("error on doc base: " + ss);
        }
        
        if (advancedJava)
            gb = new Goban2D(this);
        else
            gb = new Goban(this);
        
        // applet size, good to know
        width = Integer.parseInt(getParameter("width"));
        height = Integer.parseInt(getParameter("height"));
        if (getParameter("validate") != null)
            validating = true;
        if (getParameter("id") != null) // database id of puzzle, if given
            dbid = Integer.parseInt(getParameter("id"));
        if (getParameter("userID") != null) // user ID, if given
            userID = Integer.parseInt(getParameter("userid"));
        if (getParameter("numcom") != null) // number of comments
            numcom = Integer.parseInt(getParameter("numcom"));
        if (getParameter("movedelaytime") != null) // delay before move response in milliseconds
            moveDelayTime = Integer.parseInt(getParameter("movedelaytime"));
        if (getParameter("solutionpaths") != null) // delay before move response in milliseconds
            solutionPaths = getParameter("solutionpaths");
        if (getParameter("seenbefore") != null) // seen this problem before
            seenBefore = true;
        if (getParameter("trialid") != null) {
            trialid = Integer.parseInt(getParameter("trialid"));
            trialMode = true; // stay in trial mode until lives gone or problem solved
        }
        if (getParameter("search") != null) {
            int searchVal = Integer.parseInt(getParameter("search"));
            if (searchVal > 0)
                searchMode = true; // looking for something
        }
        
        // translation
        String language = "en";
        String country = "";
        if (getParameter("lang") != null)
            language = getParameter("lang");
        if (getParameter("country") != null)
            country = getParameter("country");
        curLocale = new Locale(language, country);
        resBundle = ResourceBundle.getBundle("lang/TextBundle",
                curLocale);
//      System.out.println("back: " + resBundle.getString("backone"));
        buttBack = new Button("<-- " + resBundle.getString("backone"));
        buttRestart = new Button(resBundle.getString("restart"));
        buttRestart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (moveDelay == null) // don't do if waiting for move response
                {
                    reset2sgf();
                    if (trialMode && !alreadyDied) {
                        smileControl.loseLife();
                    }
                    alreadyDied = false;
                }
            }
        });
        
        if (getParameter("edit") != null) {
            int editVal = Integer.parseInt(getParameter("edit"));
            if (editVal > 0) {
                atlas = new Atlas(this); // show the move tree
                atlasPane.add(atlas);
                editMode = true; // modifying some SGF
            }
        }
        
        // let's do a version check to make sure the browser isn't loading from the cache when we've got a new version available
        if (VERSION != Integer.parseInt(getParameter("version"))) {
            loadError = true;
            add(new TextArea("Your web browser is trying to load an older, cached version of this Java applet (" + VERSION + ", should be " + getParameter("version") + "). To fix this, try hitting the Reload/Refresh button WHILE holding down the Control and shift keys. If this doesn't work, try clearing your cache, quitting your browser, and reloading.", 8, 40, TextArea.SCROLLBARS_NONE));
            return;
        }
        
        if (!doneInit) {
            doneInit = true;
            
            statuslabel = new StatusLabel(this);
            commentlabel = new CommentLabel(this);
            commentlabel.addKeyListener(gb);
            movelabel = new MoveLabel(this);
            clockControl = new ClockControl(this);
            smileControl = new Smile(this);
            if (getParameter("timelimit") != null) // seconds to make a move
                clockControl.setTimelimit(Integer.parseInt(getParameter("timelimit")));
            if (getParameter("lives") != null) // lives
                smileControl.lives = Integer.parseInt(getParameter("lives"));
            
            sgf = getSGF();
            
            gb.calcViewable();
            if (editMode) {
                gb.markCrayons(); // add some color to our mundane lives
                // if there's too little showing (like nothing) then show everything
                if (gb.viewable.width < 3 || gb.viewable.height < 3) {
                    gb.viewable = new Rectangle(0, 0, 19, 19);
                    expandedMode = true;
                }
            }
            if (atlas != null)
                atlas.calculatePositions(tree);
            
            // find longest comment in sgf so we know how big to make comment box
            LongCommentRecurser longc = new LongCommentRecurser();
            if (tree != null)
                tree.generalRecurse(longc);
            commentlabel.calcString(longc.longest, width - 40);
            
            // load images from site
            srcBoard = loadImage("board.gif");
            srcWhite = loadImage("whitetrans.gif");
            srcWhite = loadImage("v2/white.png");
            srcBlack = loadImage("blacktrans.gif");
            srcBlack = loadImage("v2/black.png");
            smileyImg = loadImage("smile.gif");
            
            sBoard = srcBoard;
            // temporarily set to large
            sWhite = srcWhite;
            sBlack = srcBlack;
            
            useBigImages();
            
            // edit images
            
            final String EDIT_PATH = "edit/";
            ebiCut = loadImage(EDIT_PATH + "cross.gif");
            ebiSetup = loadImage(EDIT_PATH + "harmony.gif");
            ebiBlack = loadImage(EDIT_PATH + "blackbutt.gif");
            ebiWhite = loadImage(EDIT_PATH + "whitebutt.gif");
            ebiLabel = loadImage(EDIT_PATH + "lettera.gif");
            ebiTriangle = loadImage(EDIT_PATH + "triangle.gif");
            ebiSquare = loadImage(EDIT_PATH + "square.gif");
            ebiRight = loadImage(EDIT_PATH + "right.gif");
            ebiNot = loadImage(EDIT_PATH + "notthis.gif");
            ebiForce = loadImage(EDIT_PATH + "force.gif");
            ebiChoice = loadImage(EDIT_PATH + "target.gif");
            ebiExpand = loadImage(EDIT_PATH + "expand.gif");
            ebiContract = loadImage(EDIT_PATH + "contract.gif");
            ebCut = new PictureButton(ebiCut);
            ebCut.setBackground(Color.white);
            ebCut.addKeyListener(gb);
            ebSetup = new PictureCheckbox(ebiSetup, editCheckboxGroup, true, gb);
            ebSetup.setBackground(Color.white);
            ebBlack = new PictureCheckbox(ebiBlack, editCheckboxGroup, false, gb);
            ebBlack.setBackground(Color.white);
            ebBlack.addItemListener(this);
            ebWhite = new PictureCheckbox(ebiWhite, editCheckboxGroup, false, gb);
            ebWhite.setBackground(Color.white);
            ebWhite.addItemListener(this);
            ebLabel = new PictureCheckbox(ebiLabel, editCheckboxGroup, false, gb);
            ebLabel.setBackground(Color.white);
            ebTriangle = new PictureCheckbox(ebiTriangle, editCheckboxGroup, false, gb);
            ebTriangle.setBackground(Color.white);
            ebSquare = new PictureCheckbox(ebiSquare, editCheckboxGroup, false, gb);
            ebSquare.setBackground(Color.white);
            ebRight = new PictureButton(ebiRight);
            ebRight.setBackground(Color.white);
            ebRight.addKeyListener(gb);
            ebNot = new PictureButton(ebiNot);
            ebNot.setBackground(Color.white);
            ebNot.addKeyListener(gb);
            ebForce = new PictureButton(ebiForce);
            ebForce.setBackground(Color.white);
            ebForce.addKeyListener(gb);
            ebChoice = new PictureButton(ebiChoice);
            ebChoice.setBackground(Color.white);
            ebChoice.addKeyListener(gb);
            if (expandedMode)
                ebExpand = new PictureButton(ebiContract);
            else
                ebExpand = new PictureButton(ebiExpand);
            ebExpand.setBackground(Color.white);
            ebExpand.addKeyListener(gb);
            
            // watch these images -- we want to know when they're loaded
            tracker = new MediaTracker(this);
            tracker.addImage(srcBoard, 0);
            tracker.addImage(srcWhite, 1);
            tracker.addImage(srcBlack, 1);
            tracker.addImage(sBoard, 0);
            tracker.addImage(sWhite, 1);
            tracker.addImage(sBlack, 1);
            tracker.addImage(smileyImg, 1);
            
            // in search mode, we start off in the mists...
            if (searchMode) {
                for (int i=0;i<19;i++)
                    for (int j=0;j<19;j++)
                        gb.board[i][j].stone = MURKY;
            }
            
            // layout
            setBackground(colBack);
            setLayout(new BorderLayout());
            
            gridBag = new GridBagLayout();
            gridConstraints = new GridBagConstraints();
            
            // framework
            controlPanel = new Panel();
            controlPanel.setLayout(gridBag);
            gridConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridConstraints.gridwidth = GridBagConstraints.REMAINDER;
            gridConstraints.ipady = 2;
            controlPanel.setBackground(colBack);
            
            // to-move label
            gridBag.setConstraints(movelabel, gridConstraints);
            controlPanel.add(movelabel);
            movelabel.addKeyListener(gb);
            
            // edit button stuff
            GridLayout editGrid = new GridLayout(2, 4);
            Panel editControlPanel = new Panel();
            GridLayout editGridOpts = new GridLayout(2, 3);
            Panel editControlPanelOpts = new Panel();
            
            if (searchMode == false) {
                // restart button
//              buttRestart.setBackground(colBack);
//              buttRestart.setForeground(Color.BLACK);
                gridBag.setConstraints(buttRestart, gridConstraints);
                controlPanel.add(buttRestart);
                buttRestart.setEnabled(false);
                buttRestart.addActionListener(this);
                buttRestart.addKeyListener(gb);
                
                // back button
                gridBag.setConstraints(buttBack, gridConstraints);
                controlPanel.add(buttBack);
                buttBack.setEnabled(false);
                buttBack.addActionListener(this);
                buttBack.addKeyListener(gb);
                
                if (editMode) {
                    // rollover label
                    gridBag.setConstraints(rolloverLabel, gridConstraints);
                    controlPanel.add(rolloverLabel);
                    // submit button
                    if (dbid == 0)
                        submitEdit = new Button(resBundle.getString("submitproblem"));
                    else
                        submitEdit = new Button(resBundle.getString("submitchanges"));
                    gridBag.setConstraints(submitEdit, gridConstraints);
                    controlPanel.add(submitEdit);
                    submitEdit.addActionListener(this);
                    
                    // edit button stuff
                    editControlPanel.setLayout(editGrid);
                    editControlPanel.setBackground(colBack);
                    editControlPanelOpts.setLayout(editGridOpts);
                    editControlPanelOpts.setBackground(colBack);
                    
                    // edit buttons
                    editControlPanel.add(ebCut); ebCut.addActionListener(this);
                    editControlPanel.add(ebNot); ebNot.addActionListener(this);
                    editControlPanel.add(ebRight); ebRight.addActionListener(this);
                    editControlPanel.add(ebForce); ebForce.addActionListener(this);
                    editControlPanel.add(ebChoice); ebChoice.addActionListener(this);
                    editControlPanel.add(ebExpand); ebExpand.addActionListener(this);
                    
                    editControlPanelOpts.add(ebSetup);
                    editControlPanelOpts.add(ebBlack);
                    editControlPanelOpts.add(ebWhite);
                    editControlPanelOpts.add(ebTriangle);
                    editControlPanelOpts.add(ebSquare);
                    editControlPanelOpts.add(ebLabel);
                    
                    gridBag.setConstraints(editControlPanel, gridConstraints);
                    controlPanel.add(editControlPanel);
                    gridBag.setConstraints(editControlPanelOpts, gridConstraints);
                    controlPanel.add(editControlPanelOpts);
                    
                    // rollover stuff
                    addRollover(ebCut, "delete from here");
                    addRollover(ebNot, "disallow this move");
                    addRollover(ebRight, "mark un/correct");
                    addRollover(ebForce, "force this move");
                    addRollover(ebChoice, "mark as choice");
                    addRollover(ebExpand, "change zoom level");
                    
                    addRollover(ebSetup, "setup: shift = white");
                    addRollover(ebBlack, "play black");
                    addRollover(ebWhite, "play white");
                    addRollover(ebTriangle, "mark triangle");
                    addRollover(ebSquare, "mark square");
                    addRollover(ebLabel, "mark label");
                }
                
                // show solution
                choiceShow.addItem(resBundle.getString("showsolution"));
                choiceShow.addItem(resBundle.getString("numberedsolution"));
                choiceShow.addItem(resBundle.getString("animatesolution"));
                choiceShow.addItem(resBundle.getString("navigatesolution"));
                choiceShow.addItemListener(this);
                choiceShow.addKeyListener(gb);
                choiceShow.setBackground(colBack);
                if (trialMode)
                    choiceShow.setEnabled(false);
                if (!validating) {
                    gridBag.setConstraints(choiceShow, gridConstraints);
                    controlPanel.add(choiceShow);
                }
            }
            
            // stone size
            choiceSize.addItem(resBundle.getString("stonesize"));
            choiceSize.addItem(resBundle.getString("large"));
            choiceSize.addItem(resBundle.getString("small"));
            choiceSize.addItemListener(this);
            choiceSize.addKeyListener(gb);
            choiceSize.setBackground(colBack);
            gridBag.setConstraints(choiceSize, gridConstraints);
            controlPanel.add(choiceSize);
            
            // only add some stuff if we're actually on the live site
            if (dbid > 0) {
                // coolness
                choiceKool.addItem(resBundle.getString("ratecoolness"));
                for (int i=10;i>=1;i--)
                    choiceKool.addItem(Integer.toString(i));
                choiceKool.addItemListener(this);
                choiceKool.setBackground(colBack);
                if (!validating) {
                    gridBag.setConstraints(choiceKool, gridConstraints);
                    controlPanel.add(choiceKool);
                }
                
                if (numcom == 0)
                    buttComment = new Button(resBundle.getString("addcomment"));
                else {
                    String ns = " (" + numcom + ")";
                    if (numcom > 1)
                        buttComment = new Button(resBundle.getString("readcomments") + ns);
                    else
                        buttComment = new Button(resBundle.getString("readcomment") + ns);
                }
                buttPaths = new Button(resBundle.getString("seeattempts"));
                if (!validating) {
                    // comment
                    gridBag.setConstraints(buttComment, gridConstraints);
                    controlPanel.add(buttComment);
                    if (!seenBefore)
                        buttComment.setEnabled(false);
                    buttComment.addActionListener(this);
                    buttComment.addKeyListener(gb);
                    
                    // paths
                    if (solutionPaths == null) {
                        gridBag.setConstraints(buttPaths, gridConstraints);
                        controlPanel.add(buttPaths);
                        if (!seenBefore)
                            buttPaths.setEnabled(false);
                        buttPaths.addActionListener(this);
                        buttPaths.addKeyListener(gb);
                    }
                }
            }
            
            if (searchMode) {
                // add parts to panel for doing a search
                buttClearSearch = new Button(resBundle.getString("clear"));
                gridBag.setConstraints(buttClearSearch, gridConstraints);
                controlPanel.add(buttClearSearch);
                buttClearSearch.addActionListener(this);
                
                buttDoSearch = new Button(resBundle.getString("dosearch"));
                gridBag.setConstraints(buttDoSearch, gridConstraints);
                controlPanel.add(buttDoSearch);
                buttDoSearch.addActionListener(this);
                
                checkboxSide = new Checkbox(resBundle.getString("onside"));
                gridBag.setConstraints(checkboxSide, gridConstraints);
                controlPanel.add(checkboxSide);
            }
            
            if (trialMode) {
                gridBag.setConstraints(clockControl, gridConstraints);
                controlPanel.add(clockControl);
                
                gridBag.setConstraints(smileControl, gridConstraints);
                controlPanel.add(smileControl);
            }
            
            gridBag.setConstraints(statuslabel, gridConstraints);
            controlPanel.add(statuslabel);
            
            Panel cont = new Panel();
            cont.add(controlPanel);
            add("West", cont);
            add("North", commentlabel);
            add("Center", gb);
            if (editMode) {
                // calculate atlas size
                int leftPanel = 150; // size of crap on the left side
                int wantedMiddle = 680; // ideally, we'd have this much space to have a full 19x19 board
                int minAtlas = 256; // at least this big
                int minMiddle = 345; // board must be this big!
                int atlasW = (width - wantedMiddle - leftPanel);
                if (atlasW < minAtlas)
                    atlasW = minAtlas;
                if (atlasW + minMiddle + leftPanel > width)
                    atlasW = (width - minMiddle - leftPanel);
                atlasPane.setSize(atlasW, 356);
                add("East", atlasPane);
                //        add("East", atlas);
            }
            
            int stone = tree.getToMove();
            if (stone > 0)
                setToMove(stone);
            playerStone = gb.toMove;
        }
        
        addKeyListener(this);
    } // end of init()
    
    /**
     * if we're in eclipse, this is a good way to load in SGF
     */
    public String getSGFFromDisc() {
        String fName = getParameter("sgffile");
        if (fName == null || fName.length() == 0)
            return null;
        
        System.out.println("loading sgf file " + fName);
        
        // maybe read in file
        try {
            FileReader fr = new FileReader(fName);
            BufferedReader br = new BufferedReader(fr);
            String s;
            String sgf = "";
            while ((s = br.readLine()) != null) {
                sgf = sgf + s + "\n";
            }
            return sgf;
        } catch (FileNotFoundException e1) {
            System.err.println("file not found: " + fName);
        } catch (IOException e2) {
        }
        return null;
    }
    
    /**
     * @throws HeadlessException
     */
    private String getSGF() {
        // should we parse in an sgf history?
        String sgf = getParameter("sgf");
        if (sgf == null)
            sgf = getSGFFromDisc();
        if (sgf != null && sgf.length() > 0) {
            Parser prse = new Parser();
            try {
                tree = prse.parse(sgf, gb.board);
            }
            catch (Exception e) {
                loadError = true;
                add(new TextArea("Invalid SGF data. Sorry, you're screwed. Error message: " + e.getMessage(), 8, 40, TextArea.SCROLLBARS_NONE));
                throw new IllegalArgumentException("Invalid SGF data. Sorry, you're screwed. Error message: " + e.getMessage());
            }
            // if we're exploring paths of solutions, apply pathrecurser
            if (solutionPaths != null && solutionPaths.length() > 0) {
                PathRecurser pr = new PathRecurser(solutionPaths);
                tree.generalRecurse(pr);
            }
            // gb.curnode = tree.advance2move(this);
            tree = tree.collapsePrelude(this);
            gb.curnode = tree;
            gb.curnode.execute(this);
        }
        else {
            // special case: just make a null node and return that
            tree = new Node(null);
            // add a sig for us...
            tree.xtraTags.addElement(new String("AP"));
            tree.xtraTagVals.addElement(new String("goproblems"));
            gb.curnode = tree;
        }
        return sgf;
    }
    
    // go to 32x32
    void useBigImages() {
        imgWhite = srcWhite;
        imgBlack = srcBlack;
        imgBoard = srcBoard;
        ghostWhite = srcghostWhite;
        ghostBlack = srcghostBlack;
    }
    
    // go to 16x16
    void useSmallImages() {
        imgWhite = sWhite;
        imgBlack = sBlack;
        imgBoard = sBoard;
        ghostWhite = sghostWhite;
        ghostBlack = sghostBlack;
    }
    
    // user hit Back button -- go up one stage
    public void doBack() {
        // if the back button is disabled, don't do this
        if (buttBack.isEnabled() == false)
            return;
        
        // we keep a history of game states (one for each move)
        // just pop up the stack
        if (bstates.size() == 0)
            return;
        
        BoardState bs = (BoardState) bstates.pop();
        // load current vars from saved state
        if (bstates.size() == 0) {
            buttBack.setEnabled(false);
            buttRestart.setEnabled(false);
        }
        setToMove(bs.toMove);
        gb.curnode = bs.curnode;
        gb.lastMove = bs.lastMove;
        result = bs.result;
        for (int i=0;i<19;i++)
            for (int j=0;j<19;j++)
                gb.board[i][j] = bs.board[i][j];
        
        if (editMode)
            gb.markCrayons();
        
        setComment("");
        if (gb.curnode != null) {
            // see if we go back one more -- in the middle of response move
            if (gb.toMove != playerStone && mode != MODENAVIGATE && editMode == false)
                doBack();
            else if (gb.curnode.comment != null)
                setComment(""+gb.curnode.comment);
        }
        statuslabel.repaint();
        postMove();
    }
    
    // user clicked on expand button
    void doExpand() {
        expandedMode = !expandedMode;
        // image is what we're going to
        if (expandedMode)
            ebExpand.img = ebiContract;
        else
            ebExpand.img = ebiExpand;
        
        // change viewable rectangle
        if (expandedMode) {
            gb.viewable = new Rectangle(0, 0, 19, 19);
        }
        else {
            gb.calcViewable();
        }
        autoSizeGoban();
        gb.fullDraw = true; // so we repaint over old board
        gb.repaint();
        ebExpand.repaint();
    }
    
    void doToMoveClick(int stone) {
        //    System.out.println("stone: " + stone);
        setToMove(stone);
        playerStone = stone;
    }
    
    // based on tree and position, see who could possibly move
    void calcToMovePoss() {
        if (tree.babies.size() == 0) {
            // this means we're at the start and there is nothing around
            ebBlack.setEnabled(true);
            ebWhite.setEnabled(true);
        }
        else {
            ebWhite.setEnabled(gb.toMove == WHITE ? true : false);
            ebBlack.setEnabled(gb.toMove == BLACK ? true : false);
        }
    }
    
    void doSubmitEdit() {
        String sgf = "(" + tree.outputSGF() + ")";
        sgf = URLEncoder.encode(sgf);
        //    System.out.println("SGF: " + s);
        URL url;
        // can we send directly? or do we need a POST?
//      if (sgf.length() < 2) {
//      String params = ADD_PROBLEM_PAGE + "?";
//      if (dbid > 0)
//      params += "id=" + dbid + "&";
//      params += "validsgf=" + (tree.validSGF() ? 1 : 0) + "&sgf=" + sgf;
//      try {
//      url = new URL("http", "www.goproblems.com", params);
//      getAppletContext().showDocument(url);
//      }
//      catch (MalformedURLException e) {
//      System.out.println("error on url: " + e);
//      }
//      }
//      else 
        {
            // POST it
            showStatus(resBundle.getString("sendingproblem"));
            int objID = NetUtil.POSTtheSGF(docBase.toString(), sgf);
            if (objID > 0) {
                showStatus("problem received, loading page");
                // if success, let's go to right page
                String params = ADD_PROBLEM_PAGE + "?";
                if (dbid > 0)
                    params += "id=" + dbid + "&";
                params += "validsgf=" + (tree.validSGF() ? 1 : 0) + "&lobj=" + objID;
                try {
                    url = new URL(docBase.toString() + params);
                    getAppletContext().showDocument(url);
                }
                catch (MalformedURLException e) {
                    System.out.println("error on url: " + e);
                }
            }
            else
                showStatus("problem not received, please try again");
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == buttBack)
            doBack();
        
        if (e.getSource() == buttDoSearch)
            gb.doSearch();
        
        if (e.getSource() == buttClearSearch)
            gb.doClearSearch();
        
        if (e.getSource() == ebCut)
            gb.doCut();
        if (e.getSource() == ebRight)
            atlas.doRight();
        if (e.getSource() == ebNot)
            atlas.doNot();
        if (e.getSource() == ebForce)
            atlas.doForce();
        if (e.getSource() == ebChoice)
            atlas.doChoice();
        if (e.getSource() == ebExpand)
            doExpand();
        
        if (e.getSource() == submitEdit)
            doSubmitEdit();
        
        if (e.getSource() == buttComment && dbid > 0) {
            try {
                // record comment in database by opening php page that does the work
                String path = readablePath();
                URL url = new URL(docBase, "comment.php3?prob="+dbid + "&path="+path);
//              System.out.println(url);
                getAppletContext().showDocument(url, "_blank");
            }
            catch (java.net.MalformedURLException exep) {
            }
        }
        
        if (e.getSource() == buttPaths && dbid > 0) {
            try {
                // record comment in database by opening php page that does the work
                getAppletContext().showDocument(new URL(docBase, "prob.php3?id="+dbid+"&pths=1"), "_blank");
            }
            catch (java.net.MalformedURLException exep) {
            }
        }
    }
    
    // navigate to next time trial problem
    public void nextTimeTrial() {
        try {
            int lvs = (smileControl.lives * dbid) << 8; // cargo cult obfuscation
            lvs |= adutil.rand(256);
            getAppletContext().showDocument(new URL(docBase, "prob.php3?id="+dbid+"&trialid="+trialid+"&vl="+lvs));
        }
        catch (java.net.MalformedURLException exep) {
        }
    }
    
    // someone voted for coolness
    void doKool(String s) {
        if (dbid == 0)
            return;
        
        int k;
        try {
            k = Integer.parseInt(s);
        }
        catch (Exception e) {
            return;
        }
        // record comment in database by opening php page that does the work
        UrlThread ut = new UrlThread("koolvote.php3?id="+dbid+"&cool="+k);
        ut.start();
    }
    
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() instanceof Choice) {
            String s = (String) e.getItem();
            if (s.equals(resBundle.getString("numberedsolution")))
                doNumberedSolution();
            if (s.equals(resBundle.getString("navigatesolution")))
                doNavigateSolution();
            if (s.equals(resBundle.getString("animatesolution")))
                doAnimateSolution();
            if (s.equals(resBundle.getString("large")))
                gb.goLarge();
            if (s.equals(resBundle.getString("small")))
                gb.goSmall();
            if (e.getSource() == choiceKool)
                doKool(s);
        }
        if (e.getSource() == ebWhite)
            if (editCheckboxGroup.getSelectedCheckbox() == ebWhite)
                doToMoveClick(WHITE);
        if (e.getSource() == ebBlack)
            if (editCheckboxGroup.getSelectedCheckbox() == ebBlack)
                doToMoveClick(BLACK);
    }
    
    // show the solution animating through the moves
    public void doAnimateSolution() {
        setResult(WRONG, false); // once we've shown 'em, they lose
        reset2sgf();
        mode = MODEANIMATE;
        anim = new AnimateThread(this);
        anim.start();
    }
    
    // let user navigate through solution
    public void doNavigateSolution() {
        setResult(WRONG, false); // once we've shown 'em, they lose
        reset2sgf();
        mode = MODENAVIGATE;
        gb.markCrayons();
    }
    
    // show static solution as a numbered sequence
    public void doNumberedSolution() {
        setResult(WRONG, false); // once we've shown 'em, they lose
        reset2sgf();
        Onatop ona = new Onatop();
        // do up board
        gb.curnode.numberSolution(this, 1, ona);
        // now, are any moves on top of others? set comment appropriately
        String s = "";
        int cnt = 0;
        while (ona.child != null) {
            int num = ona.num;
            Point p = ona.p;
            //System.out.println("num " + num);
            if (cnt > 0)
                s += ", ";
            s += num + " at " + gb.board[p.x][p.y].text;
            ona = ona.child;
            cnt++;
        }
        setComment(s);
    }
    
    public int getResult() {
        return result;
    }
    
    public int getRealResult() {
        return realresult;
    }
    
    public int getToMove() {
        return gb.toMove;
    }
    
    public void setToMove(int tom) {
        gb.toMove = tom;
        movelabel.repaint();
        //    if (atlas != null)
        //      atlas.repaint();
        if (editMode) {
            // select checkboxgroup edit item accordingly
            editCheckboxGroup.setSelectedCheckbox(tom == WHITE ? ebWhite : ebBlack);
            calcToMovePoss();
        }
    }
    
    public void switchToMove() {
        setToMove(gb.toMove % 2 + 1);
    }
    
    void reset2sgf() {
        // kill animater
        if (anim != null)
            anim.wannadie = true;
        gb.clearBoard();
        gb.clearFluff();
        setResult(0, true);
        mode = MODEMOVE;
        gb.curnode = tree;
        gb.curnode.execute(this);
        setToMove(playerStone);  // which color player is
        bstates.removeAllElements();
        buttBack.setEnabled(false);
        buttRestart.setEnabled(false);
        gb.repaint();
    }
    
    // just touch the URL -- let the php do the real work
    class UrlThread extends Thread {
        String xtra;
        public UrlThread(String xtra) {
            super("Urlalator");
            setPriority(Thread.MIN_PRIORITY);
            this.xtra = xtra;
        }
        public void run() {
            try {
                URL u;
                u = new URL(docBase.toString() + "/" + xtra);
                /* InputStream is = */ u.openStream();
            }
            catch (MalformedURLException e) {
                System.err.println(e);
            }
            catch (IOException e) {
                System.err.println(e);
            }
        } // end RUN
    }
    
    // how we send result to server
    public void sendResult2Server(int res, boolean hard, String path) {
        int secs = (int) ((System.currentTimeMillis() - startMillis) / 1000);
        
        String s = "solve.php3?id="+dbid+"&solved="+(res == RIGHT ? "1" : "0")+"&userid="+userID+"&hardstop="+(hard ? "1" : "0")+"&secs="+secs+"&path="+path;
        if (trialid > 0) {
            s = s + "&trialid="+trialid+"&lives="+smileControl.lives;
        }
        UrlThread ut = new UrlThread(s);
        ut.start();
    }
    
    // how they're doing
    // we clear result on resets, but realresult will be remembered
    // hard means show it now, if it's not hard, we secretly remember their failure now as to not count any success -- they've started down a wrong path, but we don't want to inform them of this as yet
    public void setResult(int res, boolean hard) {
        setResult(res, hard, null);
    }
    public void setResult(int res, boolean hard, Point finalMove) {
        if (hard) {
            if (res == WRONG && !alreadyDied) {
                smileControl.loseLife();
                alreadyDied = true;
            }
            if (res == RIGHT) {
                clockControl.canAdvance = true;
                leaveTrialMode();
            }
        }
        
        // we might just be validating a problem -- making sure that it's actually solveable
        if (res == RIGHT && validating && !udahValidated) {
            // they did it!
            udahValidated = true;
            //	  System.out.println("validated");
            
            UrlThread ut = new UrlThread("testres.php3?id="+dbid);
            ut.start();
            
            comment = "Problem solved, added to the database -- you can return to Problematic's home.";
            commentlabel.repaint();
        }
        
        // send along failure path or solve path
        String path = generatePath();
        
        if (res > 0 &&
                (realresult == 0 || (hard && sentHard == false)) ) {
            realresult = res;
            if (hard)
                sentHard = true;
            if (!validating && dbid > 0 && (solutionPaths == null)) {
                // record result in database
                sendResult2Server(res, hard, path);
            }
        }
        if (hard) {
            result = res;
            if (buttComment != null)
                buttComment.setEnabled(true);
            if (buttPaths != null)
                buttPaths.setEnabled(true);
        }
        statuslabel.repaint();
    }
    
    /**
     * @param finalMove
     * @return
     */
    private String generatePath() {
        String path = "";
        int sz = ((Vector) bstates).size();
        for (int i=0;i<sz;i++) {
            Point move = ((BoardState) ((Vector) bstates).elementAt(i) ).lastMove;
            path = path + String.valueOf(move.x) + "." + String.valueOf(move.y) + "|";
        }
        return path;
    }
    
    /**
     * @param finalMove
     * @return
     */
    private String readablePath() {
        String path = "";
        int sz = ((Vector) bstates).size();
        for (int i=0;i<sz;i++) {
            Point move = ((BoardState) ((Vector) bstates).elementAt(i) ).lastMove;
            char c = 'A';
            // skip over 'i'
            int x = move.x;
            c = (char) ((int) 'A' + x + ((x >= (int) 'I' - (int) 'A') ? 1 : 0));
            String sx = new Character(c).toString();
            String sy = "" + (gb.boardY - move.y);
            if (i > 0)
                path += "+";
            path = path + sx + sy;
        }
        return path;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String s) {
        if (comment.equals(s))
            return;
        comment = s;
        commentlabel.repaint();
    }
    
    public void start() {
        runner = new Thread(this);
        runner.start();
    }
    
    public void stop() {
        runner = null;
    }
    
    // the GhostFilter makes an outline of a stone for hovering -- just draws every other pixel
    class GhostFilter extends RGBImageFilter {
        public GhostFilter() {}
        public int filterRGB(int x, int y, int rgb) {
            if (((x + y) & 1) == 1)
                rgb &= 0x00ffffff;
            return rgb;
        }
    }
    
    // see what fits
    public void autoSizeGoban() {
        boolean wantSmall = false;
        // are we as big as we'd like? if not, try this effective herbal supplement, guaranteed to increase her pleasure
        Dimension dim = gb.getSize();
        Dimension wantDim = gb.getPreferredSize();
        //    System.out.println("dim " + Integer.toString(dim.width) + " wantdim " +  Integer.toString(wantDim.width));
        if (dim.width < wantDim.width || dim.height < wantDim.height)
            wantSmall = true;
        if (wantSmall)
            gb.sz = 16;
        
        if (wantSmall)
            gb.goSmall();
        else
            gb.goLarge();
    }
    
    public void run() {
        if (loadError)
            return;
        try {
            tracker.waitForID(0);
            tracker.waitForID(1); }
        catch (InterruptedException e) {
            return;
        }
        
        // make image ghosts
        srcghostWhite = createImage(new FilteredImageSource(srcWhite.getSource(), new GhostFilter()));
        srcghostBlack = createImage(new FilteredImageSource(srcBlack.getSource(), new GhostFilter()));
        sBoard = createImage(new FilteredImageSource(srcBoard.getSource(), new CropImageFilter(0, 0, 16, 16)));
        sWhite = createImage(new FilteredImageSource(srcWhite.getSource(), new ReplicateScaleFilter(16, 16)));
        sBlack = createImage(new FilteredImageSource(srcBlack.getSource(), new ReplicateScaleFilter(16, 16)));
        
        sghostWhite = createImage(new FilteredImageSource(sWhite.getSource(), new GhostFilter()));
        sghostBlack = createImage(new FilteredImageSource(sBlack.getSource(), new GhostFilter()));
        
        autoSizeGoban();
        
        gb.repaint();
        smileControl.repaint();
        if (atlas != null)
            atlas.repaint();
        Thread me = Thread.currentThread();
        while (runner == me) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
    }
    
    /*
     public void update(Graphics g) {
     paint(g);
     }*/
    
    // push current board state onto a stack of board state histories
    void pushBoard(int x, int y) {
        // System.out.println("push " + bstates.size());
        BoardState bs = new BoardState();
        bs.toMove = gb.toMove;
        bs.curnode = gb.curnode;
        bs.lastMove = new Point(x, y);
        bs.result = result;
        for (int i = 0; i < 19; i++)
            for (int j = 0; j < 19; j++)
                bs.board[i][j] = new Intersection(gb.board[i][j]);
        bstates.push(bs);
        if (!trialMode)
            buttBack.setEnabled(true);
        buttRestart.setEnabled(true);
    }
    
    public void leaveTrialMode() {
        trialMode = false;
        choiceShow.setEnabled(true);
        clockControl.stopClock();
        if (bstates.size() > 0)
            buttBack.setEnabled(true);
    }
    
    public void keyPressed(KeyEvent ev) {
    }
    public void keyReleased(KeyEvent ev) {
    }
    public void keyTyped(KeyEvent ev) { 
    }
    
    void addRollover(Component comp, String text) {
        rolloverComps.addElement(comp);
        rolloverTexts.addElement(text);
        comp.addMouseListener(this);
    }
    
    public void mouseReleased(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {
        for (int i = 0; i < rolloverComps.size(); i++) {
            if (rolloverComps.elementAt(i) == e.getSource()) {
                rolloverLabel.setText((String) rolloverTexts.elementAt(i));
            }
        }
    }
    public void mouseExited(MouseEvent e) {
        for (int i = 0; i < rolloverComps.size(); i++) {
            if (rolloverComps.elementAt(i) == e.getSource()) {
                rolloverLabel.setText("");
            }
        }
    }
    
    // called after a move has been made, update some stuff
    public void postMove() {
        gb.repaint();
        if (atlas != null) {
            if ( atlas.ensureVisible() )
                atlas.repaint();
        }
    }
    
    protected Image loadImage(String loc) {
        String path = "/img/" + loc;
//      String path = "img/" + loc;
//      System.out.println("readin: " + path);
        int MAX_IMAGE_SIZE = 32000;  //Change this to the size of
        //your biggest image, in bytes.
        int count = 0;
        BufferedInputStream imgStream = new BufferedInputStream(
                this.getClass().getResourceAsStream(path));
        if (imgStream != null) {
            byte buf[] = new byte[MAX_IMAGE_SIZE];
            try {
                count = imgStream.read(buf);
                imgStream.close();
            } catch (java.io.IOException ioe) {
                System.err.println("Couldn't read stream from file: " + path);
                Image im = loadResourceImage(loc);
                System.err.println(im);
//              return im;
                return null;
            }
            if (count <= 0) {
                System.err.println("Empty file: " + path);
                return null;
            }
            return Toolkit.getDefaultToolkit().createImage(buf);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    
    public static Image loadResourceImage(String path) {
        String loc = "img/" + path;
        java.net.URL imgURL = GoApplet.class.getResource("/" + loc);
//      System.out.println("readin url: " + imgURL);
        if (imgURL == null)
            imgURL = GoApplet.class.getResource("/" + loc);
        if (imgURL != null)
            return new ImageIcon(imgURL).getImage();
        else
            return new ImageIcon(path).getImage();
    }
} // applet

class adutil {
    public static int rand(int max) {
        int x = (int) (Math.random() * max);
        if (x == max) x = max - 1;
        return x;
    }
}
