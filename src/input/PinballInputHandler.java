package input;

import mainloop.Pinball;

import com.jme.input.FirstPersonHandler;
import com.jme.input.InputHandler;
import com.jme.input.KeyInput;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.math.Vector3f;
import com.jmex.physics.DynamicPhysicsNode;
import components.Flipper;

/**
 * Es el controlador de input para el tablero de juego en si.
 * Hereda de FirstPersonHandler para poder permitir al jugador el movimiento de la camara
 * con WSAD.
 */
public class PinballInputHandler extends FirstPersonHandler
{
	private Pinball game;
	
	private static final Vector3f flipperForce = new Vector3f(0f, 0f, -60000f);
	
	public PinballInputHandler(Pinball game)
	{
		/* Llamo al constructor de FirstPersonHandler pasandole las velocidades de camara del juego */
		super(game.getCamera(), game.getPinballSettings().getCamMoveSpeed(), game.getPinballSettings().getCamTurnSpeed());
		
		this.game = game;
		
		/* Comienza deshabilitado */
		setEnabled(false);
		
		setActions();
	}
	

	private void setActions()
	{
		/* Abrir menu */
		addAction(new OpenMenuAction(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_ESCAPE, InputHandler.AXIS_NONE, false);
		
		/* Golpear con flippers izquierdos */
		addAction(new RightFlippersAction(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_RSHIFT, InputHandler.AXIS_NONE, true);
		
		
		// TODO colocar las acciones que correspondan a pinball
		// pasarle en constructor una lista de flippers, el nodo base para hacer tilt y el plunger?... y el juego! (para finish, etc...)
	}
	
	/* Accion para abrir el menu de juego */
	private class OpenMenuAction extends InputAction
	{

		public void performAction(InputActionEvent event)
		{
			if(event.getTriggerPressed())
				game.showMenu();
			
		}
		
	}
	
	/* Accion para golpear con flippers izquierdos */
	private class RightFlippersAction extends InputAction
	{
		private final Vector3f forceToApply = new Vector3f();

		public void performAction(InputActionEvent event)
		{
			/* Fijo la fuerza a aplicar */
			forceToApply.set(flipperForce).multLocal(event.getTime());
			
			if(event.getTriggerPressed())
			{
				/* Presiona la tecla */
				System.out.println("pega");
				/* Aplico la fuerza sobre los flippers */

				for (DynamicPhysicsNode flipper : game.getFlippers())
				{
					if (((Flipper)flipper.getChild(0)).isRightFlipper())
						{flipper.addForce(forceToApply); System.out.println("aplica fza"); System.out.println(flipper.getLocalTranslation());}
				}
			}
			else
			{
				/* Suelta la tecla */
				System.out.println("suelta");
			}
						
            // the really important line: apply a force to the moved node
            //dynamicNode.addForce( forceToApply );
			
		}
		
	}
}
