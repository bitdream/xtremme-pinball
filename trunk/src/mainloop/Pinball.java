package mainloop;

import gamelogic.GameLogic;
import gamestates.PhysicsEnhancedGameState;
import input.PinballInputHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

import themes.CarsThemeGameLogic;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Spatial;
import com.jme.scene.Text;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.util.Timer;
import com.jmetest.physics.Utils;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.PhysicsSpace;
import com.jmex.physics.PhysicsUpdateCallback;
import com.jmex.physics.StaticPhysicsNode;
import com.jmex.physics.contact.MutableContactInfo;
import com.jmex.physics.material.Material;

import components.Bumper;
import components.Door;
import components.Flipper;
import components.Magnet;
import components.Plunger;
import components.Spinner;
import components.Bumper.BumperType;
import components.Flipper.FlipperType;
import components.Spinner.SpinnerType;

/**
 * Clase principal del juego.
 *
 */
public class Pinball extends PhysicsEnhancedGameState
{
	
	public static final Material pinballTableMaterial = Material.PLASTIC;
	
	// Nombre a usar en el nodo fisico de todas las bolas del juego. Es para reconocer las bolas en las colisiones	
	public static final String PHYSIC_NODE_NAME_FOR_BALLS = "ball";
	
	private static final String GAME_NAME = "xtremme pinball";
	private static final String GAME_VERSION = "0.4";
	
	/* Logger de la clase Pinball */
    private static final Logger logger = Logger.getLogger(Pinball.class.getName());
    
	/* InputHandler para el pinball */
	private PinballInputHandler pinballInputHandler;
	
	/* Lista de los flippers del juego actual */
	private List<DynamicPhysicsNode> flippers;
	
	/* Lista de los bumpers del juego actual */
	private List<DynamicPhysicsNode> bumpers;
	
	/* Lista de las puertas del juego actual */
	private List<DynamicPhysicsNode> doors;
	
	/* Lista de los molinetes del juego actual */
	private List<DynamicPhysicsNode> spinners;
	
	/* Lista de los imanes del juego actual */
	private List<StaticPhysicsNode> magnets;
	
	/* Bolas del juego */
	private List<DynamicPhysicsNode> balls;
	
	/* Plunger del juego */
	private DynamicPhysicsNode plunger;
	
	/* Configuracion del juego */
	private PinballSettings pinballSettings;
	
	/* Score del juego */
	private int score = 0;
	
	/* Texto con el score para mostrar al usuario */
	private Text scoreText;
	
	/* Mensaje al usuario */
	private String message = "Mensaje inicial";
	
	/* Mensaje para mostrar al usuario */
	private Text messageText;
	
	/* Logica de juego */
	private GameLogic gameLogic;
	
	/* Timer para los FPS */
	protected Timer timer;

	
	/**
	 * Crea un estado de juego nuevo.
	 * @param name Nombre del estado de juego.
	 * @param pinballSettings Settings del pinball a iniciar.
	 */
	public Pinball(String name, PinballSettings pinballSettings)
	{
		super(name);
		
		this.pinballSettings = pinballSettings;
	
		/* Adquiero el timer para calcular los FPS */
		timer = Timer.getTimer();
		
		/* Inicializo la camara, el display y los handlers de input */
		initSystem();
		
		/* Inicializo el juego en si */
		initGame();
	}
		
