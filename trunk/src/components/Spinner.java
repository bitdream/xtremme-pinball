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
import com.jmex.physics.material.Material;


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
	
	/* Tipo de spinner */
	private SpinnerType spinnerType;
	
	/* Pinball en el que esta */
	private static PinballGameState pinballInstance;
	
	// Tiempo de la ultima colision considerada (donde se llamo a la logica del juego) entre una bola y este spinner
	private long lastConsideredCollisionTime = 0;
	
	// TODO ver si el tiempo elegido funciona al tener la version final de la mesa
	// Ventana de tiempo dentro de la cual dos colisiones seran consideradas la misma. Medido en mseg
	// El tiempo debe ser grande pq la cantidad de colisiones detectadas depende de la velocidad de la bola
	private static final long windowTimeForCollisions = 1000; 

	/**
     * Crea un nodo dinamico de molinete.
	 * @param pinball El juego.
	 * @param name Nombre del nodo de molinete.
	 * @param visualModel Modelo visual del molinete.
	 * @return El nodo creado.
     */
	public static DynamicPhysicsNode create(PinballGameState pinball, String name, Geometry visualModel, SpinnerType spinnerType)
	{
		DynamicPhysicsNode spinnerNode = pinball.getPhysicsSpace().createDynamicNode();
		
		/* Actualizo los vectores globales */
		spinnerNode.updateWorldVectors();
		
		spinnerNode.setName("Spinner");
		
		pinballInstance = pinball;
		
		/* El material de los spinners es plastico como la mesa */
        spinnerNode.setMaterial(Material.PLASTIC);
		
        /* Creo un nodo de spinner, con todas sus caracteristicas y lo fijo al nodo fisico */
        final Spinner spinner = new Spinner(name, visualModel);
        spinnerNode.attachChild(spinner);
        
        /* Le asigno el tipo */
        spinner.setSpinnerType(spinnerType);
        
        /* Genero su fisica */
        spinnerNode.generatePhysicsGeometry();

        /* Computo su masa */
		spinnerNode.computeMass();
        
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
                // Tiempo en el que se dio esta colision
                long now = System.currentTimeMillis();
                
                // Tiempo de la ultima colision considerada
                long lastColl = spinner.getLastConsideredCollisionTime();
                
                // Si la diferencia con la ultima colision considerada no es menor a 1000 ms, la tomo como otra colision
                if (!(lastColl != 0 && now -  lastColl < windowTimeForCollisions))
                {   
                	/* Llamo a la logica del juego */
            		if (spinner.getSpinnerType() == SpinnerType.NORMAL_SPINNER)
            			pinballInstance.getGameLogic().spinnerNormalCollision(spinner);
            		else if (spinner.getSpinnerType() == SpinnerType.RAMP_ENTRANCE_SPINNER)
            			pinballInstance.getGameLogic().spinnerRampEntranceCollision(spinner);
            		else
            			pinballInstance.getGameLogic().spinnerRampExitCollision(spinner);
                    
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

	public SpinnerType getSpinnerType()
	{
		return spinnerType;
	}

	public void setSpinnerType(SpinnerType spinnerType)
	{
		this.spinnerType = spinnerType;
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
