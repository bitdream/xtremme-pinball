package components;

import input.PinballInputHandler;
import mainloop.Pinball;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.input.util.SyntheticButton;
import com.jme.math.Vector3f;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.shape.Sphere;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.StaticPhysicsNode;
import com.jmex.physics.contact.ContactInfo;
import com.jmex.physics.contact.MutableContactInfo;
import com.jmex.physics.material.Material;


/**
 * Componente bumper. Ejerce una fuerza repulsora cuando la bola golpea sobre el.
 */
public class Bumper extends Node
{
	private static final long serialVersionUID = 1L;
	
	//TODO quitar en caso de que queden estaticos. Hacer que salten ante una colision me complico mucho y no lo pude hacer andar
	// no pude contrarrestar el efecto de la gravedad (se iba para abajo el bumper y si le ponia mucha masa, no lo podia hacer saltar)
	// Hacerlo saltar tmb produjo problemas, si bien la fuerza aplicada era perpendicular a la mesa, obviamente, el salto no era en el lugar y hacia 
	// que el bumper se mOviera hacia abajo del plano inclinado.
	// CONCLUSION: FLOR DE QUILOMBO HACERLO DINAMICO. OPTE POR DEJARLO ESTATICO DESPUES DE VARIAS HORAS DE LUCHA
	// Tipos de bumpers
	public enum BumperType {JUMPER, NO_JUMPER};
	// Tipo de este bumper
	//private BumperType bumperType;
	
		
	// Pinball en el que esta el flipper
	//private static Pinball pinballInstance;
	
	// Intensidad de la fuerza repulsora a aplicar sobre la bola  
	private static float forceToBallIntensity = 900f; //TODO hacer la intensidad de modo que sea suficiente para mover la bola con cualquier angulo de inclinacion permitido! 
	//Magic number probado con angulo de 15º y repele con fuerza
	
	// Valores de densidad, rebote y rozamiento entre el material del bumper y el de la mesa
	
    // Es muy pesado
	private static float bumperMaterialDensity = 99999999999999.0f;
    // Nada de rebote
	private static float bumperMaterialBounce = 0.0f;
    // Mucho rozamiento
	private static float bumperMaterialMu = 99999999.0f;
	
	// Modelo visual del bumper
	//private Geometry visualModel;
	