	/**
	 * Se debe actualizar la fisica, el puntaje, el tiempo, todo lo relacionado al juego en si.
	 */
	@Override
	public void update(float tpf)
	{
		// TODO No deberiamos estar acelerando la fisica, pero bueno, aca esta la llamada, la tenemos 3 veces mas rapida.
		//super.update(tpf);
		super.update(tpf * 3f);

		/* Actualizo el timer */
		timer.update();
		
		/* Pido al timer el tiempo transcurrido desde la ultima llamada */
		float interpolation = timer.getTimePerFrame();
		
		/* Actualizo el controlador de input */
        pinballInputHandler.update(interpolation);
        
        /* Actualizo los componentes que asi lo requieren */
        updateComponents(interpolation);

		/* Se modifico la escena, entonces actualizo el grafo */
        rootNode.updateGeometricState(interpolation, true);        

        /* Se actualiza la info que se presenta en pantalla (score y mensajes) */
        scoreText.getText().replace(0, scoreText.getText().length(), "Score: " + score);
        messageText.getText().replace(0, messageText.getText().length(), "" + message);
	}
	
	/**
	 * Se debe redibujar la escena
	 */
	@Override
	public void render(float tpf)
	{
		super.render(tpf);
	}
	
	/**
	 * Inicializar el display, los input handlers y la camara
	 */
	protected void initSystem()
	{
		/* Fijo el nombre a la ventana */
		display.setTitle(GAME_NAME + " v" + GAME_VERSION);
		
		/* Inicializo la camara */
		
		/* Perspectiva y FOV */
		cam.setFrustumPerspective(45.0f, (float)pinballSettings.getWidth() / (float)pinballSettings.getHeight(), 1, 1000);
		
		/* Ubicacion */
		Vector3f loc = new Vector3f(0.0f, 4f, 200f);
		Vector3f left = new Vector3f(-1.0f, 0.0f, 0.0f);
		Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
		Vector3f dir = new Vector3f(0.0f, 0f, -0.5f);

		cam.setFrame(loc, left, up, dir);

		/* Aplicar los cambios a la camara */
		cam.update();

	    /* Fijo la camara al display */
		display.getRenderer().setCamera(cam);
		
		/* Creo el input handler del pinball */
		pinballInputHandler = new PinballInputHandler(this);
	
		/* Quiero que ese input handler sea leido en cada paso que haga el motor de fisica,
		 * de modo tal que las fuerzas que acciones continuas apliquen (ej: plunger) se realicen */
		getPhysicsSpace().addToUpdateCallbacks(new PhysicsUpdateCallback() {
			
            public void beforeStep(PhysicsSpace space, float time)
            {
            	pinballInputHandler.update(time);
            }
            
            public void afterStep(PhysicsSpace space, float time)
            {
            }
        });
		
		/* Precision fisica */
		getPhysicsSpace().setAccuracy(0.001f);
	}

	/**
	 * Inicializar la escena
	 */
	protected void initGame()
	{
	    /* Optimizacion - aplico culling a todos los nodos */
        CullState cs = display.getRenderer().createCullState();
        cs.setCullFace(CullState.Face.Back);
        rootNode.setRenderState(cs);

		/* Creo la lista de flippers */
		flippers = new ArrayList<DynamicPhysicsNode>(4);
		
		/* Creo la lista de bumpers */
		bumpers = new ArrayList<DynamicPhysicsNode>(4);
		
		/* Creo la lista de puertas */
		doors = new ArrayList<DynamicPhysicsNode>(2);
		
		/* Creo la lista de molinetes */
		spinners = new ArrayList<DynamicPhysicsNode>(4);
		
		/* Creo la lista de imanes */
		magnets = new ArrayList<StaticPhysicsNode>(2);
		
		/* Creo la lista de bolas */
		balls = new ArrayList<DynamicPhysicsNode>(4);

        /* Armo la mesa de juego */
        buildTable();
        
        // TODO armar instancia del loader con el x3d elegido (inicialmente habra 1 solo) y
        // preguntar el theme para ahora instanciarlo y asignarlo a la variable gameLogic
        gameLogic = new CarsThemeGameLogic(this);
        
		// TODO Aca deberia ir la traduccion de X3D para formar la escena
        buildAndAttachComponents();
        
        buildLighting();

        /* Inclino todos los componentes a la vez desde el nodo raiz */
        inclinePinball();
        
		/* Actualizo el nodo raiz */
		rootNode.updateGeometricState(0.0f, true);
		rootNode.updateRenderState();
		
		// Cateles con el puntaje y los mensajes al usuario
        scoreText = Text.createDefaultTextLabel("scoreText", "Score: " + String.valueOf(score));
        scoreText.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        scoreText.setLightCombineMode(Spatial.LightCombineMode.Off);
        scoreText.setLocalTranslation(new Vector3f(display.getWidth()* 3/4, 5, 1));
        rootNode.attachChild(scoreText);
        
        messageText = Text.createDefaultTextLabel("messageText", message);
        messageText.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        messageText.setLightCombineMode(Spatial.LightCombineMode.Off);
        messageText.setLocalTranslation(new Vector3f(display.getWidth()/4, 5, 1));
        rootNode.attachChild(messageText);
	}
	
