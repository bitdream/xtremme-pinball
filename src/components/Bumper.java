package components;

import input.PinballInputHandler;
import mainloop.Pinball;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.input.util.SyntheticButton;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.shape.Sphere;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.Joint;
import com.jmex.physics.PhysicsNode;
import com.jmex.physics.TranslationalJointAxis;
import com.jmex.physics.contact.ContactInfo;
import com.jmex.physics.contact.MutableContactInfo;
import com.jmex.physics.material.Material;


/**
 * Componente bumper. Ejerce una fuerza repulsora cuando la bola golpea sobre el.
 */
public class Bumper extends Node
{
	private static final long serialVersionUID = 1L;
	
	// Tipos de bumpers
	public enum BumperType {JUMPER, NO_JUMPER};
	
	// Tipo de este bumper
	private BumperType bumperType;	
		
	// Pinball en el que esta el bumper
	private static Pinball pinballInstance;
	
	// Intensidad de la fuerza repulsora a aplicar sobre la bola  
	private static float forceToBallIntensity = 900f; //TODO hacer la intensidad de modo que sea suficiente para mover la bola con cualquier angulo de inclinacion permitido! 
	//Magic number probado con angulo de 15º y repele con fuerza
	
	// Valores de densidad, rebote y rozamiento entre el material del bumper y el de la mesa
	
    // Es pesado, pero no tanto como para hundirse en el material de la mesa.
	private static float bumperMaterialDensity = 999;
    // Nada de rebote
	private static float bumperMaterialBounce = 0.0f;
    // Mucho rozamiento
	private static float bumperMaterialMu = 99999999.0f;
	
	// Modelo visual del bumper
	private Geometry visualModel;
	
	/* Joint que lo fija a la mesa */
	private Joint joint;
	
	public static DynamicPhysicsNode create(final Pinball pinball, String name, Geometry visualModel, BumperType bumperType, PinballInputHandler input) //TODO quitar final
	{
		final DynamicPhysicsNode bumperNode = pinball.getPhysicsSpace().createDynamicNode();
		// Nombre del nodo fisico de todos los bumpers
		bumperNode.setName("bumper");
		
		pinballInstance = pinball;		
		
		/* Crear un material que no tenga rebote con el material de la mesa y que su rozamiento sea muy grande. Ademas que sea
		 * muy denso para que la bola no lo tire.
		 */
        final Material customMaterial = buildBumperMaterial("Material de bumper", bumperMaterialDensity, bumperMaterialBounce, bumperMaterialMu);
        
        /* Creo un nodo de Bumper, con todas sus caracteristicas y lo fijo al nodo fisico */
        final Bumper bumper = new Bumper(name, visualModel, bumperType);
        bumperNode.attachChild(bumper);
	
        // Genero su fisica
        bumperNode.generatePhysicsGeometry();
        
        // Setear el material del bumper
		bumperNode.setMaterial(customMaterial);
        
        // Calculo la masa del bumper (solo si lo hago dinamico)
        bumperNode.computeMass();
        
        // Para que el bumper quede pegado a la mesa
        // bumperNode.setAffectedByGravity(false);  		
		
        // Voy a fijar el bumper con un eje translacional 
        final Joint jointForBumper = pinball.getPhysicsSpace().createJoint();
        final TranslationalJointAxis translationalAxis = jointForBumper.createTranslationalAxis();
       
        // Fijo el limite de salto para el bumper 
        translationalAxis.setPositionMinimum(0);
        translationalAxis.setPositionMaximum(0.1f);

        /* Vector que indica la direccion sobre la que se puede mover el bumper. Se calcula en funcion del valor de inclinacion de la mesa */
        //translationalAxis.setDirection(new Vector3f(0, FastMath.cos(FastMath.DEG_TO_RAD * pinballInstance.getPinballSettings().getInclinationAngle()), 
        //		FastMath.sin(FastMath.DEG_TO_RAD * pinballInstance.getPinballSettings().getInclinationAngle())));
        translationalAxis.setDirection(new Vector3f(0,1,0)); //Mesa aun horizontal, en la rotacion se actualizara el eje de movimiento vertical del joint
        
        // Coloco el joint sobre el nodo de bumper 
        //jointForBumper.attach(bumperNode);
        
        // Lo fijo al centro de masa del bumper 
        jointForBumper.setAnchor(visualModel.getLocalTranslation());
        
        // Guardo que ese bumper tiene este joint
        bumper.setJoint(jointForBumper);
        
        
        // Para detectar colisiones de objetos contra los bumpers
        final SyntheticButton collisionEventHandler = bumperNode.getCollisionEventHandler();
        input.addAction( new InputAction(){
        	
        	public void performAction( InputActionEvent evt ) {
        		
        		// Sentido de la fuerza a aplicar sobre la bola
        		int sense = 1;
        		
        		// Algo colisiono con el bumper
                final ContactInfo contactInfo = ( (ContactInfo) evt.getTriggerData() );
                DynamicPhysicsNode ball, bump;

                // El contacto pudo haber sido bola -> bumper o bumper -> bola
                if ( contactInfo.getNode2() instanceof DynamicPhysicsNode && contactInfo.getNode2().getChild(0) instanceof Sphere ) {
                    // fue bumper -> bola
                    ball = (DynamicPhysicsNode) contactInfo.getNode2();
                    bump = (DynamicPhysicsNode) contactInfo.getNode1();
                    sense = 1; //TODO para mi deberia ser -1, pero sino no anda
                    
                   
                    
                    for (PhysicsNode node : pinball.getPhysicsSpace().getNodes())
					{
						if (node instanceof DynamicPhysicsNode && node.getName() != null && node.getName().equals("table"))
						{
							((DynamicPhysicsNode)node).addForce(new Vector3f(10000f, 10000f, 10000f));
						}
					}
                     
                }
                else if ( contactInfo.getNode1() instanceof DynamicPhysicsNode && contactInfo.getNode1().getChild(0) instanceof Sphere ) 
                {
                	// fue bola -> bumper
                    ball = (DynamicPhysicsNode) contactInfo.getNode1();
                    bump = (DynamicPhysicsNode) contactInfo.getNode2();
                    sense = -1;
                }
                else 
                {
                    // Colisiono el bumper contra otra cosa, por ejemplo contra la mesa, lo ignoro
                    return;
                }
                
                //DEBUG
                //System.out.println(" -------------------- " + ball.getName() + " --- " + bump.getName());
                  
                
                /* La fuerza aplicada sobre la bola tiene una intensidad proporcional a la velocidad que la bola tenia al momento de la colision
                 * y es en sentido opuesto.
                 */
                Vector3f direction = contactInfo.getContactVelocity(null); // the velocity with which the two objects hit (in direction 'into' object 1)
                Vector3f appliedForce = new Vector3f(direction.mult(forceToBallIntensity * sense * ball.getMass()));
                
                //System.out.println(" -------------------- velocidad" + direction);
                //System.out.println(" - -------------- fuerza: " + direction.mult(forceToBallIntensity * sense));

                // Aplicar la fuerza repulsora sobre la bola
                ball.clearDynamics();
                ball.addForce( appliedForce );
                
                // Aplicarle fuerza para hacer saltar a aquellos bumpers que sean saltarines (honguitos). La misma debe ser paralela a la mesa, no exclusiva en Y
                if (bumper.getBumperType().equals(BumperType.JUMPER))
                {
                	bump.clearDynamics();
                	bump.addForce (new Vector3f(0, (bump.getMass() * 500f) * FastMath.cos(FastMath.DEG_TO_RAD * pinballInstance.getPinballSettings().getInclinationAngle()),
                								   (bump.getMass() + 500f) * FastMath.sin(FastMath.DEG_TO_RAD * pinballInstance.getPinballSettings().getInclinationAngle())));
                }
                
            }        	

        }, collisionEventHandler, false );
        
    	return bumperNode;
	}
	
