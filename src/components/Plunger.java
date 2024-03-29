package components;

import gamestates.PinballGameState;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.input.util.SyntheticButton;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.Joint;
import com.jmex.physics.TranslationalJointAxis;
import com.jmex.physics.contact.ContactInfo;
import com.jmex.physics.material.Material;


/**
 * Componente de lanzadera de pelotas.
 */
public class Plunger extends Node
{
	private static final long serialVersionUID = 1L;
	
	// COMUNICADO: estos valores estan asi para que con 10 grados sea complicado
	// dar la vueltita, pero no imposible, ajustarlos si no van.
	/* Valores de movimiento del plunger al disparar */
	private static final float SHOTACCELERATION = 100, SHOTSPEED = -100;
	
	/* Valores de movimiento del usuario sobre el plunger */
	private static final float USERACELERATION = 5, USERSPEED = 1; 
	// FIN DEL COMUNICADO
	
	/* Esta suelto o lo esta controlando el usuario */
	private boolean loose;
	
	/* Distancia a su origen */
	private float distance;
	
	/* Joint que lo fija a la mesa */
	private Joint joint;
	
	/* Juego que lo contiene */
//	private PinballGameState pinball;
	
	/* Pinball en el que esta */
	private static PinballGameState pinballInstance;
	
	
	/**
	 * Crea un nodo dinamico de plunger.
	 * @param pinball El juego.
	 * @param name Nombre del nodo de plunger.
	 * @param visualModel Modelo visual del plunger.
	 * @param maxBackstep Maxima distancia permitida hacia atras para tomar fuerza.
	 * @return El nodo creado.
	 */
	public static DynamicPhysicsNode create(PinballGameState pinball, String name, Geometry visualModel, float maxBackstep)
	{
		DynamicPhysicsNode plungerNode = pinball.getPhysicsSpace().createDynamicNode();
		
		plungerNode.setName("Plunger");
		
		pinballInstance = pinball;
		
		/* El material del plunger es de goma para tener un mejor rebote */
        plungerNode.setMaterial(Material.RUBBER);
		
        /* Creo un nodo de Plunger, con todas sus caracteristicas y lo fijo al nodo fisico */
        final Plunger plunger = new Plunger(name, visualModel);
        plungerNode.attachChild(plunger);
        
        /* Genero su fisica */
        plungerNode.generatePhysicsGeometry(true);
        
        /* Computo su masa */
		plungerNode.computeMass();
        
        /* Voy a fijar el plunger con un eje translacional */
        final Joint jointForPlunger = pinball.getPhysicsSpace().createJoint();
        final TranslationalJointAxis translationalAxis = jointForPlunger.createTranslationalAxis();
        
        /* Fijo el limite del plunger */
        translationalAxis.setPositionMinimum(0);
        translationalAxis.setPositionMaximum(maxBackstep);

        /* Vector que indica la direccion sobre la que se puede mover el plunger, el eje Z */
        translationalAxis.setDirection(new Vector3f(0, 0, 1));
        
        /* Lo fijo al centro del plunger */
        jointForPlunger.setAnchor(visualModel.getLocalTranslation());
        
        /* Guardo que ese plunger tiene este joint */
        plunger.setJoint(jointForPlunger);
        
        /* Guardo el juego en el componente */
//        plunger.setPinball(pinball);
        
        /* Agrego el componente al pinball */
        pinball.setPlunger(plungerNode);
        
        /* Para detectar colisiones de objetos */
        final SyntheticButton collisionEventHandler = plungerNode.getCollisionEventHandler();
        
        /* Agrego la accion al controlador de pinball */
        pinball.getPinballInputHandler().addAction(new InputAction() {
        	
        	public void performAction(InputActionEvent evt) {
        		
        		// Algo colisiono con el plunger
                final ContactInfo contactInfo = ( (ContactInfo) evt.getTriggerData() );

                // El contacto pudo haber sido bola -> plunger o plunger -> bola, si no se dio, no hago nada mas
                if ( !((contactInfo.getNode2() instanceof DynamicPhysicsNode && contactInfo.getNode2().getName().equals(PinballGameState.PHYSIC_NODE_NAME_FOR_BALLS)) || (contactInfo.getNode1() instanceof DynamicPhysicsNode && contactInfo.getNode1().getName().equals(PinballGameState.PHYSIC_NODE_NAME_FOR_BALLS))) )
                {
                	return;
                }
        		
                /* Llamo a la logica del juego */
        		pinballInstance.getGameLogic().plungerCollision(plunger);
            }

        }, collisionEventHandler, false);
        
        plungerNode.setAffectedByGravity( false );
        
		return plungerNode;
	}
	
	/**
	 * Toma un nombre y su representacion grafica.
	 */
	public Plunger(String name, Geometry visualModel)
	{
		super(name);
		
		attachChild(visualModel);
		
		/* Empieza suelto */
		setLoose(true);
		
		/* Empieza al lado del origen */
		setDistance(1);
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

	public boolean isLoose()
	{
		return loose;
	}

	public void setLoose(boolean loose)
	{
		this.loose = loose;
	}

	public float getDistance()
	{
		return distance;
	}

	public void setDistance(float distance)
	{
		this.distance = distance;
	}
	
	public Joint getJoint()
	{
		return joint;
	}

	public void setJoint(Joint joint)
	{
		this.joint = joint;
	}

//	public void setPinball(PinballGameState pinball)
//	{
//		this.pinball = pinball;
//	}
	
	public void update(float time)
	{
		if (isLoose())
		{
            /* Esta suelto, aplico una aceleracion para volverlo a su lugar */
		    
		    // esta fijo para todos los angulos. si no va
		    // podria calcularse aceleracion += aceleracion * sin(angulo)
		    // pero pinta que asi esta bien
    		joint.getAxes().get(0).setAvailableAcceleration( SHOTACCELERATION );
    		joint.getAxes().get( 0 ).setDesiredVelocity( SHOTSPEED );
    		
		}
		else
		{
	        /* Aplico la fuerza para alejarlo del origen */
		    // quiero que se aleje lentamente al ppio por si quiere un tiro suave o de rebote
		    joint.getAxes().get(0).setAvailableAcceleration( USERACELERATION );
            joint.getAxes().get( 0 ).setDesiredVelocity( USERSPEED );
		        
		}
			
	}
}
