package xcos_image;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
 
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.scilab.modules.javasci.Scilab;
 
/**
 * A Java servlet that handles file upload from client.
 * @author www.codejava.net
 */
public class Image extends HttpServlet {
    private static final long serialVersionUID = 1L;
 
    private static final int THRESHOLD_SIZE     = 1024 * 1024 * 3;  // 3MB
    private static final int MAX_FILE_SIZE      = 1024 * 1024 * 40; // 40MB
    private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 50; // 50MB
 
    /**
     * handles file upload via HTTP POST method
     */
    protected void doPost(HttpServletRequest req,
            HttpServletResponse res) throws ServletException, IOException {
        // checks if the request actually contains upload file
        if (!ServletFileUpload.isMultipartContent(req)) {
            PrintWriter writer = res.getWriter();
            writer.println("Request does not contain upload data");
            writer.flush();
            return;
        }
         
        // configures upload settings
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(THRESHOLD_SIZE);
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
         
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MAX_FILE_SIZE);
        upload.setSizeMax(MAX_REQUEST_SIZE);
         
        // constructs the directory path to store upload file
        String uploadPath = getServletContext().getInitParameter("file-upload");
        // creates the directory if it does not exist
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }
         
        try {
            // parses the request's content to extract file data
            List formItems = upload.parseRequest(req);
            Iterator iter = formItems.iterator();
             
            // iterates over form's fields
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();
                // processes only fields that are not form fields
                if (!item.isFormField()) {
                    String filePath = uploadPath + File.separator + "file.xcos";
                    File storeFile = new File(filePath);
                     
                    // saves the file on disk
                    item.write(storeFile);
                }
            }
            req.setAttribute("message", "Upload has been done successfully!");
            res.setContentType("image/gif");             // see different MIME type
            ServletOutputStream sos = res.getOutputStream();
            try {
                Scilab sci = new Scilab(true);
                sci.open();
                //File file = File.createTempFile(prefix, suffix);
                String command = "loadXcosLibs();importXcosDiagram('e:\\apache-tomcat-7.0.62\\webapps\\data\\file.xcos');xcos_simulate(scs_m, 4);xs2png(gcf(),'e:\\apache-tomcat-7.0.62\\webapps\\data\\ans.png')";  
                sci.exec(command);
            } catch (org.scilab.modules.javasci.JavasciException e) {
                System.err.println("Could not find variable type: " + e.getLocalizedMessage());
            }
             File f = new File("e:\\apache-tomcat-7.0.62\\webapps\\data\\ans.png");
             DataInputStream dis = new DataInputStream(new FileInputStream(f));
             byte[] barray = new byte[(int) f.length()];
             
             try 
             { 
               dis.readFully(barray);           // now the array contains the image
             }
             catch (Exception e) 
             { 
               barray = null; 
             }
             finally 
             { 
               dis.close( ); 
             }
             
             sos.write(barray);                 // send the byte array to client
             sos.close();
        } catch (Exception ex) {
            req.setAttribute("message", "There was an error: " + ex.getMessage());
        }
        //getServletContext().getRequestDispatcher("/message.jsp").forward(req, res);
    }
/*  public void service(HttpServletRequest req,HttpServletResponse res) throws ServletException, IOException 
  { 
	  
	  res.setContentType("image/gif");             // see different MIME type
      ServletOutputStream sos = res.getOutputStream();
      try {
          Scilab sci = new Scilab(true);
          sci.open();
          String command = "loadXcosLibs();importXcosDiagram('e:\\apache-tomcat-7.0.62\\webapps\\data\\file.xcos');xcos_simulate(scs_m, 4);xs2png(gcf(),'e:\\apache-tomcat-7.0.62\\webapps\\data\\ans.png')";  
          sci.exec(command);
      } catch (org.scilab.modules.javasci.JavasciException e) {
          System.err.println("Could not find variable type: " + e.getLocalizedMessage());
      }
       File f = new File("e:\\apache-tomcat-7.0.62\\webapps\\data\\ans.png");
       DataInputStream dis = new DataInputStream(new FileInputStream(f));
       byte[] barray = new byte[(int) f.length()];
       
       try 
       { 
         dis.readFully(barray);           // now the array contains the image
       }
       catch (Exception e) 
       { 
         barray = null; 
       }
       finally 
       { 
         dis.close( ); 
       }
       
       sos.write(barray);                 // send the byte array to client
       sos.close();
    
  }*/
}