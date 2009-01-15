package components;

import mainloop.Pinball;

import com.jme.scene.*;
import com.jmex.physics.DynamicPhysicsNode;
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
        flipperNode.setMaterial(Material.RUBBER);
		
        /* Creo un nodo de Flipper, con todas sus caracteristicas y lo fijo al nodo fisico */
        flipperNode.attachChild(new Flipper(name, visualModel, flipperType));
        
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
}
