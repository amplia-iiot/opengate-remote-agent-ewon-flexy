/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package amplia.ewon.opengateagent;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Javier Martínez
 */
public class OGAgentResponseUtils {
    
    public static final int RESPONSE_RESULT_CODE_SUCCESSFUL = 1; // The operation request has been executed and finished without errors
    public static final int RESPONSE_RESULT_CODE_OPERATION_PENDING = 2; 
    public static final int RESPONSE_RESULT_CODE_OPERATION_ERROR_IN_PARAM = 3; 
    public static final int RESPONSE_RESULT_CODE_OPERATION_NOT_SUPPORTED = 4; 
    public static final int RESPONSE_RESULT_CODE_OPERATION_ALREADY_IN_PROGRESS = 5; 
    public static final int RESPONSE_RESULT_CODE_OPERATION_ERROR_PROCESSING = 6; 
    public static final int RESPONSE_RESULT_CODE_OPERATION_ERROR_TIMEOUT = 7; 
    public static final int RESPONSE_RESULT_CODE_OPERATION_TIMEOUT_CANCELLED = 9; 
    public static final int RESPONSE_RESULT_CODE_OPERATION_TIMEOUT_CANCELLED_INTERNAL = 10; 


    public static String responseResultCodeToString(int _resultCode)
    {
        switch(_resultCode)
        {
            case RESPONSE_RESULT_CODE_SUCCESSFUL: return "SUCCESSFUL";
            case RESPONSE_RESULT_CODE_OPERATION_PENDING: return "OPERATION_PENDING";
            case RESPONSE_RESULT_CODE_OPERATION_ERROR_IN_PARAM: return "ERROR_IN_PARAM";
            case RESPONSE_RESULT_CODE_OPERATION_NOT_SUPPORTED: return "NOT_SUPPORTED";
            case RESPONSE_RESULT_CODE_OPERATION_ALREADY_IN_PROGRESS: return "ALREADY_IN_PROGRESS";
            case RESPONSE_RESULT_CODE_OPERATION_ERROR_PROCESSING: return "ERROR_PROCESSING";
            case RESPONSE_RESULT_CODE_OPERATION_ERROR_TIMEOUT: return "ERROR_TIMEOUT";
            case RESPONSE_RESULT_CODE_OPERATION_TIMEOUT_CANCELLED: return "TIMEOUT_CANCELLED";
            case RESPONSE_RESULT_CODE_OPERATION_TIMEOUT_CANCELLED_INTERNAL: return "TIMEOUT_CANCELLED_INTERNAL";
            default:
                return "RESPONSE_STATUS_CODE_OPERATION_ERROR_PROCESSING";
        }
    }
            
    /* Formato
    {
        "version" : "7.0",
        "operation" :
        {
            "response" :
            {
                "id" : "f508ce84-01e9-11e5-a322-1697f925ec7b",
                "timestamp" : 1432454282000,
                "deviceId" : "device_1",
                "name" : "SET_CLOCK",
                "resultCode" : "SUCCESS",
                "resultDescription" : "No Error",
            }
        }
    }
    */
    public static String operationResponseJSON(String _id, Date _timestamp, String _deviceId, String _name, int _resultCode, String _resultDescription)
    {
        String ret = "";
        
        ret =  "{\n";
        ret += "    \"version\" : \"7.0\",";
        ret += "    \"operation\" :";
        ret += "    {";
        ret += "        \"response\" :";
        ret += "        {";
        ret += "            \"id\" : \"" + _id + "\",\n";
        
        Date timestamp;
        
        if(_timestamp!=null)
        {
            timestamp = _timestamp;
        } else
        {
            // Cojo la fecha y hora actuales de la máquina
            timestamp = new Date();
            timestamp.setTime(System.currentTimeMillis());
        }
        ret += "            \"timestamp\" :" + timestamp.getTime() + ",\n";
        
        if(_deviceId != null && _deviceId.length()>0)
        {
            ret += "            \"deviceId\" : \"" + _deviceId + "\",\n";
        }
        
        ret += "            \"name\" : \"" + _name + "\",\n";
        ret += "            \"resultCode\" : \"" + responseResultCodeToString(_resultCode) + "\",\n";
        ret += "            \"resultDescription\" : \"" + _resultDescription + "\"\n";
        ret += "        }";
        ret += "    }";
        ret += "}";
        
        return ret;
    }

}
