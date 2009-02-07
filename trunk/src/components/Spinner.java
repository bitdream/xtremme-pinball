package components;

import gamestates.PinballGameState;

import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.input.util.SyntheticButton;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.*;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.Joint;
import com.jmex.physics.RotationalJointAxis;
import com.jmex.physics.contact.ContactInfo;

/**
 * Componente de molinete que permite pasar la pelota y se pone a girar.
 */
public class Spinner extends Node
{
	private static final long serialVersionUID = 1L;
	
	private static float speedDecreasePercentageX = 0.995f, speedDecreasePercentageY = 0.995f;
	
	/* Tipos de spinner */
	public enum SpinnerType {NORMAL_SPINNER, RAMP_EXIT_SPINNER, RAMP_ENTRANCE_SPINNER};
	
	/* Joint que lo fija a la mesa */
	private Joint joint;
	
	/* Pinball en el que esta */
	private static PinballGameState pinballInstance;
	
	// Tiempo de la ultima colision considerada (donde se llamo a la logica del juego) entre una bola y este spinner
	private long lastConsideredCollisionTime = 0;
	
	// Ventana de tiempo dentro de la cual dos colisiones seran consideradas la misma. Medido en mseg
	// El tiempo debe ser grande pq la cantidad de colisiones detectadas depende de la velocidad de la bola
	private static long windowTimeForCollisions = 1000; 

	/**
     * Crea un nodo dinamico de molinete.
	 * @param pinball El juego.
	 * @param name Nombre del nodo de molinete.
	 * @param visualModel Modelo visual del molinete.
	 * @return El nodo creado.
     */
	public static DynamicPhysicsNode create(PinballGameState pinball, String name, Geometry visualModel/*, SpinnerType spinnerType*/)
	{
		DynamicPhysicsNode spinnerNode = pinball.getPhysicsSpace().createDynamicNode();
		
		/* Actualizo los vectores globales */
		spinnerNode.updateWorldVectors();
		
		spinnerNode.setName("Spinner");
		
		pinballInstance = pinball;
		
		// Para evitar que con angulos muy chicos la bola tarde en caer por el spinner y haga contacto mucho tiempo sumando puntos.
		windowTimeForCollisions =  pinball.getPinballSettings().getInclinationLevel() < 3 ? 1500: 1000;
		
        /* Creo un nodo de spinner, con todas sus caracteristicas y lo fijo al nodo fisico */
        final Spinner spinner = new Spinner(name, visualModel);
        spinnerNode.attachChild(spinner);
        
        /* Genero su fisica */
        spinnerNode.generatePhysicsGeometry(true);
        
        /* Computo su masa */
		spinnerNode.computeMass();
		
        /* Seteo la masa para que sea relativamente facil de pasar por la bola y para evitar que gire demasiado */
        spinnerNode.setMass(0.06f);
        
        /* Voy a fijar el spinner con un eje rotacional */
        final Joint jointForSpinner = pinball.getPhysicsSpace().createJoint();
        final RotationalJointAxis rotationalAxis = jointForSpinner.createRotationalAxis();
        
        /* Vector que indica la direccion sobre la que esta parado el eje, en este caso, X por default */
        rotationalAxis.setDirection(new Vector3f(1, 0, 0).rotate(visualModel.getWorldRotation()));
                
        /* Le fijo como punto de rotacion su centro */
        jointForSpinner.setAnchor(visualModel.getWorldTranslation());
        
        /* Guardo que esa door tiene este joint */
        spinner.setJoint(jointForSpinner);
        
        /* Agrego el componente a la lista del pinball */
        pinball.addSpinner(spinnerNode);
        
        /* Para detectar colisiones de objetos */
        final SyntheticButton collisionEventHandler = spinnerNode.getCollisionEventHandler();
        
        /* Agrego la accion al controlador de pinball */
        pinball.getPinballInputHandler().addAction(new InputAction() {
        	
        	public void performAction(InputActionEvent evt) 
        	{
        		// Algo colisiono con el spinner
                final ContactInfo contactInfo = ( (ContactInfo) evt.getTriggerData() );

                // El contacto pudo haber sido bola -> spinner o spinner -> bola, si no se dio, no hago nada mas
                if ( !((contactInfo.getNode2() instanceof DynamicPhysicsNode && contactInfo.getNode2().getName().equals(PinballGameState.PHYSIC_NODE_NAME_FOR_BALLS)) || (contactInfo.getNode1() instanceof DynamicPhysicsNode && contactInfo.getNode1().getName().equals(PinballGameState.PHYSIC_NODE_NAME_FOR_BALLS))) )
                {
                	return;
                }
        		
                // Tiempo en el que se dio esta colision
                long now = System.currentTimeMillis();
                
                // Tiempo de la ultima colision considerada
                long lastColl = spinner.getLastConsideredCollisionTime();
                
                // Si la diferencia con la ultima colision considerada no es menor a windowTimeForCollisions ms, la tomo como otra colision
                if (!(lastColl != 0 && now -  lastColl < windowTimeForCollisions))
                {   
                	/* Llamo a la logica del juego */
           			pinballInstance.getGameLogic().spinnerNormalCollision(spinner);
                    
                    // Actualizo el tiempo de la ultima colision considerada
                	spinner.setLastConsideredCollisionTime(now);
                }
                // Sino no hago nada pq es una colision repetida        
        		
           }

        }, collisionEventHandler, false);
        
        
		return spinnerNode;
	}
	
	/**
	 * Toma un nombre y su representacion grafica.
	 */
	public Spinner(String name, Geometry visualModel)
	{
		super(name);
		
		attachChild(visualModel);
	}
	
	public void recalculateJoints(PinballGameState pinball)
	{
		
		Quaternion rot = pinball.getPinballSettings().getInclinationQuaternion();
		
		/* Tomo el angulo de juego e inclino el eje anterior del joint */
		joint.getAxes().get(0).setDirection(joint.getAxes().get(0).getDirection(null).rotate(rot));
		
		/* Tomo la anterior posicion del anchor y la roto */
		joint.setAnchor(joint.getAnchor(null).rotate(rot));
		
		/* Recien ahora attacheo al nodo el joint */
		joint.attach((DynamicPhysicsNode)getParent());
	}
	
	public Joint getJoint()
	{
		return joint;
	}

	public void setJoint(Joint joint)
	{
		this.joint = joint;
	}
	
	public void update(float time)
	{
		DynamicPhysicsNode parentNode = (DynamicPhysicsNode)getParent();

		parentNode.setAngularVelocity(new Vector3f(
				parentNode.getAngularVelocity(null).x * speedDecreasePercentageX, 
				parentNode.getAngularVelocity(null).y * speedDecreasePercentageY, 
				parentNode.getAngularVelocity(null).z));
	}

    public long getLastConsideredCollisionTime() 
    {
		return lastConsideredCollisionTime;
	}

	public void setLastConsideredCollisionTime(long lastConsideredCollisionTime) 
	{
		this.lastConsideredCollisionTime = lastConsideredCollisionTime;
	}
}
