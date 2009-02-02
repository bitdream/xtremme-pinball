package loader;

import gamelogic.GameLogic;
import gamestates.PinballGameState;

import java.io.FileNotFoundException;
import java.net.URL;

import com.jme.scene.Spatial;


public class LoaderThread implements Runnable
{

    private URL resource;
    
    private PinballGameState pinballGS;
    
    private X3DLoader loader;
    
    private Spatial scene;
    
    public LoaderThread(URL resource, PinballGameState pinballGS)
    {
        this.resource = resource;
        this.pinballGS = pinballGS;
    }
    
    public void run()
    {
        try
        {
        	/* Creo el loader de este recurso X3D */
            loader = new X3DLoader(resource);

            /* Le fijo su pinball */
            loader.setPinball(pinballGS);

            /* Le fijo su lightState */
            loader.setLightState(pinballGS.getLightState());

            /* Obtengo la escena de ese X3D */
            scene = loader.loadScene();
            
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
    }
    
    public float getPercentComplete()
    {
    	return loader.getPercentComplete();
    }
    
    public GameLogic getTheme()
    {
    	return loader.getTheme(pinballGS);
    }
    
    public Spatial getScene()
    {
        return this.scene;
    }
}
