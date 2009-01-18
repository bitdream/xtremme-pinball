package loader;

import java.util.List;

import com.jme.bounding.BoundingSphere;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.light.SpotLight;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.Text;
import com.jme.scene.shape.Sphere;
import com.jme.system.DisplaySystem;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.contact.MutableContactInfo;
import com.jmex.physics.material.Material;
import com.jmex.physics.util.SimplePhysicsGame;

public class X3DLoaderTest extends
SimplePhysicsGame
//com.jme.app.SimpleGame
//mainloop.Pinball
{
    /**
     * nodo de la camara: para detectar colisiones con las paredes (no funciona)
     */
    private CameraNode cn;
    
    /** 
     * nodo dinamico de la bola: guardado para reiniciar la simulacion 
     */
    private DynamicPhysicsNode  mainBall;
    
    
    public static void main( String[] args )
    {
        X3DLoaderTest app = new X3DLoaderTest();
        app.setConfigShowMode( ConfigShowMode.NeverShow );
        app.start();
    }

    @Override
    protected void simpleInitGame()
    {
        /* Little Input Handling */
        
        /* mouse */
        MouseInput.get().setCursorVisible( false );
        
        /* teclas de reincio */
        KeyBindingManager.getKeyBindingManager().set( "comm_mariano", KeyInput.KEY_M );
        KeyBindingManager.getKeyBindingManager().set( "comm_restart", KeyInput.KEY_Y );
        
        /* carteles */
        // finally print a key-binding message
        Text infoText = Text.createDefaultTextLabel( "teclas", "[m] para resetar la camara, [y] para resetar la pelota" );
        infoText.getLocalTranslation().set( 0, 20, 0 );
        rootNode.attachChild( infoText );
        
        
        /* Gravedad */
        // para testear con más fuerza
        //getPhysicsSpace().setDirectionalGravity(new Vector3f(1, -9.80f, 1));

        /* Iluminacion */
        // test de spot, no se aplica
        SpotLight sl = new SpotLight();
        sl.setDiffuse( new ColorRGBA( 1.0f, 0.0f, 0.0f, 1.0f ) );
        sl.setAmbient( new ColorRGBA( 0.75f, 0.0f, 0.0f, 1.0f ) );
        sl.setDirection( new Vector3f( 0, -0.857f, -0.514f ) );
        sl.setLocation( new Vector3f( 0, 100, -10 ) );
        sl.setAngle( 45 );
        //sl.setExponent( 0.25f ); // omitido porque en x3d no esta
        sl.setEnabled( true );
        sl.setSpecular( new ColorRGBA( 1.0f, 0.0f, 0.0f, 1.0f ) );

        /* borramos todas las luces default */
        lightState.detachAll();
        //lightState.attach( sl ); //comentar para sacar spot
        
        
        /* X3D */
        /* directorio de las texturas, deberia salir de un config file tal vez */
        String texDir = System.getProperty( "user.dir" ) + "/resources/textures";

        /* cargamos y attacheamos la habitacion */
        rootNode.attachChild( X3DLoader.loadScene( "./resources/models/Room.x3d", texDir, lightState, null ) );

        /* cargamos y attacheamos la mesa */
        rootNode.attachChild( X3DLoader.loadScene( "./resources/models/Machine2.x3d", texDir, lightState, getPhysicsSpace() ) );
        
        //super.simpleInitGame(); //descomentar si se extiende de pinball 
        
        /* Bola */
        mainBall = getPhysicsSpace().createDynamicNode();
        
        /* material */
        // Defino un materia personalizado para poder setear las propiedades de interaccion con la mesa de plastico
        final Material customMaterial = new Material( "material de bola" );
        // Es pesado
        customMaterial.setDensity( 100.0f );
        // Detalles de contacto con el otro material
        MutableContactInfo contactDetails = new MutableContactInfo();
        // Poco rebote
        contactDetails.setBounce( 0.5f );
        // Poco rozamiento
        contactDetails.setMu( 0.50f );
        customMaterial.putContactHandlingDetails( Material.RUBBER, contactDetails ); //TODO definir el material. Antes: Material.PLASTIC
        // agregado del material
        mainBall.setMaterial(customMaterial);
        
        /* forma */
        // forma, ubicacion y attacheo a la fisica de la pelotita
        final Sphere visualMainBall = new Sphere("Bola principal", 25, 25, 1);
        mainBall.setLocalTranslation(new Vector3f(1, 100, -65));
        mainBall.attachChild(visualMainBall);
        
        /* boundingBox */
        // Agregado de bounding volume 
        mainBall.setModelBound(new BoundingSphere());
        mainBall.updateModelBound();

        /* fisica */
        // generacion de la fisica ( a nivel triangulos )
        mainBall.generatePhysicsGeometry(true);
        // Se computa la masa luego de generar la geometria fisica
        mainBall.computeMass();
        
        // attacheo al rootnode
        rootNode.attachChild(mainBall);
        
        /* Camara */
        
        /* ubicacion  y orientacion */
        cam.setLocation( new Vector3f( 0.0f, 90.0f, 0.0f ) ); // 100 son 2mts, y seria lineal
        cam.lookAt( new Vector3f( 0.0f, 0.0f, -100.0f ), new Vector3f( 0.0f, 1.0f, -1.0f ) );

        /* aplicar los cambios a la camara */
        cam.update();

        /* nodo camara para poder tratarla como un objeto de la escena */
        cn = new CameraNode( "camara1", cam );

        /* deteccion de colisiones de la camara */
//        cn.setModelBound( new BoundingSphere( 2.5f, new Vector3f( 0.0f, 0.0f, 0.0f ) ) );
//        cn.updateModelBound();
//        cn.updateFromCamera();
//        rootNode.attachChild( cn );
        
    }

    @Override
    protected void simpleUpdate()
    {
        /* reset de camara */
        if ( KeyBindingManager.getKeyBindingManager().isValidCommand( "comm_mariano", false ) )
        {
            cam.setLocation( new Vector3f( 0.0f, 90.0f, 0.0f ) );

            cam.lookAt( new Vector3f( 0.0f, 0.0f, -100.0f ), new Vector3f( 0.0f, 1.0f, -1.0f ) );
        }
        
        /* reset de pelota */
        if ( KeyBindingManager.getKeyBindingManager().isValidCommand( "comm_restart", false ) )
        {               
            mainBall.clearDynamics();
            mainBall.setLocalTranslation(new Vector3f(1, 100, -65));
            mainBall.setLocalRotation(new Quaternion());
            mainBall.updateGeometricState(0, false);
        }

        /* update del nodo de la camara */
        cn.updateFromCamera();

        /* deteccion de colisiones de camara (intento) */
//        for ( Spatial sp : rootNode.getChildren() )
//        {
//            if ( sp instanceof Node )
//            {
//                Node node = (Node) sp;
//                List<Spatial> childs = node.getChildren();
//                if ( childs != null )
//                {
//                    for ( Spatial spatial : childs )
//                    {
//                        if ( cn.hasCollision( spatial, true ) ) System.out.println( spatial );
//                    }
//                }
//            }
//        }

    }
}
