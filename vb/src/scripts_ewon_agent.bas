Rem --- eWON start section: Cyclic Section
eWON_cyclic_section:
Rem --- eWON user (start)

Rem --- eWON user (end)
End
Rem --- eWON end section: Cyclic Section
Rem --- eWON start section: Init Section
eWON_init_section:
Rem --- eWON user (start)
    // VARIABLES A CONFIGURAR
    ipAddressAndPort$ = "212.231.129.162:9955" // Conexión a opengate cloud.opengate.es
    apiKey$ = "c7a97860-13c4-4bac-8668-f888f0e549c1" // API Key asignada al dispositivo en OpenGate
    ogSwAgentName$ = "ogAgent_EWON"
    ogSwAgentVersion$ = "3.3.0"
    ogSwAgentDate$ = "2016-05-18T06:00:00Z"
	
    TimeoutDataStream% = 10 // Timer = 10 seconds
    TimeoutDMM% = 120 // Timer = 2 minutes
    TimeoutSimulator% = 3 // Timer = 3 seconds

    deviceId$ = GETSYS PRG, "SERNUM"
    
    // INICIO TIMERS
    DataStreamLoopVar = 0
    TSET 1, TimeoutDataStream% 
    ONTIMER 1, "GOTO PacecoOpenGateDatastreamsMain"

    DMMLoopVar = 0
    TSET 2, TimeoutDMM%
    ONTIMER 2, "GOTO PacecoOpenGateDMMMain"
    
    DevSimulatorLoopVar = 0
    // TSET 3, TimeoutSimulator%
    // ONTIMER 3, "GOTO PacecoOpenGateDeviceSimulatorMain"
    
    // Variables para medida de tiempos
    timeIni% = 0
    timeFin% = 0
Rem --- eWON user (end)
End
Rem --- eWON end section: Init Section


Rem --- eWON start section: PacecoOpenGateDatastreamsMain
Rem --- eWON user (start)

// Section_1_1 (Main)
PacecoOpenGateDatastreamsMain:
    DataStreamLoopVar = 1
    IF (DataStreamLoopVar > 0) THEN 
        GOTO PacecoOpenGateDatastreamsBuildAndSending
        DataStreamLoopVar = 0
    ENDIF    
END

Rem --- eWON user (end)
End
Rem --- eWON end section: PacecoOpenGateDatastreamsMain

Rem --- eWON start section: PacecoOpenGateDMMMain
Rem --- eWON user (start)

// Section_1_2 (Main)
PacecoOpenGateDMMMain:
    DMMLoopVar = 1
    IF (DMMLoopVar > 0) THEN 
        GOTO PacecoOpenGateDMMBuildAndSending
        DMMLoopVar = 0
    ENDIF    
END

Rem --- eWON user (end)
End
Rem --- eWON end section: PacecoOpenGateDMMMain

Rem --- eWON start section: PacecoOpenGateDeviceSimulatorMain
Rem --- eWON user (start)

// Section_1_3 (Main)
PacecoOpenGateDeviceSimulatorMain:
    DevSimulatorLoopVar = 1
    IF (DevSimulatorLoopVar > 0) THEN 
        GOTO PacecoOpenGateDevSimulator
        DMMLoopVar = 0
    ENDIF    
END

Rem --- eWON user (end)
End
Rem --- eWON end section: PacecoOpenGateDeviceSimulatorMain


Rem --- eWON start section: PacecoOpenGateDatastreamsBuildAndSending
Rem --- eWON user (start)

