package components;

import mainloop.Pinball;
import com.jme.math.Vector3f;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.shape.Sphere;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.PhysicsNode;
import com.jmex.physics.PhysicsSpace;
import com.jmex.physics.PhysicsUpdateCallback;
import com.jmex.physics.StaticPhysicsNode;


/**
 * Componente magnet, actua como un iman atrayendo a la bola.
 */
public class Magnet extends Node
{
	private static final long serialVersionUID = 1L;
	
	// Modelo visual del bumper
	private Geometry visualModel;
	
	// Intensidad de la fuerza
	private static int force = 5000;
	
	// Radio de alcance de la fuerza atractora
	private static float maxRadius = 3;

	public static StaticPhysicsNode create(Pinball pinball, String name, Geometry visualModel)
	{
		StaticPhysicsNode magnetNode = pinball.getPhysicsSpace().createStaticNode();
		
		final Magnet m = new Magnet(name, visualModel);
        // Creo un nodo de Magnet, con todas sus caracteristicas y lo fijo al nodo fisico
		magnetNode.attachChild(m);
        
        // Genero su fisica 
		magnetNode.generatePhysicsGeometry();
		
		// Efecto de iman en cada paso fisico
		pinball.getPhysicsSpace().addToUpdateCallbacks( new PhysicsUpdateCallback() {
			
	        public void afterStep(PhysicsSpace space, float time) 
	        {
	            for (PhysicsNode n: space.getNodes()) 
	            {	          
	            	// TODO ver: estoy suponiendo que las bolas del flipper van a estar formadas por un nodo fisico con un 
	            	// unico nodo visual attacheado. Y que dicho nodo visual sera una esfera. Otra forma de identificarlo: el nombre del nodo fisico por convencion el "ball"
	                if (n instanceof DynamicPhysicsNode && n.getChild(0) instanceof Sphere) 
	                {
	                    DynamicPhysicsNode dyn = (DynamicPhysicsNode)n;
	                    
	                    // Calcular la distancia entre la bola y el iman
	                    float distance = n.getLocalTranslation().distance(m.getVisualModel().getLocalTranslation());
	                   System.out.println("----------------distance: " + distance);
	                    // Si la distancia es menor a cierto valor, se aplica la fuerza
	                    if (distance < 40)
	                    {
	                    	/* Calcular la direccion en la que hay que aplicar la fuerza como resta de las posiciones
		                     * de la bola y del iman.
		                     */	                    
		                    Vector3f direction = m.getVisualModel().getLocalTranslation().subtract(n.getLocalTranslation()).normalize();
		                   
		                    // Aplicar la fuerza atractora. Formula magica. TODO ajustar la fuerza para un mejor comportamiento
		                    dyn.addForce(direction.mult(force*dyn.getMass()).divide(100/maxRadius*distance));
	                    }
	                    
	                }
	            }
	        }
	        public void beforeStep(PhysicsSpace space, float time) 
	        {
	        	
	        }
        } );
		
		return magnetNode;
	}
	
	public Geometry getVisualModel() 
	{
		return visualModel;
	}

	public void setVisualModel(Geometry visualModel) 
	{
		this.visualModel = visualModel;
	}

	/**
	 * Toma un nombre y la representacion grafica y crea un iman.
	 * NOTA: Es importante setear la posicion del objeto visual antes de crear el iman ya que 
	 * es necesaria para calcular cercania a las bolas.
	 */
	public Magnet(String name, Geometry visualModel)
	{
		super(name);
		this.visualModel = visualModel;
		attachChild(visualModel);

	}
}
