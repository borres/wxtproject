using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Windows.Forms;

namespace pieces
{

    class brikke
    {
        #region init
        // attributter
        PointF[] polygon;
        Rectangle box;
        Point center;
        Brush HILITE_BRUSH = Brushes.Red;
        Brush NORMAL_BRUSH = Brushes.Blue;
        Brush m_brush;
        Control m_owner;
        Matrix m_Matrix=new Matrix();
        

        public brikke(Control owner,PointF[] p)
        {
            // polygon is closed
            polygon = p;
            m_brush = NORMAL_BRUSH;
            m_owner = owner;
            makeBox();
            makeCenter();
        }
        
        private void makeBox()
        {
            float minx = Int32.MaxValue;
            float miny = Int32.MaxValue;
            float maxx = Int32.MinValue;
            float maxy = Int32.MinValue;
            for (int ix = 0; ix < polygon.Length; ix++)
            {
                minx = Math.Min(minx, polygon[ix].X);
                miny = Math.Min(miny, polygon[ix].Y);
                maxx = Math.Max(maxx, polygon[ix].X);
                maxy = Math.Max(maxy, polygon[ix].Y);
            }
            box = new Rectangle((int)minx - 2, (int)miny - 2,
                (int)maxx - (int)minx + 4, (int)maxy - (int)miny + 4);
        }
        
        private void makeCenter()
        {
            float xsum=0;
            float ysum = 0;
            for (int ix = 0; ix < polygon.Length - 1; ix++)
            {
                xsum += polygon[ix].X-box.Left;
                ysum += polygon[ix].Y-box.Top;
            }
            center.X = Convert.ToInt32(box.Left+(xsum / (polygon.Length - 1)));
            center.Y = Convert.ToInt32(box.Top+(ysum / (polygon.Length - 1)));
        }
        #endregion init

#region invalreg
        public Region Reg
        {
           get {Region r=new Region(new Rectangle(box.X-2,box.Top-2,box.Width+4,box.Height+4)); 
               r.Transform(m_Matrix); 
               return r;}
        }
#endregion invalreg

        public void hiLite(Boolean on)
        {
            if (on)
                m_brush = HILITE_BRUSH;
            else
                m_brush = NORMAL_BRUSH;
        }
        #region action
        public void move(int dx, int dy)
        {
            m_Matrix.Translate(dx, dy,MatrixOrder.Append);
        }

        public void rotate(int x, int lastX, int y, int lastY)
        {
            Matrix m = m_Matrix.Clone();
            m.Invert();
            Point[] pts = new Point[2] { new Point(x, y), new Point(lastX, lastY) };
            m.TransformPoints(pts);
            // fixed anglesize
            float v = 2.0f;
            // direction based on quadrant and direction
            if (   ((pts[0].X > center.X)  && (pts[0].Y < pts[1].Y))
                || ((pts[0].X <= center.X) && (pts[0].Y >= pts[1].Y)))
                v = -v;            
            m_Matrix.RotateAt(v,center, MatrixOrder.Prepend);
        }

        public void Draw()
        {
            Graphics DC = m_owner.CreateGraphics();
            DC.ResetTransform();
            DC.MultiplyTransform(m_Matrix);
            
            DC.FillPolygon(m_brush, polygon);
            DC.DrawLines(new Pen(Brushes.Black), polygon);

            // mark center
            DC.DrawLine(new Pen(Brushes.Black), center.X-10,center.Y,center.X+10,center.Y);
            DC.DrawLine(new Pen(Brushes.Black), center.X,center.Y-10,center.X,center.Y+10);

            // mark box
            DC.DrawRectangle(new Pen(Brushes.DarkGray), box);
            DC.ResetTransform();
        }
        #endregion action

        public Boolean polygonHit(Point p)
        {
            Matrix m = m_Matrix.Clone();
            m.Invert();
            Point[] pts = new Point[1] { new Point(p.X, p.Y) };
            m.TransformPoints(pts);
            return calculations.InsidePolygon(polygon, pts[0].X,pts[0].Y);
        }

        public Boolean WillRotate(Point p)
        {
            Matrix m = m_Matrix.Clone();
            m.Invert();
            Point[] pts = new Point[1] { new Point(p.X, p.Y) };
            m.TransformPoints(pts);
            
            // reasonable distance from center ?
            double dist = Math.Sqrt(
                (pts[0].X - center.X) * (pts[0].X - center.X) + 
                (pts[0].Y - center.Y) * (pts[0].Y - center.Y));
            
            return dist > Math.Min(box.Width, box.Height) / 3;
        }

    }
}