// Section_2_1 (Applicacion - Datastreams)
PacecoOpenGateDatastreamsBuildAndSending:

    // Variables de configuración
    fileNameDatastream$ = "og_datastreams.json"
    fileNameDatastreamBenchmarking$ = "og_datastreams_benchmarking.txt"
    urlDatastreamPath$ = "/v70/devices/" + deviceId$ + "/collect/iot?xHyKZ=" + apiKey$   // "/"
    tagNamePrefix$ = "crane"

    PRINT "Datastreams Collection for DeviceID: " + deviceId$

    // --------------- 1.- Generando Archivo Opengate ---------------
    timeIni% = FCNV TIME$,40
    
    // ----- Open Benchmarking file por output
    OPEN fileNameDatastreamBenchmarking$ FOR BINARY OUTPUT AS 4

    // ----- Open JSON file por output
    OPEN fileNameDatastream$ FOR BINARY OUTPUT AS 1
    PRINT "Creats File " + fileNameDatastream$

    // ----- Writing JSON header
    PUT 1, '{' + (CHR$(13)+CHR$(10))
    PUT 1, '    "version":"1.0.1",' + (CHR$(13)+CHR$(10))
    PUT 1, '    "datastreams" : [' +(CHR$(13)+CHR$(10))

    timeIniTagListFile% = FCNV TIME$,40
    
    OPEN "exp:$dtTL $ftT" FOR TEXT INPUT AS 2 // ----- Open data export for reading tag list
    
    LineIn$ = Get 2 // Elimino la linea de cabecera

    timeFinTagListFile% = FCNV TIME$,40
    timeElapsed$ = SFMT (timeFinTagListFile% - timeIniTagListFile%), 10,4
    PUT 4, "Elapsed - Opening Tags list File & Reading Header: " + timeElapsed$ + (CHR$(13)+CHR$(10))

    N%=1
    TagListLoopBegin:

        IF EOF 2 THEN GOTO TagListLoopEnd

        timeIniTag% = FCNV TIME$,40        
        
        // Formato: Solo interesa la segunda columna
        // 2;"CommCounter";"Communication OK counter";"S73&400";"A";....;

        timeIniTagHeader% = FCNV TIME$,40        

        LineIn$ = Get 2 // Leo la línea completa
        // PRINT LineIn$

        var% = LEN LineIn$               
        // PRINT var%
        IF (var% = 0) THEN GOTO TagListLoopBegin        
        
        idx1% = INSTR 1, LineIn$,";"
        // PRINT idx1% +1
        idx2% = INSTR (idx1% + 1), LineIn$,";"
        
        TagId$ = LineIn$(1 TO idx1%-1)
        TagName$ = LineIn$(idx1%+1 TO idx2%-1)
        // PRINT TagName$
        
        idxPaceco% = INSTR 1, TagName$,tagNamePrefix$
        
        timeFinTagHeader% = FCNV TIME$,40
        timeElapsed$ = SFMT (timeFinTagHeader% - timeIniTagHeader%), 10,4   
        N$ = SFMT (N%), 10,4  
        PUT 4, "Elapsed - Lectura y Procesado del Tag Header[" + N$ + "]: " + timeElapsed$ + (CHR$(13)+CHR$(10)) 
        
        If (idxPaceco% = 0) THEN GOTO TagListLoopBegin // Solo proceso las variables que empiecen o contengan la palabra por "paceco"
        
        // ----- Recover and Process Data for Current TagName
        TimeoutDataStream$ = SFMT TimeoutDataStream%,30,0,"%d"

        // Readed from History log
        // E$ = "exp:$dtHL $ftT$tn" + TagName$ + "$st_s" + TimeoutDataStream$ + "$et_s0$ut" // ----- Open data export for reading values for the TagName. Reading the latest 10 seconds. Marks de time
        
        // Readed from Real Time log
        E$ = "exp:$dtRL $ftT$tn" + TagName$ + "$st_s + " + TimeoutDataStream$ + "$et_s0$ut" // ----- Open data export for reading values for the TagName. Reading the latest TimeoutDataStream$ seconds. Marks de time
        
        // PRINT E$ 

        timeIniTagValueFile% = FCNV TIME$,40
        
        OPEN E$ FOR TEXT INPUT AS 3    
        
        timeFinTagValueFile% = FCNV TIME$,40
        timeElapsed$ = SFMT (timeFinTagValueFile% - timeIniTagValueFile%), 10,4                
        PUT 4, "Elapsed - Opening Tags Values File: " + timeElapsed$ + (CHR$(13)+CHR$(10))

        TagTimeStampInt%=0
        TagTimeStamp$=""
        TagIsInitValue%=0
        TagValue=0

        If N%>1 THEN  PUT 1, ',' + (CHR$(13)+CHR$(10))
                
        // ---- Inicio Objeto DataStream (TagName)
        PUT 1, '        {' + (CHR$(13)+CHR$(10))
        PUT 1, '            "id" : ' + TagName$ + ',' + (CHR$(13)+CHR$(10))
        PUT 1, '            "feed" : "",' + (CHR$(13)+CHR$(10))
        PUT 1, '            "datapoints" : [' + (CHR$(13)+CHR$(10))

        I%=1
        
        timeIniTagValueHeader% = FCNV TIME$,40
        
        LineIn$ = Get 3 // Elimino la linea de cabecera               
        
        timeFinTagValueHeader% = FCNV TIME$,40
        timeElapsed$ = SFMT (timeFinTagValueHeader% - timeIniTagValueHeader%), 10,4                
        PUT 4, "Elapsed - Opening Tags Values Get Header: " + timeElapsed$ + (CHR$(13)+CHR$(10))
        
        TagValuesLoopBegin:
            IF EOF 3 THEN GOTO TagValuesLoopEnd
            
            // ----- Read Values from a line from export block
            // Format: Si es HL
            // "TimeInt";"TimeStr";"IsInitValue";"Value";"IQuality"
            // 1453815618;"26/01/2016 13:40:18";1;0;3
            // Format: Si es RL
            // "TimeInt";"TimeStr";"Value"
            // 1453815618;"26/01/2016 13:40:18";0

            timeIniTagValueData% = FCNV TIME$,40
            
            LineIn$ = GET 3 // Lee los datos
            
            timeFinTagValueData% = FCNV TIME$,40
            timeElapsed$ = SFMT (timeFinTagValueData% - timeIniTagValueData%), 10,4                
            PUT 4, "Elapsed - Opening Tags Values Get Value: " + timeElapsed$ + (CHR$(13)+CHR$(10))

            timeIniTagValueData% = FCNV TIME$,40
            
            var% = LEN LineIn$               
            // PRINT var%
            IF (var% = 0) THEN GOTO TagValuesLoopBegin        
            
            idx1% = INSTR 1, LineIn$,";"
            idx2% = INSTR (idx1%+1), LineIn$,";"
            
            // Solo sirve si es HL no para RL
            // idx3% = INSTR (idx2%+1), LineIn$,";"
            // idx4% = INSTR (idx3%+1), LineIn$,";"

            TagTimeStampInt$ = LineIn$(1 TO idx1%-1)
            TagTimeStamp$ = LineIn$(idx1%+1 TO idx2%-1)
            // TagIsInitValue$ = LineIn$(idx2%+1 TO idx3%-1) // Solo para HL
            // TagValue$ = LineIn$(idx3%+1 TO idx4%-1) // Para HL
            TagValue$ = LineIn$(idx2%+1 TO) // Para RL
            
            // Filtro por si hay un parámetro vacio solo con CRLF
            IF (LEN (TagValue$) <= 2) THEN TagValue$ = ""
            ELSE
                // Elimino los caracteres CRLF
                idxCrLf% = INSTR 1, TagValue$,CHR$(13)
                
                IF idxCrLf% > 0 THEN 
                    // PRINT "Param:" + TagValue$
                    TagValue$ = RTRIM(LTRIM(TagValue$(1 TO idxCrLf%-1)))
                ENDIF
            ENDIF
                
            // Special values conversion
            idxName = INSTR 1, TagName$,"crane.feeding.hdg.hybridManagementSystem.status"
            IF idxName > 0 THEN
                TagValueInt% = FCNV TagValue$,30,0,"%d"
                TagValue$ = "UNKNOWN"
                IF TagValueInt% = 0 THEN TagValue$ = "IDLE"
                IF TagValueInt% = 1 THEN TagValue$ = "REGENERATING"
                IF TagValueInt% = 2 THEN TagValue$ = "SUPPORTING"
                IF TagValueInt% = 3 THEN TagValue$ = "CHARGING"
                IF TagValueInt% = 4 THEN TagValue$ = "DERATING"
                IF TagValueInt% = 7 THEN TagValue$ = "STOPPED"
                IF TagValueInt% = 10 THEN TagValue$ = "INIT_HYBRID"
                IF TagValueInt% = 11 THEN TagValue$ = "PRECHARGE"
                IF TagValueInt% = 12 THEN TagValue$ = "STAND_BY"
                IF TagValueInt% = 13 THEN TagValue$ = "DISCHARGING"
                IF TagValueInt% = 14 THEN TagValue$ = "EMERGENCY"
                IF TagValueInt% = 15 THEN TagValue$ = "SAFETY"
                
                TagValue$ = '"' + TagValue$ + '"'
            ENDIF

            idxName = INSTR 1, TagName$,"crane.feeding.hdg.hybridManagement.forceMode"
            IF idxName > 0 THEN   
                TagValueInt% = FCNV TagValue$,30,0,"%d"
                TagValue$ = "UNKNOWN"
                IF TagValueInt% = 0 THEN TagValue$ = "UNKNOWN"
                IF TagValueInt% = 1 THEN TagValue$ = "AUTOMATIC"
                IF TagValueInt% = 2 THEN TagValue$ = "MANUAL"

                TagValue$ = '"' + TagValue$ + '"'
            ENDIF
            
            idxName = INSTR 1, TagName$,"crane.hoist.joystick.status"
            IF idxName > 0 THEN   
                TagValueInt% = FCNV TagValue$,30,0,"%d"
                TagValue$ = "UNKNOWN"
                IF TagValueInt% = 0 THEN TagValue$ = "UNKNOWN"
                IF TagValueInt% = 1 THEN TagValue$ = "UP"
                IF TagValueInt% = 2 THEN TagValue$ = "DOWN"
                IF TagValueInt% = 3 THEN TagValue$ = "ZERO"

                TagValue$ = '"' + TagValue$ + '"'
            ENDIF

            idxName = INSTR 1, TagName$,"crane.trolley.joystick.status"
            IF idxName > 0 THEN   
                TagValueInt% = FCNV TagValue$,30,0,"%d"
                TagValue$ = "UNKNOWN"
                IF TagValueInt% = 0 THEN TagValue$ = "UNKNOWN"
                IF TagValueInt% = 1 THEN TagValue$ = "FWD"
                IF TagValueInt% = 2 THEN TagValue$ = "REV"
                IF TagValueInt% = 3 THEN TagValue$ = "ZERO"

                TagValue$ = '"' + TagValue$ + '"'
            ENDIF            
            
            idxName = INSTR 1, TagName$,"crane.gantry.joystick.status"
            IF idxName > 0 THEN   
                TagValueInt% = FCNV TagValue$,30,0,"%d"
                TagValue$ = "UNKNOWN"
                IF TagValueInt% = 0 THEN TagValue$ = "UNKNOWN"
                IF TagValueInt% = 1 THEN TagValue$ = "LEFT"
                IF TagValueInt% = 2 THEN TagValue$ = "RIGHT"
                IF TagValueInt% = 3 THEN TagValue$ = "ZERO"

                TagValue$ = '"' + TagValue$ + '"'
            ENDIF   
 
            // PRINT "Readed: " + TagTimeStamp$ + ", " + TagName$ + ", " + TagValue$

            // ----- Write Datastreams in JSON file
            // Formato:   {"at":1431602523,"value":41},
            If I%>1 THEN  PUT 1, ',' + (CHR$(13)+CHR$(10))        
            PUT 1, '                {"at" :' + TagTimeStampInt$ + ',"value":' + TagValue$ + '}'

            I% = I%+1
            
            timeFinTagValueData% = FCNV TIME$,40
            timeElapsed$ = SFMT (timeFinTagValueData% - timeIniTagValueData%), 10,4
            
            MyI$ = SFMT (N%), 10,4              
            PUT 4, "Elapsed - Opening Tags Values Data[" + MyI$ + "]: " + (CHR$(13)+CHR$(10))
            
            GOTO TagValuesLoopBegin
        TagValuesLoopEnd:
        
        // PRINT "Datapoints: " & I%

        PUT 1, '' + (CHR$(13)+CHR$(10)) // Meto el CR
        PUT 1, '            ]' + (CHR$(13)+CHR$(10))
        PUT 1, '        }'  // ---- Fin Objeto DataStream (TagName)

        CLOSE 3 // close history log export block

        N% = N%+1    
        
        timeFinTag% = FCNV TIME$,40
        timeElapsed$ = SFMT (timeFinTag% - timeIniTag%), 10,4       
        N$ = SFMT (N%), 10,4        
        PUT 4, "Elapsed - Lectura y Procesado Tag[" + N$ + "]: " + timeElapsed$ + (CHR$(13)+CHR$(10))
        
        If TagName$ <>"" Then GOTO TagListLoopBegin
    TagListLoopEnd:

    CLOSE 2 // close taglist export block

    // ----- Writing JSON footer
    PUT 1, '' + (CHR$(13)+CHR$(10)) // Meto el CR
    PUT 1,  '    ]' + (CHR$(13)+CHR$(10))
    PUT 1,  '}' + (CHR$(13)+CHR$(10))

    CLOSE 1 // close JSON file

    timeFin% = FCNV TIME$,40
    timeElapsed$ = SFMT (timeFin% - timeIni%), 10,4                               
    PUT 4, "Elapsed - Creating File: " + timeElapsed$ + (CHR$(13)+CHR$(10))

    CLOSE 4 // close Benchmarking file


    // ---------------  2.- Enviando Archivo Opengate a la plataforma ---------------
    timeIni% = FCNV TIME$,40

    PRINT "Sending File " + fileNameDatastream$ + "to: " + ipAddressAndPort$ + urlDatastreamPath$

    PUTHTTP ipAddressAndPort$, urlDatastreamPath$,"", "opengatejsonfile[]=[$dtUF$fn" + "/usr/" + fileNameDatastream$ + "]","failed"
    ONSTATUS "goto PacecoOpenGateDatastreamPutHttpStatus"
    ONERROR "goto PacecoDatastreamPutHttpError"

