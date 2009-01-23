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
 * Componente de puerta que permite pasar la pelota en un solo sentido.
 */
public class Door extends Node
{
	private static final long serialVersionUID = 1L;
	
	/* Tipos de puerta */
	public enum DoorType {LEFT_DOOR, RIGHT_DOOR};
	
	/* Para considerar extremos de las puertas */
	private static final float xExtreme = 0.98f, zExtreme = 0.98f;
	
	/* Tipo de esta puerta */
	private DoorType doorType;
		
	/* Joint que lo fija a la mesa */
	private Joint joint;
	
	/* Pinball en el que esta */
	private static Pinball pinballInstance;
	

    /**
     * Crea un nodo dinamico de puerta.
	 * @param pinball El juego.
	 * @param name Nombre del nodo de puerta.
	 * @param visualModel Modelo visual de la puerta.
	 * @param doorType El tipo de puerta deseado, iquierdo o derecho.
	 * @param minRotationalAngle Angulo minimo de rotacion para la puerta.
	 * @param maxRotationalAngle Angulo maximo de rotacion para la puerta.
	 * @return El nodo creado.
     */
	public static DynamicPhysicsNode create(Pinball pinball, String name, Geometry visualModel, DoorType doorType, float minRotationalAngle, float maxRotationalAngle)
	{
		DynamicPhysicsNode doorNode = pinball.getPhysicsSpace().createDynamicNode();
		
		doorNode.setName("Door");
		
		pinballInstance = pinball;
		
		/* El material de las puertas es plastico como la mesa */
        doorNode.setMaterial(Material.PLASTIC);
		
        /* Creo un nodo de puerta, con todas sus caracteristicas y lo fijo al nodo fisico */
        final Door door = new Door(name, visualModel, doorType);
        doorNode.attachChild(door);
        
        /* Genero su fisica */
        doorNode.generatePhysicsGeometry();
        
        /* Computo su masa */
		doorNode.computeMass();
        
        /* Voy a fijar la puerta con un eje rotacional */
        final Joint jointForDoor = pinball.getPhysicsSpace().createJoint();
        final RotationalJointAxis rotationalAxis = jointForDoor.createRotationalAxis();
        
        /* Maximos angulos de operacion de la puerta */
        rotationalAxis.setPositionMaximum(maxRotationalAngle);
        rotationalAxis.setPositionMinimum(minRotationalAngle);
        
        /* Vector que indica la direccion sobre la que esta parado el eje, en este caso, Y */
        rotationalAxis.setDirection(new Vector3f(0, 1, 0));
                
        /* Le fijo como punto de rotacion la punta de la puerta */
        jointForDoor.setAnchor(locateDoorExtreme(doorType, visualModel));
        
        /* Guardo que esa door tiene este joint */
        door.setJoint(jointForDoor);
        
        /* Agrego el componente a la lista del pinball */
        pinball.addDoor(doorNode);
        
        /* Para detectar colisiones de objetos */
        final SyntheticButton collisionEventHandler = doorNode.getCollisionEventHandler();
        
        /* Agrego la accion al controlador de pinball */
        pinball.getPinballInputHandler().addAction(new InputAction() {
        	
        	public void performAction(InputActionEvent evt) {
        		
                /* Llamo a la logica del juego */
                pinballInstance.getGameLogic().doorCollision(door);
            }        	

        }, collisionEventHandler, false);
        
        
		return doorNode;
	}
	
	private static Vector3f locateDoorExtreme(DoorType doorType, Geometry visualModel)
	{
		Vector3f extreme = new Vector3f();
		
		BoundingBox bBox = (BoundingBox)visualModel.getWorldBound();
		
		Vector3f extents = bBox.getExtent(null);
		
		/* En Y no va a variar, pues quiero el Y de su centro de masa */
		extreme.setY(bBox.getCenter().getY());

		/* Aproximo la punta de la puerta */
		
		if (doorType == DoorType.RIGHT_DOOR)
		{
			/* Para puertas derechas */
			extreme.setX(bBox.getCenter().getX() + (0.5f - (1 - xExtreme)) * extents.getX());
		}
		else
		{
			/* Para puertas izquierdas */
			extreme.setX(bBox.getCenter().getX() - (0.5f - (1 - xExtreme)) * extents.getX());
		}
		
		extreme.setZ(bBox.getCenter().getZ() + (0.5f - (1 - zExtreme)) * extents.getZ());

		return extreme;
	}
	
	/**
	 * Toma un nombre, el tipo de puerta y su representacion grafica.
	 */
	public Door(String name, Geometry visualModel, DoorType doorType)
	{
		super(name);
		
		attachChild(visualModel);

		this.doorType = doorType;
	}
	
	public void recalculateJoints(Pinball pinball)
	{
		
		Quaternion rot = pinball.getPinballSettings().getInclinationQuaternion();
		
		/* Tomo el angulo de juego e inclino el eje anterior del joint */
		joint.getAxes().get(0).setDirection(joint.getAxes().get(0).getDirection(null).rotate(rot));
		
		/* Tomo la anterior posicion del anchor y la roto */
		joint.setAnchor(joint.getAnchor(null).rotate(rot));
		
		/* Recien ahora attacheo al nodo el joint */
		joint.attach((DynamicPhysicsNode)getParent());
	}
	
	public DoorType getDoorType()
	{
		return doorType;
	}
	
	public boolean isRightDoor()
	{
		return doorType.equals(DoorType.RIGHT_DOOR);
	}
	
	public boolean isLeftDoor()
	{
		return doorType.equals(DoorType.LEFT_DOOR);
	}

	public Joint getJoint()
	{
		return joint;
	}

	public void setJoint(Joint joint)
	{
		this.joint = joint;
	}
}
