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
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.CullState;
import com.jme.scene.state.MaterialState;
import com.jme.system.JmeException;
import com.jmex.game.state.GameStateManager;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.StaticPhysicsNode;
import com.jmex.physics.contact.MutableContactInfo;
import com.jmex.physics.material.Material;
import com.jmex.physics.util.SimplePhysicsGame;
import components.Flipper;
import components.Plunger;
import components.Flipper.FlipperType;

/**
 * Clase principal del juego.
 *
 */
public class Pinball extends SimplePhysicsGame
{
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
	private List<Flipper> flippers;
	
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
		
		/* Proceso el estado de los flippers existentes */
       for (Flipper f : flippers)
        	f.update(interpolation);

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
		
		// Se pisa la camara, (no el display, pq tira exception si se crean dos windows a traves de el) y settings que inicializo la superclase TODO: ver si queda asi
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
		
		/* Creo los input handlers */
		pinballInputHandler = new PinballInputHandler(this);
		
		// Se elimina la accion asociada al boton ESC del teclado para evitar que salga del juego y que en vez de ello muestre el menu
		KeyBindingManager.getKeyBindingManager().remove("exit");
		
		/* Inicializo la camara */
		
		/* Perspectiva y FOV */
		cam.setFrustumPerspective(45.0f, (float)pinballSettings.getWidth() / (float)pinballSettings.getHeight(), 1, 1000);
		
		/* Ubicacion */ // TODO: Ubicar la camara en base a donde se encuentre la mesa fija que definamos
		Vector3f loc = new Vector3f(0.0f, 4f, 105f);
		Vector3f left = new Vector3f(-1.0f, 0.0f, 0.0f);
		Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
		Vector3f dir = new Vector3f(0.0f, 0f, -0.5f);
		cam.setFrame(loc, left, up, dir);
		
		/* Aplicar los cambios a la camara */
		cam.update();

	    /* Fijo la camara al display */
		display.getRenderer().setCamera(cam);
		
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

        /* Armo la mesa de juego */
        buildTable(pinballSettings.getInclinationAngle());
        
		// TODO Aca deberia ir la traduccion de X3D para formar la escena
        buildAndAttachComponents();
		
		/* Actualizo el nodo raiz */
		rootNode.updateGeometricState(0.0f, true);
		rootNode.updateRenderState();
		
		/* Creo la lista de flippers */
		flippers = new ArrayList<Flipper>(4);
		
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
		
		// TODO ver donde poner esta creacion de la bola
		/* Nodo dinamico de la bola */
		DynamicPhysicsNode mainBall = getPhysicsSpace().createDynamicNode();
        rootNode.attachChild(mainBall);
        
        
        // Defino un materia personalizado para poder setear las propiedades de interaccion con la mesa de plastico
        final Material customMaterial = new Material( "material de bola" );
        // Es pesado
        customMaterial.setDensity( 100.0f );
        // Detalles de contacto con el otro material
        MutableContactInfo contactDetails = new MutableContactInfo();
        // Poco rebote
        contactDetails.setBounce( 0.5f );
        // Poco rozamiento
        contactDetails.setMu( 0.5f );
        customMaterial.putContactHandlingDetails( Material.PLASTIC, contactDetails );
        
        
        final Sphere visualMainBall = new Sphere("Bola principal", 25, 25, 1);
		visualMainBall.setLocalTranslation(new Vector3f(0, 20, 0));

		mainBall.attachChild(visualMainBall);
		mainBall.generatePhysicsGeometry();
		mainBall.setMaterial(customMaterial);
		
		// Se computa la masa luego de generar la geometria fisica
		mainBall.computeMass();
		

		/* Pongo un flipper de prueba */
		final Box visualFlipper = new Box("Visual flipper", new Vector3f(), 5f, 1f, 2f);
		visualFlipper.setLocalTranslation(new Vector3f(7, 2, 33));
		
		rootNode.attachChild((Flipper.create(this, "Physic flipper", visualFlipper, FlipperType.LEFT_FLIPPER)));
		
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
	}
	
	private void buildTable(float inclinationAngle)
	{
		/* Nodo estatico de la mesa */
		StaticPhysicsNode table = getPhysicsSpace().createStaticNode();
		rootNode.attachChild(table);
		
		final Box visualTable = new Box( "Table", new Vector3f(), 30, 1, 80);
		Quaternion rot = new Quaternion();
		rot.fromAngles(FastMath.DEG_TO_RAD * inclinationAngle, FastMath.DEG_TO_RAD * inclinationAngle, 0.0f);
		visualTable.setLocalRotation(rot);
	
		table.attachChild(visualTable);		
		table.generatePhysicsGeometry();
		
		// Seteo el material y el color de la mesa para dioferenciarlo de la bola
		table.setMaterial(Material.PLASTIC);
	    color( table, new ColorRGBA( 0.5f, 0.5f, 0.9f, 1.0f ) );
	}
	
    /**
     * Little helper method to color a spatial.
     *
     * @param spatial the spatial to be colored
     * @param color   desired color
     */
    private void color( Spatial spatial, ColorRGBA color ) {
        final MaterialState materialState = display.getRenderer().createMaterialState();
        materialState.setDiffuse( color );
        if ( color.a < 1 ) {
            final BlendState blendState = display.getRenderer().createBlendState();
            blendState.setEnabled( true );
            blendState.setBlendEnabled( true );
            blendState.setSourceFunction( BlendState.SourceFunction.SourceAlpha );
            blendState.setDestinationFunction( BlendState.DestinationFunction.OneMinusSourceAlpha );
            spatial.setRenderState( blendState );
            spatial.setRenderQueueMode( Renderer.QUEUE_TRANSPARENT );
        }
        spatial.setRenderState( materialState );
    }
    
    
	/**
	 * TODO Solo para debugging.
	 */
	public void printDebugText(String text)
	{
		//debugText.print(text);
	}

	public Plunger getPlunger()
	{
		return null; // TODO Devolver el componente del lanzador
	}
	
	public List<Flipper> getFlippers()
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
