package input;

import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import components.Flipper;
import components.actions.FlipperRestoreAction;

/**
 * Controla un flipper haciendolo golpear cuando se toca la tecla apropiada y
 * continuamente lo restaura en su lugar original.
 */
public class FlipperInputHandler extends InputHandler
{
	private Flipper flipper;

	//the default action
    private FlipperRestoreAction restore;
    
    public void update(float time)
    {
    	if (!isEnabled())
    		return;

    	super.update(time);

    	//restore.performAction(event);
    	flipper.update();
    }
    
    public FlipperInputHandler(Flipper flipper, String api)
    {
    	this.flipper = flipper;
    	setKeyBindings(api);
        setActions(flipper);
    }

    /**
     * Fija las asociaciones de teclas con triggers
     */
    private void setKeyBindings(String api)
    {
    	KeyBindingManager keyboard = KeyBindingManager.getKeyBindingManager();

    	/* Fijo SHIFT izquierdo o derecho segun el tipo de flipper */
    	if (flipper.getFlipperType().equals(Flipper.FlipperType.LEFT_FLIPPER))
    		keyboard.set("hit", KeyInput.KEY_LSHIFT);
    	else
    		keyboard.set("hit", KeyInput.KEY_RSHIFT);
    }

    /**
     * Asigna acciones a triggers. Las acciones que no se asocian a un trigger, si se llaman
     * desde update con performAction ocurriran siempre.
     */
    private void setActions(Flipper flipper)
    {
        //BrakeAction backward = new BrakeAction(node);
        //addAction(backward, "backward", true);
        
        /* Accion continua, sin trigger de teclado */
        //restoreAction = new FlipperRestoreAction(flipper);
    }
}