	public static StaticPhysicsNode create(Pinball pinball, String name, Geometry visualModel, BumperType bumperType, PinballInputHandler input)
	{
		final StaticPhysicsNode bumperNode = pinball.getPhysicsSpace().createStaticNode();
		// Nombre del nodo fisico de todos los bumpers
		bumperNode.setName("bumper");
		//pinballInstance = pinball;
		
		
		/* Crear un material que no tenga rebote con el material de la mesa y que su rozamiento sea muy grande. Ademas que sea
		 * muy denso para que la bola no lo tire.
		 */
        final Material customMaterial = buildBumperMaterial("Material de bumper", bumperMaterialDensity, bumperMaterialBounce, bumperMaterialMu);
        
        /* Creo un nodo de Bumper, con todas sus caracteristicas y lo fijo al nodo fisico */
        final Bumper bumper = new Bumper(name, visualModel/*, bumperType*/);
        bumperNode.attachChild(bumper);
	
        // Genero su fisica
        bumperNode.generatePhysicsGeometry();
        
        // Setear el material del bumper
		bumperNode.setMaterial(customMaterial);
        
        // Calculo la masa del bumper (solo si lo hago dinamico)
        //bumperNode.computeMass();
        
        // Para que el bumper quede pegado a la mesa
        //bumperNode.setAffectedByGravity(false);  
        //bumperNode.rest();

        
        // Para detectar colisiones de objetos contra los bumpers
        final SyntheticButton collisionEventHandler = bumperNode.getCollisionEventHandler();
        input.addAction( new InputAction(){
        	
        	public void performAction( InputActionEvent evt ) {
        		
        		// Sentido de la fuerza a aplicar sobre la bola
        		int sense = 1;
        		
        		// Algo colisiono con el bumper
                final ContactInfo contactInfo = ( (ContactInfo) evt.getTriggerData() );
                DynamicPhysicsNode ball;//, bump;
                //StaticPhysicsNode bump;
                // El contacto pudo haber sido bola -> bumper o bumper -> bola
                if ( contactInfo.getNode2() instanceof DynamicPhysicsNode && contactInfo.getNode2().getChild(0) instanceof Sphere ) {
                    // fue bumper -> bola
                    ball = (DynamicPhysicsNode) contactInfo.getNode2();
                    //bump = (DynamicPhysicsNode) contactInfo.getNode1();
                    //bump = (StaticPhysicsNode) contactInfo.getNode1();
                    sense = 1; //TODO para mi deberia ser -1, pero sino no anda
                    //System.out.println(" -------------------- 1");
                     
                }
                else if ( contactInfo.getNode1() instanceof DynamicPhysicsNode && contactInfo.getNode1().getChild(0) instanceof Sphere ) {
                	// fue bola -> bumper
                    ball = (DynamicPhysicsNode) contactInfo.getNode1();
                    //bump = (DynamicPhysicsNode) contactInfo.getNode2();
                    //bump = (StaticPhysicsNode) contactInfo.getNode2();
                    sense = -1; //TODO para mi deberia ser 1, pero sino no anda
                    //System.out.println(" -------------------- 2");
                }
                else {
                	System.out.println("PROBLEMAS, entro en el else de Bumper.create()!!!");
                    // Colisiono el bumper contra otra cosa, no debe suceder, pero lo ignoro
                    return;
                }
                
                //DEBUG
                //System.out.println(" -------------------- " + ball.getName() + " --- " + bump.getName());
                               
                // La fuerza aplicada sobre la bola tiene una intensidad proporcional a la velocidad que la bola tenia al momento de la colision
                // y es en sentido opuesto.
                Vector3f direction = contactInfo.getContactVelocity(null); // the velocity with which the two objects hit (in direction 'into' object 1)
                //System.out.println(" -------------------- velocidad" + direction);
                //System.out.println(" - -------------- fuerza: " + direction.mult(forceToBallIntensity * sense));
                Vector3f appliedForce = new Vector3f(direction.mult(forceToBallIntensity * sense * ball.getMass()));

                // Aplicar la fuerza repulsora sobre la bola
                ball.clearDynamics();
                ball.addForce( appliedForce );
                
                // inventar algo mejor para hacerlo saltar pq creo q hacerrlo staticNode es lo mas facil y que funciona!
                // TODO Aplicarle fuerza grande para hacer saltar a aquellos bumpers que sean saltarines (honguitos)
                // LA FUERZA DEBE SER PARALELA A LA MESA, NO EXCLUSIVA EN Y!!!
//                if (bumper.getBumperType().equals(BumperType.JUMPER))
//                {
//                	bump.clearDynamics();
//                	bump.addForce (new Vector3f(0, (bump.getMass() * 10f) * FastMath.cos(FastMath.DEG_TO_RAD * pinballInstance.getPinballSettings().getInclinationAngle()),
//                								   (bump.getMass() + 999999999999f) * FastMath.cos(FastMath.DEG_TO_RAD * pinballInstance.getPinballSettings().getInclinationAngle())));
//                }

                
                //TODO falta hacer que se evite la aplicacion de la fuerza debido al choque de la bola! -> mucha masa en el bumper
                
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
	public Bumper(String name, Geometry visualModel/*, BumperType bumperType*/)
	{
		super(name);
		
		//this.visualModel = visualModel;
		
		attachChild(visualModel);

		//this.bumperType = bumperType;
	}
	
//	public void setBumperType(BumperType bumperType) 
//	{
//		this.bumperType = bumperType;
//	}
//	
//	public BumperType getBumperType()
//	{
//		return bumperType;
//	}
}
