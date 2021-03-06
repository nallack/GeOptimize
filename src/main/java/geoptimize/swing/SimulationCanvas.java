package geoptimize.swing;

import java.util.LinkedList;
import java.util.List;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MouseInfo;
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
import geoptimize.pso.PSOParticle;
import geoptimize.pso.PSOSimulation;
import geoptimize.pso.PSOSolution;

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
	private LinkedList<ServiceNode> dummyNodes;
	private PSOSimulation simulation;

	
	private MainWindow parent;
	
	private boolean showSwarm = true;
	private boolean showGBest = true;
	private boolean alternativeColors = false;
	private boolean particleNumbering = false;
	
	private Image image;
	private float magnification = 1;
	private float minMagnification = 0.01f;
	private float maxMagnification = 10f;
	private float magnificationSensitivity = 1.2f;
	private Rectangle region = new Rectangle();
	
	private Point cursorMapLocation = new Point();
	private Point cursorLocation = new Point();
	
	private Color textColor = Color.RED;
	private Color swarmColor = new Color(0f, 1f, 0.5f, 0.6f);
	private Color gbestColor = new Color(0f, 0.2f, 1.0f, 0.6f);
	
	private Color[] colors = new Color[] {
		new Color(0f, 1f, 0.5f, 0.6f),
		new Color(0f, 1f, 0.8f, 0.6f),
		new Color(0.9f, 0.1f, 0.8f, 0.6f),
		new Color(0.9f, 0.9f, 0.1f, 0.6f),
		new Color(0.8f, 0.4f, 0.0f, 0.6f)
	};

	
	
	public void setMagnification(float mag) {
		if(magnification != mag && image != null) {
			magnification = mag;
			Dimension d = this.getSize();
			d.width = (int)(image.getWidth(null) * magnification);
			d.height = (int)(image.getHeight(null) * magnification);
			
			updateCursor();
			
			this.setPreferredSize(d);
			this.revalidate();
			this.repaint();
			
			//TODO: want to zoom in on current scroll position
			//Need the repainted scrollbarsize...
			//int width = parent.simulationScrollPane.getHorizontalScrollBar().getMaximum();
			//int height = parent.simulationScrollPane.getVerticalScrollBar().getMaximum();
			//parent.simulationScrollPane.getHorizontalScrollBar().setValue(width/2);
			//parent.simulationScrollPane.getHorizontalScrollBar().setValue(height/2);
			
			updateCursor();
		}
	}
	
	public void updateCursor() {
		Point p = this.getMousePosition();
		
		cursorLocation.setLocation(p);
		cursorMapLocation.setLocation(p.getX() / magnification, p.getY() / magnification);
		
		parent.lblStatusBar.setText(
				"GridCoord : [" + cursorMapLocation.x + " , " + cursorMapLocation.y + " ]"
				+ " Cursor : [" + cursorLocation.x + " , " + cursorLocation.y  + "]"
				+ " Scrollbars: [" 
				+ parent.simulationScrollPane.getHorizontalScrollBar().getValue() + " , "
				+ parent.simulationScrollPane.getVerticalScrollBar().getValue() + "]");
	}
	
	public void zoom(int zoom) {
		float mag = geoptimize.helper.MathHelper.clamp(minMagnification, maxMagnification, (float)(magnification*Math.pow(magnificationSensitivity, zoom)));
		setMagnification(mag);
	}
	
	public void zoomIn() { 
		setMagnification((magnification < maxMagnification) ? magnification*magnificationSensitivity : magnification);
	} 
	
	public void zoomOut() { 
		setMagnification((magnification > minMagnification) ? magnification/magnificationSensitivity : magnification);
	}
	
	public void setShowSwarm(boolean b) {
		showSwarm = b;
	}

	public void setShowGBest(boolean b) {
		showGBest = b;
	}
	
	public void setParticleNumbering(boolean b) {
		particleNumbering = b;
	}
	
	public void setAlternativeColorMode(boolean b) {
		alternativeColors = b;
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
		
		//Dummy simulation for when there is no simulation started/loaded
		dummyNodes = new LinkedList<ServiceNode>();
		dummyNodes.add(new ServiceNode(2200, 2400, 80));
		dummyNodes.add(new ServiceNode(2400, 2600, 80));
		dummyNodes.add(new ServiceNode(2400, 2800, 80));
		
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
		
		//might not need this
		simulation = parent.context.getSimulation();
		
		if(simulation != null) {
			Color textColor = Color.RED;
			
			if(showSwarm) {
				int particleIndex = 0;
				for(PSOParticle particle : simulation.getParticles()) {
					
					List<ServiceNode> nodes = particle.getCurrent().getNodes();
					
					for(int i = 0; i < nodes.size(); i++) {
						Point p = nodes.get(i).getPosition();
						float r = nodes.get(i).getRange();
						
						if(alternativeColors) {
							graphics.setColor(colors[i%colors.length]);
						} else {
							graphics.setColor(swarmColor);
						}
						graphics.fillOval(
								(int)((p.x - r) * magnification),
								(int)((p.y - r) * magnification),
								(int)(r * 2 * magnification),
								(int)(r * 2 * magnification));
						
						graphics.setColor(textColor);
						if(particleNumbering) {
							graphics.drawString("Particle:" + particleIndex, 
								(int)(p.x * magnification),
								(int)(p.y * magnification));
						}
						
					}
					particleIndex++;
				}
			}
			
			//show globalbest
			if(showGBest) {
				
				
				List<ServiceNode> nodes = simulation.getGlobalBest().getNodes();
				for(ServiceNode node : nodes) {
					Point p = node.getPosition();
					float r = node.getRange();
					
					graphics.setColor(gbestColor);
					graphics.fillOval(
							(int)((p.x - r) * magnification),
							(int)((p.y - r) * magnification),
							(int)(r * 2 * magnification),
							(int)(r * 2 * magnification));
					
					graphics.setColor(textColor);
					graphics.drawString("fitness:" + simulation.getGlobalBest().getFitness(), 
							(int)(p.x * magnification),
							(int)(p.y * magnification + 10));
				}
			}
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
			updateCursor();
		}
	}



}
