import java.awt.Polygon;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Component;
import java.awt.event.*;
import java.applet.Applet;
import java.util.Vector;
import java.awt.geom.AffineTransform;
import java.awt.Graphics2D;

public class a3dwalker extends Applet implements Runnable
{
    // the 3D object
    gob hb=null;
    // a thread that we can start and stop by mouseclick
    Thread painter=null;
    // running ?
    boolean halted=false;

    // a walking man
    theMan man=null;

    //--------- make the man walk -------------
    float dvt=0.08f;
    //------------------------------------------

    //---------- to make the demo spin ----------
    float vx=0.0f;
    float vy=0.0f;
    float vz=0.0f;

    float dvx=0.02f;
    float dvy=0.01f;
    float dvz=0.02f;

    int lastx=0;
    int lasty=0;
    //--------------------------------------

    //Construct the applet
    public a3dwalker()
    {
    }

    //Initialize the applet
    public void init()
    {
        try { jbInit();	}
        catch(Exception e){e.printStackTrace();	}
    }

    //Component initialization
    private void jbInit() throws Exception
    {

        this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter()
        {

        public void mouseDragged(MouseEvent e)
        {
        this_mouseDragged(e);
        }
        });
        this.addMouseListener(new java.awt.event.MouseAdapter()
        {
        public void mouseClicked(MouseEvent e)
        {
        this_mouseClicked(e);
        }

        public void mousePressed(MouseEvent e)
        {
        this_mousePressed(e);
        }

        public void mouseReleased(MouseEvent e)
        {
        this_mouseReleased(e);
        }
        });

        this.setBackground(Color.white);
        this.setLayout(null);


