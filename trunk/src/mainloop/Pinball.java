package mainloop;

import input.FengJMEInputHandler;
import input.PinballInputHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;
import org.fenggui.*;
import org.fenggui.binding.render.lwjgl.LWJGLBinding;
import org.fenggui.composite.Window;
import org.fenggui.event.ButtonPressedEvent;
import org.fenggui.event.IButtonPressedListener;
import org.fenggui.layout.RowLayout;
import org.fenggui.util.Point;
import org.fenggui.util.Spacing;
import org.lwjgl.opengl.GL13;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.CullState;
import com.jme.system.JmeException;
import com.jmetest.physics.Utils;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.PhysicsSpace;
import com.jmex.physics.PhysicsUpdateCallback;
import com.jmex.physics.StaticPhysicsNode;
import com.jmex.physics.contact.MutableContactInfo;
import com.jmex.physics.material.Material;
import com.jmex.physics.util.SimplePhysicsGame;
import components.Bumper;
import components.Door;
import components.Flipper;
import components.Magnet;
import components.Plunger;
import components.Bumper.BumperType;
import components.Flipper.FlipperType;

/**
 * Clase principal del juego.
 *
 */
public class Pinball extends SimplePhysicsGame
{
	
	public static final Material pinballTableMaterial = Material.PLASTIC;
	private static final String GAME_NAME = "xtremme pinball";
	private static final String GAME_VERSION = "0.2";
	
	/* Logger de la clase Pinball */
    private static final Logger logger = Logger.getLogger(Pinball.class.getName());
    
	/* InputHandler para el pinball */
	private PinballInputHandler pinballInputHandler;
	
	/* Input Handler de FengGUI, sirve para que FengGUI capture las teclas
	 * y las envie al display de JME para ser capturadas por los otros handlers */
	private FengJMEInputHandler fengGUIInputHandler;
	
	/* Lista de los flippers del juego actual */
	private List<DynamicPhysicsNode> flippers;
	
	/* Plunger del juego */
	private DynamicPhysicsNode plunger;
	
	/* Configuracion del juego */
	private PinballSettings pinballSettings;
	
	/* Pantalla para FengGUI */
	private Display fengGUIdisplay;
	
	/* Menu de opciones */
	private Window menu;
	
	
	/**
	 * Punto de entrada al juego
	 */
	public static void main(String[] args)
	{
		/* Creo un nuevo Pinball */
		Pinball app = new Pinball();
		
		//TODO We will load our own "fantastic" Flag Rush logo. Yes, I'm an artist.
		// TODO cablear que siempre use LWJGL
		app.setConfigShowMode(ConfigShowMode.AlwaysShow, Pinball.class.getClassLoader().getResource("jmetest/data/images/FlagRush.png"));
		
		/* Doy comienzo al juego */
		app.start();
	}
	
	public void buildSettings()
	{
		/* Creo las configuraciones */
		pinballSettings = new PinballSettings();
		
		/* Les guardo los valores recogidos de la ventana de configuraciones */
		pinballSettings.setWidth(settings.getWidth());
		pinballSettings.setHeight(settings.getHeight());
		pinballSettings.setDepth(settings.getDepth());
		pinballSettings.setFreq(settings.getFrequency());
		pinballSettings.setFullscreen(settings.isFullscreen());
		pinballSettings.setRenderer(settings.getRenderer());
	}
	
	/**
	 * Se debe actualizar la fisica, el puntaje, el tiempo, todo lo relacionado al juego en si.
	 */
	protected void simpleUpdate() //float interpolation
	{
		/* Actualizo el timer */
		timer.update();
		
		/* Pido al timer el tiempo transcurrido desde la ultima llamada */
		/* Actualmente me deberia llegar en interpolation, pero el engine no lo hace aun
		 * (no esta implementado) */
		float interpolation = timer.getTimePerFrame();
		
		/* Actualizo los controladores de input */
        fengGUIInputHandler.update(interpolation);
        pinballInputHandler.update(interpolation);

		/* Se modifico la escena, entonces actualizo el grafo */
        rootNode.updateGeometricState(interpolation, true);
	}

	/**
	 * Se debe redibujar la escena
	 */
	protected void simpleRender()
	{
		/* Para que la GUI se muestre bien */
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		
		/* Muestro la pantalla de FengGUI */
		fengGUIdisplay.display();
	}

