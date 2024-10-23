package com.tugalsan.tst.htmx;

import com.tugalsan.api.log.server.TS_Log;
import io.javalin.Javalin;
import io.javalin.community.ssl.SslPlugin;
import io.javalin.community.ssl.TlsConfig;
import io.javalin.http.staticfiles.Location;
import java.io.FileNotFoundException;

/*https://javalin.io/documentation#after-handlers
    [ctx]
    // Request methods
    body()                                // request body as string
    bodyAsBytes()                         // request body as array of bytes
    bodyAsClass(clazz)                    // request body as specified class (deserialized from JSON)
    bodyStreamAsClass(clazz)              // request body as specified class (memory optimized version of above)
    bodyValidator(clazz)                  // request body as validator typed as specified class
    bodyInputStream()                     // the underyling input stream of the request
    uploadedFile("name")                  // uploaded file by name
    uploadedFiles("name")                 // all uploaded files by name
    uploadedFiles()                       // all uploaded files as list
    uploadedFileMap()                     // all uploaded files as a "names by files" map
    formParam("name")                     // form parameter by name, as string
    formParamAsClass("name", clazz)       // form parameter by name, as validator typed as specified class
    formParams("name")                    // list of form parameters by name
    formParamMap()                        // map of all form parameters
    pathParam("name")                     // path parameter by name as string
    pathParamAsClass("name", clazz)       // path parameter as validator typed as specified class
    pathParamMap()                        // map of all path parameters
    basicAuthCredentials()                // basic auth credentials (or null if not set)
    attribute("name", value)              // set an attribute on the request
    attribute("name")                     // get an attribute on the request
    attributeOrCompute("name", ctx -> {}) // get an attribute or compute it based on the context if absent
    attributeMap()                        // map of all attributes on the request
    contentLength()                       // content length of the request body
    contentType()                         // request content type
    cookie("name")                        // request cookie by name
    cookieMap()                           // map of all request cookies
    header("name")                        // request header by name (can be used with Header.HEADERNAME)
    headerAsClass("name", clazz)          // request header by name, as validator typed as specified class
    headerMap()                           // map of all request headers
    host()                                // host as string
    ip()                                  // ip as string
    isMultipart()                         // true if the request is multipart
    isMultipartFormData()                 // true if the request is multipart/formdata
    method()                              // request methods (GET, POST, etc)
    path()                                // request path
    port()                                // request port
    protocol()                            // request protocol
    queryParam("name")                    // query param by name as string
    queryParamAsClass("name", clazz)      // query param by name, as validator typed as specified class
    queryParamsAsClass("name", clazz)     // query param list by name, as validator typed as list of specified class
    queryParams("name")                   // list of query parameters by name
    queryParamMap()                       // map of all query parameters
    queryString()                         // full query string
    scheme()                              // request scheme
    sessionAttribute("name", value)       // set a session attribute
    sessionAttribute("name")              // get a session attribute
    consumeSessionAttribute("name")       // get a session attribute, and set value to null
    cachedSessionAttribute("name", value) // set a session attribute, and cache the value as a request attribute
    cachedSessionAttribute("name")        // get a session attribute, and cache the value as a request attribute
    cachedSessionAttributeOrCompute(...)  // same as above, but compute and set if value is absent
    sessionAttributeMap()                 // map of all session attributes
    url()                                 // request url
    fullUrl()                             // request url + query string
    contextPath()                         // request context path
    userAgent()                           // request user agent
    req()                                 // get the underlying HttpServletRequest

    // Response methods
    result("result")                      // set result stream to specified string (overwrites any previously set result)
    result(byteArray)                     // set result stream to specified byte array (overwrites any previously set result)
    result(inputStream)                   // set result stream to specified input stream (overwrites any previously set result)
    future(futureSupplier)                // set the result to be a future, see async section (overwrites any previously set result)
    writeSeekableStream(inputStream)      // write content immediately as seekable stream (useful for audio and video)
    result()                              // get current result stream as string (if possible), and reset result stream
    resultInputStream()                   // get current result stream
    contentType("type")                   // set the response content type
    header("name", "value")               // set response header by name (can be used with Header.HEADERNAME)
    redirect("/path", code)               // redirect to the given path with the given status code
    status(code)                          // set the response status code
    status()                              // get the response status code
    cookie("name", "value", maxAge)       // set response cookie by name, with value and max-age (optional).
    cookie(cookie)                        // set cookie using javalin Cookie class
    removeCookie("name", "/path")         // removes cookie by name and path (optional)
    json(obj)                             // calls result(jsonString), and also sets content type to json
    jsonStream(obj)                       // calls result(jsonStream), and also sets content type to json
    html("html")                          // calls result(string), and also sets content type to html
    render("/template.tmpl", model)       // calls html(renderedTemplate)
    res()                                 // get the underlying HttpServletResponse

    // Other methods
    async(runnable)                       // lifts request out of Jetty's ThreadPool, and moves it to Javalin's AsyncThreadPool
    async(asyncConfig, runnable)          // same as above, but with additonal config
    handlerType()                         // handler type of the current handler (BEFORE, AFTER, GET, etc)
    appData(typedKey)                     // get data from the Javalin instance (see app data section below)
    with(pluginClass)                     // get context plugin by class, see plugin section below
    matchedPath()                         // get the path that was used to match this request (ex, "/hello/{name}")
    endpointHandlerPath()                 // get the path of the endpoint handler that was used to match this request
    cookieStore()                         // see cookie store section below
    skipRemainingHandlers()               // skip all remaining handlers for this request
 */
