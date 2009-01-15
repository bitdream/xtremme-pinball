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

	/* Tipos de flipper */
	public enum FlipperType {LEFT_FLIPPER, RIGHT_FLIPPER};
	
	/* Tipo de este flipper */
	private FlipperType flipperType;

    
	public static DynamicPhysicsNode create(Pinball pinball, String name, Geometry visualModel, FlipperType flipperType)
	{
		DynamicPhysicsNode flipperNode = pinball.getPhysicsSpace().createDynamicNode();
		
		// Defino un materia personalizado para poder setear las propiedades de interaccion con la mesa de plastico
        //final Material customMaterial = new Material( "material de bola" );
        // Es pesado
        //customMaterial.setDensity( 100.0f );
        // Detalles de contacto con el otro material
        //MutableContactInfo contactDetails = new MutableContactInfo();
        // Poco rebote
        //contactDetails.setBounce( 0.5f );
        // Poco rozamiento
        //contactDetails.setMu( 0.5f );
        //customMaterial.putContactHandlingDetails( Material.PLASTIC, contactDetails );
        
		/* El material de los flippers es goma para simular la banda de goma que los rodea */
        //TODO flipperNode.setMaterial(Material.RUBBER);
		
        /* Creo un nodo de Flipper, con todas sus caracteristicas y lo fijo al nodo fisico */
        flipperNode.attachChild(new Flipper(name, visualModel, flipperType));
        
        /* Voy a fijar el flipper con un eje a la mesa */
        /*final Joint jointForFlipper = pinball.getPhysicsSpace().createJoint();
        final RotationalJointAxis rotationalAxis = jointForFlipper.createRotationalAxis();
        rotationalAxis.setDirection(new Vector3f(0, 1, 0)); // se puede calcular con el angulo
        jointForFlipper.attach(flipperNode);
        jointForFlipper.setAnchor(new Vector3f(10, 2, 40)); // debe ser la puntita de donde haya quedado el visual
*/
        /* Genero su fisica */
		flipperNode.generatePhysicsGeometry();
		
		/* Computo su masa */
		flipperNode.computeMass();
		
		
		return flipperNode;
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
    
    /**
     * Actualizo el estado de su golpe y a su controlador
     */
    public void update(float time)
    {
    	// TODO 
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
}