        hb=new gob(this);
        man=new theMan();
    }

    //Start the applet
    public void start()
    {
        painter=new Thread(this);
        halted=false;
        painter.start();
    }

    //Stop the applet
    public void stop()
    {
        if(painter!=null)
            halted=true;
    }

    //Destroy the applet
    public void destroy()
    {
    }

    //Get Applet information
    public String getAppletInfo()
    {
        return "Name: App3D," +
        "Author: Børre Stenseth";
    }


    public void paint(Graphics g)
    {
        //-----------------
        // a demo with some spinning boxes
        hb.clearAll();
        drawAMan();
        hb.flush(g);
    }

    public void run()
    {
        while (!halted)
        {
            try
            {
                vx+=dvx;
                vx+=dvy;
                vz+=dvz;
                repaint();
                Thread.sleep(20);
            }
            catch (InterruptedException e)
            {
                stop();
            }
        }
    }

    // override update to avois flickering
    public void update(Graphics g)
    {
        paint(g);
    }

    void drawAMan()
    {
        hb.setBackground(new Color(1.0f,1.0f,1.0f));
        hb.setOrtho(-1.5f,-1.5f,3.0f,3.0f);
        hb.setViewport(0,0,200,200);

        hb.rotateY(0.34906f);//(float)Math.toRadians(20.0));
        hb.rotateX(1.57079f);//(float)Math.toRadians(90.0));
        hb.translate(0.0f,-2.0f,0.0f);

        hb.rotateZ(vz);
        hb.rotateX(vx);
        hb.rotateY(vy);

        man.walkmove(dvt);
        hb.setColor(new Color(1.0f,1.0f,0.0f));
        man.drawMan(hb,gob.FILL);
    }
    //----------------------------------------------
    // mousing around

    void this_mouseClicked(MouseEvent e)
    {
        if((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
        {
            if(halted)
                start();
            else
                stop();
        }
    }

    void this_mousePressed(MouseEvent e)
    {
        if((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK)
        {
            lastx=e.getX();
            lasty=e.getY();

            halted=true;
            stop();
        }
    }

    void this_mouseReleased(MouseEvent e)
    {
        if((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK)
        {
            int x=e.getX();
            int y=e.getY();
            halted=false;
            start();
        }
    }

    void this_mouseDragged(MouseEvent e)
    {
        if((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK)
        {
            int x=e.getX();
            int y=e.getY();
            dvx=5.0f*(x-lastx)/this.getSize().width;
            dvy=5.0f*(y-lasty)/this.getSize().height;
            dvz=dvx-dvy;
            vx+=dvx;
            vx+=dvy;
            vz+=dvz;

            repaint();

            lastx=x;
            lasty=y;
        }
    }
    
    //--------------------------------------------------
//  start of inner classes: gob, p3D, matrix, polygon 
public class gob extends Object
{
    //----------------------------------------------------------------
    // defining the minimalistic graphical interface:
    // clearAll, drawBox, flush, pBegin, pEnd, popMatrix, pushMatrix,
    // rotateX, rotateY, rotateZ, scale, translate,
    // setBackground, setColor, setIdentityMatrix,
    // setOrtho, setperspective, setViewport, vertex
    //-----------------------------------------------------------------

    //-----------------------------------------------------------------
    // intenal classes : p3D, matrix, polygon defined at end of file
    //-----------------------------------------------------------------
    // fill or outline polygons
    public final static int FILL=0;
    public final static int FRAME=1;
    // the list of polygons to draw
    Vector drawList=new Vector(20,6);
    // matrix stack, for push and pop
    Vector matrixStack=new Vector(5,5);
    // current model(view) matrix
    matrix curMatrix=null;
    // current polygon, while we are adding vertexes
    polygon curPoly=null;
    // current drawing color
    Color curColor=null;
    // Background color, last before flush yields
    Color BKColor=null;
    // buffer to draw in to avoid flicker
    Image Buffer;
    // viewport
    float Vxl,Vyt,Vw,Vh;
    // window, model coordinates
    float Wxl,Wyt,Ww,Wh;
    // perspective angles, in degrees
    float Pvx,Pvy;
    // use perspective projection or not
    boolean isPerspective;
    // the actual component that gob will work on
    Component theComp;
    // possible background image
    Image	 theImage=null;

    public gob(Component comp)
	{
        Buffer=comp.createImage(comp.getSize().width,comp.getSize().height);
        setViewport(0,0,comp.getSize().width,comp.getSize().height);
        setOrtho(0,0,comp.getSize().width,comp.getSize().height);
        theComp=comp;
        curColor=new Color(0.9f,0.9f,0.9f);
        BKColor=new Color(1.0f,1.0f,1.0f);
	}

    public void clearAll()
    {
        drawList.removeAllElements();
        curPoly=null;
        curMatrix=new matrix();
        matrixStack.removeAllElements();
    }


	public void setIdentityMatrix() 
    {
        curMatrix=new matrix();
    }

    public void pushMatrix()
    {
        if(curMatrix==null)
            curMatrix=new matrix();
        matrixStack.addElement(curMatrix.copyMatrix(curMatrix));
    }

    public void popMatrix()
    {
        if(matrixStack.size()<1)
        {
            //underflow
            curMatrix=new matrix();
            System.out.println("Matrix stack undeflow, too many pops");
            return;
        }
        curMatrix=(matrix)matrixStack.lastElement();
        matrixStack.removeElement(matrixStack.lastElement());
    }

    public void translate(float dx,float dy,float dz)
    {
        float[] values={
        1.0f,0.0f,0.0f,dx,
        0.0f,1.0f,0.0f,dy,
        0.0f,0.0f,1.0f,dz,
        0.0f,0.0f,0.0f,1.0f};
        curMatrix.postMult(new matrix(values));
    }

    public void scale(float sx,float sy,float sz)
    {
        float[] values={
        sx,0.0f,0.0f,0.0f,
        0.0f,sy,0.0f,0.0f,
        0.0f,0.0f,sz,0.0f,
        0.0f,0.0f,0.0f,1.0f};
        curMatrix.postMult(new matrix(values));
    }

    public void rotateX(float v)
    {
        float[] values={
        1.0f,0.0f,0.0f,0.0f,
        0.0f,(float)Math.cos(v),(float)-Math.sin(v),0.0f,
        0.0f,(float)Math.sin(v),(float)Math.cos(v),0.0f,
        0.0f,0.0f,0.0f,1.0f};
        curMatrix.postMult(new matrix(values));
    }

    public void rotateY(float v)
    {
        float[] values={
        (float)Math.cos(v),0.0f,(float)Math.sin(v),0.0f,
        0.0f,1.0f,0.0f,0.0f,
        (float)-Math.sin(v),0.0f,(float)Math.cos(v),0.0f,
        0.0f,0.0f,0.0f,1.0f};
        curMatrix.postMult(new matrix(values));
    }

    public void rotateZ(float v)
    {
        float[] values={
        (float)Math.cos(v),(float)-Math.sin(v),0.0f,0.0f,
        (float)Math.sin(v),(float)Math.cos(v),0.0f,0.0f,
        0.0f,0.0f,1.0f,0.0f,
        0.0f,0.0f,0.0f,1.0f};
        curMatrix.postMult(new matrix(values));
    }

    public void pBegin(int mode)
    {
        if(mode==FILL)
            curPoly=new polygon(curColor,mode);
        else
            curPoly=new polygon(curColor,FRAME);
    }

    public void vertex(float nx, float ny, float nz)
    {
        p3D npnt=curMatrix.transform(new p3D(nx,ny,nz));
        curPoly.appendVertex(npnt);
    }

    public void pEnd()
    {
        // calculate the angle for lighting
        curPoly.calculateAngle();
        // put it into the the drawList according to depth
        // lowest minZ comes first in list
        for(int ix=0;ix<drawList.size();ix++)
        if(((polygon)drawList.elementAt(ix)).getMinZ()>curPoly.getMinZ())
        {
            drawList.insertElementAt(curPoly,ix);
            return;
        }
        drawList.addElement(curPoly);
    }

    public void flush(Graphics g)
    {
        // project the polygons
        for(int ix=0;ix<drawList.size();ix++)
        {
        // project it
        if(isPerspective)
            ((polygon)drawList.elementAt(ix)).perspectiveProject(Pvx,Pvy, Wxl,Wyt,Ww,Wh, Vxl,Vyt,Vw,Vh);
        else
            ((polygon)drawList.elementAt(ix)).paralellProject(Wxl,Wyt,Ww,Wh, Vxl,Vyt,Vw,Vh);
        }

        // draw drawList from bottom on an offscreen buffer
        Graphics tmpg=Buffer.getGraphics();
        // clear only the specified viewport
        // tmpg.clearRect(Math.round(Vxl),Math.round(Vyt),Math.round(Vw),Math.round(Vh));
        tmpg.setColor(BKColor);
        // fills with color or set out image
        tmpg.fillRect(Math.round(Vxl),Math.round(Vyt),Math.round(Vw),Math.round(Vh));
        if(theImage!=null)
            tmpg.drawImage(theImage,Math.round(Vxl),Math.round(Vyt),BKColor, null);


        // draw it
        for(int ix=0;ix<drawList.size();ix++)
            ((polygon)drawList.elementAt(ix)).render(tmpg);

        // copy to viewport
        g.clipRect(Math.round(Vxl),Math.round(Vyt),Math.round(Vw),Math.round(Vh));
        g.drawImage(Buffer,0,0,null);
    }


    public void setColor(Color c)
    {
        curColor=c;
    }

    public void setBackground(Color c)
    {
        BKColor=c;
        //theImage=null;
    }


    public void setImage(Image im)
    {
        if(im==null)
         theImage=null;
        else
         theImage=im;
    }

    public void setViewport(float xl,float yt,float w, float h)
    {
        Vxl=xl;Vyt=yt;Vw=w;Vh=h;
    }

    public void setOrtho(float xl,float yt,float w, float h)
    {
        Wxl=xl;Wyt=yt;Ww=w;Wh=h;
        isPerspective=false;
    }

    public void setPerspective(float vx,float vy)
    {
        if(vx < 0.5)
            Pvx=0.5f;
        else if(vx > 89.5)
          Pvx=89.5f;
        else
         Pvx=vx;

        if(vy<0.5)
            Pvy=0.5f;
        else if(vy > 89.5)
          Pvy=89.5f;
        else
            Pvy=vy;
        isPerspective=true;
    }

    //----------------------------------------------
    // draw a box, is handy since this is about all we
    // would be tempted  to draw in these surroundings
    public void drawBox(float dx,float dy, float dz,int mode)
    {
    // bottom
    float x=dx/2.0f;
    float y=dy/2.0f;
    float z=dz/2.0f;
    pBegin(mode);
        vertex(-x,y,-z); vertex(-x,-y,-z); vertex(x,-y,-z);	vertex(x,y,-z);
    pEnd();
    // top
    pBegin(mode);
        vertex(-x,y,z);	vertex(x,y,z); vertex(x,-y,z); vertex(-x,-y,z);
    pEnd();
    // front
    pBegin(mode);
        vertex(-x,y,-z); vertex(x,y,-z); vertex(x,y,z);	vertex(-x,y,z);
    pEnd();
    // back
    pBegin(mode);
        vertex(-x,-y,-z); vertex(-x,-y,z); vertex(x,-y,z); vertex(x,-y,-z);
    pEnd();
    // left
    pBegin(mode);
        vertex(-x,y,-z); vertex(-x,y,z); vertex(-x,-y,z); vertex(-x,-y,-z);
    pEnd();
    // right
    pBegin(mode);
        vertex(x,-y,-z); vertex(x,-y,z); vertex(x,y,z); vertex(x,y,-z);
    pEnd();
    }
    }
//-----------------  end of gob


//------------------ start of p3D
    class p3D extends Object
    {
        float x,y,z;
        public p3D(float nx, float ny, float nz)
        {
        x=nx; y=ny; z=nz;
        }
    }
//-----------------  end of p3D


//------------------ start of matrix

    class matrix extends Object
    {
        float[][]  m=null;

        public matrix()
        {
        m=new float[][]{
        {1.0f,0.0f,0.0f,0.0f},
        {0.0f,1.0f,0.0f,0.0f},
        {0.0f,0.0f,1.0f,0.0f},
        {0.0f,0.0f,0.0f,1.0f}};
    }

    public matrix(float[] values)
    {
        m=new float[][]{
        {1.0f,0.0f,0.0f,0.0f},
        {0.0f,1.0f,0.0f,0.0f},
        {0.0f,0.0f,1.0f,0.0f},
        {0.0f,0.0f,0.0f,1.0f}};

        if(values.length!=16)
            return;
        m[0][0]=values[0];  m[0][1]=values[1];  m[0][2]=values[2];  m[0][3]=values[3];
        m[1][0]=values[4];  m[1][1]=values[5];  m[1][2]=values[6];  m[1][3]=values[7];
        m[2][0]=values[8];  m[2][1]=values[9];  m[2][2]=values[10]; m[2][3]=values[11];
        m[3][0]=values[12]; m[3][1]=values[13]; m[3][2]=values[14]; m[3][3]=values[15];
    }

    matrix copyMatrix(matrix sm)
    {
        float v[]={
        sm.m[0][0],sm.m[0][1],sm.m[0][2],sm.m[0][3],
        sm.m[1][0],sm.m[1][1],sm.m[1][2],sm.m[1][3],
        sm.m[2][0],sm.m[2][1],sm.m[2][2],sm.m[2][3],
        sm.m[3][0],sm.m[3][1],sm.m[3][2],sm.m[3][3]};
        return new matrix(v);
    }

    p3D transform(p3D p)
    {
        return new p3D(
        p.x*m[0][0]+p.y*m[0][1]+p.z*m[0][2]+m[0][3],
        p.x*m[1][0]+p.y*m[1][1]+p.z*m[1][2]+m[1][3],
        p.x*m[2][0]+p.y*m[2][1]+p.z*m[2][2]+m[2][3]);
    }

    void postMult(matrix tm)
    {
        // do: m = m X tm.m
        float [][] oldm={
        {m[0][0],m[0][1],m[0][2],m[0][3]},
        {m[1][0],m[1][1],m[1][2],m[1][3]},
        {m[2][0],m[2][1],m[2][2],m[2][3]},
        {m[3][0],m[3][1],m[3][2],m[3][3]}};
        
        for(int r=0;r<4;r++)
        for(int k=0;k<4;k++)
        m[r][k]=oldm[r][0]*tm.m[0][k]+
        oldm[r][1]*tm.m[1][k]+
        oldm[r][2]*tm.m[2][k]+
        oldm[r][3]*tm.m[3][k];
    }
}
//-----------------  end of matrix


//------------------ start of polygon

class polygon extends Object
{
    Vector p=new Vector(20,6);
    float minZ=100000.0f; // higher than any point
    Color pcolor=null;
    float cosangle=0.0f;
    Polygon pol=null;
    int pmode;

    public polygon(Color c,int mode)
    {
        pmode=mode;
        pcolor=c;
    }

    void setColor(float r,float g,float b)  
    {
        pcolor=new Color(r,g,b);
    }

    float getMinZ()  
    {
        return minZ;
    }

    int getPCount()  
    {
        return p.size();
    }

    p3D getPAt(int index)
    {
        if(index>=p.size() || index<0)
            return new p3D(0.0f,0.0f,0.0f);
        return (p3D)p.elementAt(index);
    }

    void setPAt(p3D np,int index)
    {
        if(index>=p.size() || index<0)
            return;
        ((p3D)p.elementAt(index)).x=np.x;
        ((p3D)p.elementAt(index)).y=np.y;
        ((p3D)p.elementAt(index)).z=np.z;
    }

    void appendVertex(float x,float y, float z)  
    {
        appendVertex(new p3D(x,y,z));
    }

    void appendVertex(p3D pnt)
    {
        if(pnt.z<minZ)
        minZ=pnt.z;
        p.addElement(pnt);
    }

    void calculateAngle()
    {
        // calculate normal and angle twds z-axis
        if(p.size()<3)
        {
            cosangle=1.0f;
            return;
        }
        // normal,normalV, from triangle: p0 - p1 - p2
        // c=(a2b3-a3b2, a3b1-a1b3, a1b2-a2b1) 
        p3D p0=(p3D)p.elementAt(0);
        p3D p1=(p3D)p.elementAt(1);
        p3D p2=(p3D)p.elementAt(2);

        p3D normalV=new p3D((p2.y-p1.y)*(p0.z-p1.z)-(p2.z-p1.z)*(p0.y-p1.y),
        (p2.z-p1.z)*(p0.x-p1.x)-(p2.x-p1.x)*(p0.z-p1.z),
        (p2.x-p1.x)*(p0.y-p1.y)-(p2.y-p1.y)*(p0.x-p1.x));

        float len=(float)Math.sqrt(normalV.x*normalV.x+normalV.y*normalV.y+normalV.z*normalV.z);
        // normalize. need only z-component
        // dont care about direction
        // big cosangle -> light face, since light is -z
        cosangle=Math.abs(normalV.z/len);
    }

    //public void paralellProject(float xfactor,float yfactor)
    public void paralellProject(
        float Wxl, float Wyt, float Ww, float Wh,
        float Vxl, float Vyt, float Vw, float Vh)
    {
        if(p.size()<2)
            return;
        pol=new Polygon();
        float xfactor=Vw/Ww;
        float yfactor=Vh/Wh;

        for(int ix=0;ix<p.size();ix++)
        pol.addPoint(	
            Math.round( (((p3D)p.elementAt(ix)).x-Wxl)*xfactor+Vxl),
            Math.round( (((p3D)p.elementAt(ix)).y-Wyt)*yfactor+Vyt) );
    }

    public void perspectiveProject(
        float vx,float vy,
        float Wxl,float Wyt, float Ww, float Wh,
        float Vxl, float Vyt, float Vw, float Vh)
    {
        // W is not used, assumes Rectangle(-0.5,-0.5,1,1)
        if(p.size()<2)
            return;
        pol=new Polygon();
        float xf=(float)(0.5f/Math.tan(Math.toRadians(vx)));
        float yf=(float)(0.5f/Math.tan(Math.toRadians(vy)));

        for(int ix=0;ix<p.size();ix++)
        {
            float x=((p3D)p.elementAt(ix)).x/Math.abs(((p3D)p.elementAt(ix)).z)*xf;
            float y=((p3D)p.elementAt(ix)).y/Math.abs(((p3D)p.elementAt(ix)).z)*xf;
            pol.addPoint(Math.round( (x+0.5f)*Vw+Vxl ),Math.round( (y+0.5f)*Vh+Vyt )
        );
        }
    }

    public void render(Graphics g)
    {
        if(pol==null)
            return;

        if(pmode==gob.FILL)
        {
             Color c=new Color(
                (pcolor.getRed()*(cosangle/255.0f)),
                (pcolor.getGreen()*(cosangle/255.0f)),
                (pcolor.getBlue()*(cosangle)/255.0f));
            g.setColor(c);
            g.fillPolygon(pol);
        }
        else
        {
            g.setColor(pcolor);
            g.drawPolygon(pol);
        }
    }
}
//------------- end of polygon
// -----------------------------
// describing a simple robot:
// origo in middle of torso
// head up in z, facing x
//-----------------------------

//------------------------------
// the robot can only do a silly run,
// see method: walkmove
// if you want other functionality
// you must write other methods which
// updates the robots angles
//------------------------------

public class theMan
{
    // dimensions: x,y,z
    final float torso[]=   {0.2f,	0.4f,	0.6f};
    final float head[]=    {0.1f,	0.2f,	0.2f};
    final float overarm[]=	{0.1f,	0.1f,	0.25f};
    final float underarm[]={0.1f,	0.1f,	0.30f};
    final float overleg[]=	{0.1f,	0.1f,	0.3f};
    final float underleg[]={0.1f,	0.1f,	0.35f};

    // angles: around x,y,z
    float v_head[]=        {0.0f,0.0f,0.0f};
    float left_shoulder[]=	{0.0f,0.0f,0.0f};
    float left_elboe[]=	   {0.0f,0.0f,0.0f};
    float right_shoulder[]={0.0f,0.0f,0.0f};
    float right_elboe[]=   {0.0f,0.0f,0.0f};
    float left_hip[]=      {0.0f,0.0f,0.0f};
    float left_knee[]=     {0.0f,0.0f,0.0f};
    float right_hip[]=     {0.0f,0.0f,0.0f};
    float right_knee[]=    {0.0f,0.0f,0.0f};

    // joint space
    final float joint=0.05f;

    //-----------------------------------------
    // describing the walk/run

    // state in movement, argument to sinus function
    float state=0.0f;

    // max and min hip angles, refers to rotateY
    final float minhip=-0.52359f;//(float)Math.toRadians(-30.0f);
    final float maxhip=1.22173f;//(float)Math.toRadians(70.0f);

    // max and min knee angles, refers to rotateY
    final float minknee=0.34906f;//(float)Math.toRadians(20.0f);
    final float maxknee=1.57079f;//(float)Math.toRadians(90.0f);

    // max and min shoulder angles, refers to rotateY
    final float minshoulder=-1.04719f;//(float)Math.toRadians(-60.0f);
    final float maxshoulder=1.04719f;//((float)Math.toRadians(60.0f);

    // max and min elboe angles, refers to rotateY
    final float minelboe=0.174532f;//(float)Math.toRadians(10.0f);
    final float maxelboe=1.57079f;//(float)Math.toRadians(90.0f);

    public theMan()
    {
    }

    public void walkmove(float incr)
    {
        // walking according to a sinus description
        // inr is an increment on a sinus function
        // increments state
        state+=incr;
        float sx=(float)Math.sin(state);
        if(sx>=0.0f)
        {
            // calculate left arm - forward
            left_shoulder[1]=-maxshoulder*sx;
            left_elboe[1]=-maxelboe*sx;
            // calculate right arm - backward
            right_shoulder[1]=-minshoulder*sx;
            right_elboe[1]=-minelboe*sx;
            // calculate right leg - forward
            right_hip[1]=-maxhip*sx;
            right_knee[1]=maxknee*sx;
            // calculate left leg - backward
            left_hip[1]=-minhip*sx;
            left_knee[1]=minknee*sx;
        }
        else
        {
            // calculate left arm - backward
            left_shoulder[1]=minshoulder*sx;
            left_elboe[1]=minelboe*sx;
            // calculate right arm - forward
            right_shoulder[1]=maxshoulder*sx;
            right_elboe[1]=maxelboe*sx;
            // calculate right leg - backward
            right_hip[1]=minhip*sx;
            right_knee[1]=-minknee*sx;
            // calculate left leg - forward
            left_hip[1]=maxhip*sx;
            left_knee[1]=-maxknee*sx;

        }
    }

    public void drawMan(gob gb,int mode)
    {
        // origo in middle of torso
        gb.drawBox(torso[0],torso[1],torso[2],mode);
        gb.pushMatrix();
        // head
        gb.translate(0.0f,0.0f,torso[2]/2.0f+joint/2.0f);
        gb.rotateX(v_head[0]);
        gb.rotateY(v_head[1]);
        gb.rotateZ(v_head[2]);
        gb.translate(0.0f,0.0f,head[2]/2.0f);
        gb.drawBox(head[0],head[1],head[2],mode);
        gb.popMatrix();

        gb.pushMatrix();
        // left arm
        gb.translate(0.0f,torso[1]/2.0f+joint/2.0f+overarm[1]/2.0f,torso[2]/2.0f);
        gb.rotateX(left_shoulder[0]);
        gb.rotateY(left_shoulder[1]);
        gb.rotateZ(left_shoulder[2]);
        gb.translate(0.0f,0.0f,-overarm[2]/2.0f);
        gb.drawBox(overarm[0],overarm[1],overarm[2],mode);
        //---------------
        gb.translate(0.0f,0.0f,-overarm[2]/2.0f-joint/2.0f);
        gb.rotateX(left_elboe[0]);
        gb.rotateY(left_elboe[1]);
        gb.rotateZ(left_elboe[2]);
        gb.translate(0.0f,0.0f,-underarm[2]/2.0f-joint/2.0f);
        gb.drawBox(underarm[0],underarm[1],underarm[2],mode);
        gb.popMatrix();

        gb.pushMatrix();
        // right arm
        gb.translate(0.0f,-torso[1]/2.0f-joint/2.0f-overarm[1]/2.0f,torso[2]/2.0f);
        gb.rotateX(right_shoulder[0]);
        gb.rotateY(right_shoulder[1]);
        gb.rotateZ(right_shoulder[2]);
        gb.translate(0.0f,0.0f,-overarm[2]/2.0f);
        gb.drawBox(overarm[0],overarm[1],overarm[2],mode);
        //---------------
        gb.translate(0.0f,0.0f,-overarm[2]/2.0f-joint/2.0f);
        gb.rotateX(right_elboe[0]);
        gb.rotateY(right_elboe[1]);
        gb.rotateZ(right_elboe[2]);
        gb.translate(0.0f,0.0f,-underarm[2]/2.0f-joint/2.0f);
        gb.drawBox(underarm[0],underarm[1],underarm[2],mode);
        gb.popMatrix();

        gb.pushMatrix();
        // left leg
        gb.translate(0.0f,torso[1]/2.0f-overleg[1]/2.0f,-torso[2]/2.0f-joint/2.0f);
        gb.rotateX(left_hip[0]);
        gb.rotateY(left_hip[1]);
        gb.rotateZ(left_hip[2]);
        gb.translate(0.0f,0.0f,-overleg[2]/2.0f);
        gb.drawBox(overleg[0],overleg[1],overleg[2],mode);
        //---------------
        gb.translate(0.0f,0.0f,-overleg[2]/2.0f-joint/2.0f);
        gb.rotateX(left_knee[0]);
        gb.rotateY(left_knee[1]);
        gb.rotateZ(left_knee[2]);
        gb.translate(0.0f,0.0f,-underleg[2]/2.0f-joint/2.0f);
        gb.drawBox(underleg[0],underleg[1],underleg[2],mode);
        gb.popMatrix();

        gb.pushMatrix();
        // right leg
        gb.translate(0.0f,-torso[1]/2.0f+overleg[1]/2.0f,-torso[2]/2.0f-joint/2.0f);
        gb.rotateX(right_hip[0]);
        gb.rotateY(right_hip[1]);
        gb.rotateZ(right_hip[2]);
        gb.translate(0.0f,0.0f,-overleg[2]/2.0f);
        gb.drawBox(overleg[0],overleg[1],overleg[2],mode);
        //---------------
        gb.translate(0.0f,0.0f,-overleg[2]/2.0f-joint/2.0f);
        gb.rotateX(right_knee[0]);
        gb.rotateY(right_knee[1]);
        gb.rotateZ(right_knee[2]);
        gb.translate(0.0f,0.0f,-underleg[2]/2.0f-joint/2.0f);
        gb.drawBox(underleg[0],underleg[1],underleg[2],mode);
        gb.popMatrix();
    }
} 
// -------------- end of inner classes

}

