package geoptimize.swing;

import java.util.LinkedList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import geoptimize.ServiceNode;
import java.awt.BorderLayout;
import javax.swing.JProgressBar;

/***
 * Canvas Frame that draws the image member variable.
 * Also draws a list of nodes to show the current gbest.
 * 
 * @author Callan
 *
 */
public class SimulationCanvas extends JComponent {
	private static final long serialVersionUID = -2480881754680088455L;
	
	//TODO: will want to load this list from the current PSOSimulation's gbest
	private LinkedList<ServiceNode> nodes;
	
	private MainWindow parent;
	
	private Image image;
	private float magnification = 1;
	private float minMagnification = 0.01f;
	private float maxMagnification = 10f;
	private float magnificationSensitivity = 1.2f;
	private Rectangle region = new Rectangle();
	
	private Point mousePoint = new Point();
	
	public void setMagnification(float mag) {
		if(magnification != mag && image != null) {
			magnification = mag;
			Dimension d = this.getSize();
			d.width = (int)(image.getWidth(null) * magnification);
			d.height = (int)(image.getHeight(null) * magnification);
			this.setPreferredSize(d);
			this.revalidate();
			this.repaint();
		}
	}
	
	public void zoom(int zoom) {
		float mag = geoptimize.helper.Math.clamp(minMagnification, maxMagnification, (float)(magnification*Math.pow(magnificationSensitivity, zoom)));
		setMagnification(mag);
	}
	
	public void zoomIn() { 
		setMagnification((magnification < maxMagnification) ? magnification*magnificationSensitivity : magnification);
	} 
	
	public void zoomOut() { 
		setMagnification((magnification > minMagnification) ? magnification/magnificationSensitivity : magnification);
	}
	
	public void setBackgroundImage(Image image) {
		this.image = image;
		setMagnification(1);
		
		//for some reason this isn't triggering a repaint
		this.revalidate();
		this.repaint();
	}
	

	public void setRegion(Rectangle rgn) {
		// TODO Auto-generated method stub
		region = rgn;
		this.revalidate();
		this.repaint();
	}
	
	public SimulationCanvas(MainWindow window) {
		parent = window;
		setLayout(new BorderLayout(0, 0));
		
		
		//TODO: load nodes from the running simulation
		nodes = new LinkedList<ServiceNode>();
		nodes.add(new ServiceNode(2200, 2400, 80));
		nodes.add(new ServiceNode(2400, 2600, 80));
		nodes.add(new ServiceNode(2400, 2800, 80));
		
		this.addMouseMotionListener(new MouseCoordListener(window));
		
		this.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				SimulationCanvas.this.zoom(e.getWheelRotation());
			}
		});
	}
	
	@Override
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		
		drawBackground(graphics);
		drawRegion(graphics);
		drawNodes(graphics);
	}
	
	private void drawNodes(Graphics graphics) {
		
		Color fillColor = new Color(0f, 1f, 0.5f, 0.6f);
		
		graphics.setColor(fillColor);
		for(ServiceNode node : nodes) {
			Point p = node.getPosition();
			float r = node.getRange();
			
			graphics.fillOval(
					(int)(p.x * magnification),
					(int)(p.y * magnification),
					(int)(r * magnification),
					(int)(r * magnification));
		}
	}
	
	private void drawRegion(Graphics graphics) {
		graphics.setColor(Color.RED);
		graphics.drawRect(
				(int)(region.x * magnification), 
				(int)(region.y * magnification),
				(int)(region.width * magnification),
				(int)(region.height * magnification));
	}
	
	private void drawBackground(Graphics graphics) {
		if (image != null) {
			Dimension size = this.getSize();
			int width = (int)(image.getWidth(null) * magnification);
			int height = (int)(image.getHeight(null) * magnification);
			
			graphics.drawImage(image, 0, 0, width, height, null);
		}
	}
	
	private class MouseCoordListener implements MouseMotionListener {
		MainWindow window;
		public MouseCoordListener(MainWindow window) {
			this.window = window;
		}
		
		@Override
		public void mouseDragged(MouseEvent e) { }

		@Override
		public void mouseMoved(MouseEvent e) {				
			mousePoint.setLocation(e.getX() / magnification, e.getY() / magnification);
			window.lblCursorcoords.setText("[ " + mousePoint.x + " , " + mousePoint.y + " ]");
		}
	}
}
