package evil;

import main.Main;
import com.jme.math.Vector3f;
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

public class EvilThemeGameLogic extends GameLogic
{
	// Puntajes que otorga cada componente
	private static final int BUMPER_SCORE = 10, SPINNER_SCORE = 15, COMPLETE_SEQ_SCORE = 150;
	
	// Maxima cantidad de bolas que podra haber en la mesa en un determinado momento
	private static final int MAX_BALLS = 3;
	
	// Cantidad de rebotes contra bumpers para activar imanes
	private static final int ACTIVE_MAGNETS_BUMPERS = 35; 
	
	// Multiplicadores para decidir el incremento de vidas y bolas extra que se dan
	private static final int EXTRA_LIFE_STEP = 800;
	private static final int EXTRA_BALL_STEP = 500;
	
	// Contadores
	private int bumperCollisionCnt = 0;	
	private int spinnerCollisionCnt = 0; // No usado
	private int thisBallScore = 0;
	private int extraLifesCnt = 1;
	private int extraBallsCnt = 1;
	
	// Contador de pasos para la secuencia spinner+ -> bumper saltarin+ -> bumper estatico, sin perder vidas
	private int completeSeqCnt = 0;
	
	private boolean magnetsActive = false;
	
	// Sonidos
	private AudioTrack lostBallSound, lostLastBallSound, extraBallSound, gameStartSound, gameOverSound, extraLifeSound, sequenceCompletedSound, magnetOnSound, /*magnetOffSound,*/ music;
	
