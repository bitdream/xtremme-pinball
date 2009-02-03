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
	// Puntajes que otorga cada componente
	private static final int BUMPER_SCORE = 10, SPINNER_SCORE = 5, RAMP_SCORE = 50;
	
	// Maxima cantidad de bolas que podra haber en la mesa en un determinado momento
	private static final int MAX_BALLS = 3;
	
	// Multiplicadores para decidir el incremento de vidas y bolas
	private static final int EXTRA_LIFE_STEP = /*1000*/ 500; //TODO ajustar valores
	private static final int EXTRA_BALL_STEP = /*500*/ 200; //TODO ajustar valores
	
	// Texto a mostrar al usuario como encabezado de los puntos que tiene
	protected String scoreText = "Distance: ";
	
	// Texto a mostrar al usuario como encabezado de las bolas (vidas) que le quedan
	protected String ballsText = "Fuel: ";	
		
	// Contadores
	private int bumperCollisionCnt = 0;	
	private int rampCnt = 0;	
	private int spinnerCollisionCnt = 0;
	private int thisBallScore = 0;
	private int extraLifesCnt = 1;
	private int extraBallsCnt = 1;
	

	
	// Sonidos
	private AudioTrack rampUpSound, lostBallSound, lostLastBallSound, extraBallSound, gameStartSound, gameOverSound, music;
	
	public CarsThemeGameLogic(PinballGameState pinball)
	{
		super(pinball);
		
		// Preparo las pistas de audio que voy a usar 
		rampUpSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/car-theme/ramp-up.wav"), false);
		lostBallSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/car-theme/lost-ball.wav"), false);
		lostLastBallSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/car-theme/lost-last-ball.wav"), false);
		gameStartSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/car-theme/start.wav"), false);
		gameOverSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/car-theme/end.wav"), false);
		extraBallSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("resources/sounds/car-theme/extra-ball.wav"), false);
		
		// Inicializo la musica
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
			// Actualizo el score global y el hecho con esta bola (en esta mano)
			score += BUMPER_SCORE;
			thisBallScore += BUMPER_SCORE;
			
			// Se actualiza los datos de pantalla de usuario
			showScore();
			bumperCollisionCnt ++;
			
			// TODO debug
			//System.out.println("Bumper cnt: " + bumperCollisionCnt);
			
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
			score += SPINNER_SCORE;
			thisBallScore += SPINNER_SCORE;
			
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
			score += RAMP_SCORE;
			thisBallScore += RAMP_SCORE;
			
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
			
			// Perdio una bola que baja la vida, resetear contadores de rampa, bumpers, puntos de bola actual, etc
			newBallCntsReset();
		}
		else
		{
			showMessage("Slow down, be careful!!!");
		}	

		super.lostBall(ball);

	}
	
	@Override
	public void showScore()
	{
		super.showScore();
		
		// Ver si hay que hacer cosas adicionales
		analyzeScore();

	}
	
	// Segun el puntaje actual agrega bolas extra, vidas, etc
	private void analyzeScore()
	{
		// Bola extra?
		if ((score >= EXTRA_BALL_STEP * extraBallsCnt) && /*pinball.getBalls().size()*/ getInTableBallQty() < MAX_BALLS)
		{
			// Agrego una bola extra
			pinball.addBall(pinball.getExtraBallStartUp());
			extraBallsCnt++;
			
			// Mensaje y sonido al usuario
			showExtraBallMessage();
			playExtraBallSound();			
		}
		
		// Vida extra?
		if (thisBallScore >= EXTRA_LIFE_STEP * extraLifesCnt)
		{
			extraLifesCnt++;			
			
			// Mensaje y sonido al usuario
			lifes++;
			showLifes();
			showExtraLifeMessage();
			playExtraLifeSound();			
		}
		
	}
	
	private void playExtraBallSound()
	{
		extraBallSound.play();
	}
	
	private void showExtraBallMessage()
	{
		showMessage("Best lap, extra ball!!!");
	}
	
	private void playExtraLifeSound()
	{
		// TODO encontrar sonido
	}
	
	private void showExtraLifeMessage()
	{
		showMessage("Lap record, extra fuel!!!");
	}
	
	// Llamado al perder una vida
	// TODO ver si lo voy a hacer asi
	private void newBallCntsReset()
	{
		thisBallScore = 0;
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
}
