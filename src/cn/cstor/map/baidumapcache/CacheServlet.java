package cn.cstor.map.baidumapcache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author dell
 */
public class CacheServlet extends HttpServlet {

	//windows
	public static final String sCachePath = "D:\\baiducache";
	//linux
	//public static final String sCachePath = "/cVideo/baiducache";

    @Override
    public void init() throws ServletException {
        super.init(); //To change body of generated methods, choose Tools | Templates.
        
        final File cachedir = new File(sCachePath);
        if ( ! cachedir.exists() )
        {
            cachedir.mkdirs();
        }
        
        final File tilesdir = new File(sCachePath, "tiles");
        if ( ! tilesdir.exists() )
        {
            tilesdir.mkdirs();
        }
    }
    
    
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
    	final String host_base = request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    	
        final String path_info = request.getPathInfo();
        
        if ( path_info.endsWith("map.bdimg.com/images/blank.gif") )
        {
            response.sendError(404, "Not here");
        }
        else if ( path_info.endsWith("map.bdimg.com/customimage/tile") )
        {
            send_tile(request, response);
        }
        else if ( path_info.endsWith("map.bdimg.com/getmodules") )
        {
            send_modules(request, response);
        }
/*        else switch ( path_info )
        {
            case "/api.map.baidu.com/api":
                send_specialjsfile(response, "api_mod.js", host_base);
                break;
                
            case "/api.map.baidu.com/getscript":
                send_specialjsfile(response, "script_1_mod.js", host_base);
                break;
                
            default:
                response.sendError(404, "Not here");
        }*/
        else if("/api.map.baidu.com/api".equals(path_info)){
        	 send_specialjsfile(response, "api_mod.js", host_base);
        }else if("/api.map.baidu.com/getscript".equals(path_info)){
        	send_specialjsfile(response, "script_1_mod.js", host_base);
	    }else{
	    	response.sendError(404, "Not here");
	    }
        
//        response.setContentType("text/html;charset=UTF-8");
//        try (PrintWriter out = response.getWriter()) {
//            
//            final String app_base = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
//            
//            /* TODO output your page here. You may use following sample code. */
//            out.println("<!DOCTYPE html>");
//            out.println("<html>");
//            out.println("<head>");
//            out.println("<title>Servlet2 Cache</title>");            
//            out.println("</head>");
//            out.println("<body>");
//            out.println("<p>Request URL: " + request.getRequestURL()+ "</p>");
//            out.println("<p>App base: " + app_base + "</p>");
//            
//            out.println("<p>Servlet Name: " + request.getServerName() + "</p>");
//            out.println("<p>Context Path: " + request.getContextPath() + "</p>");
//            out.println("<p>Servlet Path: " + request.getServletPath() + "</p>");
//            
//            final String base_path = request.getContextPath() + request.getServletPath() + "/";
//            out.println("<p>Resulting Base path: " + base_path + "</p>");
//            
//            final String path_info = request.getPathInfo();
//            out.println("<p>Path Info: " + path_info + "</p>");
//            out.println("<p>Query String: " + request.getQueryString()+ "</p>");
//
//            final String dest_host = path_info.substring(1, path_info.indexOf("/", 1));
//            out.println("<p>Destination Host: " + dest_host + "</p>");
//            
//            
//            
//            
//            
//            
//            out.println("</body>");
//            out.println("</html>");
//        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private static void send_modules(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String mods = request.getParameter("mod").replace(',', '_');
        
        final String modules_name = mods + ".js";
        final Path path = Paths.get(sCachePath, modules_name);
        final File file = path.toFile();
        
        if ( ! file.exists() )
        {
            final String url = "http:/" + request.getPathInfo() + "?" + request.getQueryString();
            if ( ! get_jsfile(url, path) )
            {
                response.sendError(500, "Couldn't get modules");
                return;
            }
        }
        
        response.setContentType("text/javascript;charset=utf-8");
        
