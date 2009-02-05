package loader;

import gamelogic.GameLogic;
import gamestates.PinballGameState;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import com.jme.scene.Spatial;


public class LoaderThread implements Runnable, Observer
{
    private volatile boolean shouldStop = false;
    
    private volatile float percentageComplete = 0f;
    
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

            loader.addObserver( this );
            
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
        catch (LoadingStopException e) {
        }

    }
    
    public float getPercentComplete()
    {
        return this.percentageComplete;    
    }
    
    public GameLogic getTheme()
    {
    	return loader.getTheme(pinballGS);
    }
    
    public Spatial getScene()
    {
        return this.scene;
    }

    public void update( Observable o, Object arg )
    {
        if ( arg instanceof Float )
        {
            Float percentage = (Float) arg;
            this.percentageComplete = percentage;                
        }
        
        if (shouldStop) {
            throw new LoadingStopException();
        }
    }
    
    public void stop()
    {
        this.shouldStop = true;
    }
}
