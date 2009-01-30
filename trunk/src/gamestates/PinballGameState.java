package gamestates;

import gamelogic.GameLogic;
import input.PinballInputHandler;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import loader.X3DLoader;
import main.Main;
import themes.CarsThemeGameLogic;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.light.PointLight;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.Text;
import com.jme.scene.Spatial.CullHint;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Quad;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.CullState;
import com.jme.util.Debug;
import com.jme.util.Timer;
import com.jme.util.stat.StatCollector;
import com.jme.util.stat.StatType;
import com.jme.util.stat.graph.DefColorFadeController;
import com.jme.util.stat.graph.GraphFactory;
import com.jme.util.stat.graph.LineGrapher;
import com.jme.util.stat.graph.TabledLabelGrapher;
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
import components.Sensor;
import components.Bumper.BumperType;
import components.Flipper.FlipperType;
import components.Sensor.SensorType;;

/**
 * Clase principal del juego.
 *
 */
public class PinballGameState extends PhysicsEnhancedGameState
{
	
	public static final Material pinballTableMaterial = Material.PLASTIC;
	
	// Nombre a usar en el nodo fisico de todas las bolas del juego. Es para reconocer las bolas en las colisiones	
	public static final String PHYSIC_NODE_NAME_FOR_BALLS = "ball";
	
	private static final String GAME_NAME = "xtremme pinball";
	private static final String GAME_VERSION = "0.4";
	
	/* Logger de la clase Pinball */
    private static final Logger logger = Logger.getLogger(PinballGameState.class.getName());
    
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
	
	/* Sensores del juego */
	private List<DynamicPhysicsNode> sensors;
	
	/* Plunger del juego */
	private DynamicPhysicsNode plunger;
	
	/* Configuracion del juego */
	private PinballGameStateSettings pinballSettings;
	
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

	/* XXX Ubicacion inicial de la bola: cable */
	private Vector3f ballStartUp = /*new Vector3f( 17.5f, 3.5f, -9.0f )*/ /*new Vector3f( 15,15,-51 )*/ new Vector3f( 1, 16, -58);
	
	/* Ubicacion inicial de la camara */
	private Vector3f cameraStartUp = new Vector3f( 0.0f, 63.0f, 16.0f ); 
//	new Vector3f(-7.8f,11.6f,-63.6f);
	/* Lugar al que mira la camara incialmente */
	private Vector3f cameraLookAt = new Vector3f( 0.0f, 43.5f, 0.0f );
//	new Vector3f(-6.8f,11.6f,-62.6f);
	    
	/* Nodo que guarda la tabla */
	private Node tabla;
	
	/**
	 * Crea un estado de juego nuevo.
	 * @param name Nombre del estado de juego.
	 * @param pinballSettings Settings del pinball a iniciar.
	 */
	public PinballGameState(String name, PinballGameStateSettings pinballSettings)
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
		/* Actualizo el timer */
		timer.update();
		
		if (debug)
            StatCollector.update();
		
		/* Pido al timer el tiempo transcurrido desde la ultima llamada */
		float interpolation = timer.getTimePerFrame();
		
		/* Actualizo el controlador de input */
        pinballInputHandler.update(interpolation);
        if (!pause)
        {
            /* Actualizo los componentes que asi lo requieren */
            updateComponents(interpolation);
        
            /* Se modifico la escena, entonces actualizo el grafo */
            // TODO No deberiamos estar acelerando la fisica, pero bueno, aca esta la llamada, la tenemos 3 veces mas rapida.
            // super.update(tpf);
            super.update(tpf * 3f);
            
            // rootNode.updateGeometricState(interpolation, true);   // se hace en el super.update esto     
        }
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
		
		/* Inicializo al jugador */
		
		// inicializo cabeza
		/* Perspectiva y FOV */
		cam.setFrustumPerspective(45.0f, (float)pinballSettings.getWidth() / (float)pinballSettings.getHeight(), 1, 1000);
		
		/* Ubicacion */
		Vector3f loc = new Vector3f(0.0f, 0.0f, 0.0f);
		Vector3f left = new Vector3f(-1.0f, 0.0f, 0.0f);
		Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
		Vector3f dir = new Vector3f(0.0f, 0f, 1.0f);

		cam.setFrame(loc, left, up, dir);
		
        /* aplicar los cambios a la cabeza */
        cam.update();

