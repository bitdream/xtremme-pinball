package components;

import java.awt.Font;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

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
    
    private Geometry fondo;
    
    private Text3D texto;
    
    
    //proof of concept... al menos hay que apagar los logs
    public LCDScreen(String name, Geometry geom)
    {
        super(name);
        
        this.fondo = geom;

        this.attachChild( fondo );
        
        try 
        {
            GameTaskQueueManager.getManager().update(new Callable<Void>() {
    
                public Void call() throws Exception {
                    // por motivos ratas la fuente la debe generar el thread de gl
                    
                    //esta linea tiene un tema importante en jme.... tiene un thread en el foro muy
                    // reciente que dice que tiene problemas.. y lo veran en el syserr
                    Font3D font = new Font3D(new Font("Arial", Font.PLAIN, 24), 0.001f, true, true, true);
                    
                    
                    texto = font.createText("Testing 1, 2, 3", 50.0f, 0);
                    return null;
                }
            }).get();
        }
        catch (ExecutionException e)
        {
        
        }
        catch (InterruptedException e)
        {
            
        }
        
        texto.setLocalScale(new Vector3f(5.0f, 5.0f, 0.01f));
        
        texto.setFontColor( ColorRGBA.red );
        
        this.fondo.updateWorldVectors();
        texto.setLocalTranslation( fondo.getWorldTranslation() );
        this.attachChild( this.texto );
    }

}
