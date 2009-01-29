package components;

import gamestates.PinballGameState;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.input.util.SyntheticButton;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.Joint;
import com.jmex.physics.TranslationalJointAxis;
import com.jmex.physics.contact.ContactInfo;
import com.jmex.physics.contact.MutableContactInfo;
import com.jmex.physics.material.Material;


/**
 * Componente bumper. Ejerce una fuerza repulsora cuando la bola golpea sobre el.
 */
public class Bumper extends Node implements ActivableComponent
{
	private static final long serialVersionUID = 1L;
	
	// Tipos de bumpers
	public enum BumperType {JUMPER, NO_JUMPER};
	
	// Tipo de este bumper
	private BumperType bumperType;	
		
	// Pinball en el que esta el bumper
	private static PinballGameState pinballInstance;
	
	// Intensidad de la fuerza repulsora a aplicar sobre la bola  
	private static float forceToBallIntensity = 900f; // Grande para que sea un golpe seco y no haya colisiones espureas
	//TODO antes 900f. hacer la intensidad de modo que sea suficiente para mover la bola con cualquier angulo de inclinacion permitido! 
	//Magic number probado con angulo de 15 y repele con fuerza. HACERLO EN FUNCION DEL ANGULO DE INCLINACION DE LA MESA.
	// POSIBLE FORMULA: cte*(max_angle - pinball_angle) -> MAL
	// OTRA OPCION: dejarlo cte al valor, despues de todo es asi en la realidad (fuerza mecanica cte)
	
	// Valores de densidad, rebote y rozamiento entre el material del bumper y el de la mesa	
    // Es pesado, pero no tanto como para hundirse en el material de la mesa.
	private static float bumperMaterialDensity = 999;
    // Nada de rebote
	private static float bumperMaterialBounce = 0.0f;
    // Mucho rozamiento
	private static float bumperMaterialMu = 99999999.0f;
	
	// Joint que lo fija a la mesa
	private Joint joint;
	
	// Esta activo este bumper?
	public boolean active = true;
	
	// Tiempo de la ultima colision considerada (donde se llamo a la logica del juego) entre una bola y este bumper
	private long lastConsideredCollisionTime = 0;
	
	//TODO ver si el tiempo elegido funciona al tener la version final de la mesa
	// Ventana de tiempo dentro de la cual dos colisiones seran consideradas la misma. Medido en mseg
	private static final long windowTimeForCollisions = 100; 
	
