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
	// Maxima cantidad de bolas que podra haber en la mesa en un determinado momento
	protected static int MAX_BALLS = 3;
	
	protected int score;
	
	protected int lifes = 3;
	
	// Texto a mostrar al usuario como encabezado de los puntos que tiene
	protected String scoreText = "Score: ";
	
	// Texto a mostrar al usuario como encabezado de las bolas (vidas) que le quedan
	protected String ballsText = "Balls: ";	
	
	protected boolean tiltAbused = false;
	
	protected boolean gameFinished = false;
	
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
		
		// Ver si hay que hacer cosas adicionales
		analyzeScore();

	}
	
	// Segun el puntaje actual agrega bolas extra
	private void analyzeScore()
	{
		// TODO Esta logica es temporal!
		if ((score == 20 || score == 25 )&& pinball.getBalls().size() < MAX_BALLS)
		{
			pinball.addBall(pinball.getExtraBallStartUp());
			
			showExtraBallMessage();
			playExtraBallSound();

		}
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
		boolean wasAlreadyRemoved;

		// Quitar a esta bola de la lista que mantiene el pinball 
		wasAlreadyRemoved = ! pinball.getBalls().remove(ball);
		
		if (wasAlreadyRemoved)
		{
			// El sensor ya habia hecho la llamada para esta bola (por multiples colisiones)
			return;
		}
		
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
		gameFinished = true;
		
		// TODO mostrar cartel para presionar N para nuevo juego con la misma mesa. -> si lo presiona, reiniciar toda la logica y activar componentes!
		
		showMessage("Game over");
	}
	
	public void playLostLastBallSound()
	{
		// No habra sonido default
	}
	
	public void playLostBallSound()
	{
		// No habra sonido default
	}
	
	public void playExtraBallSound()
	{
		// TODO poner sonido de extra ball default
	}
	
	// Invocado cuando comienza el juego
	public abstract void gameStart();
	
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
	
	public void showExtraBallMessage()
	{
		showMessage("Extra ball!!!");
	}
}
