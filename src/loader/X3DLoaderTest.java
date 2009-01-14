package loader;

import com.jme.app.SimpleGame;

import mainloop.Pinball;

public class X3DLoaderTest extends SimpleGame//Pinball
{
    public static void main( String[] args )
    {
        X3DLoaderTest app = new X3DLoaderTest();
        app.setConfigShowMode( ConfigShowMode.NeverShow );
        app.start();
    }

    @Override
    protected void simpleInitGame()
    {
        rootNode.attachChild( X3DLoader.loadScene( "./pinball.x3d", System.getProperty( "user.dir" ) ) );
        //super.simpleInitGame();
        
    }

}
