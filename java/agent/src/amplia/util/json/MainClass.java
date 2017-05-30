package amplia.util.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *@link : http://10.0.0.53/rcgi.bin/jvmCmd?cmd=start&runCmd= -heapsize 1M -classpath/usr/Json.jar -emain MainClass
 * @author eWonSupport_Adm
 */
public class MainClass {
    
    
   
    public static void main(String[] args)
    {
        try
        {
           /* File Content
            * ------------
            * {
                "locations": {
                    "record": [
                    {
                        "id": 8817,
                        "loc": "NEW YORK CITY"
                    },
                    {
                        "id": 2873,
                        "loc": "UNITED STATES"
                    },
                    {
                        "id": 1501,
                        "loc": "NEW YORK STATE"
                    }
                    ]
                }
                }
           */           

            JSONTokener JsonT = new JSONTokener(readFile("file:////usr/test.json"));
            JSONObject req = new JSONObject(JsonT);
            JSONObject locs = req.getJSONObject("locations");
            JSONArray recs = locs.getJSONArray("record");


            for (int i = 0; i < recs.length(); ++i) 
            { 
                JSONObject record = recs.getJSONObject(i); 
                int id = record.getInt("id"); 
                System.out.println(id);
                String loc = record.getString("loc");
                System.out.println(loc);
            }
  
        }
        catch(Exception e)
        {
        System.out.println(e.getMessage());
        }
    }
    
     public static String readFile(String path)
   {
 
      FileConnection    fconn  = null;
      InputStream       is  = null;
      OutputStream      os  = null;
      
      try
      {
         fconn = (FileConnection) Connector.open(path);
         // If no exception is thrown, then the URI is valid, but the file may or may not exist.
         
         is = fconn.openInputStream();
         int Avail, Read;
         byte[] Buffer =new byte[46102];
         
         Avail = is.available();
        
            Read = is.read(Buffer);
            fconn.close();
            return new String(Buffer, 0, Read);
        
         
      }
      catch (IOException ioe)
      {
         System.out.println("Error readFileTest: "+ioe.toString());
         return "";
      }
   }
    
}
