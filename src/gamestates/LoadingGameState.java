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
	private Thread loadingThread;

	/* Angulo de inclinacion requerido para el juego a crear */
	private float inclinationAngle;

	/* Recurso de la mesa */
	private URL tableResource;
	

	public LoadingGameState(String name, float inclinationAngle, URL tableResource)
	{
		super(name);

		this.inclinationAngle = inclinationAngle;
		this.tableResource = tableResource;
		
		/* TODO (buscar musica de loading) Inicializo la musica */
		music = Main.getAudioSystem().createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/menu-loading/music.wav"), false);
		music.setLooping(true);
		
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
		final Window progressInfo = FengGUI.createWindow(fengGUIdisplay, false, false, false, true);
		progressInfo.setTitle("Loading...");
		progressInfo.setSize(200, 200);
		progressInfo.getContentContainer().setLayoutManager(new RowLayout(false));
		//progressInfo.getContentContainer().getAppearance().setPadding(new Spacing(10, 10));
		
		/* Creo el mensaje de progreso */
		final Label loadingLabel = FengGUI.createLabel("Load message");
		progressInfo.addWidget(loadingLabel);
		
		/* Creo el juego nuevo */
		final PinballGameState pinballGS = Main.newPinballGame(inclinationAngle);
		
		/* Thread de loading */
        loadingThread = new Thread(new Runnable() {

			public void run() {
				
				/* Creo los threads que crean la habitacion, la maquina y la mesa requerida */
				LoaderThread threadRoom = new LoaderThread(LoadingGameState.class.getClassLoader().getResource( "resources/models/Room.x3d" ), pinballGS);
				Thread loadRoom = new Thread(threadRoom, "roomLoadThread");
		        
		        LoaderThread threadMachine = new LoaderThread(LoadingGameState.class.getClassLoader().getResource( "resources/models/Machine.x3d" ), pinballGS);
		        Thread loadMachine = new Thread(threadMachine, "machineLoadThread");
		        
		        LoaderThread threadTable = new LoaderThread( tableResource, pinballGS );
		        Thread loadTable = new Thread(threadTable, "tableLoadThread");
		        
		        /* Los inicio */
		        // Secuencial
		        /*try
		        {
		        	loadRoom.start();
		            loadRoom.join();

		            loadMachine.start();
		            loadMachine.join();
		            
		            loadTable.start();
		            loadTable.join();
		        }
		        catch(Exception e){}*/

		        // Paralelo
		        loadRoom.start();
		        loadMachine.start();
		        loadTable.start();
		        try
		        {
		            loadRoom.join();
		            loadMachine.join();
		            loadTable.join();
		        }
		        catch(Exception e){}
		        
				/*while(value <= 1) {
					value += Math.random()*0.1;
					loadingLabel.setText(String.valueOf(value));
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}*/
		        
		        /* Atacheo los resultados */
                pinballGS.getRootNode().attachChild(threadRoom.getScene());    
                pinballGS.getRootNode().attachChild(threadMachine.getScene());
                pinballGS.getRootNode().attachChild(pinballGS.inclinePinball(threadTable.getScene()));
                
				/* Termino de cargar, cierro la ventana y destruyo el loadinggamestate */
				progressInfo.close();
				Main.endLoading();
				
				/* Ya se cargo la escena en el pinball creado. Ahora lo inicio. */
				pinballGS.setGameLogic(threadTable.getTheme());
				pinballGS.initGame();
				pinballGS.setActive(true);
			}});

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
        loadingThread.start();
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
			Main.getAudioSystem().getMusicQueue().clearTracks();
			Main.getAudioSystem().getMusicQueue().addTrack(music);
			Main.getAudioSystem().getMusicQueue().play();
		}
		else
		{
			/* Oculto el cursor */
			MouseInput.get().setCursorVisible(false);
			
			/* Detengo su musica */
			Main.getAudioSystem().getMusicQueue().clearTracks();
			
			// TODO hacer fadeout!
		}
	}
	
	@Override
	public void update(float tpf)
	{
		super.update(tpf);

		/* Actualizo el controlador de input */
        fengGUIInputHandler.update(tpf);
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
}
