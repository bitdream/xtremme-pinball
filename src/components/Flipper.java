package components;

import gamestates.PinballGameState;

import com.jme.bounding.BoundingBox;
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
import com.jmex.physics.material.Material;


/**
 * Componente de flipper, para golpear la pelotita.
 */
public class Flipper extends Node implements ActivableComponent
{
	private static final long serialVersionUID = 1L;
	
	/* Aceleracion y velocidad en los flippers */
	private static final float AVAILABLE_ACCELERATION_BACK = 15, AVAILABLE_ACCELERATION_FORWARD = 55, DESIRED_VELOCITY_BACK = 60, DESIRED_VELOCITY_FORWARD = 110;
	
	/* Fuerzas en los flippers */
	//public static final Vector3f flipperHitForce = new Vector3f(0f, 0f, -1600000f), flipperRestoreForce = new Vector3f(0f, 0f, 100000f);
	
	/* Maximos angulos de rotacion de los flippers */
	private static final float maxRightRotationalAngle = 0.3f, maxLeftRotationalAngle = 0.6f, minLeftRotationalAngle = -0.3f, minRightRotationalAngle = -0.6f;
	
	/* Para considerar extremos de los flippers */
	private static final float xExtreme = 0.95f, zExtreme = 0.8f;

	/* Tipos de flipper */
	public enum FlipperType {LEFT_FLIPPER, RIGHT_FLIPPER};
	
	/* Tipo de este flipper */
	private FlipperType flipperType;
	
	/* Joint que lo fija a la mesa */
	private Joint joint;
	
	/* Juego que lo contiene */
	private PinballGameState pinball;
	
	/* Esta activo? */
	private boolean active;
	
	/* Pinball en el que esta */
	private static PinballGameState pinballInstance;
	
	/* Lo estan usando? */
	private boolean inUse;
	

	/**
     * Crea un nodo dinamico de flipper.
	 * @param pinball El juego.
	 * @param name Nombre del nodo de flipper.
	 * @param visualModel Modelo visual del flipper.
	 * @param flipperType El tipo de flipper deseado, izquierdo o derecho.
	 * @return El nodo creado.
     */
	public static DynamicPhysicsNode create(PinballGameState pinball, String name, Geometry visualModel, FlipperType flipperType, Vector3f anchorPlace)
	{
		DynamicPhysicsNode flipperNode = pinball.getPhysicsSpace().createDynamicNode();
		
		flipperNode.setName("Flipper");
		
		pinballInstance = pinball;
		
		/* Actualizo los vectores globales */
		flipperNode.updateWorldVectors();
		
		/* Defino su material  */
        flipperNode.setMaterial(Material.PLASTIC);
		
        /* Creo un nodo de Flipper, con todas sus caracteristicas y lo fijo al nodo fisico */
        final Flipper flipper = new Flipper(name, visualModel, flipperType);
        flipper.setActive(true);
        flipperNode.attachChild(flipper);
        
        /* No los afecta la gravedad */
        flipperNode.setAffectedByGravity(false);
        
        /* Genero su fisica */
        flipperNode.generatePhysicsGeometry(true);
        
        /* Computo su masa */
		flipperNode.computeMass();
        
        /* Voy a fijar el flipper con un eje rotacional */
        final Joint jointForFlipper = pinball.getPhysicsSpace().createJoint();
        final RotationalJointAxis rotationalAxis = jointForFlipper.createRotationalAxis();

        /* Maximos angulos de operacion de los flippers */
        if (flipperType == FlipperType.LEFT_FLIPPER)
        {
	        rotationalAxis.setPositionMaximum(maxLeftRotationalAngle);
	        rotationalAxis.setPositionMinimum(minLeftRotationalAngle);
        }
        else
        {
	        rotationalAxis.setPositionMaximum(maxRightRotationalAngle);
	        rotationalAxis.setPositionMinimum(minRightRotationalAngle);
        }
        
        /* Vector que indica la direccion sobre la que esta parado el eje, en este caso, Y */
        rotationalAxis.setDirection(new Vector3f(0, 1, 0));
 
        /* Le fijo como punto de rotacion la punta del flipper */
        //debug
        if (anchorPlace == null)
            anchorPlace = locateFlipperExtreme(flipperType, visualModel);
        jointForFlipper.setAnchor(anchorPlace);
        
        /* Guardo que ese flipper tiene este joint */
        flipper.setJoint(jointForFlipper);
        
        /* Guardo el juego en el componente */
        flipper.setPinball(pinball);
        
        /* Agrego el componente a la lista del pinball */
        pinball.addFlipper(flipperNode);
        
        /* Para detectar colisiones de objetos */
        final SyntheticButton collisionEventHandler = flipperNode.getCollisionEventHandler();
        
        /* Agrego la accion al controlador de pinball */
        pinball.getPinballInputHandler().addAction(new InputAction() {
        	
        	public void performAction(InputActionEvent evt) {
        		
        		// Algo colisiono con el flipper
                final ContactInfo contactInfo = ( (ContactInfo) evt.getTriggerData() );

                // El contacto pudo haber sido bola -> flipper o flipper -> bola
                if ( (contactInfo.getNode2() instanceof DynamicPhysicsNode && contactInfo.getNode2().getName().equals(PinballGameState.PHYSIC_NODE_NAME_FOR_BALLS)) || (contactInfo.getNode1() instanceof DynamicPhysicsNode && contactInfo.getNode1().getName().equals(PinballGameState.PHYSIC_NODE_NAME_FOR_BALLS)) )
                {
                    /* Llamo a la logica del juego */
                    pinballInstance.getGameLogic().flipperCollision(flipper);
                }
            }

        }, collisionEventHandler, false);

		return flipperNode;
	}
	
