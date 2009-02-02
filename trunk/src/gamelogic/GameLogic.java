package gamelogic;

import gamestates.PinballGameState;

import main.Main;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jmex.audio.AudioSystem;
import com.jmex.audio.AudioTrack;
import com.jmex.physics.DynamicPhysicsNode;

import components.Bumper;
import components.Door;
import components.Flipper;
import components.Plunger;
import components.Spinner;


public abstract class GameLogic
{
	protected int score;
	
	protected int lifes = 3;
	
	protected PinballGameState pinball;
	
	/* Sistema de sonido */
	protected AudioSystem audio;
	
	/* Sonidos default */
	private AudioTrack bumpSound, plungerChargeSound, plungerReleaseSound, ballTouchSound, tiltSound, tiltAbuseSound, flipperMoveSound, lostBallSound, lostLastBallSound;
	
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
		
		lostBallSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/car-theme/lost-last-ball.wav"), false);
		lostLastBallSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/car-theme/lost-last-ball.wav"), false);
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
		
		/* TODO Corto la musica (fade out) y recordar recomenzarla */
		
	}
	
	// Invocado cuando se pierde una bola
	public void lostBall(DynamicPhysicsNode ball)
	{
		if (getInTableBallQty() == 1) // Era la ultima bola, debe perser una vida
		{		
			lostLastBallSound.play();
			
			// Bajar la cantidad de vidas y actualizarlas en pantalla
			lifes--;
			showLifes();

			// TODO Con algo aumentar las vidas (ptos)

			// Si aun queda alguna vida, reposicionar la bola
			if (lifes > 0)
			{
				// Reubicar la bola en el plunger
				ball.clearDynamics();
				ball.setLocalTranslation( new Vector3f(Vector3f.ZERO) );
	            ball.setLocalRotation( new Quaternion() );
	            ball.updateGeometricState( 0, false );
			}
			else
			{
				lostGame(ball);
			}
		}
		else // Todavia le quedan bolas en la mesa
		{			
			lostBallSound.play();	
			
			// Quitar a esta bola de la lista que mantiene el pinball y desattachearla del rootNode para que no se siga renderizando
			pinball.getBalls().remove(ball);
			pinball.getRootNode().detachChild(ball);
		}
	}
	
	public void lostGame(DynamicPhysicsNode ball)
	{
		// Quitar a esta bola de la lista que mantiene el pinball y desattachearla del rootNode para que no se siga renderizando
		pinball.getBalls().remove(ball);
		pinball.getRootNode().detachChild(ball);

		// Desactivar los flippers
		for (DynamicPhysicsNode flipper : pinball.getFlippers()) 
		{
			((Flipper)flipper.getChild(0)).setActive(false);
		}
		
		//TODO agregar sonido
		
		showMessage("Juego terminado");
	}
	
	// Invocado cuando comienza el juego
	public abstract void gameStart();
	
	// Invocado cuando retorna al juego (o entra por vez primera)
	public abstract void enterGame();
	
	// Invocado cuando se va del juego momentaneamente
	public abstract void leaveGame();
}