public class ServerSSL {

    final static private TS_Log d = TS_Log.of(ServerSSL.class);

    public static void main(String[] args) {
        var sslPlugin = new SslPlugin(cfg -> {
            cfg.keystoreFromClasspath("C:/dat/ssl/tomcat.jks", "MyPass");
            cfg.insecurePort = 80;
            cfg.securePort = 8443;
            cfg.sniHostCheck = false;
            cfg.tlsConfig = TlsConfig.INTERMEDIATE;
        });
        sslPlugin.reload(cfg -> {
            cfg.keystoreFromClasspath("C:/dat/ssl/tomcat.jks", "MyPass");
        });
        Javalin.create(cfg -> {

        });
        var app = Javalin.create(cfg -> {
            cfg.registerPlugin(sslPlugin);
            cfg.bundledPlugins.enableSslRedirects();
            cfg.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });
            cfg.staticFiles.add("C:/Users/me/Desktop/PDF");
        }).start();
        app.before(ctx -> {//also after
            // runs before all requests
        });
        app.beforeMatched("/path/*", ctx -> {//also after
            // runs before all matched requests (including static files)
            // runs before request to /path/*
        });
        app.get("/", ctx -> {

            var name = ctx.pathParam("name");
            var path = ctx.path();
            var matchedPath = ctx.matchedPath();
            ctx.result("name:" + name + ", path:" + path + "matchedPath:" + matchedPath);
        });

        app.exception(Exception.class, (e, ctx) -> {
            // handle general exceptions here
            // will not trigger if more specific exception-mapper found
        });
        app.error(404, ctx -> {
            ctx.result("Generic 404 message");
        });
        app.exception(FileNotFoundException.class, (e, ctx) -> {
            ctx.status(404);
        }).error(404, ctx -> {
            ctx.result("Generic 404 message");
        });
    }
//
//    private static Server configureHttp2Server(JavalinConfig config) {
//        Server server = new Server();
//
//        ServerConnector connector = new ServerConnector(server);
//        connector.setPort(8080);
//        server.addConnector(connector);
//
//        // HTTP Configuration
//        HttpConfiguration httpConfig = new HttpConfiguration();
//        httpConfig.setSendServerVersion(false);
//        httpConfig.setSecureScheme("https");
//        httpConfig.setSecurePort(8443);
//
//        // SSL Context Factory for HTTPS and HTTP/2
//        SslContextFactory sslContextFactory = new SslContextFactory();
//        sslContextFactory.setKeyStorePath(Main.class.getResource("/keystore.jks").toExternalForm()); // replace with your real keystore
//        sslContextFactory.setKeyStorePassword("password"); // replace with your real password
//        sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);
//        sslContextFactory.setProvider("Conscrypt");
//
//        // HTTPS Configuration
//        HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
//        httpsConfig.addCustomizer(new SecureRequestCustomizer());
//
//        // HTTP/2 Connection Factory
//        HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(httpsConfig);
//        ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
//        alpn.setDefaultProtocol("h2");
//
//        // SSL Connection Factory
//        SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory, alpn.getProtocol());
//
//        // HTTP/2 Connector
//        ServerConnector http2Connector = new ServerConnector(server, ssl, alpn, h2, new HttpConnectionFactory(httpsConfig));
//        http2Connector.setPort(8443);
//        server.addConnector(http2Connector);
//
//        return server;
//    }
}
