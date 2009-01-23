package components;

import mainloop.Pinball;

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
import com.jmex.physics.material.Material;


/**
 * Componente de flipper, para golpear la pelotita.
 */
public class Flipper extends Node implements ActivableComponent
{
	private static final long serialVersionUID = 1L;
	
	/* Fuerzas en los flippers */
	public static final Vector3f flipperHitForce = new Vector3f(0f, 0f, -16000000f), flipperRestoreForce = new Vector3f(0f, 0f, 1000000f);
	
	/* Maximos angulos de rotacion de los flippers */
	private static final float maxRotationalAngle = 0.4f, minRotationalAngle = -0.4f;
	
	/* Para considerar extremos de los flippers */
	private static final float xExtreme = 0.95f, zExtreme = 0.8f;

	/* Tipos de flipper */
	public enum FlipperType {LEFT_FLIPPER, RIGHT_FLIPPER};
	
	/* Tipo de este flipper */
	private FlipperType flipperType;
	
	/* Joint que lo fija a la mesa */
	private Joint joint;
	
	/* Juego que lo contiene */
	private Pinball pinball;
	
	/* Esta activo? */
	public boolean active;
	
	/* Pinball en el que esta */
	private static Pinball pinballInstance;
	
	
    /**
     * Crea un nodo dinamico de flipper.
	 * @param pinball El juego.
	 * @param name Nombre del nodo de flipper.
	 * @param visualModel Modelo visual del flipper.
	 * @param flipperType El tipo de flipper deseado, izquierdo o derecho.
	 * @return El nodo creado.
     */
	public static DynamicPhysicsNode create(Pinball pinball, String name, Geometry visualModel, FlipperType flipperType)
	{
		DynamicPhysicsNode flipperNode = pinball.getPhysicsSpace().createDynamicNode();
		
		flipperNode.setName("Flipper");
		
		pinballInstance = pinball;
		
		/* Actualizo los vectores globales */
		flipperNode.updateWorldVectors();
		
		/* El material de los flippers es goma para simular la banda de goma que los rodea */
        flipperNode.setMaterial(Material.RUBBER);
		
        /* Creo un nodo de Flipper, con todas sus caracteristicas y lo fijo al nodo fisico */
        final Flipper flipper = new Flipper(name, visualModel, flipperType);
        flipper.setActive(true);
        flipperNode.attachChild(flipper);
        
        /* Genero su fisica */
        flipperNode.generatePhysicsGeometry();
        
        /* Computo su masa */
		flipperNode.computeMass();
        
        /* Voy a fijar el flipper con un eje rotacional */
        final Joint jointForFlipper = pinball.getPhysicsSpace().createJoint();
        final RotationalJointAxis rotationalAxis = jointForFlipper.createRotationalAxis();
        
        /* Maximos angulos de operacion de los flippers */
        rotationalAxis.setPositionMaximum(maxRotationalAngle);
        rotationalAxis.setPositionMinimum(minRotationalAngle);
        
        /* Vector que indica la direccion sobre la que esta parado el eje, en este caso, Y */
        rotationalAxis.setDirection(new Vector3f(0, 1, 0));
        
        /* Le fijo como punto de rotacion la punta del flipper */
        jointForFlipper.setAnchor(locateFlipperExtreme(flipperType, visualModel));
        
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
        		
                /* Llamo a la logica del juego */
                pinballInstance.getGameLogic().flipperCollision(flipper);
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
	
	public void recalculateJoints(Pinball pinball)
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

	public void setPinball(Pinball pinball)
	{
		this.pinball = pinball;
	}
	
	public void update(float time)
	{
		Quaternion rot = pinball.getPinballSettings().getInclinationQuaternion();
		
		/* Cada vez que el motor de fisica llama a actualizacion, aplico la fuerza
		 * de recuperacion de los flippers */
		final Vector3f forceToApply = new Vector3f();
		
		forceToApply.set(flipperRestoreForce).multLocal(time);

		((DynamicPhysicsNode)getParent()).addForce(forceToApply.rotate(rot));
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
