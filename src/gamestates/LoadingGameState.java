package gamestates;

import java.net.URL;
import java.util.concurrent.Callable;

import loader.LoaderThread;
import main.Main;

import com.jme.input.InputHandler;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.util.GameTaskQueueManager;
import com.jmex.audio.AudioTrack;
import com.jmex.audio.AudioTrack.TrackType;


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
	
	/* Controlador */
	private InputHandler input;

	
	public LoadingGameState(PinballGameStateSettings settings, URL tableResource)
	{
		this.settings = settings;
		this.tableResource = tableResource;
		
		/* Inicializo la musica */
		music = Main.getAudioSystem().createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/loading/music.wav"), false);
		music.setType(TrackType.MUSIC);
		music.setLooping(true);
		music.setTargetVolume(Main.getMusicVolume());
		
		/* Accion para abortar */
	    input = new InputHandler();
	    input.addAction( new InputAction() {

            public void performAction( InputActionEvent evt )
            {
            	if ( evt.getTriggerPressed() )
            		loadWorker.stopLoading();
            }
        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_ESCAPE, InputHandler.AXIS_NONE, false );
	    
	    /* Accion de mute/unmute */
		input.addAction( new InputAction() {

            public void performAction( InputActionEvent evt )
            {
            	if ( evt.getTriggerPressed() )
            	{
	            	Main.toggleMuteAudio();
            	}
            }
        }, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_O, InputHandler.AXIS_NONE, false );
	}
	
	/**
	 * Se pone a cargar la mesa seleccionada al construir el loadinggamestate.
	 */
	public void startLoading()
	{
        /* Creo el juego nuevo */
        final PinballGameState pinballGS = Main.newPinballGame(settings);
        
        /* Agrego como tarea la carga */
        loadWorker = new LoadWorker(pinballGS, this);
        
        Thread t = new Thread(loadWorker, "LoadingThread");
        t.start();
        
        GameTaskQueueManager.getManager().update(loadWorker);
	}

	private void endLoad()
	{
        /* Termino de cargar, destruyo el loadinggamestate */
		Main.endLoading();
		
        if (loadWorker.aborted)
            Main.newMenu().setActive(true);
        else
            loadWorker.endWork();
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

		input.update( tpf );
		
		if (loadWorker.isCompleted())
		{
		    endLoad();
		}

		/* Actualizo el sistema de sonido */
        Main.getAudioSystem().update();
	}
	
	@Override
	public void render(float tpf)
	{
		super.render(tpf);
	}
	
	public void cleanup()
	{
//	    System.out.println("clean");
	}
	
	private class LoadWorker implements Runnable, Callable<Void>
	{
	    private PinballGameState pinballGS;
	    private LoadingGameState loadingGS;
	    private LoaderThread roomLoader, machineLoader, tableLoader;
	    private volatile boolean complete = false, started = false, aborted = false;
		private float percentage = 0f;
		
		public LoadWorker(PinballGameState pinballGS, LoadingGameState loadingGS)
		{
			super();
			this.pinballGS = pinballGS;
			this.loadingGS = loadingGS;
		}
		
		public synchronized boolean isCompleted()
	    {
	        return this.complete;
	    }

	    public float getPercentage()
	    {
	        if (started)
	        {
	            percentage = (machineLoader.getPercentComplete() + roomLoader.getPercentComplete() + tableLoader.getPercentComplete());
	        }

	        return (percentage / 3);
	    }
	    
	    public void endWork() 
	    {
            /* Ya se cargo la escena en el pinball creado. Ahora lo inicio. */
            pinballGS.initGame();
            pinballGS.setActive(true);
	    }

		public void run()
		{
			/* Creo los threads que crean la habitacion, la maquina y la mesa requerida */
            roomLoader = new LoaderThread(LoadingGameState.class.getClassLoader().getResource( "resources/models/Room.x3d" ), pinballGS);
            Thread roomThread = new Thread(roomLoader, "roomLoadThread");
            
            machineLoader = new LoaderThread(LoadingGameState.class.getClassLoader().getResource( "resources/models/Machine.x3d" ), pinballGS);
            Thread machineThread = new Thread(machineLoader, "machineLoadThread");
            
            tableLoader = new LoaderThread( tableResource, pinballGS );
            Thread tableThread = new Thread(tableLoader, "tableLoadThread");
            
            // Paralelizo
            if ( !aborted ) 
            {
                roomThread.start();
                machineThread.start();
                tableThread.start();
                started = true;
                try
                {
                    roomThread.join();
                    machineThread.join();
                    tableThread.join();
                }
                catch(InterruptedException e)
                {
                }
            }
            
            /* Accion para abortar */
            if ( !aborted )
            {

                /* Atacheo los resultados */
                pinballGS.getRootNode().attachChild(roomLoader.getScene());    
                pinballGS.getRootNode().attachChild(machineLoader.getScene());
                pinballGS.getRootNode().attachChild(pinballGS.inclinePinball(tableLoader.getScene()));
                pinballGS.setGameLogic(tableLoader.getTheme());
                
    			/* Marco al gamestate de juego como con carga completa */
                pinballGS.setLoadingComplete(true);
            }
            
            this.complete = true;
		}

        public Void call() throws Exception
        {
            this.loadingGS.setProgress( getPercentage(), "Loading..." );
            if (!complete && !aborted)
                GameTaskQueueManager.getManager().update(this);
            else
                this.loadingGS.setProgress( 1, "Aborting" );
            
            return null;
        }
        
        public void stopLoading()
        {
            roomLoader.stop();
            machineLoader.stop();
            tableLoader.stop();
            aborted = true;
        }
        
	}
}