	private void buildLighting()
	{
		PointLight light = new PointLight();
		light.setDiffuse( new ColorRGBA( 0.75f, 0.75f, 0.75f, 0.75f ) );
	    light.setAmbient( new ColorRGBA( 0.5f, 0.5f, 0.5f, 1.0f ) );
	    light.setLocation( new Vector3f( 100, 100, 100 ) );
	    light.setEnabled( true );
	    
	    /** Attach the light to a lightState and the lightState to rootNode. */
	    LightState lightState = display.getRenderer().createLightState();
	    lightState.setEnabled( true );
	    lightState.attach( light );
	    
	    rootNode.setRenderState( lightState );
	}
	
	private void updateComponents(float interpolation)
	{
		/* Flippers */
		for (DynamicPhysicsNode flipper : getFlippers())
		{
			((Flipper)flipper.getChild(0)).update(interpolation);
		}
		
		/* Plunger */
		if (plunger != null)
			((Plunger)plunger.getChild(0)).update(interpolation);
		
		/* Spinners */
		for (DynamicPhysicsNode spinner : getSpinners())
		{
			((Spinner)spinner.getChild(0)).update(interpolation);
		}
	}

	private void inclinePinball()
	{
		/* Se rota toda la mesa y sus componentes en el eje X */
		rootNode.setLocalRotation(getPinballSettings().getInclinationQuaternion());
		
		/* Inclino joints fisicos y demas, recalculandolos */
		/* Flippers */
		for (DynamicPhysicsNode flipper : getFlippers())
		{
			((Flipper)flipper.getChild(0)).recalculateJoints(this);
		}
		/* Bumpers */
		for (DynamicPhysicsNode bumper : getBumpers())
		{
			((Bumper)bumper.getChild(0)).recalculateJoints(this);
//			BoundingVolume b;
//			Quaternion q = new Quaternion();
//			q.fromAngles(FastMath.DEG_TO_RAD * 15f, 0f, 0f);
//			b = ((Geometry)((Node)bumper.getChild(0)).getChild(0)).getModelBound().transform(/*getPinballSettings().getInclinationQuaternion()*/ q, new Vector3f(0,0,0), new Vector3f(1,1,1)); //TODO ver esto 
//			((Geometry)((Node)bumper.getChild(0)).getChild(0)).setModelBound(b);
//			((Geometry)((Node)bumper.getChild(0)).getChild(0)).updateModelBound();
//			
//			System.out.println( ((Node)bumper.getChild(0)).getChild(0).getClass().getName()  + "   -------    " + b.distanceTo(new Vector3f(1,1,1)) + "  ----   " + q.getRotationColumn(1));
		}
		/* Doors */
		for (DynamicPhysicsNode door : getDoors())
		{
			((Door)door.getChild(0)).recalculateJoints(this);
		}
		/* Spinners */
		for (DynamicPhysicsNode spinner : getSpinners())
		{
			((Spinner)spinner.getChild(0)).recalculateJoints(this);
		}
		/* Plunger */
		if (plunger != null)
			((Plunger)plunger.getChild(0)).recalculateJoints(this);
			
	}

