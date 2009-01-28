package gamelogic;

import gamestates.PinballGameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import themes.CarsThemeGameLogic;

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
	
	protected AudioSystem audio;
	
	/* Sonidos default */
	private AudioTrack bumpSound, plungerChargeSound, plungerReleaseSound, ballTouchSound, tiltSound;
	
	/* Sonidos default de flipperMove */
	private List<AudioTrack> flipperMoveSounds;
	
	private static final int flipperMoveSoundQty = 6;
	
	protected Random rand;
	
	public GameLogic(PinballGameState pinball)
	{
		this.pinball = pinball;
		
		/* Preparo el sistema de sonido */
		audio = AudioSystem.getSystem();
		
		audio.getEar().trackOrientation(pinball.getCamera());
		audio.getEar().trackPosition(pinball.getCamera());
		
		/* Preparo los sonidos default */
		bumpSound = audio.createAudioTrack(CarsThemeGameLogic.class.getClassLoader().getResource("resources/sounds/bump.wav"), false);
		plungerChargeSound = audio.createAudioTrack(CarsThemeGameLogic.class.getClassLoader().getResource("resources/sounds/plungerCharge.wav"), false);
		plungerReleaseSound = audio.createAudioTrack(CarsThemeGameLogic.class.getClassLoader().getResource("resources/sounds/plungerRelease.wav"), false);
		ballTouchSound = audio.createAudioTrack(CarsThemeGameLogic.class.getClassLoader().getResource("resources/sounds/ballTouch.wav"), false);
		tiltSound = audio.createAudioTrack(CarsThemeGameLogic.class.getClassLoader().getResource("resources/sounds/tilt.wav"), false);

		/* Agrego los sonidos posibles de golpear con flipper */
		flipperMoveSounds = new ArrayList<AudioTrack>();
		
		for (int i = 1; i <= flipperMoveSoundQty; i++)
		{
			flipperMoveSounds.add(audio.createAudioTrack(CarsThemeGameLogic.class.getClassLoader().getResource("resources/sounds/flipperMove" + i + ".wav"), false));
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
		tiltSound.play();
	}
}
