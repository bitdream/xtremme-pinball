package gamestates;

import input.FengJMEInputHandler;

import main.Main;

import org.fenggui.Button;
import org.fenggui.Display;
import org.fenggui.FengGUI;
import org.fenggui.binding.render.lwjgl.LWJGLBinding;
import org.fenggui.composite.Window;
import org.fenggui.event.ButtonPressedEvent;
import org.fenggui.event.IButtonPressedListener;
import org.fenggui.layout.RowLayout;
import org.fenggui.util.Point;
import org.fenggui.util.Spacing;
import org.lwjgl.opengl.GL13;

import themes.CarsThemeGameLogic;

import com.jme.input.MouseInput;
import com.jmex.audio.AudioTrack;
import com.jmex.game.state.BasicGameState;


public class MenuGameState extends BasicGameState
{
	/* Pantalla para FengGUI */
	private Display fengGUIdisplay;
	
	/* Input Handler de FengGUI, sirve para que FengGUI capture las teclas
	 * y las envie al display de JME para ser capturadas por los otros handlers */
	private FengJMEInputHandler fengGUIInputHandler;

	/* Botones del menu */
	private Button continueButton, newGameButton, exitButton;
	
	/* Musica del menu */
	private AudioTrack music;
	

	public MenuGameState(String name)
	{
		super(name);
		
		/* Inicializo la musica */
		music = Main.getAudioSystem().createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/menu-loading/music.wav"), false);
		music.setLooping(true);
		
		/* Inicializo el menu */
		initMenu();
	}

	/**
	 * Inicializa el menu.
	 */
	protected void initMenu()
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

		/* Boton de continuar */
		continueButton = FengGUI.createButton(menu.getContentContainer(), "Continue");
		
		continueButton.addButtonPressedListener(new IButtonPressedListener() {
			
			public void buttonPressed(ButtonPressedEvent arg0) {

				/* Continuo el juego actual */
				Main.continueCurrentPinballGame();
				
				/* Desactivo el gamestate de menu */
				Main.deactivateMenu();

			}
		});

		/* Boton de juego nuevo */
		newGameButton = FengGUI.createButton(menu.getContentContainer(), "New game");
		
		newGameButton.addButtonPressedListener(new IButtonPressedListener() {
			
			public void buttonPressed(ButtonPressedEvent arg0) {

				/* Desactivo el gamestate de menu */
				Main.deactivateMenu();

				/* Mato el juego actual si hay alguno */
				Main.endCurrentPinballGame();
				
				/* Creo un juego nuevo y lo inicio */
				Main.newPinballGame().setActive(true);
			}
		});
		
		/* Boton de salir */
		exitButton = FengGUI.createButton(menu.getContentContainer(), "Exit");
		
		exitButton.addButtonPressedListener(new IButtonPressedListener() {
			
			public void buttonPressed(ButtonPressedEvent arg0)
			{
				/* Acabo con todo el juego */
				Main.shutdownGame();
			}
		});
 
		/* Comprime lo posible los botones */
		//menu.pack(); TODO darle algo de estilo al menu y agregarle un fondo
 
		/* Actualizo la pantalla con los nuevos componentes */
		fengGUIdisplay.layout();
	}
	
	@Override
	public void setActive(boolean active)
	{
		super.setActive(active);

		if (active)
		{
			/* Hago visible al cursor */
			MouseInput.get().setCursorVisible(true);
			
			/* Si hay juegos en transcurso, muestro el boton de continue */
			continueButton.setVisible(Main.hasInCourseGame());
		
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
