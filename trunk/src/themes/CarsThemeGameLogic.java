package themes;

import com.jmex.physics.StaticPhysicsNode;
import mainloop.Pinball;
import components.Bumper;
import components.Door;
import components.Flipper;
import components.Magnet;
import components.Plunger;
import components.Spinner;

import gamelogic.GameLogic;

public class CarsThemeGameLogic extends GameLogic
{
	private static final int bumperScore = 10, spinnerScore = 5;
	
	private int bumperCollisionCnt = 0;
	
	public CarsThemeGameLogic(Pinball pinball)
	{
		super(pinball);
		
		/* Preparo las pistas de audio que voy a usar */ // TODO falta poner las especificas del tema de autos
		//bumpSound = audio.createAudioTrack(CarsThemeGameLogic.class.getClassLoader().getResource("resources/sounds/car-theme/bump.wav"), false);
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
		
		showMessage("Colision con molinete!");
	}

	@Override
	public void spinnerRampEntranceCollision(Spinner spinner)
	{
		super.spinnerRampEntranceCollision(spinner);
		
		score += spinnerScore;
		
		showMessage("Colision con molinete!");
	}

	@Override
	public void spinnerRampExitCollision(Spinner spinner)
	{
		super.spinnerRampExitCollision(spinner);
		
		score += spinnerScore;
		
		showMessage("Colision con molinete!");
	}

	@Override
	public void tilt()
	{
		super.tilt();
	}
	
	@Override
	public void abuseTilt()
	{
		super.abuseTilt();
		// TODO Agregar logica de control de tilts -> creo q es respecto al abuso de uso de los mismos, el tema es que no se controla desde aca sino desde el input handler.
		// Imprimir en pantalla un cartel que avise el abuso de tilts y desactivar los flippers
	}
}