        // le pego los ojos
	    /* Fijo la camara al display */
		display.getRenderer().setCamera(cam);
		
		// le agrego las extremidades
		/* Creo el input handler del pinball */
		pinballInputHandler = new PinballInputHandler(this);
		
		// y lo ubico en el espacio
        /* seteo ubicacion del jugador */
        cam.setLocation( new Vector3f(cameraStartUp) ); // 100 son 2mts, y seria lineal

        // seteo la mirada del jugador 
        cam.lookAt( new Vector3f(cameraLookAt), new Vector3f( 0.0f, 1.0f, -1.0f ) );

        /* Aplicar los cambios al jugador */
        cam.update();
        
        
	
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
		initDebug();
	}

	/**
	 * Inicializar la escena
	 */
	protected void initGame()
	{
		/* Preparo la orientacion del sistema de audio */
		Main.getAudioSystem().getEar().trackOrientation(getCamera());
		Main.getAudioSystem().getEar().trackPosition(getCamera());
		
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

		/* Armo la habitacion, la mesa y la bola */
//        loadEnvironment();
        loadTable();
        setUpBall();
		
        
		// ----------------------------------------
		// TODO volar: es para debug
//        buildTable();
//        gameLogic = new CarsThemeGameLogic(this);
//        buildAndAttachComponents();
//        inclinePinball();
        buildLighting();
        //-----------------------------------------
        
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
        
        /* Aviso a la logica de juego que empieza uno */
        gameLogic.gameStart();
	}
	
	private void buildLighting()
	{
		PointLight light = new PointLight();
		light.setDiffuse( new ColorRGBA( 0.75f, 0.75f, 0.75f, 0.75f ) );
	    light.setAmbient( new ColorRGBA( 0.5f, 0.5f, 0.5f, 1.0f ) );
	    light.setLocation( new Vector3f( 100, 100, 100 ) );
	    light.setEnabled( true );
	    
	    lightState.attach( light );
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
        visualMainBall2.setLocalTranslation(new Vector3f(0, 15, -20));
		
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
		
		DynamicPhysicsNode testSpinner = Spinner.create(this, "Physic spinner", visualSpinner/*, SpinnerType.NORMAL_SPINNER*/);
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
		final Box visualBumper1 = new Box("Visual bumper 1", new Vector3f(), 2f, 2f, 2f);
		// Le doy color al bumper
		Utils.color( visualBumper1, new ColorRGBA( 1f, 1f, 0f, 1.0f ), 120 );
		
		visualBumper1.setLocalScale( new Vector3f(0.19173f,5.595036f, 0.804614f ) );
		visualBumper1.setLocalRotation( new Quaternion().fromAngleAxis( 1.593925f, new Vector3f( -0.982868f, -0.150476f, -0.106427f ) ) );
		visualBumper1.setLocalTranslation(new Vector3f(-4, 2f, 0));
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
		
		// Agrego un sensor de prueba
		final Box visualLostBallSensor = new Box("visualLostBallSensor 1", new Vector3f(), 4f, 2f, 2f);
		// TODO Ponerlo transparente para que no se vea (lo pongo verde para verlo y poder hacer debug)
		Utils.color( visualLostBallSensor, new ColorRGBA( 0f, 1f, 0f, 1f ), 120 );
		visualLostBallSensor.setLocalTranslation(new Vector3f(0, 2f, 0));
		// Agregado de bounding volume 
		visualLostBallSensor.setModelBound(new BoundingBox());
		visualLostBallSensor.updateModelBound();
		DynamicPhysicsNode lostBallSensor = Sensor.create(this, "Physic los ball sensor 1", visualLostBallSensor, SensorType.RAMP_SENSOR);
		rootNode.attachChild(lostBallSensor);
		
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
	
	public List<DynamicPhysicsNode> getSensors() 
	{
		return sensors;
	}
	
	public List<StaticPhysicsNode> getMagnets()
	{
		return magnets;
	}
	
	public PinballGameStateSettings getPinballSettings()
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
	
	public void addSensor(DynamicPhysicsNode sensor)
	{
		sensors.add(sensor);
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
	
	private void setUpBall()
	{
	    logger.info( "Construyendo pelota (haciendo pelota el pinball :)" );

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

        /* Nodo dinamico de la bola */
        final DynamicPhysicsNode mainBall = getPhysicsSpace().createDynamicNode();
        mainBall.setName( "ball" );
        rootNode.attachChild( mainBall );

        final Sphere visualMainBall = new Sphere( "Bola", 25, 25, 1 );
        visualMainBall.setLocalTranslation( new Vector3f( this.ballStartUp ) );

        // Agregado de bounding volume 
        visualMainBall.setModelBound( new BoundingSphere() );
        visualMainBall.updateModelBound();

        mainBall.attachChild( visualMainBall );
        mainBall.generatePhysicsGeometry();
        mainBall.setMaterial( customMaterial );
        // Se computa la masa luego de generar la geometria fisica
        mainBall.computeMass();

        // La agrego a la lista de bolas
        balls.add( mainBall );
	}
	
	private void loadEnvironment()
    {
        /* Iluminacion */

        /* borramos todas las luces default */
        lightState.detachAll();

        /* X3D */

        X3DLoader loader;
        /* cargamos y attacheamos la habitacion */
        try
        {
            loader = new X3DLoader( X3DLoader.class.getClassLoader().getResource("resources/models/Room.x3d") );

            /* agregamos la fisica */
            loader.setPinball( this );

            /* agregamos el lightState */
            loader.setLightState( lightState );

            Node room = loader.loadScene();
            
            /* cargamos y attacheamos la habitacion */
            rootNode.attachChild( room );
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        
        /* cargamos y attacheamos la maquina */
        try
        {
            loader = new X3DLoader(  X3DLoader.class.getClassLoader().getResource("resources/models/Machine.x3d" ) );

            /* agregamos la fisica */
            loader.setPinball( this );

            /* agregamos el lightState */
            loader.setLightState( lightState );

            Spatial machine = loader.loadScene();

            /* cargamos y attacheamos la maquina */
            rootNode.attachChild( machine );
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
    }
    
    private void loadTable()
    {
        X3DLoader loader;

        /* cargamos y attacheamos la mesa */
        try
        {
            //XXX nombre tabla
            loader = new X3DLoader(  X3DLoader.class.getClassLoader().getResource( "resources/models/Table.x3d" ) );

            /* agregamos la fisica */
            loader.setPinball( this );

            /* agregamos el lightState */
            loader.setLightState( lightState );

            tabla = loader.loadScene();

            tabla = inclinePinball( tabla );
            
            /* cargamos y attacheamos la tabla */
            rootNode.attachChild( tabla );
            
            this.gameLogic = loader.getTheme(this);
            
            //TODO remover
            if (this.gameLogic == null)
                this.gameLogic = new CarsThemeGameLogic(this);
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        
    }
    
    private Node inclinePinball( Node table )
    {
        table.updateModelBound();

        /* Se rota toda la mesa y sus componentes en el eje X */
        table.setLocalRotation(getPinballSettings().getInclinationQuaternion());
        
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
        
        return table;
    }
    
    private boolean debug = true;
    private boolean pause = false;

    protected void initDebug()
    {
        if ( debug )
        {
            /* Assign key P to action "toggle_pause". (intento de pause) */
            //          KeyBindingManager.getKeyBindingManager().set( "toggle_pause",
            //                  KeyInput.KEY_P );
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        pause = !pause;
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_P, InputHandler.AXIS_NONE, false );

            /* Home reset de camara */
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        cam.setLocation( new Vector3f( cameraStartUp ) );

                        cam.lookAt( new Vector3f( cameraLookAt ), new Vector3f( 0.0f, 1.0f, -1.0f ) );
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_HOME, InputHandler.AXIS_NONE, false );


            /* Y reset de pelota */
            pinballInputHandler.addAction( new InputAction()
            {
                private Vector3f ballStartUp = Vector3f.ZERO;
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        // falla miserablemente cuando pongamos más de 1 :D
                        balls.get( 0 ).clearDynamics();
                        balls.get( 0 ).setLocalTranslation( new Vector3f(ballStartUp) );
                        balls.get( 0 ).setLocalRotation( new Quaternion() );
                        balls.get( 0 ).updateGeometricState( 0, false );
                        //System.out.println(balls.get( 0 ).getLocalTranslation());
                    }
                }
                
                public InputAction setBallStartup(Vector3f ballsu)
                {
                    this.ballStartUp = ballsu;
                    return this;
                }
                
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_Y, InputHandler.AXIS_NONE, false );
            
            // F1 screenshot
            //            KeyBindingManager.getKeyBindingManager().set( "screen_shot",
            //                    KeyInput.KEY_F1 );
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        display.getRenderer().takeScreenShot( "SimpleGameScreenShot" );
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F1, InputHandler.AXIS_NONE, false );

            /* Assign key F2 to action "mem_report". */
            //            KeyBindingManager.getKeyBindingManager().set( "mem_report",
            //                KeyInput.KEY_F2 );
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        long totMem = Runtime.getRuntime().totalMemory();
                        long freeMem = Runtime.getRuntime().freeMemory();
                        long maxMem = Runtime.getRuntime().maxMemory();

                        logger.info( "|*|*|  Memory Stats  |*|*|" );
                        logger.info( "Total memory: " + ( totMem >> 10 ) + " kb" );
                        logger.info( "Free memory: " + ( freeMem >> 10 ) + " kb" );
                        logger.info( "Max memory: " + ( maxMem >> 10 ) + " kb" );
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F2, InputHandler.AXIS_NONE, false );

            // F3 debug de la fisica
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        showPhysics = !showPhysics;
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F3, InputHandler.AXIS_NONE, false );

            // F4 estadisticas
//            KeyBindingManager.getKeyBindingManager().set( "toggle_stats", KeyInput.KEY_F4 );
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        showGraphs = !showGraphs;
                        Debug.updateGraphs = showGraphs;
                        labGraph.clearControllers();
                        lineGraph.clearControllers();
                        labGraph.addController(new DefColorFadeController(labGraph, showGraphs ? .6f : 0f, showGraphs ? .5f : -.5f));
                        lineGraph.addController(new DefColorFadeController(lineGraph, showGraphs ? .6f : 0f, showGraphs ? .5f : -.5f));
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F4, InputHandler.AXIS_NONE, false );


            /* Assign key F5 to action "toggle_wire". */
            //            KeyBindingManager.getKeyBindingManager().set( "toggle_wire",
            //                    KeyInput.KEY_F5 );
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        wireState.setEnabled( !wireState.isEnabled() );
                        rootNode.updateRenderState();
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F5, InputHandler.AXIS_NONE, false );

            /* Assign key F6 to action "toggle_lights". */
            //            KeyBindingManager.getKeyBindingManager().set( "toggle_lights",
            //                    KeyInput.KEY_F6 );
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        lightState.setEnabled( !lightState.isEnabled() );
                        rootNode.updateRenderState();
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F6, InputHandler.AXIS_NONE, false );

            /* Assign key F7 to action "toggle_bounds". */
            //            KeyBindingManager.getKeyBindingManager().set( "toggle_bounds",
            //                    KeyInput.KEY_F7 );
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        showBounds = !showBounds;
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F7, InputHandler.AXIS_NONE, false );

            /* Assign key F8 to action "toggle_normals". */
            //            KeyBindingManager.getKeyBindingManager().set( "toggle_normals",
            //                    KeyInput.KEY_F8 );
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        showNormals = !showNormals;
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F8, InputHandler.AXIS_NONE, false );

            /* Assign key F9 to action "camera_out". */
            KeyBindingManager.getKeyBindingManager().set( "camera_out", KeyInput.KEY_F9 );
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        
                        Vector3f pos = display.getRenderer().getCamera().getLocation();
                        message = "cam[" + String.format( "%.2f", pos.x ) + ";" + String.format( "%.2f", pos.y ) + ";" + String.format( "%.2f", pos.z ) + "]";
                        messageText.getText().replace(0, messageText.getText().length(), "" + message);
                        logger.info( "Camera at: " + display.getRenderer().getCamera().getLocation() );
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F9, InputHandler.AXIS_NONE, false );

            KeyBindingManager.getKeyBindingManager().set( "camera_out", KeyInput.KEY_F9 );

            //estas son para debug... es mala idea pasarlas a estable porque se rompe la fisica
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        float angle = getPinballSettings().getInclinationAngle();
                        
                        if (angle > 9)
                        {
                            angle = 1;
                        }
                        else
                        {
                            angle += 1;
                        }
                        
                        getPinballSettings().setInclinationAngle( angle );
                        inclinePinball( tabla );
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_PGUP, InputHandler.AXIS_NONE, false );
            
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        float angle = getPinballSettings().getInclinationAngle();
                        
                        if (angle < 2)
                        {
                            angle = 10;
                        }
                        else
                        {
                            angle -= 1;
                        }
                        
                        getPinballSettings().setInclinationAngle( angle );
                        inclinePinball( tabla );
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_PGDN, InputHandler.AXIS_NONE, false );
            
            /* Assign key ADD to action "step". */
            //            KeyBindingManager.getKeyBindingManager().set( "step",
            //                    KeyInput.KEY_ADD );
            // esto debería forzar el update... no creo que valga la pena
            //            pinballInputHandler.addAction( new InputAction() {
            //                public void performAction( InputActionEvent evt ) {
            //                    if ( evt.getTriggerPressed() ) {
            //                        update();
            //                        rootNode.updateGeometricState(tpf, true);
            //                    }
            //                }
            //            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_ADD, InputHandler.AXIS_NONE, false );
            //            KeyBindingManager.getKeyBindingManager().set( "toggle_depth",
            //                KeyInput.KEY_F3 );
            // esto debería cambiar la profundidad... tampoco creo que valga la pena
            //          pinballInputHandler.addAction( new InputAction() {
            //              public void performAction( InputActionEvent evt ) {
            //                  if ( evt.getTriggerPressed() ) {
            //                      showDepth = !showDepth;
            //                  }
            //              }
            //              }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F3, InputHandler.AXIS_NONE, false );
        }
        
        statNode = new Node( "Stats node" );
        statNode.setCullHint( Spatial.CullHint.Never );
        statNode.setRenderQueueMode(Renderer.QUEUE_ORTHO);

        graphNode = new Node( "Graph node" );
        graphNode.setCullHint( Spatial.CullHint.Never );
        statNode.attachChild(graphNode);

        setupStatGraphs();
        setupStats();
    }
    
    private TabledLabelGrapher tgrapher;

