package loader;

import com.jme.app.SimpleGame;

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
        String texDir = System.getProperty( "user.dir" ) + "/resources/textures";
System.out.println(texDir);        
        rootNode.attachChild( X3DLoader.loadScene( "./resources/models/Room.x3d", texDir ) );
        rootNode.attachChild( X3DLoader.loadScene( "./resources/models/Machine.x3d", texDir ) );
        //super.simpleInitGame();
        
    }

}
