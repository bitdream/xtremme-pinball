package gamestates;

import java.net.URL;

import input.FengJMEInputHandler;

import loader.LoaderThread;
import main.Main;

import org.fenggui.Container;
import org.fenggui.Display;
import org.fenggui.FengGUI;
import org.fenggui.Label;
import org.fenggui.binding.render.lwjgl.LWJGLBinding;
import org.fenggui.decorator.background.PlainBackground;
import org.fenggui.layout.RowLayout;
import org.fenggui.layout.StaticLayout;
import org.fenggui.util.Color;
import org.fenggui.util.Spacing;
import org.lwjgl.opengl.GL13;

import com.jme.input.InputHandler;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jmex.audio.AudioTrack;
import com.jmex.audio.AudioTrack.TrackType;
import com.jmex.game.state.BasicGameState;


public class LoadingGameState extends BasicGameState
{
	/* Pantalla para FengGUI */
	private Display fengGUIdisplay;
	
	/* Input Handler de FengGUI, sirve para que FengGUI capture las teclas
	 * y las envie al display de JME para ser capturadas por los otros handlers */
	private FengJMEInputHandler fengGUIInputHandler;
	
	/* Musica de loading */
	private AudioTrack music;
	
	/* Thread que hace la carga */
	private LoadWorker loadingThread;

	/* Configuracion del a crear */
	private PinballGameStateSettings settings;

	/* Recurso de la mesa */
	private URL tableResource;
	
	/* El contenedor general */
	private Container c;
	
	/* Ventana de progreso */
	private Label loadingLabel;
	// TODO private ProgressBar pb;

	public LoadingGameState(String name, PinballGameStateSettings settings, URL tableResource)
	{
		super(name);

		this.settings = settings;
		this.tableResource = tableResource;
		
		/* Inicializo la musica */
		music = Main.getAudioSystem().createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/loading/music.wav"), false);
		music.setType(TrackType.MUSIC);
		music.setLooping(true);
		music.setTargetVolume(Main.getMusicVolume());
		
		/* Inicializo la informacion del progreso */
		initProgressInfo();
	}

