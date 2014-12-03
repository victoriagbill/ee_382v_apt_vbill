// adapted from http://daly.axiom-developer.org/TimothyDaly_files/class3/node9.html
//  - original code doesn't handle posts, so i added a post handler
//  - original code doesn't handle port specified as argument


import java.io.*;
import java.net.*;
import java.util.*;

public class WebServer {
 
 public static void main(String args[]) { 
   ServerSocket sock;
   try { 
     sock = new ServerSocket( new Integer( args[0] ) );
     System.out.println("TSS Custom webserver started at " + new Date() );
     while(true) { 
       Socket socket = sock.accept();
       System.out.println("New connection accepted " +
           socket.getInetAddress() + ":" + socket.getPort());
       try { 
         getRequest request =  new getRequest(socket);
         Thread thread = new Thread(request);
         thread.start();
       }
       catch(Exception e) { 
         e.printStackTrace();
       }
     }
   }
   catch (Exception e) { 
     e.printStackTrace();
   }
 }
}
        
class getRequest implements Runnable { 
  String CRLF = "\r\n";
  Socket sock;
  InputStream input;
  OutputStream output;
  BufferedReader bin;

  public getRequest(Socket s) throws Exception { 
    this.sock = s;
    this.input = s.getInputStream();
    this.output = s.getOutputStream();
    this.bin= new BufferedReader(new InputStreamReader(s.getInputStream()));
  }

  public void run() { 
    // AA_begin
    int contentLength = 0;
    // AA_end

    while (true) { 
        try { 
            String GETline = bin.readLine();
            System.out.println("==>"+GETline);

            if ( (GETline.equals(CRLF) || GETline.equals("") ) )
                break;

            StringTokenizer s = new StringTokenizer(GETline);
            String temp = s.nextToken();
            //AA_begin
            if (temp.equals("POST")) {
                System.out.println("Got a POST request");
                String result = "";

                // Try to process payload
                while (true) { 
                    try { 
                        GETline = bin.readLine();
                        System.out.println("==>"+GETline);
                        // AA_begin
                        // find out if this tells us the length of the POST payload:
                        StringTokenizer t = new StringTokenizer(GETline);
                        if ( t.hasMoreTokens() && t.nextToken().equals( "Content-Length:") ) {
                            contentLength = new Integer( t.nextToken() );
                        }
                        if ( (GETline.equals(CRLF) || GETline.equals("") ) ) {

                            System.out.println("Post payload length is: " + contentLength );

                            // have to process payload one char at a time, cannot use readline since the request is
                            // not terminated with a newline (if we use a readline here, it
                            // hangs waiting for a newline or EOF)
                            String PostPayload = "";   
                            for( int i = 0 ; i < contentLength; i++ ) {
                                PostPayload += new Character( (char) bin.read() ).toString();
                            }

                            // For now, assume only name/value pair
                            String[] NameValue = PostPayload.split("=", 2);
                            String Name = NameValue[0];     //URLDecoder.decode(NameValue[0], "utf-8");
                            String Value = NameValue[1];    //URLDecoder.decode(NameValue[1], "utf-8");
                            String str = Name + " " + Value;
                            System.out.println(str);
                            CompileAndRun sc = new CompileAndRun();
                            result = sc.compileAndRun(Value);
                            break;

                            /*
                            PostPayload.replace('+', ' ');
                            String[] NameValuePairs = PostPayload.split("&");

                            for(int i =0; i < NameValuePairs.length ; i++)
                            {
                                String[] NameValue = NameValuePairs[i].split("=", 2);
                                String Name = URLDecoder.decode(NameValue[0], "utf-8");
                                String Value = URLDecoder.decode(NameValue[1], "utf-8");
                                String str = Name + " " + Value;
                                System.out.println(str);
                                CompileAndRun sc = new CompileAndRun();
                                result = sc.compileAndRun(Value);
                            }       
                            break;
                            */
                        } 
                     } catch (Exception e) { 
                         e.printStackTrace();
                     }
                }

                String serverln = "WebServer"+CRLF;
                String statusln = null;
                String contentTypeln = null;
                String contentLengthln = "error";
                String mime="text/html";

                statusln = "HTTP/1.0 200 OK" + CRLF ;
                contentTypeln = "Content-type: "+mime+CRLF;
                contentLengthln = "Content-Length: " +
                result.length() + CRLF;
                output.write(statusln.getBytes());
                System.out.print("<=="+statusln);
                output.write(serverln.getBytes());
                System.out.print("<=="+serverln);
                output.write(contentTypeln.getBytes());
                System.out.print("<=="+contentTypeln);
                output.write(contentLengthln.getBytes());
                System.out.print("<=="+contentLengthln);
                output.write(CRLF.getBytes());
                System.out.print("<=="+CRLF);
                System.out.flush();

                result = "<pre>" + result + "</pre>";

                output.write( result.getBytes() );
                break;
            }

            // AA_end
            if (temp.equals("GET")) {
          String filename = s.nextToken();
          filename = "./htdocs" + filename;
          FileInputStream inFile = null ;
          boolean fileExists = true ;
          try { 
            inFile = new FileInputStream( filename );
          } 
          catch ( FileNotFoundException e ) { 
            fileExists = false ;
          }
          String serverln = "WebServer"+CRLF;
          String statusln = null;
          String contentTypeln = null;
          String FourOFour = null;
          String contentLengthln = "error";
          String mime="text/html";

          if ( !(filename.endsWith(".html")) )
            mime="text/plain";

          if (fileExists) { 
            statusln = "HTTP/1.0 200 OK" + CRLF ;
            contentTypeln = "Content-type: "+mime+CRLF;
            contentLengthln = "Content-Length: " +
               (new Integer(inFile.available())).toString() + CRLF;
          }
          else { 
            statusln = "HTTP/1.0 404 Not Found" + CRLF ;
            contentTypeln = "Content-type: " + "text/html" + CRLF;
            FourOFour = 
             "<HTML>" + 
             " <HEAD>" +
             "  <TITLE>" +
             "   404 Not Found" +
             "  </TITLE>" +
             " </HEAD>" +
             " <BODY>"+
             "  404 Not Found "+filename+
             " </BODY>"+
             "</HTML>";
            contentLengthln = "Content-Length: " +
               (new Integer(FourOFour.length())).toString() + CRLF;
          }

          output.write(statusln.getBytes());
          System.out.print("<=="+statusln);
          output.write(serverln.getBytes());
          System.out.print("<=="+serverln);
          output.write(contentTypeln.getBytes());
          System.out.print("<=="+contentTypeln);
          output.write(contentLengthln.getBytes());
          System.out.print("<=="+contentLengthln);
          output.write(CRLF.getBytes());
          System.out.print("<=="+CRLF);
          System.out.flush();

          if (fileExists) { 
            byte[] buffer = new byte[1024] ;
            int bytes = 0 ;
            while ((bytes = inFile.read(buffer)) != -1 ) { 
              output.write(buffer, 0, bytes);
            }
            inFile.close();
            System.out.println("<== 200: file sent: "+filename);
          }
          else { 
            output.write(FourOFour.getBytes());
            System.out.println("<== 404: not found: "+filename);
          }
        }
      }
      catch (Exception e) { 
        e.printStackTrace();
      }
    }
    try { 
      output.close();
      bin.close();
      sock.close();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
}