	/**
	 * Inicializar el display, los input handlers y la camara
	 */
	@Override
	protected void initSystem()
	{
		// Invoco al metodo que esta implementado en SimplePhysicsGame, que a su vez invoca a BaseSimpleGame
		super.initSystem();

		/* Tomo las configuraciones */
		buildSettings();
		
		/* Se pisa la camara, (no el display, porque arroja exception si se crean dos windows a traves de el)
		 * y settings que inicializo la superclase */
		try
		{
			/* Creo la camara */
		    cam = display.getRenderer().createCamera(pinballSettings.getWidth(), pinballSettings.getHeight());
		} catch (JmeException e)
		{
			logger.log(Level.SEVERE, "No se pudo crear la pantalla de juego", e);
			System.exit(1);
		}

		/* Fijo el nombre a la ventana */
		display.setTitle(GAME_NAME + " v" + GAME_VERSION);
		
		/* Fijo el fondo en negro */
		display.getRenderer().setBackgroundColor(ColorRGBA.black.clone());
		
		/* Inicializo la camara */
		
		/* Perspectiva y FOV */
		cam.setFrustumPerspective(45.0f, (float)pinballSettings.getWidth() / (float)pinballSettings.getHeight(), 1, 1000);
		
		/* Ubicacion */ // TODO: Ubicar la camara en base a donde se encuentre la mesa fija que definamos
		Vector3f loc = new Vector3f(0.0f, 4f, 200f);
		Vector3f left = new Vector3f(-1.0f, 0.0f, 0.0f);
		Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
		Vector3f dir = new Vector3f(0.0f, 0f, -0.5f); //en z -0.5f

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
		
		/* Se elimina la accion asociada al boton ESC del teclado para evitar que salga del juego y hacer que en vez de ello muestre el menu */
		KeyBindingManager.getKeyBindingManager().remove("exit");
		
		// Para que la velocidad del juego sea mayor
		this.setPhysicsSpeed(3.0f);
	}

	/**
	 * Inicializar la escena. Invocado por BaseSimpleGame.initGame()
	 */
	protected void simpleInitGame()
	{
	    /* Optimizacion - aplico culling a todos los nodos */
        CullState cs = display.getRenderer().createCullState();
        cs.setCullFace(CullState.Face.Back);
        rootNode.setRenderState(cs);

		/* Creo la lista de flippers */
		flippers = new ArrayList<DynamicPhysicsNode>(4);
		
        /* Armo la mesa de juego */
        buildTable();
        
		// TODO Aca deberia ir la traduccion de X3D para formar la escena
        buildAndAttachComponents();

        /* Inclino todos los componentes a la vez desde el nodo raiz */
        inclinePinball();
        
		/* Actualizo el nodo raiz */
		rootNode.updateGeometricState(0.0f, true);
		rootNode.updateRenderState();

		/* Inicializo la GUI */
		initGUI();
		
		/* Muestro el menu */
		showMenu();
	}
	
	/**
	 * Inicializa la GUI.
	 */
	protected void initGUI()
	{
		/* Obtengo un display en Feng con LWJGL */
		fengGUIdisplay = new Display(new LWJGLBinding());
 
		/* Inicializo el input handler de FengGUI */
		fengGUIInputHandler = new FengJMEInputHandler(fengGUIdisplay);
 
		/* Creo el menu */
		final Window menu = FengGUI.createWindow(fengGUIdisplay, false, false, false, true);
		menu.setTitle("Main menu");
		menu.setPosition(new Point(50, 200));
		menu.getContentContainer().setLayoutManager(new RowLayout(false));
		menu.getContentContainer().getAppearance().setPadding(new Spacing(10, 10));
		menu.setVisible(false);
		this.menu = menu;
 
		/* Boton de juego nuevo */
		final Button newGameButton = FengGUI.createButton(menu.getContentContainer(), "New game");
		
		newGameButton.addButtonPressedListener(new IButtonPressedListener() {
			
			public void buttonPressed(ButtonPressedEvent arg0) {

				/* Habilito el controlador del juego */
				pinballInputHandler.setEnabled(true);
				
				/* Oculto el menu */
				hideMenu();

				// TODO Iniciar o reiniciar el juego. Esto de la pausa es temporal, sacarlo despues.
				Pinball.this.pause = false; 
			}
		});
		
		/* Boton de salir */
		final Button exitButton = FengGUI.createButton(menu.getContentContainer(), "Exit");
		
		exitButton.addButtonPressedListener(new IButtonPressedListener() {
			
			public void buttonPressed(ButtonPressedEvent arg0)
			{
 				Pinball.this.finish();
			}
		});
 
		/* Comprime lo posible los botones */
		//menu.pack(); TODO darle algo de estilo al menu
 
		/* Actualizo la pantalla con los nuevos componentes */
		fengGUIdisplay.layout();

	}
	
