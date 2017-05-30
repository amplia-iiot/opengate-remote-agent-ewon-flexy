/*
 * Copyright © 2015, Oracle and/or its affiliates.  All rights reserved. 
 *
 * This software is dual-licensed to you under the MIT License (MIT) and the
 * Universal Permissive License (UPL).  See the LICENSE file in the root directory
 * for license terms.  You may choose either license, or both.
 */
package com.oracle.jmee.samples.webserverdemo;

// import com.oracle.jmee.samples.webserver.FileSystemRequestHandler;
import com.oracle.jmee.samples.webserver.ResourceRequestHandler;
import com.oracle.jmee.samples.webserver.WebServer;
import java.io.IOException;
import javax.microedition.midlet.MIDlet;


/**
 * Demo version which has been optimized for platforms with strict memory
 * limitations. Please see comments for class of original demo and accompanying
 * readme file for more information.
 */
public class LightWebServerApplication /* extends MIDlet */ 
{

    // Names and default values of used properties
    private static final String FILE_SYSTEM_HANDLER_ROOT_PROPERTY_NAME = "WebServerApplication-FileSystemRequestHandler-Root";
    private static final String FILE_SYSTEM_HANDLER_ROOT_DEFAULT_VALUE = System.getProperty("user.dir");

    private static final String LISTENING_PORT_PROPERTY_NAME = "WebServerApplication-ListeningPort";
    private static final int LISTENING_PORT_DEFAULT_VALUE = 8095;

    private static final String PRINT_DIRECTORY_CONTENTS_PROPERTY_NAME = "WebServerApplication-FileSystemRequestHandler-PrintDirectoryContents";
    private static final boolean PRINT_DIRECTORY_CONTENTS_DEFAULT_VALUE = true;

    private static final String FILE_SYSTEM_HANDLER_WELCOME_PAGE_PROPERTY_NAME = "WebServerApplication-FileSystemRequestHandler-WelcomePage";
    private static final String FILE_SYSTEM_HANDLER_WELCOME_PAGE_DEFAULT_VALUE = null;

    private static final String RESOURCE_HANDLER_ROOT_PROPERTY_NAME = "WebServerApplication-JarResourceRequestHandler-Root";
    private static final String RESOURCE_HANDLER_ROOT_DEFAULT_VALUE = "/resources";

    // Web server instance
    private WebServer webServer;
    
    public WebServer getWebServer()
    {
        return webServer;
    }
    
    public String getAppProperty(String _propertyName)
    {
        if(_propertyName.equals(FILE_SYSTEM_HANDLER_ROOT_PROPERTY_NAME))
        {
            return FILE_SYSTEM_HANDLER_ROOT_DEFAULT_VALUE;
        } else if(_propertyName.equals(LISTENING_PORT_PROPERTY_NAME))
        {
            return "" + LISTENING_PORT_DEFAULT_VALUE;        
        } else if(_propertyName.equals(PRINT_DIRECTORY_CONTENTS_PROPERTY_NAME))
        {
            return "" + PRINT_DIRECTORY_CONTENTS_DEFAULT_VALUE;        
        } else if(_propertyName.equals(FILE_SYSTEM_HANDLER_WELCOME_PAGE_PROPERTY_NAME))
        {
            return FILE_SYSTEM_HANDLER_WELCOME_PAGE_DEFAULT_VALUE;        
        } else if(_propertyName.equals(RESOURCE_HANDLER_ROOT_PROPERTY_NAME))
        {
            return RESOURCE_HANDLER_ROOT_DEFAULT_VALUE;        
        }
        return null;
    }

