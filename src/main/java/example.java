
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.people.PeopleInterface;
import com.flickr4java.flickr.people.User;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.SearchParameters;
import com.flickr4java.flickr.tags.Tag;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Demonstrates the authentication-process.
 * <p>
 *
 * If you registered API keys, you find them with the shared secret at your
 * <a href="http://www.flickr.com/services/api/registered_keys.gne">list of API
 * keys</a>
 *
 * @author mago
 * @version $Id: AuthExample.java,v 1.6 2009/08/25 19:37:45 x-mago Exp $
 */
public class example {

    static double radius; // km
    static double latMin, latMax, longMax, longMin;
    public static Connection con;
    public static PreparedStatement pst = null;

    //findByLatLon
    public static void main(String[] args) throws FlickrException, SQLException {

        String apikey = "0cc8bfe44058b1286dbe34b347a0d2ec";
        String secret = "51e0865017204168";
        Flickr flickr = new Flickr(apikey, secret, new REST());

        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/FlickrDB?useSSL=false", "root", "1214848");
            System.out.println("connected with " + con.toString());

        } catch (Exception e) {
            System.out.println("not connect to server and message is" + e.getMessage());
        }

//        longMin,latMin,longMax,latMax
        BBox(48.8584, 2.2945);
        String minimum_longitude = Double.toString(longMin);
        String minimum_latitude = Double.toString(latMin);
        String maximum_longitude = Double.toString(longMax);
        String maximum_latitude = Double.toString(latMax);

        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setAccuracy(5);
        searchParameters.setBBox(minimum_longitude, minimum_latitude, maximum_longitude, maximum_latitude);

        PhotoList<Photo> list = flickr.getPhotosInterface().search(searchParameters, 0, 0);
        int userexists = -1;
        int picexists = -1;
        int tagexists = -1;

        int i = 1;
        for (Photo pic : list) {

            System.out.println("count : " + i++);
            String user = pic.getOwner().getId();
            String picID = pic.getId();
            User use = flickr.getPeopleInterface().getInfo(user);
            String user_loc = use.getLocation();
            String user_name = use.getRealName();
            Photo phototag = flickr.getTagsInterface().getListPhoto(picID);
            Collection<Tag> tags = phototag.getTags();
            System.out.println("picID : " + picID);
            System.out.println("tagss.toString( : " + tags.toString());


            //check wether the user exists or not
            pst = con.prepareCall("SELECT count(*) AS total FROM User WHERE  UserID= (?)");
            pst.setString(1, user);
            ResultSet res = pst.executeQuery();
            if (res.next()) {
                userexists = res.getInt("total");
                System.out.println("userexists" + userexists);
            }
            //check wether the pic exists or not
            pst = con.prepareCall("SELECT count(*) AS total FROM Photo WHERE  PhotoID= (?)");
            pst.setString(1, picID);
            ResultSet res2 = pst.executeQuery();
            if (res2.next()) {
                picexists = res2.getInt("total");
                System.out.println("picexists " + picexists);
            }

            if (userexists == 0) {

                System.out.println("user ID = " + user + "\n");
                pst = con.prepareCall("INSERT INTO User (UserID,Location,Name) VALUES (?,?,?) ");
                pst.setString(1, user);
                pst.setString(2, user_loc);
                pst.setString(3, user_name);

                pst.execute();
            }

            if (picexists == 0) {
                System.out.println("pic ID = " + picID + "\n");
                pst = con.prepareCall("INSERT INTO PHOTO (PhotoID,UserID) VALUES (?,?)");
                pst.setString(1, picID);
                pst.setString(2, user);
                pst.execute();
            }
                        for (Tag t : tags) {

                //check wether the tag exists or not
                pst = con.prepareCall("SELECT count(*) AS total FROM Tags WHERE  TagID= (?)");
                pst.setString(1, t.getId());
                ResultSet res3 = pst.executeQuery();
                if (res3.next()) {
                    tagexists = res3.getInt("total");
                    System.out.println("tagexists " + tagexists);

                    if (tagexists == 0) {
                        System.out.println("Tag ID = " + t.getId() + "\n");
                        pst = con.prepareCall("INSERT INTO Tags (TagID,PhotoID,TagValue) VALUES (?,?,?)");
                        pst.setString(1, t.getId());
                        pst.setString(2, picID);
                        pst.setString(3, t.getValue());
                        pst.execute();
                    }

                }
            }
        }

        //        for (Place place : placelist) {
        //            System.out.println("place name : " + place.getName());
        //            System.out.println("getPlace Id : " + place.getPlaceId());
        //            System.out.println("getWoeId : " + place.getWoeId());
        //            System.out.println("getPlaceUrl : " + place.getPlaceUrl());
        //            //System.out.println("toString : " + place.toString()); //returns null
        //
    }

    public static void BBox(double lon, double lat) {
        double R = 6371; // earth radius in km
        radius = 20; // km
        latMin = lon - Math.toDegrees(radius / R / Math.cos(Math.toRadians(lat)));
        latMax = lon + Math.toDegrees(radius / R / Math.cos(Math.toRadians(lat)));
        longMax = lat + Math.toDegrees(radius / R);
        longMin = lat - Math.toDegrees(radius / R);

    }

}
