package themes;

import main.Main;

import com.jme.math.Vector3f;
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
	private static final int bumperScore = 10, spinnerScore = 5;
	
	private int bumperCollisionCnt = 0;
	
	// Cantidad de veces que paso por la rampa
	private int rampPassedCnt = 0;	
	
	private AudioTrack lostBallSound, lostLastBallSound, rampUpSound, music;
	
	public CarsThemeGameLogic(PinballGameState pinball)
	{
		super(pinball);
		
		/* Preparo las pistas de audio que voy a usar */
		lostBallSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/car-theme/lost-last-ball.wav"), false);
		lostLastBallSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/car-theme/lost-last-ball.wav"), false);
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
			// TODO por cada colision entra 4 veces aca, es acorde al tiempo que dure la colision, asi que no es bug
			// pero hay que notar que cada colision "visible" al ojo humano sumara 40 ptos
			score += bumperScore;
			
			// Se actualiza los datos de pantalla de usuario
			showScore();
			showMessage("Colision con bumper!");
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
		// Se actualiza los datos de pantalla de usuario
		showScore();
		showMessage("Colision con molinete!");
	}
	
	public void sensorRampCollision()
	{
		super.sensorRampCollision();
		
		// TODO setear que se paso por la rampa (incrementar un contador o algo)
		rampPassedCnt++;
		showMessage("Paso por rampa N: " + String.valueOf(rampPassedCnt));
	}

//	@Override
//	public void spinnerRampEntranceCollision(Spinner spinner)
//	{
//		super.spinnerRampEntranceCollision(spinner);
//		
//		score += spinnerScore;
//		// Se actualiza los datos de pantalla de usuario
//		showScore();
//		showMessage("Colision con molinete entrando a rampa!");
//		
//		rampUpSound.play();
//	}
//
//	@Override
//	public void spinnerRampExitCollision(Spinner spinner)
//	{
//		super.spinnerRampExitCollision(spinner);
//		
//		score += spinnerScore;
//		// Se actualiza los datos de pantalla de usuario
//		showScore();
//		showMessage("Colision con molinete saliendo de rampa!");
//	}

	@Override
	public void tilt()
	{
		super.tilt();
		showMessage("Tilt! Cuidado, no abuse de su uso!");
	}
	
	@Override
	public void abuseTilt()
	{
		super.abuseTilt();
		
		// Imprimir en pantalla un cartel que avise el abuso de tilts 
		showMessage("Abuso de tilt! Flippers deshabilitados!");
		
		// Desactivar los flippers
		for (DynamicPhysicsNode flipper : pinball.getFlippers()) 
		{
			((Flipper)flipper.getChild(0)).setActive(false);
		}
	}

	@Override
	public void lostBall(DynamicPhysicsNode ball)
	{
		showMessage("Bola perdida");

		if (getInTableBallQty() == 0) // Era la ultima bola
		{
			lostLastBallSound.play();
			
			// Reubicar la bola en el plunger TODO ver posicion, aveces la reposiciona mal!!!
    		ball.clearDynamics();
    		ball.getLocalTranslation().set(new Vector3f(0, 5, -100));
		}
		else // Todavia le quedan bolas en la mesa
		{			
			lostBallSound.play();
			
			// Quitar a esta bola de la lista que mantiene el pinball y desattachearla del rootNode para que no se siga renderizando
			pinball.getBalls().remove(ball);
			pinball.getRootNode().detachChild(ball);			
		}

	}

	@Override
	public void gameStart()
	{
		showMessage("Gana la carrera!!!");		
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
