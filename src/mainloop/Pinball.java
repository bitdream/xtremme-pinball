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
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.light.PointLight;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.system.JmeException;
import com.jmex.physics.util.SimplePhysicsGame;
import components.Flipper;
import components.Plunger;

/**
 * Clase principal del juego.
 *
 */
public class Pinball extends SimplePhysicsGame
{
	private static final String GAME_NAME = "xtremme pinball";
	private static final String GAME_VERSION = "0.1";
	
	/* Logger de la clase Pinball */
    private static final Logger logger = Logger.getLogger(Pinball.class.getName());
    
    /* Timer para calcular tiempos transcurridos */
	//protected Timer timer;
	
	/* Camara de la escena */
	//private Camera cam;
	
	/* InputHandler para el pinball */
	private PinballInputHandler pinballInputHandler;
	
	/* Input Handler de FengGUI, sirve para que FengGUI capture las teclas
	 * y las envie al display de JME para ser capturadas por los otros handlers */
	private FengJMEInputHandler fengGUIInputHandler;
	
	/* Lista de los flippers del juego actual */
	private List<Flipper> flippers;
	
	/* Configuracion del juego */
	private PinballSettings pinballSettings;
	
	/* Nodo raiz del grafo de la escena */
	// private Node rootNode;
	
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
		/* Limpio la pantalla y dibujo la escena via JME */
// Esto ya lo hace el metodo render de las superclases
//		display.getRenderer().clearBuffers();
//		display.getRenderer().draw(rootNode);
		
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
			/* Creo el display */
//			display = DisplaySystem.getDisplaySystem(pinballSettings.getRenderer());
//			display.createWindow(pinballSettings.getWidth(), 
//					pinballSettings.getHeight(), 
//					pinballSettings.getDepth(), 
//					pinballSettings.getFreq(), 
//					pinballSettings.isFullscreen());

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
		pinballInputHandler = new PinballInputHandler(cam, this);
		
		//TODO Agrego el InputHandler a input (tal como lo hace SimplePhysicsGame con el FirstPersonHandler que crea)
		input.addToAttachedHandlers(pinballInputHandler);
		
		/* Hago visible al cursor para poder seleccionar las opciones del menu TODO ver si esto va aca o que pasa si sale del menu (deberia desaparecer el cursor) */
		MouseInput.get().setCursorVisible(true);
		
		/* Inicializo la camara */
		
		/* Perspectiva y FOV */
		cam.setFrustumPerspective(45.0f, (float)pinballSettings.getWidth() / (float)pinballSettings.getHeight(), 1, 1000);
		
		/* Ubicacion */ // TODO: Ubicar la camara en base a donde se encuentre la mesa fija que definamos
		Vector3f loc = new Vector3f(0.0f, 0.0f, 25.0f);
		Vector3f left = new Vector3f(-1.0f, 0.0f, 0.0f);
		Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
		Vector3f dir = new Vector3f(0.0f, 0f, -0.5f);
		cam.setFrame(loc, left, up, dir);
		
		/* Aplicar los cambios a la camara */
		cam.update();
				
		 /* Creo el timer para medir tiempos transcurridos. Ya viene creado por las superclases */
//	     timer = Timer.getTimer();

	    /* Fijo la camara al display */
		display.getRenderer().setCamera(cam);
	}

	/**
	 * Inicializar la escena. Invocado por BaseSimpleGame.initGame()
	 */
	protected void simpleInitGame()
	{
		/* Creo un ZBuffer para mostrar los pixeles mas cercanos a la camara que estan por encima de otros objetos */
// Ya lo hace BaseSimpleGame
//	    ZBufferState buf = display.getRenderer().createZBufferState();
//	    buf.setEnabled(true);
//	    buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
//	    rootNode.setRenderState(buf);
	    
	    /* Ilumino la escena */
// Ya lo hace BaseSimpleGame(con una ubicacion diferente de la luz, ver si sirve, sino se agregan luces al LightState o se cambian.) 
//		rootNode.setRenderState(buildLighting());
	    
	    /* Optimizacion - aplico culling a todos los nodos */
        CullState cs = display.getRenderer().createCullState();
        cs.setCullFace(CullState.Face.Back);
        rootNode.setRenderState(cs);
		
		// TODO Aca deberia ir la traduccion de X3D para formar la escena
        /*Sphere s = new Sphere("Sphere", 30, 30, 25);
		s.setLocalTranslation(new Vector3f(0, 0, -40));
		s.setModelBound(new BoundingBox());
		s.updateModelBound();*/

		/*ts = display.getRenderer().createTextureState();
		ts.setEnabled(true);
		ts.setTexture(TextureManager.loadTexture(Pinball.class.getClassLoader()
				.getResource("jmetest/data/images/Monkey.jpg"),
				Texture.MinificationFilter.Trilinear, Texture.MagnificationFilter.Bilinear));

		s.setRenderState(ts);*/

        // TODO super temporal
        buildAndAttachComponents();
		
		/* Actualizo el nodo raiz */
		rootNode.updateGeometricState(0.0f, true);
		rootNode.updateRenderState();
		
		/* Creo la lista de flippers */
		flippers = new ArrayList<Flipper>(4);
		
		/* Inicializo la GUI */
		initGUI();
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
		this.menu = menu;
 
		/* Boton de juego nuevo */
		final Button newGameButton = FengGUI.createButton(menu.getContentContainer(), "New game");
		
		newGameButton.addButtonPressedListener(new IButtonPressedListener() {
			
			public void buttonPressed(ButtonPressedEvent arg0) {

				/* Habilito el controlador del juego */
				pinballInputHandler.setEnabled(true);
				
				/* Oculto el menu */
				menu.setVisible(false);

				// TODO Iniciar o reiniciar el juego.
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
	
	/**
	 * Creo la luz default 
	 */
    // TODO volar, no se usa mas, lo mismo se hace desde una superclase
	private LightState buildLighting()
	{
		/* Luz puntual */
        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setLocation(new Vector3f(10, 10, 10));
        light.setEnabled(true);
        
        /* Light state */
        LightState lightState = display.getRenderer().createLightState();
        lightState.setEnabled(true);
        lightState.attach(light);
	    
	    return lightState;
	}

	private void buildAndAttachComponents()
	{// TODO super temporal, esto vendria del X3d. Ahora hay que meterle nodos fisicos!!!
		
		// Create our box
		Box box = new Box("The Box", new Vector3f(-1, -1, -1), new Vector3f(1, 1, 1));
		box.updateRenderState();
		// Rotate the box 25 degrees along the x and y axes.
		Quaternion rot = new Quaternion();
		rot.fromAngles(FastMath.DEG_TO_RAD * 25, FastMath.DEG_TO_RAD * 25, 0.0f);
		box.setLocalRotation(rot);
		// Attach the box to the root node
		rootNode.attachChild(box);
		
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
}
