package components;

import mainloop.Pinball;

import com.jme.math.Vector3f;
import com.jme.scene.*;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.Joint;
import com.jmex.physics.TranslationalJointAxis;
import com.jmex.physics.material.Material;


/**
 * Componente de lanzadera de pelotas.
 */
public class Plunger extends Node
{
	private static final long serialVersionUID = 1L;
	
	/* Fuerza de charge */
	public static final Vector3f plungerChargeForce = new Vector3f(0f, 0f, 500f);
	
	/* Esta suelto o lo esta controlando el usuario */
	private boolean loose;
	
	/* Distancia a su origen */
	private float distance;
	
	/**
	 * Crea un nodo dinamico de plunger.
	 * @param pinball El juego.
	 * @param name Nombre del nodo de plunger.
	 * @param visualModel Modelo visual del plunger.
	 * @param maxBackstep Maxima distancia permitida hacia atras para tomar fuerza.
	 * @return El nodo creado.
	 */
	public static DynamicPhysicsNode create(Pinball pinball, String name, Geometry visualModel, float maxBackstep)
	{
		DynamicPhysicsNode plungerNode = pinball.getPhysicsSpace().createDynamicNode();
		
		/* El material del plunger es de goma para tener un mejor rebote */
        plungerNode.setMaterial(Material.RUBBER);
		
        /* Creo un nodo de Plunger, con todas sus caracteristicas y lo fijo al nodo fisico */
        Plunger plunger = new Plunger(name, visualModel);
        plungerNode.attachChild(plunger);
        
        /* Genero su fisica */
        plungerNode.generatePhysicsGeometry();
        
        /* Computo su masa */
		plungerNode.computeMass();
        
        /* Voy a fijar el plunger con un eje translacional */
        final Joint jointForPlunger = pinball.getPhysicsSpace().createJoint();
        final TranslationalJointAxis translationalAxis = jointForPlunger.createTranslationalAxis();
        
        /* Fijo el limite del plunger */
        translationalAxis.setPositionMinimum(0);
        translationalAxis.setPositionMaximum(maxBackstep);

        /* Vector que indica la direccion sobre la que se puede mover el plunger, el eje Z */
        translationalAxis.setDirection(new Vector3f(0, 0, 1)); // TODO se puede calcular con el angulo
        
        /* Coloco el joint sobre el nodo de plunger */
        jointForPlunger.attach(plungerNode);
        
        /* Lo fijo al centro del plunger */
        jointForPlunger.setAnchor(visualModel.getLocalTranslation());//new Vector3f(25, 3, 90)); // TODO debe ser la puntita de donde haya quedado el visual visualModel.getLocalTranslation() > sacar la punta
        
        
		return plungerNode;
	}
	
	/**
	 * Toma un nombre y su representacion grafica.
	 */
	public Plunger(String name, Geometry visualModel)
	{
		super(name);
		
		attachChild(visualModel);
		
		setLoose(true);
		
		setDistance(0);
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
}