//  private TimedAreaGrapher lgrapher;
    private LineGrapher lgrapher;

    private Quad lineGraph, labGraph;
    
    protected boolean showGraphs = false;
    
    /**
     * The root node for our stats and text.
     */
    protected Node statNode;

    /**
     * The root node for our stats graphs.
     */
    protected Node graphNode;
    
    protected void setupStats() {
        lgrapher.addConfig(StatType.STAT_FRAMES, LineGrapher.ConfigKeys.Color.name(), ColorRGBA.green);
        lgrapher.addConfig(StatType.STAT_FRAMES, LineGrapher.ConfigKeys.Stipple.name(), 0XFF0F);
        lgrapher.addConfig(StatType.STAT_TRIANGLE_COUNT, LineGrapher.ConfigKeys.Color.name(), ColorRGBA.cyan);
        lgrapher.addConfig(StatType.STAT_TRIANGLE_COUNT, TabledLabelGrapher.ConfigKeys.FrameAverage.name(), true);
        lgrapher.addConfig(StatType.STAT_QUAD_COUNT, LineGrapher.ConfigKeys.Color.name(), ColorRGBA.lightGray);
        lgrapher.addConfig(StatType.STAT_QUAD_COUNT, TabledLabelGrapher.ConfigKeys.FrameAverage.name(), true);
        lgrapher.addConfig(StatType.STAT_LINE_COUNT, LineGrapher.ConfigKeys.Color.name(), ColorRGBA.red);
        lgrapher.addConfig(StatType.STAT_LINE_COUNT, TabledLabelGrapher.ConfigKeys.FrameAverage.name(), true);
        lgrapher.addConfig(StatType.STAT_GEOM_COUNT, LineGrapher.ConfigKeys.Color.name(), ColorRGBA.gray);
        lgrapher.addConfig(StatType.STAT_GEOM_COUNT, TabledLabelGrapher.ConfigKeys.FrameAverage.name(), true);
        lgrapher.addConfig(StatType.STAT_TEXTURE_BINDS, LineGrapher.ConfigKeys.Color.name(), ColorRGBA.orange);
        lgrapher.addConfig(StatType.STAT_TEXTURE_BINDS, TabledLabelGrapher.ConfigKeys.FrameAverage.name(), true);

        tgrapher.addConfig(StatType.STAT_FRAMES, TabledLabelGrapher.ConfigKeys.Decimals.name(), 0);
        tgrapher.addConfig(StatType.STAT_FRAMES, TabledLabelGrapher.ConfigKeys.Name.name(), "Frames/s:");
        tgrapher.addConfig(StatType.STAT_TRIANGLE_COUNT, TabledLabelGrapher.ConfigKeys.Decimals.name(), 0);
        tgrapher.addConfig(StatType.STAT_TRIANGLE_COUNT, TabledLabelGrapher.ConfigKeys.Name.name(), "Avg.Tris:");
        tgrapher.addConfig(StatType.STAT_TRIANGLE_COUNT, TabledLabelGrapher.ConfigKeys.FrameAverage.name(), true);
        tgrapher.addConfig(StatType.STAT_QUAD_COUNT, TabledLabelGrapher.ConfigKeys.Decimals.name(), 0);
        tgrapher.addConfig(StatType.STAT_QUAD_COUNT, TabledLabelGrapher.ConfigKeys.Name.name(), "Avg.Quads:");
        tgrapher.addConfig(StatType.STAT_QUAD_COUNT, TabledLabelGrapher.ConfigKeys.FrameAverage.name(), true);
        tgrapher.addConfig(StatType.STAT_LINE_COUNT, TabledLabelGrapher.ConfigKeys.Decimals.name(), 0);
        tgrapher.addConfig(StatType.STAT_LINE_COUNT, TabledLabelGrapher.ConfigKeys.Name.name(), "Avg.Lines:");
        tgrapher.addConfig(StatType.STAT_LINE_COUNT, TabledLabelGrapher.ConfigKeys.FrameAverage.name(), true);
        tgrapher.addConfig(StatType.STAT_GEOM_COUNT, TabledLabelGrapher.ConfigKeys.Decimals.name(), 0);
        tgrapher.addConfig(StatType.STAT_GEOM_COUNT, TabledLabelGrapher.ConfigKeys.Name.name(), "Avg.Objs:");
        tgrapher.addConfig(StatType.STAT_GEOM_COUNT, TabledLabelGrapher.ConfigKeys.FrameAverage.name(), true);
        tgrapher.addConfig(StatType.STAT_TEXTURE_BINDS, TabledLabelGrapher.ConfigKeys.Decimals.name(), 0);
        tgrapher.addConfig(StatType.STAT_TEXTURE_BINDS, TabledLabelGrapher.ConfigKeys.Name.name(), "Avg.Tex binds:");
        tgrapher.addConfig(StatType.STAT_TEXTURE_BINDS, TabledLabelGrapher.ConfigKeys.FrameAverage.name(), true);
        
        // If you want to try out 
//        lgrapher.addConfig(StatType.STAT_RENDER_TIMER, TimedAreaGrapher.ConfigKeys.Color.name(), ColorRGBA.blue);
//        lgrapher.addConfig(StatType.STAT_UNSPECIFIED_TIMER, TimedAreaGrapher.ConfigKeys.Color.name(), ColorRGBA.white);
//        lgrapher.addConfig(StatType.STAT_STATES_TIMER, TimedAreaGrapher.ConfigKeys.Color.name(), ColorRGBA.yellow);
//        lgrapher.addConfig(StatType.STAT_DISPLAYSWAP_TIMER, TimedAreaGrapher.ConfigKeys.Color.name(), ColorRGBA.red);
//
//        tgrapher.addConfig(StatType.STAT_RENDER_TIMER, TabledLabelGrapher.ConfigKeys.Decimals.name(), 2);
//        tgrapher.addConfig(StatType.STAT_RENDER_TIMER, TabledLabelGrapher.ConfigKeys.Name.name(), "Render:");
//      tgrapher.addConfig(StatType.STAT_RENDER_TIMER, TabledLabelGrapher.ConfigKeys.FrameAverage.name(), true);
//        tgrapher.addConfig(StatType.STAT_UNSPECIFIED_TIMER, TabledLabelGrapher.ConfigKeys.Decimals.name(), 2);
//        tgrapher.addConfig(StatType.STAT_UNSPECIFIED_TIMER, TabledLabelGrapher.ConfigKeys.Name.name(), "Other:");
//      tgrapher.addConfig(StatType.STAT_UNSPECIFIED_TIMER, TabledLabelGrapher.ConfigKeys.FrameAverage.name(), true);
//        tgrapher.addConfig(StatType.STAT_STATES_TIMER, TabledLabelGrapher.ConfigKeys.Decimals.name(), 2);
//        tgrapher.addConfig(StatType.STAT_STATES_TIMER, TabledLabelGrapher.ConfigKeys.Name.name(), "States:");
//      tgrapher.addConfig(StatType.STAT_STATES_TIMER, TabledLabelGrapher.ConfigKeys.FrameAverage.name(), true);
//        tgrapher.addConfig(StatType.STAT_DISPLAYSWAP_TIMER, TabledLabelGrapher.ConfigKeys.Decimals.name(), 2);
//        tgrapher.addConfig(StatType.STAT_DISPLAYSWAP_TIMER, TabledLabelGrapher.ConfigKeys.Name.name(), "DisplaySwap:");
//      tgrapher.addConfig(StatType.STAT_DISPLAYSWAP_TIMER, TabledLabelGrapher.ConfigKeys.FrameAverage.name(), true);
//
//        StatCollector.addTimedStat(StatType.STAT_RENDER_TIMER);
//        StatCollector.addTimedStat(StatType.STAT_STATES_TIMER);
//        StatCollector.addTimedStat(StatType.STAT_UNSPECIFIED_TIMER);
//        StatCollector.addTimedStat(StatType.STAT_DISPLAYSWAP_TIMER);
    }
    
    /**
     * Set up the graphers we will use and the quads we'll show the stats on.
     *
     */
    protected void setupStatGraphs() {
        StatCollector.setSampleRate(1000L);
        StatCollector.setMaxSamples(40);

        lineGraph = new Quad("lineGraph", display.getWidth(), display.getHeight()*.75f) {
            private static final long serialVersionUID = 1L;
            @Override
            public void draw(Renderer r) {
                StatCollector.pause();
                super.draw(r);
                StatCollector.resume();
            }
        };
        lgrapher = GraphFactory.makeLineGraph((int)(lineGraph.getWidth()+.5f), (int)(lineGraph.getHeight()+.5f), lineGraph);
//      lgrapher = GraphFactory.makeTimedGraph((int)(lineGraph.getWidth()+.5f), (int)(lineGraph.getHeight()+.5f), lineGraph);
        lineGraph.setLocalTranslation((display.getWidth()*.5f), (display.getHeight()*.625f),0);
        lineGraph.setCullHint(CullHint.Always);
        lineGraph.getDefaultColor().a = 0;
        graphNode.attachChild(lineGraph);
        
        Text f4Hint = new Text("f4", "F4 - toggle stats") {
            private static final long serialVersionUID = 1L;
            @Override
            public void draw(Renderer r) {
                StatCollector.pause();
                super.draw(r);
                StatCollector.resume();
            }
        };
        f4Hint.setCullHint( Spatial.CullHint.Never );
        f4Hint.setRenderState( Text.getDefaultFontTextureState() );
        f4Hint.setRenderState( Text.getFontBlend() );
        f4Hint.setLocalScale(.8f);
        f4Hint.setTextColor(ColorRGBA.gray);
        f4Hint.setLocalTranslation(display.getRenderer().getWidth() - f4Hint.getWidth() - 15, display.getRenderer().getHeight() - f4Hint.getHeight() - 10, 0);
        graphNode.attachChild(f4Hint);

        labGraph = new Quad("labelGraph", display.getWidth(), display.getHeight()*.25f) {
            private static final long serialVersionUID = 1L;
            @Override
            public void draw(Renderer r) {
                StatCollector.pause();
                super.draw(r);
                StatCollector.resume();
            }
        };
        tgrapher = GraphFactory.makeTabledLabelGraph((int)(labGraph.getWidth()+.5f), (int)(labGraph.getHeight()+.5f), labGraph);
        tgrapher.setColumns(2);
        tgrapher.setMinimalBackground(false);
        tgrapher.linkTo(lgrapher);
        labGraph.setLocalTranslation((display.getWidth()*.5f), (display.getHeight()*.125f),0);
        labGraph.setCullHint(CullHint.Always);
        labGraph.getDefaultColor().a = 0;
        graphNode.attachChild(labGraph);
        
    }
    
    @Override
    public void setActive(boolean active)
    {
    	super.setActive(active);
    	
    	/* Notifico a la logica de juego cada vez que
    	 * ingreso, reingreso o salgo del juego (para ir al menu) */
    	if (active)
    		gameLogic.enterGame();
    	else
    		gameLogic.leaveGame();
    }
}