	public static DynamicPhysicsNode create(PinballGameState pinball, String name, Geometry visualModel, BumperType bumperType)
	{
		final DynamicPhysicsNode bumperNode = pinball.getPhysicsSpace().createDynamicNode();
		
		// Nombre del nodo fisico de todos los bumpers
		bumperNode.setName("Bumper");
		
		pinballInstance = pinball;	
		
		/* Crear un material que no tenga rebote con el material de la mesa y que su rozamiento sea muy grande. Ademas que sea
		 * muy denso para que la bola no lo tire.
		 */
        final Material customMaterial = buildBumperMaterial("Material de bumper", bumperMaterialDensity, bumperMaterialBounce, bumperMaterialMu);
        
        /* Creo un nodo de Bumper, con todas sus caracteristicas y lo fijo al nodo fisico */
        final Bumper bumper = new Bumper(name, visualModel, bumperType);
        bumperNode.attachChild(bumper);
	
        // Genero su fisica
        bumperNode.generatePhysicsGeometry(true); // Para que use triangulos cuando lo necesitemos
        //PhysicsNode.generatePhysicsGeometry(bumper, bumperNode, true);
        
        // Setear el material del bumper
		bumperNode.setMaterial(customMaterial);
        
        // Calculo la masa del bumper (solo si lo hago dinamico)
        bumperNode.computeMass();
        
       
        // Voy a fijar el bumper con un eje translacional 
        final Joint jointForBumper = pinball.getPhysicsSpace().createJoint();
        final TranslationalJointAxis translationalAxis = jointForBumper.createTranslationalAxis();
       
        // Fijo el limite de salto para el bumper 
        translationalAxis.setPositionMinimum(0);
        translationalAxis.setPositionMaximum(0.1f);

        // Vector que indica la direccion sobre la que se puede mover el bumper. Se calcula en funcion del valor de inclinacion de la mesa
        // Mesa aun horizontal, en la rotacion se actualizara el eje de movimiento vertical del joint
        translationalAxis.setDirection(new Vector3f(0,1,0)); 
        
        // Lo fijo al centro de masa del bumper 
        jointForBumper.setAnchor(visualModel.getLocalTranslation());
        
        // Guardo que ese bumper tiene este joint
        bumper.setJoint(jointForBumper);
        
        
        // Para detectar colisiones de objetos contra los bumpers
        final SyntheticButton collisionEventHandler = bumperNode.getCollisionEventHandler();
        pinball.getPinballInputHandler().addAction( new InputAction(){
        	
        	public void performAction( InputActionEvent evt ) {
        		
        		// Sentido de la fuerza a aplicar sobre la bola
        		int sense = 1;

        		// Algo colisiono con el bumper
                final ContactInfo contactInfo = ( (ContactInfo) evt.getTriggerData() );
                DynamicPhysicsNode ball, bump;

                // El contacto pudo haber sido bola -> bumper o bumper -> bola
                if ( contactInfo.getNode2() instanceof DynamicPhysicsNode && /*contactInfo.getNode2().getChild(0) instanceof Sphere*/ contactInfo.getNode2().getName() != null && contactInfo.getNode2().getName().equals(PinballGameState.PHYSIC_NODE_NAME_FOR_BALLS) ) { 
                    // fue bumper -> bola
                    ball = (DynamicPhysicsNode) contactInfo.getNode2();
                    bump = (DynamicPhysicsNode) contactInfo.getNode1();
                    sense = 1; //TODO para mi deberia ser -1, pero sino no anda    
                }
                else if ( contactInfo.getNode1() instanceof DynamicPhysicsNode && /*contactInfo.getNode1().getChild(0) instanceof Sphere*/ contactInfo.getNode1().getName() != null && contactInfo.getNode1().getName().equals(PinballGameState.PHYSIC_NODE_NAME_FOR_BALLS) ) 
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
                
                // Solo si el bumper esta activo debe ejercer la fuerza sobre la bola y saltar (si es saltarin)
                if (((Bumper)bump.getChild(0)).isActive())
                {
                    /* La fuerza aplicada sobre la bola tiene una intensidad proporcional a la velocidad que la bola tenia al momento de la colision
                     * y es en sentido opuesto.
                     */
                    Vector3f direction = contactInfo.getContactVelocity(null); // the velocity with which the two objects hit (in direction 'into' object 1)
                    Vector3f appliedForce = new Vector3f(direction.mult(forceToBallIntensity * sense * ball.getMass()));

                    // Aplicar la fuerza repulsora sobre la bola
                    ball.clearDynamics();
                    ball.addForce( appliedForce );
                    
                    // Aplicarle fuerza para hacer saltar a aquellos bumpers que sean saltarines (honguitos). La misma debe ser paralela a la mesa, no exclusiva en Y
                    if (bumper.getBumperType().equals(BumperType.JUMPER))
                    {
                    	bump.clearDynamics();
                    	bump.addForce (new Vector3f(0, (bump.getMass() * 500f) * FastMath.cos(FastMath.DEG_TO_RAD * pinballInstance.getPinballSettings().getInclinationAngle()),
                    								   (bump.getMass() * 500f) * FastMath.sin(FastMath.DEG_TO_RAD * pinballInstance.getPinballSettings().getInclinationAngle())));
                    }
                }
                
                // Tiempo en el que se dio esta colision
                long now = System.currentTimeMillis();
                
                // Tiempo de la ultima colision considerada
                long lastColl = bumper.getLastConsideredCollisionTime();
                
                // Si la diferencia con la ultima colision considerada no es menor a 100 ms, la tomo como otra colision
                if (!(lastColl != 0 && now -  lastColl < windowTimeForCollisions))
                {   
                	// Llamo a la logica del juego. Lo hago por mas que el bumper no este activo, ya que ella determinara que hacer.
                    pinballInstance.getGameLogic().bumperCollision(bumper);
                    
                    // Actualizo el tiempo de la ultima colision considerada
                    bumper.setLastConsideredCollisionTime(now);
                }
                // Sino no hago nada pq es una colision repetida               
            }        	

        }, collisionEventHandler, false );
        
        /* Agrego el componente a la lista del pinball */
        pinball.addBumper(bumperNode);
        
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
        material.putContactHandlingDetails( PinballGameState.pinballTableMaterial, contactDetails );
        return material;
	}



	/**
	 * Toma un nombre, el tipo de bumper y su representacion grafica.
	 */
	public Bumper(String name, Geometry visualModel, BumperType bumperType)
	{
		super(name);
		
		//this.visualModel = visualModel;
		
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
	public void recalculateJoints(PinballGameState pinball)
	{
		Quaternion rot = pinball.getPinballSettings().getInclinationQuaternion();

		/* Tomo el angulo de juego e inclino el eje anterior del joint */
		joint.getAxes().get(0).setDirection(joint.getAxes().get(0).getDirection(null).rotate(rot));
		
		// Calculos hechos a mano
//		joint.getAxes().get(0).setDirection(new Vector3f(0, FastMath.cos(FastMath.DEG_TO_RAD * pinballInstance.getPinballSettings().getInclinationAngle()), 
//		        		FastMath.sin(FastMath.DEG_TO_RAD * pinballInstance.getPinballSettings().getInclinationAngle())));
		
		
		/* Tomo la anterior posicion del anchor y la roto */
		joint.setAnchor(joint.getAnchor(null).rotate(rot));
		
		/* Recien ahora attacheo al nodo el joint */
		joint.attach((DynamicPhysicsNode)getParent());
		
	}

	public void setActive(boolean active)
	{
		this.active = active; 
		
	}
	
	public boolean isActive()
	{
		return this.active;
	}
	
	public long getLastConsideredCollisionTime() 
	{
		return lastConsideredCollisionTime;
	}

	public void setLastConsideredCollisionTime(long lastCollisionTime) 
	{
		this.lastConsideredCollisionTime = lastCollisionTime;
	}
	
}