        final long len = file.length();
        if ( len > 0 )
        {
            response.setContentLength((int)len);
            Files.copy(path , response.getOutputStream());
        }
        else
        {
            response.setStatus(204); // no content;
        }
        
    }
    
    private static void send_specialjsfile(HttpServletResponse response, String name, String host_base) throws IOException {
        final Path p1 = Paths.get(sCachePath, name);
        final File f = p1.toFile();
        if ( ! f.exists() )
        {
            response.sendError(404, "doesn't exist");
            return;
        }

        response.setContentType("text/javascript;charset=utf-8");
        
        final String orig = new String(Files.readAllBytes(p1), StandardCharsets.UTF_8);
        final String modi = orig.replaceAll("__CSTOR_ADDRESS__", host_base);
        final byte[] data = modi.getBytes(StandardCharsets.UTF_8);
        
        final long len = data.length;
        if ( len > 0 )
        {
            response.setContentLength((int)len);
            response.getOutputStream().write(data);
        }
        else
        {
            response.setStatus(204); // no content;
        }
    }
    
    private static void send_jsfile(HttpServletResponse response, String name) throws IOException {
    	final Path p1 = Paths.get(sCachePath, name);
    	final File f = p1.toFile();
    	if ( ! f.exists() )
    	{
    		response.sendError(404, "doesn't exist");
    		return;
    	}
    	
    	response.setContentType("text/javascript;charset=utf-8");
    	
    	final long len = f.length();
    	if ( len > 0 )
    	{
    		response.setContentLength((int)len);
    		Files.copy(p1 , response.getOutputStream());
    	}
    	else
    	{
    		response.setStatus(204); // no content;
    	}
    }

    private static void send_tile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String x = request.getParameter("x");
        final String y = request.getParameter("y");
        final String z = request.getParameter("z");
        final String style = request.getParameter("customid");
        
        final String tilename = style + "_" + z + "_" + x + "_" + y + ".jpeg";
        final Path path = Paths.get(sCachePath, "tiles", tilename);
        final File file = path.toFile();
        
        if ( ! file.exists() )
        {
            if ( ! get_tile(request, path) )
            {
                response.sendError(500, "Couldn't get tile");
                return;
            }
        }
        
        response.setContentType("image/jpeg");
        
        final long len = file.length();
        if ( len > 0 )
        {
            response.setContentLength((int)len);
            Files.copy(path , response.getOutputStream());
        }
        else
        {
            response.setStatus(204); // no content;
        }
    }
    
    private static boolean get_tile(HttpServletRequest request, Path dest) {
        
        final String url = "http:/" + request.getPathInfo() + "?" + request.getQueryString();
        try
        {
            getFile(url, dest);
        }
        catch (IOException ex)
        {
            Logger.getLogger(CacheServlet.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }

    private static final int sTimeout = 2000;
    
    private static boolean get_jsfile(final String url, final Path dest) throws MalformedURLException, IOException {
        final URL u = new URL(url);
        HttpURLConnection con = (HttpURLConnection)u.openConnection();
        con.setConnectTimeout(sTimeout);
        con.setReadTimeout(sTimeout);
        con.connect();
        
        int response_code;
        try
        {
            if ( (response_code = con.getResponseCode()) == 200 )
            {
                final int contentLength = con.getContentLength();
                if ( contentLength > 0 )
                {
                    final byte[] buffer = new byte[contentLength];
                    final int readed = readStream(con.getInputStream(), buffer, contentLength);

                    if ( readed > 0 )
                    {
                        Files.write(dest, buffer);
                        return true;
                    }
                    else
                    {
                        System.out.println("no data js");
                    }
                }
                else if ( "chunked".equals(con.getHeaderField("Transfer-Encoding")) )
                {
                    final byte[] buffer = readChunkedStream(con.getInputStream());
                    if ( buffer.length > 0 )
                    {
//                        System.out.println("Data(chunked):");
                        Files.write(dest, buffer);
                        return true;
                    }
                    else
                    {
                        System.out.println("no data js (chunked)");
                    }
                }
            }
            else
            {
                System.out.println("Readed was not 200 js:" + response_code);
            }
        }
        finally
        {
//            con.disconnect();
        }
        
        return false;
    }
    
    private static boolean getFile(final String url, final Path dest) throws MalformedURLException, IOException {
        final URL u = new URL(url);
        HttpURLConnection con = (HttpURLConnection)u.openConnection();
        con.setConnectTimeout(sTimeout);
        con.setReadTimeout(sTimeout);
        con.connect();
        
        int response_code;
        try
        {
            if ( (response_code = con.getResponseCode()) == 200 )
            {
                final int contentLength = con.getContentLength();
                if ( contentLength > 0 )
                {
                    final byte[] buffer = new byte[contentLength];
                    final int readed = readStream(con.getInputStream(), buffer, contentLength);

                    if ( readed > 0 )
                    {
                        Files.write(dest, buffer);
                        return true;
                    }
                    else
                    {
                        System.out.println("no data");
                    }
                }
                else if ( "chunked".equals(con.getHeaderField("Transfer-Encoding")) )
                {
                    final byte[] buffer = readChunkedStream(con.getInputStream());
                    if ( buffer.length > 0 )
                    {
//                        System.out.println("Data(chunked):");
                        Files.write(dest, buffer);
                        return true;
                    }
                    else
                    {
                        System.out.println("no data(chunked)");
                    }
                }
            }
            else
            {
                System.out.println("Readed was not 200:" + response_code);
            }
        }
        finally
        {
//            con.disconnect();
        }
        
        return false;
    }
    
    private static int readStream(final InputStream inputStream,
                                  final byte[] buffer,
                                  final int length )
            throws IOException {
        try
        {
            int pos = 0;
            do
            {
                final int readed = inputStream.read(buffer, pos, length - pos);
                if ( readed == -1 )
                    break;

                pos += readed;
            } while ( pos < length );
            
            return pos;
        }
        finally
        {
            try { inputStream.close(); } catch (Exception ex) {}
        }
    }
    
    private static byte[] readChunkedStream(final InputStream inputStream) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        final byte[] buffer = new byte[1024];
        int length;
        
        while ( (length = inputStream.read(buffer)) != -1)
            bos.write(buffer, 0, length);
        
        return bos.toByteArray();
    }
    
    private static String extension_to_content_type(final String extension) {
/*        switch (extension)
        {
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "cur": return "application/octet-stream";
        }*/
        if("png".equals(extension)){
        	 return "image/png";
        }else if("gif".equals(extension)){
        	 return "image/gif";
        }else if("cur".equals(extension)){
        	 return "application/octet-stream";
        }
        return "application/octet-stream";
    }
    
}
