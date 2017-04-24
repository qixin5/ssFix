import java.applet.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.event.*;
import java.awt.image.*;
import java.text.*;
import java.awt.image.PixelGrabber;

          // Shape imports
/*
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import sun.awt.geom.Crossings;
*/

public class Gob implements HyperConstants,TextConstants
{             
HyperView curView       = null;
Image gobPic            = null;     // Current Reference.
Graphics graphics       = null;     // 
Image selectImage       = null;
Graphics selectGraphics = null;

Image    asynchImage1    = null;   // For double buffered external input.
Graphics asynchGraphics1 = null;   //

Image    asynchImage2    = null;   // For double buffered external input.
Graphics asynchGraphics2 = null;   //

Image backGround         = null;

int totalAttached        = 0;     // Total number of Gobs attached
int maxAttached          = 64;    // Change to suit your needs.  
int attachedIndex        = 0;     
int curAttached          = 0;

Gob[] attachedGob        = new Gob[maxAttached];
Scanline[]   scanLine    = null;

Gob destGob             = null;
Graphics destGraphics   = null;

int fGPen               = 1;
int bGPen               = 1;
int drawMode            = CJAM1;
String gobName          = "";
String infoString       = ""; 
int currentZIndex       = 0;

Spline spline           = null;    // V2.43
double wSin             = 0.0;  // Sin of width relative to z
double hSin             = 0.0;  // Sin of height relative to z
double hyp              = 0.0;  // Hypotenues of Sin

int width                 = 0;  
int height                = 0;
int depth                 = 1;   // Granularity of z
int widthBase             = 0;   // Actual width of image
int heightBase            = 0;   // Actual height of iamge 
int gobNumber             = -1;  // index in goblist #
int bottomMax             = 0;
int topMin                = 0;
int leftMin               = 0;
int rightMax              = 0;
int oldX                  = 0;
int oldY                  = 0;      // For BackGround Restore
int oldZ                  = 0;

int x                     = 0;
int y                     = 0;        // Current Position
int z                     = 0;
   // Multiple background update list/ MouseHandler event stack.
int mX []     = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
int mX2[]     = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
int mY []     = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
int mY2[]     = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
int moveNum   = -1;
 
int x2    = 0;
int y2    = 0;

int gobHx = 0;
int gobHy = 0;      // Home position
int gobHz = 0;

int gravity =1;

int dX=0;
int dY=0;         // Delta for movment
int dZ=0;
Gob orbitGob          = null;
int gobOrbitRadius    = 0;
volatile int gobFlags = 0;
int gobFlags2         = GOB_ACTIVATE_ENA;   // 32 wasnt enough :)

int gobEventFlags     = 0;
int gobTypeFlags      = 0;

int dispatchHoldCount = 0;

int gobGadIndex       = 0;
volatile int clickX=0,clickY=0;  // If onClick() enabled where it was clicked on
volatile int lClickX=-1,lClickY=-1;
int tint = 0;
int collisionClassMask;

public void printGobAttr(HyperView curView)
{
int cc=curView.fGPen;
int cf=curView.curFont;    

   curView.curFont=2;
   curView.fGPen=4;
   curView.text("x      :"+this.x,1,20);
   curView.text("y      :"+this.y,1,40);
   curView.text("z      :"+this.z,1,60);
   curView.text("Dx     :"+this.dX,1,80);
   curView.text("dY     :"+this.dY,1,100);
   curView.text("dZ     :"+this.dZ,1,120);
   curView.text("Width  :"+this.width,1,140);
   curView.text("Height :"+this.height,1,160);
   curView.text("WSin   :"+this.wSin,1,180);
   curView.text("HSin   :"+this.hSin,1,200);
   curView.text("Hyp    :"+this.hyp,1,220);
   curView.text("Height :"+curView.height,1,240);
   curView.text("Width  :"+curView.width,1,260);
   curView.fGPen       = cc;
   curView.curFont     = cf; 
}

Gob()
{
   //this(HyperView.getCurView(),"",10,10,0);
   //return new Gob();
}

Gob(HyperView tView,String tName,int tFlags)
{
   this(tView,tName,-1,-1,tFlags);
}
      //   Target Gob, Name, etc
Gob(Gob tGob,String tName,int tFlags)
{
   this(tGob,tName,-1,-1,tFlags);
}

Gob(Gob tGob,String tName,int tX,int tY,int pX,int pY,Spline tSpline,int tFlags)
{
   this(tGob,tName,tX,tY,pX,pY,tFlags);
   spline         = tSpline;
   x                 = spline.x[0];
   y                 = spline.y[0];
   z                 = spline.z[0];
   spline.targetX = spline.x[1];
   spline.targetY = spline.y[1];
   spline.targetZ = spline.z[1];
   gobFlags |= GOB_SPLINE_ENA;
   if((tSpline.flags & SplineConstants.SP_ZBUFFER_ENA)!=0)
   {
      gobZInit();
      gobFlags |= ZBUFFER_ENA;
   }
}

Gob(HyperView tView,String tName,int tX,int tY,int pX,int pY)
{
   this(tView,tName,tX,tY,0);
   x = pX; y = pY;
}


Gob(Gob tGob,String tName,int tX,int tY,int pX,int pY,int tFlags)
{
   this(tGob,tName,tX,tY,tFlags);
   x = pX; y = pY;
}

Gob(Gob tGob,String tName,int tX,int tY,int tFlags)
{
   this(tGob.curView,tName,tX,tY,(tFlags | GOB_ONGOB));
   if(tGob.totalAttached < tGob.maxAttached)
   {
      tGob.attachedGob[tGob.totalAttached] = this;
      tGob.totalAttached++;
   }
   else
   {
      // Reallocate and swap into place
      Gob tmpGob[] = new Gob[tGob.maxAttached+10];
      for(int t = 0;t<tGob.totalAttached;t++)
         tmpGob[t] = attachedGob[t];
      tGob.attachedGob = null;
      tGob.attachedGob = tmpGob;
      tGob.attachedGob[tGob.totalAttached] = this;
      tGob.totalAttached++; 
      tGob.maxAttached += 10;
   }
   tGob.destGob = this;
   tGob.gobFlags |= GOB_ATTACHED_ENA;
   //curView.HyperViewOut("Contruct added "+this.toString()+" To "+tGob.toString());
}

Gob(HyperView tView,String tName,int sx,int sy,int pX,int pY,int tFlags)
{
   this(tView,tName,sx,sy,tFlags);
   x = pX;
   y = pY;
}

Gob(HyperView tView,String tName,int sx,int sy,int tFlags)
{
   gobName = tName;
   curView = tView;
   if(sx == -1)
   {
      width  = 75;
      height = 75; 
      depth = curView.depth;  
   }
   else
   {   
      width  = sx;
      height = sy;
   }
   gobFlags |= tFlags;      
     // Spline will overwrite this
   x = 0;  //curView.rand(curView.width-this.width);
   y = 0;  //curView.rand(curView.height-this.height);
   z = 0;  //curView.rand(depth>>1)+1;
   bottomMax = (curView.height - height)-1;
   topMin    = (dY);
   leftMin   = (dX);
   rightMax  = (curView.width  - width)   -1;      
        // asynchImage1      = curView.createImage(width,height);
        // asynchGraphics1   = asynchImage1.getGraphics();
        // asynchImage2    = curView.createImage(width,height);
        // asynchGraphics2 = asynchImage2.getGraphics(); 
                 //**** Main Bitmap        
   try
   {
      gobPic          = curView.createImage(width,height);
      graphics        = gobPic.getGraphics(); 
      if((curView.ioFlags & HyperConstants.IMAGE_FROM_JAR_ENA)!=0)
      {
         gobPic = curView.getImageFromJar(gobName);
      }
      else
      {
         gobPic =
          curView.getAppletContext().getImage(new URL(curView.getCodeBase(),gobName));
      }
   }
   catch(MalformedURLException m)
   {
      curView.errOut("addGob();"+curView.getCodeBase()+gobName);
   }
            // Selected BitMap
   if((gobFlags & GOB_USE_SELECT) !=0)
   {
      try
      {
         selectImage = curView.createImage(width,height);   
         selectGraphics = selectImage.getGraphics();     
         if((curView.ioFlags & HyperConstants.IMAGE_FROM_JAR_ENA)!=0)
            selectImage = curView.getImageFromJar("sel_"+gobName );
         else
          selectImage =
         curView.getAppletContext().getImage(
          new URL(curView.getCodeBase(),"sel_"+gobName));
      }
      catch(MalformedURLException m2)
      {
         curView.errOut("addGob();"+curView.getCodeBase()+gobName);  
      }
   }
   widthBase  = width;
   heightBase = height;
           // compute ZBuffer
double tw  = 0.0;
double td  = 0.0;
double th  = 0.0; 
   td = (double)depth*(double)depth;
   tw = (double)widthBase/2.0;
   th = (double)Math.sqrt(
    (double)(tw*tw)+(double)td);
   hyp=th;
   wSin=(double)depth/th;
   tw = (double)heightBase/2.0;
   hSin=(double)depth/th;
   gobNumber = curView.addGob(this);
}
// @@@ 
Gob(HyperView tView,String tName,int sx,int sy,Spline tSpline,int tFlags)
{
   this(tView,tName,sx,sy,tFlags|GOB_SPLINE_ENA);
   depth = curView.depth;
   spline = tSpline;
   x = spline.x[0];
   y = spline.y[0];
   z = spline.z[0];
   spline.targetX = spline.x[1];
   spline.targetY = spline.y[1];
   spline.targetZ = spline.z[1];
      // Compute Z buffer data
   if((tSpline.flags & SplineConstants.SP_ZBUFFER_ENA)!=0)
   {
      gobZInit();
      gobFlags |= ZBUFFER_ENA;
   }
}

public void addSpline(Spline tSpline)
{

   this.spline = tSpline;
   gobFlags |= GOB_SPLINE_ENA;
   widthBase  = width;
   heightBase = height;
//System.out.println("WidthBase = "+widthBase);

   depth = curView.depth;
   if((tSpline.flags & SplineConstants.SP_ZBUFFER_ENA)!=0)
   {
      gobZInit();
      gobFlags |= ZBUFFER_ENA;
   }
}

public void zENA()
{
   gobFlags |= ZBUFFER_ENA;
}

public void zOff()
{
   gobFlags &= (~ZBUFFER_ENA);
}

// Todo 0as the GOb may not always be in the View, this needs to reflect this fact.

public boolean inView()
{
   if(this.x > 0 && this.x < curView.width)
   {
      if(this.y > 0 && this.y < curView.height)
         return true;
   } 
   return false;
}
// See last Todo note

public boolean onDisplay()
{
   if(x > 0 && x < curView.width)
   {
      if(this.y > 0 && this.y < curView.height)  
         this.gobFlags |= GOB_ON_DISPLAY;
   }
   gobFlags &= ~(GOB_ON_DISPLAY);
   return false;
}

public void enable()
{
   gobFlags |= GOB_ON_DISPLAY;
}

public void gobZInit()
{
double tw  = 0.0;
double td  = 0.0;
double th  = 0.0;
//System.out.println("GobZInit");
   if(depth == 0)
   {
      depth = (800 + 600)>>1;
   }
   td = (double)depth * (double)depth; // Square depth
   tw = (double)widthBase / 2.0;
        // Hypotenuse
   th = (double)Math.sqrt((double)(tw*tw)+(double)td);
   hyp=th;
   wSin = (double)depth/th;
   tw   = (double)heightBase/2.0;
   th   = Math.sqrt(tw*tw+td);
   hSin = (double)+depth/th;
   if(z < 0)
   {
      z = 0;
      height = heightBase;
      width  = widthBase;
   }
   else
   {
      width = (int)
       (widthBase*(double)wSin/(double)z+1);
      if(width < 1)
      {
         width = 2;
      }
      height = (int)
       (heightBase * (double)
      hSin/(double)z+1);
      if(height <2)
      {
         height = 2;
      }
   }
   return;
}

public Graphics getGraphics()
{
   return graphics;
}

public void onClick()
{
   curView.statusText("null onClick().gobNumber "+gobNumber);
}

public void remGob()
{
   
}

public void addGob(Gob tGob)
{
   tGob.destGob = this;
   if(totalAttached < maxAttached)
   {
      attachedGob[totalAttached] = tGob;
      tGob.attachedIndex = totalAttached;
      totalAttached++;
   }
   else
   {
//System.out.println("Reallocting!!!!!!");
      // Reallaocate and swaparoo.
      Gob tmpGob[] = new Gob[maxAttached+10];
      for(int t = 0;t<totalAttached;t++)
         tmpGob[t] = attachedGob[t];
      attachedGob = null;
      attachedGob = tmpGob;
      attachedGob[tGob.totalAttached] = this;
      totalAttached++; 
      maxAttached += 10;
   }
   gobFlags |= GOB_ATTACHED_ENA;
   //curView.hyperViewOut("Adding "+tGob.toString()+" To "+this.toString());
   tGob.gobFlags |= GOB_ONGOB;
}

public void disable()
{
   this.gobFlags &= (~GOB_ON_DISPLAY);
}

public void move(int tx,int ty)
{
   x  = tx;
   y  = ty;
   x2 = tx+width;
   y2 = ty+height;
}

public Image getImageFromJar(String fileName)
{
int length = 0;
   if( fileName == null )
       return null; 
   Image image = null; 
   try
   {        
      image = curView.getAppletContext().getImage( 
       getClass().getResource(fileName) );
   } 
   catch(Exception exc)
   { 
      curView.hyperViewOut( exc +" getting resource " +fileName ); 
      return null; 
   } 
   return image; 
} 

boolean isInView()
{
   if(x >0 && x <= (curView.x-width) && y > 0 && y <= (curView.height-height))
      return true;
   return false;
}

public void cleanUp()
{
 //  System.out.println("Gob() CleanUp()");
   if(gobPic != null)
   {
      gobPic.flush(); 
      graphics.dispose(); 
   }
   if(selectImage != null)
   {
      selectImage.flush();
      selectGraphics.dispose();
   }
   if(asynchImage1 != null)
   {
      asynchImage1.flush();    // For double buffered external input.
      asynchGraphics1.dispose();
   }
   if(asynchImage2 != null)
   {
      asynchImage2.flush();    // For double buffered external input.
      asynchGraphics2.dispose();
   }
   if(backGround != null)
   {
       backGround.flush();
   }   
/*
int totalAttached        = 0;
int maxAttached          = 1024;
int attachedIndex        = 0;
int curAttached          = 0;
*/
   
   destGob        = null;
   destGraphics   = null;
   gobName          = null;
   infoString       = null; 
//int currentZIndex       = 0;
   if(spline != null)
      spline.cleanUp();
   mX       = null;
   mX2      = null;
   mY       = null;
   mY2      = null;
   orbitGob = null;
}
 // This routine converts the Image into 
 // a Double linked list of Scanlines
 // This works, but I am going to change it to
 // use a 1 bitplane bitmask.
 //
public void initScanline(int tInt)
{
   if(scanLine != null)
      return;
boolean alphaState          = true;
int endPixel                = width-1;
int z                       = 0;
int gobPixel[]              = null; 
Image img                   = null;
int tempScanLinePointNumber = 0;
       // reduce indirection
Scanline tScanline = null;
Scanline lastScanline = null;
   scanLine       = new Scanline[height]; 
Scanline last              = null;
   collisionClassMask = tInt;
   gobPixel = new int[(width * height)];
   PixelGrabber pixelGrabber = new 
    PixelGrabber(gobPic, 0, 0, width, height, gobPixel, 0, width);
   try 
   {
	 pixelGrabber.grabPixels();
   } 
   catch(InterruptedException e) 
   {
      System.err.println("interrupted waiting for pixels!");
	return;
   }
   if((pixelGrabber.getStatus() & ImageObserver.ABORT) != 0) 
   {
      System.err.println("image fetch aborted or errored");
      return;
   }
   int tX      = 0;
   int tY      = 0;
   int tOffset = 0;
          // Compute initial state;
   System.out.println(
    "getOutline() "+(width * height)+" w="+width+" h="+height+" l="+gobPixel.length);
   try
   {
      alphaState   = ((gobPixel[tOffset] & 0x00ffffff)==0);
      for(tY=0;tY<height;tY++)
      {
         tScanline = null;
         // System.out.println("Row - "+tY);
         scanLine[tY] = last = tScanline = new Scanline();
         for(tX=0;tX<width;tX++)
         {
            if(alphaState)
            {
               System.out.print("0");
               
               if((gobPixel[tOffset] & 0x00ffffff)!=0)  //---**
               {
               //System.out.print("sl: "+x);
                  alphaState = false;
                  tScanline.solid = alphaState; 
                  tScanline.x = tX;
               }            
            }
            else
            {
               System.out.print("1");
               if((gobPixel[tOffset] & 0x00ffffff)==0) //---***
               {
                  alphaState     = true;
                  tScanline.solid = alphaState; 
                     //System.out.print("s2: "+x);
                     //System.out.println(","+x2);
                  tScanline.x2   = tX;
                  if((tScanline.x != 0) && (tScanline.x2 != 0))
                  {
                     last           = tScanline;
                     tScanline.next =  new Scanline();
                     tScanline      = tScanline.next;
                  }
               }
            }
            tOffset++;
         }
         if(!alphaState) 
         {      
            tScanline.x2 = (width-1);          
         }
         if((tScanline.x == 0) && (tScanline.x2 == 0))
         {
            last.next = null;
         }
         System.out.print("\r\n");
      }
   }
   catch(Exception any)
   {
      System.out.println("tX="+tX+" tY="+tY+" tO"+(tOffset+tX));
      any.printStackTrace();
   }
   for(int i = 0;i<height;i++)
   {
      //System.out.print(i+")");
      Scanline sc = scanLine[i];
      while(sc != null )
      {
         
         //System.out.print(" -><- "+sc.x+" "+sc.x2+",");
         sc = sc.next; 
      }
      System.out.print("\r\n"); 
   }
   //img = curView.createImage(new MemoryImageSource(width, height,gobPixel,0, width));
   // vp.graphics.drawImage(img,0,0,width,height,curView);
}


public void printScanLines()
{
   if(scanLine == null)
   {
      initScanline(0xffffffff);
   }
   String oString;
   String s1 = "1";
   String s2 = "0";
       //-------
   int tLen = scanLine.length;
   for(int z=0;z<tLen;z++)
   {
      if(scanLine[z].solid)
      {
         int scanLen = scanLine[z].x2 - scanLine[z].x;
         if(scanLine[z].solid)
         {
            oString = s1;
         }
         else
         {
            oString = s2;
         }
         for(int i=0;i<scanLen;i++)
         {
            System.out.print(oString);  
         }
      }
   }
}

public void collisionOn()
{
   gobFlags |= GOB_COLLISION_ENA;
}

public void collisionOff()
{
   gobFlags &= ~GOB_COLLISION_ENA;
}

public String toString()
{
String tString = "<GOB name = "+gobName+">";

   tString += "x="+x+" y="+y+"z="+z;

   if((gobFlags & GOB_ONGOB)!= 0)
   {
      tString += gobName+" (a)->"+destGob.gobNumber;
      tString += "\r\nflags1 "+Binary.toString(gobFlags);
      tString += "\r\nflags2 "+Binary.toString(gobFlags2);
   }
   else
   {
      if((gobFlags & HyperConstants.GOB_ATTACHED_ENA)!= 0)
         tString += "(A)->"+totalAttached;
      tString += "\r\nflags1 "+Binary.toString(gobFlags);
      tString += "\r\nflags2 "+Binary.toString(gobFlags2);
   }   
   tString += "\r\n</GOB>\r\n";
   return tString;
}
}

