
import java.sql.Connection;
import java.sql.DriverManager;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author roro
 */
public class DBtest {
    
    public static void main(String[] args) {
     try
    {
         Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/FlickrDB","root","1214848");


System.out.println("connected with "+con.toString());


    }
    catch(Exception e)
    {
        System.out.println("not connect to server and message is"+e.getMessage());
    }
    
    
    }
    
}
