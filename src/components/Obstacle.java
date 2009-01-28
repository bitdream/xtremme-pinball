package components;

import gamestates.PinballGameState;

import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jmex.physics.StaticPhysicsNode;


/**
 * Componente obstacle.
 */
public class Obstacle extends Node
{
	private static final long serialVersionUID = 1L;
	
	public static StaticPhysicsNode create(PinballGameState pinball, String name, Geometry visualModel)
	{
		StaticPhysicsNode obstacleNode = pinball.getPhysicsSpace().createStaticNode();
		
		obstacleNode.setName("Obstacle");
		
		final Obstacle m = new Obstacle(name, visualModel);
		
        /* Creo un nodo de Obstacle, con todas sus caracteristicas y lo fijo al nodo fisico */
		obstacleNode.attachChild(m);
        
        /* Genero su fisica */ 
		obstacleNode.generatePhysicsGeometry();
		
		return obstacleNode;
	}
	
	/**
	 * Toma un nombre y la representacion grafica y crea un obstaculo.
	 */
	public Obstacle(String name, Geometry visualModel)
	{
		super(name);

		attachChild(visualModel);

	}
}
