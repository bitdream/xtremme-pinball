package input;

import gamestates.PinballGameState;
import main.Main;
import com.jme.input.FirstPersonHandler;
import com.jme.input.InputHandler;
import com.jme.input.KeyInput;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jmex.physics.DynamicPhysicsNode;
import components.Flipper;
import components.Plunger;

/**
 * Es el controlador de input para el tablero de juego en si.
 * Hereda de FirstPersonHandler para poder permitir al jugador el movimiento de la camara
 * con WSAD.
 */
public class PinballInputHandler extends FirstPersonHandler
{
	private PinballGameState game;
	
	// Para la accion de tilt
	// Cantidad maxima de tilts permitidos dentro del intervalo de tiempo especificado
	private static int tiltsAllowed = 5;
	// Intervalo de timepo en milisegundos
	private static long tiltPenalizationTimeInterval = 5000; // Son 5 segundos	
	//Intervalo de tiempo luego del cual se blanquearan los tilts anteriores y se podra seguir usandolo sin penalizacion
	private static long tiltFreeTimeInterval = 5000; // Son 5 segundos
	
	public PinballInputHandler(PinballGameState game)
	{
		/* Llamo al constructor de FirstPersonHandler pasandole las velocidades de camara del juego */
		super(game.getCamera(), game.getPinballSettings().getCamMoveSpeed(), game.getPinballSettings().getCamTurnSpeed());
		
		this.game = game;
		
		setActions();
	}
	
