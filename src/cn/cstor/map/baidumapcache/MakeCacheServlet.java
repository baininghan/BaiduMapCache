/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.cstor.map.baidumapcache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author dell
 */
public class MakeCacheServlet extends HttpServlet {
    
    private static final int sTimeout = 2000;

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
        response.setContentType("text/html;charset=UTF-8");
        	PrintWriter out = response.getWriter();
        //try (PrintWriter out = response.getWriter()) {
            //final String host_base= "_CSTOR_ADDRESS_"
            //final String host_base = request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
            
            final String host_base = "__CSTOR_ADDRESS__";
            final String app_base = request.getScheme() + "://" + host_base;
            
            
            final String api_url = "http://api.map.baidu.com/api?v=2.0&ak=BzL5c9UaDlph64beEDkDNzG3wrsNZ2b0";
            System.out.println("Getting: " + api_url);
            final String api_str = getString(api_url);

            final String new_api = api_str.replaceFirst("http://api.map.baidu.com", app_base + "/cache/api.map.baidu.com");

            Files.write(Paths.get(CacheServlet.sCachePath,"api_ori.js"), api_str.getBytes(StandardCharsets.UTF_8));
            Files.write(Paths.get(CacheServlet.sCachePath,"api_mod.js"), new_api.getBytes(StandardCharsets.UTF_8));
            
            final Pattern p1 = Pattern.compile("src=\"([^\"]*)\"");
            final Matcher m1 = p1.matcher(api_str);
            if ( ! m1.find() )
            {
                System.out.println("Didn't find what i was looking for");
                out.println("Didn't find next path");
                return;
            }

            final String script_1 = m1.group(1);
            System.out.println("Getting: " + script_1);
            final String script_1_str = getString(script_1);
            
            final String script_1_m01 = Pattern.compile("\"([^\"]*.map.bdimg.com)\"")
                                               .matcher(script_1_str)
                                               .replaceAll("\"" + host_base + "/cache/$1\"");

            final String script_1_m02 = Pattern.compile("\"http://([^\"]*.map.bdimg.com/images/)\"")
                                               .matcher(script_1_m01)
                                               .replaceAll("\"http://" + host_base + "/cache/$1\"");


            final String script_1_m03 = Pattern.compile("\"([^\"]*.baidu.com(:[^\"]+)?(/[^\"]*)?)\"")
                                               .matcher(script_1_m02)
                                               .replaceAll("\"" + host_base + "/cache/$1\"");

            final String script_1_m04 = Pattern.compile("https://([^.]*.baidu.com/)")
                                               .matcher(script_1_m03)
                                               .replaceAll("$1");
            
            final String script_1_m05 = Pattern.compile("\"([^\"]*.bdstatic.com/[^\"]+)\"")
                                               .matcher(script_1_m04)
                                               .replaceAll("\"" + host_base + "/cache/$1\"");
            
            final String script_1_mod = script_1_m05.replace("http://static.tieba.baidu.com", "static.tieba.baidu.com");

//            final String script_1_mod = Pattern.compile("\"([^\"]*.?map.baidu.com(:[^\"]+)?(/[^\"]*))\"")
//                                               .matcher(script_1_m02)
//                                               .replaceAll("\"" + host_base + "/cache/$1\"");
            
            Files.write(Paths.get(CacheServlet.sCachePath,"script_1_ori.js"), script_1_str.getBytes(StandardCharsets.UTF_8));
            Files.write(Paths.get(CacheServlet.sCachePath,"script_1_mod.js"), script_1_mod.getBytes(StandardCharsets.UTF_8));
            
            
            
            
            
            
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet MakeCacheServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet MakeCacheServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
       // }
    }
    
    private static String getString(final String url) throws MalformedURLException, IOException {
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
                System.out.println("Content-Type: " + con.getContentType());
                final int contentLength = con.getContentLength();
                if ( contentLength > 0 )
                {
                    final byte[] buffer = new byte[contentLength];
                    final int readed = readStream(con.getInputStream(), buffer, contentLength);

                    if ( readed > 0 )
                    {
                        return new String(buffer, 0, readed, StandardCharsets.UTF_8);
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
                        System.out.println("Data(chunked):");

                        return new String(buffer, 0, buffer.length, StandardCharsets.UTF_8);
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
        
        return null;
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

}