	/**
	 * Se la llama para limpiar el juego una vez finalizado
	 */
    @Override
	public void cleanup()
	{
		super.cleanup();
		
		/* Limpieza de texturas */
		//TODO ts.deleteAll();
		
		/* Limpieza del mouse */
		MouseInput.get().removeListeners();
		MouseInput.destroyIfInitalized();
		
		/* Limpieza del teclado */
		KeyInput.destroyIfInitalized();
	}
	
	private void buildAndAttachComponents()
	{// TODO super temporal, esto vendria del X3d. Ahora hay que meterle nodos fisicos!!!

		logger.info("Construyendo componentes");
		
		// Defino un materia personalizado para poder setear las propiedades de interaccion con la mesa
        final Material customMaterial = new Material( "material de bola" );
        // Es pesado
        customMaterial.setDensity( 100.0f );
        // Detalles de contacto con el otro material
        MutableContactInfo contactDetails = new MutableContactInfo();
        // Poco rebote
        contactDetails.setBounce( 0.5f );
        // Poco rozamiento
        contactDetails.setMu( 0.5f );
        customMaterial.putContactHandlingDetails( pinballTableMaterial, contactDetails );
        
        
		// TODO ver donde poner esta creacion de la bola
		/* Nodo dinamico de la bola */
		DynamicPhysicsNode mainBall = getPhysicsSpace().createDynamicNode();
		// El nodo fisico de todas las bolas debera llamarse "ball" -> POR CONVENCION!
		mainBall.setName("ball");
        rootNode.attachChild(mainBall);
               
        final Sphere visualMainBall = new Sphere("Bola principal", 25, 25, 1);
		visualMainBall.setLocalTranslation(new Vector3f(-5, 20, -60));
		
		// Agregado de bounding volume 
		visualMainBall.setModelBound(new BoundingSphere());
		visualMainBall.updateModelBound();

		mainBall.attachChild(visualMainBall);
		mainBall.generatePhysicsGeometry();
		mainBall.setMaterial(customMaterial);		
		// Se computa la masa luego de generar la geometria fisica
		mainBall.computeMass();
		
		// La agrego a la lista de bolas
		balls.add(mainBall);
		
		// Una segunda bola para probar el bumper
		/* Nodo dinamico de la bola */
		final DynamicPhysicsNode mainBall2 = getPhysicsSpace().createDynamicNode(); 
		mainBall2.setName("ball");
        rootNode.attachChild(mainBall2);
               
        final Sphere visualMainBall2 = new Sphere("Bola 2", 25, 25, 1);
        visualMainBall2.setLocalTranslation(new Vector3f(25, 15, -2));
		
		// Agregado de bounding volume 
		visualMainBall2.setModelBound(new BoundingSphere());
		visualMainBall2.updateModelBound();

		mainBall2.attachChild(visualMainBall2);
		mainBall2.generatePhysicsGeometry();
		mainBall2.setMaterial(customMaterial);		
		// Se computa la masa luego de generar la geometria fisica
		mainBall2.computeMass();
		
		// La agrego a la lista de bolas
		balls.add(mainBall2);
		
		// Una tercera bola para probar el iman magnet1
		/* Nodo dinamico de la bola */
		final DynamicPhysicsNode mainBall3 = getPhysicsSpace().createDynamicNode();
		mainBall3.setName("ball");
        rootNode.attachChild(mainBall3);
               
        final Sphere visualMainBall3 = new Sphere("Bola 3", 25, 25, 1);
        visualMainBall3.setLocalTranslation(new Vector3f(-10, 5, -10));
		
		// Agregado de bounding volume 
        visualMainBall3.setModelBound(new BoundingSphere());
        visualMainBall3.updateModelBound();

		mainBall3.attachChild(visualMainBall3);
		mainBall3.generatePhysicsGeometry();
		mainBall3.setMaterial(customMaterial);		
		// Se computa la masa luego de generar la geometria fisica
		mainBall3.computeMass();
		
		// La agrego a la lista de bolas
		balls.add(mainBall3);
		
		//----------------------------------------
		
		//TODO quitar esto, es para debug de bumper
		// Mover la bola dos hacia el bumper1
		// getPhysicsSpace().addToUpdateCallbacks( new PhysicsUpdateCallback() {
	     //       public void beforeStep( PhysicsSpace space, float time ) {
	     //       	/* Checkea todas las acciones para ver si alguna debe ser invocada, en tal caso invoca a performAction
	     //       	 * pasandole el tiempo 'time' 
	     //       	 */
	     //       	mainBall2.addForce(new Vector3f(-7000f, 0f, 0f));
	     //       }
	     //       public void afterStep( PhysicsSpace space, float time ) {
		//
	      //      }
	      //  } );
		 
		//-----------------------------------------

		/* Pongo flippers de prueba */
		final Box rightVisualFlipper = new Box("Right visual flipper", new Vector3f(), 7, 1, 2);
		rightVisualFlipper.setLocalTranslation(new Vector3f(11, 3, 70));
		rightVisualFlipper.setModelBound(new BoundingBox());
		rightVisualFlipper.updateModelBound();
		
		/* Le doy color */
		Utils.color(rightVisualFlipper, new ColorRGBA(0f, 1.0f, 0f, 1.0f), 128);
		
		DynamicPhysicsNode rightTestFlipper = Flipper.create(this, "Physic right flipper", rightVisualFlipper, FlipperType.RIGHT_FLIPPER);
		rootNode.attachChild(rightTestFlipper);
		
		final Box leftVisualFlipper = new Box("Left visual flipper", new Vector3f(), 7, 1, 2);
		leftVisualFlipper.setLocalTranslation(new Vector3f(-11, 3, 70));
		leftVisualFlipper.setModelBound(new BoundingBox());
		leftVisualFlipper.updateModelBound();
		
		/* Le doy color */
		Utils.color(leftVisualFlipper, new ColorRGBA(0f, 1.0f, 0f, 1.0f), 128);
		
		DynamicPhysicsNode leftTestFlipper = Flipper.create(this, "Physic left flipper", leftVisualFlipper, FlipperType.LEFT_FLIPPER);
		rootNode.attachChild(leftTestFlipper);
		
		final Box leftSmallVisualFlipper = new Box("Left small visual flipper", new Vector3f(), 3.5f, 0.5f, 1);
		leftSmallVisualFlipper.setLocalTranslation(new Vector3f(-19, 3, -20));
		leftSmallVisualFlipper.setModelBound(new BoundingBox());
		leftSmallVisualFlipper.updateModelBound();
		
		/* Le doy color */
		Utils.color(leftSmallVisualFlipper, new ColorRGBA(0f, 1.0f, 0f, 1.0f), 128);
		
		DynamicPhysicsNode leftSmallTestFlipper = Flipper.create(this, "Physic small left flipper", leftSmallVisualFlipper, FlipperType.LEFT_FLIPPER);
		rootNode.attachChild(leftSmallTestFlipper);


		//-----------------------------------------
		
		/* Pongo un plunger de prueba */
		final Box visualPlunger = new Box("Visual plunger", new Vector3f(), 1, 1, 15);
		visualPlunger.setLocalTranslation(new Vector3f(25, 3, 90));
		
		/* Le doy color */
		Utils.color(visualPlunger, new ColorRGBA(1.0f, 1.0f, 0f, 1.0f), 128);
		
		DynamicPhysicsNode testPlunger = Plunger.create(this, "Physic plunger", visualPlunger, 10);
		rootNode.attachChild(testPlunger);

		//-----------------------------------------
		
		/* Pongo una puerta de prueba */
		final Box visualDoor = new Box("Visual door", new Vector3f(), 3, 1, 0.1f);
		visualDoor.setLocalTranslation(new Vector3f(25, 3, 75));
		visualDoor.setModelBound(new BoundingBox());
		visualDoor.updateModelBound();

		
		/* Le doy color */
		Utils.color(visualDoor, new ColorRGBA(0f, 1.0f, 1.0f, 1.0f), 128);
		
		DynamicPhysicsNode testDoor = Door.create(this, "Physic door", visualDoor, Door.DoorType.RIGHT_DOOR, -1.3f, 0.5f);
		rootNode.attachChild(testDoor);
		
		//-----------------------------------------
		
		/* Pongo un molinete de prueba */
		final Box visualSpinner = new Box("Visual spinner", new Vector3f(), 1.25f, 2.5f, 0.1f);
		visualSpinner.setLocalTranslation(new Vector3f(-12, 4, 30));
		visualSpinner.setModelBound(new BoundingBox());
		visualSpinner.updateModelBound();

		
		/* Le doy color */
		Utils.color(visualSpinner, new ColorRGBA(1f, 0.3f, 0.5f, 1.0f), 128);
		
		DynamicPhysicsNode testSpinner = Spinner.create(this, "Physic spinner", visualSpinner, SpinnerType.NORMAL_SPINNER);
		rootNode.attachChild(testSpinner);
		
		/*Box box = new Box("The Box", new Vector3f(-1, -1, -1), new Vector3f(1, 1, 1));
		box.updateRenderState();
		// Rotate the box 25 degrees along the x and y axes.
		Quaternion rot = new Quaternion();
		rot.fromAngles(FastMath.DEG_TO_RAD * 25, FastMath.DEG_TO_RAD * 25, 0.0f);
		box.setLocalRotation(rot);
		// Attach the box to the root node
		rootNode.attachChild(box);
		*/
		/*// Caja para LFlipper (esto vendria de X3d)
		Box b1 = new Box("LFlipper Shape", new Vector3f(), 10, 2.5f, 2.5f);
        b1.setModelBound(new BoundingBox());
        b1.updateModelBound();
        // LFlipper
        Flipper Lflipper = new Flipper("LFlipper", b1, Flipper.FlipperType.LEFT_FLIPPER);
        Lflipper.setLocalTranslation(new Vector3f(0, 0, -40));
        inputHandlerLflipper = new FlipperInputHandler(Lflipper, pinballSettings.getRenderer());
        scene.attachChild(Lflipper);
        Lflipper.updateWorldBound();
        Lflipper.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        
        //flippers.add(Lflipper);*/
		
		//-----------------------------------------
		
		/* Pongo un iman de prueba */
		final Box visualMagnet1 = new Box("Visual magnet 1", new Vector3f(), 2f, 4f, 2f);
		// Le doy color al iman
		Utils.color( visualMagnet1, new ColorRGBA( 1.0f, 0f, 0f, 1.0f ), 128 );
		// Es importante setear la posicion del objeto visual antes de crear el iman! (es necesaria para calcular distancia)
		visualMagnet1.setLocalTranslation(new Vector3f(-25, 5, 50));
		
		// Agregado de bounding volume 
		visualMagnet1.setModelBound(new BoundingBox());
		visualMagnet1.updateModelBound();
				
		StaticPhysicsNode magnet1 = Magnet.create(this, "Physic magnet 1", visualMagnet1);
		rootNode.attachChild(magnet1);
		
		// Agrego otro iman
		//final Box visualMagnet2 = new Box("Visual magnet 2", new Vector3f(), 2f, 4f, 2f);
		//visualMagnet2.setLocalTranslation(new Vector3f(15, 5, -10));
		//StaticPhysicsNode magnet2 = Magnet.create(this, "Physic magnet 2", visualMagnet2);
		//rootNode.attachChild(magnet2);
		
		//-----------------------------------------

		// Agrego un bumper
		final Box visualBumper1 = new Box("Visual bumper 1", new Vector3f(), 2f, 4f, 2f);
		// Le doy color al bumper
		Utils.color( visualBumper1, new ColorRGBA( 0f, 1f, 0f, 1.0f ), 120 );
		visualBumper1.setLocalTranslation(new Vector3f(0, 5.6f, 0));
		// Agregado de bounding volume 
		visualBumper1.setModelBound(new BoundingBox());
		visualBumper1.updateModelBound();
		DynamicPhysicsNode bumper1 = Bumper.create(this, "Physic bumper 1", visualBumper1, BumperType.JUMPER);
		rootNode.attachChild(bumper1);
		
		// Agrego otro bumper
		final Box visualBumper2 = new Box("Visual bumper 1", new Vector3f(), 2f, 4f, 2f);
		// Le doy color al bumper
		Utils.color( visualBumper2, new ColorRGBA( 0f, 1f, 0f, 1.0f ), 120 );
		visualBumper2.setLocalTranslation(new Vector3f(25, 5, 0));
		// Agregado de bounding volume 
		visualBumper2.setModelBound(new BoundingBox());
		visualBumper2.updateModelBound();
		//StaticPhysicsNode bumper2 = Bumper.create(this, "Physic bumper 2", visualBumper2/*, BumperType.JUMPER*/, pinballInputHandler);
		//rootNode.attachChild(bumper2);
		//bumpers.add(bumper2);
		
	}
	
