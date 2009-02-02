package gamestates;

import gamelogic.GameLogic;
import input.PinballInputHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import main.Main;

import com.jme.bounding.BoundingSphere;
import com.jme.input.InputHandler;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.light.PointLight;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.Text;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.util.Timer;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.PhysicsSpace;
import com.jmex.physics.PhysicsUpdateCallback;
import com.jmex.physics.StaticPhysicsNode;
import com.jmex.physics.material.Material;
import components.Bumper;
import components.Door;
import components.Flipper;
import components.Plunger;
import components.Spinner;

/**
 * Clase principal del juego.
 *
 */
public class PinballGameState extends PhysicsEnhancedGameState
{
	
	public static final Material pinballTableMaterial = Material.PLASTIC;
	
	// Nombre a usar en el nodo fisico de todas las bolas del juego. Es para reconocer las bolas en las colisiones	
	public static final String PHYSIC_NODE_NAME_FOR_BALLS = "ball";
	
	private static final String GAME_NAME = "!xtremme pinball";
	private static final String GAME_VERSION = "0.5";
	
	/* Logger de la clase Pinball */
    private static final Logger logger = Logger.getLogger(PinballGameState.class.getName());
    
	/* InputHandler para el pinball */
	private PinballInputHandler pinballInputHandler;
	
	/* Lista de los flippers del juego actual */
	private List<DynamicPhysicsNode> flippers;
	
	/* Lista de los bumpers saltarines del juego actual */
	private List<DynamicPhysicsNode> jumperBumpers;
	
	/* Lista de los bumpers no saltarines del juego actual */
	private List<StaticPhysicsNode> noJumperBumpers;
	
	/* Lista de las puertas del juego actual */
	private List<DynamicPhysicsNode> doors;
	
	/* Lista de los molinetes del juego actual */
	private List<DynamicPhysicsNode> spinners;
	
	/* Lista de los imanes del juego actual */
	private List<StaticPhysicsNode> magnets;
	
	/* Bolas del juego */
	private List<DynamicPhysicsNode> balls;
	
	/* Sensores del juego */
	private List<StaticPhysicsNode> sensors;
	
	/* Plunger del juego */
	private DynamicPhysicsNode plunger;
	
	/* Configuracion del juego */
	private PinballGameStateSettings pinballSettings;
	
	/* Score del juego */
	private int score = 0;
	
	/* Texto con el score para mostrar al usuario */
	private Text scoreText;
	
	/* Mensaje al usuario */
	private String message = "";
	
	private Text fpsText;
	
	/* Mensaje para mostrar al usuario */
	private Text messageText;
	
	/* Logica de juego */
	private GameLogic gameLogic;
	
	/* Timer para los FPS */
	protected Timer timer;

	/* XXX Ubicacion inicial de la bola: cable */
	private Vector3f ballStartUp = new Vector3f( 4.88f, 1.2f, -3.0f ); 
	/*new Vector3f( 15,15,-51 )*/ 
	/*new Vector3f( 1, 16, -58)*/
	/*new Vector3f(-3.22f,20,-15.37485f)*/
	    
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
		
		/* Borro todas las luces default */
        lightState.detachAll();
		
