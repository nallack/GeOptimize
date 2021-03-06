package geoptimize.pso.fitness;

import java.awt.Rectangle;

import geoptimize.GridData;
import geoptimize.ServiceNode;
import geoptimize.pso.PSOSolution;


public class PSOFitnessFast extends PSOFitnessFunction {

	public PSOFitnessFast(GridData grid, Rectangle region) {
		super(grid, region);
	}
	
	/***
	 * Steps through entire population grid and adds a the pixel 
	 * to the fitness if it is within range of a tower.
	 * TODO: Broken atm.
	 * @param grid
	 * @param region
	 * @return
	 */
	@Override
	public float calcFitness(PSOSolution solution) {

		float[] inrange = new float[region.width * region.height];
		
		for(int y = (int)region.getMinY(); y < region.getMaxY(); y++) {
			for(int x = (int)region.getMinX(); x < region.getMaxX(); x++) {
				for(ServiceNode n : solution.getNodes()) {
					int xdist = x - n.getPosition().x;
					int ydist = y - n.getPosition().y;
					int distsqr = xdist*xdist+ydist*ydist;
					
					if(distsqr < n.getRange() * n.getRange()) {
						int rx = x - region.x;
						int ry = y - region.y;
						inrange[ry*region.width + rx] = grid.get(x, y);
					}
					
				}
			}
		}
		
		float fitness = 0;
		for(int ry = 0; ry < region.height; ry++) {
			for(int rx = 0; rx < region.width; rx++) {
				fitness += inrange[ry*region.width+rx];
			}
		}
		return fitness;
	}

}
