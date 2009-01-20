package components;

import mainloop.Pinball;

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
	
	/* Tipo de esta puerta */
	private DoorType doorType;
	
	/* Modelo visual de la puerta */
	private Geometry visualModel;

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
		
		/* El material de las puertas es plastico como la mesa */
        doorNode.setMaterial(Material.PLASTIC);
		
        /* Creo un nodo de puerta, con todas sus caracteristicas y lo fijo al nodo fisico */
        Door door = new Door(name, visualModel, doorType);
        doorNode.attachChild(door);
        
        /* Genero su fisica */
        doorNode.generatePhysicsGeometry();
        
        /* Computo su masa */
		doorNode.computeMass();
        
        /* Voy a fijar la puerta con un eje rotacional */
        final Joint jointForDoor = pinball.getPhysicsSpace().createJoint();
        final RotationalJointAxis rotationalAxis = jointForDoor.createRotationalAxis();
        
        /* Maximos angulos de operacion de la puerta */
        if (door.isLeftDoor())
        {// TODO probar y ver
        	rotationalAxis.setPositionMaximum(-maxRotationalAngle);
        	rotationalAxis.setPositionMinimum(-minRotationalAngle);
        }
        else
        {
        	rotationalAxis.setPositionMaximum(maxRotationalAngle);
        	rotationalAxis.setPositionMinimum(minRotationalAngle);
        }
        
        /* Vector que indica la direccion sobre la que esta parado el eje, en este caso, Y */
        rotationalAxis.setDirection(new Vector3f(0, 1, 0)); // TODO se puede calcular con el angulo
        
        /* Coloco el joint sobre el nodo de puerta */
        jointForDoor.attach(doorNode);
        
        /* Le fijo como punto de rotacion la punta de la puerta */
        jointForDoor.setAnchor(new Vector3f(28, 3, 75)); // TODO debe ser la puntita de donde haya quedado el visual visualModel.getLocalTranslation() > sacar la punta
        
        
		return doorNode;
	}
	
	/**
	 * Toma un nombre, el tipo de puerta y su representacion grafica.
	 */
	public Door(String name, Geometry visualModel, DoorType doorType)
	{
		super(name);
		
		this.visualModel = visualModel;
		
		attachChild(visualModel);

		this.doorType = doorType;
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
}
