package gamelogic;

import gamestates.PinballGameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import main.Main;

import com.jmex.audio.AudioSystem;
import com.jmex.audio.AudioTrack;

import components.Bumper;
import components.Door;
import components.Flipper;
import components.Plunger;
import components.Spinner;


public abstract class GameLogic
{
	protected int score;
	
	protected PinballGameState pinball;
	
	/* Sistema de sonido */
	protected AudioSystem audio;
	
	/* Sonidos default */
	private AudioTrack bumpSound, plungerChargeSound, plungerReleaseSound, ballTouchSound, tiltSound, tiltAbuseSound;
	
	/* Sonidos default de flipperMove */
	private List<AudioTrack> flipperMoveSounds;
	
	private static final int flipperMoveSoundQty = 6;
	
	protected Random rand;
	
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

		/* Agrego los sonidos posibles de golpear con flipper */
		flipperMoveSounds = new ArrayList<AudioTrack>();
		
		for (int i = 1; i <= flipperMoveSoundQty; i++)
		{
			flipperMoveSounds.add(audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/flipperMove" + i + ".wav"), false));
		}
		
		/* Inicializo el random */
		rand = new Random();
	}
	
	public void showScore()
	{
		pinball.setScore(score);
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
	
	public void flipperCollision(Flipper flipper)
	{
		ballTouchSound.play();
	}
	
	public void plungerCollision(Plunger plunger)
	{
		ballTouchSound.play();
	}
	
	public void spinnerRampEntranceCollision(Spinner spinner)
	{
		ballTouchSound.play();
	}
	
	public void spinnerRampExitCollision(Spinner spinner)
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
		flipperMoveSounds.get(rand.nextInt(flipperMoveSoundQty)).play();
	}
	
	public void rightFlipperMove(Flipper flipper)
	{
		/* Ejecuto un sonido al azar para el golpe */
		flipperMoveSounds.get(rand.nextInt(flipperMoveSoundQty)).play();
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
	public abstract void lostBall();
	
	// Invocado cuando comienza el juego
	public abstract void gameStart();
	
	// Invocado cuando retorna al juego (o entra por vez primera)
	public abstract void enterGame();
	
	// Invocado cuando se va del juego momentaneamente
	public abstract void leaveGame();
}