END

Rem --- eWON user (end)
End
Rem --- eWON end section: PacecoOpenGateDatastreamsBuildAndSending


Rem --- eWON start section: PacecoOpenGateDMMBuildAndSending
Rem --- eWON user (start)

// Section_2_2 (Applicacion - DMM)
PacecoOpenGateDMMBuildAndSending:

    // Variables de configuración
    fileNameDMM$ = "og_dmm.json"
    fileNameDMMBenchmarking$ = "og_dmm_benchmarking.txt"
    urlDMMPath$ = "/v70/devices/" + deviceId$ + "/collect/dmm?xHyKZ=" + apiKey$   // "/"
    
    SETSYS SYS, "LOAD"  // Cargo las variables 
    
    deviceId$ = GETSYS PRG, "SERNUM"
    deviceIdentification$ = GETSYS SYS, "Identification"
    deviceInformation$ = GETSYS SYS, "Information"
    
    datetime$ = TIME$
    upTime% = GETSYS PRG, "MSEC"
    
    PRINT "DMM Collection for DeviceID: " + deviceId$

    // --------------- 1.- Generando Archivo Opengate ---------------
    timeIni% = FCNV TIME$,40
    
    // ----- Open Benchmarking file por output
    OPEN fileNameDMMBenchmarking$ FOR BINARY OUTPUT AS 6

    
    // ----- Open and Read Stat File
    OPEN "exp:$dtES$ftT" FOR TEXT INPUT AS 7
    
    MbPartNum$ = ""
    
    SIFBootldrRev$ = ""
    CodeName$ = ""
    BuildInfo$ = ""
    FwrVersion$ = ""
    JavaVersion$ = ""    
    HasGsm$ = ""
    ModemInitStatus$ = ""

    GsmIMEI$ = ""
    ModemDetectedTxt$ = ""
    ModemType$ = "MOBILE"
    ModemExtInfo$ = "" // Formato: MFR: Telit , MODEL: HE910-D , VER: 12.00.026   
    
    GsmCCID$ = ""
    GsmIMS$ = ""
    GsmOpName$ = ""
    GsmIMSI$ = ""
    PppIp$ = ""
    PppIpType$ = ""    
    
    GsmCellId$ = ""
    GsmLAC$ = ""  
    GsmSignal$ = ""
    GsmOpId$ = ""    
    
    EthWANCableIn$ = ""
    WanIp$ = ""
    
    SumVPNStatus$ = ""
    VpnIp$ = ""
    
    StatusParameterListLoopBegin:

        IF EOF 7 THEN GOTO StatusParameterListLoopEnd

        L$ = Get 7
        
        lenL% = LEN L$
        
        IF (lenL% <= 0) THEN GOTO StatusParameterListLoopBegin
        
        idx1% = INSTR 1, L$,":"   
        ParameterName$ = RTRIM(LTRIM(L$(1 TO idx1%-1)))
        ParameterValue$ = RTRIM(LTRIM(L$(idx1%+1 TO)))
        
        // Filtro para evitar que entren lineas solo con CRLF
        IF (LEN (ParameterValue$) <= 2) THEN GOTO StatusParameterListLoopBegin

        // Elimino los caracteres CRLF
        idx2% = INSTR 1, ParameterValue$,CHR$(13)
        
        IF idx2% > 0 THEN 
            // PRINT "Param:" + ParameterValue$
            ParameterValue$ = RTRIM(LTRIM(ParameterValue$(1 TO idx2%-1)))
        ENDIF

        // Obtengo Variables
        idx1% = INSTR 1, ParameterName$ ,"MbPartNum"
        IF (idx1% > 0) THEN 
            MbPartNum$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF
        
        
        idx1% = INSTR 1, ParameterName$ ,"SIFBootldrRev"
        IF (idx1% > 0) THEN 
            SIFBootldrRev$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF
        
        idx1% = INSTR 1, ParameterName$ ,"CodeName"
        IF (idx1% > 0) THEN 
            CodeName$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF        

        idx1% = INSTR 1, ParameterName$ ,"BuildInfo"
        IF (idx1% > 0) THEN 
            BuildInfo$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF 
        
        idx1% = INSTR 1, ParameterName$ ,"FwrVersion"
        IF (idx1% > 0) THEN 
            FwrVersion$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF         

        idx1% = INSTR 1, ParameterName$ ,"JavaVersion"
        IF (idx1% > 0) THEN 
            JavaVersion$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF  

        idx1% = INSTR 1, ParameterName$ ,"HasGsm"
        IF (idx1% > 0) THEN 
            HasGsm$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF   

        idx1% = INSTR 1, ParameterName$ ,"ModemInitStatus"
        IF (idx1% > 0) THEN 
            ModemInitStatus$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF         
        
        idx1% = INSTR 1, ParameterName$ ,"GsmIMEI"
        IF (idx1% > 0) THEN 
            GsmIMEI$ = ParameterValue$            
            GOTO StatusParameterListLoopBegin
        ENDIF     
        
        idx1% = INSTR 1, ParameterName$ ,"ModemDetectedTxt"
        IF (idx1% > 0) THEN 
            ModemDetectedTxt$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF     
        
        idx1% = INSTR 1, ParameterName$ ,"ModemType"
        IF (idx1% > 0) THEN 
            ModemType$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF              
        
        idx1% = INSTR 1, ParameterName$ ,"ModemExtInfo"
        IF (idx1% > 0) THEN 
            ModemExtInfo$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF     

        idx1% = INSTR 1, ParameterName$ ,"GsmCCID"
        IF (idx1% > 0) THEN 
            GsmCCID$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF    
        
        idx1% = INSTR 1, ParameterName$ ,"GsmIMS"
        IF (idx1% > 0) THEN 
            GsmIMS$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF    

        idx1% = INSTR 1, ParameterName$ ,"GsmOpName"
        IF (idx1% > 0) THEN 
            GsmOpName$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF    

        idx1% = INSTR 1, ParameterName$ ,"GsmIMSI"
        IF (idx1% > 0) THEN 
            GsmIMSI$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF    

        idx1% = INSTR 1, ParameterName$ ,"PppIp"
        IF (idx1% > 0) THEN 
            PppIp$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF    
        
        idx1% = INSTR 1, ParameterName$ ,"GsmCellId"
        IF (idx1% > 0) THEN 
            GsmCellId$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF    

        idx1% = INSTR 1, ParameterName$ ,"GsmLAC"
        IF (idx1% > 0) THEN 
            GsmLAC$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF    
        
        idx1% = INSTR 1, ParameterName$ ,"GsmSignal"
        IF (idx1% > 0) THEN 
            GsmSignal$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF    

        idx1% = INSTR 1, ParameterName$ ,"GsmOpId"
        IF (idx1% > 0) THEN 
            GsmOpId$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF
        
        idx1% = INSTR 1, ParameterName$ ,"EthWANCableIn"
        IF (idx1% > 0) THEN 
            EthWANCableIn$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF

        idx1% = INSTR 1, ParameterName$ ,"WanIp"
        IF (idx1% > 0) THEN 
            WanIp$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF
        
        idx1% = INSTR 1, ParameterName$ ,"SumVPNStatus"
        IF (idx1% > 0) THEN 
            SumVPNStatus$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF
        
        idx1% = INSTR 1, ParameterName$ ,"VpnIp"
        IF (idx1% > 0) THEN 
            VpnIp$ = ParameterValue$
            GOTO StatusParameterListLoopBegin
        ENDIF        
        

        GOTO StatusParameterListLoopBegin
    StatusParameterListLoopEnd:       
    
    CLOSE 7  // Cierro fichero de parametros
    
    // ----- Open JSON file por output
    OPEN fileNameDMM$ FOR BINARY OUTPUT AS 5
    PRINT "Created File " + fileNameDMM$

    // ----- Writing JSON header
    PUT 5, '{' + (CHR$(13)+CHR$(10))
    PUT 5, '  "version":"7.0",' + (CHR$(13)+CHR$(10))
    PUT 5, '  "event" : {' +(CHR$(13)+CHR$(10))
    
    // ---- Inicio Objeto Device)
    PUT 5, '    "device" : {' + (CHR$(13)+CHR$(10))
    PUT 5, '      "id" : "' + deviceId$ + '",' + (CHR$(13)+CHR$(10))
    PUT 5, '      "path" : [],' + (CHR$(13)+CHR$(10))
    PUT 5, '      "name" : "' + deviceId$ + '",' + (CHR$(13)+CHR$(10))
    PUT 5, '      "description" : "' + deviceIdentification$ + ". " + deviceInformation$ + '",' + (CHR$(13)+CHR$(10))
    
    // OJO hay que extraer el modelo y version de MbPartNum$. Formato "FLEXY20300_00"
    idx1% = INSTR 1, MbPartNum$,"_"   
    DeviceModel$ = RTRIM(LTRIM(MbPartNum$(1 TO idx1%-1)))
    DeviceModelVersion$ = RTRIM(LTRIM(MbPartNum$(idx1%+1 TO)))
    
    PUT 5, '      "hardware" : {' + (CHR$(13)+CHR$(10))
    PUT 5, '        "serialnumber" : "' + deviceId$ + '",' + (CHR$(13)+CHR$(10))
    PUT 5, '        "manufacturer" : {' + (CHR$(13)+CHR$(10))
    PUT 5, '          "name" : "eWON",' + (CHR$(13)+CHR$(10))
    PUT 5, '          "oui" : ""' + (CHR$(13)+CHR$(10))
    PUT 5, '        },' +(CHR$(13)+CHR$(10))   
    PUT 5, '        "model" : {' + (CHR$(13)+CHR$(10))
    PUT 5, '          "name" : "' DeviceModel$ + '",' + (CHR$(13)+CHR$(10))
    PUT 5, '          "version" : "' + DeviceModelVersion$ + '"' + (CHR$(13)+CHR$(10))
    PUT 5, '        },' +(CHR$(13)+CHR$(10)) 
    
    // OJO Convertir datetime$ (18/09/2003 15:45:30) a formato 8601. "clockDate" : "2014-07-08T13:02:41Z"
    idx1% = INSTR 1, datetime$," "       
    dateVar$ = RTRIM(LTRIM(datetime$(1 TO idx1%-1)))
    timeVar$ = RTRIM(LTRIM(datetime$(idx1%+1 TO)))
    
    idx1% = INSTR 1, datetime$,"/"       
    idx2% = INSTR (idx1% + 1), datetime$,"/"
    
    dayVar$ = RTRIM(LTRIM(dateVar$(1 TO idx1%-1)))
    monthVar$ = RTRIM(LTRIM(dateVar$(idx1%+1 TO idx2%-1)))
    yearVar$ = RTRIM(LTRIM(dateVar$(idx2%+1 TO)))
    
    clockDate$ = yearVar$ + "-" + monthVar$ + "-" + dayVar$ + "T" + timeVar$ + "Z"
    
    upTime% = upTime%/1000
    upTime$ = SFMT upTime%,30,0,"%d"
    
    PUT 5, '        "clockDate" : "' + clockDate$ + '",' + (CHR$(13)+CHR$(10))
    PUT 5, '        "upTime" : "' + upTime$ + '"' + (CHR$(13)+CHR$(10))
    
    PUT 5, '     },' +(CHR$(13)+CHR$(10))
    PUT 5, '     "softwareList" : [' + (CHR$(13)+CHR$(10))
    
    PUT 5, '       {' + (CHR$(13)+CHR$(10))
    PUT 5, '          "name" : "BootLoader",' + (CHR$(13)+CHR$(10))
    PUT 5, '          "type" : "FIRMWARE",' + (CHR$(13)+CHR$(10))
    PUT 5, '          "version" : "' + SIFBootldrRev$ + '",' + (CHR$(13)+CHR$(10))
    PUT 5, '          "date" : ""' + (CHR$(13)+CHR$(10))
    PUT 5, '       },' + (CHR$(13)+CHR$(10))
        
    firmware$ = CodeName$ + "." + BuildInfo$ + "-" + FwrVersion$
    
    PUT 5, '       {' + (CHR$(13)+CHR$(10))
    PUT 5, '          "name" : "Firmware",' + (CHR$(13)+CHR$(10))
    PUT 5, '          "type" : "FIRMWARE",' + (CHR$(13)+CHR$(10))
    PUT 5, '          "version" : "' + SIFBootldrRev$ + '",' + (CHR$(13)+CHR$(10))
    PUT 5, '          "date" : ""' + (CHR$(13)+CHR$(10))
    PUT 5, '       },' + (CHR$(13)+CHR$(10))
    
    PUT 5, '       {' + (CHR$(13)+CHR$(10))
    PUT 5, '          "name" : "' + ogSwAgentName$ + '",' + (CHR$(13)+CHR$(10))
    PUT 5, '          "type" : "SOFTWARE",' + (CHR$(13)+CHR$(10))
    PUT 5, '          "version" : "' + ogSwAgentVersion$ + '",' + (CHR$(13)+CHR$(10))
    PUT 5, '          "date" : "' + ogSwAgentDate$ + '"' + (CHR$(13)+CHR$(10))
    PUT 5, '       },' + (CHR$(13)+CHR$(10))
	
    PUT 5, '       {' + (CHR$(13)+CHR$(10))
    PUT 5, '          "name" : "Java",' + (CHR$(13)+CHR$(10))
    PUT 5, '          "type" : "SOFTWARE",' + (CHR$(13)+CHR$(10))
    PUT 5, '          "version" : "' + JavaVersion$ + '",' + (CHR$(13)+CHR$(10))
    PUT 5, '          "date" : ""' + (CHR$(13)+CHR$(10))
    PUT 5, '       }' + (CHR$(13)+CHR$(10))
    
    PUT 5, '     ],' + (CHR$(13)+CHR$(10)) 
    
    PUT 5, '     "communicationsModules" : [' + (CHR$(13)+CHR$(10)) 

    HasGsm% = FCNV HasGsm$,30,0, "%d"
    
    ModemInitStatus% = 0
    ModemInitStatus% = FCNV ModemInitStatus$,30,0, "%d"
    
    IF HasGsm% > 0 THEN   
    
        // Obtención Manufacturer, Model, Version
        // Formato: MFR: Telit , : HE910-D , VER: 12.00.026              
        idx1% = INSTR 1, ModemExtInfo$ ,"MFR:"
        idx2% = INSTR idx1%, ModemExtInfo$ ,","        
        ModemExtInfo_MFR$ = LTRIM (RTRIM (ModemExtInfo$(idx1%+4 TO idx2%-1)))  // Manufacturer
                    
        idx1% = INSTR 1, ModemExtInfo$ ,"MODEL:"
        idx2% = INSTR idx1%, ModemExtInfo$ ,","        
        ModemExtInfo_MODEL$ = LTRIM (RTRIM (ModemExtInfo$(idx1%+6 TO idx2%-1)))  // Model

        idx1% = INSTR 1, ModemExtInfo$ ,"VER:"
        ModemExtInfo_VER$ = LTRIM (RTRIM (ModemExtInfo$(idx1%+4 TO)))  // Version
        
        IF (LEN(GsmIMEI$) = 0) THEN GsmIMEI$ = deviceId$ + "_0000"

        PUT 5, '       {' + (CHR$(13)+CHR$(10))
        PUT 5, '          "id" : "' + GsmIMEI$ + '",' + (CHR$(13)+CHR$(10))
        PUT 5, '          "name" : "' + ModemDetectedTxt$ + '",' + (CHR$(13)+CHR$(10))
        PUT 5, '          "type" : "' + ModemType$ + '",' + (CHR$(13)+CHR$(10))
        PUT 5, '          "hardware" : {' + (CHR$(13)+CHR$(10))
        PUT 5, '            "serialnumber" : "' + GsmIMEI$ + '",' + (CHR$(13)+CHR$(10))
        PUT 5, '            "manufacturer" : {' + (CHR$(13)+CHR$(10))
        PUT 5, '              "name" : "' + ModemExtInfo_MFR$ + '",' + (CHR$(13)+CHR$(10))
        PUT 5, '              "oui" : ""' + (CHR$(13)+CHR$(10))
        PUT 5, '            },' +(CHR$(13)+CHR$(10))   
        PUT 5, '            "model" : {' + (CHR$(13)+CHR$(10))
        PUT 5, '              "name" : "' + ModemExtInfo_MODEL$ + '",' + (CHR$(13)+CHR$(10))
        PUT 5, '              "version" : "' + ModemExtInfo_VER$ + '"' + (CHR$(13)+CHR$(10))
        PUT 5, '            }' +(CHR$(13)+CHR$(10))   
        PUT 5, '          },' +(CHR$(13)+CHR$(10))
        
        operationalStatus$ = "STOPPED"
        IF ModemInitStatus% > 0 THEN operationalStatus$ = "RUNNING"
        PUT 5, '          "operationalStatus" : "' + operationalStatus$ + '",' + (CHR$(13)+CHR$(10))
        
        PUT 5, '          "subscriber" : {' + (CHR$(13)+CHR$(10))
        PUT 5, '            "id" : "' + GsmCCID$ + '",' + (CHR$(13)+CHR$(10))
        PUT 5, '            "type" : "SIM"' + (CHR$(13)+CHR$(10))
        PUT 5, '          },' +(CHR$(13)+CHR$(10))
        
        // OJO Calculo IPType
        PppIpType$ = "IPV4"  
        
        PUT 5, '          "subscription" : {' + (CHR$(13)+CHR$(10))
        PUT 5, '            "id" : "' + GsmIMS$ + '",' + (CHR$(13)+CHR$(10))
        PUT 5, '            "name" : "' + GsmIMS$ + '",' + (CHR$(13)+CHR$(10))
        PUT 5, '            "operator" : "' + GsmOpName$ + '",' + (CHR$(13)+CHR$(10))
        PUT 5, '            "imsi" : "' + GsmIMSI$ + '",' + (CHR$(13)+CHR$(10))
        PUT 5, '            "address" : {' + (CHR$(13)+CHR$(10))
        PUT 5, '              "type" : "' + PppIpType$ + '",' + (CHR$(13)+CHR$(10))
        PUT 5, '              "value" : "' + PppIp$ + '"' + (CHR$(13)+CHR$(10))
        PUT 5, '            }' +(CHR$(13)+CHR$(10))
        PUT 5, '          },' +(CHR$(13)+CHR$(10))       
        
        PUT 5, '          "mobile" : {' + (CHR$(13)+CHR$(10))
        PUT 5, '            "cellId" : "' + GsmCellId$ + '",' + (CHR$(13)+CHR$(10))
        PUT 5, '            "lac" : "' + GsmLAC$ + '",' + (CHR$(13)+CHR$(10))
        PUT 5, '            "signalStrength" : "' + GsmSignal$ + '",' + (CHR$(13)+CHR$(10))
        PUT 5, '            "plmn" : "' + GsmOpId$ + '"' + (CHR$(13)+CHR$(10))
        PUT 5, '          }' +(CHR$(13)+CHR$(10))
        
        PUT 5, '       }'
    ENDIF 
    
    EthWANCableIn% = FCNV EthWANCableIn$,30,0, "%d"
    
    // IF EthWANCableIn% > 0 THEN   
    //  PUT 5, ',' + (CHR$(13)+CHR$(10))
    //  PUT 5, '       {' + (CHR$(13)+CHR$(10))
    //  PUT 5, '          "id" : "' + deviceId$ + '_EthWAN_MDL",' + (CHR$(13)+CHR$(10))
    
    // OJO Calculo IPType
    //    EthWANIpType$ = "IPV4"  
        
    //    PUT 5, '          "subscription" : {' + (CHR$(13)+CHR$(10))
    //    PUT 5, '            "id" : "' + deviceId$ + '_EthWAN_SBN",' + (CHR$(13)+CHR$(10))
    //    PUT 5, '            "name" : "' + deviceId$ + '_EthWAN_SBN",' + (CHR$(13)+CHR$(10))
    //    PUT 5, '            "address" : {' + (CHR$(13)+CHR$(10))
    //    PUT 5, '              "type" : "' + EthWANIpType$ + '",' + (CHR$(13)+CHR$(10))
    //    PUT 5, '              "value" : "' + WanIp$ + '"' + (CHR$(13)+CHR$(10))
    //    PUT 5, '            }' +(CHR$(13)+CHR$(10))
    //    PUT 5, '          }' +(CHR$(13)+CHR$(10))  

    //  PUT 5, '       }'
    // ENDIF
    
    SumVPNStatus% = FCNV SumVPNStatus$,30,0, "%d"
    
    // Formato: 999 00 deviceId$(sin guiones. Ej: 1523-0156-21 --> 1523015621): "999001523015621"
    idx1% = INSTR 1, deviceId$,"-"
    idx2% = INSTR (idx1%+1), deviceId$,"-"    
    
    VpnSubscriptionId$ = "99900" + deviceId$(1 TO idx1%-1) + deviceId$(idx1%+1 TO idx2%-1) + deviceId$(idx2%+1 TO)
    
    IF SumVPNStatus% > 0 THEN   
       PUT 5, ',' + (CHR$(13)+CHR$(10))
       PUT 5, '       {' + (CHR$(13)+CHR$(10))
       
       // OJO deberia ser el modulo que corresponda GSM o Ethernet. Habrá que ver como detectar esto
       PUT 5, '          "id" : "' + deviceId$ + '_EthWAN_MDL",' + (CHR$(13)+CHR$(10))
    
       // OJO Calculo IPType
       VpnIpType$ = "IPV4"  
     
       PUT 5, '          "subscription" : {' + (CHR$(13)+CHR$(10))
       PUT 5, '            "id" : "' + VpnSubscriptionId$ + '",' + (CHR$(13)+CHR$(10))
       PUT 5, '            "name" : "' + VpnSubscriptionId$ + '",' + (CHR$(13)+CHR$(10))
       PUT 5, '            "address" : {' + (CHR$(13)+CHR$(10))
       PUT 5, '              "type" : "' + VpnIpType$ + '",' + (CHR$(13)+CHR$(10))
       PUT 5, '              "value" : "' + VpnIp$ + '"' + (CHR$(13)+CHR$(10))
       PUT 5, '            }' +(CHR$(13)+CHR$(10))
       PUT 5, '          }' +(CHR$(13)+CHR$(10))  

       PUT 5, '       }'
    ENDIF    
    
    PUT 5, '' + (CHR$(13)+CHR$(10))
    
    PUT 5, '      ]' + (CHR$(13)+CHR$(10)) 
    
    PUT 5, '    }' +(CHR$(13)+CHR$(10))
    PUT 5, '  }' +(CHR$(13)+CHR$(10))
    PUT 5, '}' +(CHR$(13)+CHR$(10))
        
    CLOSE 5 // close JSON file

    timeFin% = FCNV TIME$,40
    timeElapsed$ = SFMT (timeFin% - timeIni%), 10,4                          
    PUT 6, "Elapsed - Creating File: " + timeElapsed$ + (CHR$(13)+CHR$(10))
    
    CLOSE 6 // close Benchmarking file

    // ---------------  2.- Enviando Archivo Opengate a la plataforma ---------------
    timeIni% = FCNV TIME$,40

    PRINT "Sending File " + fileNameDMM$ + "to: " + ipAddressAndPort$ + urlDMMPath$
        
    PUTHTTP ipAddressAndPort$, urlDMMPath$,"", "opengatejsonfile[]=[$dtUF$fn" + "/usr/" + fileNameDMM$ + "]","failed"
    ONSTATUS "goto PacecoOpenGateDMMPutHttpStatus"
    ONERROR "goto PacecoDMMPutHttpError"

