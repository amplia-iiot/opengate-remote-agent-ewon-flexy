/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package amplia.ewon.opengateagent;

import amplia.util.json.JSONArray;
import com.ewon.ewonitf.EWException;
import com.ewon.ewonitf.EwonSystem;
import com.ewon.ewonitf.IOManager;
import com.ewon.ewonitf.RuntimeControl;
import com.oracle.jmee.samples.webserverdemo.LightWebServerApplication;
import com.ewon.ewonitf.SysControlBlock;

/**
 *
 * @author Javier Martínez
 */
public class OGAgentMain implements OGAgentOperationHandlerInterface 
{
    public static void main(String[] _args)
    {
        // Obtengo el device ID
        String deviceId = "unknown";
        
        try
        {
            SysControlBlock sysControlBlock = new SysControlBlock(SysControlBlock.INF);
            deviceId = sysControlBlock.getItem("sernum");
            System.out.println("DeviceID:" + deviceId);
        } catch (EWException ex)
        {
            System.out.println("Fallo al obtener el Device ID:"+ex.toString());
            return;
        }
        
        OGAgentWebServer ogAgentWebServer = new OGAgentWebServer(deviceId);
        ogAgentWebServer.setOGAgentOperationHandler(new OGAgentMain());
        
        try{
           System.out.println("OpengateAgentMain.Starting...");        
           ogAgentWebServer.startApp();       
           System.out.println("OpengateAgentMain.Initiated");        
        } catch (Exception ex)
        {
           ogAgentWebServer.destroyApp(true);
        }
    }

    public String handle(String _id, String _deviceId, JSONArray _path, String _operationName, JSONArray _parameters) {
        String ret = "";
        
        if(_operationName.equals("REBOOT_EQUIPMENT"))
        {
            ret = OGAgentResponseUtils.operationResponseJSON(_id, null, _deviceId, _operationName, OGAgentResponseUtils.RESPONSE_RESULT_CODE_SUCCESSFUL , "");
            doRebootEquipment(_parameters);
        }
        else
        {
            
        }
        
        return ret;
    }

    private void doRebootEquipment(JSONArray _parameters)
    {
            System.out.println("doRebootEquipment:"+_parameters.toString());
            // Espero 2 segundos para ejecutar el reicio
            try
            {
                Thread.sleep(2000);
            } catch (Exception ex) {}
            // RuntimeControl.reboot(); 
            try
            {
                // IOManager.writeTag("crane.containersCount", (float)1.0);
                IOManager.writeTag("crane.feeding.hdg.plc.resetAlarm", (float)1.0);
            }
            catch (Exception e)
            {
               System.out.println("IOManager.writeTag->Error occured: "+e.toString());
            }
    }
}