	private static Vector3f locateFlipperExtreme(FlipperType flipperType, Geometry visualModel)
	{
		Vector3f extreme = new Vector3f();
		
		BoundingBox bBox = (BoundingBox)visualModel.getWorldBound();
		
		Vector3f extents = bBox.getExtent(null);
		
		/* En Y no va a variar, pues quiero el Y de su centro de masa */
		extreme.setY(bBox.getCenter().getY());

		/* Aproximo la punta del flipper */
		
		if (flipperType == FlipperType.RIGHT_FLIPPER)
		{
			/* Para flippers derechos */
			extreme.setX(bBox.getCenter().getX() + (0.5f - (1 - xExtreme)) * extents.getX());
		}
		else
		{
			/* Para flippers izquierdos */
			extreme.setX(bBox.getCenter().getX() - (0.5f - (1 - xExtreme)) * extents.getX());
		}
		
		extreme.setZ(bBox.getCenter().getZ() + (0.5f - (1 - zExtreme)) * extents.getZ());

		return extreme;
	}
	
	/**
	 * Toma un nombre, el tipo de flipper y su representacion grafica.
	 */
	public Flipper(String name, Geometry visualModel, FlipperType flipperType)
	{
		super(name);
		
		attachChild(visualModel);

		this.flipperType = flipperType;
	}
	
	public void recalculateJoints(PinballGameState pinball)
	{
		
		Quaternion rot = pinball.getPinballSettings().getInclinationQuaternion();
		
		/* Tomo el angulo de juego e inclino el eje del joint */
		joint.getAxes().get(0).setDirection(joint.getAxes().get(0).getDirection(null).rotate(rot));
		
		/* Tomo la anterior posicion del anchor y la roto */
		joint.setAnchor(joint.getAnchor(null).rotate(rot));
		
		/* Recien ahora attacheo al nodo el joint */
		joint.attach((DynamicPhysicsNode)getParent());
	}

	public FlipperType getFlipperType()
	{
		return flipperType;
	}
	
	public boolean isRightFlipper()
	{
		return flipperType.equals(FlipperType.RIGHT_FLIPPER);
	}
	
	public boolean isLeftFlipper()
	{
		return flipperType.equals(FlipperType.LEFT_FLIPPER);
	}

	public Joint getJoint()
	{
		return joint;
	}

	public void setJoint(Joint joint)
	{
		this.joint = joint;
	}

	public void setPinball(PinballGameState pinball)
	{
		this.pinball = pinball;
	}
	
	public void update(float time)
	{
		/* Fijo las velocidades deseadas en base a si esta en uso o no (y si es izq o der) */
		if (isInUse())
		{
			/* Fijo la aceleracion con la que cuenta */
			joint.getAxes().get(0).setAvailableAcceleration(AVAILABLE_ACCELERATION_FORWARD);
			
			if (isLeftFlipper())
			{
				joint.getAxes().get(0).setDesiredVelocity(DESIRED_VELOCITY_FORWARD);
			}
			else
			{
				joint.getAxes().get(0).setDesiredVelocity(-DESIRED_VELOCITY_FORWARD);
			}
		}
		else
		{
			/* Fijo la aceleracion con la que cuenta */
			joint.getAxes().get(0).setAvailableAcceleration(AVAILABLE_ACCELERATION_BACK);
			
			if (isLeftFlipper())
			{
				joint.getAxes().get(0).setDesiredVelocity(-DESIRED_VELOCITY_BACK);
			}
			else
			{
				joint.getAxes().get(0).setDesiredVelocity(DESIRED_VELOCITY_BACK);
			}
		}
		
//		TODO Viejo metodo de las fuerzas explicitas
//		Quaternion rot = pinball.getPinballSettings().getInclinationQuaternion();
//		
//		/* Cada vez que el motor de fisica llama a actualizacion, aplico la fuerza
//		 * de recuperacion de los flippers */
//		final Vector3f forceToApply = new Vector3f();
//		
//		forceToApply.set(flipperRestoreForce).multLocal(time);
//
//		((DynamicPhysicsNode)getParent()).addForce(forceToApply.rotate(rot));
	}
	
	public void setActive(boolean active)
	{
		this.active = active; 
		
	}
	
	public boolean isActive()
	{
		return this.active;
	}

	public boolean isInUse()
	{
		return inUse;
	}

	public void setInUse(boolean inUse)
	{
		this.inUse = inUse;
	}
}
