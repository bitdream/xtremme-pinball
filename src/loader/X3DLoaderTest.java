package loader;

import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme.bounding.BoundingSphere;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.light.SpotLight;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.pass.BasicPassManager;
import com.jme.renderer.pass.ShadowedRenderPass;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.Text;
import com.jme.scene.shape.Sphere;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.contact.MutableContactInfo;
import com.jmex.physics.impl.ode.OdePhysicsSpace;
import com.jmex.physics.material.Material;
import com.jmex.physics.util.SimplePhysicsGame;

public class X3DLoaderTest extends SimplePhysicsGame
//com.jme.app.SimpleGame
//mainloop.Pinball
{
    /**
     * nodo de la camara: para detectar colisiones con las paredes (no funciona)
     */
    private CameraNode         cn;

    /**
     * nodo dinamico de la bola: guardado para reiniciar la simulacion
     */
    private DynamicPhysicsNode mainBall;

    public static void main( String[] args )
    {
        X3DLoaderTest app = new X3DLoaderTest();
        app.setConfigShowMode( ConfigShowMode.NeverShow );
        app.start();
    }

    private static ShadowedRenderPass sPass     = new ShadowedRenderPass();
    protected BasicPassManager        pManager;
    protected Node                    occluders = new Node( "occluders" );

    @Override
    protected void simpleInitGame()
    {
        /* Little Input Handling */
        Logger.getLogger( Node.class.getName() ).setLevel( Level.SEVERE );
        Logger.getLogger( OdePhysicsSpace.class.getName() ).setLevel( Level.SEVERE );
        
        /* mouse */
        MouseInput.get().setCursorVisible( true );

        /* teclas de reincio */
        KeyBindingManager.getKeyBindingManager().set( "comm_mariano", KeyInput.KEY_M );
        KeyBindingManager.getKeyBindingManager().set( "comm_restart", KeyInput.KEY_Y );

        /* carteles */
        // finally print a key-binding message
        Text infoText = Text
            .createDefaultTextLabel( "teclas", "[m] para resetar la camara, [y] para resetar la pelota" );
        infoText.getLocalTranslation().set( 0, 20, 0 );
        rootNode.attachChild( infoText );

        /* Gravedad */
        // para testear con más fuerza
        getPhysicsSpace().setDirectionalGravity(new Vector3f(0, -9.76f, 0.85f));
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

        X3DLoader loader;
//        long time=System.currentTimeMillis();
        /* cargamos y attacheamos la habitacion */
        try
        {
            loader = new X3DLoader( X3DLoader.class.getClassLoader().getResource("resources/models/Room.x3d") );

            /* agregamos la fisica */
            loader.setPinball( getPhysicsSpace() );

            /* agregamos el lightState */
            loader.setLightState( lightState );

            Spatial scene = loader.loadScene();
            /* cargamos y attacheamos la habitacion */
            rootNode.attachChild( scene );
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        
        /* cargamos y attacheamos la maquina */
        try
        {
            loader = new X3DLoader(  X3DLoader.class.getClassLoader().getResource("resources/models/Machine.x3d" ) );

            /* agregamos la fisica */
            loader.setPinball( getPhysicsSpace() );

            /* agregamos el lightState */
            loader.setLightState( lightState );

            Spatial scene = loader.loadScene();

            //occluders.attachChild( scene );
            /* cargamos y attacheamos la habitacion */
            rootNode.attachChild( scene );
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        
        /* cargamos y attacheamos la mesa */
        try
        {
            loader = new X3DLoader(  X3DLoader.class.getClassLoader().getResource( "resources/models/Table.x3d" ) );

            /* agregamos la fisica */
            loader.setPinball( getPhysicsSpace() );

            /* agregamos el lightState */
            loader.setLightState( lightState );

            Spatial scene = loader.loadScene();

            //occluders.attachChild( scene );
            /* cargamos y attacheamos la habitacion */
            rootNode.attachChild( scene );
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        
//        Thread load = new Thread(new LoadThread( "resources/models/Room.x3d" ));
//        load.start();
//        Thread load1 = new Thread(new LoadThread( "resources/models/Machine.x3d" ));
//        load1.start();
//        Thread load2 = new Thread(new LoadThread( "resources/models/Table.x3d" ));
//        load2.start();
//
//        try {
//            load.join();
//            load1.join();
//            load2.join();
//        }
//        catch(Exception e)
//        {
//            
//        }
//        System.out.println("time is "+(System.currentTimeMillis()-time));
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
        customMaterial.putContactHandlingDetails( Material.RUBBER, contactDetails );
        // agregado del material
        mainBall.setMaterial( customMaterial );

        /* forma */
        // forma, ubicacion y attacheo a la fisica de la pelotita
        final Sphere visualMainBall = new Sphere( "Bola principal", 25, 25, 1 );
        mainBall.setLocalTranslation( new Vector3f(this.ballStartUp ));
        mainBall.attachChild( visualMainBall );

        /* boundingBox */
        // Agregado de bounding volume 
        mainBall.setModelBound( new BoundingSphere() );
        mainBall.updateModelBound();

        /* fisica */
        // generacion de la fisica ( a nivel triangulos )
        mainBall.generatePhysicsGeometry( true );
        // Se computa la masa luego de generar la geometria fisica
        mainBall.computeMass();

        // attacheo al rootnode
        rootNode.attachChild( mainBall );
//        occluders.attachChild( mainBall );
        
        
        /* sombreado */
//      pManager = new BasicPassManager();
//      sPass.add( rootNode );
//      sPass.addOccluder( occluders );
//        rootNode.setRenderQueueMode( Renderer.QUEUE_OPAQUE );
//        sPass.setRenderShadows( true );
//        sPass.setLightingMethod( ShadowedRenderPass.LightingMethod.Additive );
//        pManager.add( sPass );
//
//        RenderPass rPass = new RenderPass();
//        rPass.add( statNode );
//        pManager.add( rPass );
        
        
        
        
        
        
        /* Camara */

        /* ubicacion  y orientacion */
        cam.setLocation( new Vector3f( 0.0f, 102.5f, 0.0f ) ); // 100 son 2mts, y seria lineal
        
        // mejorar esto
        cam.lookAt( new Vector3f( 0.0f, 0.0f, -100.0f ), new Vector3f( 0.0f, 1.0f, -1.0f ) );

        /* aplicar los cambios a la camara */
        cam.update();

        /* nodo camara para poder tratarla como un objeto de la escena */
//        cn = new CameraNode( "camara1", cam );
//        cn.updateFromCamera();
//
//        DynamicPhysicsNode cnphys = getPhysicsSpace().createDynamicNode();
//        cnphys.attachChild( cn );
//
//        cnphys.setModelBound( new BoundingSphere( 2.5f, new Vector3f( 0.0f, 0.0f, 0.0f ) ) );
//        cnphys.updateModelBound();
//        cnphys.generatePhysicsGeometry( true );
//        //cnphys.computeMass();
//
//        cnphys.setAffectedByGravity( false );
//        final SyntheticButton collisionEventHandler = cnphys.getCollisionEventHandler();
//        input.addAction( new InputAction()
//        {
//
//            public void performAction( InputActionEvent evt )
//            {
//
//                System.out.println( " detecto la colision de la camara -------------------" );
//            }
//
//        }, collisionEventHandler, false );
//        /* deteccion de colisiones de la camara */
//        rootNode.attachChild( cnphys );

    }
    
    //private Vector3f ballStartUp =new Vector3f( 1, 50, -65 );
    private Vector3f ballStartUp =new Vector3f( -2, 40, -85.4f );

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
            mainBall.setLocalTranslation( new Vector3f(this.ballStartUp) );
            mainBall.setLocalRotation( new Quaternion() );
            mainBall.updateGeometricState( 0, false );
        }

        /* update del nodo de la camara */
//        cn.updateFromCamera();
//
        /* actualización de las sombras */
//        if ( !pause )
//        {
//            pManager.updatePasses( tpf );
//        }


    }

//    @Override
//    protected void simpleRender()
//    {
//        /** Have the PassManager render. */
//        pManager.renderPasses( display.getRenderer() );
//    }
//    
//    private class LoadThread implements Runnable {
//
//        private String filename;
//        
//        @Override
//        public void run()
//        {
//            try
//            {
//                X3DLoader loader = new X3DLoader( X3DLoader.class.getClassLoader().getResource( this.filename ) );
//    
//                /* agregamos la fisica */
//                loader.setPinball( getPhysicsSpace() );
//    
//                /* agregamos el lightState */
//                loader.setLightState( lightState );
//    
//                Spatial scene = loader.loadScene();
//                
//                synchronized ( rootNode )
//                {
//                    /* cargamos y attacheamos la habitacion */
//                    rootNode.attachChild( scene );    
//                }
//                
//            }
//            catch ( FileNotFoundException e )
//            {
//                e.printStackTrace();
//            }
//        }
//        
//        public LoadThread( String filename )
//        {
//            this.filename = filename;
//        }
//        
//    }
}
