package components;

import gamestates.PinballGameState;

import java.awt.Font;
import java.util.concurrent.Callable;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.util.GameTaskQueueManager;
import com.jmex.font3d.Font3D;
import com.jmex.font3d.Text3D;


public class LCDScreen extends Node
{
    private static final long serialVersionUID = 1L;
    
    private Geometry background;
    
    private Text3D text3d;
    
    private String currentText;
    
    
    public LCDScreen(PinballGameState pinball, String name, Geometry geom)
    {
        super(name);
        
        background = geom;

        /* Fijo el fondo */
        attachChild(background);
        
        /* Lo fijo al pinball como la unica pantalla */
        pinball.setLCDScreen(this);

        /* Texto inicial nulo */
        setText("", 24, ColorRGBA.black);
    }
    
    public void setText(final String text, final int size, final ColorRGBA color)
    {
    	/* Fijo el texto actual */
    	currentText = text;
    	
    	/* Le saco el texto anterior */
    	detachChild(text3d);
    	
    	/* Creo la tarea de armar la fuente */
        try
        {
            GameTaskQueueManager.getManager().update(new Callable<Void>() {
    
                public Void call() throws Exception {

                    Font3D font = new Font3D(new Font("Helvetica", Font.PLAIN, size), 0.001f, true, true, true);
                    
                    text3d = font.createText(text, size, 0);
                    
                    return null;
                }
            }).get();
        }
        catch (Exception e)
        {
        }
        
        /* Lo aplasto en Z */
        text3d.setLocalScale(new Vector3f(1.0f, 1.0f, 0.01f));
        
        /* Le fijo el color */
        text3d.setFontColor(color);
        
        background.updateWorldVectors();
        text3d.setLocalTranslation(background.getWorldTranslation());
        attachChild(text3d);
    }

	public String getCurrentText()
	{
		return currentText;
	}
}
