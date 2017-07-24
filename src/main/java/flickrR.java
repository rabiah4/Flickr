
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.people.User;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.SearchParameters;
import com.flickr4java.flickr.places.Place;
import com.flickr4java.flickr.places.PlacesList;
import com.flickr4java.flickr.tags.Tag;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

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
public class flickrR {

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

            String picID = pic.getId();
            Photo photo = flickr.getPhotosInterface().getPhoto(picID);
            User owner = photo.getOwner();
            String ownerID = owner.getId();
            String owner_loc = owner.getLocation();
            String owner_name = owner.getRealName();
            String placeID="";
            Collection<Tag> tags = photo.getTags();

            double photo_lat = photo.getGeoData().getLatitude();
            double photo_long = photo.getGeoData().getLongitude();
            int photo_acc = photo.getGeoData().getAccuracy();

            PlacesList<Place> place = flickr.getPlacesInterface().findByLatLon(photo_lat, photo_long, photo_acc);
            for (Place p : place) {
                 placeID = p.getPlaceId();
            }

           
            //check wether the user exists or not
            pst = con.prepareCall("SELECT count(*) AS total FROM User WHERE  UserID= (?)");
            pst.setString(1, ownerID);
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

                System.out.println("user ID = " + ownerID + "\n");
                pst = con.prepareCall("INSERT INTO User (UserID,Location,Name) VALUES (?,?,?) ");
                pst.setString(1, ownerID);
                pst.setString(2, owner_loc);
                pst.setString(3, owner_name);

                pst.execute();
            }

            if (picexists == 0) {
                System.out.println("pic ID = " + picID + "\n");
                pst = con.prepareCall("INSERT INTO PHOTO (PhotoID,UserID,PlaceID) VALUES (?,?,?)");
                pst.setString(1, picID);
                pst.setString(2, ownerID);
                pst.setString(3, placeID);
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