    // @Override
    public void startApp() {
        System.out.println("*********************************");
        System.out.println("* OGAgent Web Server Starting... *");
        System.out.println("*********************************");

        // Obtaining port to listen for incoming connections
        int listeningPort = getIntFromProperty(LISTENING_PORT_PROPERTY_NAME, LISTENING_PORT_DEFAULT_VALUE);
        if (listeningPort < 0 || listeningPort > 65535) {
            System.out.println("Listening port must be in [0,65535] range. The specified value is: " + listeningPort);
            destroyApp(true);
            return;
        }
        
        // Creating a web server instance
        webServer = new WebServer(listeningPort);

        // Persisting connections is disabled due to memory limitations
        webServer.setPersistConnections(false);

        // Setting buffer size
        webServer.setTransferBufferSize(1024);

        // Initializing and adding handler which allows to access the files from
        // the filesystem. Getting values of configuration options from JAD
        String fileSystemHandlerRoot = getAppProperty(FILE_SYSTEM_HANDLER_ROOT_PROPERTY_NAME);
        if (fileSystemHandlerRoot == null) {
            fileSystemHandlerRoot = FILE_SYSTEM_HANDLER_ROOT_DEFAULT_VALUE;
        }

        boolean listDirectoryContents = getBooleanFromProperty(PRINT_DIRECTORY_CONTENTS_PROPERTY_NAME, PRINT_DIRECTORY_CONTENTS_DEFAULT_VALUE);

        String welcomPagePath = getAppProperty(FILE_SYSTEM_HANDLER_WELCOME_PAGE_PROPERTY_NAME);
        if (welcomPagePath == null) {
            welcomPagePath = FILE_SYSTEM_HANDLER_WELCOME_PAGE_DEFAULT_VALUE;
        }
                
        /*
        FileSystemRequestHandler fileSystemRequestHandler = new FileSystemRequestHandler(fileSystemHandlerRoot, listDirectoryContents);
        fileSystemRequestHandler.setWelcomePagePath(welcomPagePath);
        webServer.addRequestHandler("/fs", fileSystemRequestHandler);
        */

        // Initializing and adding handler which allows to access files from JAR
        String resourceHandlerRoot = getAppProperty(RESOURCE_HANDLER_ROOT_PROPERTY_NAME);
        if (resourceHandlerRoot == null) {
            resourceHandlerRoot = RESOURCE_HANDLER_ROOT_DEFAULT_VALUE;
        }
        ResourceRequestHandler resourceRequestHandler = new ResourceRequestHandler(resourceHandlerRoot);
       
        // Registering resource handler for two contexts: root and "/resources"
        webServer.addRequestHandler("/", resourceRequestHandler);

        webServer.addRequestHandler("/resources", resourceRequestHandler);

        // Registering test cookie handler and tracing handler
        webServer.addRequestHandler("/cookie", new CookieTestRequestHandler());

        webServer.addRequestHandler("/trace", new TraceRequestHandler());
        
        try {

            System.out.println("********************************");
            System.out.println("* Before webServer.start: " + listeningPort + "  *");
            
            webServer.start();
            
            System.out.println("* WebServer.localAddress: " + webServer.getServerSocket().getLocalAddress() + "  *");
            System.out.println("* WebServer.localPort: " + webServer.getServerSocket().getLocalPort()+ "  *");
            System.out.println("* Web server started successfully *");
            System.out.println("********************************");

            
        } catch (Exception e) {
            System.out.println("Failed to start server: " + e.getMessage());
            destroyApp(true);
        }
    }

    private int getIntFromProperty(String propertyName, int defaultValue) {
        // Getting property from the current Application properties without delimeters
        String intStringValue = getAppProperty(propertyName);
        if (intStringValue == null || intStringValue.length() == 0) {
            return defaultValue;
        } else {
            try {
                return Integer.parseInt(intStringValue);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }

    private boolean getBooleanFromProperty(String propertyName, boolean defaultValue) {
        // Getting property from the current Application properties without delimeters
        String booleanStringValue = getAppProperty(propertyName);
        if (booleanStringValue == null || booleanStringValue.length() == 0) {
            return defaultValue;
        } else {
            String testedValue = booleanStringValue.trim();
            if(testedValue.equals("true")) return true;
            else if(testedValue.equals("false")) return false;
            else  return defaultValue;
        }
    }

    // @Override
    public void destroyApp(boolean unconditional) {
        if (webServer != null) {
            try {
                webServer.stop();
                System.out.println("Web server stopped successfully");
            } catch (Exception e) {
                System.out.println("Failed to stop server: " + e.getMessage());
            }
        }

        System.out.println("**********************************");
        System.out.println("*   Web Server Demo destroyed    *");
        System.out.println("**********************************");
        // Es del midlet notifyDestroyed();
    }


    public static void main(String[] _args)
    {
       LightWebServerApplication wsApp = new LightWebServerApplication();
       try{
           wsApp.startApp();       
       } catch (Exception ex)
       {
           System.out.println("");
           wsApp.destroyApp(true);
       }
    }
}