	/**
	 * Inicializa la informacion de progreso.
	 */
	protected void initProgressInfo()
	{
		/* Obtengo un display en Feng con LWJGL */
		fengGUIdisplay = new Display(new LWJGLBinding());
 
		/* Inicializo el input handler de FengGUI */
		fengGUIInputHandler = new FengJMEInputHandler(fengGUIdisplay);
	
		/* Creo el contenedor general */
		c = new Container();
		c.getAppearance().add(new PlainBackground(Color.BLUE));
		fengGUIdisplay.addWidget(c);
		c.getAppearance().setPadding(new Spacing(10, 10));
		c.setLayoutManager(new RowLayout(false));
		
		/* Creo el mensaje de progreso */
		loadingLabel = FengGUI.createLabel(c, "");
		
		/* TODO progress bar
		pb = FengGUI.createProgressBar(progressInfo.getContentContainer());
        pb.setText("Working");
        pb.setSize(250, 25);
        pb.setShrinkable(false);
        pb.setX(25);
        pb.setY(25);*/
		
		/* Thread de loading */
        loadingThread = new LoadWorker();

        // TODO hacer que si pone ESC se cancela la carga (o si toca el boton CANCEL) fengGUIInputHandler.addAction
        fengGUIInputHandler.addAction( new InputAction() {

            @Override
            public void performAction( InputActionEvent evt )
            {
                loadingThread.stopLoading();
            }
        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_ESCAPE, InputHandler.AXIS_NONE, false );
        
		/* Actualizo la pantalla con los nuevos componentes */
		fengGUIdisplay.layout();
		
		/* Centro los elementos */
		StaticLayout.center(c, fengGUIdisplay);
		StaticLayout.center(loadingLabel, c);
	}
	
	/**
	 * Se pone a cargar la mesa seleccionada al construir el loadinggamestate.
	 */
	public void startLoading()
	{
        /* Creo el juego nuevo */
        final PinballGameState pinballGS = Main.newPinballGame(settings);
        
        loadingThread.setPinball(pinballGS);
        
	    Thread t = new Thread(loadingThread, "LoadingThread");
	    t.start();

	}

	private void endLoad()
	{
        /* Termino de cargar */
        
		/* Remuevo todo lo que esta en el contenedor */
		c.removeAllWidgets();
		
		/* Destruyo el loadinggamestate */
        Main.endLoading();
	}
	
	@Override
	public void setActive(boolean active)
	{
		super.setActive(active);
		
		if (active)
		{
			/* Muestro el cursor */
			MouseInput.get().setCursorVisible(true);
		
			/* Inicio su musica */
			Main.getAudioSystem().getMusicQueue().addTrack(music);
			Main.getAudioSystem().getMusicQueue().setCurrentTrack(music);
		}
		else
		{
			/* Oculto el cursor */
			MouseInput.get().setCursorVisible(false);
		}
	}
	
	@Override
	public void update(float tpf)
	{
		super.update(tpf);

		this.loadingLabel.setText( this.loadingThread.getPercentageString(this.loadingLabel.getText()) );
		
		if (this.loadingThread.isCompleted())
		{
		    endLoad();
		    this.loadingThread.endWork();
		}
		else
		{
		    /* Actualizo el controlador de input */
		    fengGUIInputHandler.update(tpf);
		}
        
        /* Actualizo el sistema de sonido */
        Main.getAudioSystem().update();
	}
	
	@Override
	public void render(float tpf)
	{
		super.render(tpf);
		
		/* Para que la GUI se muestre bien */
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		
		/* Muestro la pantalla de FengGUI */
		fengGUIdisplay.display();
	}
	
	private class LoadWorker implements Runnable {
	    private PinballGameState pinballGS;
	    private LoaderThread roomLoader, machineLoader, tableLoader;
	    private Thread roomThread, machineThread, tableThread; 
	    private Boolean complete = false, started = false;
	    private float percentage = 0f;
	    
	    // no funca y no tengo ganas de pollear en el loader
	    public void stopLoading()
	    {
	        // seniales no capturadas en ningun lado
//	        this.roomThread.interrupt();
//	        this.machineThread.interrupt();
//            this.tableThread.interrupt();
            System.out.println("Stopping load is not working yet");
	    }
	    
	    public void setPinball(PinballGameState pinballGS)
	    {
	        this.pinballGS = pinballGS;
	    }
	    
	    public synchronized boolean isCompleted()
	    {
	        return this.complete;
	    }
	    
	    public synchronized String getPercentageString(String old)
	    {
	        if (started) 
	        {
	            float percent = (machineLoader.getPercentComplete() + roomLoader.getPercentComplete() + tableLoader.getPercentComplete());
	            if ( percentage == percent ) // float identity
	                return old;
	            percentage = percent;
	        }
	        
	        return String.format( "%.2f %%", percentage / 3 * 100 );
	    }
	    
	    public void endWork() 
	    {
            /* Ya se cargo la escena en el pinball creado. Ahora lo inicio. */
            pinballGS.initGame();
            pinballGS.setActive(true);
	    }
	    
        public void run() {
            
            /* Creo los threads que crean la habitacion, la maquina y la mesa requerida */
            roomLoader = new LoaderThread(LoadingGameState.class.getClassLoader().getResource( "resources/models/Room.x3d" ), pinballGS);
            roomThread = new Thread(roomLoader, "roomLoadThread");
            
            machineLoader = new LoaderThread(LoadingGameState.class.getClassLoader().getResource( "resources/models/Machine.x3d" ), pinballGS);
            machineThread = new Thread(machineLoader, "machineLoadThread");
            
            tableResource = LoadingGameState.class.getClassLoader().getResource( "resources/models/themes/Cars.x3d" );
            tableLoader = new LoaderThread( tableResource, pinballGS );
            tableThread = new Thread(tableLoader, "tableLoadThread");
            
            // Paralelizo
            roomThread.start();
            machineThread.start();
            tableThread.start();
            synchronized ( started )
            {
                started = true;
            }
            try
            {
                roomThread.join();
                machineThread.join();
                tableThread.join();
            }
            catch(InterruptedException e)
            {
//                System.out.println("chau");
//                System.exit( 1 );
            }
            
            /* Atacheo los resultados */
            pinballGS.getRootNode().attachChild(roomLoader.getScene());    
            pinballGS.getRootNode().attachChild(machineLoader.getScene());
            pinballGS.getRootNode().attachChild(pinballGS.inclinePinball(tableLoader.getScene()));
            pinballGS.setGameLogic(tableLoader.getTheme());
            
            synchronized ( complete )
            {
                this.complete = true;    
            }
            
        }
	}
}
