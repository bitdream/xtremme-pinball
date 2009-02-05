package gamelogic;

import gamestates.PinballGameState;
import main.Main;
import com.jmex.audio.AudioSystem;
import com.jmex.audio.AudioTrack;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.StaticPhysicsNode;

import components.Bumper;
import components.Door;
import components.Flipper;
import components.Magnet;
import components.Plunger;
import components.Spinner;


public abstract class GameLogic
{
	protected static int INITIAL_LIFES = 3;
	protected int lifes = INITIAL_LIFES;
	protected int score;
	
	// Texto a mostrar al usuario como encabezado de los puntos que tiene
	protected String scoreText = "Score: ";
	
	// Texto a mostrar al usuario como encabezado de las bolas (vidas) que le quedan
	protected String ballsText = "Balls: ";	
	
	// Texto a mostrar al usuario cuando se termina el juego
	protected String gameOverMessage = "Game over. Press N to start a new game.";
	
	protected boolean tiltAbused = false;
	
	protected PinballGameState pinball;
	
	/* Sistema de sonido */
	protected AudioSystem audio;
	
	/* Sonidos default */
	private AudioTrack bumpSound, plungerChargeSound, plungerReleaseSound, ballTouchSound, tiltSound, tiltAbuseSound, flipperMoveSound;
	
	public GameLogic(PinballGameState pinball)
	{
		this.pinball = pinball;
		
		audio = Main.getAudioSystem();
		
		/* Preparo los sonidos default */
		bumpSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/bump.wav"), false);
		plungerChargeSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/plungerCharge.wav"), false);
		plungerReleaseSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/plungerRelease.wav"), false);
		ballTouchSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/ballTouch.wav"), false);
		tiltSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/tilt.wav"), false);
		tiltAbuseSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/tilt-abuse.wav"), false);
		flipperMoveSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/flipperMove.wav"), false);
	}
	
	public void showScore()
	{
		pinball.setScore(score);

	}
	
	public void showLifes()
	{
		pinball.setLifes(lifes);
	}
	
	public int getLifes() 
	{
		return lifes;
	}

	public void showMessage(String message)
	{
		pinball.setMessage(message);
	}
	
	public int getInTableBallQty()
	{
		return pinball.getBalls().size();
	}
	
	/* La idea es que los siguientes metodos sean overrideados en caso de necesitarse y
	 * que llamen a super.metodo(objeto)
	 */
	
	public void bumperCollision(Bumper bumper)
	{
		if (bumper.isActive())
		{
			bumpSound.play();
		}
	}
	
	public void doorCollision(Door door)
	{
		ballTouchSound.play();
	}
	
	public abstract void flipperCollision(Flipper flipper);

	
	public abstract void plungerCollision(Plunger plunger);

	
	public void sensorRampCollision()
	{
		ballTouchSound.play();
	}
	
	public void spinnerNormalCollision(Spinner spinner)
	{
		ballTouchSound.play();
	}
	
	public void plungerCharge(Plunger plunger)
	{
		plungerChargeSound.play();
	}
	
	public void plungerRelease(Plunger plunger)
	{
		plungerReleaseSound.play();
	}
	
	public void leftFlipperMove(Flipper flipper)
	{
		/* Ejecuto un sonido al azar para el golpe */
		flipperMoveSound.play();
	}
	
	public void rightFlipperMove(Flipper flipper)
	{
		/* Ejecuto un sonido al azar para el golpe */
		flipperMoveSound.play();
	}
	
	// Invocado cuando el usuario hace tilt
	public void tilt()
	{
		tiltSound.play();
	}
	
	// Invocado cuando el usuario hizo abuso del uso de tilt
	public void abuseTilt()
	{
		tiltAbuseSound.play();
		
		/* Corto la musica actual (pero sigo en ejecucion por si abren el menu en el medio */
		audio.getMusicQueue().getCurrentTrack().fadeOut(1.5f);

		// Seteo la variable de aviso
		tiltAbused = true;
		
		// Desactivar los flippers
		for (DynamicPhysicsNode flipper : pinball.getFlippers()) 
		{
			((Flipper)flipper.getChild(0)).setActive(false);
		}
		// Desactivar bumpers
		for (DynamicPhysicsNode bumper : pinball.getJumperBumpers()) 
		{
			((Bumper)bumper.getChild(0)).setActive(false);
		}
		for (StaticPhysicsNode bumper : pinball.getNoJumperBumpers()) 
		{
			((Bumper)bumper.getChild(0)).setActive(false);
		}		
		// TODO descomentar
		// Desactivar los magnets
		for (StaticPhysicsNode magnet : pinball.getMagnets()) 
		{
			((Magnet)magnet.getChild(0)).setActive(false);
		}
		// Desactivar tilt
		pinball.getPinballInputHandler().setTiltActive(false);
		
	}
	