END

Rem --- eWON user (end)
End
Rem --- eWON end section: PacecoOpenGateDMMBuildAndSending


Rem --- eWON start section: PacecoOpenGateDatastreamPutHttpStatus
Rem --- eWON user (start)

// Section_3_1 (Status checking)
PacecoOpenGateDatastreamPutHttpStatus:
    StatusCode% = GETSYS PRG, "LSTERR"
    PRINT "PUTHTTP Sending Status:" + str$ StatusCode%

    // ----- Open Benchmarking file por output
    OPEN fileNameDatastreamBenchmarking$ FOR BINARY OUTPUT AS 4
    
    timeFin% = FCNV TIME$,40
    timeElapsed$ = SFMT (timeFin% - timeIni%), 10,4                              
    PUT 4, "Elapsed - Sending File: " + timeElapsed$ + (CHR$(13)+CHR$(10))
    CLOSE 4 // close Benchmarking file

END

Rem --- eWON user (end)
End
Rem --- eWON end section: PacecoOpenGateDatastreamPutHttpStatus


Rem --- eWON start section: PacecoOpenGateDMMPutHttpStatus
Rem --- eWON user (start)

// Section_3_2 (Status checking)
PacecoOpenGateDMMPutHttpStatus:
    StatusCode% = GETSYS PRG, "LSTERR"
    PRINT "PUTHTTP Sending Status:" + str$ StatusCode%

    // ----- Open Benchmarking file por output
    OPEN fileNameDMMBenchmarking$ FOR BINARY OUTPUT AS 6

    timeFin% = FCNV TIME$,40
    timeElapsed$ = SFMT (timeFin% - timeIni%), 10,4                              
    PUT 6, "Elapsed - Sending File: " + timeElapsed$ + (CHR$(13)+CHR$(10))
    CLOSE 6 // close Benchmarking file

