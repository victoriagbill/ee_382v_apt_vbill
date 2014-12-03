import java.util.Date;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;

public class CompileAndRun {

    static void debug( String s ) { System.out.println("TSS Debug [" + (new Date()) + "] " + s); }


    public static String compileAndRun( String srcCode ) {


      String result = "";

      try {

        // Get the RunnableBars class as a string.
        String UserProgram = "";  
        FileInputStream fRunnableBars = new FileInputStream(new File("UserProgram_Template.java"));

        // SKAFIX: string builder example, append
        while (fRunnableBars.available() > 0) {

            UserProgram += new Character( (char) fRunnableBars.read() ).toString();
        }
        fRunnableBars.close();

        // Insert user program into RunnableBars class
        UserProgram = UserProgram.replace("UserProgram_Template", "UserProgram");
        UserProgram = UserProgram.replace("/*REPLACEME*/", srcCode);

        File f=new File("UserProgram.java");
        if(!f.exists())
        {
            f.createNewFile();
        }
        else
        {
            f.delete();
            f.createNewFile();
        }

        FileOutputStream fop=new FileOutputStream(f);
        fop.write(UserProgram.getBytes());
        fop.flush();
        fop.close();

        // TODO: platform dependency
        // for windows
        // String[] cmd1 = new String[2];
        // cmd1[0] = "C:\\Program Files\\Java\\jdk1.6.0_18\\bin\\javac.exe";
        // cmd1[1] = "C:\\home\\workspace\\jTrade\\salim-ms\\UserProgram.java";
        // for linux
        String[] cmd1 = { "javac", "UserProgram.java" };

        Runtime run = Runtime.getRuntime();
        // TOOD: platform dependency
        String [] envp = { "CLASSPATH=/home/adnan/TradingSoftwareSystem/" };
        Process pr = run.exec(cmd1, envp);
        pr.waitFor();
        debug("performed compile of user-supplied code");

        String line = "";
        BufferedReader stderr = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

        while ((line=stderr.readLine())!=null)
        {
          result += line + "\n";
          debug(result);
        }

        BufferedReader stdin = new BufferedReader(new InputStreamReader(pr.getInputStream()));

        while ((line=stdin.readLine())!=null)
        {
          result += line + "\n";
        }

        // Exit early if the compile failed
        if (pr.exitValue() != 0)
            return result;

        // Execute the SampleRun system
        
        // TODO - platform dependency
        // following is appropriate for Windows platforms
        // String[] cmd2 = new String[4];
        // cmd2[0] = "C:\\Program Files\\Java\\jdk1.6.0_18\\bin\\java.exe";
        // cmd2[0] = "java";
        // cmd2[1] = "-cp";
        // cmd2[2] = ".;lib\\*;";
        // cmd2[3] = "SystemRun";
        // String[] env2 = new String[1];
        // env2[0] = "";
        // for linux
        String [] cmd2 = {"java", "-cp", "lib/sqlitejdbc-v056.jar:lib/ta-lib-0.4.0.jar:.", "SystemRun"};
        String [] env2 = {"CLASSPATH=."};

        // TODO - platform dependency
        // set startdir as appropriate
        // following is appropriate for Windows platforms
        // File startdir=new File("C:\\home\\workspace\\jTrade\\salim-ms\\");
        File startdir=new File(".");

        debug("starting to execute user strategy, this can take 10s of seconds.");
        pr = run.exec(cmd2, env2, startdir);
        pr.waitFor();
        debug("done executing user strategy");

        // SKAFIX: argh. this doesn't work when there is no error
        //stderr = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
        //while ((line=stderr.readLine())!=null)
        //{
        //  result += line + "\n";
        //}
        stdin = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        while ((line=stdin.readLine())!=null)
        {
          result += line + "\n";
          debug( line );
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      return result;
    }

    private static String readFileAsString(String filePath) throws java.io.IOException{
       byte[] buffer = new byte[(int) new File(filePath).length()];
       BufferedInputStream f = new BufferedInputStream(new FileInputStream(filePath));
       f.read(buffer);
       return new String(buffer);
    }

    public static void main( String [] args ) {
      try {
        String result = compileAndRun( readFileAsString("run.txt" ) );
        System.out.println(result);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
}
