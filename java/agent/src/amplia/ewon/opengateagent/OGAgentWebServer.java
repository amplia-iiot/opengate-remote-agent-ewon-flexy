/*
 * Copyright © 2015, Oracle and/or its affiliates.  All rights reserved. 
 *
 * This software is dual-licensed to you under the MIT License (MIT) and the
 * Universal Permissive License (UPL).  See the LICENSE file in the root directory
 * for license terms.  You may choose either license, or both.
 */
package amplia.ewon.opengateagent;

// import com.oracle.jmee.samples.webserver.FileSystemRequestHandler;
import amplia.util.json.JSONArray;
import amplia.util.json.JSONException;
import amplia.util.json.JSONObject;
import amplia.util.json.JSONTokener;
import com.oracle.jmee.samples.webserver.HttpCookie;
import com.oracle.jmee.samples.webserver.HttpRequest;
import com.oracle.jmee.samples.webserver.HttpResponse;
import com.oracle.jmee.samples.webserver.RequestHandler;
import com.oracle.jmee.samples.webserver.ResourceRequestHandler;
import com.oracle.jmee.samples.webserver.WebServer;
import com.oracle.jmee.samples.webserverdemo.CookieTestRequestHandler;
import com.oracle.jmee.samples.webserverdemo.TraceRequestHandler;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.midlet.MIDlet;


/**
 * Demo version which has been optimized for platforms with strict memory
 * limitations. Please see comments for class of original demo and accompanying
 * readme file for more information.
 */
public class OGAgentWebServer /* extends MIDlet */ implements RequestHandler
{

    // Names and default values of used properties
    private static final String FILE_SYSTEM_HANDLER_ROOT_PROPERTY_NAME = "WebServerApplication-FileSystemRequestHandler-Root";
    private static final String FILE_SYSTEM_HANDLER_ROOT_DEFAULT_VALUE = System.getProperty("user.dir");

    private static final String LISTENING_PORT_PROPERTY_NAME = "WebServerApplication-ListeningPort";
    private static final int LISTENING_PORT_DEFAULT_VALUE = 1123;

    private static final String PRINT_DIRECTORY_CONTENTS_PROPERTY_NAME = "WebServerApplication-FileSystemRequestHandler-PrintDirectoryContents";
    private static final boolean PRINT_DIRECTORY_CONTENTS_DEFAULT_VALUE = true;

    private static final String FILE_SYSTEM_HANDLER_WELCOME_PAGE_PROPERTY_NAME = "WebServerApplication-FileSystemRequestHandler-WelcomePage";
    private static final String FILE_SYSTEM_HANDLER_WELCOME_PAGE_DEFAULT_VALUE = null;

    private static final String RESOURCE_HANDLER_ROOT_PROPERTY_NAME = "WebServerApplication-JarResourceRequestHandler-Root";
    private static final String RESOURCE_HANDLER_ROOT_DEFAULT_VALUE = "/resources";
    
    private static final String NEW_LINES_CHARACTERS = "\r\n";
    
    private String m_deviceId = "";
    
    private OGAgentOperationHandlerInterface m_operationHandler = null;
    
    public void setOGAgentOperationHandler(OGAgentOperationHandlerInterface _operationHandler)
    {
        m_operationHandler = _operationHandler;
    }
    
    public OGAgentWebServer(String _deviceId)
    {
        m_deviceId = _deviceId;
    }

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
        System.out.println("********************************");
        System.out.println("*   Web Server Demo Starting    *");
        System.out.println("********************************");

        // Obtaining port to listen for incoming connections
        int listeningPort = getIntFromProperty(LISTENING_PORT_PROPERTY_NAME, LISTENING_PORT_DEFAULT_VALUE);
        if (listeningPort < 0 || listeningPort > 65535) {
            System.out.println("Listening port must be in [0,65535] range. The specified value is: " + listeningPort);
            destroyApp(true);
            return;
        }
        
        // Creating a web server instance
        webServer = new WebServer(listeningPort);

        System.out.println("WebServer instance created");
        
        // Persisting connections is disabled due to memory limitations
        webServer.setPersistConnections(false);

        // Setting buffer size
        webServer.setTransferBufferSize(1024);

