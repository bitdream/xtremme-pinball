package components;

import gamestates.PinballGameState;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.input.util.SyntheticButton;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.StaticPhysicsNode;
import com.jmex.physics.contact.ContactInfo;
import com.jmex.physics.material.Material;


/**
 * Componente sensor, se usa para detectar el paso de la bola por determinados lugares.
 */
public class Sensor extends Node
{

private static final long serialVersionUID = 1L;
	
	/* Tipos de sensor */
	public enum SensorType {RAMP_SENSOR, LOST_BALL_SENSOR};
	
   /* Tipo de SENSOR */
	private SensorType sensorType;
	
	/* Pinball en el que esta */
	private static PinballGameState pinballInstance;
	
	// Tiempo de la ultima colision considerada (donde se llamo a la logica del juego) entre una bola y este door
	private long lastConsideredCollisionTime = 0;
	
	// TODO ver si el tiempo elegido funciona al tener la version final de la mesa
	// Ventana de tiempo dentro de la cual dos colisiones seran consideradas la misma. Medido en mseg
	// El tiempo debe ser grande pq la cantidad de colisiones detectadas depende de la velocidad de la bola
	private static final long windowTimeForCollisions = 5000; 
	

	/**
     * Crea un nodo dinamico que actua de sensor. Con estatico no funciona!
	 * @param pinball El juego.
	 * @param name Nombre del nodo de molinete.
	 * @param visualModel Modelo visual del sensor (si se desea se lo hace transparente).
	 * @return El nodo creado.
     */
	public static StaticPhysicsNode create(PinballGameState pinball, String name, Geometry visualModel, SensorType sensorType)
	{
		StaticPhysicsNode sensorNode = pinball.getPhysicsSpace().createStaticNode();
		
		sensorNode.setName("Sensor");
		
		pinballInstance = pinball;
		
        /* Creo un nodo de sensor, con todas sus caracteristicas y lo fijo al nodo fisico */
        final Sensor sensor = new Sensor(name, visualModel);
        sensorNode.attachChild(sensor);
        
        /* Le asigno el tipo */
        sensor.setSensorType(sensorType);
        
        /* Genero su fisica */
		sensorNode.generatePhysicsGeometry(true);
		
		/* El material de los sensores es ghost */
		sensorNode.setMaterial(Material.GHOST);

		/* Agrego el componente a la lista del pinball */
        pinball.addSensor(sensorNode);
        
		// Detectar colisiones contra el sensor y actuar en consecuencia
		final SyntheticButton collisionEventHandler = sensorNode.getCollisionEventHandler();
        pinball.getPinballInputHandler().addAction( new InputAction(){
        	
        	public void performAction( InputActionEvent evt ) 
        	{
        		
        		final ContactInfo contactInfo = ( (ContactInfo) evt.getTriggerData() );
                DynamicPhysicsNode ball;
                //StaticPhysicsNode s;

                // El contacto pudo haber sido bola -> sensor o sensor -> bola
                if ( contactInfo.getNode2() instanceof DynamicPhysicsNode && contactInfo.getNode2().getName() != null && contactInfo.getNode2().getName().equals(PinballGameState.PHYSIC_NODE_NAME_FOR_BALLS) /*&&
                		contactInfo.getNode1() instanceof StaticPhysicsNode && contactInfo.getNode1().getName() != null && contactInfo.getNode1().getName().equals("SensorPerder")*/) 
                { 
                    // fue sensor -> bola
                    ball = (DynamicPhysicsNode) contactInfo.getNode2();  
                    //s = (StaticPhysicsNode) contactInfo.getNode1();  
                }
                else if ( contactInfo.getNode1() instanceof DynamicPhysicsNode && contactInfo.getNode1().getName() != null && contactInfo.getNode1().getName().equals(PinballGameState.PHYSIC_NODE_NAME_FOR_BALLS) /*&&
                		     contactInfo.getNode2() instanceof StaticPhysicsNode && contactInfo.getNode2().getName() != null && contactInfo.getNode2().getName().equals("SensorPerder")*/) 
                {
                	// fue bola -> sensor
                    ball = (DynamicPhysicsNode) contactInfo.getNode1();  
                    //s = (StaticPhysicsNode) contactInfo.getNode2();  
                }
                else 
                {
                    // Colisiono el sensor contra otra cosa, por ejemplo contra la mesa, lo ignoro
                    return;
                }
                
                // Tiempo en el que se dio esta colision
                long now = System.currentTimeMillis();
                
                // Tiempo de la ultima colision considerada
                long lastColl = sensor.getLastConsideredCollisionTime();
                
                // Si la diferencia con la ultima colision considerada no es menor a windowTimeForCollisions ms, la tomo como otra colision
                if (!(lastColl != 0 && now -  lastColl < windowTimeForCollisions))
                {   
                    // TODO Para debug, quitar
            		//System.out.println("2- Colision entre " + ball.getChild(0).getName() + " y " + s.getName());
            		
            		// Dependiendo del tipo de sensor, se llama a metodos diferentes de la logica del juego
            		if (sensor.getSensorType() == SensorType.LOST_BALL_SENSOR)
            		{    		
            			pinballInstance.getGameLogic().lostBall(ball);
            		}
            		else 
            		{
            			pinballInstance.getGameLogic().sensorRampCollision();
            		}
            		
                    sensor.setLastConsideredCollisionTime(now);
                }
                // Sino no hago nada pq es una colision repetida       		
        	}
        }, collisionEventHandler, false );
		
		return sensorNode;
	}
	
	
	/**
	 * Toma un nombre y su representacion grafica.
	 */
	public Sensor(String name, Geometry visualModel)
	{
		super(name);
		
		attachChild(visualModel);
	}

	public SensorType getSensorType() 
	{
		return sensorType;
	}

	public void setSensorType(SensorType sensorType) 
	{
		this.sensorType = sensorType;
	}
	
    public long getLastConsideredCollisionTime() 
    {
		return lastConsideredCollisionTime;
	}

	public void setLastConsideredCollisionTime(long lastConsideredCollisionTime) 
	{
		this.lastConsideredCollisionTime = lastConsideredCollisionTime;
	}
}
