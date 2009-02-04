package gamestates;

import java.net.URL;

import input.FengJMEInputHandler;

import loader.LoaderThread;
import main.Main;

import org.fenggui.Display;
import org.fenggui.FengGUI;
import org.fenggui.Label;
import org.fenggui.binding.render.lwjgl.LWJGLBinding;
import org.fenggui.composite.Window;
import org.fenggui.layout.RowLayout;
import org.fenggui.layout.StaticLayout;
import org.lwjgl.opengl.GL13;

import com.jme.input.MouseInput;
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

	/* Angulo de inclinacion requerido para el juego a crear */
	private PinballGameStateSettings settings;

	/* Recurso de la mesa */
	private URL tableResource;
	
	/* ventana de progreso */
	private Label loadingLabel;
	private Window progressInfo;

	public LoadingGameState(String name, PinballGameStateSettings settings, URL tableResource)
	{
		super(name);

		this.settings = settings;
		this.tableResource = tableResource;
		
		/* TODO (buscar musica de loading) Inicializo la musica */
		music = Main.getAudioSystem().createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/loading/music.wav"), false);
		music.setType(TrackType.MUSIC);
		music.setLooping(true);
		music.setVolume(Main.getMusicVolume());
		
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
		
		// TODO hacer que si pone ESC se cancela la carga (o si toca el boton CANCEL) fengGUIInputHandler.addAction
 
		/* Creo la ventana de progreso */
		progressInfo = FengGUI.createWindow(fengGUIdisplay, false, false, false, true);
		progressInfo.setTitle("Loading...");
		progressInfo.setSize(200, 200);
		progressInfo.getContentContainer().setLayoutManager(new RowLayout(false));
		//progressInfo.getContentContainer().getAppearance().setPadding(new Spacing(10, 10));
		
		/* Creo el mensaje de progreso */
		loadingLabel = FengGUI.createLabel("");
		progressInfo.addWidget(loadingLabel);
		
		/* Thread de loading */
        loadingThread = new LoadWorker();

		/* Actualizo la pantalla con los nuevos componentes */
		fengGUIdisplay.layout();
		
		/* Centro los elementos */
		StaticLayout.center(progressInfo, fengGUIdisplay);
		StaticLayout.center(loadingLabel, progressInfo);
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
        /* Termino de cargar, cierro la ventana y destruyo el loadinggamestate */
        progressInfo.close();
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
	    private LoaderThread threadRoom;
	    private LoaderThread threadMachine;
	    private LoaderThread threadTable;
	    private Boolean complete = false;
	    private Boolean started = false;
	    private float percentage = 0f;
	    
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
	            float percent = (threadMachine.getPercentComplete() + threadRoom.getPercentComplete() + threadTable.getPercentComplete());
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
            threadRoom = new LoaderThread(LoadingGameState.class.getClassLoader().getResource( "resources/models/Room.x3d" ), pinballGS);
            Thread loadRoom = new Thread(threadRoom, "roomLoadThread");
            
            threadMachine = new LoaderThread(LoadingGameState.class.getClassLoader().getResource( "resources/models/Machine.x3d" ), pinballGS);
            Thread loadMachine = new Thread(threadMachine, "machineLoadThread");
            
//            tableResource = LoadingGameState.class.getClassLoader().getResource( "resources/models/TableAux.x3d" );
            threadTable = new LoaderThread( tableResource, pinballGS );
            Thread loadTable = new Thread(threadTable, "tableLoadThread");
            
            // Paralelizo
            loadRoom.start();
            loadMachine.start();
            loadTable.start();
            synchronized ( started )
            {
                started = true;
            }
            try
            {
                loadRoom.join();
                loadMachine.join();
                loadTable.join();
            }
            catch(InterruptedException e){}
            
            /* Atacheo los resultados */
            pinballGS.getRootNode().attachChild(threadRoom.getScene());    
            pinballGS.getRootNode().attachChild(threadMachine.getScene());
            pinballGS.getRootNode().attachChild(pinballGS.inclinePinball(threadTable.getScene()));
            pinballGS.setGameLogic(threadTable.getTheme());
            
            synchronized ( complete )
            {
                this.complete = true;    
            }
            
        }
	}
}
