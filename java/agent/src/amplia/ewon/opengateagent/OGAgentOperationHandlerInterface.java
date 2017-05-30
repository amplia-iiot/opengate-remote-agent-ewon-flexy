/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package amplia.ewon.opengateagent;

import amplia.util.json.JSONArray;
import amplia.util.json.JSONObject;

/**
 *
 * @author Javier Martínez
 */
public interface OGAgentOperationHandlerInterface {
    // Manejo de Operationces
    // Retorna el JSON de respuesta
    public String handle(String _id, String _deviceId, JSONArray _path, String _operationName, JSONArray _parameters);
}