	private void setActions()
	{
		/* Abrir menu */
		addAction(new OpenMenuAction(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_ESCAPE, InputHandler.AXIS_NONE, false);
		
		/* Golpear con flippers derechos */
		// TODO addAction(new RightFlippersAction(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_RCONTROL, InputHandler.AXIS_NONE, true); //KeyInput.KEY_RSHIFT
		addAction(new RightFlippersActionOnce(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_RCONTROL, InputHandler.AXIS_NONE, false); //KeyInput.KEY_RSHIFT
		
		/* Golpear con flippers izquierdos */
		// TODO addAction(new LeftFlippersAction(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_LCONTROL, InputHandler.AXIS_NONE, true); // KeyInput.KEY_LSHIFT
		addAction(new LeftFlippersActionOnce(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_LCONTROL, InputHandler.AXIS_NONE, false); // KeyInput.KEY_LSHIFT
		
		/* Activar plunger */
		addAction(new ChargePlungerAction(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_RETURN, InputHandler.AXIS_NONE, false);
		
		/* Hacer el tilt */
		addAction(new tiltAction(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_SPACE, InputHandler.AXIS_NONE, false);
	}
	
	/* Accion para abrir el menu de juego */
	private class OpenMenuAction extends InputAction
	{

		public void performAction(InputActionEvent event)
		{
			if(event.getTriggerPressed())
			{
				Main.pauseCurrentPinballGame();
				Main.activateMenu();
			}
		}
		
	}
	
	/* TODO (quitada para usar velocs y accels) Accion para golpear con flippers derechos */
//	private class RightFlippersAction extends InputAction
//	{
//		private final Vector3f forceToApply = new Vector3f();
//		
//		private Quaternion rot = game.getPinballSettings().getInclinationQuaternion();
//
//		public void performAction(InputActionEvent event)
//		{
//			if(event.getTriggerPressed())
//			{
//				/* Presiona la tecla, fijo la fuerza a aplicar */
//				forceToApply.set(Flipper.flipperHitForce).multLocal(event.getTime());
//
//				for (DynamicPhysicsNode flipper : game.getFlippers())
//				{
//					Flipper actualFlipper = (Flipper)flipper.getChild(0);
//					/* Aplico la fuerza sobre los flippers derechos */
//					if (actualFlipper.isRightFlipper() && actualFlipper.isActive())
//					{
//						flipper.addForce(forceToApply.rotate(rot));
//					}
//				}
//			}
//		}
//		
//	}
	
	/* Accion para golpear con flippers derechos por unica vez */
	private class RightFlippersActionOnce extends InputAction
	{
		public void performAction(InputActionEvent event)
		{
			if(event.getTriggerPressed())
			{
				for (DynamicPhysicsNode flipper : game.getFlippers())
				{
					Flipper actualFlipper = (Flipper)flipper.getChild(0);
				
					if (actualFlipper.isRightFlipper() && actualFlipper.isActive())
					{
						/* Aviso a la logica de juego */
						game.getGameLogic().rightFlipperMove(actualFlipper);
						
						/* Lo marco como en uso */
						actualFlipper.setInUse(true);
					}
				}
			}
			else
			{
				for (DynamicPhysicsNode flipper : game.getFlippers())
				{
					Flipper actualFlipper = (Flipper)flipper.getChild(0);
				
					if (actualFlipper.isRightFlipper() && actualFlipper.isActive())
					{
						/* Dejo de usar este flipper */
						actualFlipper.setInUse(false);
					}
				}
			}
		}
	}
	
	/* TODO (quitada para usar velocs y accels) Accion para golpear con flippers izquierdos */
//	private class LeftFlippersAction extends InputAction
//	{
//		private final Vector3f forceToApply = new Vector3f();
//		
//		private Quaternion rot = game.getPinballSettings().getInclinationQuaternion();
//
//		public void performAction(InputActionEvent event)
//		{
//			if(event.getTriggerPressed())
//			{
//				/* Presiona la tecla, fijo la fuerza a aplicar */
//				forceToApply.set(Flipper.flipperHitForce).multLocal(event.getTime());
//
//				for (DynamicPhysicsNode flipper : game.getFlippers())
//				{
//					Flipper actualFlipper = (Flipper)flipper.getChild(0);
//					/* Aplico la fuerza sobre los flippers izquierdos */
//					if (actualFlipper.isLeftFlipper() && actualFlipper.isActive())
//					{
//						flipper.addForce(forceToApply.rotate(rot));
//					}
//				}
//			}
//		}
//	}
	
	/* Accion para golpear con flippers izquierdos por unica vez */
	private class LeftFlippersActionOnce extends InputAction
	{
		public void performAction(InputActionEvent event)
		{
			if(event.getTriggerPressed())
			{
				for (DynamicPhysicsNode flipper : game.getFlippers())
				{
					Flipper actualFlipper = (Flipper)flipper.getChild(0);
					/* Aplico la fuerza sobre los flippers izquierdos */
					if (actualFlipper.isLeftFlipper() && actualFlipper.isActive())
					{
						/* Aviso a la logica de juego */
						game.getGameLogic().leftFlipperMove(actualFlipper);
						
						/* Lo marco como en uso */
						actualFlipper.setInUse(true);
					}
				}
			}
			else
			{
				for (DynamicPhysicsNode flipper : game.getFlippers())
				{
					Flipper actualFlipper = (Flipper)flipper.getChild(0);
				
					if (actualFlipper.isLeftFlipper() && actualFlipper.isActive())
					{
						/* Dejo de usar este flipper */
						actualFlipper.setInUse(false);
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
			if (game.getPlunger() == null)
				return;
			
			Plunger plunger = (Plunger)(game.getPlunger().getChild(0));
			
			if(event.getTriggerPressed())
			{
				/* El jugador agarro el plunger, ya no esta suelto */
				plunger.setLoose(false);
				
				/* Aviso a la logica de juego */
				game.getGameLogic().plungerCharge(plunger);
			}
			else if (!event.getTriggerPressed())
			{
				/* Solto el plunger, guardo la posicion hasta donde llego y
				 * lo marco como soltado */
				plunger.setLoose(true);
				plunger.setDistance(game.getPlunger().getLocalTranslation().z);
				
				/* Aviso a la logica de juego */
				game.getGameLogic().plungerRelease(plunger);
			}
		}
		
	}
	
	/* Accion para realizar el tilt */
	private class tiltAction extends InputAction
	{
		// Cantidad de tiempos actualmente cargados en el arreglo de tiempos
		int actualTiltArrayElems = 0;
		
		// Arreglo de tiempos
		long [] tiltArrayTimes = new long[tiltsAllowed]; 
		
		//Ultimo tiempo en que se hizo tilt
		long lastTiltTime = 0;
		
		public void performAction(InputActionEvent event)
		{
			Vector3f cameraMovement = new Vector3f(3,3,3);
			Vector3f initCameraPos = game.getCamera().getLocation();

			
			if(event.getTriggerPressed())
			{
				// Intensidad de la fuerza a aplicar
				float forceIntensity = 500; //90000f;
				// Computo la fuerza a aplicar sobre las bolas. Es la misma para cada una de ellas. La direccion se determina de forma aleatorea
				Vector3f force = new Vector3f(FastMath.sign(FastMath.nextRandomInt(-1, 1)),
						0.0f, /*FastMath.sign(FastMath.nextRandomInt(-1, 1)) , */
						FastMath.sign(FastMath.nextRandomInt(-1, 1))).mult(forceIntensity);
				
				 // Tomo cada bola de la escena y le aplico la fuerza
				 for (DynamicPhysicsNode ball: game.getBalls()) 
		         {		                                       
                    // Fuerza random en las tres direcciones. La bola puede saltar y colisionar contra el vidrio.
                    ball.addForce(force.mult(ball.getMass()));
                    
                    // Muevo la camara
                    game.getCamera().setLocation(initCameraPos.add(cameraMovement));	            	                
		         }
				 
				 // Agregado de la logica necesaria para la deteccion del uso abusivo de tilt
				 
  			     // Tiempo actual
				 long now = System.currentTimeMillis();
				 
				 // Si la diferencia entre el ultimo tilt y el actual es mayor a tiltFreeTimeInterval seg, blanqueo el arreglo. Ademas debe haber habido al menos 1 tilt anterior
				 if (now - lastTiltTime > tiltFreeTimeInterval && lastTiltTime != 0)
				 {
					 // Poner en cero los elementos
					for (int i=0; i<tiltArrayTimes.length;i++)
					{
						tiltArrayTimes[i] = 0;
					}
					
					// Inicializar variables
					actualTiltArrayElems = 0;
					lastTiltTime = 0;					
				 }				 
				 
				 // Shifteo, almaceno el tiempo actual en el arreglo y actualizo variables
				 shiftLeft(tiltArrayTimes);
				 tiltArrayTimes[tiltsAllowed - 1] = now;
				 lastTiltTime = tiltArrayTimes[tiltsAllowed - 1];
				 
				 // Actualizar la cantidad de elementos en el arreglo de tiempo (como maximo siempre habra tiltsAllowed)
				 if (actualTiltArrayElems < tiltsAllowed )
				 {
					 actualTiltArrayElems++;
				 }					 
				 
				 // Si la diferencia entre el tiempo mas viejo almacenado y el ultimo es menor a tiltsAllowed seg, invocar abuseTilt()
				 // Se realizo tilt tiltsAllowed veces o mas durante un lapso de tiltPenalizationTimeInterval milisegundos
				 if (lastTiltTime - tiltArrayTimes[0] < tiltPenalizationTimeInterval && actualTiltArrayElems == tiltsAllowed)
				 {
					 /* Aviso a la logica de juego */
					 game.getGameLogic().abuseTilt();
				 }
				 else
				 {
					 /* Aviso a la logica de juego */
					 game.getGameLogic().tilt();
				 }			

			}
			else if (!event.getTriggerPressed())
			{
				// Se solto la tecla de tilt. Vuelvo la camara a la posicion original.
				game.getCamera().setLocation(initCameraPos.subtract(cameraMovement));
			}
		
		}
		
		private void shiftLeft(long[] arr)
		{
			for(int i=0; i<arr.length-1;i++)
			{
				arr[i] = arr[i+1];
			}
		}
	}
}
