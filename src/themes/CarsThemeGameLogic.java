package themes;

import main.Main;
import com.jmex.audio.AudioTrack;
import com.jmex.audio.AudioTrack.TrackType;
import com.jmex.physics.DynamicPhysicsNode;
import components.Bumper;
import components.Door;
import components.Flipper;
import components.Plunger;
import components.Spinner;
import gamelogic.GameLogic;
import gamestates.PinballGameState;


public class CarsThemeGameLogic extends GameLogic
{
	private static final int bumperScore = 10, spinnerScore = 5, rampScore = 100;
	
	// Texto a mostrar al usuario como encabezado de los puntos que tiene
	protected String scoreText = "Distance: ";
	
	// Texto a mostrar al usuario como encabezado de las bolas (vidas) que le quedan
	protected String ballsText = "Fuel: ";	
		
	// Contadores
	private int bumperCollisionCnt = 0;	
	private int rampCnt = 0;	
	private int spinnerCollisionCnt = 0;
	
	private AudioTrack rampUpSound, lostBallSound, lostLastBallSound, extraBallSound, gameStartSound, gameOverSound, music;
	
	public CarsThemeGameLogic(PinballGameState pinball)
	{
		super(pinball);
		
		/* Preparo las pistas de audio que voy a usar */
		rampUpSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/car-theme/ramp-up.wav"), false);
		lostBallSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/car-theme/lost-ball.wav"), false);
		lostLastBallSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/car-theme/lost-last-ball.wav"), false);
		gameStartSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/car-theme/start.wav"), false);
		gameOverSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/car-theme/end.wav"), false);
		extraBallSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/car-theme/extra-ball.wav"), false);
		
		/* Inicializo la musica */
		music = Main.getAudioSystem().createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/car-theme/music.wav"), false);
		music.setType(TrackType.MUSIC);
		music.setLooping(true);
		music.setVolume(Main.getMusicVolume());
	}

	@Override
	public void bumperCollision(Bumper bumper)
	{
		super.bumperCollision(bumper);
		
		// Si el bumper esta activo, se hace la suma de puntos y de colisiones contra bumpers
		// El abuso de tilt lo desactiva asi que no se realizara la sumatoria de puntos
		if (bumper.isActive())
		{
			score += bumperScore;
			
			// Se actualiza los datos de pantalla de usuario
			showScore();
			bumperCollisionCnt ++;
			
			// TODO debug
			System.out.println("Bumper cnt: " + bumperCollisionCnt);
			
			// Si colisiono mas de x veces desactivo los imanes. Solo para testeo!
//			if (bumperCollisionCnt > 5)
//			{
//				//bumper.setActive(false);
//				for (StaticPhysicsNode magnet : pinball.getMagnets()) 
//				{
//					Magnet m = (Magnet)magnet.getChild(0);
//					m.setActive(false);
//				}
//			}
		}
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
		
		// No sumar si hay abuso de tilt
		if (!tiltAbused)
		{
			score += spinnerScore;
			// Se actualizan los datos de pantalla de usuario
			showScore();
			spinnerCollisionCnt++;
			// TODO debug
			//System.out.println("Spinner cnt: " + spinnerCollisionCnt);
		}		

	}
	
	public void sensorRampCollision()
	{
		// Sinido de este theme para el pasaje por rampa
		rampUpSound.play();

		// No sumar si hay abuso de tilt
		if (!tiltAbused)
		{
			score += rampScore;
			// Se actualizan los datos de pantalla de usuario
			showScore();
			rampCnt++;
			// TODO debug
			//System.out.println("Rampa cnt: " + rampCnt);
		}
	}

	@Override
	public void tilt()
	{
		super.tilt();
		showMessage("Tilt, don't abuse!!!");
	}
	
	@Override
	public void abuseTilt()
	{
		super.abuseTilt();
		
		// Imprimir en pantalla un cartel que avise el abuso de tilts de este theme
		showMessage("Too much tilt, flippers disabled!!!");
		
		// No contabilizar mas puntos hasta que no se hayan perdido todas las bolas de esta mano, esto se logra desactivando bumpers y puntaje
		// de todos los componentes. Ya esta hecho en cada componente (los no desactivables hacen uso de la variable tiltAbused)
	
	}

	@Override
	public void lostBall(DynamicPhysicsNode ball)
	{
		// Detectar si ya se hizo la llamada para esta bola, en tal caso no estara en la lista
		boolean firstTime;

		// Si es la primera vez que se llama a este metodo por esta bola (colisiones multiples) 
		firstTime = pinball.getBalls().contains(ball);
		
		if (!firstTime)
		{
			// El sensor ya habia hecho la llamada para esta bola (por multiples colisiones) asi que la ignoro
			return;
		}
		
		
		// Muestro el mensaje de este theme
		if (getInTableBallQty() == 1)
		{
			showMessage("Crash, be careful!!!");
			
			// Perdio una bola que baja la vida, resetear contadores de rampa, bumpers, etc
			newBallCntsReset();
		}
		else
		{
			showMessage("Slow down, be careful!!!");
		}	

		super.lostBall(ball);

	}
	
	private void newBallCntsReset()
	{
		rampCnt = 0;
		bumperCollisionCnt = 0;
		spinnerCollisionCnt = 0;
	}
	
	@Override	
	public void playLostLastBallSound()
	{
		lostLastBallSound.play();
	}
	
	@Override	
	public void playLostBallSound()
	{
		lostBallSound.play();
	}

	@Override	
	public void playExtraBallSound()
	{
		extraBallSound.play();
	}
	
	@Override
	public void lostGame(DynamicPhysicsNode ball)
	{
		super.lostGame(ball);
		
		// Mensaje propio de este theme, debe imprimirse luego del default (impreso por super.lostGame())
		showMessage("Race over..."); // TODO agregar press N to new game
		
		gameOverSound.play();
	}

	@Override
	public void gameStart()
	{
		showMessage("Start your engines!!!");
		
		gameStartSound.play();
	}

	@Override
	public void enterGame()
	{
		/* Inicio su musica */
		audio.getMusicQueue().addTrack(music);
		audio.getMusicQueue().setCurrentTrack(music);
		
		if (tiltAbused)
			audio.getMusicQueue().getCurrentTrack().setVolume(0);
	}

	@Override
	public void leaveGame()
	{

	}
	
	@Override	
	public String getScoreText()
	{
		return scoreText;
	}
	
	@Override	
	public String getBallsText()
	{
		return ballsText;
	}
	
	@Override
	public void showExtraBallMessage()
	{
		showMessage("Best lap, extra ball!!!");
	}
}
