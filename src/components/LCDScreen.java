package components;

import gamestates.PinballGameState;

import java.awt.Font;
import java.util.concurrent.Callable;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.state.RenderState;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;
import com.jmex.font3d.Font3D;
import com.jmex.font3d.Text3D;


public class LCDScreen extends Node
{
    private static final long serialVersionUID = 1L;
    
    private Geometry background;
    
    private Text3D text3d;
    
    private String currentText;
    
    private Font3D font;
    
    
    public LCDScreen(PinballGameState pinball, String name, Geometry geom)
    {
        super(name);
        
        /* Apago el logger */
        java.util.logging.Logger.getLogger(com.jmex.font3d.math.Triangulator.class.getName()).setLevel(java.util.logging.Level.OFF);

        background = geom;

        /* Fijo el fondo */
        attachChild(background);
        
        /* Lo fijo al pinball como la unica pantalla */
        pinball.setLCDScreen(this);
        
    	/* Creo la tarea de armar la fuente */
        try
        {
            GameTaskQueueManager.getManager().update(new Callable<Void>() {
    
                public Void call() throws Exception {

                    font = new Font3D(new Font("Helvetica", Font.PLAIN, 24), 0.001f, true, true, true);
                    
                    return null;
                }
            }).get();
        }
        catch (Exception e)
        {
        }
    }
    
    public void setText(final String text, final int size, final ColorRGBA color)
    {
    	/* Fijo el texto actual */
    	currentText = text;
    	
    	/* Le saco el texto anterior */
    	detachChild(text3d);
        
        /* Creo el texto */
        text3d = font.createText(text, size, 0);
        
        /* Lo aplasto en Z */
        text3d.setLocalScale(new Vector3f(1.0f, 1.0f, 0.01f));
        
        /* Le fijo el color */
        text3d.setFontColor(color);

        /* Ubico al texto en el fondo */
        centerText();
        
        /* Le agrego el nuevo texto */
        attachChild(text3d);
        
        /* Y lo actualizo */
        text3d.updateRenderState();
    }
    
    private void centerText()
    {
    	background.updateWorldVectors();
    	
    	Vector3f bckgCenter = background.getWorldTranslation();
    	
    	text3d.setLocalTranslation(new Vector3f(bckgCenter.getX() - background.getLocalScale().getX() + 0.2f, bckgCenter.getY() - 0.3f * background.getLocalScale().getY(), bckgCenter.getZ()));
    }

	public String getCurrentText()
	{
		return currentText;
	}
	
	public void draw(Renderer r)
	{
	    RenderState rs = this.setRenderState( DisplaySystem.getDisplaySystem().getRenderer()
            .createTextureState() );
	    this.updateRenderState();
	    this.text3d.onDraw( r );
	    this.setRenderState( rs );
	    this.updateRenderState();
	    this.background.onDraw( r );
	}
}