	// Invocado cuando se pierde una bola
	public void lostBall(DynamicPhysicsNode ball)
	{
		// Detectar si ya se hizo la llamada para esta bola, en tal caso no estara en la lista
//		boolean wasAlreadyRemoved;

		// Quitar a esta bola de la lista que mantiene el pinball 
		/*wasAlreadyRemoved = ! */pinball.getBalls().remove(ball);
		
		// No lo hago pq lo hago desde la logica del theme
/*		if (wasAlreadyRemoved)
		{
			// El sensor ya habia hecho la llamada para esta bola (por multiples colisiones)
			return;
		}
*/		
		// Desattachearla del rootNode para que no se siga renderizando
		pinball.getRootNode().detachChild(ball);
		
		if (getInTableBallQty() == 0) // Era la ultima bola, debe perder una vida
		{		
			// Bajar la cantidad de vidas y actualizarlas en pantalla
			lifes--;
			
			// Actualizar el cartel con las vidas y mostrarlo al usuario
			showLifes();
			
			// Si aun queda alguna vida, reponer la bola en el plunger
			if (lifes > 0)
			{
				// Para que se pueda overridear desde el theme
				playLostLastBallSound();
				
				// Nueva bola desde la posicion del plunger
				pinball.addBall(pinball.getBallStartUp());
				
				// Si se perdieron las bolas por estar deshabilitados los flippers debido a abuso de tilt, rehabilitarlos
				if (tiltAbused)
				{
					// Habilitar flippers, bumpers y poner en false el boolean para rehabilitar puntos y tilt
					for (DynamicPhysicsNode flipper : pinball.getFlippers()) 
					{
						((Flipper)flipper.getChild(0)).setActive(true);
					}
					for (DynamicPhysicsNode bumper : pinball.getJumperBumpers()) 
					{
						((Bumper)bumper.getChild(0)).setActive(true);
					}
					for (StaticPhysicsNode bumper : pinball.getNoJumperBumpers()) 
					{
						((Bumper)bumper.getChild(0)).setActive(true);
					}	
					
					pinball.getPinballInputHandler().setTiltActive(true);
					
					// Reinicio la variable
					tiltAbused = false;

					audio.getMusicQueue().getCurrentTrack().fadeIn(1, 1);
				}
			}
			else
			{
				lostGame(ball);
			}
		}
		else // Todavia le quedan bolas en la mesa
		{			
			playLostBallSound();
		}
	}
	
	public void lostGame(DynamicPhysicsNode ball)
	{
		// Desactivar los flippers
		for (DynamicPhysicsNode flipper : pinball.getFlippers()) 
		{
			((Flipper)flipper.getChild(0)).setActive(false);
		}
		
		// Desactivar el tilt
		pinball.getPinballInputHandler().setTiltActive(false);
		
		showMessage(gameOverMessage);
		
		// Para recibir si presiona N y hay que reiniciar el juego
		pinball.getPinballInputHandler().setNewGameActive(true);
		
	}
	
	public void playLostLastBallSound()
	{
		// No habra sonido default
	}
	
	public void playLostBallSound()
	{
		// No habra sonido default
	}
	
	// Invocado cuando comienza el juego
	public void gameStart()
	{
		// Vaciar la lista de bolas. Deberia estarlo, pero lo hago por las dudas
		pinball.getBalls().clear();
		
		// Crear la bola inicial
		pinball.addBall(pinball.getBallStartUp());
		
		// Activar los componentes y reiniciar la logica del juego
		restarLogic();
		
		// Actualizo en pantalla el puntaje del nuevo juego (0) y las vidas
		showScore();
		showLifes();
	}
	
	public void restarLogic()
	{
		// Reiniciar vidas, puntos, tiltAbused y lo comun a todos los juegos
		// El theme debera overridearlo y reiniciar sus contadores y logica propia
		lifes = INITIAL_LIFES;
		score = 0;
		
		// Habilitar flippers, bumpers y poner en false el boolean para rehabilitar puntos y tilt
		for (DynamicPhysicsNode flipper : pinball.getFlippers()) 
		{
			((Flipper)flipper.getChild(0)).setActive(true);
		}
		for (DynamicPhysicsNode bumper : pinball.getJumperBumpers()) 
		{
			((Bumper)bumper.getChild(0)).setActive(true);
		}
		for (StaticPhysicsNode bumper : pinball.getNoJumperBumpers()) 
		{
			((Bumper)bumper.getChild(0)).setActive(true);
		}	
		
		pinball.getPinballInputHandler().setTiltActive(true);
		
		// Desactivar la tecla N de nuevo juego
		pinball.getPinballInputHandler().setNewGameActive(false);
		
		// Reinicio la variable
		tiltAbused = false;
	}
	
	// Invocado cuando retorna al juego (o entra por vez primera)
	public abstract void enterGame();
	
	// Invocado cuando se va del juego momentaneamente
	public abstract void leaveGame();
	
	
	public String getScoreText()
	{
		return scoreText;
	}
	
	public String getBallsText()
	{
		return ballsText;
	}
}
