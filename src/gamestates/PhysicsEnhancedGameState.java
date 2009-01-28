package gamestates;

import com.jme.renderer.Camera;
import com.jme.renderer.Renderer;
import com.jme.scene.state.LightState;
import com.jme.system.DisplaySystem;
import com.jme.util.geom.Debugger;
import com.jmex.physics.PhysicsDebugger;
import com.jmex.physics.util.states.PhysicsGameState;


public class PhysicsEnhancedGameState extends PhysicsGameState
{

	protected DisplaySystem display;
	
	protected Camera cam;

	protected LightState lightState;
	
	public PhysicsEnhancedGameState(String name)
	{
		super(name);
		
		display = DisplaySystem.getDisplaySystem();
		
		cam = display.getRenderer().getCamera();

	    /** Attach the light to a lightState and the lightState to rootNode. */
        lightState = display.getRenderer().createLightState();
        lightState.setEnabled( true );
        
        rootNode.setRenderState( lightState );
		
		rootNode.updateRenderState();
	}
	
	@Override
	public void render(float tpf) 
	{
	    super.render( tpf );
        doDebug(DisplaySystem.getDisplaySystem().getRenderer());
    }
	
	protected boolean showBounds, showNormals, showPhysics;
	
    protected void doDebug(Renderer r) {
        /**
         * If showing bounds, draw rootNode's bounds, and the bounds of all its
         * children.
         */
        if ( showBounds ) {
            Debugger.drawBounds( rootNode, r, true );
        }

        if ( showNormals ) {
            Debugger.drawNormals( rootNode, r );
            Debugger.drawTangents( rootNode, r );
        }

        if ( showPhysics ) {
            PhysicsDebugger.drawPhysics( getPhysicsSpace(), r );
        }
    }
}
