package components;

import mainloop.Pinball;

import com.jme.math.Vector3f;
import com.jme.scene.*;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.Joint;
import com.jmex.physics.RotationalJointAxis;
import com.jmex.physics.material.Material;


/**
 * Componente de flipper, para golpear la pelotita.
 */
public class Flipper extends Node
{
	private static final long serialVersionUID = 1L;
	
	/* Fuerzas en los flippers */
	public static final Vector3f flipperHitForce = new Vector3f(0f, 0f, -16000000f), flipperRestoreForce = new Vector3f(0f, 0f, 1000000f);
	
	private static final float maxRotationalAngle = 0.4f, minRotationalAngle = -0.4f;

	/* Tipos de flipper */
	public enum FlipperType {LEFT_FLIPPER, RIGHT_FLIPPER};
	
	/* Tipo de este flipper */
	private FlipperType flipperType;
	
	/* Modelo visual del flipper */
	private Geometry visualModel;
	
	/* Joint que lo fija a la mesa */
	private Joint joint;

    /**
     * Crea un nodo dinamico de flipper.
	 * @param pinball El juego.
	 * @param name Nombre del nodo de flipper.
	 * @param visualModel Modelo visual del flipper.
	 * @param flipperType El tipo de flipper deseado, iquierdo o derecho.
	 * @return El nodo creado.
     */
	public static DynamicPhysicsNode create(Pinball pinball, String name, Geometry visualModel, FlipperType flipperType)
	{
		DynamicPhysicsNode flipperNode = pinball.getPhysicsSpace().createDynamicNode();
		
		/* El material de los flippers es goma para simular la banda de goma que los rodea */
        flipperNode.setMaterial(Material.RUBBER);
		
        /* Creo un nodo de Flipper, con todas sus caracteristicas y lo fijo al nodo fisico */
        Flipper flipper = new Flipper(name, visualModel, flipperType);
        flipperNode.attachChild(flipper);
        
        /* Genero su fisica */
        flipperNode.generatePhysicsGeometry();
        
        /* Computo su masa */
		flipperNode.computeMass();
        
        /* Voy a fijar el flipper con un eje rotacional */
        final Joint jointForFlipper = pinball.getPhysicsSpace().createJoint();
        final RotationalJointAxis rotationalAxis = jointForFlipper.createRotationalAxis();
        
        /* Maximos angulos de operacion de los flippers */
        if (flipper.isLeftFlipper())
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
        
        /* Coloco el joint sobre el nodo de flipper */
        jointForFlipper.attach(flipperNode);
        
        /* Le fijo como punto de rotacion la punta del flipper */
        jointForFlipper.setAnchor(new Vector3f(14, 3, 60)); // TODO debe ser la puntita de donde haya quedado el visual visualModel.getLocalTranslation() > sacar la punta
        
        /* Guardo que ese flipper tiene este joint */
        flipper.setJoint(jointForFlipper);
        
        
		return flipperNode;
	}
	
	/**
	 * Toma un nombre, el tipo de flipper y su representacion grafica.
	 */
	public Flipper(String name, Geometry visualModel, FlipperType flipperType)
	{
		super(name);
		
		this.visualModel = visualModel;
		
		attachChild(visualModel);

		this.flipperType = flipperType;
	}
	
	public void recalculateJoints()
	{
		/* Le asigno al joint el anchor nuevo en base a la posicion del modelo visual */
		joint.setAnchor(visualModel.getLocalTranslation()); // TODO puntita!
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
}