	private void buildTable()
	{
		/* Nodo estatico de la mesa */
		StaticPhysicsNode table = getPhysicsSpace().createStaticNode();
		rootNode.attachChild(table);
		
		final Box visualTable = new Box( "Table", new Vector3f(), 30, 1, 80);
	
		table.attachChild(visualTable);		
		table.generatePhysicsGeometry();
		
		// Seteo el material de la mesa
		table.setMaterial( pinballTableMaterial );
		
		// Seteo el color de la mesa. Brillo al maximo
	    Utils.color( table, new ColorRGBA( 0.5f, 0.5f, 0.9f, 1.0f ), 128 );
	}
	
	public DynamicPhysicsNode getPlunger()
	{
		return plunger;
	}
	
	public List<DynamicPhysicsNode> getFlippers()
	{
		return flippers;
	}
	
	public List<DynamicPhysicsNode> getBumpers() 
	{
		return bumpers;
	}
	
	public List<DynamicPhysicsNode> getDoors() 
	{
		return doors;
	}
	
	public List<DynamicPhysicsNode> getSpinners() 
	{
		return spinners;
	}
	
	public List<StaticPhysicsNode> getMagnets()
	{
		return magnets;
	}
	
	public PinballSettings getPinballSettings()
	{
		return pinballSettings;
	}
	
	public Camera getCamera()
	{
		return cam;
	}

	public PinballInputHandler getPinballInputHandler()
	{
		return pinballInputHandler;
	}
	
	public void addBumper(DynamicPhysicsNode bumper)
	{
		bumpers.add(bumper);
	}
	
	public void addMagnet(StaticPhysicsNode magnet)
	{
		magnets.add(magnet);
	}

	public void setPlunger(DynamicPhysicsNode plunger)
	{
		this.plunger = plunger;
	}
	
	public void addDoor(DynamicPhysicsNode door)
	{
		doors.add(door);
	}
	
	public void addFlipper(DynamicPhysicsNode flipper)
	{
		flippers.add(flipper);
	}
	
	public void addSpinner(DynamicPhysicsNode spinner)
	{
		spinners.add(spinner);
	}

	public void setScore(int score)
	{
		this.score = score;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public GameLogic getGameLogic()
	{
		return gameLogic;
	}
	
	public List<DynamicPhysicsNode> getBalls()
	{
		return balls;
	}
}
