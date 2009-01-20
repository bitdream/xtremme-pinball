package input;

import mainloop.Pinball;

import com.jme.input.FirstPersonHandler;
import com.jme.input.InputHandler;
import com.jme.input.KeyInput;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.shape.Sphere;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.PhysicsNode;

import components.Flipper;
import components.Plunger;

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
		
		/* Golpear con flippers derechos */
		addAction(new RightFlippersAction(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_RSHIFT, InputHandler.AXIS_NONE, true);
		
		/* Golpear con flippers izquierdos */
		addAction(new LeftFlippersAction(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_LSHIFT, InputHandler.AXIS_NONE, true);
		
		/* Retraer plunger */
		addAction(new ChargePlungerAction(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_RETURN, InputHandler.AXIS_NONE, false);
		
		/* Hacer el tilt */
		addAction(new tiltAction(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_M, InputHandler.AXIS_NONE, false);
		
		// TODO colocar las acciones que correspondan a pinball
		// pasarle en constructor una lista de flippers, el nodo base para hacer tilt y el plunger?... y el juego! (para finish, etc...)
	}
	
	@Override
	public void update(float time)
	{
		super.update(time);
		
		Quaternion rot = game.getPinballSettings().getInclinationQuaternion();
		
		/* Cada vez que el motor de fisica llama a actualizacion, aplico la fuerza
		 * de recuperacion de los flippers */
		final Vector3f forceToApply = new Vector3f();
		
		forceToApply.set(Flipper.flipperRestoreForce).multLocal(event.getTime());

		for (DynamicPhysicsNode flipper : game.getFlippers())
		{
			flipper.addForce(forceToApply.rotate(rot));
		}
		
		/* Plunger */
		Plunger plunger;
		if (game.getPlunger() != null)
		{
			 plunger = (Plunger)game.getPlunger().getChild(0);
		
			if (plunger.isLoose()) /* Esta suelto, aplico una fuerza proporcional al cuadrado de la distancia que obtuvo */
				game.getPlunger().addForce((new Vector3f(0, 0,
						-10 * game.getPinballSettings().getInclinationAngle()
						-1000 * (float)Math.pow(plunger.getDistance(), 2))
				).rotate(rot));
			else /* Aplico la fuerza para alejarlo del origen */
				game.getPlunger().addForce(Plunger.plungerChargeForce.rotate(rot));
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
		
		private Quaternion rot = game.getPinballSettings().getInclinationQuaternion();

		public void performAction(InputActionEvent event)
		{
			if(event.getTriggerPressed())
			{
				/* Presiona la tecla, fijo la fuerza a aplicar */
				forceToApply.set(Flipper.flipperHitForce).multLocal(event.getTime());

				for (DynamicPhysicsNode flipper : game.getFlippers())
				{
					/* Aplico la fuerza sobre los flippers derechos */
					if (((Flipper)flipper.getChild(0)).isRightFlipper())
					{
						flipper.addForce(forceToApply.rotate(rot));
					}
				}
			}
		}
		
	}
	
	/* Accion para golpear con flippers izquierdos */
	private class LeftFlippersAction extends InputAction
	{
		private final Vector3f forceToApply = new Vector3f();
		
		private Quaternion rot = game.getPinballSettings().getInclinationQuaternion();

		public void performAction(InputActionEvent event)
		{
			if(event.getTriggerPressed())
			{
				/* Presiona la tecla, fijo la fuerza a aplicar */
				forceToApply.set(Flipper.flipperHitForce).multLocal(event.getTime());

				for (DynamicPhysicsNode flipper : game.getFlippers())
				{
					/* Aplico la fuerza sobre los flippers izquierdos */
					if (((Flipper)flipper.getChild(0)).isLeftFlipper())
					{
						flipper.addForce(forceToApply.rotate(rot));
					}
				}
			}
		}
		
	}
	
	/* Accion para retraer el plunger */
	private class ChargePlungerAction extends InputAction
	{

		public void performAction(InputActionEvent event)
		{
			Plunger plunger = (Plunger)(game.getPlunger().getChild(0));
			
			if(event.getTriggerPressed())
			{
				/* Agarro el plunger, ya no esta suelto */
				plunger.setLoose(false);
			}
			else if (!event.getTriggerPressed())
			{
				/* Solto el plunger, guardo la posicion hasta donde llego y
				 * lo marco como soltado */
				plunger.setLoose(true);
				plunger.setDistance(game.getPlunger().getLocalTranslation().z);
			}
		}
		
	}
	
	/* Accion para realizar el tilt */
	private class tiltAction extends InputAction
	{
		public void performAction(InputActionEvent event)
		{
			Vector3f cameraMovement = new Vector3f(3,3,3);
			Vector3f initCameraPos = game.getCamera().getLocation();
			
			if(event.getTriggerPressed())
			{
				// Intensidad de la fuerza a aplicar
				float forceIntensity = 500; //90000f;
				// Computo la fuerza a aplicar sobre las bolas. Es la misma para cada una de ellas. La direccion se determina de forma aleatorea
				Vector3f force = new Vector3f(/*FastMath.nextRandomFloat() * */FastMath.sign(FastMath.nextRandomInt(-1, 1)) , 
						/*FastMath.nextRandomFloat() * */FastMath.sign(FastMath.nextRandomInt(-1, 1)),
						/*FastMath.nextRandomFloat() * */FastMath.sign(FastMath.nextRandomInt(-1, 1))).mult(forceIntensity);
				
				// Tomo cada bola de la escena y le aplico la fuerza
				 for (PhysicsNode node: game.getPhysicsSpace().getNodes()) 
		         {	          
		            	// TODO ver: estoy suponiendo que las bolas del flipper van a estar formadas por un nodo fisico con un 
		            	// unico nodo visual attacheado. Y que dicho nodo visual sera una esfera. 
		            	// Otra forma de identificarlo: el nombre del nodo fisico por convencion el "ball"
		                if (node instanceof DynamicPhysicsNode && node.getChild(0) instanceof Sphere) 
		                {
		                    DynamicPhysicsNode ball = (DynamicPhysicsNode)node;
		                               
		                    // Fuerza random en las tres direcciones. La bola puede saltar y colisionar contra el vidrio.
		                    ball.addForce(force.mult(ball.getMass()));
		                    
		                    // Muevo la camara
		                    game.getCamera().setLocation(initCameraPos.add(cameraMovement));		                    
		                    
		                }		                
		         }
			}
			else if (!event.getTriggerPressed())
			{
				// Se solto la tecla de tilt. Vuelvo la camara a la posicion original.
				game.getCamera().setLocation(initCameraPos.subtract(cameraMovement));
			}
		
		}
	}
}