        System.out.println("WebServer instance setTransferBufferSize done");

        // Initializing and adding handler which allows to access the files from
        // the filesystem. Getting values of configuration options from JAD
        String fileSystemHandlerRoot = getAppProperty(FILE_SYSTEM_HANDLER_ROOT_PROPERTY_NAME);
        if (fileSystemHandlerRoot == null) {
            fileSystemHandlerRoot = FILE_SYSTEM_HANDLER_ROOT_DEFAULT_VALUE;
        }

        System.out.println("getAppProperty(FILE_SYSTEM_HANDLER_ROOT_PROPERTY_NAME) done");

        boolean listDirectoryContents = getBooleanFromProperty(PRINT_DIRECTORY_CONTENTS_PROPERTY_NAME, PRINT_DIRECTORY_CONTENTS_DEFAULT_VALUE);

        System.out.println("getBooleanFromProperty(PRINT_DIRECTORY_CONTENTS_PROPERTY_NAME, PRINT_DIRECTORY_CONTENTS_DEFAULT_VALUE) done");

        String welcomPagePath = getAppProperty(FILE_SYSTEM_HANDLER_WELCOME_PAGE_PROPERTY_NAME);
        if (welcomPagePath == null) {
            welcomPagePath = FILE_SYSTEM_HANDLER_WELCOME_PAGE_DEFAULT_VALUE;
        }
        
        System.out.println("getAppProperty(FILE_SYSTEM_HANDLER_WELCOME_PAGE_PROPERTY_NAME) done");
        
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
        // webServer.addRequestHandler("/", resourceRequestHandler);

        // webServer.addRequestHandler("/resources", resourceRequestHandler);

        // Registering test cookie handler and tracing handler
        // webServer.addRequestHandler("/cookie", new CookieTestRequestHandler());

        // webServer.addRequestHandler("/trace", new TraceRequestHandler());
        
        // Registrando Handler para peticiones OG
        String ogAgentUrl= "/v70/devices/"+m_deviceId+"/operation/requests";        
        webServer.addRequestHandler(ogAgentUrl, this);
        System.out.println("Registrada URL: "+ogAgentUrl);
        
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

