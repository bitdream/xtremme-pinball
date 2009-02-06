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
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Spatial;
import com.jme.scene.Text;
import com.jme.scene.shape.Quad;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.util.NanoTimer;
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
import components.Sensor;
import components.Spinner;
import components.Sensor.SensorType;

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
	private static final String GAME_VERSION = "0.6";
	
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
	
	/* Vidas que quedan */
	private int lifes;
	
	/* HUD -------------------------------------------------- */
	
	/* Barra superior del HUD */
	private Quad hudTopBar;
	
	/* Texto con el score, para mostrar al usuario */
	private Text scoreText;
	
	/* Texto con la cantidad de vidas restantes, para mostrar al usuario */
	private Text lifesRemainingText;
	
	/* Texto para mostrar los fps */
	private Text fpsText;
	
	/* Textos para titulos */
	private Text scoreTitleText, lifesRemainingTitleText;
	
	/* Mensaje para mostrar al usuario */
	private Text messageTextBottom, messageTextMiddle, messageTextTop;
	
	/* Longitud maxima de un mensaje */
	private static final int MAX_GAME_MESSAGE_LEN = 40;
	
	/* Colores de las barras */
	private ColorRGBA hudBackgroundColor = new ColorRGBA(0.2f, 0.2f, 0.4f, 1f);
	private ColorRGBA hudBackgroundBorderColor = new ColorRGBA(0f, 0f, 0f, 1f);
	
	/* Color del texto */
	private ColorRGBA hudTextColor = new ColorRGBA(0f, 0.7f, 0.7f, 1f);
	
	/* Ancho en % del ancho total para los paneles de puntaje y vidas */
	private final float scoreLifesPanelWidth = 0.09f;
	
	/* Posiciones en % de la altura del panel de mensajes */
	private float topTextPosition = 0.23f;
	private float middleTextPosition = 0.55f;
	private float bottomTextPosition = 1f;
	
	/* Escalas de los textos */
	private float topTextScale, middleTextScale, bottomTextScale, scoreAndLifesTextScale;
	
	/* Coeficientes de las rectas que estiman los porcentajes de escalado para las distintas resoluciones */
	private static final float SL_SCALE_COEF_A = 0.0010119f, SL_SCALE_COEF_B = 0.222857f;
	private static final float TT_SCALE_COEF_A = 0.0002976f, TT_SCALE_COEF_B = 0.5714f;
	private static final float MT_SCALE_COEF_A = 0.000595238f, MT_SCALE_COEF_B = 0.542857f;
	private static final float BT_SCALE_COEF_A = 0.00238095f, BT_SCALE_COEF_B = 0.17143f;
	
	/* ------------------------------------------------------ */
	
	/* Mostrar o no los FPS */
	private boolean showFPS = false;
	
	/* Logica de juego */
	private GameLogic gameLogic;
	
	/* Timer para los FPS */
	protected Timer timer;
    private float lastSampleTime = 0;
    
    /* Carga de la mesa completa */
    private boolean loadingComplete = false;

    /* Pausa de la fisica */
    private boolean pause = false;
    
	/* XXX Ubicacion inicial de la bola: cable */
	private Vector3f ballStartUp = new Vector3f( 4.88f, 0.5f, -1.60f );


	/**
	 * Crea un estado de juego nuevo.
	 * @param name Nombre del estado de juego.
	 * @param pinballSettings Settings del pinball a iniciar.
	 */
	public PinballGameState(String name, PinballGameStateSettings pinballSettings)
	{
		super(name);
		
		logger.info("Pinball Game State creado");
		
		this.pinballSettings = pinballSettings;
	
		/* Adquiero el timer para calcular los FPS */
		timer = new NanoTimer();
		
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
        	super.update(tpf * 3);
        	
            /* Actualizo los componentes que asi lo requieren */
            updateComponents(interpolation);
        }

        if (showFPS)
        {
	        float timeMS = timer.getTime() * 1f/1000000;
	
	        if (timeMS - lastSampleTime > 1000)
	        {
	            fpsText.getText().replace(0, fpsText.getText().length(), Integer.toString((int)timer.getFrameRate()) + " fps");
	            lastSampleTime = timeMS;
	        }
        }

        /* Actualizo el sistema de sonido */
        Main.getAudioSystem().update();
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
		
		/* desactivamos el input hasta que no se pueda jugar */
		pinballInputHandler.setEnabled( false );
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
//        addBall(this.ballStartUp); -> hecho desde la logica (metodo startGame())
        ballStartUp.rotate( pinballSettings.getInclinationQuaternion() );
        
        // Inicializo las vidas
        lifes = gameLogic.getLifes();
        
        /* Construyo el HUD */
        buildHUD();
        
		/* Actualizo el nodo raiz */
		rootNode.updateGeometricState(0.0f, true);
		rootNode.updateRenderState();
        
        /* Aviso a la logica de juego que empieza uno */
        gameLogic.gameStart();
        
        /* Una vez que esta todo seteado, dejamos tocar las teclas */
        pinballInputHandler.setEnabled( true );  
	}
	
	private void buildHUD()
	{
		/* Estimo los porcentajes de escalado segun resolucion */
		estimateScalingPercentages();
		
		/* Barra superior del HUD */
		hudTopBar = new Quad("HUD Top Bar", display.getWidth(), 0.08f * display.getHeight());
		hudTopBar.setRenderQueueMode(Renderer.QUEUE_ORTHO);
		hudTopBar.setLightCombineMode(Spatial.LightCombineMode.Off);
		hudTopBar.setSolidColor(hudBackgroundColor);
		hudTopBar.setLocalTranslation(new Vector3f(display.getWidth() * 0.5f, display.getHeight() - hudTopBar.getHeight() * 0.5f, 0));
        rootNode.attachChild(hudTopBar);
        
        /* Borde inferior de la barra superior del HUD */
        Quad hudTopBarBorder = new Quad("HUD Top Bar Border", display.getWidth(), 2);
        hudTopBarBorder.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        hudTopBarBorder.setLightCombineMode(Spatial.LightCombineMode.Off);
        hudTopBarBorder.setSolidColor(hudBackgroundBorderColor);
        hudTopBarBorder.setLocalTranslation(new Vector3f(display.getWidth() * 0.5f, display.getHeight() - hudTopBar.getHeight() - hudTopBarBorder.getHeight() * 0.5f, 0));
        rootNode.attachChild(hudTopBarBorder);
        
        /* Borde divisor de las vidas */
        Quad hudTopBarLeftBorder = new Quad("HUD Top Bar Left Border", 1, hudTopBar.getHeight());
        hudTopBarLeftBorder.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        hudTopBarLeftBorder.setLightCombineMode(Spatial.LightCombineMode.Off);
        hudTopBarLeftBorder.setSolidColor(hudBackgroundBorderColor);
        hudTopBarLeftBorder.setLocalTranslation(new Vector3f(display.getWidth() * scoreLifesPanelWidth, hudTopBar.getLocalTranslation().y, 0));
        rootNode.attachChild(hudTopBarLeftBorder);
        
        /* Borde divisor del puntaje */
        Quad hudTopBarRightBorder = new Quad("HUD Top Bar Right Border", 1, hudTopBar.getHeight());
        hudTopBarRightBorder.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        hudTopBarRightBorder.setLightCombineMode(Spatial.LightCombineMode.Off);
        hudTopBarRightBorder.setSolidColor(hudBackgroundBorderColor);
        hudTopBarRightBorder.setLocalTranslation(new Vector3f(display.getWidth() * (1 - scoreLifesPanelWidth), hudTopBar.getLocalTranslation().y, 0));
        rootNode.attachChild(hudTopBarRightBorder);
        
        /* Creo los textos del HUD para mensajes */
        
        /* Mensaje superior */
        messageTextTop = Text.createDefaultTextLabel("messageText", "");
        messageTextTop.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        messageTextTop.setLightCombineMode(Spatial.LightCombineMode.Off);
        messageTextTop.setLocalTranslation(new Vector3f(display.getWidth() * 0.5f - messageTextTop.getWidth() * 0.5f, display.getHeight() - topTextPosition * hudTopBar.getHeight(), 1));
        messageTextTop.setTextColor(hudTextColor);
        messageTextTop.setLocalScale(topTextScale);
        rootNode.attachChild(messageTextTop);
        
        /* Mensaje central */
        messageTextMiddle = Text.createDefaultTextLabel("messageText", "");
        messageTextMiddle.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        messageTextMiddle.setLightCombineMode(Spatial.LightCombineMode.Off);
        messageTextMiddle.setLocalTranslation(new Vector3f(display.getWidth() * 0.5f - messageTextMiddle.getWidth() * 0.5f, display.getHeight() - middleTextPosition * hudTopBar.getHeight(), 1));
        messageTextMiddle.setTextColor(hudTextColor);
        messageTextMiddle.setLocalScale(middleTextScale);
        rootNode.attachChild(messageTextMiddle);
        
        /* Mensaje inferior */
        messageTextBottom = Text.createDefaultTextLabel("messageText", "");
        messageTextBottom.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        messageTextBottom.setLightCombineMode(Spatial.LightCombineMode.Off);
        messageTextBottom.setLocalTranslation(new Vector3f(display.getWidth() * 0.5f - messageTextBottom.getWidth() * 0.5f, display.getHeight() - bottomTextPosition * hudTopBar.getHeight(), 1));
        messageTextBottom.setTextColor(hudTextColor);
        messageTextBottom.setLocalScale(bottomTextScale);
        rootNode.attachChild(messageTextBottom);
        
        /* Texto de fps */
        fpsText = Text.createDefaultTextLabel("fpsText", "");
        fpsText.setLocalScale(0.8f);
        fpsText.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        fpsText.setLightCombineMode(Spatial.LightCombineMode.Off);
        fpsText.setLocalTranslation(new Vector3f(2, 2, 1));
        rootNode.attachChild(fpsText);
        
		/* Puntaje */
        scoreTitleText = Text.createDefaultTextLabel("scoreTitleText", gameLogic.getScoreText());
        scoreTitleText.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        scoreTitleText.setLightCombineMode(Spatial.LightCombineMode.Off);
        scoreTitleText.setLocalTranslation(new Vector3f(display.getWidth() * (1 - 0.5f * scoreLifesPanelWidth) - 0.5f * scoreTitleText.getWidth(), display.getHeight() - 0.45f * hudTopBar.getHeight(), 1));
        scoreTitleText.setTextColor(hudTextColor);
        scoreTitleText.setLocalScale(scoreAndLifesTextScale);
        rootNode.attachChild(scoreTitleText);
        
        scoreText = Text.createDefaultTextLabel("scoreText", String.valueOf(score));
        scoreText.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        scoreText.setLightCombineMode(Spatial.LightCombineMode.Off);
        scoreText.setLocalTranslation(new Vector3f(display.getWidth() * (1 - 0.5f * scoreLifesPanelWidth) - 0.5f * scoreText.getWidth(), display.getHeight() - 0.85f * hudTopBar.getHeight(), 1));
        scoreText.setTextColor(hudTextColor);
        scoreText.setLocalScale(scoreAndLifesTextScale);
        rootNode.attachChild(scoreText);
        
        /* Vidas restantes */
        lifesRemainingTitleText = Text.createDefaultTextLabel("lifesRemainingTitleText", gameLogic.getLifesText());
        lifesRemainingTitleText.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        lifesRemainingTitleText.setLightCombineMode(Spatial.LightCombineMode.Off);
        lifesRemainingTitleText.setLocalTranslation(new Vector3f((scoreLifesPanelWidth * display.getWidth() - lifesRemainingTitleText.getWidth()) * 0.5f, display.getHeight() - 0.45f * hudTopBar.getHeight(), 1));
        lifesRemainingTitleText.setTextColor(hudTextColor);
        lifesRemainingTitleText.setLocalScale(scoreAndLifesTextScale);
        rootNode.attachChild(lifesRemainingTitleText);
        
        lifesRemainingText = Text.createDefaultTextLabel("lifesRemainingText", String.valueOf(lifes));
        lifesRemainingText.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        lifesRemainingText.setLightCombineMode(Spatial.LightCombineMode.Off);
        lifesRemainingText.setLocalTranslation(new Vector3f((scoreLifesPanelWidth * display.getWidth() - lifesRemainingText.getWidth()) * 0.5f, display.getHeight() - 0.85f * hudTopBar.getHeight(), 1));
        lifesRemainingText.setTextColor(hudTextColor);
        lifesRemainingText.setLocalScale(scoreAndLifesTextScale);
        rootNode.attachChild(lifesRemainingText);
	}
	
	private void estimateScalingPercentages()
	{
		topTextScale = TT_SCALE_COEF_A * display.getHeight() + TT_SCALE_COEF_B;
		middleTextScale = MT_SCALE_COEF_A * display.getHeight() + MT_SCALE_COEF_B;
		bottomTextScale = BT_SCALE_COEF_A * display.getHeight() + BT_SCALE_COEF_B;
		scoreAndLifesTextScale = SL_SCALE_COEF_A * display.getHeight() + SL_SCALE_COEF_B;
	}
	
	// Para poder reiniciar el juego. Es invocado por el inputHandler (tecla N)
	public void reinitGame()
	{
		//initGame();
		gameLogic.gameStart();
		
        // Inicializo las vidas
        lifes = gameLogic.getLifes();

        // Los carteles ya fueron creados en el initGame() y sus valores los setea la logica
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
	
	public void toggleShowFPS()
	{
		showFPS = !showFPS;
		
		/* Si no debo mostrar mas los fps borro el contenido del
		 * texto (y no se va a actualizar mas hasta que no se prenda de nuevo) */
		if (!showFPS)
		{
			fpsText.getText().delete(0, fpsText.getText().length());
		}
	}

	public void setScore(int score)
	{
		this.score = score;
		
		/* Actualizo el texto de score en pantalla */
		scoreText.getText().replace(0, scoreText.getText().length(), String.valueOf(score));
	}
	
	public void setLifes(int lifes)
	{
		this.lifes = lifes;
		
		/* Actualizo el texto de vidas en pantalla */
		lifesRemainingText.getText().replace(0, lifesRemainingText.getText().length(), String.valueOf(lifes));
	}

	public void showGameMessage(String message)
	{
		int len = message.length();
		String newMessage = message;
		
		/* Si supera el tamanio maximo, lo trunco */
		if (len > MAX_GAME_MESSAGE_LEN)
			newMessage = message.substring(0, MAX_GAME_MESSAGE_LEN);
		
		/* Coloco el texto del medio en el superior */
		messageTextTop.getText().replace(0, messageTextTop.getText().length(), "" + messageTextMiddle.getText());
		
		/* Coloco el texto de abajo en el del medio */
		messageTextMiddle.getText().replace(0, messageTextMiddle.getText().length(), "" + messageTextBottom.getText());
		
		/* Coloco el nuevo mensaje en el de abajo */
        messageTextBottom.getText().replace(0, messageTextBottom.getText().length(), "" + newMessage);
        
        /* Reposiciono los mensajes */
        messageTextTop.setLocalTranslation(new Vector3f(display.getWidth() * 0.5f - messageTextTop.getWidth() * 0.5f, display.getHeight() - topTextPosition * hudTopBar.getHeight(), 1));
        messageTextMiddle.setLocalTranslation(new Vector3f(display.getWidth() * 0.5f - messageTextMiddle.getWidth() * 0.5f, display.getHeight() - middleTextPosition * hudTopBar.getHeight(), 1));
        messageTextBottom.setLocalTranslation(new Vector3f(display.getWidth() * 0.5f - messageTextBottom.getWidth() * 0.5f, display.getHeight() - bottomTextPosition * hudTopBar.getHeight(), 1));
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
    
    // Se basa en la posicion del sensor de rampa luego de rotada la mesa.
    public Vector3f getExtraBallStartUp()
    {
		Vector3f sensorOriginalPos = new Vector3f();
		Vector3f sensorRotatedPos = new Vector3f();
		
    	// Se que hay un solo sensor de rampa
    	for (StaticPhysicsNode sensor : sensors) 
    	{
			Sensor s = (Sensor) sensor.getChild(0);
			
			if (s.getSensorType().equals(SensorType.RAMP_SENSOR))
			{
				sensorOriginalPos = s.getVisualModel().getLocalTranslation();
				sensorRotatedPos = sensorOriginalPos.rotate(getPinballSettings().getInclinationQuaternion());
			}			
		}
    	// Una vez que se las coordenadas, retorno un vector que sea un poco mas a la izquierda (-x) y arriba (+y). La bola es de diametro 0.5
    	return sensorRotatedPos.add(new Vector3f(-0.5f, 0.5f, 0f));
    }
    
    public void addBall(Vector3f location)
	{
        /* Nodo dinamico de la bola */
        final DynamicPhysicsNode ball = getPhysicsSpace().createDynamicNode();
        ball.setName( PHYSIC_NODE_NAME_FOR_BALLS );
        rootNode.attachChild( ball );

        final Sphere visualMainBall = new Sphere( "Bola", /*10*/25, /*10*/25, 0.25f );
        visualMainBall.setLocalTranslation( location );

        // Agregado de bounding volume 
        visualMainBall.setModelBound( new BoundingSphere() );
        visualMainBall.updateModelBound();

        ball.attachChild( visualMainBall );
        ball.generatePhysicsGeometry();
        ball.setMaterial(Material.IRON);
        
        // Se computa la masa luego de generar la geometria fisica
        ball.computeMass();

        ball.updateGeometricState(0, true);
        
        // La agrego a la lista de bolas
        balls.add( ball );
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

    public boolean isLoadingComplete()
    {
        return loadingComplete;
    }

    public void setLoadingComplete(boolean loadingComplete)
    {
        this.loadingComplete = loadingComplete;
    }

    public void togglePause()
    {
        pause = ! pause;
    }
    
    ///XXX debug
    // variables de debug
    private boolean showGraphs = false;
    
    protected void initDebug()
    {
        if ( com.jme.util.Debug.debug )
        {
            boolean luces = false;
            if (luces)
            {
                com.jme.light.PointLight light = new com.jme.light.PointLight();
                light.setDiffuse( new ColorRGBA( 0.75f, 0.75f, 0.75f, 0.75f ) );
                light.setAmbient( new ColorRGBA( 0.5f, 0.5f, 0.5f, 1.0f ) );
                light.setLocation( new Vector3f( 100, 100, 100 ) );
                light.setEnabled( true );
                
                lightState.attach( light );
            }

//            // Y reset de pelota 
//            pinballInputHandler.addAction( new InputAction()
//            {
//                public void performAction( InputActionEvent evt )
//                {
//                    if ( evt.getTriggerPressed() )
//                    {
//                        
//                        if (balls.size() == 0)
//                            addBall(ballStartUp);
//                        
//                        balls.get( 0 ).clearDynamics();
//                        balls.get( 0 ).setLocalTranslation( new Vector3f(Vector3f.ZERO) );
//                        balls.get( 0 ).setLocalRotation( new Quaternion() );
//                        balls.get( 0 ).updateGeometricState( 0, false );
//                        
//                    }
//                }
//                
//            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_Y, InputHandler.AXIS_NONE, false );
            
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
                        showGameMessage("cam[" + String.format( "%.2f", pos.x ) + ";" + String.format( "%.2f", pos.y ) + ";" + String.format( "%.2f", pos.z ) + "]");
                        System.out.println( "Camera at: " + display.getRenderer().getCamera().getLocation() );
                        System.out.println( "Camera facing to " + display.getRenderer().getCamera().getDirection());
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F9, InputHandler.AXIS_NONE, false );

            // F10 mostrar el mouse 
            pinballInputHandler.addAction( new InputAction()
            {
                public void performAction( InputActionEvent evt )
                {
                    if ( evt.getTriggerPressed() )
                    {
                        MouseInput.get().setCursorVisible( !MouseInput.get().isCursorVisible() );
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F10, InputHandler.AXIS_NONE, false );
            
            
            /* libre para pruebas */
            pinballInputHandler.addAction( new InputAction() 
            {
                public void performAction( InputActionEvent evt ) {
                    if ( evt.getTriggerPressed() ) {
                        
                    }
                }
            }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F11, InputHandler.AXIS_NONE, false );
            
        }
        
    }

}