	public EvilThemeGameLogic(PinballGameState pinball)
	{
		super(pinball);
		
		// Preparo las pistas de audio que voy a usar 
		lostBallSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("evil/sounds/lost-ball.wav"), false); 
		lostLastBallSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("evil/sounds/lost-last-ball.wav"), false);
		gameStartSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("evil/sounds/start.wav"), false);
		gameOverSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("evil/sounds/end.wav"), false);
		extraBallSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("evil/sounds/extra-ball.wav"), false);
		extraLifeSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("evil/sounds/life-up.wav"), false);
		sequenceCompletedSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("evil/sounds/seq-complete.wav"), false);
		magnetOnSound = audio.createAudioTrack(this.getClass().getClassLoader().getResource("evil/sounds/magnet-on.wav"), false);
		
		// Inicializo la musica
		music = Main.getAudioSystem().createAudioTrack(this.getClass().getClassLoader().getResource("evil/sounds/music.wav"), false);
		music.setType(TrackType.MUSIC);
		music.setLooping(true);
		music.setTargetVolume(Main.getMusicVolume());
		
		scoreText = "Score";
		ballsText = "Health";		
		gameOverMessage = "You're dead! Press N to play again.";
		
		// Inicializo la posicion inicial de la bola considerando la rotacion de la mesa
		ballStartUp = new Vector3f( 4.88f, 0.5f, -1.60f ).rotate( pinball.getPinballSettings().getInclinationQuaternion());
	}

	@Override
	public void bumperCollision(Bumper bumper)
	{
		boolean seqCompleteDetected = false;
		super.bumperCollision(bumper);
		
		// Si el bumper esta activo, se hace la suma de puntos y de colisiones contra bumpers
		// El abuso de tilt lo desactiva asi que no se realizara la sumatoria de puntos
		if (bumper.isActive())
		{
			// Ver si forma parte de la secuencia spinner+ -> bumper saltarin+ -> bumper estatico			
			if (bumper.getBumperType().equals(BumperType.JUMPER))
			{
				if (completeSeqCnt == 1 || completeSeqCnt == 2)
				{
					// Siguiente paso de la secuencia
					completeSeqCnt = 2;
					
					// Mensaje al usuario diciendo el proximo paso a seguir
					showMessage("To loose him go to the static bumpers!"); 
				}
				// Sino valia cero y debe seguir asi
			}
			else // Es un bumper estatico
			{
				// Ver si completo la secuencia
				if (completeSeqCnt == 2)
				{
					seqCompleteDetected = true;
					
					// Otorgar el bonus por secuencia completa
					score += COMPLETE_SEQ_SCORE;
					thisBallScore += COMPLETE_SEQ_SCORE;
					
					// Reiniciar la secuencia
					completeSeqCnt = 0;
					
					// Mensaje al usuario avisando del bonus obtenido
					showMessage("You killed the werewolf!"); 
					
					// Sonido de checkpoint
					sequenceCompletedSound.play();
				}
				else if (completeSeqCnt == 1)
				{
					// Reiniciar la secuencia
					completeSeqCnt = 0;
					
					// Mensaje al usuario diciendo que perdio la secuencia
					showMessage("The werewolf lost you, you were lucky!"); 
				}
				// Sino es cero y debe seguir asi

			}
			
			// Actualizo el score global y el hecho con esta bola (en esta mano) si es que ya no lo hice al detectar secuencia completa
			if (!seqCompleteDetected)
			{
				score += BUMPER_SCORE;
				thisBallScore += BUMPER_SCORE;
			}
			
			// Se actualiza los datos de pantalla de usuario
			showScore();
			bumperCollisionCnt ++;
			
			// Cada ACTIVE_MAGNETS_BUMPERS rebotes sin perder vidas, contra bumpers de cualquier tipo, activar los imanes hasta que una bola se pierda
			if ( bumperCollisionCnt == ACTIVE_MAGNETS_BUMPERS && !magnetsActive)
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
				
				// Reinicio el contador
				bumperCollisionCnt = 0;
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
			
			// Si cnt vale 0, ponerlo en 1. Inicia la secuencia
			// Si cnt vale 1, dejarlo en 1 (repite spinners)
			// Si cnt vale 2, reiniciarlo, rompio la seq. (debia ir a bumpers inferiores)
			
			// Ver si forma parte de la secuencia spinner+ -> bumper saltarin+ -> bumper estatico
			if (completeSeqCnt == 0 || completeSeqCnt == 1)
			{
				completeSeqCnt = 1;  //Se realizo el segundo paso de la secuencia
				
				// Mensaje al usuario diciendo el proximo paso a seguir
				showMessage("Werewolf behind you, go to the bumpers!"); 
			}
			else
			{
				completeSeqCnt = 0;
				
				// Mensaje al usuario diciendo que perdio la secuencia
				showMessage("The werewolf lost you, you were lucky!");
			}
		}		
	}
	
	@Override
	public void tilt()
	{
		super.tilt();
		showMessage("Tilt, don't abuse!");
	}
	
	@Override
	public void abuseTilt()
	{
		super.abuseTilt();
		
		// Imprimir en pantalla un cartel que avise el abuso de tilts de este theme
		showMessage("Too much tilt, flippers disabled!");
		
		// No contabilizar mas puntos hasta que no se hayan perdido todas las bolas de esta mano, esto se logra desactivando bumpers y puntaje
		// de todos los componentes. Ya esta hecho en cada componente (los no desactivables hacen uso de la variable tiltAbused)
	
		// Los desactiva super.abuseTilt(), solo resta actualizar la variable
		magnetsActive = false;
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
			// Si con esta bola no sumo ningun punto, regalarle una vida (como si nunca la hubiera perdido)
			if (thisBallScore == 0)
			{
				// Para que no le reste la vida
				lifes ++;
				
				showMessage("You are so afraid, try again..."); 
			}
			else
			{
				showMessage("You are under a spell, lost ball!");
			}
			
			// Perdio una bola que baja la vida, resetear contadores de rampa, bumpers, puntos de bola actual, etc
			newBallCntsReset();			
		}
		else
		{
			showMessage("The werewolves are getting closer!");
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
		if (score >= EXTRA_BALL_STEP * extraBallsCnt)
		{
			/* Primero actualizo el contador de bolas extra y luego me fijo si hay que colocar una en la mesa (si hay menos de MAX_BALLS sobre ella).
			 * Esto lo hago para evitar que se postergue el otorgamiento de la bola extra, se siga sumando puntaje, y cuando el jugador pierda bolas y las
			 * mismas sean menos que MAX_BALLS en la mesa, se le otorguen dos bolas extra casi seguidas (pudiendo colisionar en la rampa que es desde donde
			 * salen).
			 */
			extraBallsCnt++;
			
			if (getInTableBallQty() < MAX_BALLS)
			{
				// Agrego una bola extra a la mesa.
				pinball.addBall(extraBallRotatedPossibleStartUps.get(0));
				
				// Mensaje y sonido al usuario
				showExtraBallMessage();
				playExtraBallSound();
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
		showMessage("Hunters arrived in your help!");
	}
	
	private void playExtraLifeSound()
	{
		extraLifeSound.play();
	}
	
	private void showExtraLifeMessage()
	{
		showMessage("Witch hiding place found, health up!");
	}
	
	private void playActiveMagnetsSound()
	{
		magnetOnSound.play();
	}
	
	private void showActiveMagnetsMessage()
	{
		showMessage("You are lost in a swamp!");
	}
	
	private void showDisabledMagnetsMessage()
	{
		showMessage("You escaped from the swamp!");
	}
	
	// Llamado al perder una vida
	private void newBallCntsReset()
	{
		thisBallScore = 0;
		completeSeqCnt = 0;
		// rampCnt = 0;
		bumperCollisionCnt = 0;
		spinnerCollisionCnt = 0; // no usado
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
		showMessage("Run for your life!");
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
		
		// Desactivar los magnets si es que alguno estaba activo 
		for (StaticPhysicsNode magnet : pinball.getMagnets()) 
		{
			((Magnet)magnet.getChild(0)).setActive(false); 
		}
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
	public String getLifesText()
	{
		return ballsText;
	}
}
