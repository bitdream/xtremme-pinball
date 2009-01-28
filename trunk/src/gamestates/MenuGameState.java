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

import com.jme.input.MouseInput;
import com.jmex.game.state.BasicGameState;


public class MenuGameState extends BasicGameState
{
	/* Pantalla para FengGUI */
	private Display fengGUIdisplay;
	
	/* Input Handler de FengGUI, sirve para que FengGUI capture las teclas
	 * y las envie al display de JME para ser capturadas por los otros handlers */
	private FengJMEInputHandler fengGUIInputHandler;
	

	public MenuGameState(String name)
	{
		super(name);
		
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

		// TODO falta el boton de continuar juego... poner todo esto en setActive?
		
		/* Boton de juego nuevo */
		final Button newGameButton = FengGUI.createButton(menu.getContentContainer(), "New game");
		
		newGameButton.addButtonPressedListener(new IButtonPressedListener() {
			
			public void buttonPressed(ButtonPressedEvent arg0) {

				/* Mato el juego actual si hay alguno */
				Main.endCurrentPinballGame();
				
				/* Creo un juego nuevo y lo inicio */
				Main.newPinballGame().setActive(true);
				
				/* Desactivo el gamestate de menu */
				Main.deactivateMenu();

			}
		});
		
		/* Boton de salir */
		final Button exitButton = FengGUI.createButton(menu.getContentContainer(), "Exit");
		
		exitButton.addButtonPressedListener(new IButtonPressedListener() {
			
			public void buttonPressed(ButtonPressedEvent arg0)
			{
				/* Acabo con todo el juego */
				Main.shutdownGame();
			}
		});
 
		/* Comprime lo posible los botones */
		//menu.pack(); TODO darle algo de estilo al menu
 
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