	private void inclinePinball()
	{
		/* Inclino desde el nodo raiz */
		Quaternion rot = new Quaternion();
		
		/* Se rota toda la mesa y sus componentes en el eje X */
		rot.fromAngles(FastMath.DEG_TO_RAD * pinballSettings.getInclinationAngle(), 0f, 0f);
		rootNode.setLocalRotation(rot);
		
		/* Inclino joints fisicos y demas, recalculandolos */
		/* TODO for (DynamicPhysicsNode flipper : getFlippers())
		{
			System.out.println(flipper.getChild(0).getLocalTranslation());
			((Flipper)flipper.getChild(0)).recalculateJoints();
			
		}*/
	}
	
	public void showMenu()
	{
		/* Desactivo el controlador del pinball */
		pinballInputHandler.setEnabled(false);
		
		/* Muestro el menu */
		menu.setVisible(true);
		
		/* Hago visible al cursor */
		MouseInput.get().setCursorVisible(true);
		
		/* Pauseo el juego */
		pause = true;
	}
	
	public void hideMenu()
	{
		/* Oculto el menu */
		menu.setVisible(false);
		
		/* Oculto el cursor */
		MouseInput.get().setCursorVisible(false);
	}

	/**
	 * Si cambia la resolucion se llama este metodo
	 */
	@Override
	protected void reinit()
	{
		super.reinit();
		/* Creo nuevamente el display con las propiedades requeridas */
		display.recreateWindow(pinballSettings.getWidth(), 
				pinballSettings.getHeight(), 
				pinballSettings.getDepth(), 
				pinballSettings.getFreq(), 
				pinballSettings.isFullscreen());
	}
    
	/**
	 * Salida exitosa del juego
	 */
    protected void quit()
    {
    	super.quit();
    	System.exit(0);
    }

	/**
	 * Se la llama para limpiar el juego una vez finalizado
	 */
    @Override
	protected void cleanup()
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
		// El nodo fisico de todas las bolas debera llamarse "ball"
		mainBall.setName("ball");
        rootNode.attachChild(mainBall);
               
        final Sphere visualMainBall = new Sphere("Bola principal", 25, 25, 1);
		visualMainBall.setLocalTranslation(new Vector3f(0, 20, -60));
		
		// Agregado de bounding volume 
		visualMainBall.setModelBound(new BoundingSphere());
		visualMainBall.updateModelBound();

		mainBall.attachChild(visualMainBall);
		mainBall.generatePhysicsGeometry();
		mainBall.setMaterial(customMaterial);		
		// Se computa la masa luego de generar la geometria fisica
		mainBall.computeMass();
		
		// Una segunda bola para probar el bumper
		/* Nodo dinamico de la bola */
		final DynamicPhysicsNode mainBall2 = getPhysicsSpace().createDynamicNode(); //TODO sacar final, es para debug
		mainBall2.setName("ball");
        rootNode.attachChild(mainBall2);
               
        final Sphere visualMainBall2 = new Sphere("Bola 2", 25, 25, 1);
        visualMainBall2.setLocalTranslation(new Vector3f(25, 5, -2));
		
		// Agregado de bounding volume 
		visualMainBall2.setModelBound(new BoundingSphere());
		visualMainBall2.updateModelBound();

		mainBall2.attachChild(visualMainBall2);
		mainBall2.generatePhysicsGeometry();
		mainBall2.setMaterial(customMaterial);		
		// Se computa la masa luego de generar la geometria fisica
		mainBall2.computeMass();
		
		// Una tercera bola para probar el iman magnet1
		/* Nodo dinamico de la bola */
		final DynamicPhysicsNode mainBall3 = getPhysicsSpace().createDynamicNode(); //TODO sacar final, es para debug
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
		