		/* Inicializo la camara, el display y los handlers de input */
		initSystem();
	}
		
	/**
	 * Se debe actualizar la fisica, el puntaje, el tiempo, todo lo relacionado al juego en si.
	 */
	@Override
	public void update(float tpf)
	{
		/* FIXME No deberiamos estar acelerando la fisica, pero bueno, aca esta 
		 * la llamada, la tenemos 3 veces mas rapida.
		 */

//		super.update(tpf * 3f); // Con esto el juego anda mas fluido, no ponerlo dentro del if (!pause)!!!

		/* Actualizo el timer */
		timer.update();
		
		if (com.jme.util.Debug.debug)
		    com.jme.util.stat.StatCollector.update();
		
		/* Pido al timer el tiempo transcurrido desde la ultima llamada */
		float interpolation = timer.getTimePerFrame();
		
		/* Actualizo el controlador de input */
        pinballInputHandler.update(interpolation);
        if (!pause)
        {
            /* Se modifico la escena, entonces actualizo el grafo */
         // super.update(tpf);
        	super.update(tpf * /*3*/ 3);
        	
            /* Actualizo los componentes que asi lo requieren */
            updateComponents(interpolation);
        
//            super.update(tpf * 3f);
            
        }
        
        /* Se actualiza la info que se presenta en pantalla (score y mensajes) */
        scoreText.getText().replace(0, scoreText.getText().length(), "Score: " + score);
        messageText.getText().replace(0, messageText.getText().length(), "" + message);
        fpsText.getText().replace( 4, fpsText.getText().length(), Integer.toString( (int)timer.getFrameRate()/2 ) );
        
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
	    // para tener en cuenta.. hace mas dinamico el asunto, pero la bola vuela mas es una gadorcha
//	    final float g = getPhysicsSpace().getDirectionalGravity( null ).y;
//	    getPhysicsSpace().setDirectionalGravity( new Vector3f(0, FastMath.cos( FastMath.DEG_TO_RAD * 10 ) * g, -FastMath.sin( FastMath.DEG_TO_RAD * 10 ) * g) );

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
        cam.setLocation( pinballSettings.getCamStartPos() ); // 100 son 2mts, y seria lineal

        // seteo la mirada del jugador 
        cam.lookAt( pinballSettings.getCamStartLookAt(), new Vector3f( 0.0f, 1.0f, 0.0f ) );

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
		
		/* Creo la lista de flippers */
		flippers = new ArrayList<DynamicPhysicsNode>(4);
		
		/* Creo la lista de bumpers saltarines*/
		jumperBumpers = new ArrayList<DynamicPhysicsNode>(4);
		
		/* Creo la lista de bumpers estaticos */
		noJumperBumpers = new ArrayList<StaticPhysicsNode>(4);
		
		/* Creo la lista de puertas */
		doors = new ArrayList<DynamicPhysicsNode>(2);
		
		/* Creo la lista de molinetes */
		spinners = new ArrayList<DynamicPhysicsNode>(4);
		
		/* Creo la lista de imanes */
		magnets = new ArrayList<StaticPhysicsNode>(2);
		
	    /* Creo la lista de sensores */
        sensors = new ArrayList<StaticPhysicsNode>(2);
		
		/* Creo la lista de bolas */
		balls = new ArrayList<DynamicPhysicsNode>(4);
		
		/* activamos el debug */
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


		/* Armo la habitacion, la mesa y la bola */
//        loadEnvironment();
//        loadTable();
        setUpBall();
		
        
		// ----------------------------------------
		// TODO volar: es para debug
//        buildTable();
//        gameLogic = new themes.CarsThemeGameLogic(this);
//        buildAndAttachComponents();
//        inclinePinball();
//        buildLighting();
        fpsText = Text.createDefaultTextLabel( "fpsText", "fps " + timer.getFrameRate() );
        fpsText.setLocalScale( 0.80f );
        fpsText.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        fpsText.setLightCombineMode(Spatial.LightCombineMode.Off);
        fpsText.setLocalTranslation(new Vector3f(1, display.getHeight()*.5f, 1));
        rootNode.attachChild(fpsText);
        //-----------------------------------------
        
		/* Actualizo el nodo raiz */
		rootNode.updateGeometricState(0.0f, true);
		rootNode.updateRenderState();
		
		// Cateles con el puntaje y los mensajes al usuario
        scoreText = Text.createDefaultTextLabel("scoreText", "Score: " + String.valueOf(score));
        scoreText.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        scoreText.setLightCombineMode(Spatial.LightCombineMode.Off);
        scoreText.setLocalTranslation(new Vector3f(display.getWidth()* 3f/4, 5, 1));
        rootNode.attachChild(scoreText);
        
        messageText = Text.createDefaultTextLabel("messageText", message);
        messageText.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        messageText.setLightCombineMode(Spatial.LightCombineMode.Off);
        messageText.setLocalTranslation(new Vector3f(display.getWidth()/6, 5, 1));
        rootNode.attachChild(messageText);
        
        /* Aviso a la logica de juego que empieza uno */
        gameLogic.gameStart();
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

	private void buildLighting()
    {
        PointLight light = new PointLight();
        light.setDiffuse( new ColorRGBA( 0.75f, 0.75f, 0.75f, 0.75f ) );
        light.setAmbient( new ColorRGBA( 0.5f, 0.5f, 0.5f, 1.0f ) );
        light.setLocation( new Vector3f( 100, 100, 100 ) );
        light.setEnabled( true );
        
        lightState.attach( light );
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
	
	public DynamicPhysicsNode getPlunger()
	{
		return plunger;
	}
	
	public List<DynamicPhysicsNode> getFlippers()
	{
		return flippers;
	}
	
	public List<DynamicPhysicsNode> getJumperBumpers() 
	{
		return jumperBumpers;
	}
	
	public List<StaticPhysicsNode> getNoJumperBumpers() 
	{
		return noJumperBumpers;
	}
	
	public List<DynamicPhysicsNode> getDoors() 
	{
		return doors;
	}
	
	public List<DynamicPhysicsNode> getSpinners() 
	{
		return spinners;
	}
	
	public List<StaticPhysicsNode> getSensors() 
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
	
	public void addJumperBumper(DynamicPhysicsNode bumper)
	{
		jumperBumpers.add(bumper);
	}
	
	public void addNoJumperBumper(StaticPhysicsNode bumper)
	{
		noJumperBumpers.add(bumper);
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
	
	public void addSensor(StaticPhysicsNode sensor)
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
	
	public Vector3f getBallStartUp() 
    {
        return ballStartUp;
    }
    
    public LightState getLightState()
    {
        return lightState;
    }

    public void setGameLogic(GameLogic gameLogic)
    {
        this.gameLogic = gameLogic;
    }
    
    public void setLifes( int lifes )
    {
        // FIXME sorry no me compilaba y puse esto
        System.out.println("ERROR: linea 535 en pinball");
    }
    
	
	private void setUpBall()
	{
	    logger.info( "Construyendo pelota (haciendo pelota el pinball :)" );
/* TODO ojo, aca comento esto porque quiero probar solo con IRON
        // Defino un materia personalizado para poder setear las propiedades de interaccion con la mesa
        final Material customMaterial = new Material( "material de bola" );
        // Es pesado
        customMaterial.setDensity( 10.0f );
        // Detalles de contacto con el otro material
        MutableContactInfo contactDetails = new MutableContactInfo();
        // Poco rebote
        contactDetails.setBounce( 0.5f );
        // Poco rozamiento
        contactDetails.setMu( 0.5f );
        customMaterial.putContactHandlingDetails( pinballTableMaterial, contactDetails );
*/
        /* Nodo dinamico de la bola */
        final DynamicPhysicsNode mainBall = getPhysicsSpace().createDynamicNode();
        mainBall.setName( PHYSIC_NODE_NAME_FOR_BALLS );
        rootNode.attachChild( mainBall );

        final Sphere visualMainBall = new Sphere( "Bola", 10, 10, 0.25f );
        visualMainBall.setLocalTranslation( new Vector3f( this.ballStartUp ) );

        // Agregado de bounding volume 
        visualMainBall.setModelBound( new BoundingSphere() );
        visualMainBall.updateModelBound();

        mainBall.attachChild( visualMainBall );
        mainBall.generatePhysicsGeometry();
//		mainBall.setMaterial( customMaterial );
        mainBall.setMaterial(Material.IRON);
        // Se computa la masa luego de generar la geometria fisica
        mainBall.computeMass();

        // La agrego a la lista de bolas
        balls.add( mainBall );
        }
        
    public Spatial inclinePinball( Spatial table )
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
        for (DynamicPhysicsNode bumper : getJumperBumpers())
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
    
    // variables de debug
    private boolean pause = false;
    private boolean showDepth = false;
    private boolean showGraphs = false;
    private static final float g = -9.81f; 
    private Node tabla;
    
    protected void initDebug()
    {
        if ( com.jme.util.Debug.debug )
        {
            // P pausa fisica
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

            // Home reset de camara
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        cam.setLocation( pinballSettings.getCamStartPos() );

                        cam.lookAt( pinballSettings.getCamStartLookAt(), new Vector3f( 0.0f, 1.0f, -1.0f ) );
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_HOME, InputHandler.AXIS_NONE, false );


            // Y reset de pelota 
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        // falla miserablemente cuando pongamos mas de 1 bola :D
                        balls.get( 0 ).clearDynamics();
                        balls.get( 0 ).setLocalTranslation( new Vector3f(Vector3f.ZERO) );
                        balls.get( 0 ).setLocalRotation( new Quaternion() );
                        balls.get( 0 ).updateGeometricState( 0, false );
                    }
                }
                
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_Y, InputHandler.AXIS_NONE, false );
            
            // F1 screenshot
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

            // F2 mostrar reporte de memoria
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        long totMem = Runtime.getRuntime().totalMemory();
                        long freeMem = Runtime.getRuntime().freeMemory();
                        long maxMem = Runtime.getRuntime().maxMemory();

                        System.out.println( "|*|*|  Memory Stats  |*|*|" );
                        System.out.println( "Total memory: " + ( totMem >> 10 ) + " kb" );
                        System.out.println( "Free memory: " + ( freeMem >> 10 ) + " kb" );
                        System.out.println( "Max memory: " + ( maxMem >> 10 ) + " kb" );
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
            pinballInputHandler.addAction( new InputAction()
            {
                 
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        showGraphs = !showGraphs;
                        com.jmex.game.state.StatisticsGameState stats = (com.jmex.game.state.StatisticsGameState)com.jmex.game.state.GameStateManager.getInstance().getChild("stats");
                        if (stats == null) {
                            stats = new com.jmex.game.state.StatisticsGameState("stats", 1f, 0.25f, 0.75f, true);
                            com.jmex.game.state.GameStateManager.getInstance().attachChild( stats );
                        }

                        if (showGraphs) {
                            stats.setActive( true );
                        } else {
                            stats.setActive( false );
                        }
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F4, InputHandler.AXIS_NONE, false );


            // F5 mostrar wires
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

            // F6 apagar luces
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

            // F7 mostrar bounding boxes visuales
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

            // F8 mostrar normales
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

            // F9 mostrar posicion de la camara
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        
                        Vector3f pos = display.getRenderer().getCamera().getLocation();
                        message = "cam[" + String.format( "%.2f", pos.x ) + ";" + String.format( "%.2f", pos.y ) + ";" + String.format( "%.2f", pos.z ) + "]";
                        messageText.getText().replace(0, messageText.getText().length(), "" + message);
                        System.out.println( "Camera at: " + display.getRenderer().getCamera().getLocation() );
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F9, InputHandler.AXIS_NONE, false );

            // F11 mostrar el mouse 
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        MouseInput.get().setCursorVisible( !MouseInput.get().isCursorVisible() );
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F11, InputHandler.AXIS_NONE, false );
            
            // INS subir angulo fisico
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {

                        float angle = FastMath.RAD_TO_DEG * FastMath.atan( getPhysicsSpace().getDirectionalGravity( null ).y / -getPhysicsSpace().getDirectionalGravity( null ).z);
                        
                        if (angle > 89)
                        {
                            angle = 1;
                        }
                        else
                        {
                            angle += 1;
                        }
                        getPhysicsSpace().setDirectionalGravity( new Vector3f(0, FastMath.cos( FastMath.DEG_TO_RAD * angle ) * g, -FastMath.sin( FastMath.DEG_TO_RAD * angle ) * g) );                        
                        System.out.println( "angulo = " + angle );
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_INSERT, InputHandler.AXIS_NONE, false );
            
            // DEL bajar angulo fisico
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        float angle = FastMath.RAD_TO_DEG * FastMath.atan( getPhysicsSpace().getDirectionalGravity( null ).y / -getPhysicsSpace().getDirectionalGravity( null ).z);
                        
                        if (angle < 2)
                        {
                            angle = 89;
                        }
                        else
                        {
                            angle -= 1;
                        }
                        getPhysicsSpace().setDirectionalGravity( new Vector3f(0, FastMath.cos( FastMath.DEG_TO_RAD * angle ) * g, -FastMath.sin( FastMath.DEG_TO_RAD * angle ) * g) );                        
                        System.out.println( "angulo = " + angle );
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_DELETE, InputHandler.AXIS_NONE, false );
            
            //estas son para debug... es mala idea pasarlas a estable porque se rompe la tabla
            // PGUP subir angulo visual
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
            
            // PGDOWN bajar angulo visual
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
            // esto debería forzar el update... no creo que valga la pena
            pinballInputHandler.addAction( new InputAction() {
                public void performAction( InputActionEvent evt ) {
                    if ( evt.getTriggerPressed() ) {
                        float tpf =  timer.getTimePerFrame();
                        update( tpf );
                        rootNode.updateGeometricState(tpf, true);
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_ADD, InputHandler.AXIS_NONE, false );
            // esto debería cambiar la profundidad... tampoco creo que valga la pena
            pinballInputHandler.addAction( new InputAction() 
            {
                public void performAction( InputActionEvent evt ) {
                    if ( evt.getTriggerPressed() ) {
                        showDepth = !showDepth;
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F10, InputHandler.AXIS_NONE, false );
            
        }
        
    }
    
}
