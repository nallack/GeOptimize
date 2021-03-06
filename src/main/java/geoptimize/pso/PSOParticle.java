package geoptimize.pso;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import javax.vecmath.Vector2f;

import geoptimize.GridData;
import geoptimize.ServiceNode;
import geoptimize.helper.MathHelper;
import geoptimize.pso.fitness.PSOFitnessFunction;

/***
 * 
 * A particle containing a current solution, and a local best solution
 * Also contains particle velocity and inertia.
 * 
 * @author Callan
 *
 */
public class PSOParticle {
	
	protected PSOSolution current;
	protected float[] inertias;
	protected Vector2f[] velocities;
	
	protected PSOSolution localBest;
	
	protected int nNodes;
	protected int range;
	protected Rectangle region;
	
	//TODO: might want to move these to the GUI
	float localBestWeight = 0.3f;
	float globalBestWeight = 0.1f;
	float inertia = 1f;
	
	public PSOSolution getCurrent() { return current; }
	public PSOSolution getLocalBest() { return localBest; }
	
	public PSOParticle(int nNodes, int range, float lbest, float gbest, float inertia, Rectangle region) {
		this.nNodes = nNodes;
		this.range = range;
		this.localBestWeight = lbest;
		this.globalBestWeight = gbest;
		this.inertia = inertia;
		this.region = region;
		
		this.current = PSOSolution.CreateRandom(nNodes, range, region);
		this.inertias = new float[nNodes];
		this.velocities = new Vector2f[nNodes];
		for(int i = 0; i < nNodes; i++) {
			inertias[i] = inertia;
			velocities[i] = new Vector2f(0, 0);
		}
		
		this.localBest = (PSOSolution)this.current.clone();
	}
	
	private PSOParticle() { }
	
	/***
	 * Do a full clone
	 */
	public Object clone() {
		PSOParticle p = new PSOParticle();
		
		p.nNodes = this.nNodes;		
		p.range = this.range;
		p.region = (Rectangle)this.region.clone();
		
		
		p.current = (PSOSolution)this.current.clone();
		p.localBest = (PSOSolution)this.localBest.clone();
		
		return p;
	}
	
	/***
	 * Steps the current solution using particle data.
	 * Algorithm used from http://tracer.uc3m.es/tws/pso/basics.html
	 * @param globalBest
	 */
	public void step(PSOSolution globalBest) {
		///step using globalBest, localBest, inertias, velocities
		
		
		Random r = new Random();
		int aMax = 150;
		
		for(int i = 0; i < nNodes; i++) {
			
			velocities[i].x = 
					inertias[i] * velocities[i].x +
					localBestWeight  * r.nextInt(2) * MathHelper.clamp(-aMax, aMax, localBest.nodes.get(i).getPosition().x  - current.nodes.get(i).getPosition().x) +
					globalBestWeight * r.nextInt(2) * MathHelper.clamp(-aMax, aMax, globalBest.nodes.get(i).getPosition().x - current.nodes.get(i).getPosition().x);
			
			velocities[i].y =
					inertias[i] * velocities[i].y +
					localBestWeight  * r.nextInt(2) * MathHelper.clamp(-aMax, aMax, localBest.nodes.get(i).getPosition().y  - current.nodes.get(i).getPosition().y) +
					globalBestWeight * r.nextInt(2) * MathHelper.clamp(-aMax, aMax, globalBest.nodes.get(i).getPosition().y - current.nodes.get(i).getPosition().y);
			
			velocities[i].x = MathHelper.clamp(-(float)region.width, (float)region.width, velocities[i].x);
			velocities[i].y = MathHelper.clamp(-(float)region.height, (float)region.height, velocities[i].y);
			
			int x = (int)(current.nodes.get(i).getPosition().x + velocities[i].x);
			int y = (int)(current.nodes.get(i).getPosition().y + velocities[i].y);
			
			x = MathHelper.clamp((int)region.getMinX(), (int)region.getMaxX(), x);
			y = MathHelper.clamp((int)region.getMinY(), (int)region.getMaxY(), y);
			
			current.nodes.get(i).setPosition(x, y);
			
		}
	}
	
	/***
	 * Call this after stepping the simulation.
	 * May want to run in parallel as this is the slowest part of
	 * the simulation.
	 * @param function
	 */
	public void updateFitness(PSOFitnessFunction function) {
		current.fitness = function.calcFitness(current);
		
		if(current.fitness > localBest.fitness) {
			localBest = (PSOSolution)current.clone();
		}
	}
	
	
	/***
	 * Random step test, simply moves particles randomly.
	 */
	private void randomStep() {
		Random r = new Random();

		for(ServiceNode n : current.nodes)
		{
			n.setPosition(
				MathHelper.clamp(region.x, region.x + region.width, n.getPosition().x - 5 + r.nextInt(11)), 
				MathHelper.clamp(region.y, region.y + region.height, n.getPosition().y - 5 + r.nextInt(11))
			);
		}		
	}
}
