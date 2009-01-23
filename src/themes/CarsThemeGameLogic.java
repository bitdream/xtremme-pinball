package themes;

import mainloop.Pinball;
import components.Bumper;
import components.Door;
import components.Flipper;
import components.Plunger;

import gamelogic.GameLogic;

public class CarsThemeGameLogic extends GameLogic
{
	private int bumperCollisionCnt = 0;

	public CarsThemeGameLogic(Pinball pinball)
	{
		super(pinball);
	}

	@Override
	public void bumperCollision(Bumper bumper)
	{
		// Si el bumper esta activo, se hace la suma de puntos y de colisiones contra bumpers
		if (bumper.isActive())
		{
			// TODO por cada colision entra 4 veces aca, es acorde al tiempo que dure la colision, asi que no es bug
			// pero hay que notar que cada colision "visible" al ojo humano sumara 40 ptos
			score+=10;
			
			// Se actualiza los datos de pantalla de usuario
			showScore();
			showMessage("Colision con bumper!");
			bumperCollisionCnt ++;
			
			// Si colisiono mas de x veces lo desactivo. Solo para testeo! TODO
//			if (bumperCollisionCnt > 0)
//			{
//				bumper.setActive(false);
//			}
		}
		
		//TODO agregando en cada instancia de bumper un cnt de colisiones puedo dar bonus por X colisiones contra el mismo bumper.
	}

	@Override
	public void doorCollision(Door door)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void flipperCollision(Flipper flipper)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void plungerCollision(Plunger plunger)
	{
		// TODO Auto-generated method stub

	}

}