	/**
	 *  Crea el material con nombre, densidad, rebote y rozamiento indicados, respecto al material de la mesa del pinball 
	 */
	private static Material buildBumperMaterial(String name, float density, float bounce, float mu) 
	{
		Material material = new Material( name );
		material.setDensity( density );
        // Detalles de contacto con el otro material
        MutableContactInfo contactDetails = new MutableContactInfo();
        contactDetails.setBounce( bounce );
        contactDetails.setMu( mu);
        material.putContactHandlingDetails( Pinball.pinballTableMaterial, contactDetails );
        return material;
	}



	/**
	 * Toma un nombre, el tipo de bumper y su representacion grafica.
	 */
	public Bumper(String name, Geometry visualModel, BumperType bumperType)
	{
		super(name);
		
		this.visualModel = visualModel;
		
		attachChild(visualModel);

		this.bumperType = bumperType;
	}
	
	public void setBumperType(BumperType bumperType) 
	{
		this.bumperType = bumperType;
	}
	
	public BumperType getBumperType()
	{
		return bumperType;
	}
	

	public Joint getJoint() 
	{
		return joint;
	}

	
	public void setJoint(Joint joint) 
	{
		this.joint = joint;
	}
	
	// Rota el joint del bumper. Para ser invocado luego de inclinar la mesa con todos sus componentes.
	public void recalculateJoints(Pinball pinball)
	{
		Quaternion rot = pinball.getPinballSettings().getInclinationQuaternion();

		/* Tomo el angulo de juego e inclino el eje del joint */
		joint.getAxes().get(0).setDirection(new Vector3f(0, 1, 0).rotate(rot));
		
//		joint.getAxes().get(0).setDirection(new Vector3f(0, FastMath.cos(FastMath.DEG_TO_RAD * pinballInstance.getPinballSettings().getInclinationAngle()), 
//		        		FastMath.sin(FastMath.DEG_TO_RAD * pinballInstance.getPinballSettings().getInclinationAngle())));
//		
		
//		/* Le asigno al joint el anchor nuevo en base a la posicion del modelo visual */
//		joint.setAnchor(visualModel.getLocalTranslation().rotate(rot)); // TODO Unirlo en la punta
//		
//		/* Roto el modelo visual como lo deberia haber rotado la rotacion general de la mesa */
//		visualModel.setLocalTranslation(visualModel.getLocalTranslation().rotate(rot));
//		visualModel.setLocalRotation(rot);
		
		/* Tomo la anterior posicion del anchor y la roto */
		joint.setAnchor(joint.getAnchor(null).rotate(rot));
		
		/* Recien ahora attacheo al nodo el joint */
		joint.attach((DynamicPhysicsNode)getParent());
		
	}
	
}