END


Rem --- eWON user (end)
End
Rem --- eWON end section: PacecoOpenGateDMMPutHttpStatus



Rem --- eWON start section: PacecoDatastreamPutHttpError
Rem --- eWON user (start)

// Section_4_1 (Error)
PacecoDatastreamPutHttpError:
    ErrorCode% = GETSYS PRG, "LSTERR"
    PRINT "PUTHTTP Sending Error:" + str$ ErrorCode%

    // ----- Open Benchmarking file por output
    OPEN fileNameDatastreamBenchmarking$ FOR BINARY OUTPUT AS 4
    
    timeFin% = FCNV TIME$,40
    timeElapsed$ = SFMT (timeFin% - timeIni%), 10,4                              
    PUT 4, "Elapsed - Sending File: " + timeElapsed$ + (CHR$(13)+CHR$(10))
    CLOSE 4 // close Benchmarking file    
END

Rem --- eWON user (end)
End
Rem --- eWON end section: PacecoDatastreamPutHttpError


Rem --- eWON start section: PacecoDMMPutHttpError
Rem --- eWON user (start)

// Section_4_2 (Error)
PacecoDMMPutHttpError:
    ErrorCode% = GETSYS PRG, "LSTERR"
    PRINT "PUTHTTP Sending Error:" + str$ ErrorCode%

    // ----- Open Benchmarking file por output
    OPEN fileNameDMMBenchmarking$ FOR BINARY OUTPUT AS 6
    
    timeFin% = FCNV TIME$,40
    timeElapsed$ = SFMT (timeFin% - timeIni%), 10,4                              
    PUT 6, "Elapsed - Sending File: " + timeElapsed$ + (CHR$(13)+CHR$(10))
    CLOSE 6 // close Benchmarking file
