package input;

import gamestates.PinballGameState;
import main.Main;

import com.jme.input.FirstPersonHandler;
import com.jme.input.InputHandler;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
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
	
	private boolean tiltActive = true;
	
	@Override
	public void setEnabled( boolean value )
	{
	    super.setEnabled( value );
	    KeyInput.get().clear();
	    MouseInput.get().clear();
	}
	
	public boolean isTiltActive() 
	{
		return tiltActive;
	}

	public void setTiltActive(boolean tiltActive) 
	{
		this.tiltActive = tiltActive;
	}
	
	private boolean newGameActive = false;
	
	public boolean isNewGameActive() 
	{
		return newGameActive;
	}

	public void setNewGameActive(boolean newGameActive) 
	{
		this.newGameActive = newGameActive;
	}

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
		addAction(new RightFlippersActionOnce(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_M, InputHandler.AXIS_NONE, false);
		addAction(new RightFlippersActionOnce(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_RCONTROL, InputHandler.AXIS_NONE, false);
		addAction(new RightFlippersActionOnce(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_RSHIFT, InputHandler.AXIS_NONE, false);
		
		/* Golpear con flippers izquierdos */
		addAction(new LeftFlippersActionOnce(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_C, InputHandler.AXIS_NONE, false);
		addAction(new LeftFlippersActionOnce(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_LCONTROL, InputHandler.AXIS_NONE, false);
		addAction(new LeftFlippersActionOnce(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_LSHIFT, InputHandler.AXIS_NONE, false);
		
		/* Activar plunger */
		addAction(new ChargePlungerAction(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_RETURN, InputHandler.AXIS_NONE, false);
		
		/* Hacer el tilt */
		addAction(new tiltAction(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_SPACE, InputHandler.AXIS_NONE, false);
		
		/* Nuevo juego */
		addAction(new NewGameAction(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_N, InputHandler.AXIS_NONE, false);
        
		/* Posiciones de camara */
		addAction(new ChangeCameraAction(game.getCamera(), game.getPinballSettings().getCamStartPos(), game.getPinballSettings().getCamStartLookAt()), 
            InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_1, InputHandler.AXIS_NONE, false );
        
        addAction( new ChangeCameraAction(game.getCamera(), new Vector3f( -0.92f, 0.5f, 1.0f ), new Vector3f(-0.92f,0.90f,-1.30f)), 
            InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_2, InputHandler.AXIS_NONE, false );
        
        addAction( new ChangeCameraAction(game.getCamera(), new Vector3f( -0.90f, 13.5f, -21.5f ), new Vector3f( -0.90f, -15.5f, 0f )), 
            InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_3, InputHandler.AXIS_NONE, false );
        
        addAction( new ChangeCameraAction(game.getCamera(), new Vector3f( 27, 19, 27 ), new Vector3f( 0f, 5f, 0f )), 
            InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_4, InputHandler.AXIS_NONE, false );
        
        // reset de camara
        addAction(new ChangeCameraAction(game.getCamera(), game.getPinballSettings().getCamStartPos(), game.getPinballSettings().getCamStartLookAt()), 
            InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_HOME, InputHandler.AXIS_NONE, false );

        // F1 screenshot
        addAction( new ScreenShotAction(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F1, InputHandler.AXIS_NONE, false );
        
        /* Activar/desactivar show FPS */
        addAction(new ShowFPSAction(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_F, InputHandler.AXIS_NONE, false);
        
        /* Pausar/Despausar fisica */
        // P pausa fisica
        addAction( new PauseAction(), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_P, InputHandler.AXIS_NONE, false );
    }
    
	/* Accion de pausar la fisica */
	private class PauseAction extends InputAction
    {
        public void performAction( InputActionEvent evt )
        {
            if ( evt.getTriggerPressed() )
            {
                game.togglePause();
            }
        }
    }
	
	/* Accion de sacar una screenshot */
	private class ScreenShotAction extends InputAction
    {
	    private int counter = 0;
	    
        public void performAction( InputActionEvent evt )
        {
            if ( evt.getTriggerPressed() )
            {
                game.getDisplay().getRenderer().takeScreenShot( "ScreenShot" + counter++ );
            }
        }
    }
	
	/* Accion de activar o desactivar la muestra de FPS */
    private class ShowFPSAction extends InputAction
    {
        public void performAction( InputActionEvent evt )
        {
            if (evt.getTriggerPressed())
            {
           		game.toggleShowFPS();
            }        
    	}
    }

	/* Accion de comenzar nuevo juego luego de perder */
    private class NewGameAction extends InputAction
    {
        public void performAction( InputActionEvent evt )
        {
        	// Tecla con efecto solo si se acaba de terminar un juego
            if ( evt.getTriggerPressed() && newGameActive )
            {
           		game.reinitGame();
            }        
    	}
    }
    
    
	/* accion de la camara */
    private class ChangeCameraAction extends InputAction
    {
        private Vector3f location, lookAtpos;
        private Camera   cam;
    
        public ChangeCameraAction( Camera cam, Vector3f location, Vector3f lookAtpos )
        {
            this.cam = cam;
            this.location = location;
            this.lookAtpos = lookAtpos;
        }
    
        public void performAction( InputActionEvent evt )
        {
            if ( evt.getTriggerPressed() )
            {
                cam.setLocation( new Vector3f(location) );
                
                cam.lookAt( new Vector3f(lookAtpos), new Vector3f( 0.0f, 1.0f, 0.0f ) );
            }
        
    	}
    }

	
	/* Accion para abrir el menu de juego */
	private class OpenMenuAction extends InputAction
	{

		public void performAction(InputActionEvent event)
		{
			if(event.getTriggerPressed())
			{
				Main.pauseCurrentPinballGame();
				Main.newMenu().setActive(true);
			}
		}
		
	}
	
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
				
					if (actualFlipper.isRightFlipper())
					{
						/* Dejo de usar este flipper */
						actualFlipper.setInUse(false);
					}
				}
			}
		}
	}
	
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
				
					if (actualFlipper.isLeftFlipper())
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
		
		// Ultimo tiempo en que se hizo tilt
		long lastTiltTime = 0;
		
		// Indica si queda pendiente reposicionar la camara
		boolean pendingRestore = false;
		
		public void performAction(InputActionEvent event)
		{
			Vector3f cameraMovement = new Vector3f(3,3,3);
			Vector3f initCameraPos = game.getCamera().getLocation();

			// Si el tilt no esta activo, no debo permitir hacer tilt (aunque si reposicionar la camara)
	
			if ( !event.getTriggerPressed() && (tiltActive || pendingRestore) )
			{
				// Se solto la tecla de tilt. Vuelvo la camara a la posicion original. A pesar de q el tilt este desactivo!
				game.getCamera().setLocation(initCameraPos.subtract(cameraMovement));
				
				if (pendingRestore)
				{
					pendingRestore = false;
				}				
				
			}
			else if(event.getTriggerPressed() && tiltActive)
			{
				// Intensidad de la fuerza a aplicar
				float forceIntensity = 150;
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
					 
					 // Queda pendiente restorear la posicion de la camara cuando se suelte la tecla de tilt (el tilt ya va a estar desactivado)
					 pendingRestore = true;
				 }
				 else
				 {
					 /* Aviso a la logica de juego */
					 game.getGameLogic().tilt();
				 }			

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
