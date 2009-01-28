package gamestates;

import com.jme.renderer.Camera;
import com.jme.system.DisplaySystem;
import com.jmex.physics.util.states.PhysicsGameState;


public class PhysicsEnhancedGameState extends PhysicsGameState
{

	protected DisplaySystem display;
	
	protected Camera cam;

	
	public PhysicsEnhancedGameState(String name)
	{
		super(name);
		
		display = DisplaySystem.getDisplaySystem();
		
		cam = display.getRenderer().getCamera();

		rootNode.updateRenderState();
	}
}
