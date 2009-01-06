package mainloop;

import input.FlipperInputHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.*;

import com.jme.app.BaseGame;
import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.light.DirectionalLight;
import com.jme.math.Matrix3f;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.system.JmeException;
import com.jme.util.TextureManager;
import com.jme.util.Timer;
import components.Flipper;


public class Pinball extends BaseGame
{
	private static final String GAME_NAME = "Pinball Pro";
	private static final String GAME_VERSION = "0.1";
	
	/* Logger de la clase Pinball */
    private static final Logger logger = Logger.getLogger(Pinball.class.getName());
    
    /* Timer para los FPS */
	protected Timer timer;
	
	/* Camara de la escena */
	private Camera cam;
	
	private FlipperInputHandler inputHandlerLflipper;
	
	private List<Flipper> flippers;
	
	//TODO the root node of the scene graph
	private Node scene;
	
	//TODO TextureState to show the monkey on the sphere.
	private TextureState ts;
	
	/* Atributos visuales requeridos por el usuario */
	private int width, height, depth, freq;
	private boolean fullscreen;

	/**
	 * Punto de entrada al juego
	 */
	public static void main(String[] args)
	{
		/* Creo un nuevo Pinball */
		Pinball app = new Pinball();
		
		//TODO We will load our own "fantastic" Flag Rush logo. Yes, I'm an artist.
		app.setConfigShowMode(ConfigShowMode.AlwaysShow, Pinball.class.getClassLoader()
				.getResource("jmetest/data/images/FlagRush.png"));
		
		/* Doy comienzo al juego */
		app.start();
	}

	/**
	 * Se debe actualizar la fisica, el puntaje, el tiempo, todo lo relacionado al juego en si.
	 */
	protected void update(float interpolation)
	{
		/* Actualizo el timer */
		timer.update();
		
		/* Actualizo el framerate */
		interpolation = timer.getTimePerFrame();
		
        /* Proceso el estado de los flippers existentes */
		inputHandlerLflipper.update(interpolation);
        for (Flipper f : flippers)
        	f.update(); // TODO ver si conviene mas en vez de recorrer una lista de flippers, recorrer de una el grafo
		
		/* Chequeo de teclas generales */
		// ESC -> Salir
		if (KeyBindingManager.getKeyBindingManager().isValidCommand("exit"))
		{
			finished = true;
		}
		
		if (KeyBindingManager.getKeyBindingManager().isValidCommand("Lflipper"))
		{
			flippers.get(0).setLocalRotation(new Quaternion(1f, 1f, 1f, 1f));
		}
		
		/* Se modifico la escena, entonces actualizo el grafo */
        scene.updateGeometricState(interpolation, true);
	}

	/**
	 * Se debe redibujar la escena
	 */
	protected void render(float interpolation)
	{
		/* Limpio la pantalla */
		display.getRenderer().clearBuffers();

		/* Dibujo la escena */
		display.getRenderer().draw(scene);
	}

	/**
	 * Inicializar el display y la camara
	 */
	protected void initSystem()
	{
		/* Guardo las configuraciones */
		width = settings.getWidth();
		height = settings.getHeight();
		depth = settings.getDepth();
		freq = settings.getFrequency();
		fullscreen = settings.isFullscreen();
		
		/* Creo la lista de flippers */
		flippers = new ArrayList<Flipper>();
		
		try
		{
			/* Creo el display */
			display = DisplaySystem.getDisplaySystem(settings.getRenderer());
			display.createWindow(width, height, depth, freq, fullscreen);

			/* Creo la camara */
			cam = display.getRenderer().createCamera(width, height);
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
		cam.setFrustumPerspective(45.0f, (float)width / (float)height, 1, 1000);
		
		/* Ubicacion */
		Vector3f loc = new Vector3f(0.0f, 0.0f, 25.0f);
		Vector3f left = new Vector3f(-1.0f, 0.0f, 0.0f);
		Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
		Vector3f dir = new Vector3f(0.0f, 0f, -0.5f);
		cam.setFrame(loc, left, up, dir);
		
		
		/* Aplicar los cambios a la camara */
		cam.update();
		
		 /* Creo el timer para controlar FPS */
	    timer = Timer.getTimer();

	    /* Fijo la camara al display */
		display.getRenderer().setCamera(cam);

		/* Mapeo de teclas */
		// ESC -> Exit
		KeyBindingManager.getKeyBindingManager().set("exit", KeyInput.KEY_ESCAPE);
		
		// A
		KeyBindingManager.getKeyBindingManager().set("Lflipper", KeyInput.KEY_A);
	}

	/**
	 * Inicializar la escena
	 */
	protected void initGame()
	{
		scene = new Node("Nodo de escena");
		
		/* Creo un ZBuffer para mostrar los pixeles mas cercanos a la camara que estan por encima de otros objetos */
	    ZBufferState buf = display.getRenderer().createZBufferState();
	    buf.setEnabled(true);
	    buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
	    scene.setRenderState(buf);
	    
	    /* Optimizacion - aplico culling a todos los nodos */
        CullState cs = display.getRenderer().createCullState();
        cs.setCullFace(CullState.Face.Back);
        scene.setRenderState(cs);
		
		// TODO Aca deberia ir la traduccion de X3D para formar la escena
		
		//TODO Create our Sphere
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
		
        
        /* Construyo las luces de la escena */
		scene.setRenderState(buildLighting());
		
		
		
		//update the scene graph for rendering
		scene.updateGeometricState(0.0f, true);
		scene.updateRenderState();
	}

	/**
	 * Si cambia la resolucion se llama este metodo
	 */
	protected void reinit()
	{
		/* Creo nuevamente el display con las propiedades requeridas */
		display.recreateWindow(width, height, depth, freq, fullscreen);
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
	protected void cleanup()
	{
		/* Limpieza de texturas */
		//ts.deleteAll();
	}
	
	/**
	 * Creo la luz default 
	 */
	private LightState buildLighting()
	{
		/* Creo una luz direccional */
	    DirectionalLight light = new DirectionalLight();
	    light.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
	    light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
	    light.setDirection(new Vector3f(1, -1, -1));
	    light.setEnabled(true);

	      /* Uno la luz a un lightState, y el lightState a la escena */
	    LightState lightState = display.getRenderer().createLightState();
	    lightState.setEnabled(true);
	    lightState.attach(light);
	    
	    return lightState;
	}

	private void buildAndAttachComponents()
	{// TODO super temporal, esto vendria del X3d
		
		// Caja para LFlipper (esto vendria de X3d)
		Box b1 = new Box("LFlipper Shape", new Vector3f(), 10, 2.5f, 2.5f);
        b1.setModelBound(new BoundingBox());
        b1.updateModelBound();
        // LFlipper
        Flipper Lflipper = new Flipper("LFlipper", b1, Flipper.FlipperType.LEFT_FLIPPER);
        Lflipper.setLocalTranslation(new Vector3f(0, 0, -40));
        inputHandlerLflipper = new FlipperInputHandler(Lflipper, settings.getRenderer());
        scene.attachChild(Lflipper);
        Lflipper.updateWorldBound();
        Lflipper.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        
        flippers.add(Lflipper);
	}

}
