package components;

import mainloop.Pinball;

import com.jme.bounding.BoundingBox;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.TransformMatrix;
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
	
	
	public static Vector3f rotateVector3f(Vector3f anchor, Quaternion rotaa)
    {
		Quaternion rot = new Quaternion();
		
		/* Se rota toda la mesa y sus componentes en el eje X */
		rot.fromAngles(FastMath.DEG_TO_RAD * 15f, 0f, 0f);
		
		System.out.println(anchor);
			Vector3f copia = new Vector3f(anchor);
            TransformMatrix matrix = new TransformMatrix(rot, Vector3f.ZERO);
            matrix.multPoint(copia);
            System.out.println(copia);
            return copia;
    }

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
		
		flipperNode.setName("Flipper");
		
		/* Actualizo los vectores globales */
		flipperNode.updateWorldVectors();
		
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
        rotationalAxis.setPositionMaximum(maxRotationalAngle);
        rotationalAxis.setPositionMinimum(minRotationalAngle);
        
        /* Vector que indica la direccion sobre la que esta parado el eje, en este caso, Y */
        rotationalAxis.setDirection(new Vector3f(0, 1, 0));
        
        /* Le fijo como punto de rotacion la punta del flipper */
        jointForFlipper.setAnchor(locateFlipperExtreme(flipperType, visualModel));
        
        /* Guardo que ese flipper tiene este joint */
        flipper.setJoint(jointForFlipper);
        
        
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
		
		/* Tomo el angulo de juego e inclino el eje del joint, que es en Y */
		joint.getAxes().get(0).setDirection(new Vector3f(0, 1, 0).rotate(rot));
		
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
}
