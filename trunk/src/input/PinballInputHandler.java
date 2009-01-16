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
	
	@Override
	public void update(float time)
	{
		super.update(time);
		
		/* Cada vez que el motor de fisica llama a actualizacion, aplico la fuerza
		 * de recuperacion de los flippers */
		final Vector3f forceToApply = new Vector3f();
		
		forceToApply.set(Flipper.flipperRestoreForce).multLocal(event.getTime());
		
		for (DynamicPhysicsNode flipper : game.getFlippers())
		{
			flipper.addForce(forceToApply);
			//flipper.addForce(forceToApply, new Vector3f(2.5f, 0, 0));
		}
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
	
	/* Accion para golpear con flippers derechos */
	private class RightFlippersAction extends InputAction
	{
		private final Vector3f forceToApply = new Vector3f();

		public void performAction(InputActionEvent event)
		{
			if(event.getTriggerPressed())
			{
				/* Presiona la tecla, fijo la fuerza a aplicar */
				forceToApply.set(Flipper.flipperHitForce).multLocal(event.getTime());

				for (DynamicPhysicsNode flipper : game.getFlippers())
				{
					/* Aplico la fuerza sobre los flippers */
					if (((Flipper)flipper.getChild(0)).isRightFlipper())
					{
						flipper.addForce(forceToApply);
						//flipper.addForce(forceToApply, new Vector3f(2.5f, 0, 0));
					}
				}
			}
		}
		
	}
}