		//----------------------------------------
		//TODO quitar esto, es para debug de bumper
		// Mover la bola dos hacia el bumper1
		 getPhysicsSpace().addToUpdateCallbacks( new PhysicsUpdateCallback() {
	            public void beforeStep( PhysicsSpace space, float time ) {
	            	/* Checkea todas las acciones para ver si alguna debe ser invocada, en tal caso invoca a performAction
	            	 * pasandole el tiempo 'time' 
	            	 */
	            	mainBall2.addForce(new Vector3f(-7000f, 0f, 0f));
	            }
	            public void afterStep( PhysicsSpace space, float time ) {

	            }
	        } );
		//-----------------------------------------

		/* Pongo un flipper de prueba */
		final Box visualFlipper = new Box("Visual flipper", new Vector3f(), 5, 1, 2);
		visualFlipper.setLocalTranslation(new Vector3f(10, 3, 60));
		
		/* Le doy color */
		Utils.color(visualFlipper, new ColorRGBA(0f, 1.0f, 0f, 1.0f), 128);
		
		DynamicPhysicsNode testFlipper = Flipper.create(this, "Physic flipper", visualFlipper, FlipperType.RIGHT_FLIPPER);
		rootNode.attachChild(testFlipper);
		flippers.add(testFlipper);
		
		/* Pongo un plunger de prueba */
		final Box visualPlunger = new Box("Visual plunger", new Vector3f(), 1, 1, 15);
		visualPlunger.setLocalTranslation(new Vector3f(25, 3, 90));
		
		/* Le doy color */
		Utils.color(visualPlunger, new ColorRGBA(1.0f, 1.0f, 0f, 1.0f), 128);
		
		DynamicPhysicsNode testPlunger = Plunger.create(this, "Physic plunger", visualPlunger, 10);
		rootNode.attachChild(testPlunger);
		plunger = testPlunger;
		
		/* Pongo una puerta de prueba */
		final Box visualDoor = new Box("Visual door", new Vector3f(), 3, 1, 0.1f);
		visualDoor.setLocalTranslation(new Vector3f(25, 3, 75));
		
		/* Le doy color */
		Utils.color(visualDoor, new ColorRGBA(0f, 1.0f, 1.0f, 1.0f), 128);
		
		DynamicPhysicsNode testDoor = Door.create(this, "Physic door", visualDoor, Door.DoorType.RIGHT_DOOR, -0.8f, 0.5f);
		rootNode.attachChild(testDoor);
		
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
//		final Box visualMagnet2 = new Box("Visual magnet 2", new Vector3f(), 2f, 4f, 2f);
//		visualMagnet2.setLocalTranslation(new Vector3f(15, 5, -10));
//		StaticPhysicsNode magnet2 = Magnet.create(this, "Physic magnet 2", visualMagnet2);
//		rootNode.attachChild(magnet2);
		

		// Agrego un bumper
		final Box visualBumper1 = new Box("Visual bumper 1", new Vector3f(), 2f, 4f, 2f);
		// Le doy color al bumper
		Utils.color( visualBumper1, new ColorRGBA( 0f, 1f, 0f, 1.0f ), 120 );
		visualBumper1.setLocalTranslation(new Vector3f(0, 5, 0));
		// Agregado de bounding volume 
		visualBumper1.setModelBound(new BoundingBox());
		visualBumper1.updateModelBound();
		StaticPhysicsNode bumper1 = Bumper.create(this, "Physic bumper 1", visualBumper1, BumperType.JUMPER, pinballInputHandler);
		rootNode.attachChild(bumper1);
		
		// Agrego otro bumper
		final Box visualBumper2 = new Box("Visual bumper 1", new Vector3f(), 2f, 4f, 2f);
		// Le doy color al bumper
		Utils.color( visualBumper2, new ColorRGBA( 0f, 1f, 0f, 1.0f ), 120 );
		visualBumper2.setLocalTranslation(new Vector3f(25, 5, 0));
		// Agregado de bounding volume 
		visualBumper2.setModelBound(new BoundingBox());
		visualBumper2.updateModelBound();
		//StaticPhysicsNode bumper2 = Bumper.create(this, "Physic bumper 2", visualBumper2, BumperType.JUMPER, pinballInputHandler);
		//rootNode.attachChild(bumper2);
		
		
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
		// Seteo el color de la mesa para diferenciarlo de la bola. Brillo al maximo
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
	
	public Node getTiltNode()
	{
		return null; // TODO Devolver el nodo sobre el que se debe ejecutar la accion de tilt
	}

	public PinballSettings getPinballSettings()
	{
		return pinballSettings;
	}
	
	public Camera getCamera()
	{
		return cam;
	}
}
