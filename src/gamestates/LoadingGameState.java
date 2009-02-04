package gamestates;

import java.net.URL;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

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
import com.jme.util.GameTaskQueueManager;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jmex.audio.AudioTrack;
import com.jmex.audio.AudioTrack.TrackType;
import com.jmex.game.state.BasicGameState;


public class LoadingGameState extends com.jmex.game.state.load.LoadingGameState
{
	/* Musica de loading */
	private AudioTrack music;

	/* Configuracion del a crear */
	private PinballGameStateSettings settings;

	/* Recurso de la mesa */
	private URL tableResource;
	
	/* El cargador */
	private LoadWorker loadWorker;

	
	public LoadingGameState(PinballGameStateSettings settings, URL tableResource)
	{
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
		// TODO hacer que si pone ESC se cancela la carga (o si toca el boton CANCEL) fengGUIInputHandler.addAction
	}
	
	/**
	 * Se pone a cargar la mesa seleccionada al construir el loadinggamestate.
	 */
	public void startLoading()
	{
        /* Creo el juego nuevo */
        final PinballGameState pinballGS = Main.newPinballGame(settings);
        
        /* Agrego como tarea la carga */
        LoadWorker loadWorker = new LoadWorker(pinballGS);
        GameTaskQueueManager.getManager().update(loadWorker);
	}

	private void endLoad()
	{
        /* Termino de cargar, destruyo el loadinggamestate */
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

		setProgress(loadWorker.getPercentage(), "Loading");
		
		if (loadWorker.isCompleted())
		{
		    endLoad();
		    loadWorker.endWork();
		}

		/* Actualizo el sistema de sonido */
        Main.getAudioSystem().update();
	}
	
	@Override
	public void render(float tpf)
	{
		super.render(tpf);
	}
	
	private class LoadWorker implements Callable<Void>
	{
	    private PinballGameState pinballGS;
	    private LoaderThread roomLoader, machineLoader, tableLoader;
	    private Thread roomThread, machineThread, tableThread; 
	    private Boolean complete = false, started = false;
		private float percentage = 0f;
		
		public LoadWorker(PinballGameState pinballGS)
		{
			super();
			this.pinballGS = pinballGS;
		}
		
		public synchronized boolean isCompleted()
	    {
	        return this.complete;
	    }
	    
	    public synchronized float getPercentage()
	    {
	        if (started)
	        {
	            percentage = (machineLoader.getPercentComplete() + roomLoader.getPercentComplete() + tableLoader.getPercentComplete());
	        }

	        return (percentage / 3) * 100;
	    }
	    
	    public void endWork() 
	    {
            /* Ya se cargo la escena en el pinball creado. Ahora lo inicio. */
            pinballGS.initGame();
            pinballGS.setActive(true);
	    }

		public Void call() throws Exception
		{
			/* Creo los threads que crean la habitacion, la maquina y la mesa requerida */
            roomLoader = new LoaderThread(LoadingGameState.class.getClassLoader().getResource( "resources/models/Room.x3d" ), pinballGS);
            roomThread = new Thread(roomLoader, "roomLoadThread");
            
            machineLoader = new LoaderThread(LoadingGameState.class.getClassLoader().getResource( "resources/models/Machine.x3d" ), pinballGS);
            machineThread = new Thread(machineLoader, "machineLoadThread");
            
            tableLoader = new LoaderThread( tableResource, pinballGS );
            Thread loadTable = new Thread(tableLoader, "tableLoadThread");
            
            // Paralelizo
            roomThread.start();
            machineThread.start();
            tableThread.start();
            synchronized (started)
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
			
			return null;
		}
	}
}