    public HttpResponse handle(HttpRequest request, String contextPath, String relativePath) {
                // Gathering information about request in the string builder
        StringBuffer info = new StringBuffer();
        info.append("Handling request from ").append(request.getRemoteAddress()).append(": ").append(NEW_LINES_CHARACTERS);
        info.append("\tVersion: ").append(request.getHttpVersion()).append(NEW_LINES_CHARACTERS);
        info.append("\tRequested URI: ").append(request.getRequestUri()).append(NEW_LINES_CHARACTERS);
        info.append("\tRequested path: ").append(request.getRequestPath()).append(NEW_LINES_CHARACTERS);
        info.append("\tContext path: ").append(contextPath).append(NEW_LINES_CHARACTERS);
        info.append("\tRelative to the context path: ").append(relativePath).append(NEW_LINES_CHARACTERS);
        info.append("\tUsed method: ").append(request.getRequestMethodAsString()).append(NEW_LINES_CHARACTERS);

        // Gathering information about headers
        Enumeration headersNamesEnumeration = request.getHeadersNames();
        if (headersNamesEnumeration.hasMoreElements()) {
            info.append(NEW_LINES_CHARACTERS);
            info.append("Headers: ").append(NEW_LINES_CHARACTERS);
            
            for (;headersNamesEnumeration.hasMoreElements();) 
            {
                String headerName = (String)headersNamesEnumeration.nextElement();
                info.append('\t').append(headerName).append(": ").append(request.getHeaderValue(headerName)).append(NEW_LINES_CHARACTERS);
            }
        }

        // Gathering information about URI parameters
        Enumeration uriParametersNamesEnumeration = request.getUriParametersNames();
        if (uriParametersNamesEnumeration.hasMoreElements()) {
            info.append(NEW_LINES_CHARACTERS);
            info.append("GET parameters: ").append(NEW_LINES_CHARACTERS);
            for (;uriParametersNamesEnumeration.hasMoreElements();) 
            {
                String parameterName = (String)uriParametersNamesEnumeration.nextElement();
                info.append('\t').append(parameterName).append("=").append(request.getUriParameter(parameterName)).append(NEW_LINES_CHARACTERS);
            }
        }

        // Gathering information about body parameters
        Enumeration postParametersNamesEnumeration = request.getPostParametersNames();
        if (postParametersNamesEnumeration.hasMoreElements()) {
            info.append(NEW_LINES_CHARACTERS);
            info.append("POST parameters: ").append(NEW_LINES_CHARACTERS);
            for (;postParametersNamesEnumeration.hasMoreElements();) 
            {
                String parameterName = (String)postParametersNamesEnumeration.nextElement();
                info.append('\t').append(parameterName).append("=").append(request.getPostParameter(parameterName)).append(NEW_LINES_CHARACTERS);
            }
        }

        // Gathering information about cookies
        Vector httpCookies = request.getHttpCookies();
        if (httpCookies.size() > 0) {
            info.append(NEW_LINES_CHARACTERS);
            info.append("Cookies: ").append(NEW_LINES_CHARACTERS);
            for (Enumeration cookiesEnumeration = httpCookies.elements();cookiesEnumeration.hasMoreElements();) {
                HttpCookie cookie = (HttpCookie)cookiesEnumeration.nextElement();
                info.append('\t').append(cookie).append(NEW_LINES_CHARACTERS);
            }
        }

        // Appending request body
        String requestBodyAsString = request.getRequestBodyAsString();
        if (requestBodyAsString != null && requestBodyAsString.length() > 0) {
            info.append(NEW_LINES_CHARACTERS);
            info.append("Body:").append(NEW_LINES_CHARACTERS);
            info.append(requestBodyAsString).append(NEW_LINES_CHARACTERS);
        }
        // System.out.println(info.toString());
        
        String httpResponse = "";
        
        try 
        {
            httpResponse = ogRequestParse(requestBodyAsString);
            return HttpResponse.created(httpResponse).setContentType("application/json; charset=utf-8");
        } catch (JSONException ex)
        {
            // Hay que construir el formato de respuesta con error
            return HttpResponse.badRequest(ex.toString()).setContentType("application/json; charset=utf-8");    
        }

        // Sending response with the collection information as a plain text
        // return HttpResponse.ok(info.toString()).setContentType("text/plain; charset=utf-8");     
    }
    /*  FORMATO
        {
            "operation" :
            {
                "request" :
                {
                    "id" : "f508ce84-01e9-11e5-a322-1697f925ec7b",
                    "deviceId": "device_1",
                    "path" : [],
                    "timestamp" : 1432454275000,
                    "name" : "SET_CLOCK",
                    "parameters": [
                        {
                            "name" : "datetime",
                            "value" : "YYYY-MM-DD"
                        },
                        {
                            "name" : "time",
                            "value" : "hh:mm:ss.s"
                        },
                        {
                            "name" : "timezone",
                            "value" : "(+/-)hh:mm"
                        },
                        {
                            "name" : "daylightsavingtime",
                            "value" : "(+/-)hh:mm"
                        }
                    ]
                }
            }
        }    
    */
    private String ogRequestParse(String _json) throws JSONException
    {
        String ret = "";
        JSONObject jsonParser = new JSONObject(new JSONTokener(_json));
        JSONObject jsonRequest = jsonParser.getJSONObject("operation").getJSONObject("request");
        
        
        String operationId = jsonRequest.getString("id");     
        
        String deviceId = ""; 
        if(jsonRequest.has("deviceId"))
        {
            deviceId = jsonRequest.getString("deviceId");
            
        }

        JSONArray path = new JSONArray();
        if(jsonRequest.has("path"))
        {
            path = jsonRequest.getJSONArray("path");;
            
        }
        
        String operationName = jsonRequest.getString("name");
        JSONArray parameters = jsonRequest.getJSONArray("parameters");
        
        if(m_operationHandler==null)
        {
            ret = OGAgentResponseUtils.operationResponseJSON(operationId,null,deviceId,operationName,OGAgentResponseUtils.RESPONSE_RESULT_CODE_OPERATION_NOT_SUPPORTED,"Operation handler not registered");
        } else
        {
            ret = m_operationHandler.handle(operationId, deviceId, path, operationName, parameters);
        }
        return ret;
    }    
}
