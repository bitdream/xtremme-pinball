package components;

import gamestates.PinballGameState;

import com.jme.math.Vector3f;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.PhysicsSpace;
import com.jmex.physics.PhysicsUpdateCallback;
import com.jmex.physics.StaticPhysicsNode;


/**
 * Componente magnet, actua como un iman atrayendo a la bola.
 */
public class Magnet extends Node implements ActivableComponent
{
	private static final long serialVersionUID = 1L;
	
	// Modelo visual del bumper
	private Geometry visualModel;
	
	// Intensidad de la fuerza
	private static int force = 500; //TODO antes 500 Quizas sea bueno hacerla directamente proporcional al angulo de inclinacion
	
	//TODO hacer forceFieldRadius y maxRadius acordes a los valores y dimensiones que tengan finalmente los objetos de la mesa (xed final)
	// Radio del campo de la fuerza magnetica
	private static int forceFieldRadius = 40; // Testeado con los sig valores: bolas de radio 1 e iman un Box("Visual magnet 1", new Vector3f(), 2f, 4f, 2f);
	
	// Constante de proporcionalidad para el calculo de la intensidad de la fuerza atractora
	private static float maxRadius = 3; //Valor de testeo: 3 cuando las bolas son de radio 1
	
	// Esta activo este magnet?
	public boolean active = false; //FIXME

	public static StaticPhysicsNode create(final PinballGameState pinball, String name, Geometry visualModel)
	{
		StaticPhysicsNode magnetNode = pinball.getPhysicsSpace().createStaticNode();
		
		magnetNode.setName("Magnet");
		
		final Magnet magnet = new Magnet(name, visualModel);
        // Creo un nodo de Magnet, con todas sus caracteristicas y lo fijo al nodo fisico
		magnetNode.attachChild(magnet);
        
        // Genero su fisica 
		magnetNode.generatePhysicsGeometry(true);
		
		// Efecto de iman en cada paso fisico
		pinball.getPhysicsSpace().addToUpdateCallbacks( new PhysicsUpdateCallback() {
			
	        public void afterStep(PhysicsSpace space, float time) 
	        {
	        	// Solo se aplica la fuerza sobre las bolas si el iman esta activo
	        	if (magnet.isActive())
	        	{
	        		 for (DynamicPhysicsNode ball: pinball.getBalls()) 
	 	            {	          
	 	                    // Calcular la distancia entre la bola y el iman
	 	                    // IMPORTANTE: la distancia se calcula entre los centros de masa de los objetos
	 	                    float distance = ball.getLocalTranslation().distance(magnet.getVisualModel().getLocalTranslation());

	 	                    // Si la distancia es menor a cierto valor, se aplica la fuerza
	 	                    if (distance < forceFieldRadius)
	 	                    {
	 	                    	/* Calcular la direccion en la que hay que aplicar la fuerza como resta de las posiciones
	 		                     * de la bola y del iman. El sentido de la fuerza debe ser hacia el iman.
	 		                     */	 
	 		                    Vector3f direction = magnet.getVisualModel().getLocalTranslation().subtract(ball.getLocalTranslation()).normalize();	         
	 		                    
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
		
		/* Agrego el componente a la lista del pinball */
        pinball.addMagnet(magnetNode);
		
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

	public void setActive(boolean active)
	{
		this.active = active; 
		
	}
	
	public boolean isActive()
	{
		return this.active;
	}
}
