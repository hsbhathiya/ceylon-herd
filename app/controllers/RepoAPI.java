package controllers;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;

import play.Logger;
import play.mvc.Before;
import util.ApiVersion;

import models.Module;
import models.Module.Type;
import models.ModuleVersion;

public class RepoAPI extends MyController {
    
    public static final int RESULT_LIMIT = 20;

    @Before
    public static void fixFormat(){
        Logger.info(request.format);
        // default to json
        if(request.format == null
                || (!request.format.equals("json")
                        && !request.format.equals("xml")))
            request.format = "json";
        // Play doesn't set the charset for us when rendering a template :(
        response.contentType = "application/"+request.format+"; charset="+response.encoding;
    }
    
    public static void completeVersions(String apiVersion, String module, String version, String type, Integer binaryMajor, Integer binaryMinor){
        if(module == null || module.isEmpty())
            badRequest("module parameter required");
        Module mod = Module.findByName(module);
        if(mod == null)
            notFound("Module not found");
        Type t = getType(type);
        ApiVersion v = getApiVersion(apiVersion);
        
        List<ModuleVersion> versions = ModuleVersion.completeVersionForModuleAndBackend(mod, version, t, binaryMajor, binaryMinor);
        
        renderArgs.put("apiVersion", v);
        render(versions);
    }

    private static Type getType(String type) {
        if(type == null || type.isEmpty())
            return Type.JVM;
        if(type.equalsIgnoreCase("jvm"))
            return Type.JVM;
        if(type.equalsIgnoreCase("javascript"))
            return Type.JS;
        if(type.equalsIgnoreCase("source"))
            return Type.SRC;
        if(type.equalsIgnoreCase("all"))
            return Type.ALL;
        if(type.equalsIgnoreCase("code"))
            return Type.CODE;
        error(HttpURLConnection.HTTP_BAD_REQUEST, "Unknown type, must be one of: jvm,javascript,source,all,code");
        // never reached
        return null;
    }

    public static void completeModules(String apiVersion, String module, String type, Integer binaryMajor, Integer binaryMinor){
        Type t = getType(type);
        ApiVersion v = getApiVersion(apiVersion);

        List<Module> modules = Module.completeForBackend(module, t, binaryMajor, binaryMinor);
        long total = Module.completeForBackendCount(module, t, binaryMajor, binaryMinor);
        long start = 0;
        
        renderArgs.put("type", t);
        renderArgs.put("apiVersion", v);
        // we need to put those in renderArgs rather than render() because they may be null
        renderArgs.put("binaryMajor", binaryMajor);
        renderArgs.put("binaryMinor", binaryMinor);
        render(modules, start, total);
    }

    public static void searchModules(String apiVersion, String query, String type, Integer start, Integer count, 
            Integer binaryMajor, Integer binaryMinor){
        if(start == null || start < 0)
            start = 0;
        if(count == null || count < 0 || count > RESULT_LIMIT)
            count = RESULT_LIMIT;
        Type t = getType(type);
        ApiVersion v = getApiVersion(apiVersion);

        List<Module> modules = Module.searchForBackend(query, t, start, count, binaryMajor, binaryMinor);
        long total = Module.searchForBackendCount(query, t, binaryMajor, binaryMinor);
        
        renderArgs.put("type", t);
        renderArgs.put("apiVersion", v);
        // we need to put those in renderArgs rather than render() because they may be null
        renderArgs.put("binaryMajor", binaryMajor);
        renderArgs.put("binaryMinor", binaryMinor);
        render(modules, start, total);
    }

    private static ApiVersion getApiVersion(String apiVersion) {
        for(ApiVersion v : ApiVersion.values()){
            if(v.version.equals(apiVersion))
                return v;
        }
        badRequest("Invalid apiVersion parameter: "+apiVersion+". This instance of Herd supports 1 or 2.");
        return null;
    }
}
