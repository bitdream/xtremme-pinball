package gamestates;

import gamelogic.GameLogic;
import input.PinballInputHandler;

import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import loader.X3DLoader;
import main.Main;

import com.jme.bounding.BoundingBox;
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
import com.jme.scene.TexCoords;
import com.jme.scene.Text;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.CullState;

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
import components.Sensor;
import components.Spinner;
import components.Flipper.FlipperType;
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
	
	private static final String GAME_NAME = "xtremme pinball";
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
	private String message = "";
	
	private Text fpsText;
	
	/* Mensaje para mostrar al usuario */
	private Text messageText;
	
	/* Logica de juego */
	private GameLogic gameLogic;
	
	/* Timer para los FPS */
	protected Timer timer;

	/* XXX Ubicacion inicial de la bola: cable */
	private Vector3f ballStartUp = new Vector3f( 4.88f, 1.2f, -3.0f ) /*new Vector3f( 15,15,-51 )*/ /*new Vector3f( 1, 16, -58)*/; //1, 16, -58
	//new Vector3f(-3.22f,20,-15.37485f);
	/* Ubicacion inicial de la camara */
	private Vector3f cameraStartUp = 
	    new Vector3f( 0.0f, 15.0f, 4.0f ); 
//      new Vector3f(-7.8f,11.6f,-63.6f);
//	    new Vector3f(0,50,80);
	/* Lugar al que mira la camara incialmente */
	private Vector3f cameraLookAt = 
	    new Vector3f( 0.0f, 9.875f, 0.0f );
//     new Vector3f(-6.8f,11.6f,-62.6f);
//	    Vector3f.ZERO;
	    
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
		// TODO No deberiamos estar acelerando la fisica, pero bueno, aca esta la llamada, la tenemos 3 veces mas rapida.
//		super.update(tpf * 3f); // Con esto el juego anda mas fluido, no ponerlo dentro del if (!pause)!!!

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
            // TODO No deberiamos estar acelerando la fisica, pero bueno, aca esta la llamada, la tenemos 3 veces mas rapida.
        	super.update(tpf * 3);
        	
            /* Actualizo los componentes que asi lo requieren */
            updateComponents(interpolation);
        
            /* Se modifico la escena, entonces actualizo el grafo */
            // TODO No deberiamos estar acelerando la fisica, pero bueno, aca esta la llamada, la tenemos 3 veces mas rapida.
            // super.update(tpf);
//            super.update(tpf * 3f);
            
            // rootNode.updateGeometricState(interpolation, true);   // se hace en el super.update esto     
        }
        
        /* Se actualiza la info que se presenta en pantalla (score y mensajes) */
        scoreText.getText().replace(0, scoreText.getText().length(), "Score: " + score);
        messageText.getText().replace(0, messageText.getText().length(), "" + message);
        fpsText.getText().replace( 4, fpsText.getText().length(), Integer.toString( (int)timer.getFrameRate()/2 ) );
        
//    	for (DynamicPhysicsNode bumper : getJumperBumpers()) 
//    	{
//    		float remainingFrames = ((Bumper)bumper.getChild(0)).getRemainigFramesInMovement();
//			if ( remainingFrames > 0.0f )
//			{
//				// Restarle 1 y si queda menor o igual a cero, ponerlo en cero y limpiarle la fisica para que deje de moverse
//				remainingFrames--;
//				if (remainingFrames <= 0.0f)
//				{
//					bumper.clearDynamics();
//					((Bumper)bumper.getChild(0)).setRemainigFramesInMovement(0.0f);
//				}
//			}
//		}
        
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
        sensors = new ArrayList<DynamicPhysicsNode>(2);
		
		/* Creo la lista de bolas */
		balls = new ArrayList<DynamicPhysicsNode>(4);

		/* Armo la habitacion, la mesa y la bola */
//        loadEnvironment();
        loadTable();
        setUpBall();
		
        
		// ----------------------------------------
		// TODO volar: es para debug
