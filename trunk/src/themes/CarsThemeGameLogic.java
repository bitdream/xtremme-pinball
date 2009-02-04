package themes;

import main.Main;
import com.jmex.audio.AudioTrack;
import com.jmex.audio.AudioTrack.TrackType;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.StaticPhysicsNode;
import components.Bumper;
import components.Door;
import components.Flipper;
import components.Magnet;
import components.Plunger;
import components.Spinner;
import components.Bumper.BumperType;
import gamelogic.GameLogic;
import gamestates.PinballGameState;


public class CarsThemeGameLogic extends GameLogic
{
	// Puntajes que otorga cada componente
	private static final int BUMPER_SCORE = 10, SPINNER_SCORE = 5, RAMP_SCORE = 50, COMPLETE_SEQ_SCORE = 300;
	
	// Maxima cantidad de bolas que podra haber en la mesa en un determinado momento
	private static final int MAX_BALLS = 3;
	
	// Multiplicadores para decidir el incremento de vidas y bolas
	private static final int EXTRA_LIFE_STEP = /*1000*/ 500; //TODO ajustar valores para la entrega
	private static final int EXTRA_BALL_STEP = /*500*/ 70; //TODO ajustar valores para la entrega
	
	// Contadores
	private int bumperCollisionCnt = 0;	
	private int rampCnt = 0;	
	private int spinnerCollisionCnt = 0;
	private int thisBallScore = 0;
	private int extraLifesCnt = 1;
	private int extraBallsCnt = 1;
	
	// Contador de pasos para la secuencia bumper saltarin -> spinner -> rampa sin perder vidas
	private int completeSeqCnt = 0;
	
	private boolean magnetsActive = false;
	
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
		music.setTargetVolume(Main.getMusicVolume());
		
		scoreText = "Distance: ";
		ballsText = "Fuel: ";		
		gameOverMessage = "Broke engine, race over... Press N to start a new game.";
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
			
			if (bumper.getBumperType().equals(BumperType.JUMPER))
			{
				// Inicio de secuencia bumper saltarin -> spinner -> rampa
				completeSeqCnt = 1;
				
				// Mensaje al usuario diciendo el proximo paso a seguir
				showMessage("To overtake next car go to the spinners!!!"); 
				// FIXME estos mensajes seran tapados por los de vida extra y bola extra llamados por showScore, 
				// ver si poner ambos al mismo tiempo o como solucionarlo con el HUD
			}
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
			
			// Ver si forma parte de la secuencia bumper saltarin -> spinner -> rampa
			if (completeSeqCnt == 1)
			{
				completeSeqCnt = 2;  //Se realizo el segundo paso de la secuencia
				// Mensaje al usuario diciendo el proximo paso a seguir
				showMessage("Opponent overtook. Get to checkpoint through the ramp!!!");
			}
			else
			{
				if(completeSeqCnt == 2)
				{
					// No obedecio la secuencia (hizo spinner -> spinner), cartel indicador
					showMessage("Next time obey team orders. Sequence aborted!!!");
				}
				
				completeSeqCnt = 0; //Reseteo la secuencia
			}
		}		
	}
	
	public void sensorRampCollision()
	{
		// No sumar si hay abuso de tilt
		if (!tiltAbused)
		{				
			// Sonido de este theme para el pasaje por rampa
			rampUpSound.play();
			
			// Ver si forma parte de la secuencia bumper saltarin -> spinner -> rampa
			if (completeSeqCnt == 2)
			{
				// Otorgar el bonus por secuencia completa
				score += COMPLETE_SEQ_SCORE;
				thisBallScore += COMPLETE_SEQ_SCORE;
				
				// Mensaje al usuario diciendo avisando del bonus obtenido
				showMessage("Checkpoint!!!");
			}
			else
			{
				// Solo sumar los puntos de pasaje por rampa
				score += RAMP_SCORE;
				thisBallScore += RAMP_SCORE;
				
				if(completeSeqCnt == 1)
				{
					// No obedecio la secuencia, cartel indicador
					showMessage("Next time obey team orders. Sequence aborted!!!");
				}
			}
			// Reinicio la secuencia siempre
			completeSeqCnt = 0;  
			
			// Se actualizan los datos de pantalla de usuario
			showScore();
			rampCnt++;
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
			showMessage("Pit stop, lost ball!!!");
			
			// Perdio una bola que baja la vida, resetear contadores de rampa, bumpers, puntos de bola actual, etc
			newBallCntsReset();			
		}
		else
		{
			showMessage("Accident in front of you, slow down!!!");
		}	
		
		if (magnetsActive)
		{
			// Desactivar los magnets si es que alguno estaba activo 
			for (StaticPhysicsNode magnet : pinball.getMagnets()) 
			{
				((Magnet)magnet.getChild(0)).setActive(false);
			}
			showDisabledMagnetsMessage();
			
			magnetsActive = false;
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
		if ((score >= EXTRA_BALL_STEP * extraBallsCnt) && getInTableBallQty() < MAX_BALLS)
		{
			// Agrego una bola extra
			pinball.addBall(pinball.getExtraBallStartUp());
			extraBallsCnt++;
			
			// Mensaje y sonido al usuario
			showExtraBallMessage();
			playExtraBallSound();	
			
			// Cada 3 bolas extra, activar los imanes hasta que una bola se pierda
			if ( (extraBallsCnt - 1) != 0 && (extraBallsCnt - 1) % 3 == 0)
			{
				// Activar los magnets 
				for (StaticPhysicsNode magnet : pinball.getMagnets()) 
				{
					((Magnet)magnet.getChild(0)).setActive(true);
				}
				magnetsActive = true;
				
				// Mensaje y sonido al usuario
				showActiveMagnetsMessage();
				playActiveMagnetsSound();
			}
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
	
	private void playActiveMagnetsSound()
	{
		// TODO encontrar sonido
	}
	
	private void showActiveMagnetsMessage()
	{
		showMessage("Oil in the course, magnets activated!!!");
	}
	
	private void showDisabledMagnetsMessage()
	{
		showMessage("Magnets disabled!!!");
	}
	
	// Llamado al perder una vida
	// TODO Varios contadores no se usan!
	private void newBallCntsReset()
	{
		thisBallScore = 0;
		completeSeqCnt = 0;
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

		// Sonido de juego finalizado
		gameOverSound.play();
	}

	@Override
	public void gameStart()
	{
		super.gameStart();
		
		// Mensaje y sonido de nuevo juego
		showMessage("Gentlemen, start your engines!!!");		
		gameStartSound.play();
		
		// Desactivar los magnets si es que alguno estaba activo 
		for (StaticPhysicsNode magnet : pinball.getMagnets()) 
		{
			((Magnet)magnet.getChild(0)).setActive(false);
		}
		magnetsActive = false;
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
	
	@Override public void restarLogic()
	{
		super.restarLogic();
		
		// Reiniciar contadores intenos y activar los no activados por super.restartLogic()
		
		// Reinicio variables que valen durante 1 vida
		newBallCntsReset();
		
		// Reinicio el resto de las variables
		extraLifesCnt = 1;
		extraBallsCnt = 1;
		
//		// TODO para debug de imanes. Ponerle false despues. Quitarlo todo!!!
//		// Desactivar los magnets si es que alguno estaba activo 
//		for (StaticPhysicsNode magnet : pinball.getMagnets()) 
//		{
//			((Magnet)magnet.getChild(0)).setActive(/*false*/true); 
//		}
//		magnetsActive = false;
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
