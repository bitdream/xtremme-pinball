package components;

import input.FlipperInputHandler;

import com.jme.scene.*;

/**
 * Componente de flipper, para golpear la pelotita.
 */
public class Flipper extends Node
{
	private static final long serialVersionUID = 1L;
    
	/* Tipos de flipper */
	public enum FlipperType {LEFT_FLIPPER, RIGHT_FLIPPER};
	
	/* Tipo de este flipper */
	private FlipperType flipperType;

	/* Modelo grafico del flipper */
	private Spatial model;
	
	/* Controlador del flipper */
	private FlipperInputHandler inputHandler;
	
	/* Indica si esta en su posicion de descanso */
	private boolean restored;
    
    
	/**
	 * Toma un nombre, el tipo de flipper y su representacion grafica.
	 */
	public Flipper(String id, Spatial model, FlipperType flipperType)
	{
		super(id);
		setModel(model);
		this.flipperType = flipperType;
		this.restored = true;
	}

	/**
	 * Fija el modelo espacial del flipper. Remueve antes modelos anteriormente
	 * fijados.
     */
    public void setModel(Spatial model)
    {
    	this.detachChild(this.model);
    	this.model = model;
    	this.attachChild(this.model);
    }
    
    /**
     * Actualizo el estado de su golpe y a su controlador
     */
    public void update()
    {
    	
    }

	public void setInputHandler(FlipperInputHandler inputHandler)
	{
		this.inputHandler = inputHandler;
	}

	public FlipperType getFlipperType()
	{
		return flipperType;
	}

	public boolean isRestored()
	{
		return restored;
	}
}
