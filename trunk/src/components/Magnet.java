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
	
	//TODO hacer forceFieldRadius y maxRadius acordes a los valores y dimensiones que tengan finalmente los objetos de la mesa (xed final)
	// Radio del campo de la fuerza magnetica
	private static int forceFieldRadius = 40; // Testeado con los sig valores: bolas de radio 1 e iman un Box("Visual magnet 1", new Vector3f(), 2f, 4f, 2f);
	
	// Constante de proporcionalidad para el calculo de la intensidad de la fuerza atractora
	private static float maxRadius = 3; //Valor de testeo: 3 cuando las bolas son de radio 1

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
	            for (PhysicsNode node: space.getNodes()) 
	            {	          
	            	// TODO ver: estoy suponiendo que las bolas del flipper van a estar formadas por un nodo fisico con un 
	            	// unico nodo visual attacheado. Y que dicho nodo visual sera una esfera. 
	            	// Otra forma de identificarlo: el nombre del nodo fisico por convencion el "ball"
	                if (node instanceof DynamicPhysicsNode && node.getChild(0) instanceof Sphere) 
	                {
	                    DynamicPhysicsNode ball = (DynamicPhysicsNode)node;
	                    
	                    // Calcular la distancia entre la bola y el iman
	                    // IMPORTANTE: la distancia se calcula entre los centros de masa de los objetos
	                    float distance = node.getLocalTranslation().distance(m.getVisualModel().getLocalTranslation());
	                    //System.out.println("----------------distance: " + distance + " con el nodo " + node.getChild(0).getName());
	                    // Si la distancia es menor a cierto valor, se aplica la fuerza
	                    if (distance < forceFieldRadius)
	                    {
	                    	/* Calcular la direccion en la que hay que aplicar la fuerza como resta de las posiciones
		                     * de la bola y del iman. El sentido de la fuerza debe ser hacia el iman.
		                     */	                    
		                    Vector3f direction = m.getVisualModel().getLocalTranslation().subtract(node.getLocalTranslation()).normalize();
		                   
		                    // Aplicar la fuerza atractora. Formula magica. TODO ajustar la fuerza para un mejor comportamiento
		                    // Es inversamente proporcional a la distancia
		                    ball.addForce(direction.mult(force*ball.getMass()).divide(100/maxRadius*distance));
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