//        buildTable();
//        gameLogic = new themes.CarsThemeGameLogic(this);
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
        scoreText.setLocalTranslation(new Vector3f(display.getWidth()* 3f/4, 5, 1));
        rootNode.attachChild(scoreText);
        
        messageText = Text.createDefaultTextLabel("messageText", message);
        messageText.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        messageText.setLightCombineMode(Spatial.LightCombineMode.Off);
        messageText.setLocalTranslation(new Vector3f(display.getWidth()/6, 5, 1));
        rootNode.attachChild(messageText);
        
        fpsText = Text.createDefaultTextLabel( "fpsText", "fps " + timer.getFrameRate() );
        fpsText.setLocalScale( 0.80f );
        fpsText.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        fpsText.setLightCombineMode(Spatial.LightCombineMode.Off);
        fpsText.setLocalTranslation(new Vector3f(1, display.getHeight()*.5f, 1));
        rootNode.attachChild(fpsText);
        
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
		for (DynamicPhysicsNode bumper : getJumperBumpers())
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
		mainBall.setName(PHYSIC_NODE_NAME_FOR_BALLS);
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
		mainBall2.setName(PHYSIC_NODE_NAME_FOR_BALLS);
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
		mainBall3.setName(PHYSIC_NODE_NAME_FOR_BALLS);
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
        java.nio.IntBuffer indexBuffer = com.jme.util.geom.BufferUtils.createIntBuffer(RflipIndices);
        java.nio.FloatBuffer vertexBuffer = com.jme.util.geom.BufferUtils.createFloatBuffer(RflipVertices);
        java.nio. FloatBuffer colorBuffer = com.jme.util.geom.BufferUtils.createFloatBuffer((float[])null);
        java.nio.FloatBuffer normalBuffer = com.jme.util.geom.BufferUtils.createFloatBuffer((float[])null);
        
        float[] texCoord;
        {
            float[] vertices = RflipVertices;
            float[] texCoords = new float[vertices.length / 3 * 2];

            float minX = 0;
            float maxX = 0;
            float minY = 0;
            float maxY = 0;
            float minZ = 0;
            float maxZ = 0;

            // Get the extents of the object
            for (int i = 0; i * 3 < vertices.length; i++) {
                minX = Math.min(minX, vertices[i * 3 + 0]);
                maxX = Math.max(maxX, vertices[i * 3 + 0]);
                minY = Math.min(minY, vertices[i * 3 + 1]);
                maxY = Math.max(maxY, vertices[i * 3 + 1]);
                minZ = Math.min(minZ, vertices[i * 3 + 2]);
                maxZ = Math.max(maxZ, vertices[i * 3 + 2]);
            }

            // The coordinate with the largest extent is s, the second largest t
            float xExtent = maxX - minX;
            float yExtent = maxY - minY;
            float zExtent = maxZ - minZ;
            int sCoord = 0;
            int tCoord = 1;
            // default order: X-Y-Z
            if (yExtent > xExtent) {
                if (zExtent > yExtent) { // order: Z-Y-X
                    sCoord = 2;
                    tCoord = 1;
                } else {
                    sCoord = 1;
                    if (zExtent > xExtent) { // order: Y-Z-X
                        tCoord = 2;
                    } else { // order: Y-X-Z
                        tCoord = 0;
                    }
                }
            } else {
                if (zExtent > xExtent) { // order: Z-X-Y
                    sCoord = 2;
                    tCoord = 0;
                } else {
                    if (zExtent > yExtent) { // order: X-Z-Y
                        tCoord = 2;
                    } // else order: X-Y-Z (default)
                }
            }

            // Fill the texCoords array
            float[] minValues = { minX, minY, minZ };
            float[] extents = { xExtent, yExtent, zExtent };
            for (int i = 0; i * 3 < vertices.length; i++) {
                texCoords[i * 2 + 0] = (vertices[i * 3 + sCoord] - minValues[sCoord])
                        / extents[sCoord];
                texCoords[i * 2 + 1] = (vertices[i * 3 + tCoord] - minValues[tCoord])
                        / extents[tCoord];
            }

            texCoord = texCoords;
        }
   
        java.nio.FloatBuffer texCoordBuffer = com.jme.util.geom.BufferUtils
                .createFloatBuffer(texCoord);
        final TriMesh rightVisualFlipper = 
            new TriMesh("Right visual flipper", vertexBuffer, normalBuffer, colorBuffer,
                new TexCoords(texCoordBuffer), indexBuffer);
        new com.jme.util.geom.NormalGenerator().generateNormals(rightVisualFlipper, 0);
		//final Box rightVisualFlipper = new Box("Right visual flipper", new Vector3f(), 7, 1, 2);
        
        rightVisualFlipper.getLocalRotation().fromAngleAxis(3.048941f,
            new Vector3f(0.046361f, 0.706364f, -0.706329f));
        rightVisualFlipper.setLocalScale( new Vector3f(2.288804f, 2.288804f, 16.101135f).mult( 2 ) );
        
		rightVisualFlipper.setLocalTranslation(new Vector3f(11, 3.1f, 70));
		rightVisualFlipper.setModelBound(new BoundingBox());
		rightVisualFlipper.updateModelBound();
		
		/* Le doy color */
		Utils.color(rightVisualFlipper, new ColorRGBA(0f, 1.0f, 0f, 1.0f), 128);
		
		DynamicPhysicsNode rightTestFlipper = Flipper.create(this, "Physic right flipper", rightVisualFlipper, FlipperType.RIGHT_FLIPPER, new Vector3f(14,4.2f,68.5f));
		rootNode.attachChild(rightTestFlipper);
		
		final Box leftVisualFlipper = new Box("Left visual flipper", new Vector3f(), 7, 1, 2);
		leftVisualFlipper.setLocalTranslation(new Vector3f(-11, 3, 70));
		leftVisualFlipper.setModelBound(new BoundingBox());
		leftVisualFlipper.updateModelBound();
		
		/* Le doy color */
		Utils.color(leftVisualFlipper, new ColorRGBA(0f, 1.0f, 0f, 1.0f), 128);
		
		DynamicPhysicsNode leftTestFlipper = Flipper.create(this, "Physic left flipper", leftVisualFlipper, FlipperType.LEFT_FLIPPER, null);
		rootNode.attachChild(leftTestFlipper);
		
		final Box leftSmallVisualFlipper = new Box("Left small visual flipper", new Vector3f(), 3.5f, 0.5f, 1);
		leftSmallVisualFlipper.setLocalTranslation(new Vector3f(-19, 3, -20));
		leftSmallVisualFlipper.setModelBound(new BoundingBox());
		leftSmallVisualFlipper.updateModelBound();
		
		/* Le doy color */
		Utils.color(leftSmallVisualFlipper, new ColorRGBA(0f, 1.0f, 0f, 1.0f), 128);
		
		DynamicPhysicsNode leftSmallTestFlipper = Flipper.create(this, "Physic small left flipper", leftSmallVisualFlipper, FlipperType.LEFT_FLIPPER, null);
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
		
		DynamicPhysicsNode testDoor = Door.create(this, "Physic door", visualDoor, Door.DoorType.RIGHT_DOOR, -1.3f, 0.5f, null);
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
		DynamicPhysicsNode bumper1 = Bumper.createJumperBumper(this, "Physic bumper 1", visualBumper1);
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
		Utils.color( visualLostBallSensor, new ColorRGBA( 0f, 1f, 0f, 0.5f ), 120 );
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
		
		Box visualTable = new Box( "Table", new Vector3f(), 30, 1, 80);
	
		table.attachChild(visualTable);		
		table.generatePhysicsGeometry();
		
		// Seteo el material de la mesa
		table.setMaterial( pinballTableMaterial );
		
		// Seteo el color de la mesa. Brillo al maximo
	    Utils.color( table, new ColorRGBA( 0.5f, 0.5f, 0.9f, 1.0f ), 128 );

	    
	    // pared izq
	    table = getPhysicsSpace().createStaticNode();
        rootNode.attachChild(table);
        
        visualTable = new Box( "left", new Vector3f(), 1, 4, 80);
        visualTable.setLocalTranslation( new Vector3f(-30,0,0) );
    
        table.attachChild(visualTable);     
        table.generatePhysicsGeometry();
        
        // Seteo el material de la mesa
        table.setMaterial( pinballTableMaterial );
        
        // Seteo el color de la mesa. Brillo al maximo
        Utils.color( table, new ColorRGBA( 0.5f, 0.5f, 0.9f, 1.0f ), 128 );
        
        // pared der
        table = getPhysicsSpace().createStaticNode();
        rootNode.attachChild(table);
        
        visualTable = new Box( "left", new Vector3f(), 1, 4, 80);
        visualTable.setLocalTranslation( new Vector3f(30,0,0) );
    
        table.attachChild(visualTable);     
        table.generatePhysicsGeometry();
        
        // Seteo el material de la mesa
        table.setMaterial( pinballTableMaterial );
        
        // Seteo el color de la mesa. Brillo al maximo
        Utils.color( table, new ColorRGBA( 0.5f, 0.5f, 0.9f, 1.0f ), 128 );
        
        //fondo
        table = getPhysicsSpace().createStaticNode();
        rootNode.attachChild(table);
        
        visualTable = new Box( "left", new Vector3f(), 30, 4, 1);
        visualTable.setLocalTranslation( new Vector3f(0,0,-80) );
    
        table.attachChild(visualTable);     
        table.generatePhysicsGeometry();
        
        // Seteo el material de la mesa
        table.setMaterial( pinballTableMaterial );
        
        // Seteo el color de la mesa. Brillo al maximo
        Utils.color( table, new ColorRGBA( 0.5f, 0.5f, 0.9f, 1.0f ), 128 );
        
        
        // slider izq
        table = getPhysicsSpace().createStaticNode();
        rootNode.attachChild(table);
        
        visualTable = new Box( "left", new Vector3f(), 8, 4, 1);
        visualTable.setLocalTranslation( new Vector3f(25,0,60) );
        visualTable.setLocalRotation( new Quaternion( ).fromAngles(0,FastMath.DEG_TO_RAD * 45f,0));
    
        table.attachChild(visualTable);     
        table.generatePhysicsGeometry();
        
        // Seteo el material de la mesa
        table.setMaterial( pinballTableMaterial );
        
        // Seteo el color de la mesa. Brillo al maximo
        Utils.color( table, new ColorRGBA( 0.5f, 0.5f, 0.9f, 1.0f ), 128 );  
        
        // slider der
        table = getPhysicsSpace().createStaticNode();
        rootNode.attachChild(table);
        
        visualTable = new Box( "left", new Vector3f(), 8, 4, 1);
        visualTable.setLocalTranslation( new Vector3f(-25,0,60) );
        visualTable.setLocalRotation( new Quaternion( ).fromAngles(0,FastMath.DEG_TO_RAD * -45f,0));    
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
/* TODO ojo, aca comento esto porque quiero probar solo con IRON
        // Defino un materia personalizado para poder setear las propiedades de interaccion con la mesa
        final Material customMaterial = new Material( "material de bola" );
        // Es pesado
        customMaterial.setDensity( 10.0f ); //TODO antes 100
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
    
    private boolean pause = false;
    private boolean showDepth = false;
    private boolean showGraphs = false;

    protected void initDebug()
    {
        if ( com.jme.util.Debug.debug )
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
//            KeyBindingManager.getKeyBindingManager().set( "toggle_stats", KeyInput.KEY_F4 );
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
//            KeyBindingManager.getKeyBindingManager().set( "camera_out", KeyInput.KEY_F9 );
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
                        pinballInputHandler.addAction( new InputAction() {
                            public void performAction( InputActionEvent evt ) {
                                if ( evt.getTriggerPressed() ) {
                                    float tpf =  timer.getTimePerFrame();
                                    update( tpf );
                                    rootNode.updateGeometricState(tpf, true);
                                }
                            }
                        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_ADD, InputHandler.AXIS_NONE, false );
            //            KeyBindingManager.getKeyBindingManager().set( "toggle_depth",
            //                KeyInput.KEY_F3 );
            // esto debería cambiar la profundidad... tampoco creo que valga la pena
                      pinballInputHandler.addAction( new InputAction() {
                          public void performAction( InputActionEvent evt ) {
                              if ( evt.getTriggerPressed() ) {
                                  showDepth = !showDepth;
                              }
                          }
                          }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F10, InputHandler.AXIS_NONE, false );
            
              pinballInputHandler.addAction( new InputAction() {
                  public void performAction( InputActionEvent evt ) {
                      if ( evt.getTriggerPressed() ) {
                          MouseInput.get().setCursorVisible(!MouseInput.get().isCursorVisible());
                      }
                  }
                  }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F11, InputHandler.AXIS_NONE, false );

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
    
    private static int[] RflipIndices = {

        33,34,35,32,33,35,32,35,36,31,32,36,30,31,36,30,36,37,29,30,37,28,29,37,27,28,37,27,
        37,38,26,27,38,25,26,38,24,25,38,24,38,39,23,24,39,23,39,40,22,23,40,21,22,40,21,40,
        41,20,21,41,20,41,42,19,20,42,18,19,42,18,42,43,17,18,43,17,43,44,16,17,44,15,16,44,
        15,44,45,14,15,45,13,14,45,13,45,46,12,13,46,11,12,46,11,46,47,10,11,47,9,10,47,9,
        47,48,8,9,48,7,8,48,6,7,48,6,48,49,5,6,49,5,49,50,4,5,50,4,50,51,3,4,51,3,51,52,2,3,
        52,2,52,53,2,53,54,1,2,54,1,54,55,0,1,55,0,55,56,0,56,57,59,0,57,59,57,58,58,61,59,
        61,60,59,57,62,58,62,61,58,56,63,57,63,62,57,55,64,56,64,63,56,54,65,55,65,64,55,53,
        66,54,66,65,54,52,67,53,67,66,53,51,68,52,68,67,52,50,69,51,69,68,51,49,70,50,70,69,
        50,48,71,49,71,70,49,47,72,48,72,71,48,46,73,47,73,72,47,45,74,46,74,73,46,44,75,45,
        75,74,45,43,76,44,76,75,44,42,77,43,77,76,43,41,78,42,78,77,42,40,79,41,79,78,41,39,
        80,40,80,79,40,38,81,39,81,80,39,37,82,38,82,81,38,36,83,37,83,82,37,35,84,36,84,83,
        36,34,85,35,85,84,35,33,86,34,86,85,34,32,87,33,87,86,33,31,88,32,88,87,32,30,89,31,
        89,88,31,29,90,30,90,89,30,28,91,29,91,90,29,27,92,28,92,91,28,26,93,27,93,92,27,25,
        94,26,94,93,26,24,95,25,95,94,25,23,96,24,96,95,24,22,97,23,97,96,23,21,98,22,98,97,
        22,20,99,21,99,98,21,19,100,20,100,99,20,18,101,19,101,100,19,17,102,18,102,101,18,
        16,103,17,103,102,17,15,104,16,104,103,16,14,105,15,105,104,15,13,106,14,106,105,14,
        12,107,13,107,106,13,11,108,12,108,107,12,10,109,11,109,108,11,9,110,10,110,109,10,
        8,111,9,111,110,9,7,112,8,112,111,8,6,113,7,113,112,7,5,114,6,114,113,6,4,115,5,115,
        114,5,3,116,4,116,115,4,2,117,3,117,116,3,1,118,2,118,117,2,119,0,60,0,59,60,0,119,
        1,119,118,1,85,86,84,86,87,84,84,87,83,87,88,83,88,89,83,83,89,82,89,90,82,90,91,82,
        91,92,82,82,92,81,92,93,81,93,94,81,94,95,81,81,95,80,95,96,80,80,96,79,96,97,79,97,
        98,79,79,98,78,98,99,78,78,99,77,99,100,77,100,101,77,77,101,76,101,102,76,76,102,
        75,102,103,75,103,104,75,75,104,74,104,105,74,105,106,74,74,106,73,106,107,73,107,
        108,73,73,108,72,108,109,72,109,110,72,72,110,71,110,111,71,111,112,71,112,113,71,
        71,113,70,113,114,70,70,114,69,114,115,69,69,115,68,115,116,68,68,116,67,116,117,67,
        67,117,66,66,117,65,117,118,65,65,118,64,118,119,64,64,119,63,63,119,62,60,62,119,
        62,60,61
        };

    private static float[] RflipVertices = {
        0.959677f,-0.583074f,-0.049665f,0.909426f,-0.569651f,-0.049665f,0.851084f,-0.553364f,
        -0.049665f,0.785562f,-0.534387f,-0.049665f,0.713772f,-0.512891f,-0.049665f,0.636627f,
        -0.48905f,-0.049665f,0.555038f,-0.463037f,-0.049665f,0.469919f,-0.435023f,-0.049665f,
        0.382182f,-0.405182f,-0.049665f,0.292738f,-0.373687f,-0.049665f,0.202501f,-0.34071f,
        -0.049665f,0.112383f,-0.306424f,-0.049665f,0.023295f,-0.271001f,-0.049665f,-0.10392f,
        -0.220853f,-0.049665f,-0.232654f,-0.17207f,-0.049665f,-0.360967f,-0.123491f,-0.049665f,
        -0.48692f,-0.073959f,-0.049665f,-0.608574f,-0.022314f,-0.049665f,-0.723988f,0.032602f,
        -0.049665f,-0.831224f,0.091948f,-0.049665f,-0.928341f,0.156884f,-0.049665f,-1.0134f,
        0.228567f,-0.049665f,-1.084462f,0.308158f,-0.049665f,-1.139587f,0.396814f,-0.049665f,
        -1.176836f,0.495694f,-0.049665f,-1.184946f,0.54646f,-0.049665f,-1.183377f,0.597793f,
        -0.049665f,-1.17301f,0.648179f,-0.049665f,-1.154727f,0.696106f,-0.049665f,-1.129413f,
        0.740058f,-0.049665f,-1.09795f,0.778522f,-0.049665f,-1.061221f,0.809985f,-0.049665f,
        -1.020109f,0.832931f,-0.049665f,-0.975496f,0.845849f,-0.049665f,-0.928267f,0.847223f,
        -0.049665f,-0.879302f,0.83554f,-0.049665f,-0.829486f,0.809286f,-0.049665f,-0.620274f,
        0.666687f,-0.049665f,-0.424393f,0.533188f,-0.049665f,-0.240821f,0.408114f,-0.049665f,
        -0.06854f,0.290792f,-0.049665f,0.093473f,0.180548f,-0.049665f,0.246236f,0.076709f,
        -0.049665f,0.39077f,-0.021397f,-0.049665f,0.528096f,-0.114446f,-0.049665f,0.659232f,
        -0.203109f,-0.049665f,0.7852f,-0.288061f,-0.049665f,0.90702f,-0.369975f,-0.049665f,
        1.025711f,-0.449524f,-0.049665f,1.059373f,-0.475102f,-0.049665f,1.080739f,-0.497906f,
        -0.049665f,1.091361f,-0.517979f,-0.049665f,1.092793f,-0.535366f,-0.049665f,1.086586f,
        -0.550111f,-0.049665f,1.074294f,-0.562258f,-0.049665f,1.057469f,-0.571853f,-0.049665f,
        1.037662f,-0.578938f,-0.049665f,1.016427f,-0.583558f,-0.049665f,0.995316f,-0.585758f,
        -0.049665f,0.975882f,-0.585582f,-0.049665f,0.975882f,-0.585582f,0.050335f,0.995316f,
        -0.585758f,0.050335f,1.016427f,-0.583558f,0.050335f,1.037662f,-0.578938f,0.050335f,
        1.057469f,-0.571853f,0.050335f,1.074294f,-0.562258f,0.050335f,1.086586f,-0.550111f,
        0.050335f,1.092793f,-0.535366f,0.050335f,1.091361f,-0.517979f,0.050335f,1.080739f,
        -0.497906f,0.050335f,1.059373f,-0.475102f,0.050335f,1.025711f,-0.449524f,0.050335f,
        0.90702f,-0.369975f,0.050335f,0.7852f,-0.288061f,0.050335f,0.659232f,-0.203109f,
        0.050335f,0.528096f,-0.114446f,0.050335f,0.39077f,-0.021397f,0.050335f,0.246236f,
        0.076709f,0.050335f,0.093473f,0.180548f,0.050335f,-0.06854f,0.290792f,0.050335f,
        -0.240821f,0.408114f,0.050335f,-0.424393f,0.533188f,0.050335f,-0.620274f,0.666687f,
        0.050335f,-0.829486f,0.809286f,0.050335f,-0.879302f,0.83554f,0.050335f,-0.928267f,
        0.847223f,0.050335f,-0.975496f,0.845849f,0.050335f,-1.020109f,0.832931f,0.050335f,
        -1.061221f,0.809985f,0.050335f,-1.09795f,0.778522f,0.050335f,-1.129413f,0.740058f,
        0.050335f,-1.154727f,0.696106f,0.050335f,-1.17301f,0.648179f,0.050335f,-1.183377f,
        0.597793f,0.050335f,-1.184946f,0.54646f,0.050335f,-1.176836f,0.495694f,0.050335f,
        -1.139587f,0.396814f,0.050335f,-1.084462f,0.308158f,0.050335f,-1.0134f,0.228567f,
        0.050335f,-0.928341f,0.156884f,0.050335f,-0.831224f,0.091948f,0.050335f,-0.723988f,
        0.032602f,0.050335f,-0.608574f,-0.022314f,0.050335f,-0.48692f,-0.073959f,0.050335f,
        -0.360967f,-0.123491f,0.050335f,-0.232654f,-0.17207f,0.050335f,-0.10392f,-0.220853f,
        0.050335f,0.023295f,-0.271001f,0.050335f,0.112383f,-0.306424f,0.050335f,0.202501f,
        -0.34071f,0.050335f,0.292738f,-0.373687f,0.050335f,0.382182f,-0.405182f,0.050335f,
        0.469919f,-0.435023f,0.050335f,0.555038f,-0.463037f,0.050335f,0.636627f,-0.48905f,
        0.050335f,0.713772f,-0.512891f,0.050335f,0.785562f,-0.534387f,0.050335f,0.851084f,
        -0.553364f,0.050335f,0.909426f,-0.569651f,0.050335f,0.959677f,-0.583074f,0.050335f
        };

    
	public Vector3f getBallStartUp() 
	{
		return ballStartUp;
	}
}
