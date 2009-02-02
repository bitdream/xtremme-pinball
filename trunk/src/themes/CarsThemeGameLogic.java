package themes;

import main.Main;
import com.jmex.audio.AudioTrack;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.StaticPhysicsNode;
import components.Bumper;
import components.Door;
import components.Flipper;
import components.Magnet;
import components.Plunger;
import components.Spinner;
import gamelogic.GameLogic;
import gamestates.PinballGameState;

public class CarsThemeGameLogic extends GameLogic
{
	private static final int bumperScore = 10, spinnerScore = 5, rampScore = 100;
	
	private int bumperCollisionCnt = 0;
	
	// Cantidad de veces que paso por la rampa
	private int rampPassedCnt = 0;	
	
	private AudioTrack rampUpSound, music;
	
	public CarsThemeGameLogic(PinballGameState pinball)
	{
		super(pinball);
		
		/* Preparo las pistas de audio que voy a usar */
		rampUpSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/car-theme/ramp-up.wav"), false);
		
		/* Inicializo la musica */
		music = Main.getAudioSystem().createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/car-theme/music.wav"), false);
		music.setLooping(true);
	}

	@Override
	public void bumperCollision(Bumper bumper)
	{
		super.bumperCollision(bumper);
		
		// Si el bumper esta activo, se hace la suma de puntos y de colisiones contra bumpers
		if (bumper.isActive())
		{
			score += bumperScore;
			
			// Se actualiza los datos de pantalla de usuario
			showScore();
			bumperCollisionCnt ++;
			
			// Si colisiono mas de x veces desactivo los imanes. Solo para testeo! TODO
			if (bumperCollisionCnt > 5)
			{
				//bumper.setActive(false);
				for (StaticPhysicsNode magnet : pinball.getMagnets()) 
				{
					Magnet m = (Magnet)magnet.getChild(0);
					m.setActive(false);
				}
			}
		}
		
		//TODO agregando en cada instancia de bumper un cnt de colisiones puedo dar bonus por X colisiones contra el mismo bumper.
	}

	@Override
	public void doorCollision(Door door)
	{
		super.doorCollision(door);
	}

	@Override
	public void flipperCollision(Flipper flipper)
	{
	}

	@Override
	public void plungerCollision(Plunger plunger)
	{
	}

	@Override
	public void spinnerNormalCollision(Spinner spinner)
	{
		super.spinnerNormalCollision(spinner);
		
		score += spinnerScore;
		
		// Se actualizan los datos de pantalla de usuario
		showScore();
	}
	
	public void sensorRampCollision()
	{
		super.sensorRampCollision();

		score += rampScore;
		
		// Se actualizan los datos de pantalla de usuario
		showScore();
		rampPassedCnt++;
		
		showMessage("Paso por rampa N: " + String.valueOf(rampPassedCnt));
		
		//TODO aumentar ptos por pasaje de rampa y armar secuencias ...blablabla
	}

	@Override
	public void tilt()
	{
		super.tilt();
		showMessage("Tilt, no abuse!!");
	}
	
	@Override
	public void abuseTilt()
	{
		super.abuseTilt();
		
		// Imprimir en pantalla un cartel que avise el abuso de tilts 
		showMessage("Tilt, flippers deshabilitados!");
		
		// Desactivar los flippers
		for (DynamicPhysicsNode flipper : pinball.getFlippers()) 
		{
			((Flipper)flipper.getChild(0)).setActive(false);
		}
	}

	@Override
	public void lostBall(DynamicPhysicsNode ball)
	{
		
		// Muestro el mensaje de este theme
		showMessage("Bola perdida");

		super.lostBall(ball);

	}
	
	@Override
	public void lostGame(DynamicPhysicsNode ball)
	{
		super.lostGame(ball);
		
		// Mensaje propio de este theme, debe imprimirse luego del default (impreso por super.lostGame())
		showMessage("Carrera terminada...");
	}

	@Override
	public void gameStart()
	{
		showMessage("Gentlemen, start your engines!!!");		
	}

	@Override
	public void enterGame()
	{
		/* Inicio su musica */
		audio.getMusicQueue().clearTracks();
		audio.getMusicQueue().addTrack(music);
		audio.getMusicQueue().play();
	}

	@Override
	public void leaveGame()
	{
		/* Detengo su musica */
		audio.getMusicQueue().clearTracks();
		
		// TODO hacer fadeout!
	}
}
