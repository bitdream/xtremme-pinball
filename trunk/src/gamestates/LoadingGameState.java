package gamestates;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;
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

	/* Recurso de la mesa */
	private URL tableResource;
	
	/* El cargador */
	private LoadWorker loadWorker;
	
	/* Controlador */
	private InputHandler input;

	
	public LoadingGameState(PinballGameStateSettings settings, URL tableResource)
	{
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
            	{
            		loadWorker.stopLoading();
            		setProgress( 0, "Aborting" );
            	}
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
		
        /* Creo el juego nuevo */
        final PinballGameState pinballGS = Main.newPinballGame(settings);
        
        /* Agrego como tarea la carga */
        loadWorker = new LoadWorker(pinballGS, this);
        
		
		setProgress( 0, "Loading..." );
	}
	

	public void startLoading()
	{

	}

	private void endLoad()
	{
        /* Termino de cargar, destruyo el loadinggamestate */
	    Main.endLoading();
		
        if (loadWorker.aborted)
        {
            Main.newMenu().setActive(true);
        }
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
			
			 /* Se pone a cargar la mesa seleccionada al construir el loadinggamestate. */
	        Thread t = new Thread(loadWorker, "LoadingThread");
	        t.start();
	        
			/* Inicio su musica */
			Main.getAudioSystem().getMusicQueue().addTrack(music);
			Main.getAudioSystem().getMusicQueue().setCurrentTrack(music);
		}
		else
		{
			/* Oculto el cursor unicamente cuando arranca el juego */
		    if (loadWorker.isCompleted())
		    {
		        if (!loadWorker.aborted)
		            MouseInput.get().setCursorVisible(false);
		        
	            endLoad();
		    }
		}
	}
	
	@Override
	public void update(float tpf)
	{
		super.update( tpf );

		input.update( tpf );

		/* Actualizo el sistema de sonido */
        Main.getAudioSystem().update();
	}
	
//	@Override
//	public void render(float tpf)
//	{
//        super.render(tpf);
//	}
	
	public void cleanup()
	{
	    rootNode.detachAllChildren();
	    rootNode.clearControllers();
	}
	
	private class LoadWorker implements Runnable, Callable<Void>, Observer
	{
	    private PinballGameState pinballGS;
	    private LoadingGameState loadingGS;
	    private LoaderThread roomLoader, machineLoader, tableLoader;
	    private volatile boolean complete = false, aborted = false;
		private volatile float percentageMachine = 0f, percentageRoom = 0f, percentageTable = 0f;
		
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
	        return (percentageRoom + percentageMachine + percentageTable)/3 ;
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
            roomLoader = new LoaderThread(LoadingGameState.class.getClassLoader().getResource( "resources/models/Room.x3d" ), pinballGS, 0);
            roomLoader.addObserver( this );
            Thread roomThread = new Thread(roomLoader, "roomLoadThread");
            
            machineLoader = new LoaderThread(LoadingGameState.class.getClassLoader().getResource( "resources/models/Machine.x3d" ), pinballGS, 1);
            machineLoader.addObserver( this );
            Thread machineThread = new Thread(machineLoader, "machineLoadThread");
            
            tableLoader = new LoaderThread( tableResource, pinballGS, 2 );
            tableLoader.addObserver( this );
            Thread tableThread = new Thread(tableLoader, "tableLoadThread");
            
            // Paralelizo
            if ( !aborted ) 
            {
                roomThread.start();
                machineThread.start();
                tableThread.start();
                
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
            this.loadingGS.setProgress( 1 );
		}

        public Void call() throws Exception
        {
            if (!complete)
                this.loadingGS.setProgress( getPercentage() );
//            System.out.println("percent " + Integer.toString( (int)(getPercentage() * 100) ) + ": " + Boolean.toString( (int)(getPercentage() * 100) == 100) );
            return null;
        }
        
        public void stopLoading()
        {
            roomLoader.stop();
            machineLoader.stop();
            tableLoader.stop();
            aborted = true;
        }

        @Override
        public void update( Observable o, Object arg )
        {
            if (o instanceof LoaderThread)
            {
                LoaderThread lt = (LoaderThread)o;
                int id = lt.getID();
                if (id == 0)
                    percentageRoom = lt.getPercentComplete();
                else if (id == 1)
                    percentageMachine = lt.getPercentComplete();
                else if (id == 2)
                    percentageTable = lt.getPercentComplete();
            }
            
            if (!complete && !aborted )
            {
                GameTaskQueueManager.getManager().update(this);
            }
        }
        
	}
}