END

Rem --- eWON user (end)
End
Rem --- eWON end section: PacecoDMMPutHttpError


Rem --- eWON start section: PacecoOpenGateDevSimulator
Rem --- eWON user (start)

// Section_2_3 (Device - Simulator)
PacecoOpenGateDevSimulator:

PRINT "OpenGate Device Simulator Started..."

currentTime% = FCNV TIME$,40

// Range between 350 and 500
valueDouble = 350 + (currentTime% MOD 150)
SETIO "crane.feeding.hdg.generator.consumption",valueDouble

// Range between 10 and 50 
valueInt% = 10 + (currentTime% MOD 40)
SETIO "crane.containersCount",valueInt%


// Range between 2000 and 2500
valueDouble = 2000 + (currentTime% MOD 1500)
SETIO "crane.feeding.hdg.generator.powerActive",valueDouble

// Range between 2500 and 3000
valueDouble = 2500 + (currentTime% MOD 1500)
SETIO "crane.feeding.hdg.ucs.power",valueDouble

// Range between 0 and 100
valueDouble = 0 + (currentTime% MOD 100)
SETIO "crane.feeding.hdg.ucs.soc",valueDouble


// Range between 0 and 3
valueInt% = 0 + (currentTime% MOD 3)

SETIO "crane.feeding.hdg.hybridManagementSystem.status",0
IF valueInt% = 0 THEN SETIO "crane.feeding.hdg.hybridManagementSystem.status",0  // "IDLE"
IF valueInt% = 1 THEN SETIO "crane.feeding.hdg.hybridManagementSystem.status",1  // "REGENERATING"
IF valueInt% = 2 THEN SETIO "crane.feeding.hdg.hybridManagementSystem.status",3  // "CHARGING"
IF valueInt% = 3 THEN SETIO "crane.feeding.hdg.hybridManagementSystem.status",13 // "DISCHARGING"

PRINT "OpenGate Device Simulator Finished..."

END

Rem --- eWON user (end)
End
Rem --- eWON end section: PacecoOpenGateDevSimulator
