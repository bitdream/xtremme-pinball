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
	
	// Intensidad de la fuerza. Seteada desde el codigo de abajo en funcion del angulo de inclinacion
	private static int force;

	// Radio del campo de la fuerza magnetica
	private static float forceFieldRadius = 4f; // La bola tiene radio 0.25 
	
	// Constante de proporcionalidad para el calculo de la intensidad de la fuerza atractora
	private static float maxRadius = 3f;
	
	// Esta activo este magnet?
	public boolean active = false;

	public static StaticPhysicsNode create(final PinballGameState pinball, String name, Geometry visualModel)
	{
		StaticPhysicsNode magnetNode = pinball.getPhysicsSpace().createStaticNode();
		
		magnetNode.setName("Magnet");
		
		final Magnet magnet = new Magnet(name, visualModel);
		
        // Creo un nodo de Magnet, con todas sus caracteristicas y lo fijo al nodo fisico
		magnetNode.attachChild(magnet);
        
        // Genero su fisica 
		magnetNode.generatePhysicsGeometry(/*true*/);		
		
		// La fuerza dependera del angulo de inclinacion de la mesa
		force = mapAngleToForce(pinball.getPinballSettings().getInclinationLevel());	
		
		// Efecto de iman en cada paso fisico
		pinball.getPhysicsSpace().addToUpdateCallbacks( new PhysicsUpdateCallback() {
			
	        public void afterStep(PhysicsSpace space, float time) 
	        {
	        	// Solo se aplica la fuerza sobre las bolas si el iman esta activo
	        	if (magnet.isActive())
	        	{
	        		 for (DynamicPhysicsNode ball: pinball.getBalls()) 
	 	            {	          
	        			 ball.updateWorldVectors(true);
	        			 
	 	                    // Calcular la distancia entre la bola y el iman
	 	                    // IMPORTANTE: la distancia se calcula entre los centros de masa de los objetos
	 	                    float distance = ball.getChild(0).getWorldTranslation().distance(magnet.getVisualModel().getLocalTranslation());
	        			 	
	 	                    // Si la distancia es menor a cierto valor, se aplica la fuerza
	 	                    if (distance < forceFieldRadius)
	 	                    {
	 	                    	/* Calcular la direccion en la que hay que aplicar la fuerza como resta de las posiciones
	 		                     * de la bola y del iman. El sentido de la fuerza debe ser hacia el iman.
	 		                     */	 
	 		                    Vector3f direction = magnet.getVisualModel().getLocalTranslation().subtract(ball.getChild(0).getWorldTranslation()).normalize();	         
	 		                    
	 		                    // Es inversamente proporcional a la distancia
	 		                    Vector3f appliedForce = direction.mult(force*ball.getMass()).divide(100/maxRadius*distance);

	 		                    // Bajar la intensidad de la fuerza en z para que la bola no suba, pero si se vaya para el costado
	 		                    appliedForce.setZ(appliedForce.getZ()/2);

	 		                    // Aplicar la fuerza atractora. 
	 		                    ball.addForce(appliedForce);                 
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
	
	/**
	 * En base al angulo de inclinacion de la mesa calcula la constante aplicada para el computo de la intensidad 
	 * de la fuerza atractora
	 */
	private static int mapAngleToForce(float angle)
	{
		float epsilon = 0.001f;
		int force;
		
		if ( angle <= 1 + epsilon)
		{
			force = 20;
		}
		else if ( angle <= 2 + epsilon)
		{
			force = 35;
		}
		else if ( angle <= 3 + epsilon)
		{
			force = 60;
		}		
		else if ( angle <= 4 + epsilon)
		{
			force = 80;
		}
		else if ( angle <= 5 + epsilon)
		{
			force = 105;
		}
		else if (angle <= 6 + epsilon)
		{
			force = 150;
		}
		else if (angle <= 7 + epsilon)
		{
			force = 185;
		}
		else if (angle <= 8 + epsilon)
		{
			force = 210;
		}		
		else if (angle <= 9 + epsilon)
		{
			force = 275;
		}
		else // Pensado para un angulo maximo de 10 grados
		{
			force = 300;
		}
		
		return force;
	}
}
